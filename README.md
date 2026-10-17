# CourseCraft AI

CourseCraft AI is a full-stack course generation platform that turns a topic prompt into a structured curriculum with modules, lessons, progress tracking, AI-generated lesson content, quizzes, and recommended YouTube videos.

The project is designed to run locally with a lightweight demo auth mode, then scale to production with MongoDB Atlas, Auth0, and a real AI provider such as Gemini or OpenRouter.

## Features

- Generate complete course outlines from a single topic.
- Browse generated courses, modules, and lesson summaries.
- Generate rich lesson content on demand.
- Track completed lessons and assessment scores per user.
- Search for relevant YouTube videos for lessons.
- Download generated lessons as styled PDFs.
- Generate Hinglish audio explanations for lessons with Gemini TTS.
- Run with local demo authentication or Auth0 JWT authentication.
- Use template output for local development or Gemini/OpenRouter for real AI generation.

## Tech Stack

- Frontend: React 19, Vite, React Router, Tailwind CSS, Axios, Auth0 React SDK.
- Backend: Java 21, Spring Boot 4, Spring Web MVC, Spring Security, OAuth2 Resource Server, Spring Data MongoDB.
- Database: MongoDB local, Docker, or MongoDB Atlas.
- CI: GitHub Actions for backend tests, frontend linting, and frontend builds.

## Repository Structure

```text
CourseCraft-AI/
|-- backend/                 # Spring Boot API
|   |-- src/main/java/       # Controllers, services, models, auth, AI providers
|   |-- src/main/resources/  # Spring application configuration
|   |-- src/test/java/       # Backend tests
|   |-- .env.example         # Backend environment template
|   |-- Dockerfile           # Production backend image
|   `-- pom.xml
|-- frontend/                # Vite React app
|   |-- src/                 # Pages, layouts, components, API clients, auth context
|   |-- .env.example         # Frontend environment template
|   |-- vercel.json          # SPA rewrites for Vercel
|   `-- package.json
|-- .github/workflows/ci.yml # CI pipeline
`-- README.md
```

## Prerequisites

Install the following before running the project:

- Java 21.
- Node.js 22 and npm.
- MongoDB 7 or a MongoDB Atlas cluster.
- Git.
- Optional: Auth0 application and API for production authentication.
- Optional: Gemini API key or OpenRouter API key for real AI generation.
- Optional: YouTube Data API v3 key for lesson video search.

## Quick Start

Clone the repository:

```bash
git clone <repository-url>
cd CourseCraft-AI
```

Create backend environment variables:

```bash
cp backend/.env.example backend/.env
```

For a local MongoDB instance, use:

```env
MONGODB_URI=mongodb://localhost:27017/coursecraft
CORS_ALLOWED_ORIGINS=http://localhost:5173
AI_PROVIDER=template
JWT_AUTH_ENABLED=false
LOCAL_DEV_USER_ID=local-dev-user
```

Create frontend environment variables:

```bash
cp frontend/.env.example frontend/.env
```

Use the local backend URL:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Start MongoDB locally. If you use Docker:

```bash
docker run --name coursecraft-mongo -p 27017:27017 -d mongo:7
```

Start the backend:

```bash
cd backend
set -a
source .env
set +a
./mvnw spring-boot:run
```

In a second terminal, start the frontend:

```bash
cd frontend
npm install
npm run dev
```

Open the app at [http://localhost:5173](http://localhost:5173).

## Local Development Workflow

Run backend tests:

```bash
cd backend
./mvnw test
```

Run frontend checks:

```bash
cd frontend
npm run lint
npm run build
```

Preview the production frontend build:

```bash
cd frontend
npm run build
npm run preview
```

Check backend health:

```bash
curl http://localhost:8080/actuator/health
```

## Environment Configuration

Never commit `.env` files. Keep real secrets in your local environment, deployment platform, or secret manager.

### Backend

The backend reads variables from the shell environment. For local development, load `backend/.env` before starting Spring Boot.

Required for normal operation:

- `MONGODB_URI`: MongoDB connection string. Use `mongodb://localhost:27017/coursecraft` locally or a MongoDB Atlas URI in production.
- `CORS_ALLOWED_ORIGINS`: Comma-separated frontend origins allowed to call the API, for example `http://localhost:5173` locally or your production frontend URL.
- `AI_PROVIDER`: One of `template`, `gemini`, or `openrouter`. Use `template` for local development without external AI calls.
- `JWT_AUTH_ENABLED`: Use `false` for local demo auth. Use `true` when securing `/api/**` with Auth0 JWTs.

