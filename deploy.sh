#!/bin/bash

# --- 1. 변수 설정 ---
EC2_HOST="13.124.105.243"
EC2_USER="ubuntu"
SSH_KEY_PATH="C:/Users/seram/OneDrive/문서(OneDrive)/2025-2학기/캡디2/my-ec2-key.pem"
REMOTE_APP_DIR="/home/${EC2_USER}/ppt-backend"
GIT_REPO_URL="git@github.com:hye2021/ppt-backend.git"
GIT_BRANCH="main"
# DEPLOY_KEY_PATH는 EC2 내에서 사용될 Deploy Key 개인 키 경로입니다.
# EC2에 /home/ubuntu/.ssh/d_ed25519 파일이 있어야 합니다.
DEPLOY_KEY_PATH="/home/${EC2_USER}/.ssh/d_ed25519"

# --- 2. 로컬 Git 리포지토리 업데이트 (선택 사항) ---
echo "1. (Optional) Fetching latest code from Git locally..."
git pull origin main
if [ $? -ne 0 ]; then
    echo "Local Git pull failed. Continuing with deployment."
fi


# --- 3. EC2 인스턴스에 SSH 접속하여 Git Pull 및 Docker Compose 실행 ---
echo "2. Connecting to EC2 and deploying with Git Pull & Docker Compose..."
ssh -i "${SSH_KEY_PATH}" "${EC2_USER}@${EC2_HOST}" << EOF
  echo "--- Inside EC2 ---"

  # ssh-agent 설정 (이전 로그에서 'Error connecting to agent'가 나왔지만, Git Pull 성공 여부는 별개일 수 있음)
  eval "$(ssh-agent -s)" || echo "Warning: ssh-agent could not be started or configured."
  ssh-add "${DEPLOY_KEY_PATH}" || echo "Warning: ssh-add failed. Ensure Deploy Key is correctly configured and permissions are 600."

  # 프로젝트 디렉토리로 이동하여 Git 리포지토리를 처리합니다.
  if [ ! -d "${REMOTE_APP_DIR}/.git" ]; then # .git 디렉토리가 없다면 (최초 배포)
    echo "Git repository not found on EC2. Cloning into $(basename "${REMOTE_APP_DIR}")..."
    cd $(dirname "${REMOTE_APP_DIR}")
    git clone "${GIT_REPO_URL}" $(basename "${REMOTE_APP_DIR}")
    if [ $? -ne 0 ]; then
        echo "Git clone failed on EC2. Exiting remote script."
        exit 1
    fi
    cd "${REMOTE_APP_DIR}"
  else # .git 디렉토리가 이미 있다면 (재배포)
    echo "Git repository already exists. Forcing pull latest code from origin/${GIT_BRANCH}..."
    cd "${REMOTE_APP_DIR}"
    git fetch origin "${GIT_BRANCH}" # 원격의 최신 정보를 가져옵니다.
    git reset --hard origin/"${GIT_BRANCH}" # 로컬을 원격과 동일하게 강제 재설정 (divergent branches 문제 해결)
    if [ $? -ne 0 ]; then
        echo "Git reset --hard failed on EC2. Exiting remote script."
        exit 1
    fi
  fi

  # ssh-agent 종료 (보안을 위해 필요 없으면 실행 중인 에이전트를 종료합니다.)
  ssh-agent -k || echo "Warning: ssh-agent could not be killed."

  # Docker Compose를 사용하여 애플리케이션 배포
  echo "Stopping existing containers..."
  docker-compose down # 기존 컨테이너 중지 및 삭제 (볼륨 데이터는 유지)

  echo "Building new images (including JAR build inside Dockerfile)..."
  # Dockerfile 구문 오류 해결 (주석을 ENV 라인 위로 이동했는지 확인!)
  docker-compose build --no-cache

  echo "Starting new containers..."
  docker-compose up -d

  echo "Docker Compose deployment complete."
  echo "Check logs with 'docker-compose logs -f'"
  echo "--- Exiting EC2 ---"
EOF

# --- 4. 배포 완료 메시지 ---
echo "Deployment process completed successfully!"
echo "You can check the application status by accessing http://${EC2_HOST}"