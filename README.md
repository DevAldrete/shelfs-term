# Shelfs

A lightweight, terminal-based library management system written in plain Java 25.

## Features

- **Authentication** — login, sign-up, and session management with role-based access control (RBAC)
- **Browse Books** — available to every logged-in user; search by title, author, or ISBN; see all physical copies with their barcodes and availability status
- **Manage Books** *(admin only)* — add new titles or extra copies, remove copies by barcode, update book metadata
- **Manage Users** *(admin only)* — register, list, update, remove users; upgrade members to administrators
- **Manage Loans** — members can loan and return books and view their own active loans; administrators get full visibility over all loans, loans by user, and overdue items
- **Data persistence** — state is saved to `data/` as human-readable JSON on logout or exit and automatically reloaded on the next startup; no database or external library required

## Requirements

| Tool | Version |
|------|---------|
| Java | 25      |
| Maven | 3.x   |
| just *(optional)* | any |

## Running

```bash
# Recommended (compiles + runs)
just jrun

# With Maven directly
mvn compile exec:java

# Clean build artifacts
mvn clean
```

## Default credentials

| Role | Email | Password |
|------|-------|----------|
| Administrator | admin@example.com | passwordsafe |
| Member | john@example.com | password123 |
| Member | anna@example.com | password123 |

> These seed accounts are only created on first launch. Once `data/users.json` exists, the persisted state is loaded instead.

## Project structure

```
src/main/java/com/devaldrete/
├── App.java                    # Entry point: auth loop + main menu
├── IO.java                     # Console I/O utility (not in repo)
├── domain/                     # Plain POJOs: User, Administrator, Member,
│                               #   BookDefinition, BookItem, Loan, Role, Status
├── repositories/               # In-memory CRUD stores
├── services/
│   ├── AuthService.java        # Login / signup / session state
│   ├── BookService.java        # Book business logic
│   ├── LoanService.java        # Loan business logic (14-day period, max 2 loans)
│   ├── Library.java            # Facade: wires services + owns console menus
│   ├── PersistenceService.java # JSON save/load (no external dependencies)
│   └── UserService.java        # User business logic
└── utils/
    ├── BarcodeGenerator.java   # BC-{8-char UUID} uppercase
    └── ISBNGenerator.java      # ISBN-13 generation + validation
```

## Persistence

Data is stored in three JSON files under `data/` (created automatically):

| File | Contents |
|------|----------|
| `data/users.json` | All user accounts |
| `data/books.json` | Book definitions and physical copies |
| `data/loans.json` | Active loans |

The files are written on **logout** and on **exit**. They are read once at **startup** if they already exist.

## Business rules

- A user may hold at most **2 active loans** simultaneously.
- The loan period is **14 days**.
- Barcodes follow the format `BC-XXXXXXXX` (uppercase hex).

## Tests

```bash
mvn test
```

Tests live in `src/test/java/com/devaldrete/` and use JUnit 3.
