#!/bin/bash

# --- 1. 변수 설정 ---
# !!!주의!!! 아래 값들을 여러분의 실제 환경에 맞게 변경해야 합니다.
# 이 스크립트가 로컬 PC에서 EC2에 접속하기 위한 정보입니다.
EC2_HOST="13.124.105.243"
EC2_USER="ubuntu"
SSH_KEY_PATH="C:/Users/seram/OneDrive/문서(OneDrive)/2025-2학기/캡디2/main.pem"

# 이 스크립트가 EC2에서 GitHub 리포지토리에 접근하기 위한 정보입니다.
#예: /home/ubuntu/ppt-backend
REMOTE_APP_DIR="/home/${EC2_USER}/ppt-backend"

# GIT_REPO_URL: GitHub Private Repository의 SSH URL입니다.
GIT_REPO_URL="git@github.com:hye2021/ppt-backend.git" 
GIT_BRANCH="main"

# DEPLOY_KEY_PATH: EC2 인스턴스 내에 저장된 GitHub Deploy Key 개인 키 파일의 전체 경로입니다.
DEPLOY_KEY_PATH="/home/${EC2_USER}/.ssh/id_ed25519" # <--- Deploy Key 개인 키 파일 경로


# --- 2. 로컬 Git 리포지토리 업데이트 (선택 사항) ---
echo "1. (Optional) Fetching latest code from Git locally..."
# 이 명령어는 로컬 PC의 현재 디렉토리(ppt-backend)에서 GitHub로부터 최신 코드를 가져옵니다.
# 로컬 개발 환경의 코드도 최신으로 유지하기 위함이며, 배포 자체와는 직접적인 관계는 없습니다.
git pull origin main
if [ $? -ne 0 ]; then
    echo "Local Git pull failed. Continuing with deployment."
    # 로컬 pull 실패가 배포를 막지는 않으므로 스크립트 종료하지 않습니다.
fi


# --- 3. EC2 인스턴스에 SSH 접속하여 Git Pull 및 Docker Compose 실행 ---
echo "2. Connecting to EC2 and deploying with Git Pull & Docker Compose..."
# ssh 명령을 통해 로컬 PC에서 EC2 인스턴스로 접속하고, 이후 'EOF'와 'EOF' 사이의 모든 명령어를 EC2에서 실행합니다.
ssh -i "${SSH_KEY_PATH}" "${EC2_USER}@${EC2_HOST}" << EOF
  echo "--- Inside EC2 ---" # EC2 내부에서 실행되고 있음을 알리는 메시지

  # Git 명령이 Deploy Key를 사용하도록 SSH 에이전트 설정
  # ssh-agent는 SSH 개인 키를 메모리에 로드하여, 키를 반복적으로 입력하지 않고도
  # Git과 같은 SSH 기반 서비스에 자동으로 인증할 수 있도록 해주는 프로그램입니다.
  eval "$(ssh-agent -s)" # ssh-agent를 백그라운드에서 시작하고 필요한 환경 변수를 설정합니다.

  # Deploy Key 개인 키를 ssh-agent에 등록합니다.
  # 이제 이후의 git 명령들은 이 키를 사용하여 GitHub에 인증을 시도합니다.
  ssh-add "${DEPLOY_KEY_PATH}"

  # 프로젝트 디렉토리로 이동하여 Git 리포지토리를 처리합니다.
  # -d "${REMOTE_APP_DIR}/.git": ${REMOTE_APP_DIR} 경로에 .git 디렉토리가 존재하는지 확인합니다.
  if [ ! -d "${REMOTE_APP_DIR}/.git" ]; then # .git 디렉토리가 없다면 (최초 배포)
    # 클론 작업을 위해 ${REMOTE_APP_DIR}의 부모 디렉토리로 이동합니다.
    # $(dirname "${REMOTE_APP_DIR}")는 "/home/ubuntu"가 됩니다.
    cd $(dirname "${REMOTE_APP_DIR}")
    echo "Cloning Git repository into $(basename "${REMOTE_APP_DIR}")..."
    # GitHub 리포지토리를 EC2의 ${REMOTE_APP_DIR} 경로로 클론합니다.
    # $(basename "${REMOTE_APP_DIR}")는 "ppt-backend"이므로, "/home/ubuntu/ppt-backend"로 클론됩니다.
    # 이때 ssh-agent에 등록된 Deploy Key를 사용하여 GitHub에 인증합니다.
    git clone "${GIT_REPO_URL}" $(basename "${REMOTE_APP_DIR}")
    if [ $? -ne 0 ]; then # Git clone 명령이 실패했는지 확인
        echo "Git clone failed on EC2. Exiting remote script."
        exit 1 # 스크립트를 즉시 종료합니다.
    fi
    cd "${REMOTE_APP_DIR}" # 클론된 디렉토리로 다시 이동합니다.
  else # .git 디렉토리가 이미 있다면 (이전 배포 이력이 있는 경우, 재배포)
    echo "Git repository already exists. Pulling latest code..."
    cd "${REMOTE_APP_DIR}" # 프로젝트 디렉토리로 이동합니다.
    # 최신 코드를 GitHub로부터 가져옵니다. 이때도 ssh-agent에 등록된 Deploy Key로 인증합니다.
    git pull origin "${GIT_BRANCH}"
    if [ $? -ne 0 ]; then # Git pull 명령이 실패했는지 확인
        echo "Git pull failed on EC2. Exiting remote script."
        exit 1 # 스크립트를 즉시 종료합니다.
    fi
  fi

  # ssh-agent 종료 (보안을 위해 필요 없으면 실행 중인 에이전트를 종료합니다.)
  ssh-agent -k

  # Docker Compose를 사용하여 애플리케이션 배포
  echo "Stopping existing containers..."
  # 기존에 실행 중이던 모든 Docker 컨테이너를 중지하고 삭제합니다. (볼륨 데이터는 유지)
  docker-compose down 

  echo "Building new images (including JAR build inside Dockerfile)..."
  # Dockerfile을 사용하여 새로운 이미지를 빌드합니다.
  # --no-cache: 캐시를 사용하지 않고 항상 새로 빌드하여 최신 코드가 반영되도록 합니다.
  docker-compose build --no-cache 

  echo "Starting new containers..."
  # 모든 Docker 서비스를 백그라운드(-d)에서 실행합니다.
  docker-compose up -d 

  echo "Docker Compose deployment complete."
  echo "Check logs with 'docker-compose logs -f'"
  echo "--- Exiting EC2 ---"
EOF

# --- 4. 배포 완료 메시지 ---
echo "Deployment process completed successfully!"
# 웹 브라우저에서 애플리케이션에 접속할 수 있는 URL을 안내합니다.
# EC2 보안 그룹에서 80번 포트가 열려 있으므로 포트 번호를 명시할 필요가 없습니다.
echo "You can check the application status by accessing http://${EC2_HOST}"