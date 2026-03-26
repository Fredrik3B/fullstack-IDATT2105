<template>
  <div class="dashboard-root">
    <AppHeader />

    <!-- Welcome banner -->
    <section class="welcome-banner">
      <div class="welcome-glow"></div>
      <div class="welcome-inner">
        <span class="welcome-tag">Internkontrollsystem</span>
        <h1 class="welcome-heading">
          God dag, <span class="welcome-accent">Kari</span>
        </h1>
        <p class="welcome-sub">Everest Sushi &amp; Fusion AS · Torsdag 26. mars 2026</p>
      </div>
    </section>

    <main class="dashboard-main">
      <div class="dashboard-content">

        <!-- Checklist modules -->
        <section>
          <h2 class="section-heading">Dagens sjekklister</h2>
          <div class="checklist-grid">
            <div class="checklist-placeholder">
              <div class="checklist-header">
                <span class="checklist-dot checklist-dot--food"></span>
                <h3 class="checklist-title">IC-Mat</h3>
              </div>
              <p class="checklist-hint">Sjekkliste-komponenten kommer her</p>
            </div>
            <div class="checklist-placeholder">
              <div class="checklist-header">
                <span class="checklist-dot checklist-dot--alcohol"></span>
                <h3 class="checklist-title">IC-Alkohol</h3>
              </div>
              <p class="checklist-hint">Sjekkliste-komponenten kommer her</p>
            </div>
          </div>
        </section>

        <!-- Shared overview -->
        <section>
          <h2 class="section-heading">Oversikt</h2>
          <div class="stat-grid">
            <div class="stat-card">
              <span class="stat-label">Aktive avvik</span>
              <span class="stat-value stat-value--danger">0</span>
              <span class="stat-hint">Ingen avvik registrert</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">Oppgaver i dag</span>
              <span class="stat-value">0</span>
              <span class="stat-hint">Ingen oppgaver</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">Siste kontroll</span>
              <span class="stat-value stat-value--muted">—</span>
              <span class="stat-hint">Ikke gjennomført</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">Rapporter</span>
              <span class="stat-value stat-value--muted">0</span>
              <span class="stat-hint">Ingen rapporter</span>
            </div>
          </div>
        </section>

      </div>
    </main>
  </div>
</template>

<script setup>
import AppHeader from '../components/layout/AppHeader.vue'
</script>

<style scoped>
.dashboard-root {
  min-height: 100vh;
  background: #F4F4F6;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif;
}

/* ── Welcome banner ── */
.welcome-banner {
  position: relative;
  overflow: hidden;
  background: #111126;
  padding: 80px 24px;
}

.welcome-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 900px;
  height: 900px;
  border-radius: 50%;
  background: radial-gradient(circle at center, #2D2B55 0%, #1A1A35 45%, transparent 72%);
  pointer-events: none;
}

.welcome-inner {
  position: relative;
  z-index: 1;
  max-width: 680px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 20px;
}

.welcome-tag {
  display: inline-block;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #D4E835;
  border: 1px solid #D4E835;
  border-radius: 9999px;
  padding: 4px 14px;
}

.welcome-heading {
  margin: 0;
  font-size: clamp(32px, 5vw, 56px);
  font-weight: 700;
  color: #ffffff;
  line-height: 1.15;
}

.welcome-accent {
  color: #D4E835;
}

.welcome-sub {
  margin: 0;
  font-size: 14px;
  color: #C8C8D8;
}

/* ── Main layout ── */
.dashboard-main {
  padding: 48px 24px;
}

.dashboard-content {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.section-heading {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 700;
  color: #1A1A2E;
}

/* ── Checklist placeholders ── */
.checklist-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  align-items: stretch;
}

.checklist-placeholder {
  background: #ffffff;
  border: 2px dashed #C8C8D8;
  border-radius: 12px;
  padding: 32px 24px;
  min-height: 300px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-shadow: 0 1px 3px rgba(26, 26, 46, 0.06);
}

.checklist-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 12px;
  border-bottom: 1px solid #E0E0EA;
}

.checklist-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.checklist-dot--food    { background: #D4E835; }
.checklist-dot--alcohol { background: #4B4A72; }

.checklist-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1A1A2E;
}

.checklist-hint {
  margin: 0;
  font-size: 12px;
  color: #AAAACC;
}

/* ── Stat cards ── */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background: #ffffff;
  border: 1px solid #E0E0EA;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  box-shadow: 0 1px 3px rgba(26, 26, 46, 0.06);
}

.stat-label {
  font-size: 12px;
  font-weight: 500;
  color: #8888AA;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1A1A2E;
}

.stat-value--danger { color: #CC3333; }
.stat-value--muted  { color: #AAAACC; }

.stat-hint {
  font-size: 10px;
  color: #AAAACC;
}

/* ── Responsive ── */
@media (max-width: 900px) {
  .checklist-grid {
    grid-template-columns: 1fr;
  }
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
