/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          dark: 'var(--bg-main)',
          card: 'var(--bg-card)',
          primary: 'var(--accent-purple)',
          secondary: 'var(--accent-indigo)',
        }
      },
      fontFamily: {
        sans: ['var(--font-sans)', 'system-ui', '-apple-system', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
