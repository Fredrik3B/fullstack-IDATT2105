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
    required: true
  }
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
  isCreateOpen,
  isEditOpen,
  isLibraryOpen,
  isTaskPoolOpen,
  editingCard,
  togglePending,
  toggleTask,
  submitCard,
  logTemperatureMeasurement,
  openCreateModal,
  openLibraryModal,
  openTaskPoolModal,
  editChecklist,
  closeEditModal,
  handleCreatedChecklist,
  handleUpdatedChecklist,
  handleDeleteChecklist,
  openChecklistOnWorkbench
} = useIcModulePage(config)
</script>

<template>
  <ChecklistDashboard
    :module-label="config.moduleLabel"
    title="Checklists"
    :date-label="dateLabel"
    v-model:activePeriod="activePeriod"
    :cards="workbenchCards"
    :highlighted-checklist-id="highlightedChecklistId"
    :now="now"
    @open-library="openLibraryModal"
    @manage-tasks="openTaskPoolModal"
    @create="openCreateModal"
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
    @delete="handleDeleteChecklist"
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
</template>
