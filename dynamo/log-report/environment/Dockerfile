FROM public.ecr.aws/docker/library/python:3.13-slim-bookworm@sha256:6771159cd4fa5d9bba1258caf0b82e6b73458c694d178ad97c5e925c2d0e1a91

WORKDIR /app

RUN pip install --no-cache-dir pytest==8.4.1 pytest-json-ctrf==0.5.3

COPY app/service.log /app/service.log
COPY tests /tests

RUN chmod +x /tests/test.sh
