<!-- # LOTO API

## Prérequis système
- Python 3.8+
- Java 21
- PostgreSQL 14+
- MongoDB 7.0+

## Installation

### 1. Cloner le projet
```bash
git clone <repository_url>
cd LOTO_API_v3
```

### 2. Configurer l'environnement

#### A. Configuration Python
```bash
# Rendre le script exécutable
chmod +x setup_venv.sh

# Exécuter le script de configuration Python
./setup_venv.sh

# Activer l'environnement virtuel
source venv/bin/activate
```

#### B. Configuration des bases de données
```bash
# Rendre les scripts exécutables
chmod +x install_dependencies.sh config_postgres.sh

# Installer les dépendances système
sudo ./install_dependencies.sh

# Configurer PostgreSQL et MongoDB
sudo ./config_postgres.sh
```

### 3. Configuration de l'application

```bash
# Copier le fichier d'environnement exemple
cp .env.example .env

# Éditer les configurations
nano .env
```

### 4. Migration des données

```bash
# Rendre le script exécutable
chmod +x migrate_db.sh

# Lancer la migration
./migrate_db.sh
```

### 5. Démarrage de l'application

#### Option A : Démarrage direct
```bash
./mvnw spring-boot:run
```

#### Option B : Démarrage avec Docker
```bash
# Construire et démarrer les conteneurs
docker-compose up -d
```

## Test de l'installation

```bash
# Rendre le script exécutable
chmod +x test_connections.sh

# Lancer les tests
./test_connections.sh
```

## Versions des dépendances

### Python
```
python-dotenv==1.0.0
ruamel.yaml==0.17.21
psycopg2-binary==2.9.9
SQLAlchemy==1.4.47
pymongo==4.6.1
```

### Java
```xml
<java.version>21</java.version>
<postgresql.version>42.7.5</postgresql.version>
<mongodb.version>4.11.1</mongodb.version>
```

### Bases de données
- PostgreSQL 14.11
- MongoDB 7.0.5

## Structure du projet
```
LOTO_API_v3/
├── src/
│   └── main/
│       ├── java/
│       └── resources/
├── docker/
├── scripts/
└── config/
```

## Commandes utiles

### Gestion des bases de données
```bash
# Démarrer les bases de données
./start_databases.sh

# Arrêter les bases de données
./stop_databases.sh

# Vérifier l'état
./status_databases.sh
```

### Maintenance
```bash
# Backup des données
./backup_databases.sh

# Restauration
./restore_databases.sh

# Nettoyage des logs
./clean_logs.sh
```

## Support

Pour toute question ou problème :
1. Consulter les logs : `./view_logs.sh`
2. Vérifier l'état des services : `./check_status.sh`
3. Contacter l'équipe de support

## Contributions

1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add some AmazingFeature'`)
4. Push la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

 -->

<!-- 📘 LOTO TRACKER API
Portfolio 2025 – Holberton School RENNES

Version : v2.0.0
Spécification : OpenAPI 3.0
Auteur : Stéphane Dinahet

PARTIE I — CONTEXTE & OBJECTIFS (1 → 10)

Le projet Loto Tracker API est une API REST sécurisée dédiée au suivi du Loto français.

Il permet la gestion complète des utilisateurs, tickets et résultats.

Il s’inscrit dans un objectif RNCP6 (Développeur Concepteur d’Applications).

Le projet est pensé pour être déployable en production.

Il respecte les standards REST, JWT, OAS 3.0.

Il utilise une architecture multi-bases (SQL + NoSQL).

Il est documenté intégralement via Swagger.

Il intègre une logique métier complète (tickets ↔ gains).

Il est conçu pour évoluer vers de l’IA prédictive.

Il est maintenable, testable et extensible.

PARTIE II — STACK TECHNIQUE DÉTAILLÉE (11 → 25)

Langage backend principal : Java 21

Framework backend : Spring Boot

Sécurité : Spring Security

Authentification : JWT (JSON Web Token)

Documentation API : Swagger / OpenAPI 3.0

Build & dépendances : Maven

Base relationnelle : PostgreSQL (ou MySQL compatible)

Base NoSQL : MongoDB

Frontend : HTML5 / CSS3 / JavaScript

Framework CSS : Bootstrap

Visualisation : Chart.js (en développement)

Scraping FDJ : Service interne Java

Planification : tâches programmées (cron-like)

OS recommandé : Linux / WSL2

Serveur cible : Alwaysdata (déploiement prévu)

PARTIE III — ARCHITECTURE GÉNÉRALE (26 → 40)

Architecture 3 tiers : Front / API / Données

Frontend statique séparé du backend

API REST découplée du client

PostgreSQL utilisé pour données transactionnelles

MongoDB utilisé pour données historiques volumineuses

