# Rental Hub - Postman Security Tests

Backend: `http://localhost:8081`

## Demo accounts
- Admin: `admin@rentalhub.com` / `admin123`
- User: `user@rentalhub.com` / `user123`

## Login
POST `/api/auth/login`

Admin or user login returns:
```json
{
  "user": {"id": 1, "name": "Rental Hub Admin", "email": "admin@rentalhub.com", "role": "ADMIN"},
  "token": "eyJ..."
}
```
Copy the token and use Postman Authorization -> Bearer Token for protected requests.

## Security tests
1. GET `/api/properties` without token -> **200** (public browsing).
2. GET `/api/applications` without token -> **401 Unauthorized**.
3. POST `/api/applications` with no token -> **401**.
4. Login with wrong password -> **401**.
5. Admin login -> **200 + token + role ADMIN**.
6. User login -> **200 + token + role USER**.
7. GET `/api/applications` with admin token -> **200**.
8. GET `/api/applications` with user token -> **403 Forbidden**.
9. POST/PUT/DELETE `/api/properties` with user token -> **403**.
10. POST `/api/applications` with user token and `{ "propertyId": 1 }` -> **201**.
11. GET `/api/applications/user/{userId}` with the same user token -> **200**.
12. GET another user's applications with user token -> **403**.
13. Approve/reject/cancel-approval with admin token -> permitted according to status.
14. Cancel another user's application with user token -> **403**.
15. Delete another user's account with user token -> **403**.
16. Admin can delete a normal user -> **204**.
17. Admin account deletion -> **403**.

For protected requests, use `Authorization: Bearer <token>`.
