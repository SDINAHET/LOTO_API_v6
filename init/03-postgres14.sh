#!/bin/bash
set -e

cd "$(dirname "$0")"

# Ajout repo officiel PostgreSQL (PGDG) pour avoir 14 sur Ubuntu
sudo install -d /usr/share/postgresql-common/pgdg
sudo curl -fsSL https://www.postgresql.org/media/keys/ACCC4CF8.asc \
  | sudo gpg --dearmor -o /usr/share/postgresql-common/pgdg/apt.postgresql.org.gpg

echo "deb [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.gpg] http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" \
  | sudo tee /etc/apt/sources.list.d/pgdg.list >/dev/null

sudo apt update -y
sudo xargs -a apt.postgres.txt apt install -y

sudo service postgresql start
