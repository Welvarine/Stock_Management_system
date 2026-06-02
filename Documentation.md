# BNR Stock Management System — Project Documentation

## 1. Project Overview
The **BNR Stock Management System** is a professional, high-security enterprise application designed to manage IT inventory, track stock requests, and maintain a rigorous audit trail. The system features a modern, rounded aesthetic with a primary focus on user experience and data integrity.

---

## 2. Technical Architecture
The application is built using a modern decoupled architecture:

*   **Backend**: Spring Boot 3 (Java)
    *   **Security**: Spring Security with BCrypt password hashing.
    *   **Persistence**: Hibernate / JPA with a relational database (PostgreSQL).
    *   **Mailing**: JavaMailSender integrated with SMTP for real-time OTP delivery.
*   **Frontend**: Vue 3 + Vite
    *   **State Management**: Pinia (Centralized stores for Auth, Inventory, Requests, and Currency).
    *   **Icons**: Lucide-Vue-Next for a consistent, professional look.
    *   **Styling**: Vanilla CSS with modern design tokens (Glassmorphism, CSS Variables).

---

## 3. User Roles & Access Control
The system implements four distinct roles, each with specific permissions:

| Role | Responsibilities |
| :--- | :--- |
| **System Admin** | Oversees all users, monitors system stats, reviews role change requests, and audits logs. |
| **Stock Admin** | Manages inventory (Add, Edit, Delete), handles the Trash system, and exports reports. |
| **Approver** | Reviews stock requests from requesters and provides rejection reasons for denied items. |
| **Requester** | Views available stock and submits requests for items needed for operational tasks. |

---

## 4. Key Features

### 📦 Inventory Management (Stock Admin)
*   **Full CRUD**: Add, Edit, and Delete items with category-specific tracking.
*   **Trash System**: Deleted items are moved to a "Trash" tab, allowing for recovery or permanent deletion (Soft Delete).
*   **Report Export**: Export the current inventory to Excel format for offline reporting.

### 💸 Dynamic Currency System
*   **Live Exchange Rates**: Fetches real-time rates (USD/EUR) relative to the base currency (RWF).
*   **Instant Toggle**: Switch between RWF, USD, and EUR across the entire application instantly.
*   **Persistence**: Prices are always stored in RWF to maintain accounting accuracy.

### 🤝 Request Workflow
*   **Requester Panel**: Browse available stock and submit requests with quantity validation.
*   **Approver Panel**: Review pending requests with clear action buttons.
*   **Rejection Reasoning**: Approvers must provide a reason from a curated list (or custom text) when rejecting a request.

### 🛡️ System Administration & Security
*   **Audit Logs**: A central database tracking every login, logout, stock creation, and role change.
*   **User Directory**: Monitor all registered users and their online/offline status.
*   **Role Upgrade with OTP**: Users can request a higher role (e.g., Requester -> Approver). Upon approval, an OTP is sent to their email to finalize the upgrade.
*   **Self-Healing Passwords**: A security initializer that automatically upgrades legacy plain-text passwords to BCrypt hashing on system startup.

---

## 5. UI/UX Design Standards
The project follows a "Premium & Professional" design language:
*   **Inline Feedback**: Replaced intrusive browser alerts with custom `.inline-alert` components (Success/Danger).
*   **Aesthetic Borders**: 32px corner rounding on cards for a modern, friendly feel.
*   **Responsive Tables**: Pagination (7 items per page) ensures all content fits on a single screen without vertical scrolling.
*   **Micro-animations**: Subtle hover effects and fade-in transitions for a "living" interface.

---

## 6. Setup & Installation

### Prerequisites
*   JDK 21
*   Node.js (LTS)
*   Maven 3.x
*   SMTP Server credentials (e.g., Gmail App Password)

### Backend Setup
1.  Configure `.env` or `application.properties` with database and SMTP details.
2.  Run `mvn spring-boot:run`.
3.  The system will automatically initialize a `sysadmin` account if the database is empty.

### Frontend Setup
1.  `cd frontend`
2.  `npm install`
3.  `npm run dev`

---

## 7. Audit Log Data Flow
1.  **Event**: A user performs an action (e.g., login).
2.  **Capture**: The Backend Controller creates an `AuditLog` entry.
3.  **Persistence**: The entry is saved to the `audit_logs` table.
4.  **Retrieval**: The System Admin Dashboard fetches logs via `GET /users/logs`.
5.  **Visualization**: The frontend maps technical operation codes (e.g., `STOCK_CREATE`) to human-readable badges and colors.
