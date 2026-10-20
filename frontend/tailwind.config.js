/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eef4ff',
          100: '#dce8ff',
          600: '#155eff',
          700: '#0f4ad1',
        },
      },
    },
  },
  plugins: [],
};
