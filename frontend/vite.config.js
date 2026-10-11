import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// During development, requests to /api go to the Spring Boot server on port 8080,
// so the browser sees one origin and the session cookie just works.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: { '/api': 'http://localhost:8080' },
  },
})
