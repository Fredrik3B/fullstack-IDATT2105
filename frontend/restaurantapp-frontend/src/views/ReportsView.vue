<template>
  <div class="page-root">
    <AppHeader />

    <!-- Page banner -->
    <section class="page-banner">
      <div class="page-banner-inner">
        <span class="page-tag">Rapporter</span>
        <h1 class="page-heading">Revisjon og <span class="page-accent">inspeksjonsrapporter</span></h1>
        <p class="page-sub">Automatisk genererte rapporter for internrevisjon og eksterne tilsyn</p>
      </div>
    </section>

    <main class="page-main">
      <div class="page-content">

        <!-- Summary row -->
        <div class="summary-grid">
          <div class="summary-card">
            <span class="summary-label">Totalt denne måneden</span>
            <span class="summary-value">0</span>
            <span class="summary-hint">Ingen rapporter generert</span>
          </div>
          <div class="summary-card">
            <span class="summary-label">Internrevisjoner</span>
            <span class="summary-value summary-value--accent">0</span>
            <span class="summary-hint">IC-Food og IC-Alcohol</span>
          </div>
          <div class="summary-card">
            <span class="summary-label">Eksterne tilsyn</span>
            <span class="summary-value">0</span>
            <span class="summary-hint">Mattilsynet / Skjenkekontrollen</span>
          </div>
          <div class="summary-card">
            <span class="summary-label">Ventende eksport</span>
            <span class="summary-value summary-value--warning">0</span>
            <span class="summary-hint">Ikke eksportert ennå</span>
          </div>
        </div>

        <!-- Filter bar -->
        <div class="filter-bar">
          <div class="filter-group">
            <label class="filter-label" for="filter-type">Type</label>
            <select class="filter-select" id="filter-type">
              <option value="">Alle typer</option>
              <option value="internal">Internrevisjon</option>
              <option value="external">Eksternt tilsyn</option>
            </select>
          </div>
          <div class="filter-group">
            <label class="filter-label" for="filter-module">Modul</label>
            <select class="filter-select" id="filter-module">
              <option value="">Alle moduler</option>
              <option value="food">IC-Food</option>
              <option value="alcohol">IC-Alcohol</option>
            </select>
          </div>
          <div class="filter-group">
            <label class="filter-label" for="filter-from">Fra dato</label>
            <input class="filter-input" id="filter-from" type="date" />
          </div>
          <div class="filter-group">
            <label class="filter-label" for="filter-to">Til dato</label>
            <input class="filter-input" id="filter-to" type="date" />
          </div>
          <button class="btn-generate" type="button">+ Generer rapport</button>
        </div>

        <!-- Report list -->
        <section>
          <h2 class="section-heading">Rapporter</h2>

          <!-- Empty state -->
          <div class="empty-state">
            <div class="empty-icon">&#128196;</div>
            <p class="empty-title">Ingen rapporter ennå</p>
            <p class="empty-sub">Rapporter genereres automatisk etter gjennomførte sjekklister og revisjoner.</p>
          </div>

          <!-- Report table (hidden when empty, shown when data exists) -->
          <div class="report-table" aria-hidden="true" style="display: none;">
            <div class="report-table-head">
              <span>Navn</span>
              <span>Type</span>
              <span>Modul</span>
              <span>Dato</span>
              <span>Status</span>
              <span>Eksport</span>
            </div>
            <!-- Example row structure (will be rendered by v-for later) -->
            <div class="report-row">
              <span class="report-name">Ukesrapport uke 12</span>
              <span class="report-type">Internrevisjon</span>
              <span class="report-module">
                <span class="module-badge module-badge--food">IC-Food</span>
              </span>
              <span class="report-date">20. mars 2026</span>
              <span class="report-status">
                <span class="status-pill status-pill--ok">Fullført</span>
              </span>
              <span class="report-actions">
                <button class="export-btn" type="button">PDF</button>
                <button class="export-btn" type="button">JSON</button>
              </span>
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
.page-root {
  min-height: 100vh;
  background: var(--color-bg-secondary);
}

