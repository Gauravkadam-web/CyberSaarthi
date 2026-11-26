# CyberSaarthi — Setup & Deployment Guide
**Product Name:** CyberSaarthi  
**Version:** 1.0 | **Release:** v1.0.0  

This guide provides instructions for deploying and running CyberSaarthi locally, either via **Docker Compose** (recommended for evaluators and demonstration) or natively via individual service processes.

---

## 1. System Requirements & Prerequisites

### Minimum Hardware:
- **Processor:** Dual-core 2.0 GHz or higher (x86_64 or ARM64).
- **RAM:** 4 GB minimum (8 GB recommended for simultaneous local builds).
- **Disk Space:** 5 GB free disk space.

### Software Prerequisites:
- **Docker Engine:** Version 24+ and **Docker Compose** v2+ (for containerized deployment).
- **Native Development Tools (Alternative to Docker):**
  - **Java Development Kit (JDK):** Version 21 LTS (Eclipse Temurin or OpenJDK).
  - **Apache Maven:** Version 3.9+.
  - **Python:** Version 3.12+ with virtual environment support (`venv`).
  - **Node.js:** Version 18 LTS or 20 LTS with `npm`.
  - **PostgreSQL:** Version 14, 15, or 16.

---

## 2. Option A: Deployment via Docker Compose (Recommended)

Docker Compose orchestrates the four application tiers (PostgreSQL, FastAPI ML Service, Spring Boot Core Backend, and React Frontend) in isolated containers with automatic health-checking and dependency wiring.

### Step 1: Clone the Repository
```bash
git clone https://github.com/Gauravkadam-web/CyberSaarthi.git
cd CyberSaarthi
```

### Step 2: Launch All Services
```bash
docker compose up --build -d
```

### Step 3: Verify Container Health
```bash
docker compose ps
```
All four containers (`cybersaarthi-postgres`, `cybersaarthi-ml`, `cybersaarthi-backend`, `cybersaarthi-frontend`) will report status **healthy / Up**.

### Step 4: Seed Demo Data (Optional for Instant Showcase)
```bash
docker compose exec -T postgres psql -U postgres -d cybersaarthi_dev < backend/src/main/resources/db/demo/demo_seed_data.sql
```

### Step 5: Access the Application
- **Frontend Portal:** [http://localhost:3000](http://localhost:3000)
- **Spring Boot API & Swagger:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **FastAPI ML Docs:** [http://localhost:8000/docs](http://localhost:8000/docs)

---

## 3. Option B: Native Local Development Setup

### 3.1 Step 1: PostgreSQL Setup
1. Create a database named `cybersaarthi_dev`:
   ```sql
   CREATE DATABASE cybersaarthi_dev;
   ```
2. Verify connection on port `5432` with username `postgres` and password `postgres`.

### 3.2 Step 2: Python FastAPI ML Service Setup
```powershell
# Navigate to ml directory
cd ml

# Create virtual environment
python -m venv .venv

# Activate virtual environment
# Windows:
.\.venv\Scripts\Activate.ps1
# Linux/macOS:
source .venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Run automated tests
pytest -v tests

# Launch service on port 8000
uvicorn inference.main:app --host 127.0.0.1 --port 8000 --reload
```
*Health Check:* Open `http://localhost:8000/api/v1/health` in your browser.

### 3.3 Step 3: Spring Boot Core Backend Setup
In a new terminal window:
```powershell
# Navigate to backend directory
cd backend

# Run automated tests (Flyway will migrate in-memory test database)
mvn test

# Run the application (Flyway will migrate cybersaarthi_dev schema automatically)
mvn spring-boot:run
```
*Health Check:* Open `http://localhost:8080/swagger-ui.html`.

### 3.4 Step 4: React Frontend Setup
In a new terminal window:
```powershell
# Navigate to frontend directory
cd frontend

# Install npm packages
npm install

# Start Vite development server
npm run dev
```
*Access UI:* Open `http://localhost:3000` or the port displayed by Vite.

### 3.5 Step 5: Seeding Synthetic Demo Data
To populate 25+ incidents, quiz attempts, and reviews:
- **Windows PowerShell:**
  ```powershell
  .\scripts\seed_demo_data.ps1
  ```
- **Windows Command Prompt:**
  ```cmd
  scripts\seed_demo_data.bat
  ```

---

## 4. Default Demo Accounts

| Account Role | Email Address | Password |
|---|---|---|
| **Portal Administrator** | `admin@cybersaarthi.org` | `CyberSaarthi@123` |
| **Citizen Reporter** | `citizen@cybersaarthi.org` | `CyberSaarthi@123` |
| **Student Learner** | `student@cybersaarthi.org` | `CyberSaarthi@123` |

---

## 5. Troubleshooting & FAQ

- **Database Connection Refused:**
  Ensure PostgreSQL service is running (`net start postgresql-x64-16` on Windows or `sudo systemctl status postgresql` on Linux).
- **Port Conflicts:**
  If port `8080`, `8000`, `3000`, or `5432` is already in use, override them in `.env` or set `SERVER_PORT` / `PORT` environment variables.
- **ML Fallback Notice:**
  If the FastAPI ML service is stopped, Spring Boot will automatically activate the keyword rule fallback without failing citizen submissions (NFR-04).
