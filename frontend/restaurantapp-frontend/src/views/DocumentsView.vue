<template>
  <div class="page-root">
    <AppHeader />

    <!-- Page banner -->
    <section class="page-banner">
      <div class="page-banner-inner">
        <span class="page-tag">Dokumenter</span>
        <h1 class="page-heading">Dokumentlagring og <span class="page-accent">sertifikater</span></h1>
        <p class="page-sub">Sentralisert lagring av retningslinjer, opplæringsmateriell og sertifikater</p>
      </div>
    </section>

    <main class="page-main">
      <div class="page-content">

        <!-- Summary row -->
        <div class="summary-grid">
          <div class="summary-card">
            <span class="summary-label">Totalt dokumenter</span>
            <span class="summary-value">0</span>
            <span class="summary-hint">Ingen dokumenter lastet opp</span>
          </div>
          <div class="summary-card">
            <span class="summary-label">Retningslinjer</span>
            <span class="summary-value summary-value--accent">0</span>
            <span class="summary-hint">Policies og prosedyrer</span>
          </div>
          <div class="summary-card">
            <span class="summary-label">Opplæringsmateriell</span>
            <span class="summary-value">0</span>
            <span class="summary-hint">Kurs og instruksjoner</span>
          </div>
          <div class="summary-card">
            <span class="summary-label">Sertifikater</span>
            <span class="summary-value summary-value--warning">0</span>
            <span class="summary-hint">Utløper snart: ingen</span>
          </div>
        </div>

        <!-- Action bar -->
        <div class="action-bar">
          <div class="search-wrap">
            <span class="search-icon">&#128269;</span>
            <input class="search-input" type="search" placeholder="Søk i dokumenter..." />
          </div>
          <div class="filter-group">
            <label class="filter-label" for="filter-cat">Kategori</label>
            <select class="filter-select" id="filter-cat">
              <option value="">Alle kategorier</option>
              <option value="policy">Retningslinjer</option>
              <option value="training">Opplæringsmateriell</option>
              <option value="cert">Sertifikater</option>
            </select>
          </div>
          <div class="filter-group">
            <label class="filter-label" for="filter-module">Modul</label>
            <select class="filter-select" id="filter-module">
              <option value="">Alle moduler</option>
              <option value="shared">Felles</option>
              <option value="food">IC-Food</option>
              <option value="alcohol">IC-Alcohol</option>
            </select>
          </div>
          <button class="btn-upload" type="button">+ Last opp dokument</button>
        </div>

        <!-- Category sections -->
        <section>
          <div class="category-header">
            <h2 class="section-heading">Retningslinjer</h2>
            <span class="category-count">0 dokumenter</span>
          </div>
          <div class="doc-list">
            <div class="empty-state">
              <p class="empty-title">Ingen retningslinjer lastet opp</p>
              <p class="empty-sub">Last opp bedriftens policies og prosedyrer for hygiene og alkoholhåndtering.</p>
            </div>
            <!-- Document row structure (will be v-for rendered later) -->
            <div class="doc-row" style="display: none;" aria-hidden="true">
              <div class="doc-icon doc-icon--pdf">PDF</div>
              <div class="doc-info">
                <span class="doc-name">Hygienepolicy 2026.pdf</span>
                <span class="doc-meta">Lastet opp 15. jan 2026 · 1.2 MB</span>
              </div>
              <span class="doc-badge doc-badge--shared">Felles</span>
              <div class="doc-actions">
                <button class="doc-btn" type="button">Last ned</button>
                <button class="doc-btn doc-btn--danger" type="button">Slett</button>
              </div>
            </div>
          </div>
        </section>

        <section>
          <div class="category-header">
            <h2 class="section-heading">Opplæringsmateriell</h2>
            <span class="category-count">0 dokumenter</span>
          </div>
          <div class="doc-list">
            <div class="empty-state">
              <p class="empty-title">Ingen opplæringsmateriell lastet opp</p>
              <p class="empty-sub">Legg til kursmateriell, instruksjoner og opplæringsdokumenter for ansatte.</p>
            </div>
          </div>
        </section>

        <section>
          <div class="category-header">
            <h2 class="section-heading">Sertifikater</h2>
            <span class="category-count">0 sertifikater</span>
          </div>
          <div class="doc-list">
            <div class="empty-state">
              <p class="empty-title">Ingen sertifikater registrert</p>
              <p class="empty-sub">Legg til sertifikater for ansatte, f.eks. serveringsbevilling og mattrygghetskurs.</p>
            </div>
            <!-- Certificate row structure (will be v-for rendered later) -->
            <div class="cert-row" style="display: none;" aria-hidden="true">
              <div class="doc-icon doc-icon--cert">CERT</div>
              <div class="doc-info">
                <span class="doc-name">Serveringsbevillingskurs – Kari Nordmann</span>
                <span class="doc-meta">Utstedt 10. jan 2025 · Utløper 10. jan 2027</span>
              </div>
              <span class="cert-expiry cert-expiry--ok">Gyldig</span>
              <div class="doc-actions">
                <button class="doc-btn" type="button">Last ned</button>
                <button class="doc-btn doc-btn--danger" type="button">Slett</button>
              </div>
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

