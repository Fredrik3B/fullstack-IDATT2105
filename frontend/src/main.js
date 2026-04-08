import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'

import './assets/styles/variables.css'
import './assets/styles/auth.css'

/**
 * Application bootstrap entry.
 *
 * Registers Pinia + router, restores auth state, and mounts the app only
 * after auth init has completed so first-route guards read consistent state.
 */
const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

const authStore = useAuthStore()
authStore.initAuth().finally(() => {
  app.mount('#app')
})