Séparation stricte des responsabilités

Contrôleurs REST dédiés par domaine

Services métiers centralisés

DTO pour échanges API

Gestion des exceptions centralisée

Logs applicatifs configurés

Sécurité déclarative par annotations

Endpoints publics et protégés séparés

Gestion fine des rôles

Prêt pour montée en charge

PARTIE IV — SÉCURITÉ & AUTHENTIFICATION (41 → 55)

Authentification basée sur JWT

Token transmis via header Authorization

Token possible via cookie sécurisé

Rôles : ADMIN, USER

Protection des endpoints sensibles

Accès public limité

Pages d’erreur personnalisées

Filtrage CORS contrôlé

Sécurisation Swagger (login admin dédié)

Vérification de l’identité à chaque requête

Expiration du token

Rafraîchissement contrôlé

Aucune donnée sensible exposée

Logs d’accès disponibles

Conforme aux bonnes pratiques OWASP

PARTIE V — BASES DE DONNÉES (56 → 70)

PostgreSQL stocke les utilisateurs

PostgreSQL stocke les tickets

PostgreSQL stocke les gains

Relations normalisées

Identifiants uniques par utilisateur

MongoDB stocke l’historique FDJ

Historique depuis 2019

Indexation par date

Recherches rapides par plage

Séparation SQL / NoSQL justifiée

Intégrité référentielle assurée

Sauvegardes possibles

Migration possible vers cloud

Accès DB sécurisé

Données persistantes

PARTIE VI — ENDPOINTS API (71 → 90)

/api/auth/register – inscription

/api/auth/login4 – login JWT

/api/auth/logout – logout

/api/auth/me – user courant

/api/users – gestion users

/api/tickets – CRUD tickets

/api/gains – calcul gains

/api/tirages – tirages FDJ

/api/historique/last20 – derniers tirages

/api/predictions/latest – prédictions

/api/loto/scrape – scraping

/api/admin/users – admin users

/api/admin/tickets – admin tickets

/api/admin/ticket-gains – admin gains

/api/protected/userinfo – protégé

/api/hello – health check

/401 /403 /404 /500 – erreurs

Endpoints documentés Swagger

Respect REST

Versionnés

PARTIE VII — INSTALLATION & INITIALISATION (91 → 110)

OS recommandé : Ubuntu / WSL2

Installer Java 21

Installer Maven

Installer PostgreSQL

Installer MongoDB

Cloner le dépôt Git

Configurer les variables d’environnement

Créer la base SQL

Lancer MongoDB

Vérifier accès DB

Lancer mvn clean install

Lancer mvn spring-boot:run

Vérifier Swagger

Vérifier /api/hello

Créer un utilisateur

Se connecter

Créer un ticket

Lancer un calcul

Consulter historique

API opérationnelle

PARTIE VIII — DÉPLOIEMENT RNCP6 (111 → 125)

Choix Alwaysdata

Base PostgreSQL distante

MongoDB distant

Variables d’environnement sécurisées

HTTPS via reverse proxy

Accès public contrôlé

Logs serveur activés

Sauvegardes planifiées

Tests post-déploiement

Documentation publique

Scalabilité possible

Sécurité renforcée

Monitoring basique

Maintenance facilitée

Projet prêt production

CONCLUSION RNCP6

Ce projet démontre :

une maîtrise complète du backend moderne,

une architecture professionnelle,

une sécurité avancée,

une capacité de déploiement réel,

une documentation exhaustive,

👉 Conforme aux attentes RNCP6 / jury technique. -->


# 📘 LOTO TRACKER API
Portfolio 2026 – Holberton School RENNES

Version : v5.0.0

Spécification : OpenAPI 3.0

Auteur : Stéphane Dinahet

## 1. Introduction générale

Le projet Loto Tracker API est une application backend de type API REST sécurisée, destinée à centraliser, traiter et exposer les données relatives au Loto français.
Il permet à des utilisateurs de créer un compte, de soumettre des tickets de jeu, de consulter l’historique de leurs participations, et de comparer automatiquement leurs tickets avec les résultats officiels de la FDJ afin de déterminer d’éventuels gains.

Ce projet a été conçu comme un projet de fin de parcours à visée RNCP6, démontrant la capacité du candidat à concevoir, implémenter, sécuriser, documenter et préparer au déploiement une application backend professionnelle.

## 2. Objectifs pédagogiques et professionnels (RNCP6)

Le projet répond explicitement aux attendus du Titre RNCP Niveau 6 – Développeur Concepteur d’Applications, notamment :

- analyser un besoin métier et le traduire en fonctionnalités techniques ;

- concevoir une architecture logicielle robuste ;

- développer une API REST conforme aux standards actuels ;

