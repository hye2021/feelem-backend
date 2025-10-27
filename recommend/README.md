.venv\Scripts\activate  
uvicorn app.main:app --reload --port 8080
curl http://127.0.0.1:8080/health

pip install -r requirements.txt
