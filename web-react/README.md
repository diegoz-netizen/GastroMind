# GastroMind — Web (React + Vite)

Frontend web de GastroMind (login HU-02).

## Requisitos

- Node.js 20+ y npm
- Backend `backend-spring` corriendo en `http://localhost:8080`

## Ejecución

```bash
cd web-react
npm install
cp .env.example .env.local   # URLs de los backends (opcional: sin él se usan las de desarrollo)
npm run dev
```

| Variable          | Backend      | Por defecto             |
|-------------------|--------------|-------------------------|
| `VITE_API_URL`    | Spring Boot  | `http://localhost:8080` |
| `VITE_DJANGO_URL` | Django       | `http://localhost:8000` |

Las pantallas toman las URLs de `src/config/api.js`. Si se cambia `.env.local`, hay que reiniciar `npm run dev`.

La app queda disponible en `http://localhost:5173` (origen permitido por CORS en el backend).

## Scripts

| Comando           | Descripción                          |
|-------------------|--------------------------------------|
| `npm run dev`     | Servidor de desarrollo con HMR       |
| `npm run build`   | Build de producción en `dist/`       |
| `npm run preview` | Sirve localmente el build generado   |
| `npm run lint`    | Lint con Oxlint                      |
