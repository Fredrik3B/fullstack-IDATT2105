import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'

import './assets/styles/variables.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// Restore auth state from localStorage before the first route is resolved.
// This means the router guards can read isAuthenticated immediately.
const authStore = useAuthStore()
authStore.initAuth().finally(() => {
  app.mount('#app')
})
