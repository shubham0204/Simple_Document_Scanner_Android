# syntax=docker/dockerfile:1

FROM python:3.10-slim-buster
WORKDIR app/
COPY requirements.txt requirements.txt
RUN pip3 install -r requirements.txt
COPY . .
CMD [ "python3" , "-m" , "uvicorn" , "main:app" , "--port" , "8080" ]
CMD [ "python3" , "-m" , "streamlit" , "run" , "app.py" , "--server.port" , "8080" ]
