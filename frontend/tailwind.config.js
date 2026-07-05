/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        surface: {
          950: '#05070d',
          900: '#0b0f19',
          800: '#111827',
          700: '#1a2233',
          600: '#242e42',
          500: '#33405a',
        },
        status: {
          healthy: '#22c55e',
          degraded: '#f59e0b',
          offline: '#ef4444',
          info: '#3b82f6',
        },
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'ui-monospace', 'SFMono-Regular', 'monospace'],
      },
      boxShadow: {
        panel: '0 1px 2px 0 rgb(0 0 0 / 0.4), 0 0 0 1px rgb(255 255 255 / 0.03)',
      },
    },
  },
  plugins: [],
}
