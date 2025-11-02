/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Primary brand color, typical for a professional finance application
        'bank-primary': '#104975', // A deep, professional blue
        'bank-secondary': '#38a169', // A complementary green for success/deposits
        'bank-danger': '#e53e3e', // Red for errors/withdrawals
        'bank-light': '#edf2f7', // Light gray for backgrounds
        'bank-dark': '#1a202c', // Dark gray for text/headers
      },
      fontFamily: {
        // Use a modern, readable font stack
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        // Custom shadow for cards/elements to give a lifted, premium feel
        'card': '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.06)',
      }
    },
  },
  plugins: [],
}