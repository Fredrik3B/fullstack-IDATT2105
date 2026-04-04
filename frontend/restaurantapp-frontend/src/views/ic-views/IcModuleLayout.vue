<template>
  <section class="module-layout">
    <router-view :key="route.fullPath" />
  </section>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<style scoped>
.module-layout {
  min-height: 100%;
  padding: 0 0 var(--space-10);
}
</style>
