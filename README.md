# HelpDesk AI - AI-Powered Smart Customer Support & Complaint Management System
VCube Hack Quest 1.0 - Problem 6 (Customer Service)

## Stack
Java 17 · Spring Boot 3.4 · Spring Data JPA · Spring Security (JWT) · REST APIs · MySQL · **Spring AI** (OpenAI-compatible LLM) · React 18 (Vite) · Git/GitHub · Postman · Render

## Features
**Customer pages:** Register -> Login -> Check-in (dashboard) -> Raise complaint -> Track complaints (status stepper, conversation, star feedback) + floating **AI assistant** chat.
**Support desk (agent/admin):** analytics, all tickets, status/priority change, assignment, internal notes, AI resolution + AI reply drafts.

| Requirement | Where |
|---|---|
| Registration / Login | `AuthController`, JWT, BCrypt; pages Register, Login |
| Complaint submission + ticket generation (`TKT-1001`...) | `TicketService.create` |
| Categorization | AI picks from the `categories` table |
| Priority management / prediction | AI sets LOW-CRITICAL, staff can override |
| Ticket assignment | auto-assign to least-loaded agent + manual (`ticket_assignments`) |
| Status tracking | OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED |
| Customer feedback | 1-5 stars + text after resolution |
| Support analytics | `/api/staff/analytics`, Support desk page |
| **AI:** classification, sentiment, priority, suggested resolution, customer response | `AiService` (Spring AI `ChatClient`, backend only) |
| AI uses real app data | categories list in prompt, ticket data for resolution/reply, customer's tickets for assistant |

If the LLM key is missing/unreachable, `AiService` falls back to keyword rules so the app never breaks.

## Run locally
**1. MySQL** running locally (tables are auto-created; DB `support_db` is auto-created).
**2. Backend**
```bash
cd backend
export DB_USER=root DB_PASSWORD=yourpassword
export AI_API_KEY=sk-...            # any OpenAI-compatible key
# optional: AI_BASE_URL (e.g. https://api.groq.com/openai) and AI_MODEL
mvn spring-boot:run                 # http://localhost:8080
```
**3. Frontend**
```bash
cd frontend
cp .env.example .env
npm install && npm run dev          # http://localhost:5173
```
Demo staff logins (seeded): `admin@support.com / Admin@123`, `agent@support.com / Agent@123`, `agent2@support.com / Agent@123`. Customers register themselves.

## Deploy on Render
1. Create a MySQL database (Aiven / Railway / any cloud MySQL) and note host, user, password.
2. Push to GitHub -> Render -> New -> Blueprint -> pick the repo (`render.yaml`).
3. Set `DB_URL` (`jdbc:mysql://HOST:PORT/DB?useSSL=true&serverTimezone=UTC`), `DB_USER`, `DB_PASSWORD`, `AI_API_KEY`.
4. After both deploy: set `VITE_API_URL` on the web service to the API URL (then redeploy) and `CORS_ORIGINS` on the API to the frontend URL.

## Submission checklist
- GitHub repo URL · Live URL · `docs/ER-Diagram.md` (export PNG via mermaid.live) · `docs/AI-Customer-Support.postman_collection.json` · this README · demo
