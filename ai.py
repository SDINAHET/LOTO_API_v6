import os
import re
import time
import random
from collections import Counter
from datetime import datetime, timedelta
from typing import Dict, Optional, Tuple, List, Any

import httpx
from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from pymongo import MongoClient
from zoneinfo import ZoneInfo
from typing import Optional
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles
from pathlib import Path
import logging
from logging.handlers import RotatingFileHandler


# -----------------------------
# Config
# -----------------------------
OLLAMA_BASE = os.getenv("OLLAMA_BASE", "http://127.0.0.1:11434")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "llama3.1:8b")

MONGO_URI = os.getenv("MONGO_URI", "mongodb://localhost:27017")
MONGO_DB = os.getenv("MONGO_DB", "lotodb")
MONGO_COL = os.getenv("MONGO_COL", "historique")

MAX_MESSAGE_LEN = int(os.getenv("MAX_MESSAGE_LEN", "900"))
OLLAMA_TIMEOUT_SEC = float(os.getenv("OLLAMA_TIMEOUT_SEC", "180"))

RL_MAX = int(os.getenv("RL_MAX", "30"))
RL_WINDOW = int(os.getenv("RL_WINDOW", "60"))

CACHE_TTL_SEC = int(os.getenv("CACHE_TTL_SEC", "3600"))
ALLOWED_ORIGINS_RAW = os.getenv("ALLOWED_ORIGINS", "*").strip()

PARIS_TZ = ZoneInfo("Europe/Paris")
UTC_TZ = ZoneInfo("UTC")

SYSTEM = (
    "Tu es un assistant public du Loto Français.\n"
    "Tu expliques les règles, probabilités, et guides l’utilisateur.\n"
    "IMPORTANT :\n"
    "- Tu n’inventes jamais de tirage officiel.\n"
    "- Pour un tirage par date, les données viennent de MongoDB.\n"
    "- Si un tirage n’existe pas en base, tu dis que tu ne l’as pas.\n"
    "- Réponds en français, clair et plutôt court.\n"
)

# DATE_RE = re.compile(r"\b(\d{2})/(\d{2})/(\d{4})\b")
DATE_RE = re.compile(r"\b(\d{1,2})/(\d{1,2})/(\d{4})\b")


TICKET_RE = re.compile(
    r"(?P<n1>\d{1,2})\s*[ ,;-]\s*(?P<n2>\d{1,2})\s*[ ,;-]\s*(?P<n3>\d{1,2})\s*[ ,;-]\s*(?P<n4>\d{1,2})\s*[ ,;-]\s*(?P<n5>\d{1,2})"
    r"\s*(?:\+|chance|c)\s*(?P<chance>\d{1,2})",
    re.IGNORECASE,
)

SINCE_YEAR_RE = re.compile(r"(?:depuis|since|a partir|à partir)\s*(20\d{2})", re.IGNORECASE)
ASK_COMBOS_RE = re.compile(r"(propose|proposer|suggest|donne)\s+.*(combinaison|combinaisons|numéros|numeros)", re.IGNORECASE)
ASK_STATS_RE = re.compile(r"(stat|stats|fréquence|frequence|probabilit|proba)", re.IGNORECASE)
ASK_SECOND_DRAW_RE = re.compile(r"(2e|2ème|second|deuxième)\s+(tirage|draw)", re.IGNORECASE)
INCLUDE_SECOND_RE = re.compile(r"(inclure\s+second|include\s+second|second\s+tirage\s*[:]?|2e\s+tirage)", re.IGNORECASE)

DEFAULT_RANK10_GAIN = 2.20

ASK_CODES_RE = re.compile(r"\b(codes?\s+gagnants?)\b", re.IGNORECASE)


LOG_DIR = Path(os.getenv("AI_LOG_DIR", "/tmp"))
LOG_DIR.mkdir(parents=True, exist_ok=True)
MONGO_LOG_FILE = Path(os.getenv("MONGO_LOG_FILE", str(LOG_DIR / "mongo_queries.log")))

LOG_MONGO = os.getenv("LOG_MONGO", "0") == "1"

logger = logging.getLogger("ai")
logger.setLevel(logging.INFO)
logger.propagate = False

