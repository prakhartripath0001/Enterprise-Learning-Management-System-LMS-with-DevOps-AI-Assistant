import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    watch: {
      usePolling: true,
    },
    hmr: {
      clientPort: 5173,
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/setupTests.js',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/**',
        'eslint.config.js',
        'vite.config.js',
        'postcss.config.js',
        'tailwind.config.js',
        'src/main.jsx',
        'src/App.jsx',
        'src/api-verification-checklist.js',
        'src/pages/About.jsx',
        'src/pages/Contact.jsx',
        'src/pages/Courses.jsx',
        'src/layouts/AppLayout.jsx',
        'src/layouts/Sidebar.jsx',
        'src/layouts/Footer.jsx',
        'src/components/ui/Modal.jsx',
        'src/components/ui/Select.jsx',
        'src/components/ui/Spinner.jsx',
        'src/components/ui/Table.jsx',
        'src/components/ui/TextArea.jsx',
        'src/components/Navbar.jsx',
        'src/components/Footer.jsx',
        'src/tests/**',
      ],
      thresholds: {
        lines: 80,
        statements: 80,
        functions: 80,
        branches: 80,
      },
    },
  },
})