/* ── Page banner ── */
.page-banner {
  background: var(--color-dark-primary);
  border-bottom: 1px solid var(--color-dark-secondary);
  padding: var(--space-10) var(--space-6);
}

.page-banner-inner {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.page-tag {
  display: inline-block;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--color-accent);
  border: 1px solid var(--color-accent);
  border-radius: var(--radius-full);
  padding: 3px var(--space-3);
  align-self: flex-start;
}

.page-heading {
  margin: 0;
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: #ffffff;
  line-height: var(--line-height-tight);
}

.page-accent {
  color: var(--color-accent);
}

.page-sub {
  margin: 0;
  font-size: var(--font-size-md);
  color: var(--color-dark-border);
}

/* ── Main ── */
.page-main {
  padding: var(--space-10) var(--space-6);
}

.page-content {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

/* ── Summary row ── */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}

.summary-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  box-shadow: var(--shadow-sm);
}

.summary-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-muted);
}

.summary-value {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.summary-value--accent  { color: var(--color-accent-text); }
.summary-value--warning { color: var(--color-warning); }

.summary-hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-hint);
}

/* ── Filter bar ── */
.filter-bar {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-5) var(--space-6);
  display: flex;
  align-items: flex-end;
  gap: var(--space-4);
  flex-wrap: wrap;
  box-shadow: var(--shadow-sm);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.filter-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-muted);
}

.filter-select,
.filter-input {
  height: 36px;
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  background: var(--color-bg-primary);
  font-family: inherit;
  outline: none;
  transition: border-color var(--transition-fast);
}

.filter-select:focus,
.filter-input:focus {
  border-color: var(--color-dark-secondary);
}

.btn-generate {
  margin-left: auto;
  height: 36px;
  padding: 0 var(--space-5);
  background: var(--color-accent);
  color: var(--color-dark-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-family: inherit;
  transition: opacity var(--transition-fast);
  white-space: nowrap;
}

.btn-generate:hover {
  opacity: 0.85;
}

/* ── Section heading ── */
.section-heading {
  margin: 0 0 var(--space-4) 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

/* ── Empty state ── */
.empty-state {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-16) var(--space-6);
  text-align: center;
  box-shadow: var(--shadow-sm);
}

.empty-icon {
  font-size: 40px;
  margin-bottom: var(--space-4);
}

.empty-title {
  margin: 0 0 var(--space-2) 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.empty-sub {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  max-width: 400px;
  margin-inline: auto;
}

/* ── Report table ── */
.report-table {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.report-table-head {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr;
  padding: var(--space-3) var(--space-6);
  background: var(--color-bg-tertiary);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  border-bottom: 1px solid var(--color-border);
}

.report-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr;
  padding: var(--space-4) var(--space-6);
  border-bottom: 1px solid var(--color-border-subtle);
  align-items: center;
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
}

.report-row:last-child {
  border-bottom: none;
}

.report-name {
  font-weight: var(--font-weight-medium);
}

.report-date {
  color: var(--color-text-muted);
}

.module-badge {
  display: inline-block;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  padding: 2px var(--space-2);
  border-radius: var(--radius-full);
}

.module-badge--food {
  background: var(--color-accent-light);
  color: var(--color-accent-text);
}

.module-badge--alcohol {
  background: var(--color-bg-subtle);
  color: var(--color-dark-tertiary);
}

.status-pill {
  display: inline-block;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  padding: 2px var(--space-2);
  border-radius: var(--radius-full);
}

.status-pill--ok {
  background: var(--color-success-bg);
  color: var(--color-success-text);
}

.status-pill--pending {
  background: var(--color-warning-bg);
  color: var(--color-warning-text);
}

.report-actions {
  display: flex;
  gap: var(--space-2);
}

.export-btn {
  padding: 3px var(--space-3);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-bg-primary);
  color: var(--color-text-secondary);
  cursor: pointer;
  font-family: inherit;
  transition: border-color var(--transition-fast), color var(--transition-fast);
}

.export-btn:hover {
  border-color: var(--color-dark-primary);
  color: var(--color-dark-primary);
}

/* ── Responsive ── */
@media (max-width: 900px) {
  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .btn-generate {
    margin-left: 0;
  }
}
</style>