/* ── Summary ── */
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

/* ── Action bar ── */
.action-bar {
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

.search-wrap {
  position: relative;
  flex: 1;
  min-width: 200px;
}

.search-icon {
  position: absolute;
  left: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  font-size: var(--font-size-md);
  pointer-events: none;
}

.search-input {
  width: 100%;
  height: 36px;
  padding: 0 var(--space-3) 0 var(--space-8);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  background: var(--color-bg-primary);
  font-family: inherit;
  outline: none;
  box-sizing: border-box;
  transition: border-color var(--transition-fast);
}

.search-input:focus {
  border-color: var(--color-dark-secondary);
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

.filter-select {
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

.filter-select:focus {
  border-color: var(--color-dark-secondary);
}

.btn-upload {
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
  white-space: nowrap;
  transition: opacity var(--transition-fast);
  margin-left: auto;
}

.btn-upload:hover {
  opacity: 0.85;
}

/* ── Category header ── */
.category-header {
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.section-heading {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.category-count {
  font-size: var(--font-size-sm);
  color: var(--color-text-hint);
}

/* ── Document list ── */
.doc-list {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

/* ── Empty state ── */
.empty-state {
  padding: var(--space-10) var(--space-6);
  text-align: center;
}

.empty-title {
  margin: 0 0 var(--space-2) 0;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.empty-sub {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  max-width: 420px;
  margin-inline: auto;
}

/* ── Document row ── */
.doc-row,
.cert-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-6);
  border-bottom: 1px solid var(--color-border-subtle);
}

.doc-row:last-child,
.cert-row:last-child {
  border-bottom: none;
}

.doc-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  flex-shrink: 0;
}

.doc-icon--pdf {
  background: var(--color-danger-bg);
  color: var(--color-danger-text);
}

.doc-icon--cert {
  background: var(--color-accent-light);
  color: var(--color-accent-text);
}

.doc-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 0;
}

.doc-name {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.doc-meta {
  font-size: var(--font-size-xs);
  color: var(--color-text-hint);
}

.doc-badge {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  padding: 2px var(--space-2);
  border-radius: var(--radius-full);
  white-space: nowrap;
}

.doc-badge--shared {
  background: var(--color-bg-subtle);
  color: var(--color-dark-tertiary);
}

.cert-expiry {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  padding: 2px var(--space-2);
  border-radius: var(--radius-full);
  white-space: nowrap;
}

.cert-expiry--ok {
  background: var(--color-success-bg);
  color: var(--color-success-text);
}

.cert-expiry--warning {
  background: var(--color-warning-bg);
  color: var(--color-warning-text);
}

.cert-expiry--expired {
  background: var(--color-danger-bg);
  color: var(--color-danger-text);
}

.doc-actions {
  display: flex;
  gap: var(--space-2);
  flex-shrink: 0;
}

.doc-btn {
  padding: 4px var(--space-3);
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

.doc-btn:hover {
  border-color: var(--color-dark-primary);
  color: var(--color-dark-primary);
}

.doc-btn--danger {
  color: var(--color-danger);
  border-color: var(--color-danger-border);
}

.doc-btn--danger:hover {
  background: var(--color-danger-bg);
  border-color: var(--color-danger);
}

/* ── Responsive ── */
@media (max-width: 900px) {
  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .btn-upload {
    margin-left: 0;
  }
}
</style>