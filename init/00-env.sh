#!/bin/bash
set -e

sudo apt update -y
sudo apt install -y locales tzdata ca-certificates gnupg software-properties-common

sudo locale-gen en_US.UTF-8 >/dev/null