AI provider variables:

- `GEMINI_API_KEY`: Required when `AI_PROVIDER=gemini`.
- `GEMINI_MODEL`: Defaults to `gemini-1.5-flash`.
- `GEMINI_TTS_MODEL`: Defaults to `gemini-2.5-flash-preview-tts`; used for Hinglish lesson audio.
- `GEMINI_TTS_VOICE`: Defaults to `Kore`.
- `OPENROUTER_API_KEY`: Required when `AI_PROVIDER=openrouter`.
- `OPENROUTER_MODEL`: Defaults to `openai/gpt-4o-mini`.
- `OPENROUTER_BASE_URL`: Defaults to `https://openrouter.ai/api/v1`.
- `OPENROUTER_HTTP_REFERER`: URL sent to OpenRouter for attribution.
- `OPENROUTER_APP_TITLE`: App title sent to OpenRouter for attribution.

Authentication variables:

- `LOCAL_DEV_USER_ID`: User ID used when JWT auth is disabled.
- `AUTH0_ISSUER_URI`: Required when `JWT_AUTH_ENABLED=true`, for example `https://your-tenant.region.auth0.com/`.
- `AUTH0_AUDIENCE`: Required when `JWT_AUTH_ENABLED=true`; must match the Auth0 API audience.

Optional integrations:

- `YOUTUBE_API_KEY`: Enables `/api/youtube` by calling the YouTube Data API v3. Without it, video search returns a service unavailable response.

### Frontend

Required:

- `VITE_API_BASE_URL`: Backend API base URL, for example `http://localhost:8080`.

Optional Auth0 variables:

- `VITE_AUTH0_DOMAIN`: Auth0 tenant domain.
- `VITE_AUTH0_CLIENT_ID`: Auth0 single-page application client ID.
- `VITE_AUTH0_AUDIENCE`: Auth0 API audience. This should match backend `AUTH0_AUDIENCE`.

If `VITE_AUTH0_DOMAIN` and `VITE_AUTH0_CLIENT_ID` are empty, the frontend uses local demo authentication backed by browser local storage.

## Auth Modes

Local development mode:

- Leave frontend Auth0 variables empty.
- Set `JWT_AUTH_ENABLED=false`.
- The frontend uses a demo user and the backend resolves all requests to `LOCAL_DEV_USER_ID`.

Production Auth0 mode:

- Create an Auth0 single-page application for the React app.
- Create an Auth0 API and use its identifier as the audience.
- Add `http://localhost:5173/auth/callback` for local callback testing.
- Add your production frontend callback URL, for example `https://your-app.vercel.app/auth/callback`.
- Set `VITE_AUTH0_DOMAIN`, `VITE_AUTH0_CLIENT_ID`, and `VITE_AUTH0_AUDIENCE` in the frontend environment.
- Set `JWT_AUTH_ENABLED=true`, `AUTH0_ISSUER_URI`, and `AUTH0_AUDIENCE` in the backend environment.

## API Overview

All API routes are served by the backend on `VITE_API_BASE_URL`.

- `GET /api/auth/me`: Return the current resolved user and auth provider.
- `GET /api/courses`: List courses for the current user.
- `GET /api/courses/{courseId}`: Fetch a single course.
- `POST /api/courses/generate`: Generate a course outline. Body: `{ "topic": "Machine learning basics" }`.
- `GET /api/courses/{courseId}/modules/{moduleId}/lessons/{lessonId}`: Fetch generated lesson content.
- `POST /api/courses/{courseId}/modules/{moduleId}/lessons/{lessonId}/generate`: Generate lesson content.
- `POST /api/courses/{courseId}/modules/{moduleId}/lessons/{lessonId}/audio`: Generate Hinglish lesson audio as a WAV response.
- `GET /api/courses/{courseId}/progress`: Fetch course progress for the current user.
- `PUT /api/courses/{courseId}/progress/lessons/{lessonId}`: Update lesson progress. Body: `{ "completed": true, "assessmentScore": 90 }`.
- `GET /api/youtube?query=...`: Return the first matching YouTube video for a query.
- `GET /actuator/health`: Health check endpoint.