- sécuriser les accès et les données ;

- gérer plusieurs systèmes de stockage adaptés aux usages ;

- produire une documentation technique exploitable par un tiers ;

- préparer un déploiement en environnement réel.

## 3. Description fonctionnelle détaillée

L’API couvre les fonctionnalités suivantes :

- Authentification sécurisée par JWT ;

- Gestion des utilisateurs avec rôles (Admin / Utilisateur) ;

- Gestion des tickets de loto (CRUD complet) ;

- Comparaison automatique des tickets avec les tirages FDJ ;

- Calcul des gains (fonctionnalité en cours d’optimisation) ;

- Historique des tickets utilisateur ;

- Historique public des résultats FDJ depuis 2019 ;

- Recherche par date ou plage de dates ;

- Scraping automatique des résultats officiels ;

- Administration avancée via endpoints dédiés.

## 4. Choix technologiques – justification détaillée
### 4.1 Backend – Java & Spring Boot

Le backend est développé en Java 21, version LTS moderne, garantissant :

- stabilité à long terme ;

- meilleures performances ;

- sécurité accrue.

Le framework Spring Boot a été retenu pour :

- sa maturité industrielle ;

- sa large adoption en entreprise ;

- son intégration native avec Spring Security ;

- sa capacité à produire rapidement des API REST robustes.

### 4.2 Sécurité – Spring Security & JWT

La sécurité repose sur :

- Spring Security pour le filtrage des requêtes ;

- JWT (JSON Web Token) pour l’authentification stateless.

Ce choix permet :

- une architecture scalable ;

- une séparation claire client / serveur ;

- l’absence de session côté serveur.

Les rôles (ADMIN, USER) sont embarqués dans le token afin de contrôler précisément l’accès aux ressources.

### 4.3 Documentation – OpenAPI / Swagger

L’API est documentée via OpenAPI Specification 3.0, exposée par Swagger UI.

Cette documentation constitue :

- un contrat technique entre backend et frontend ;

- une référence pour les tests ;

- un outil de démonstration lors de la soutenance RNCP.

### 4.4 Bases de données – choix multi-stockage
Base relationnelle (PostgreSQL)

Une base relationnelle est utilisée pour :

- les utilisateurs ;

- les tickets ;

- les gains ;

les relations entre entités.

Ce choix est motivé par :

- la nécessité d’intégrité référentielle ;

- la gestion des relations complexes ;

- la cohérence transactionnelle.

Base NoSQL (MongoDB)

- MongoDB est utilisée pour :

- stocker l’historique complet des tirages FDJ ;

- gérer un volume important de données ;

- faciliter les recherches par date.

Ce choix est justifié par :

- la flexibilité du schéma ;

- les performances en lecture ;

- l’adéquation aux données historiques.

## 5. Architecture générale de l’application

L’architecture suit un modèle 3-tiers :

- Frontend
Application cliente (navigateur) consommant l’API REST.

- Backend (API)
Cœur du système :

logique métier ;

sécurité ;

orchestration des données.

- Bases de données
Stockage persistant, sécurisé et structuré.

Cette architecture permet :

une évolutivité naturelle ;

un déploiement indépendant des composants ;

une maintenance facilitée.

## 6. Sécurité globale et conformité

L’application implémente :

- authentification JWT obligatoire pour les routes sensibles ;

- contrôle d’accès basé sur les rôles ;

- séparation claire des endpoints publics et protégés ;

- pages d’erreur personnalisées (401, 403, 404, 500) ;

- bonnes pratiques OWASP (principe du moindre privilège).

## 7. Installation – environnement système
### 7.1 Système recommandé

Linux (Ubuntu)

ou Windows avec WSL2

Ce choix est motivé par :

- la compatibilité avec les scripts ;

- la gestion simplifiée des services ;

- la proximité avec les environnements serveurs.

### 7.2 Prérequis logiciels

Les outils suivants sont requis :

- Java 21

- Maven

- PostgreSQL

- MongoDB

- Git

Vérification :

```bash
java -version
mvn -version
psql --version
mongod --version
```

### 7.3 Installation des dépendances (Ubuntu / WSL)

```bash
sudo apt update
sudo apt install -y \
  openjdk-21-jdk \
  maven \
  postgresql postgresql-contrib \
  mongodb \
  git

pip install "fastapi[standard]" uvicorn httpx pymongo python-dotenv
```

## 8. Installation du projet
### 8.1 Récupération du code source

```bash
git clone [<url-du-depot>](https://github.com/SDINAHET/LOTO_API_v4.git)
cd LOTO_API_v4
```

### 8.2 Initialisation de PostgreSQL

Démarrer le service :

```bash
sudo service postgresql start
```

Créer la base de données :

