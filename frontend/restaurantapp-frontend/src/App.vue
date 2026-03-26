<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="app-container">
    <AppHeader />
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<style>
.app-container {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(212, 232, 53, 0.18), transparent 26%),
    linear-gradient(180deg, #f8f8fb 0%, #f1f2f8 100%);
}

.app-main {
  min-height: calc(100vh - var(--nav-height));
  padding: 28px;
  color: var(--color-text-primary);
}

.app-main a {
  color: var(--color-text-secondary);
}

html,
body,
#app {
  width: 100%;
  min-height: 100%;
  margin: 0;
}

body {
  font-family: var(--font-sans);
}
</style>
