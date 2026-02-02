#!/bin/bash
set -e

cd "$(dirname "$0")"
sudo apt update -y
sudo xargs -a apt.base.txt apt install -y
