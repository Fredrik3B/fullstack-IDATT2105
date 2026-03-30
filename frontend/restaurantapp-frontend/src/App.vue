<template>
  <router-view />
  <ToastContainer />
</template>

<script setup>
import ToastContainer from '@/components/ui/ToastContainer.vue'
</script>

<style>
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
