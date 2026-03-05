Dockerfile.ai

```yml
FROM python:3.11-slim

WORKDIR /app

# deps minimales
RUN pip install --no-cache-dir fastapi uvicorn[standard] httpx pymongo

COPY ai.py /app/ai.py

EXPOSE 8091
CMD ["uvicorn", "ai:app", "--host", "0.0.0.0", "--port", "8091"]
```

Docker-compose.yml
```yml
version: "3.8"

services:
  backend:
    build:
      context: .
      dockerfile: Dockerfile.backend
    ports:
      - "8082:8082"
    depends_on:
      postgres:
        condition: service_healthy
      mongodb:
        condition: service_started
    environment:
      SPRING_PROFILES_ACTIVE: ci
      TZ: Europe/Paris
      JAVA_TOOL_OPTIONS: "-Duser.timezone=Europe/Paris"

      # Mongo
      SPRING_DATA_MONGODB_URI: mongodb://mongodb:27017/lotodb

      # Postgres
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/lotodb
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres

  frontend:
    build:
      context: .
      dockerfile: Dockerfile.frontend
    ports:
      - "5500:5500"
    depends_on:
      - backend

  mongodb:
    image: mongo:latest
    restart: always
    ports:
      - "27018:27017"
    environment:
      MONGO_INITDB_DATABASE: lotodb
      TZ: Europe/Paris
    volumes:
      - mongodb_data:/data/db

  postgres:
    image: postgres:14
    restart: always
    ports:
      - "5434:5432"
    environment:
      POSTGRES_DB: lotodb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      TZ: Europe/Paris
      PGTZ: Europe/Paris
    volumes:
      - ./docker/postgres/init:/docker-entrypoint-initdb.d
      # (optionnel) persistance réelle :
      # - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d lotodb"]
      interval: 10s
      timeout: 5s
      retries: 20

  # =========================
  # AI SERVICE (OPTIONNEL)
  # =========================
  ai:
    image: python:3.11-slim
    profiles: ["ai"]   # ✅ ne démarre pas par défaut
    restart: unless-stopped
    working_dir: /app
    volumes:
      - ./ai.py:/app/ai.py:ro
    environment:
      TZ: Europe/Paris

      # Mongo
      MONGO_URI: mongodb://mongodb:27017
      MONGO_DB: lotodb
      MONGO_COL: historique

      # Ollama
      OLLAMA_BASE: http://ollama:11434

      # ✅ IMPORTANT: modèle léger pour 8 Go
      # Très bon compromis :
      OLLAMA_MODEL: llama3.1:8b
      # Très bon compromis :
      # OLLAMA_MODEL: llama3.2:3b
      # Alternative légère :
      # OLLAMA_MODEL: phi3:mini

      # ✅ évite les longues phrases qui explosent:
      MAX_MESSAGE_LEN: "900"
      OLLAMA_TIMEOUT_SEC: "360"

      # (optionnel) si tu ajoutes une logique AI_ENABLED dans ai.py plus tard
      # AI_ENABLED: "1"
    command: >
      sh -c "pip install --no-cache-dir fastapi uvicorn httpx pymongo &&
             uvicorn ai:app --host 0.0.0.0 --port 8091"
    ports:
      - "8091:8091"
    depends_on:
      - mongodb
      - ollama

  ollama:
    image: ollama/ollama:latest
    profiles: ["ai"]   # ✅ ne démarre pas par défaut
    restart: unless-stopped
    ports:
      - "11435:11434"
    volumes:
      - ollama_data:/root/.ollama
    # ✅ Limites mémoire: "deploy:" n'est pas appliqué par docker compose classique.
    # Utilise plutôt mem_limit.
    mem_limit: 16g

volumes:
  mongodb_data:
  postgres_data:
  ollama_data:
```

docker compose --profile ai up --build -d
curl -s http://127.0.0.1:11435/api/tags
docker exec -it loto_api_v6-ollama-1 ollama pull llama3.1:8b
docker exec -it loto_api_v6-ollama-1 ollama list
curl -s -X POST http://127.0.0.1:8091/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Quelles erreurs éviter en jouant au Loto ?"}'



Arrêt / Redémarrage
Stop (sans supprimer)
docker compose --profile ai stop
Start (après stop)
docker compose --profile ai start
Restart
docker compose --profile ai restart ai ollama
Down (stop + supprime containers réseau)
docker compose --profile ai down
Down + suppression volumes (⚠️ efface données persistées)
docker compose --profile ai down -v
Nettoyage (optionnel)
Supprimer images non utilisées
docker image prune -f
Supprimer tout ce qui traîne (dangereux si tu veux garder tes caches)
docker system prune -af


