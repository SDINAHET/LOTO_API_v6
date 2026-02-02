#!/bin/bash
set -e

cd "$(dirname "$0")"
sudo apt update -y
sudo xargs -a apt.java.txt apt install -y

# JAVA_HOME (optionnel mais propre)
JAVA_HOME_PATH="/usr/lib/jvm/java-21-openjdk-amd64"
if ! grep -q "JAVA_HOME" ~/.bashrc; then
  echo "export JAVA_HOME=${JAVA_HOME_PATH}" >> ~/.bashrc
  echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
fi