if not any(isinstance(h, RotatingFileHandler) and getattr(h, "baseFilename", "") == str(MONGO_LOG_FILE)
           for h in logger.handlers):
    handler = RotatingFileHandler(
        MONGO_LOG_FILE, maxBytes=2_000_000, backupCount=3, encoding="utf-8"
    )
    handler.setFormatter(logging.Formatter("%(asctime)s %(levelname)s %(message)s"))
    logger.addHandler(handler)


# -----------------------------
# App + CORS
# -----------------------------
app = FastAPI(title="loto-ai-service", version="3.0.0")

# if ALLOWED_ORIGINS_RAW == "*":
#     allow_origins = ["*"]
# else:
#     allow_origins = [x.strip() for x in ALLOWED_ORIGINS_RAW.split(",") if x.strip()]

# app.add_middleware(
#     CORSMiddleware,
#     allow_origins=allow_origins,
#     allow_credentials=False,
#     allow_methods=["*"],
#     allow_headers=["*"],
# )

# allow_origins = [
#     "http://localhost:5500",
#     "http://127.0.0.1:5500",
#     "https://stephanedinahet.fr",
# ]

# app.add_middleware(
#     CORSMiddleware,
#     allow_origins=allow_origins,
#     allow_credentials=False,
#     allow_methods=["*"],
#     allow_headers=["*"],
# )

allow_origins = [
    "http://localhost:5500",
    "http://127.0.0.1:5500",
    "https://stephanedinahet.fr",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=allow_origins,
    allow_credentials=True,   # ✅ requis si ton front envoie credentials: "include"
    allow_methods=["*"],
    allow_headers=["*"],
)

# def safe_float(x) -> Optional[float]:
#     """
#     Convertit une valeur en float sans jamais lever d'exception.
#     Retourne None si conversion impossible.
#     """
#     if x is None:
#         return None
#     try:
#         return float(str(x).replace(",", "."))
#     except Exception:
#         return None

def safe_float(x) -> Optional[float]:
    """
    Convertit une valeur en float sans lever d'exception.
    Gère: "2,20", "2.20", "2,20 €", "2.20€", Decimal128, etc.
    """
    if x is None:
        return None
    s = str(x).strip()
    # extrait le premier nombre (avec , ou .)
    m = re.search(r"-?\d+(?:[.,]\d+)?", s)
    if not m:
        return None
    try:
        return float(m.group(0).replace(",", "."))
    except Exception:
        return None

def gain_with_fallback(draw: dict, rank: int, match_count: int, chance_ok: bool) -> Optional[float]:
    gain = read_gain_for_rank(draw, rank)
    # fallback uniquement si Mongo ne donne rien ET cas rang 10 (0/5 + chance ok)
    if gain is None and rank == 10 and match_count == 0 and chance_ok:
        return DEFAULT_RANK10_GAIN
    return gain


# -----------------------------
# Mongo (read-only)
# -----------------------------
mongo = MongoClient(MONGO_URI)
db = mongo[MONGO_DB]
historique = db[MONGO_COL]

# -----------------------------
# Cache + Rate limit
# -----------------------------
_cache: Dict[str, Tuple[float, Any]] = {}
_rl: Dict[str, Tuple[float, int]] = {}

def cache_get(key: str) -> Optional[Any]:
    item = _cache.get(key)
    if not item:
        return None
    exp, val = item
    if time.time() > exp:
        _cache.pop(key, None)
        return None
    return val

def cache_set(key: str, val: Any, ttl: int = CACHE_TTL_SEC) -> None:
    _cache[key] = (time.time() + ttl, val)

def get_client_ip(request: Request) -> str:
    return request.client.host if request.client else "unknown"

def rate_limit(ip: str) -> None:
    now = time.time()
    window_start, count = _rl.get(ip, (now, 0))
    if now - window_start > RL_WINDOW:
        window_start, count = now, 0
    count += 1
    _rl[ip] = (window_start, count)
    if count > RL_MAX:
        raise HTTPException(status_code=429, detail="Trop de requêtes. Réessaie dans 1 minute.")

# -----------------------------
# Dates / Mongo helpers (Paris-correct)
# -----------------------------
def _to_paris_date_str(dt) -> str:
    if hasattr(dt, "strftime"):
        if dt.tzinfo is None:
            dt = dt.replace(tzinfo=UTC_TZ)
        return dt.astimezone(PARIS_TZ).strftime("%d/%m/%Y")
    return "?"

