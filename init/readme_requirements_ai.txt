sudo apt update
sudo apt install -y python3-full python3-venv
# python3 -m venv .venv
source .venv/bin/activate
pip install -U pip
# python -m uvicorn ai:app --host 0.0.0.0 --port 8090
pip install fastapi uvicorn httpx
# python -m uvicorn ai:app --host 0.0.0.0 --port 8090

pip install pymongo
# python -m uvicorn ai:app --host 0.0.0.0 --port 8090

pip install "fastapi[standard]" uvicorn httpx pymongo python-dotenv
python3 -m uvicorn ai:app --host 0.0.0.0 --port 8090

root@UID7E:/mnt/d/Users/steph/Documents/6ème trimestre/LOTO_API_v6# OLLAMA_BASE="http://127.0.0.1:11434" MONGO_URI="mongodb://127.0.0.1:27017" uvicorn ai:app --host 0.0.0.0 --port 8091 --reload
http://localhost:8091/ai/latest-draw
http://172.18.71.179:8091/ai/latest-draw

Dépannage demarrage service Ai apres une erreur 500:
```bash
OLLAMA_BASE="http://127.0.0.1:11434"\
MONGO_URI="mongodb://127.0.0.1:27017" \
uvicorn ai:app --host 0.0.0.0 --port 8091 --reload
```

http://localhost:8091/
curl -i http://localhost:8091/
curl -i http://localhost:8091/ai/health
curl -s http://localhost:8091/ai/health | jq .model



(.venv) uid7e@DESKTOP-PIOUMIU:/mnt/c/Users/Sté
phane_HP/Documents/LOTO_API_v4-integrate_ia$ ip -4 addr show eth0 | grep -oP '(?<=inet\s)\d+(\.\d+){3}'
172.27.158.250


   ✅ Spring UP

=== URLS INTRANET ===
Front :  http://172.18.71.179:5500/
API   :  http://172.18.71.179:8082/swagger-ui/index.html
=====================

   🪟 Ouverture demandée côté Windows: http://localhost:5500/
   🪟 Ouverture demandée côté Windows: http://localhost:8082/swagger-ui/index.html
==> AI service (8091)
   ⏭️ AI désactivée (AI_ENABLED=false)

   PS C:\WINDOWS\system32> netsh interface portproxy add v4tov4 listenaddress=0.0.0.0 listenport=5500 connectaddress=172.18.71.179 connectport=5500
>> netsh interface portproxy add v4tov4 listenaddress=0.0.0.0 listenport=8082 connectaddress=172.18.71.179 connectport=8082
>>


PS C:\WINDOWS\system32>  netsh interface portproxy add v4tov4 listenaddress=0.0.0.0 listenport=8091 connectaddress=172.18.71.179 connectport=8091

   Adresse IPv4. . . . . . . . . . . . . .: 192.168.1.251
   hhtp://192.168.1.251:5500/
   hhtp://192.168.1.251:8082/swagger-ui/index.html
   hhtp://192.168.1.251:8091/docs


================= ACCÈS DISPONIBLES =================

🖥️  LOCAL (PC uniquement)
   Front : http://localhost:5500/
   API   : http://localhost:8082/swagger-ui/index.html

🐧 WSL (réseau interne)
   Front : http://172.18.71.179:5500/
   API   : http://172.18.71.179:8082/swagger-ui/index.html

📱 INTRANET (Téléphone / autres PC)
   Front : http://192.168.56.1:5500/
   API   : http://192.168.56.1:8082/swagger-ui/index.html
=====================================================



================= URLS =================
Localhost:
  Front : http://localhost:5500/
  API   : http://localhost:8082/swagger-ui/index.html
  AI    : http://localhost:8091/health

WSL (souvent 172.x.x.x) :
  Front : http://172.18.71.179:5500/
  API   : http://172.18.71.179:8082/swagger-ui/index.html
  AI    : http://172.18.71.179:8091/health

Windows LAN (souvent 192.168.x.x ou 10.x.x.x) :
  Front : http://192.168.56.1:5500/
  API   : http://192.168.56.1:8082/swagger-ui/index.html
  AI    : http://192.168.56.1:8091/health

👉 Depuis ton téléphone (même Wi-Fi) : utilise EN PRIORITÉ ces URLs Windows.
========================================

   🪟 Ouverture demandée côté Windows: http://localhost:5500/
   🪟 Ouverture demandée côté Windows: http://localhost:8082/swagger-ui/index.html
   🪟 Ouverture demandée côté Windows: http://localhost:8091/health
==> Services lancés. (CTRL+C ne stoppera pas les nohup).
    Logs:
    - /tmp/static_http_5500.log
    - /tmp/spring_8082.log
    - /tmp/ai_8091.log
