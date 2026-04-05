<script setup>
import ChecklistDashboard from '../../features/ic-checklists/ChecklistDashboard.vue'
import ChecklistLibraryModal from '../../features/ic-checklists/ChecklistLibraryModal.vue'
import CreateChecklistModal from '../../features/ic-checklists/CreateChecklistModal.vue'
import ManageTaskTemplatesModal from '../../features/ic-checklists/ManageTaskTemplatesModal.vue'
import { getIcModuleConfig } from '../../features/ic-checklists/moduleConfig'
import { useIcModulePage } from '../../features/ic-checklists/useIcModulePage'

const props = defineProps({
  module: {
    type: String,
    required: true,
  },
})

const config = getIcModuleConfig(props.module)

const {
  activePeriod,
  cards,
  workbenchCards,
  loadedChecklistIds,
  highlightedChecklistId,
  now,
  dateLabel,
  isLoading,
  loadError,
  isCreateOpen,
  isEditOpen,
  isLibraryOpen,
  isTaskPoolOpen,
  editingCard,
  deleteDialog,
  togglePending,
  toggleTask,
  submitCard,
  logTemperatureMeasurement,
  reloadChecklists,
  openCreateModal,
  openLibraryModal,
  openTaskPoolModal,
  editChecklist,
  closeEditModal,
  requestDeleteChecklist,
  closeDeleteDialog,
  handleCreatedChecklist,
  handleUpdatedChecklist,
  handleDeleteChecklist,
  openChecklistOnWorkbench,
  removeChecklistFromWorkbench,
} = useIcModulePage(config)
</script>

<template>
  <ChecklistDashboard
    :module-label="config.moduleLabel"
    title="Checklists"
    :date-label="dateLabel"
    :module-description="config.moduleDescription"
    :summary-hint="config.summaryHint"
    v-model:activePeriod="activePeriod"
    :cards="workbenchCards"
    :highlighted-checklist-id="highlightedChecklistId"
    :now="now"
    :is-loading="isLoading"
    :load-error="loadError"
    @open-library="openLibraryModal"
    @manage-tasks="openTaskPoolModal"
    @create="openCreateModal"
    @refresh="reloadChecklists"
    @toggle-task="toggleTask"
    @toggle-pending="togglePending"
    @submit-checklist="submitCard"
    @log-temperature="logTemperatureMeasurement"
    @edit-checklist="editChecklist"
  />

  <CreateChecklistModal
    v-model:open="isCreateOpen"
    :module="config.module"
    :module-label="config.moduleLabel"
    @manage-tasks="openTaskPoolModal"
    @created="handleCreatedChecklist"
  />

  <CreateChecklistModal
    v-model:open="isEditOpen"
    mode="edit"
    :initial-card="editingCard"
    :module="config.module"
    :module-label="config.moduleLabel"
    @manage-tasks="openTaskPoolModal"
    @close="closeEditModal"
    @delete="requestDeleteChecklist"
    @remove-from-workbench="removeChecklistFromWorkbench"
    @updated="handleUpdatedChecklist"
  />

  <ChecklistLibraryModal
    v-model:open="isLibraryOpen"
    :module-label="config.moduleLabel"
    :cards="cards"
    :loaded-checklist-ids="loadedChecklistIds"
    @open-checklist="openChecklistOnWorkbench"
  />

  <ManageTaskTemplatesModal
    v-model:open="isTaskPoolOpen"
    :module="config.module"
    :module-label="config.moduleLabel"
  />

  <div
    v-if="deleteDialog.open"
    class="confirm-overlay"
    role="dialog"
    aria-modal="true"
    aria-label="Delete checklist"
  >
    <div class="confirm-dialog">
      <div class="confirm-kicker">{{ config.moduleLabel }}</div>
      <h2>Delete checklist?</h2>
      <p>
        "{{ deleteDialog.checklist?.title || 'This checklist' }}" will be removed from the library
        and any current workbench usage.
      </p>
      <div class="confirm-note">This action cannot be undone.</div>
      <div class="confirm-actions">
        <button type="button" class="confirm-secondary" @click="closeDeleteDialog">Cancel</button>
        <button
          type="button"
          class="confirm-danger"
          @click="handleDeleteChecklist(deleteDialog.checklist)"
        >
          Delete checklist
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.confirm-overlay {
  position: fixed;
  inset: 0;
  z-index: 90;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
  background: rgba(12, 16, 28, 0.52);
  backdrop-filter: blur(2px);
}

.confirm-dialog {
  width: min(440px, 100%);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
  background: var(--color-bg-primary);
  box-shadow: var(--shadow-lg);
  padding: var(--space-6);
  display: grid;
  gap: var(--space-4);
}

.confirm-kicker {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.confirm-dialog h2 {
  margin: 0;
  font-size: var(--font-size-2xl);
  line-height: 1.2;
  color: var(--color-text-primary);
}

.confirm-dialog p,
.confirm-note {
  margin: 0;
  font-size: var(--font-size-md);
  line-height: var(--line-height-normal);
  color: var(--color-text-secondary);
}

.confirm-note {
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  background: var(--color-danger-bg);
  color: var(--color-danger-text);
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}

.confirm-secondary,
.confirm-danger {
  min-height: 40px;
  padding: 0 var(--space-4);
  border-radius: var(--radius-md);
  border: 1px solid transparent;
  font: inherit;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
}

.confirm-secondary {
  background: var(--color-bg-secondary);
  border-color: var(--color-border);
  color: var(--color-text-primary);
}

.confirm-danger {
  background: var(--color-danger);
  color: #ffffff;
}

@media (max-width: 720px) {
  .confirm-actions {
    flex-direction: column-reverse;
  }

  .confirm-secondary,
  .confirm-danger {
    width: 100%;
  }
}
</style>