def _paris_day_to_utc_range(date_fr: str) -> Tuple[datetime, datetime]:
    d = datetime.strptime(date_fr, "%d/%m/%Y")
    start_paris = d.replace(hour=0, minute=0, second=0, microsecond=0, tzinfo=PARIS_TZ)
    end_paris = start_paris + timedelta(days=1)
    start_utc = start_paris.astimezone(UTC_TZ)
    end_utc = end_paris.astimezone(UTC_TZ)
    return start_utc, end_utc

def find_draw_by_date_fr(date_fr: str) -> Optional[dict]:
    # ✅ Cherche le tirage correspondant au JOUR Paris (corrige le 23:00Z = J+1 Paris)
    start_utc, end_utc = _paris_day_to_utc_range(date_fr)
    q = {"dateDeTirage": {"$gte": start_utc, "$lt": end_utc}}
    return mongo_find_one(q)


def get_latest_draw() -> Optional[dict]:
    return mongo_find_one({}, sort=[("dateDeTirage", -1)])


def format_draw(draw: dict) -> str:
    nums = [draw.get("boule1"), draw.get("boule2"), draw.get("boule3"), draw.get("boule4"), draw.get("boule5")]
    chance = draw.get("numeroChance")
    jour = draw.get("jourDeTirage", "")
    date_str = _to_paris_date_str(draw.get("dateDeTirage"))
    return f"Tirage du {date_str} {f'({jour})' if jour else ''} : {' '.join(map(str, nums))} + Chance {chance}"

def serialize_draw(draw: dict) -> dict:
    if not draw:
        return {}

    date_str = _to_paris_date_str(draw.get("dateDeTirage"))
    nums = [draw.get("boule1"), draw.get("boule2"), draw.get("boule3"), draw.get("boule4"), draw.get("boule5")]
    chance = draw.get("numeroChance")

    second_nums = [
        draw.get("boule1SecondTirage"),
        draw.get("boule2SecondTirage"),
        draw.get("boule3SecondTirage"),
        draw.get("boule4SecondTirage"),
        draw.get("boule5SecondTirage"),
    ]
    second_ok = all(isinstance(x, int) for x in second_nums)

    payload = {
        "date_fr": date_str,
        "jour": draw.get("jourDeTirage"),
        "anneeNumeroDeTirage": draw.get("anneeNumeroDeTirage"),
        "nums": nums,
        "chance": chance,
        "combinaison": draw.get("combinaisonGagnante"),
        "rangs": {
            "1": {"gagnants": draw.get("nombreDeGagnantAuRang1"), "rapport": draw.get("rapportDuRang1")},
            "2": {"gagnants": draw.get("nombreDeGagnantAuRang2"), "rapport": draw.get("rapportDuRang2")},
            "3": {"gagnants": draw.get("nombreDeGagnantAuRang3"), "rapport": draw.get("rapportDuRang3")},
            "4": {"gagnants": draw.get("nombreDeGagnantAuRang4"), "rapport": draw.get("rapportDuRang4")},
            "5": {"gagnants": draw.get("nombreDeGagnantAuRang5"), "rapport": draw.get("rapportDuRang5")},
            "6": {"gagnants": draw.get("nombreDeGagnantAuRang6"), "rapport": draw.get("rapportDuRang6")},
            "7": {"gagnants": draw.get("nombreDeGagnantAuRang7"), "rapport": draw.get("rapportDuRang7")},
            "8": {"gagnants": draw.get("nombreDeGagnantAuRang8"), "rapport": draw.get("rapportDuRang8")},
            "9": {"gagnants": draw.get("nombreDeGagnantAuRang9"), "rapport": draw.get("rapportDuRang9")},
        },
        "codes": {
            "nombre": draw.get("nombreDeCodesGagnants"),
            "rapport": draw.get("rapportCodesGagnants"),
            "liste": draw.get("codesGagnants"),
        },
        "jokerplus": draw.get("numeroJokerplus"),
        "devise": draw.get("devise"),
    }

    if second_ok:
        payload["second_tirage"] = {
            "nums": second_nums,
            "combinaison": draw.get("combinaisonGagnanteSecondTirage"),
            "rangs": {
                "1": {"gagnants": draw.get("nombreDeGagnantAuRang1SecondTirage"), "rapport": draw.get("rapportDuRang1SecondTirage")},
                "2": {"gagnants": draw.get("nombreDeGagnantAuRang2SecondTirage"), "rapport": draw.get("rapportDuRang2SecondTirage")},
                "3": {"gagnants": draw.get("nombreDeGagnantAuRang3SecondTirage"), "rapport": draw.get("rapportDuRang3SecondTirage")},
                "4": {"gagnants": draw.get("nombreDeGagnantAuRang4SecondTirage"), "rapport": draw.get("rapportDuRang4SecondTirage")},
            }
        }

    return payload

