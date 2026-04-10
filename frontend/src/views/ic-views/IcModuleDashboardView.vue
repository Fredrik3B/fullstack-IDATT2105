<script setup>
import { computed, ref } from 'vue'
import ChecklistDashboard from '@/components/ic-checklists/ChecklistDashboard.vue'
import alcoholImage from '@/assets/images/GraderAvAlkoholpåvirkning.jpg'
import ChecklistLibraryModal from '@/components/ic-checklists/ChecklistLibraryModal.vue'
import CreateChecklistModal from '@/components/ic-checklists/CreateChecklistModal.vue'
import ManageTaskTemplatesModal from '@/components/ic-checklists/ManageTaskTemplatesModal.vue'
import SharedConfirmDialog from '@/components/ic-checklists/SharedConfirmDialog.vue'
import { getIcModuleConfig } from '@/composables/ic-checklists/moduleConfig'
import { useIcModulePage } from '@/composables/ic-checklists/useIcModulePage'

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
  canManageChecklists,
  canManageTaskPool,
  reminderSummary,
  loadedChecklistIds,
  highlightedChecklistId,
  now,
  dateLabel,
  isLoading,
  isRefreshing,
  loadError,
  isCreateOpen,
  isEditOpen,
  isLibraryOpen,
  isTaskPoolOpen,
  isCreatingChecklist,
  isUpdatingChecklist,
  openingChecklistId,
  deletingChecklistId,
  removingChecklistId,
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

const deleteDialogMessage = computed(
  () =>
    `"${deleteDialog.value.checklist?.title || 'This checklist'}" will be removed from the library and any current workbench usage.`,
)
const taskPoolRefreshToken = ref(0)

function handleTaskPoolChanged() {
  taskPoolRefreshToken.value += 1
}
</script>

<template>
  <ChecklistDashboard
    :module-label="config.moduleLabel"
    title="Checklists"
    :date-label="dateLabel"
    :module-description="config.moduleDescription"
    :summary-hint="config.summaryHint"
    :can-manage-checklists="canManageChecklists"
    :can-manage-task-pool="canManageTaskPool"
    v-model:activePeriod="activePeriod"
    :cards="workbenchCards"
    :reminder-summary="reminderSummary"
    :highlighted-checklist-id="highlightedChecklistId"
    :now="now"
    :is-loading="isLoading"
    :is-refreshing="isRefreshing"
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
  >
    <template v-if="props.module === 'IC_ALCOHOL'" #below-content>
      <div class="alcohol-info-panel">
        <div class="alcohol-info-panel__image-card">
          <img :src="alcoholImage" alt="Grader av alkoholpåvirkning" class="alcohol-info-panel__image" />
        </div>
        <div class="alcohol-info-panel__text-card">
          <h2 class="alcohol-info-panel__title">Serveringsansvar</h2>
          <p class="alcohol-info-panel__lead">
            Som ansatt med skjenkebevilling har du et personlig ansvar for å sikre at alkohol ikke
            serveres til mindreårige, synlig berusede personer, eller på en måte som bryter
            skjenkelovgivningen.
          </p>
          <h3 class="alcohol-info-panel__subtitle">Konsekvenser ved brudd</h3>
          <ul class="alcohol-info-panel__list">
            <li>Inndragning av skjenkebevilling – midlertidig eller permanent</li>
            <li>Bøter og sanksjoner mot virksomheten</li>
            <li>Personlig straffeansvar for den ansatte som serverte</li>
            <li>Erstatningskrav ved skade forårsaket av overstadig berusede gjester</li>
          </ul>
          <p class="alcohol-info-panel__footer">
            Bruk sjekklistene aktivt og dokumenter alle avvik. God internkontroll er ditt beste
            vern.
          </p>
        </div>
      </div>
    </template>

  </ChecklistDashboard>

  <CreateChecklistModal
    v-model:open="isCreateOpen"
    :module="config.module"
    :module-label="config.moduleLabel"
    :can-manage-checklists="canManageChecklists"
    :can-manage-tasks="canManageTaskPool"
    :task-pool-refresh-token="taskPoolRefreshToken"
    :save-pending="isCreatingChecklist"
    @manage-tasks="openTaskPoolModal"
    @created="handleCreatedChecklist"
  />

  <CreateChecklistModal
    v-model:open="isEditOpen"
    mode="edit"
    :initial-card="editingCard"
    :module="config.module"
    :module-label="config.moduleLabel"
    :can-manage-checklists="canManageChecklists"
    :can-manage-tasks="canManageTaskPool"
    :task-pool-refresh-token="taskPoolRefreshToken"
    :save-pending="isUpdatingChecklist"
    :delete-pending="String(deletingChecklistId ?? '') === String(editingCard?.id ?? '')"
    :remove-pending="String(removingChecklistId ?? '') === String(editingCard?.id ?? '')"
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
    :opening-checklist-id="openingChecklistId"
    @open-checklist="openChecklistOnWorkbench"
  />

  <ManageTaskTemplatesModal
    v-model:open="isTaskPoolOpen"
    :module="config.module"
    :module-label="config.moduleLabel"
    @changed="handleTaskPoolChanged"
  />

  <SharedConfirmDialog
    v-model:open="deleteDialog.open"
    :kicker="config.moduleLabel"
    title="Delete checklist?"
    :message="deleteDialogMessage"
    detail="This action cannot be undone."
    confirm-label="Delete checklist"
    :is-processing="String(deletingChecklistId ?? '') === String(deleteDialog.checklist?.id ?? '')"
    tone="danger"
    @cancel="closeDeleteDialog"
    @confirm="handleDeleteChecklist(deleteDialog.checklist)"
  />
</template>

<style scoped>
.alcohol-info-panel {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
  align-items: stretch;
}

.alcohol-info-panel__image-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  background: var(--color-bg-primary);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.alcohol-info-panel__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.alcohol-info-panel__text-card {
  padding: var(--space-6);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  background: var(--color-bg-primary);
  box-shadow: var(--shadow-sm);
}

.alcohol-info-panel__title {
  margin: 0 0 var(--space-3);
  font-size: var(--font-size-xl);
  color: var(--color-text-primary);
}

.alcohol-info-panel__lead {
  margin: 0 0 var(--space-5);
  font-size: var(--font-size-md);
  line-height: var(--line-height-normal);
  color: var(--color-text-secondary);
}

.alcohol-info-panel__subtitle {
  margin: 0 0 var(--space-2);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.alcohol-info-panel__list {
  margin: 0 0 var(--space-5);
  padding-left: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
  line-height: var(--line-height-normal);
  color: var(--color-text-secondary);
}

.alcohol-info-panel__footer {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  font-style: italic;
}

@media (max-width: 980px) {
  .alcohol-info-panel {
    grid-template-columns: 1fr;
  }
}
</style>
