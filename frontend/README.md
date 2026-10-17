# CourseCraft AI Frontend

React/Vite single-page app for CourseCraft AI.

## Setup

```bash
cp .env.example .env
npm install
npm run dev
```

The app expects `VITE_API_BASE_URL` to point at the Spring Boot API, usually `http://localhost:8080` in local development.

## Scripts

- `npm run dev`: start the Vite development server.
- `npm run lint`: run ESLint.
- `npm run test`: run frontend tests.
- `npm run build`: create a production build in `dist`.
- `npm run preview`: preview the production build locally.

See the root `README.md` for full project setup, deployment, and contribution guidance.
