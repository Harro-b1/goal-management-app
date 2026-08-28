import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  build: {
    outDir: 'build',
  },
  server: {
    proxy: {
      '/categories': 'http://localhost:8081',
      '/goals': 'http://localhost:8081',
      '/schedules': 'http://localhost:8081',
      '/schedule-templates': 'http://localhost:8081',
      '/events': 'http://localhost:8081',
      '/event-templates': 'http://localhost:8081',
    },
  },
});
