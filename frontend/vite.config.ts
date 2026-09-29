import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // The browser only talks to Vite (localhost:5173); Vite forwards /api/* to Spring Boot.
    // Same origin from the browser's point of view -> no CORS setup needed in development.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
