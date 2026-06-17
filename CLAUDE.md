# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Quick Start Commands

### Backend (Spring Boot)
```bash
# Build
cd backend
mvn clean install

# Run development server
mvn spring-boot:run

# Run tests
mvn test

# Run single test class
mvn test -Dtest=UserRepositoryTest

# Run single test method
mvn test -Dtest=UserRepositoryTest#testFindByGoogleId

# Format code
mvn spotless:apply

# Check dependencies for vulnerabilities
mvn dependency-check:check
```

### Frontend (React)
```bash
# Install dependencies
cd frontend
npm install

# Run development server
npm start

# Build for production
npm run build

# Run tests
npm test

# Format code
npx prettier --write "src/**/*.{js,css,html}"

# Lint
npx eslint src
```

### Database
```bash
# Start PostgreSQL with Docker
docker-compose up -d

# Stop database
docker-compose down

# View database logs
docker-compose logs -f postgres

# Access database via psql
docker exec -it tracking-postgres psql -U postgres -d tracking
```

### Full Development Stack
```bash
# Terminal 1: Database
docker-compose up

# Terminal 2: Backend
cd backend && mvn spring-boot:run

# Terminal 3: Frontend
cd frontend && npm start
```

## Architecture Overview

### System Design

**Authentication Flow:**
- Google OAuth 2.0 PKCE → Backend JWT token (7-day expiration, no refresh)
- JWT stored in localStorage on frontend
- Interceptor adds `Authorization: Bearer <token>` to all API requests

**Core Domain Model:**
```
User (Google OAuth)
  ├─ TikTokAccount (username)
      ├─ Tracking (session, once per day max 5)
      │   └─ Report (snapshot with followers count)
      │       └─ FollowerChange (added/removed usernames)
      └─ TopUnfollower (aggregated unfollow stats)
```

**Key Relationships:**
- 1 User → Many TikTokAccounts
- 1 TikTokAccount → Many Trackings
- 1 Tracking → 1 Report (latest per tracking)
- 1 Report → Many FollowerChanges
- 1 TikTokAccount → Many TopUnfollowers (unique username)

### Backend Structure

**Feature-Based Package Layout** (`src/main/java/com/tracking/`):
- `config/` - Spring Security, CORS, application beans
- `entity/` - JPA entities with @PrePersist/@PreUpdate hooks for timestamps
- `repository/` - Spring Data JPA interfaces (custom @Query for daily limit check)
- `service/` - Business logic (TrackingService handles follower comparison)
- `controller/` - REST endpoints, Authentication extraction from JWT principal
- `dto/` - Request/response objects (thin mappers for entity↔DTO)
- `security/` - JwtTokenProvider (HS512), JwtAuthenticationFilter
- `util/` - GoogleTokenVerifier (calls Google API to validate token)
- `mapper/` - Entity to DTO converters (UserMapper, TikTokAccountMapper)

**Important Implementation Details:**

1. **Daily Tracking Limit**
   - `TrackingRepository.countTodayByTikTokAccountId()` uses `DATE(t.createdAt) = CURRENT_DATE`
   - Enforced before Tracking entity creation
   - Returns 400 if limit exceeded

2. **Follower Comparison Logic** (TrackingService.compareFollowers)
   - Fetches previous report's follower set from FollowerChange records
   - Diff: added = current \ previous, removed = previous \ current
   - Updates TopUnfollower table atomically on unfollow detection

3. **JWT Token Generation**
   - Subject = userId (Long), Claim "email" = user email
   - Uses HMAC SHA-512, keys derived from UTF-8 secret
   - Expiration in seconds (default 604800 = 7 days)

4. **Database Auto-Creation**
   - `spring.jpa.hibernate.ddl-auto=update` in application.yml
   - Entities have @PrePersist for createdAt, @PreUpdate for updatedAt
   - Unique constraints on (username, user_id) for TikTokAccount
   - Unique constraint on (tikTokAccountId, username) for TopUnfollower

### Frontend Structure

**Component Hierarchy:**
```
App
├─ AuthProvider (context)
├─ LoginPage (Google Sign-In)
└─ ProtectedRoute
    └─ Dashboard
        ├─ TikTokAccounts (add/select account, create tracking)
        ├─ Reports (view comparison, expand details)
        └─ TopUnfollowers (ranked table)
```

**State Management:**
- AuthContext: `{ user, token, isAuthenticated, login, logout }`
- Component-level state for forms (useState)
- localStorage persists JWT token across page reloads