# -----------------------------
# Ticket parsing + ranking
# -----------------------------
def parse_ticket(text: str):
    m = TICKET_RE.search(text)
    if not m:
        return None
    nums = [int(m.group(f"n{i}")) for i in range(1, 6)]
    chance = int(m.group("chance"))
    return nums, chance

def validate_ticket(nums, chance) -> Optional[str]:
    if len(nums) != 5:
        return "Le ticket doit contenir 5 numéros."
    if len(set(nums)) != 5:
        return "Les 5 numéros doivent être tous différents."
    if any(n < 1 or n > 49 for n in nums):
        return "Les numéros doivent être entre 1 et 49."
    if chance < 1 or chance > 10:
        return "Le numéro Chance doit être entre 1 et 10."
    return None

def compute_rank(match_count: int, chance_ok: bool) -> Optional[int]:
    if match_count == 5 and chance_ok: return 1
    if match_count == 5 and not chance_ok: return 2
    if match_count == 4 and chance_ok: return 3
    if match_count == 4 and not chance_ok: return 4
    if match_count == 3 and chance_ok: return 5
    if match_count == 3 and not chance_ok: return 6
    if match_count == 2 and chance_ok: return 7
    if match_count == 2 and not chance_ok: return 8
    if match_count == 1 and chance_ok: return 9
    if match_count == 0 and chance_ok: return 10
    return None

# def read_gain(draw: dict, key: str) -> Optional[float]:
#     if key in draw and draw[key] is not None:
#         try:
#             return float(str(draw[key]).replace(",", "."))
#         except Exception:
#             return None
#     return None

def read_gain(draw: dict, key: str) -> Optional[float]:
    if key in draw:
        return safe_float(draw.get(key))
    return None

def read_gain_for_rank(draw: dict, rank: int) -> Optional[float]:
    return read_gain(draw, f"rapportDuRang{rank}")

# Second tirage : dans tes docs, pas de chance associé.
# On mappe : 5 bons = rang1, 4 bons = rang2, 3 bons = rang3, 2 bons = rang4
def second_rank_from_matches(match_count: int) -> Optional[int]:
    if match_count == 5: return 1
    if match_count == 4: return 2
    if match_count == 3: return 3
    if match_count == 2: return 4
    return None

def read_second_gain(draw: dict, rank: int) -> Optional[float]:
    return read_gain(draw, f"rapportDuRang{rank}SecondTirage")

# -----------------------------
# Stats + combos (MongoDB)
# -----------------------------
def compute_number_frequencies_cached() -> Tuple[Counter, Counter]:
    cached = cache_get("freq:all")
    if cached:
        return cached

    proj = {
        "boule1": 1, "boule2": 1, "boule3": 1, "boule4": 1, "boule5": 1,
        "numeroChance": 1
    }

    docs = mongo_find_all({}, projection=proj)

    nums: List[int] = []
    chances: List[int] = []

    for d in docs:
        for k in ("boule1", "boule2", "boule3", "boule4", "boule5"):
            v = d.get(k)
            if isinstance(v, int):
                nums.append(v)
        c = d.get("numeroChance")
        if isinstance(c, int):
            chances.append(c)

    num_freq = Counter(nums)
    chance_freq = Counter(chances)

    cache_set("freq:all", (num_freq, chance_freq), ttl=6 * 3600)
    return num_freq, chance_freq


def weighted_sample_without_replacement(items: List[int], weights: List[float], k: int) -> List[int]:
    chosen = []
    pool = list(zip(items, weights))
    for _ in range(k):
        total = sum(w for _, w in pool) or 1.0
        r = random.random() * total
        acc = 0.0
        pick_idx = 0
        for i, (_, w) in enumerate(pool):
            acc += w
            if acc >= r:
                pick_idx = i
                break
        chosen.append(pool[pick_idx][0])
        pool.pop(pick_idx)
    return chosen

