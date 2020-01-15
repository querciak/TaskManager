FROM ubuntu:18.04

RUN apt-get update && \
    apt-get install -y sudo bash curl unzip

RUN curl https://rclone.org/install.sh | sudo bash

COPY rclone.conf /root/.config/rclone/rclone.conf