```bash
sudo -u postgres psql
CREATE DATABASE loto_tracker;
```

Configurer les accès dans application.properties ou via variables d’environnement.

### 8.3 Initialisation de MongoDB

Démarrer MongoDB :
```bash
sudo service mongod start
```

Aucune création manuelle n’est requise : les collections sont créées dynamiquement.

## 9. Build et lancement de l’application
### 9.1 Compilation du projet
```bash
mvn clean install
```

Cette étape :

- télécharge les dépendances ;

- compile le code ;

- exécute les tests éventuels.

### 9.2 Lancement du backend
```bash
mvn spring-boot:run
```

Le serveur démarre sur :

http://localhost:8082

## 10. Vérifications post-installation

Swagger UI accessible
http://localhost:8082/swagger-ui/index.html

Endpoint /api/hello répond

Inscription utilisateur fonctionnelle

Connexion JWT valide

Création de ticket possible

Consultation de l’historique FDJ

## 11. Préparation au déploiement (RNCP6)

Le projet est conçu pour être déployé sur Alwaysdata ou équivalent :

- API exposée via HTTPS ;

- bases de données sécurisées ;

- secrets stockés dans des variables d’environnement ;

- accès public limité aux endpoints nécessaires.

## 12. Évolutivité et perspectives

Le projet est prêt pour :

- conteneurisation Docker ;

- CI/CD ;

- montée en charge ;

- intégration d’IA prédictive ;

- notifications utilisateur.

## 13. Conclusion RNCP6

Le projet Loto Tracker API démontre :

- une maîtrise avancée du backend moderne ;

- une architecture professionnelle et sécurisée ;

- une capacité de déploiement réel ;

- une documentation complète et exploitable.

Il répond pleinement aux exigences RNCP6.


Test dans docker
```bash
docker compose -f docker-compose.test.yml up --abort-on-container-exit --remove-orphans

docker compose -f docker-compose.test.yml up --build --abort-on-container-exit --remove-orphans --exit-code-from tests


docker compose -f docker-compose.test.yml down -v
docker compose -f docker-compose.test.yml up --build --abort-on-container-exit



root@batriviere-serv1:~/Loto_API_prod/src/main/resources/static# sudo chmod -x /root/.nvm/versions/node/v20.19.2/bin/htt
p-server
root@batriviere-serv1:~/Loto_API_prod/src/main/resources/static# sudo chmod +x /root/.nvm/versions/node/v20.19.2/bin/htt
p-server
root@batriviere-serv1:~/Loto_API_prod/src/main/resources/static# lsof -i :5500
COMMAND    PID     USER   FD   TYPE    DEVICE SIZE/OFF NODE NAME
apache2 471765 www-data   44u  IPv4 156272529      0t0  TCP localhost:57356->localhost:5500 (CLOSE_WAIT)
apache2 471766 www-data   44u  IPv4 156282032      0t0  TCP localhost:55852->localhost:5500 (CLOSE_WAIT)
apache2 471767 www-data   44u  IPv4 156281107      0t0  TCP localhost:35594->localhost:5500 (CLOSE_WAIT)
apache2 471768 www-data   44u  IPv4 156275245      0t0  TCP localhost:57354->localhost:5500 (CLOSE_WAIT)
apache2 471801 www-data   44u  IPv4 156272531      0t0  TCP localhost:57360->localhost:5500 (CLOSE_WAIT)
apache2 471968 www-data   44u  IPv4 156273245      0t0  TCP localhost:58362->localhost:5500 (CLOSE_WAIT)
apache2 478177 www-data   44u  IPv4 156272533      0t0  TCP localhost:57376->localhost:5500 (CLOSE_WAIT)
apache2 478178 www-data   44u  IPv4 156273428      0t0  TCP localhost:57342->localhost:5500 (CLOSE_WAIT)
root@batriviere-serv1:~/Loto_API_prod/src/main/resources/static# http-server -p 5500


export default {
  async fetch(request) {
    const ORIGIN = "https://stephanedinahet.fr";
    const MAINTENANCE = "https://maintenance2-30r.pages.dev/";

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 2500);

    try {
      const url = new URL(request.url);
      const target = ORIGIN + url.pathname + url.search;

      const res = await fetch(target, {
        method: request.method,
        headers: request.headers,
        body: ["GET", "HEAD"].includes(request.method) ? null : request.body,
        signal: controller.signal,
      });

      clearTimeout(timeout);

      if ([502, 503, 504].includes(res.status)) {
        return Response.redirect(MAINTENANCE, 302);
      }

      return res;
    } catch (e) {
      clearTimeout(timeout);
      return fetch(MAINTENANCE);
    }
  },
};


cd src/main/resources/static
npx http-server -a 0.0.0.0 -p 5500

```