## Production Deployment

### Backend

The backend includes a multi-stage Dockerfile.

Build the image:

```bash
cd backend
docker build -t coursecraft-api .
```

Run it locally:

```bash
docker run --rm -p 8080:8080 \
  --env-file .env \
  coursecraft-api
```

For production, deploy the image to a platform such as Render, Railway, Fly.io, AWS ECS, Google Cloud Run, or Azure Container Apps. Configure these environment variables in the platform:

- `MONGODB_URI` pointing to MongoDB Atlas or another managed MongoDB instance.
- `CORS_ALLOWED_ORIGINS` set to the production frontend URL.
- `AI_PROVIDER` and the matching API key variables.
- `JWT_AUTH_ENABLED=true` plus Auth0 issuer and audience values.
- `YOUTUBE_API_KEY` if YouTube recommendations should be enabled.

### Frontend

The frontend is ready for Vercel-style SPA hosting through `frontend/vercel.json`.

Build command:

```bash
npm run build
```

Output directory:

```text
dist
```

Set frontend production variables in the hosting platform:

- `VITE_API_BASE_URL` set to the deployed backend URL.
- Auth0 variables if production auth is enabled.

Make sure the production frontend origin is also present in backend `CORS_ALLOWED_ORIGINS`.

## CI

The GitHub Actions workflow runs on pushes and pull requests targeting `main`.

- Backend job: starts MongoDB 7 and runs `./mvnw -B test`.
- Frontend job: installs dependencies with `npm ci`, then runs `npm run lint` and `npm run build`.

Run the same checks locally before opening a pull request:

```bash
cd backend && ./mvnw test
cd ../frontend && npm ci && npm run lint && npm run build
```

## Troubleshooting

Backend cannot connect to MongoDB:

- Confirm MongoDB is running.
- Verify `MONGODB_URI`.
- For MongoDB Atlas, URL-encode special characters in usernames or passwords and allow your current IP address in Atlas network access.

Frontend cannot reach the backend:

- Confirm the backend is running on port `8080`.
- Confirm `VITE_API_BASE_URL=http://localhost:8080`.
- Confirm `CORS_ALLOWED_ORIGINS` includes the frontend origin.

AI generation fails:

- If using local development only, set `AI_PROVIDER=template`.
- If using Gemini or OpenRouter, confirm the matching API key is present and valid.

YouTube search fails:

- Set `YOUTUBE_API_KEY` to a valid YouTube Data API v3 key.
- Confirm the key has access to the YouTube Data API.

Auth0 login or API calls fail:

- Confirm the frontend callback URL is registered in Auth0.
- Confirm frontend `VITE_AUTH0_AUDIENCE` matches backend `AUTH0_AUDIENCE`.
- Confirm backend `AUTH0_ISSUER_URI` includes the trailing slash used by Auth0.

## Contributing

Contributions are welcome. Please keep changes focused and easy to review.

1. Fork or branch from `main`.
2. Create a descriptive branch name, for example `feat/course-export` or `fix/auth-callback`.
3. Install dependencies and copy both `.env.example` files.
4. Make your changes with tests or verification steps when applicable.
5. Run backend tests and frontend checks locally.
6. Open a pull request with a clear summary, screenshots for UI changes, and any new environment variables or migration notes.

Recommended commit style:

- `feat: add course export`
- `fix: handle empty lesson content`
- `docs: improve setup guide`
- `test: cover progress updates`
- `chore: update tooling`

## Security Notes

- Do not commit `.env`, API keys, Auth0 secrets, MongoDB credentials, or generated build outputs.
- Use Auth0/JWT mode in production. Local demo auth is for development only.
- Restrict MongoDB Atlas access to trusted networks where possible.
- Keep `CORS_ALLOWED_ORIGINS` narrow in production.
- Rotate API keys immediately if they are exposed.

## License

This project is licensed under the MIT License. See `LICENSE` for details.
