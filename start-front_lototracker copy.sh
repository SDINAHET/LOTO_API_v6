#!/bin/bash

# Stop on error
set -e

echo "🚀 Démarrage Spring Boot..."
mvn spring-boot:run &

# Attendre un peu que le backend démarre
sleep 5

echo "🌐 Démarrage serveur HTTP Node.js (static)..."
cd src/main/resources/static

npx http-server -a 0.0.0.0 -p 5500
