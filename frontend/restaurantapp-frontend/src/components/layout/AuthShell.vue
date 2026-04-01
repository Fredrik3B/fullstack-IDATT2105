<template>
  <div class="auth-shell">
    <div class="auth-shell__ambient" aria-hidden="true">
      <span class="blob blob--one"></span>
      <span class="blob blob--two"></span>
      <span class="blob blob--three"></span>
      <span class="grain"></span>
    </div>

    <div class="auth-shell__inner">
      <aside class="auth-shell__aside">
        <span class="aside-tag">{{ tag }}</span>
        <h1 class="aside-title">{{ asideTitle }}</h1>
        <p class="aside-body">{{ asideBody }}</p>

        <ul class="aside-list" v-if="features.length">
          <li v-for="feature in features" :key="feature">{{ feature }}</li>
        </ul>
      </aside>

      <section class="auth-shell__main">
        <div class="auth-card">
          <div class="card-head">
            <div class="brand-row">
              <div class="brand-mark">IC</div>
              <div>
                <p class="brand-title">ICSystem</p>
                <p class="brand-subtitle">Internal control that teams actually use</p>
              </div>
            </div>

            <div>
              <p class="eyebrow">{{ eyebrow }}</p>
              <h2 class="title">{{ title }}</h2>
              <p class="subtitle">{{ subtitle }}</p>
            </div>
          </div>

          <div class="card-body">
            <slot />
          </div>

          <div v-if="$slots.footer" class="card-footer">
            <slot name="footer" />
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
defineProps({
  tag: { type: String, default: 'Welcome' },
  eyebrow: { type: String, default: 'Account access' },
  title: { type: String, default: 'Welcome back' },
  subtitle: { type: String, default: '' },
  asideTitle: { type: String, default: 'Run your checks with confidence' },
  asideBody: { type: String, default: 'A calmer workflow for hygiene, alcohol control, and daily compliance.' },
  features: { type: Array, default: () => [] }
})
</script>

<style scoped>
.auth-shell {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8);
  background: linear-gradient(135deg, #141428 0%, #212043 45%, #2a2852 100%);
  overflow: hidden;
}

.auth-shell__ambient {
  position: absolute;
  inset: 0;
}

.blob {
  position: absolute;
  border-radius: 9999px;
  filter: blur(2px);
  opacity: 0.8;
}

.blob--one {
  width: 380px;
  height: 380px;
  background: radial-gradient(circle at 35% 35%, rgba(212, 232, 53, 0.45), rgba(212, 232, 53, 0));
  top: -80px;
  left: -120px;
  animation: driftA 14s ease-in-out infinite;
}

.blob--two {
  width: 420px;
  height: 420px;
  background: radial-gradient(circle at 60% 40%, rgba(255, 255, 255, 0.12), rgba(255, 255, 255, 0));
  bottom: -140px;
  right: -120px;
  animation: driftB 16s ease-in-out infinite;
}

.blob--three {
  width: 240px;
  height: 240px;
  background: radial-gradient(circle at 50% 50%, rgba(212, 232, 53, 0.2), rgba(212, 232, 53, 0));
  top: 42%;
  right: 38%;
  animation: driftC 18s ease-in-out infinite;
}

.grain {
  position: absolute;
  inset: 0;
  opacity: 0.12;
  background-image: radial-gradient(rgba(255, 255, 255, 0.45) 0.55px, transparent 0.55px);
  background-size: 3px 3px;
}

.auth-shell__inner {
  position: relative;
  width: min(1160px, 100%);
  display: grid;
  grid-template-columns: 1.03fr 1fr;
  gap: var(--space-8);
  align-items: stretch;
  z-index: 1;
}

.auth-shell__aside {
  color: rgba(255, 255, 255, 0.94);
  padding: var(--space-10) var(--space-8);
  border-radius: var(--radius-xl);
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.11), rgba(255, 255, 255, 0.03));
  backdrop-filter: blur(5px);
  box-shadow: 0 24px 40px rgba(0, 0, 0, 0.22);
}

.aside-tag {
  display: inline-flex;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  padding: 5px 11px;
  border-radius: var(--radius-full);
  border: 1px solid rgba(212, 232, 53, 0.55);
  color: var(--color-accent);
  margin-bottom: var(--space-4);
}

.aside-title {
  margin: 0;
  font-size: clamp(1.9rem, 4vw, 2.6rem);
  line-height: 1.08;
  letter-spacing: -0.03em;
  font-family: var(--font-display);
}

.aside-body {
  margin: var(--space-4) 0 0;
  font-size: var(--font-size-md);
  color: rgba(255, 255, 255, 0.76);
  line-height: 1.7;
  max-width: 42ch;
}

.aside-list {
  list-style: none;
  margin: var(--space-8) 0 0;
  padding: 0;
  display: grid;
  gap: var(--space-3);
}

.aside-list li {
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  background: rgba(17, 16, 36, 0.35);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.82);
  font-size: var(--font-size-sm);
}

.auth-shell__main {
  display: flex;
}

.auth-card {
  width: 100%;
  border-radius: calc(var(--radius-xl) + 2px);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.97), rgba(255, 255, 255, 0.94));
  border: 1px solid rgba(255, 255, 255, 0.72);
  box-shadow: 0 20px 36px rgba(13, 12, 24, 0.35);
  padding: var(--space-8);
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  animation: riseIn 300ms ease-out;
}

.card-head {
  display: grid;
  gap: var(--space-4);
}

.brand-row {
  display: flex;
  gap: var(--space-3);
  align-items: center;
}

.brand-mark {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  background: #191833;
  color: var(--color-accent);
  font-size: 0.9rem;
  font-weight: var(--font-weight-bold);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.brand-title {
  margin: 0;
  font-weight: 700;
  color: #1b1a34;
}

.brand-subtitle {
  margin: 1px 0 0;
  color: #6f6d90;
  font-size: 12px;
}

.eyebrow {
  margin: 0;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: #7875a3;
}

.title {
  margin: var(--space-1) 0 0;
  font-size: clamp(1.4rem, 2.4vw, 1.9rem);
  line-height: 1.2;
  color: #181733;
  letter-spacing: -0.02em;
}

.subtitle {
  margin: var(--space-2) 0 0;
  color: #69678a;
  font-size: var(--font-size-md);
}

.card-body {
  display: grid;
  gap: var(--space-4);
}

.card-footer {
  border-top: 1px solid rgba(27, 26, 52, 0.08);
  padding-top: var(--space-4);
}

@keyframes driftA {
  0%, 100% { transform: translate3d(0, 0, 0); }
  50% { transform: translate3d(20px, 16px, 0); }
}

@keyframes driftB {
  0%, 100% { transform: translate3d(0, 0, 0); }
  50% { transform: translate3d(-16px, -14px, 0); }
}

@keyframes driftC {
  0%, 100% { transform: translate3d(0, 0, 0); }
  50% { transform: translate3d(14px, -20px, 0); }
}

@keyframes riseIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1000px) {
  .auth-shell {
    padding: var(--space-6);
  }

  .auth-shell__inner {
    grid-template-columns: 1fr;
  }

  .auth-shell__aside {
    order: 2;
    padding: var(--space-6);
  }

  .auth-card {
    order: 1;
    padding: var(--space-6);
  }
}

@media (max-width: 620px) {
  .auth-shell {
    padding: var(--space-4);
  }

  .aside-title {
    font-size: 1.7rem;
  }

  .auth-card {
    border-radius: var(--radius-lg);
  }
}
</style>
