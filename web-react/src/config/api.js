// URLs de los backends. Se leen de .env.local (copia de .env.example); si no existe, se usan las de desarrollo.
export const SPRING_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';
export const DJANGO_URL = import.meta.env.VITE_DJANGO_URL ?? 'http://localhost:8000';