def generate_probable_combinations(count: int = 5) -> List[dict]:
    count = min(max(count, 1), 10)
    num_freq, chance_freq = compute_number_frequencies_cached()

    top_nums = [n for n, _ in num_freq.most_common(35)] or list(range(1, 50))
    top_weights = [float(num_freq.get(n, 1)) for n in top_nums]

    top_chances = [c for c, _ in chance_freq.most_common(10)] or list(range(1, 11))
    top_c_weights = [float(chance_freq.get(c, 1)) for c in top_chances]

    combos = []
    for _ in range(count):
        nums = sorted(weighted_sample_without_replacement(top_nums, top_weights, 5))
        total = sum(top_c_weights) or 1.0
        r = random.random() * total
        acc = 0.0
        chance = top_chances[0]
        for c, w in zip(top_chances, top_c_weights):
            acc += w
            if acc >= r:
                chance = c
                break
        combos.append({"nums": nums, "chance": chance})
    return combos

def parse_codes_gagnants(draw: dict) -> List[str]:
    raw = draw.get("codesGagnants")
    if not raw:
        return []
    if isinstance(raw, list):
        return [str(x).strip() for x in raw if str(x).strip()]
    # c’est un string du style "U 3394 9824, M 3455 1085, ..."
    parts = [p.strip() for p in str(raw).split(",")]
    return [p for p in parts if p]

# -----------------------------
# Ollama call (robuste)
# -----------------------------
async def ollama_chat(user_message: str) -> str:
    payload = {
        "model": OLLAMA_MODEL,
        "stream": False,
        "messages": [
            {"role": "system", "content": SYSTEM},
            {"role": "user", "content": user_message},
        ],
    }

    timeout = httpx.Timeout(
        timeout=OLLAMA_TIMEOUT_SEC,
        connect=10.0,
        read=OLLAMA_TIMEOUT_SEC,
        write=10.0,
        pool=10.0
    )

    try:
        async with httpx.AsyncClient(timeout=timeout) as client:
            r = await client.post(f"{OLLAMA_BASE}/api/chat", json=payload)
            r.raise_for_status()
            data = r.json()
        return data.get("message", {}).get("content", "") or ""
    except httpx.ReadTimeout:
        return "⏳ L’IA met trop de temps à répondre (timeout). Réessaie dans quelques secondes."
    except Exception:
        return "❌ Erreur IA."

def mongo_to_pseudo_sql(collection: str, op: str, query, projection=None, sort=None, limit=None) -> str:
    q = query or {}
    proj = projection or {}
    parts = [f"{op.upper()} {collection}"]
    parts.append(f"WHERE {q}")
    if proj:
        parts.append(f"PROJECTION {proj}")
    if sort:
        parts.append(f"SORT {sort}")
    if limit is not None:
        parts.append(f"LIMIT {limit}")
    return " | ".join(parts)

def log_mongo(op: str, *, query=None, projection=None, sort=None, limit=None, took_ms: float | None = None):
    if not LOG_MONGO:
        return

    msg = mongo_to_pseudo_sql(MONGO_COL, op, query, projection, sort, limit)
    if took_ms is not None:
        msg += f" | took_ms={took_ms:.2f}"

    logger.info(msg)


def mongo_find_one(query: dict, *, projection: dict | None = None, sort=None) -> Optional[dict]:
    t0 = time.perf_counter()
    doc = historique.find_one(query, projection=projection, sort=sort)
    took_ms = (time.perf_counter() - t0) * 1000
    log_mongo("find_one", query=query, projection=projection, sort=sort, took_ms=took_ms)
    return doc

def mongo_find_all(query: dict, *, projection=None, sort=None, limit=None) -> List[dict]:
    t0 = time.perf_counter()
    cursor = historique.find(query, projection=projection, sort=sort)
    if limit is not None:
        cursor = cursor.limit(limit)
    docs = list(cursor)
    took_ms = (time.perf_counter() - t0) * 1000
    log_mongo("find_all", query=query, projection=projection, sort=sort, limit=limit, took_ms=took_ms)
    return docs


def mongo_find(query: dict, *, projection: dict | None = None, sort=None, limit: int | None = None):
    # On log l'intention ici (car le cursor est lazy)
    log_mongo("find", query=query, projection=projection, sort=sort, limit=limit)
    cursor = historique.find(query, projection=projection, sort=sort)
    if limit is not None:
        cursor = cursor.limit(limit)
    return cursor





# -----------------------------
# API models
# -----------------------------
class ChatReq(BaseModel):
    message: str = Field(..., min_length=1)
    locale: str = "fr"