**API Integration:**
- Axios instance with auto-header interceptor (adds Bearer token)
- Service layer (`services/api.js`) exports namespaced functions
  - `authService.login(googleToken)`
  - `tiktokService.createAccount(username)`
  - `trackingService.createTracking(accountId, followers)`
  - `topUnfollowerService.getTopUnfollowers(accountId)`

**UI Patterns:**
- Follower list rendered as array of FollowerChangeDto with copy buttons
- Reports expandable (details shown on header click)
- Top unfollowers table with rank, username, unfollow count
- All timestamps localized to user's browser

## Configuration & Environments

**Backend (application.yml):**
- Database URL/credentials from env vars (falls back to localhost defaults)
- JWT secret/expiration from env
- CORS origin hardcoded to `http://localhost:3000` (change for production)
- Logging: root=INFO, com.tracking=DEBUG

**Frontend (.env):**
- `REACT_APP_API_URL` - backend endpoint (default localhost:8080/api)
- `REACT_APP_GOOGLE_CLIENT_ID` - OAuth client ID

**Production Checklist:**
1. Update CORS origin in SecurityConfig (no localhost)
2. Change JWT_SECRET to cryptographically secure value
3. Set up proper database in production environment
4. Configure Google OAuth redirect URI to match frontend domain
5. Use environment-specific application profiles (`application-prod.yml`)

## Testing Strategy

**Backend:**
- Unit tests for services (mock repositories)
- Integration tests for repositories (use @DataJpaTest with TestDatabase)
- Controller tests (mock services, test HTTP status and response structure)
- No tests for mappers (single-line conversions)

**Frontend:**
- Component tests using React Testing Library
- API mock with MSW or jest.mock
- Focus on user interactions, not implementation details

## Common Patterns & Conventions

### Exception Handling
- Backend: Throw RuntimeException with descriptive message, return 400
- Frontend: Catch axios errors, show alert() (can be replaced with toast)
- No custom exception hierarchy yet (add GlobalExceptionHandler when needed)

### Entity Timestamps
- All entities have createdAt (immutable) and updatedAt (auto-updated)
- Use @PrePersist/@PreUpdate hooks, never manually set
- Timezone: UTC (JDBC driver handles)

### Follower Change Storage
- FollowerChange stores username string, not username_id (no separate Username entity)
- changeType = "added" or "removed" (enum candidate)
- previousReportId is reserved for future optimization (not yet used)

### Copy-to-Clipboard
- Frontend uses `navigator.clipboard.writeText()`, shows confirmation alert
- No error handling for clipboard access denial

## Git Workflow

- Feature branches off `main`
- Commit messages: `<type>(<scope>): <message>` (e.g., `feat(auth): add JWT token validation`)
- Keep .env files out of git (use .env.example)
- Backend pom.xml and frontend package.json must be committed

## Known Limitations & Future Work

1. **TikTok API Integration** - Currently accepts follower list as manual input. Replace with real TikTok API (official API has strict quotas)
2. **Email Notifications** - No notification when tracking limit hit or unfollowers detected
3. **Refresh Tokens** - JWT-only, no refresh mechanism (7-day expiration is hard limit)
4. **Rate Limiting** - No API rate limiter on backend
5. **Data Validation** - Minimal client-side validation (add Formik/Yup for forms)
6. **Error Codes** - Backend returns generic 400/500, consider structured error codes
7. **TopUnfollower TTL** - Never expires, consider archival strategy for old records

## Security Notes

- Google token verified server-side via Google API (critical)
- CORS restricted to frontend origin
- JWT stored in localStorage (XSS vulnerable, consider HttpOnly cookies)
- SQL injection protected (Spring Data JPA parameterized queries)
- No CSRF protection needed (stateless JWT auth)
- Ensure JWT_SECRET is not committed to repo

## Performance Considerations

- FollowerChange list fetched eagerly for reports (lazy load if 1000+ followers)
- TopUnfollower table unsorted (add index on (tikTokAccountId, unfollowCount DESC))
- No pagination on reports (add limit/offset if history grows large)
- Frontend: Reports list renders all expanded/collapsed, consider virtualization for 1000+

## Useful Files

- `backend/src/main/resources/application.yml` - All configurable properties
- `backend/pom.xml` - Dependency versions (JJWT 0.12.3, Spring Boot 4.1.0)
- `frontend/.env.example` - Required env vars for frontend
- `docker-compose.yml` - Database container definition
- `README.md` - User-facing setup instructions
