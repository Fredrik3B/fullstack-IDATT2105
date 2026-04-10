<template>
  <div class="app-container">
    <AppHeader />
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
/**
 * AppLayout
 *
 * Root shell for all authenticated application pages. Renders the sticky
 * AppHeader at the top and a `<main>` content area that hosts the current
 * route via `<router-view>`. Background gradients are applied here via global
 * CSS so they span the full viewport.
 */
import AppHeader from './AppHeader.vue'
</script>

<style>
.app-container {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(212, 232, 53, 0.24), transparent 30%),
    linear-gradient(180deg, #f8f8fc 0%, #ebedf6 100%);
}

.app-main {
  min-height: calc(100vh - var(--nav-height));
  color: var(--color-text-primary);
}

.app-main a {
  color: var(--color-text-secondary);
}
</style>