# -----------------------------
# Endpoints
# -----------------------------
@app.get("/health")
def health():
    return {
        "status": "ok",
        "ollama": OLLAMA_BASE,
        "model": OLLAMA_MODEL,
        "mongo": {"uri": MONGO_URI, "db": MONGO_DB, "collection": MONGO_COL},
        "rate_limit": {"max": RL_MAX, "window_sec": RL_WINDOW},
        "cors": {"allowed_origins": allow_origins},
        "server_time_utc": datetime.now(tz=UTC_TZ).isoformat(),
    }

@app.get("/")
def root():
    return {"status": "ok", "service": "loto-ai-service"}

@app.get("/ai/health")
def ai_health():
    # Alias attendu par le frontend
    return health()


@app.get("/ai/latest-draw")
def latest_draw():
    d = get_latest_draw()
    if not d:
        raise HTTPException(status_code=404, detail="Aucun tirage en base.")
    return {"draw": serialize_draw(d)}

@app.get("/ai/stats/frequencies")
def frequencies(top: int = 10):
    num_freq, chance_freq = compute_number_frequencies_cached()
    top = min(max(top, 1), 20)
    return {
        "top_numbers": [{"n": n, "count": c} for n, c in num_freq.most_common(top)],
        "top_chances": [{"n": n, "count": c} for n, c in chance_freq.most_common(min(top, 10))],
    }

@app.get("/ai/probable-combinations")
def probable_combinations(count: int = 5):
    return {"combinations": generate_probable_combinations(count)}

