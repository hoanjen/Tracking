# TikTok Follower Tracker

A full-stack application to track TikTok followers, compare changes, and identify top unfollowers.

## Features

- **Google OAuth 2.0 PKCE Login** - Secure authentication with Google
- **Multi-Account Support** - Track multiple TikTok accounts
- **Daily Tracking Limit** - Create up to 5 trackings per day per account
- **Follower Comparison** - Compare followers between tracking sessions
- **Change Detection** - Identify who followed and unfollowed
- **Top Unfollowers List** - Track users who frequently unfollow
- **Copy to Clipboard** - Quick copy functionality for usernames

## Tech Stack

### Backend
- Spring Boot 4.1.0
- Spring Data JPA
- Spring Security
- PostgreSQL
- JWT Authentication

### Frontend
- React 19
- React Router v6
- Axios
- Google OAuth Library

## Setup Instructions

### Prerequisites
- Java 21+
- Node.js 16+
- PostgreSQL 12+
- Docker (optional, for quick database setup)

### Database Setup

**Option 1: Using Docker**
```bash
docker-compose up -d
```

**Option 2: Manual PostgreSQL**
```sql
CREATE DATABASE tracking;
```

### Backend Setup

1. Navigate to backend directory:
```bash
cd backend
```

2. Create `.env` file from `.env.example`:
```bash
cp .env.example .env
```

3. Update `.env` with your configuration:
```
DB_URL=jdbc:postgresql://localhost:5432/tracking
DB_USERNAME=postgres
DB_PASSWORD=postgres
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
JWT_SECRET=your-secure-secret-key
```

4. Build and run:
```bash
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend Setup

1. Navigate to frontend directory:
```bash
cd frontend
```

2. Create `.env` file from `.env.example`:
```bash
cp .env.example .env
```

3. Update `.env`:
```
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_GOOGLE_CLIENT_ID=your-google-client-id
```

4. Install and run:
```bash
npm install
npm start
```

The frontend will start on `http://localhost:3000`

## API Documentation

### Authentication
- `POST /api/auth/login` - Login with Google token
- `GET /api/auth/me` - Get current user

### TikTok Accounts
- `POST /api/tiktok-accounts` - Create new TikTok account
- `GET /api/tiktok-accounts` - Get user's accounts
- `DELETE /api/tiktok-accounts/{id}` - Delete account

### Tracking
- `POST /api/tracking/{accountId}/create-tracking` - Create tracking session
- `GET /api/tracking/{accountId}/history` - Get tracking history

### Top Unfollowers
- `GET /api/top-unfollowers/{accountId}` - Get top unfollowers list

## Database Schema

### Users
- id (PK)
- googleId (unique)
- email
- name
- createdAt
- updatedAt

### TikTokAccounts
- id (PK)
- userId (FK)
- username (unique per user)
- createdAt
- updatedAt

### Trackings
- id (PK)
- tikTokAccountId (FK)
- createdAt

### Reports
- id (PK)
- trackingId (FK)
- totalFollowers
- createdAt

### FollowerChanges
- id (PK)
- reportId (FK)
- username
- changeType (added/removed)
- previousReportId

### TopUnfollowers
- id (PK)
- tikTokAccountId (FK)
- username (unique per account)
- unfollowCount
- createdAt
- updatedAt

## Environment Variables

### Backend (.env)
- `DB_URL` - PostgreSQL connection URL
- `DB_USERNAME` - Database user
- `DB_PASSWORD` - Database password
- `SERVER_PORT` - Server port (default: 8080)
- `JWT_SECRET` - JWT signing secret
- `JWT_EXPIRATION` - JWT expiration in seconds (default: 604800 = 7 days)
- `GOOGLE_CLIENT_ID` - Google OAuth client ID
- `GOOGLE_CLIENT_SECRET` - Google OAuth secret
- `GOOGLE_REDIRECT_URI` - OAuth redirect URI
- `TIKTOK_API_BASE_URL` - TikTok API base URL
- `TIKTOK_ACCESS_TOKEN` - TikTok API access token

### Frontend (.env)
- `REACT_APP_API_URL` - Backend API URL
- `REACT_APP_GOOGLE_CLIENT_ID` - Google OAuth client ID

## Running in Development

Terminal 1 - Database:
```bash
docker-compose up
```

Terminal 2 - Backend:
```bash
cd backend
mvn spring-boot:run
```

Terminal 3 - Frontend:
```bash
cd frontend
npm start
```

## Project Structure

```
.
├── backend/
│   ├── src/main/java/com/tracking/
│   │   ├── config/          - Spring configurations
│   │   ├── controller/      - REST controllers
│   │   ├── dto/             - Data transfer objects
│   │   ├── entity/          - JPA entities
│   │   ├── mapper/          - Entity to DTO mappers
│   │   ├── repository/      - Data access layer
│   │   ├── service/         - Business logic
│   │   ├── security/        - JWT and security config
│   │   ├── exception/       - Exception handlers
│   │   └── util/            - Utility classes
│   ├── resources/
│   │   └── application.yml  - Application configuration
│   └── pom.xml              - Maven dependencies
├── frontend/
│   ├── src/
│   │   ├── components/      - React components
│   │   ├── pages/           - Page components
│   │   ├── services/        - API services
│   │   ├── hooks/           - Custom React hooks
│   │   ├── context/         - React context
│   │   ├── utils/           - Utility functions
│   │   └── styles/          - Global styles
│   ├── package.json         - NPM dependencies
│   └── .env.example         - Environment template
└── docker-compose.yml       - Docker compose for database
```

## Key Implementation Details

### JWT Authentication
- Access token only (no refresh token)
- 7-day expiration
- HS512 signing algorithm
- Extracted from Authorization header (Bearer token)

### Daily Tracking Limit
- Maximum 5 trackings per TikTok account per calendar day
- Enforced at service layer
- Tracked via SQL query on Tracking table

### Follower Comparison
- Compares latest two tracking sessions
- Detects added followers and removed followers
- Automatically updates TopUnfollowers table

### Google OAuth Flow
- Frontend: Google Sign-In button redirects to Google
- Frontend: Sends ID token to backend
- Backend: Verifies token via Google API
- Backend: Creates or retrieves user, generates JWT
- Frontend: Stores JWT in localStorage

## Future Enhancements

- Export reports to CSV/PDF
- Scheduled tracking (automatic)
- Follower growth charts
- Notification system
- Mobile app
- Real-time updates with WebSocket

## License

MIT
# Tracking
# Tracking
# Tracking
# Tracking
# Tracking