Lancement du docker et installation du LLM
```bash
root@UID7E:/mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6# docker compose --profile ai up --build -d
WARN[0000] /mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6/docker-compose.yml: the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion
[+] Running 10/10
 ✔ ai Pulled                                                                                                                                                                                                                                            12.7s
   ✔ 206356c42440 Pull complete                                                                                                                                                                                                                          8.4s
   ✔ 13159fd0b051 Pull complete                                                                                                                                                                                                                          8.7s
   ✔ 269d3f7471e2 Pull complete                                                                                                                                                                                                                         10.4s
   ✔ 28c7e2bc4784 Pull complete                                                                                                                                                                                                                         10.4s
 ✔ ollama Pulled                                                                                                                                                                                                                                       141.0s
   ✔ 01d7766a2e4a Pull complete                                                                                                                                                                                                                          5.1s
   ✔ 3f9c2f409377 Pull complete                                                                                                                                                                                                                         15.6s
   ✔ 1456a4fdac7c Pull complete                                                                                                                                                                                                                         15.9s
   ✔ 494fb628d88a Pull complete                                                                                                                                                                                                                        138.8s
[+] Building 28.4s (29/29) FINISHED                                                                                                                                                                                                            docker:default
 => [backend internal] load build definition from Dockerfile.backend                                                                                                                                                                                     0.1s
 => => transferring dockerfile: 1.20kB                                                                                                                                                                                                                   0.1s
 => [backend internal] load metadata for docker.io/library/maven:3.9.9-eclipse-temurin-21                                                                                                                                                                0.0s
 => [backend internal] load metadata for docker.io/library/eclipse-temurin:21-jre-jammy                                                                                                                                                                  0.0s
 => [backend internal] load .dockerignore                                                                                                                                                                                                                0.1s
 => => transferring context: 2B                                                                                                                                                                                                                          0.1s
 => [backend build 1/9] FROM docker.io/library/maven:3.9.9-eclipse-temurin-21                                                                                                                                                                            0.0s
 => [backend internal] load build context                                                                                                                                                                                                                3.8s
 => => transferring context: 140.09MB                                                                                                                                                                                                                    3.8s
 => [backend stage-1 1/3] FROM docker.io/library/eclipse-temurin:21-jre-jammy                                                                                                                                                                            0.0s
 => CACHED [backend build 2/9] WORKDIR /build                                                                                                                                                                                                            0.0s
 => CACHED [backend build 3/9] COPY pom.xml .                                                                                                                                                                                                            0.0s
 => CACHED [backend build 4/9] COPY .mvn .mvn                                                                                                                                                                                                            0.0s
 => CACHED [backend build 5/9] COPY mvnw .                                                                                                                                                                                                               0.0s
 => CACHED [backend build 6/9] RUN chmod +x mvnw                                                                                                                                                                                                         0.0s
 => CACHED [backend build 7/9] RUN ./mvnw -B -DskipTests dependency:go-offline                                                                                                                                                                           0.0s
 => [backend build 8/9] COPY src src                                                                                                                                                                                                                     0.8s
 => [backend build 9/9] RUN ./mvnw -B -DskipTests clean package                                                                                                                                                                                         18.7s
 => CACHED [backend stage-1 2/3] WORKDIR /app                                                                                                                                                                                                            0.0s
 => [backend stage-1 3/3] COPY --from=build /build/target/*.jar app.jar                                                                                                                                                                                  0.3s
 => [backend] exporting to image                                                                                                                                                                                                                         0.5s
 => => exporting layers                                                                                                                                                                                                                                  0.4s
 => => writing image sha256:4439c746cb8aa096da71b6cee6c80ffd687bb8ff566ed6fe2dda7706cdca7fcd                                                                                                                                                             0.0s
 => => naming to docker.io/library/loto_api_v6-backend                                                                                                                                                                                                   0.0s
 => [backend] resolving provenance for metadata file                                                                                                                                                                                                     0.0s
 => [frontend internal] load build definition from Dockerfile.frontend                                                                                                                                                                                   0.1s
 => => transferring dockerfile: 411B                                                                                                                                                                                                                     0.1s
 => [frontend internal] load metadata for docker.io/library/node:18-alpine                                                                                                                                                                               0.9s
 => [frontend internal] load .dockerignore                                                                                                                                                                                                               0.1s
 => => transferring context: 2B                                                                                                                                                                                                                          0.1s
 => [frontend 1/4] FROM docker.io/library/node:18-alpine@sha256:8d6421d663b4c28fd3ebc498332f249011d118945588d0a35cb9bc4b8ca09d9e                                                                                                                         0.0s
 => [frontend internal] load build context                                                                                                                                                                                                               1.0s
 => => transferring context: 10.71kB                                                                                                                                                                                                                     1.0s
 => CACHED [frontend 2/4] RUN npm install -g http-server                                                                                                                                                                                                 0.0s
 => CACHED [frontend 3/4] WORKDIR /app                                                                                                                                                                                                                   0.0s
 => CACHED [frontend 4/4] COPY src/main/resources/static /app                                                                                                                                                                                            0.0s
 => [frontend] exporting to image                                                                                                                                                                                                                        0.0s
 => => exporting layers                                                                                                                                                                                                                                  0.0s
 => => writing image sha256:cd03d65ab4937438e52a02e5bb2f4bdae5b39ac1abafe273ef54cfff33048104                                                                                                                                                             0.0s
 => => naming to docker.io/library/loto_api_v6-frontend                                                                                                                                                                                                  0.0s
 => [frontend] resolving provenance for metadata file                                                                                                                                                                                                    0.0s
[+] Running 7/7
 ✔ Volume "loto_api_v6_ollama_data"  Created                                                                                                                                                                                                             0.0s
 ✔ Container loto_api_v6-ollama-1    Started                                                                                                                                                                                                             0.7s
 ✔ Container loto_api_v6-mongodb-1   Running                                                                                                                                                                                                             0.0s
 ✔ Container loto_api_v6-postgres-1  Healthy                                                                                                                                                                                                             0.7s
 ✔ Container loto_api_v6-backend-1   Started                                                                                                                                                                                                             1.4s
 ✔ Container loto_api_v6-ai-1        Started                                                                                                                                                                                                             1.3s
 ✔ Container loto_api_v6-frontend-1  Started                                                                                                                                                                                                             1.6s
root@UID7E:/mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6# curl -s http://127.0.0.1:11435/api/tags
{"models":[]}root@UID7E:/mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6# docker exec -it loto_api_v6-ollama-1 ollama pull llama3.1:8b
pulling manifest
pulling 667b0c1932bc: 100% ▕████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████▏ 4.9 GB
pulling 948af2743fc7: 100% ▕████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████▏ 1.5 KB
pulling 0ba8f0e314b4: 100% ▕████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████▏  12 KB
pulling 56bb8bd477a5: 100% ▕████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████▏   96 B
pulling 455f34728c9b: 100% ▕████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████▏  487 B
verifying sha256 digest
writing manifest
success
root@UID7E:/mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6# curl -s http://127.0.0.1:11435/api/tags
{"models":[{"name":"llama3.1:8b","model":"llama3.1:8b","modified_at":"2026-03-03T15:46:15.793802868Z","size":4920753328,"digest":"46e0c10c039e019119339687c3c1757cc81b9da49709a3b3924863ba87ca666e","details":{"parent_model":"","format":"gguf","family":"llama","families":["llama"],"parameter_size":"8.0B","quantization_level":"Q4_K_M"}}]}root@UID7E:/mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6# docker exec -it loto_api_v6-ollama-1 ollama list
NAME           ID              SIZE      MODIFIED
llama3.1:8b    46e0c10c039e    4.9 GB    37 seconds ago
root@UID7E:/mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6# curl -s -X POST http://127.0.0.1:8091/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Quelles erreurs éviter en jouant au Loto ?"}'
{"answer":"En jouant au Loto, voici les erreurs à éviter :\n\n1. **N'achète pas de billet avec un nombre fixe** : c'est un piège classique. Les numéros gagnants ne suivent aucune règle particulière.\n2. **N'achète pas de billet avec des numéros \"faciles\"** : les numéros impairs ou pairs ne sont pas plus gagnants que les autres.\n3. **N'achète pas trop de billets** : cela vous coûtera plus cher et augmentera les chances de perdre.\n4. **N'achète pas de billet le jour du tirage** : vous n'aurez pas le temps de vérifier les numéros et risquez de perdre votre ticket.\n5. **N'oubliez pas de vérifier vos numéros** : assurez-vous de lire vos numéros correctement pour éventuellement gagner.\n\nLes bons conseils sont : jouer régulièrement, choisir vos numéros aléatoirement et ne pas dépenser trop d'argent."}root@UID7E:/mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6#


docker stats


[+] Running 7/7
 ✔ Volume "loto_api_v6_ollama_data"  Created                                                                                                                                                                                                             0.0s
 ✔ Container loto_api_v6-ollama-1    Started                                                                                                                                                                                                             0.7s
 ✔ Container loto_api_v6-mongodb-1   Running                                                                                                                                                                                                             0.0s
 ✔ Container loto_api_v6-postgres-1  Healthy                                                                                                                                                                                                             0.7s
 ✔ Container loto_api_v6-backend-1   Started                                                                                                                                                                                                             1.4s
 ✔ Container loto_api_v6-ai-1        Started                                                                                                                                                                                                             1.3s
 ✔ Container loto_api_v6-frontend-1  Started

pulling manifest
CONTAINER ID   NAME                     CPU %     MEM USAGE / LIMIT     MEM %     NET I/O           BLOCK I/O   PIDS
7370a4f31665   loto_api_v6-frontend-1   0.00%     20.44MiB / 19.39GiB   0.10%     58.4kB / 124kB    0B / 0B     11
2cdf9fb146db   loto_api_v6-ai-1         0.29%     57.45MiB / 19.39GiB   0.29%     8.49MB / 167kB    0B / 0B     8
4607b907edc8   loto_api_v6-ollama-1     0.00%     2.262GiB / 8GiB       28.27%    5.01GB / 54.5MB   0B / 0B     25
c077ddd60133   loto_api_v6-backend-1    0.16%     555.1MiB / 19.39GiB   2.80%     85.3MB / 8.96MB   0B / 0B     55
1b155a3bfe77   loto_api_v6-postgres-1   0.00%     65.02MiB / 19.39GiB   0.33%     182kB / 240kB     0B / 0B     18
ca40d760da95   loto_api_v6-mongodb-1    0.18%     170.1MiB / 19.39GiB   0.86%     6.68MB / 84.5MB   0B / 0B     56
560b2f8b3a1d   backend_node             0.02%     63.42MiB / 19.39GiB   0.32%     34kB / 26.4kB     0B / 0B     23
362b4e095dfc   backend_postgres         0.00%     38.53MiB / 19.39GiB   0.19%     32.3kB / 27.8kB   0B / 0B     6


root@UID7E:/mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6# docker compose stop
WARN[0000] /mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6/docker-compose.yml: the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion
[+] Stopping 4/4
 ✔ Container loto_api_v6-frontend-1  Stopped                                                                                                                                                                                                             0.9s
 ✔ Container loto_api_v6-backend-1   Stopped                                                                                                                                                                                                             0.8s
 ✔ Container loto_api_v6-postgres-1  Stopped                                                                                                                                                                                                             1.1s
 ✔ Container loto_api_v6-mongodb-1   Stopped                                                                                                                                                                                                             1.3s
root@UID7E:/mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6#
```