@app.post("/ai/chat")
async def ai_chat(req: ChatReq, request: Request):
    ip = get_client_ip(request)
    rate_limit(ip)

    msg = (req.message or "").strip()
    if len(msg) > MAX_MESSAGE_LEN:
        raise HTTPException(status_code=413, detail=f"Message trop long (max {MAX_MESSAGE_LEN} caractères).")

    low = msg.lower()
    ticket = parse_ticket(msg)
    mdate = DATE_RE.search(msg)

    payload_ui: Dict[str, Any] = {}

    if ticket:
        nums, ch = ticket
        payload_ui["ticketSuggestion"] = {"nums": nums, "chance": ch, "date": None}


    if mdate and ASK_CODES_RE.search(msg):
        d, mo, y = mdate.groups()
        date_fr = f"{int(d):02d}/{int(mo):02d}/{y}"
        draw = find_draw_by_date_fr(date_fr)
        if not draw:
            return {"answer": f"Je ne trouve aucun tirage enregistré pour le {date_fr} (jour Paris) dans MongoDB."}

        codes = parse_codes_gagnants(draw)
        if not codes:
            return {"answer": f"📌 {date_fr} : aucun code gagnant en base."}

        return {"answer": "📌 Codes gagnants du " + date_fr + " :\n- " + "\n- ".join(codes)}


    # --- stats / combos ---
    if ASK_STATS_RE.search(msg):
        num_freq, chance_freq = compute_number_frequencies_cached()
        payload_ui["stats"] = {
            "top_numbers": [{"n": n, "count": c} for n, c in num_freq.most_common(10)],
            "top_chances": [{"n": n, "count": c} for n, c in chance_freq.most_common(5)],
        }
        return {"answer": "Voilà les stats de fréquence (MongoDB).", **payload_ui}

    if ASK_COMBOS_RE.search(msg) or "combinaison" in low:
        n = 5
        m_n = re.search(r"\b(10|[1-9])\b", low)
        if m_n:
            n = int(m_n.group(1))
        combos = generate_probable_combinations(n)
        payload_ui["combinations"] = combos
        return {"answer": f"Voici {len(combos)} propositions basées sur les fréquences MongoDB.", **payload_ui}

    # --- Ticket + depuis YYYY (total) ---
    if ticket:
        m_since = SINCE_YEAR_RE.search(msg)
        if m_since:
            since_year = int(m_since.group(1))
            nums, ch = ticket

            err = validate_ticket(nums, ch)
            if err:
                return {"answer": "❌ " + err, **payload_ui}

            start = datetime(since_year, 1, 1, tzinfo=UTC_TZ)

            q = {"dateDeTirage": {"$gte": start}}
            proj = {
                "dateDeTirage": 1,
                "boule1": 1, "boule2": 1, "boule3": 1, "boule4": 1, "boule5": 1,
                "numeroChance": 1,
                "rapportDuRang1": 1, "rapportDuRang2": 1, "rapportDuRang3": 1, "rapportDuRang4": 1,
                "rapportDuRang5": 1, "rapportDuRang6": 1, "rapportDuRang7": 1, "rapportDuRang8": 1,
                "rapportDuRang9": 1, "rapportDuRang10": 1,
            }
            cursor = mongo_find(q, projection=proj)


            total = 0.0
            played = 0
            wins = 0
            best: List[Tuple[float, str, int]] = []

            for draw in cursor:
                played += 1
                draw_nums = [draw.get("boule1"), draw.get("boule2"), draw.get("boule3"), draw.get("boule4"), draw.get("boule5")]
                draw_ch = draw.get("numeroChance")
                if not all(isinstance(x, int) for x in draw_nums) or not isinstance(draw_ch, int):
                    continue

                good = set(nums).intersection(set(draw_nums))
                match_count = len(good)
                chance_ok = (draw_ch == ch)
                rank = compute_rank(match_count, chance_ok)
                if rank is None:
                    continue

                gain = read_gain_for_rank(draw, rank)
                if gain is None:
                    continue

                wins += 1
                total += gain
                best.append((gain, _to_paris_date_str(draw.get("dateDeTirage")), rank))

            best.sort(key=lambda x: x[0], reverse=True)
            best10 = best[:10]

            out = (
                f"📊 Bilan depuis {since_year} pour: {' '.join(map(str, nums))} + Chance {ch}\n"
                f"- Tirages analysés: {played}\n"
                f"- Tirages gagnants: {wins}\n"
                f"- Total gagné (selon rapports en base): {total:.2f} €"
            )
            if best10:
                out += "\n- Top 10 gains:\n" + "\n".join([f"  • {g:.2f} € (rang {r}) — {d}" for (g, d, r) in best10])

            return {"answer": out, "total_gains": round(total, 2), **payload_ui}

    # --- Ticket + Date => résultat principal + second tirage ---
    if ticket and mdate:
        d, mo, y = mdate.groups()
        date_fr = f"{int(d):02d}/{int(mo):02d}/{y}"

        nums, ch = ticket
        payload_ui["ticketSuggestion"]["date"] = date_fr

        err = validate_ticket(nums, ch)
        if err:
            return {"answer": "❌ " + err, **payload_ui}

        draw = find_draw_by_date_fr(date_fr)
        if not draw:
            return {"answer": f"Je ne trouve aucun tirage enregistré pour le {date_fr} (jour Paris) dans MongoDB.", **payload_ui}

        date_draw = _to_paris_date_str(draw.get("dateDeTirage"))
        jour = draw.get("jourDeTirage", "")

        draw_nums = [draw.get("boule1"), draw.get("boule2"), draw.get("boule3"), draw.get("boule4"), draw.get("boule5")]
        draw_ch = draw.get("numeroChance")

        if not all(isinstance(x, int) for x in draw_nums) or not isinstance(draw_ch, int):
            return {"answer": "Je trouve un document, mais les champs du tirage principal sont incomplets.", **payload_ui}

        # ---------- Principal ----------
        good = sorted(set(nums).intersection(set(draw_nums)))
        match_count = len(good)
        chance_ok = (draw_ch == ch)
        rank = compute_rank(match_count, chance_ok)

        total_gain = 0.0
        has_gain = False

        out_lines = []
        out_lines.append(f"📅 Date demandée : {date_fr}")
        out_lines.append(f"📌 Tirage en base (Paris) : {date_draw}{(' ' + jour) if jour else ''}")
        out_lines.append("")
        out_lines.append(f"🎯 Tirage principal : {' '.join(map(str, draw_nums))} + Chance {draw_ch}")
        out_lines.append(f"🎟️ Ton ticket      : {' '.join(map(str, nums))} + Chance {ch}")
        out_lines.append(
            f"✅ Match           : {match_count}/5 ({', '.join(map(str, good)) if good else 'aucun'}) — "
            f"Chance: {'OK' if chance_ok else 'NON'}"
        )

        if rank is None:
            out_lines.append("❌ Résultat principal : pas gagnant.")
        else:
            gain = gain_with_fallback(draw, rank, match_count, chance_ok)
            out_lines.append(
                f"🏆 Résultat principal : rang {rank}"
                + (f" — gain {gain:.2f} €" if gain is not None else " — gain non disponible.")
            )
            if gain is not None:
                total_gain += gain
                has_gain = True

        # ---------- Second tirage (uniquement si demandé OU question explicite) ----------
        include_second = bool(INCLUDE_SECOND_RE.search(msg)) or bool(ASK_SECOND_DRAW_RE.search(msg))

        if include_second:
            s_nums = [
                draw.get("boule1SecondTirage"),
                draw.get("boule2SecondTirage"),
                draw.get("boule3SecondTirage"),
                draw.get("boule4SecondTirage"),
                draw.get("boule5SecondTirage"),
            ]

            out_lines.append("")
            out_lines.append("— — —")

            if all(isinstance(x, int) for x in s_nums):
                s_good = sorted(set(nums).intersection(set(s_nums)))
                s_match = len(s_good)
                s_rank = second_rank_from_matches(s_match)

                out_lines.append(f"🎯 Second tirage   : {' '.join(map(str, s_nums))}")
                out_lines.append(f"✅ Match           : {s_match}/5 ({', '.join(map(str, s_good)) if s_good else 'aucun'})")

                if s_rank is None:
                    out_lines.append("❌ Résultat second tirage : pas gagnant.")
                else:
                    # read_second_gain() => read_gain() => safe_float(), donc pas besoin de safe_float() ici
                    s_gain = read_second_gain(draw, s_rank)

                    out_lines.append(
                        f"🏆 Résultat second tirage : rang {s_rank}"
                        + (f" — gain {s_gain:.2f} €" if s_gain is not None else " — gain non disponible.")
                    )

                    if s_gain is not None:
                        total_gain += s_gain
                        has_gain = True
            else:
                out_lines.append("ℹ️ Second tirage demandé, mais absent en base pour cette date.")

        # ---------- Total (principal + second) ----------
        if has_gain:
            out_lines.append("")
            out_lines.append(f"💰 Total gains (principal + second) : {total_gain:.2f} €")

        return {"answer": "\n".join(out_lines), **payload_ui}



    # --- Date seule => tirage Mongo (Paris-correct) ---
    if mdate and not ticket:
        d, mo, y = mdate.groups()
        # date_fr = f"{d}/{mo}/{y}"
        date_fr = f"{int(d):02d}/{int(mo):02d}/{y}"

        draw = find_draw_by_date_fr(date_fr)
        if not draw:
            return {"answer": f"Je ne trouve aucun tirage enregistré pour le {date_fr} (jour Paris) dans MongoDB."}

        return {"answer": format_draw(draw)}

    # --- Règles / proba (LLM + cache) ---
    if "règle" in low or "regle" in low or "comment on joue" in low:
        key = "static:rules"
        cached = cache_get(key)
        if cached:
            return {"answer": cached}
        answer = await ollama_chat("Explique simplement les règles du Loto Français (5 numéros + numéro Chance).")
        cache_set(key, answer, ttl=6 * 3600)
        return {"answer": answer}

    if "probabilit" in low or "chance de gagner" in low or "proba" in low:
        key = "static:proba"
        cached = cache_get(key)
        if cached:
            return {"answer": cached}
        answer = await ollama_chat("Explique les probabilités principales du Loto (court, sans tableau long).")
        cache_set(key, answer, ttl=6 * 3600)
        return {"answer": answer}

    # --- Chat normal ---
    answer = await ollama_chat(msg)
    return {"answer": answer}

@app.get("/ai/codes/{date_fr}")
def codes_by_date(date_fr: str):
    # accepte 1/12/2025 ou 01/12/2025
    m = DATE_RE.search(date_fr)
    if not m:
        raise HTTPException(status_code=400, detail="Format attendu: JJ/MM/AAAA")
    d, mo, y = m.groups()
    date_fr = f"{int(d):02d}/{int(mo):02d}/{y}"

    draw = find_draw_by_date_fr(date_fr)
    if not draw:
        raise HTTPException(status_code=404, detail=f"Aucun tirage pour {date_fr}")

    codes = parse_codes_gagnants(draw)
    return {
        "date_fr": date_fr,
        "jour": draw.get("jourDeTirage"),
        "nombreDeCodesGagnants": draw.get("nombreDeCodesGagnants"),
        "rapportCodesGagnants": safe_float(draw.get("rapportCodesGagnants")),
        "codes": codes,
    }
