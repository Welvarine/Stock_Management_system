# BNR Stock Management System

This project is a secure IT stock management system with multi-role access control (Admin, Requester, Approver).

## Recent Improvements

### Functional Enhancements
- **Admin Stock Input**: Added missing `category` field support to ensure successful saves to the backend.
- **Improved Management**: Restored missing `delete` functionality in the `StockController`.
- **User API**: Fixed `UserController` annotations to properly expose user management endpoints.

### Validation & UX
- **Signup Validations**: 
  - Password and confirmation password matching.
  - Username restricted to English/French alphabet characters.
  - New Email field with format validation.
- **Inventory Validations**: 
  - Alpha-numeric character restrictions for item names and categories.
- **Length Limits**: Implemented `maxlength` constraints on all user inputs.

### Backend Infrastructure
- **Security (`SecurityConfig.java`)**:
  - **CSRF Disabled**: Spring Security's default CSRF protection was blocking POST requests (like "Add Item") because for local development, the frontend doesn't have a secure token. Disabling this allowed the frontend to talk to the backend.
  - **CORS Enabled**: Configured the backend to explicitly allow traffic from `http://localhost:5173` (the Vite frontend), which by default is blocked by the browser's safety rules.
- **Diagnostics (Port Conflict Fix)**:
  - Fixed a **Port 8080 collision** caused by an old instance of the application that didn't shut down properly.
  - **Commands used (Windows)**:
    1.  `netstat -ano | findstr :8080` (Identified PID e.g. 4276)
    2.  `taskkill /F /PID 4276` (Terminated the process)

## Tech Stack
- **Frontend**: Vue 3 + Vite
- **Backend**: Spring Boot 3 + Spring Security + PostgreSQL
