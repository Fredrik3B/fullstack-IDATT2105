<script setup>
import { computed } from 'vue'
import ChecklistDashboard from '../../features/ic-checklists/ChecklistDashboard.vue'
import ChecklistLibraryModal from '../../features/ic-checklists/ChecklistLibraryModal.vue'
import CreateChecklistModal from '../../features/ic-checklists/CreateChecklistModal.vue'
import ManageTaskTemplatesModal from '../../features/ic-checklists/ManageTaskTemplatesModal.vue'
import SharedConfirmDialog from '../../features/ic-checklists/SharedConfirmDialog.vue'
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
  canManageTaskPool,
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
</script>

<template>
  <ChecklistDashboard
    :module-label="config.moduleLabel"
    title="Checklists"
    :date-label="dateLabel"
    :module-description="config.moduleDescription"
    :summary-hint="config.summaryHint"
    :can-manage-task-pool="canManageTaskPool"
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
    :can-manage-tasks="canManageTaskPool"
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
    :can-manage-tasks="canManageTaskPool"
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
