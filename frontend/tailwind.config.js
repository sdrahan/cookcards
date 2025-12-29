/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["../src/main/resources/templates/**/*.{html,js,jsx}"],
  theme: {
    extend: {
      fontFamily: {
        lato: ['Lato', 'sans-serif'],
        montserrat: ['Montserrat', 'sans-serif'],
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}

