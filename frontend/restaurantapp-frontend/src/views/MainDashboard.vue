<template>
  <div class="dashboard-root">
    <!-- Welcome banner -->
    <section class="welcome-banner">
      <div class="welcome-glow"></div>
      <div class="welcome-inner">
        <span class="welcome-tag">Internnalcontrolsystem</span>
        <h1 class="welcome-heading">
          God dag, <span class="welcome-accent">"Change to name of user"</span>
        </h1>
        <p class="welcome-sub">CHange to neme of resturant and the current date</p>
      </div>
    </section>

    <main class="dashboard-main">
      <div class="dashboard-content">

        <!-- Checklist modules -->
        <section>
          <h2 class="section-heading">Todays checklists</h2>
          <div class="checklist-grid">
            <div class="checklist-placeholder">
              <div class="checklist-header">
                <span class="checklist-dot checklist-dot--food"></span>
                <h3 class="checklist-title">IC-Food</h3>
              </div>
              <p class="checklist-hint">Sjekkliste-komponenten kommer her</p>
            </div>
            <div class="checklist-placeholder">
              <div class="checklist-header">
                <span class="checklist-dot checklist-dot--alcohol"></span>
                <h3 class="checklist-title">IC-Alcohol</h3>
              </div>
              <p class="checklist-hint">Sjekkliste-komponenten kommer her</p>
            </div>
          </div>
        </section>

        <!-- Shared overview -->
        <section>
          <h2 class="section-heading">Overview</h2>
          <div class="stat-grid">
            <div class="stat-card">
              <span class="stat-label">Active deviations</span>
              <span class="stat-value stat-value--danger">0</span>
              <span class="stat-hint">No deviations</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">Tasks today</span>
              <span class="stat-value">0</span>
              <span class="stat-hint">No tasks</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">Last control</span>
              <span class="stat-value stat-value--muted">—</span>
              <span class="stat-hint">Not completed</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">Reports</span>
              <span class="stat-value stat-value--muted">0</span>
              <span class="stat-hint">No reports</span>
            </div>
          </div>
        </section>

      </div>
    </main>
  </div>
</template>

<script setup>
</script>

<style scoped>
.dashboard-root {
  min-height: 100vh;
  background: var(--color-bg-secondary);
}

/* ── Welcome banner ── */
.welcome-banner {
  position: relative;
  overflow: hidden;
  background: var(--color-dark-primary);
  padding: var(--space-16) var(--space-6);
}

.welcome-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 900px;
  height: 900px;
  border-radius: 50%;
  background: radial-gradient(
    circle at center,
    var(--color-dark-secondary) 0%,
    var(--color-dark-primary) 55%,
    transparent 72%
  );
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
  gap: var(--space-5);
}

.welcome-tag {
  display: inline-block;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-accent);
  border: 1px solid var(--color-accent);
  border-radius: var(--radius-full);
  padding: 4px var(--space-3);
}

.welcome-heading {
  margin: 0;
  font-size: clamp(32px, 5vw, 56px);
  font-weight: var(--font-weight-bold);
  color: #ffffff;
  line-height: var(--line-height-tight);
}

.welcome-accent {
  color: var(--color-accent);
}

.welcome-sub {
  margin: 0;
  font-size: var(--font-size-md);
  color: var(--color-dark-border);
}

/* ── Main layout ── */
.dashboard-main {
  padding: var(--space-12) var(--space-6);
}

.dashboard-content {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-10);
}

.section-heading {
  margin: 0 0 var(--space-4) 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

/* ── Checklist placeholders ── */
.checklist-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
  align-items: stretch;
}

.checklist-placeholder {
  background: var(--color-bg-primary);
  border: 2px dashed var(--color-border-strong);
  border-radius: var(--radius-lg);
  padding: var(--space-8) var(--space-6);
  min-height: 300px;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  box-shadow: var(--shadow-sm);
}

.checklist-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--color-border);
}

.checklist-dot {
  width: 10px;
  height: 10px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.checklist-dot--food    { background: var(--color-accent); }
.checklist-dot--alcohol { background: var(--color-dark-tertiary); }

.checklist-title {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.checklist-hint {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-hint);
}

/* ── Stat cards ── */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}

.stat-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  box-shadow: var(--shadow-sm);
}

.stat-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-muted);
}

.stat-value {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.stat-value--danger { color: var(--color-danger); }
.stat-value--muted  { color: var(--color-text-hint); }

.stat-hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-hint);
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
