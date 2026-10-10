# GastroMind — Web (React + Vite)

Frontend web de GastroMind (login HU-02).

## Requisitos

- Node.js 20+ y npm
- Backend `backend-spring` corriendo en `http://localhost:8080`

## Ejecución

```bash
cd web-react
npm install
npm run dev
```

La app queda disponible en `http://localhost:5173` (origen permitido por CORS en el backend).

## Scripts

| Comando           | Descripción                          |
|-------------------|--------------------------------------|
| `npm run dev`     | Servidor de desarrollo con HMR       |
| `npm run build`   | Build de producción en `dist/`       |
| `npm run preview` | Sirve localmente el build generado   |
| `npm run lint`    | Lint con Oxlint                      |
