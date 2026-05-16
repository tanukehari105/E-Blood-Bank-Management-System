# Blood Bank Management System

## ✅ STATUS: FIXED AND RUNNING!

All errors have been resolved. Both backend and frontend are running successfully!

A full-stack capstone project using Java Swing + Spring Boot + SQLite.

## Architecture
```
Java Swing (FlatLaf UI) → REST API → Spring Boot → SQLite
```

## Prerequisites
- Java 17+
- Maven 3.8+

## Running the Application

### Step 1: Start the Backend
```bash
cd backend
mvn spring-boot:run
```
Backend runs on http://localhost:8080

### Step 2: Start the Frontend
```bash
cd frontend
mvn compile exec:java -Dexec.mainClass="com.bloodbank.ui.BloodBankApp"
```

Or use the provided scripts:
- Windows: `start-backend.bat` and `start-frontend.bat`
- Linux/Mac: `start-backend.sh` and `start-frontend.sh`

## Default Credentials
| Username | Password  | Role  |
|----------|-----------|-------|
| admin    | admin123  | ADMIN |
| staff    | staff123  | STAFF |

## Features
- **Dashboard** - Stats, blood stock overview, alerts, charts
- **Donor Management** - CRUD, eligibility check (90-day rule), search/filter
- **Blood Inventory** - Stock management, expiry tracking, low-stock alerts
- **Donations** - Register donations, auto-update inventory & donor record
- **Blood Requests** - Submit/approve/reject requests, smart donor matching
- **Reports** - Generate and export PDF/CSV reports

## API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/login | Login |
| GET/POST | /api/donors | List/Create donors |
| PUT/DELETE | /api/donors/{id} | Update/Delete donor |
| GET | /api/donors/eligible | Get eligible donors |
| GET/POST | /api/inventory | List/Add inventory |
| GET | /api/inventory/stock | Stock by blood group |
| GET/POST | /api/donations | List/Register donations |
| GET/POST | /api/requests | List/Create requests |
| PUT | /api/requests/{id}/approve | Approve request |
| PUT | /api/requests/{id}/reject | Reject request |
| GET | /api/dashboard | Dashboard stats |

## Database
SQLite database (`bloodbank.db`) is auto-created in the backend directory on first run.
