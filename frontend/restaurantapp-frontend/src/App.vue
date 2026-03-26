<script setup>
import AppHeader from './components/layout/AppHeader.vue'
</script>

<template>
  <div class="app-container">
    <AppHeader />
    <main class="app-main">
      <h1>You did it!</h1>
      <p>
        Visit <a href="https://vuejs.org/" target="_blank" rel="noopener">vuejs.org</a> to read the
        documentation
      </p>
    </main>
  </div>
</template>

<style>
.app-container {
  min-height: 100vh;
  background: var(--color-bg-secondary);
}

.app-main {
  min-height: calc(100vh - var(--nav-height));
  padding: var(--space-6);
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
</style>
