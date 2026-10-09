# Rental Hub - Final JWT Security

Full-stack rental management project using React + Vite frontend and Spring Boot backend.

## Run backend
```
cd backend
mvn spring-boot:run
```
Backend: http://localhost:8081

## Run frontend
```
cd frontend
npm install
npm run dev
```
Frontend: http://localhost:5173

## Demo accounts
Admin: `admin@rentalhub.com` / `admin123`
User: `user@rentalhub.com` / `user123`

## Security
- BCrypt password hashing
- JWT authentication
- 30-minute JWT expiry
- Stateless Spring Security
- Role-based authorization (ADMIN/USER)
- Protected user/application endpoints
- Safe user/application response DTOs; password hashes are never returned by these APIs
- Frontend route protection and inactivity timeout
- CORS restricted to the Vite frontend

See `POSTMAN-SECURITY-TESTS.md` for the complete Postman checklist.


## Approved Application - Property Owner Details
When an admin approves a rental application, the user's My Applications dashboard displays the approved property's owner name, phone number, and email. These owner contact fields are hidden from the user while the application is pending/rejected/cancelled and are returned to the user only for an APPROVED application. Admins enter owner details when creating or updating a property.
