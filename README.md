# CourseCraft-AI
AI-powered course generator that transforms any topic prompt into structured modules, rich lessons, quizzes, videos, PDFs, and multilingual explanations.

## Backend Environment

The backend reads runtime configuration from environment variables. Start by filling in `backend/.env` using `backend/.env.example` as the template.

For MongoDB Atlas, set `MONGODB_URI` to your Atlas connection string:

```env
MONGODB_URI=mongodb+srv://<username>:<url-encoded-password>@<cluster-host>/coursecraft?retryWrites=true&w=majority&appName=<app-name>
```

If your MongoDB password contains special characters, URL-encode it before placing it in the URI.

Run the backend with the env file loaded:

```bash
cd backend
set -a
source .env
set +a
./mvnw spring-boot:run
```