docker compose stop

👉 Ça va :

Stopper tous les containers

Supprimer les containers

Garder les volumes (Mongo, Postgres, Ollama models)


docker compose stop

👉 Containers stoppés mais toujours existants
👉 Tu peux relancer avec :

docker compose start


Si tu veux arrêter seulement le profil AI
docker compose --profile ai down

⚠️ 4️⃣ Si tu veux TOUT supprimer (y compris volumes)

⚠️ ATTENTION : ça supprime les modèles Ollama et les bases

docker compose down -v


Vérifier ce qui tourne
docker ps

Pour voir même ceux arrêtés :
docker ps -a

loto_api_v6-ollama-1
loto_api_v6-ai-1
loto_api_v6-backend-1
loto_api_v6-frontend-1
loto_api_v6-postgres-1
loto_api_v6-mongodb-1

docker compose down

Tu peux vérifier la RAM utilisée :
docker stats

docker-compose avec Nginx LB (solution A),
![alt text](<ChatGPT Image 3 mars 2026, 17_26_13.png>)
![alt text](<ChatGPT Image 3 mars 2026, 17_26_00.png>)



root@UID7E:/mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6# ss -ltnp | grep 8091
sudo lsof -nP -iTCP:8091 -sTCP:LISTEN
LISTEN 0      2048          0.0.0.0:8091       0.0.0.0:*    users:(("python3",pid=4993,fd=16))
COMMAND  PID USER   FD   TYPE DEVICE SIZE/OFF NODE NAME
python3 4993 root   16u  IPv4  46736      0t0  TCP *:8091 (LISTEN)
root@UID7E:/mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6# sudo kill 4993
root@UID7E:/mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6# ss -ltnp | grep 8091 || echo "8091 libre"
8091 libre
root@UID7E:/mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6# sudo kill 4993
kill: (4993): No such process
root@UID7E:/mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6# ss -ltnp | grep 8091 || echo "8091 libre"
8091 libre
root@UID7E:/mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6#
