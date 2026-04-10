<template>
  <section class="page-header">
    <div class="page-header__main">
      <div class="eyebrow">{{ eyebrow }}</div>
      <h1>{{ title }}</h1>
      <p v-if="description" class="page-description">{{ description }}</p>
    </div>
    <div class="actions">
      <slot name="actions" />
    </div>
  </section>
</template>

<script setup>
/**
 * PageHeader
 *
 * Dark hero-style section header used at the top of full-page views (e.g. Reports,
 * Documents). Renders an eyebrow badge, an H1 title, and an optional description
 * in the left column. The `actions` named slot on the right column is used for
 * filter controls, stat cards, or other page-level actions.
 *
 * @prop {string} eyebrow      - Small uppercase badge above the title.
 * @prop {string} title        - Main page heading.
 * @prop {string} [description]- Supporting paragraph below the title.
 */
defineProps({
  eyebrow: { type: String, required: true },
  title: { type: String, required: true },
  description: { type: String, default: '' },
})
</script>

<style scoped>
.page-header {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(300px, 0.9fr);
  gap: var(--space-6);
  padding: var(--space-8);
  border-radius: var(--radius-lg);
  background: linear-gradient(180deg, var(--color-dark-primary) 0%, #232248 100%);
  box-shadow: var(--shadow-md);
}

.page-header__main {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--space-2);
}

.eyebrow {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 var(--space-3);
  border-radius: var(--radius-full);
  border: 1px solid rgba(212, 232, 53, 0.32);
  background: rgba(212, 232, 53, 0.08);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--color-accent);
}

h1 {
  margin: 0;
  font-size: clamp(32px, 4vw, 42px);
  line-height: var(--line-height-tight);
  color: #ffffff;
}

.page-description {
  margin: 0;
  max-width: 60ch;
  font-size: var(--font-size-md);
  line-height: var(--line-height-normal);
  color: rgba(255, 255, 255, 0.82);
}

.actions {
  display: grid;
  gap: var(--space-4);
  align-content: start;
}

@media (max-width: 900px) {
  .page-header {
    grid-template-columns: 1fr;
    padding: var(--space-6);
  }

  .actions {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .page-header {
    padding: var(--space-5) var(--space-4);
  }
}
</style>
