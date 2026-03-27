<script setup>
import { ref } from 'vue'
import ChecklistDashboard from '../checklists/ChecklistDashboard.vue'

const cards = ref([
  {
    title: 'Skjenkerutine',
    subtitle: 'IK-Alkohol - daglig',
    statusLabel: '2/5 fullfort',
    statusTone: 'warning',
    progress: 40,
    featured: true,
    moduleChip: 'IK-Alkohol',
    sections: [
      {
        title: 'Opplaering og sertifisering',
        items: [
          { label: 'Bekreft intern opplaering for kveldsskift', meta: '08:00', state: 'completed' }
        ]
      },
      {
        title: 'Daglig logging',
        items: [
          { label: 'Loggfor antall avviste gjester', meta: 'Avventer', state: 'pending', highlighted: true },
          { label: 'Bekreft ansvarlig skjenking gjennomgaatt', meta: 'Manglar', state: 'todo' }
        ]
      },
      {
        title: 'Dokumentasjon',
        items: [
          { label: 'Sjekk gyldighet pa skjenkesertifikat', meta: '18:00', state: 'todo' },
          { label: 'Registrer eventuelle avvik i vaktlogg', meta: 'Etter stengetid', state: 'todo' }
        ]
      }
    ]
  },
  {
    title: 'Helgerapport',
    subtitle: 'Ukentlig - fredag',
    statusLabel: 'Planlagt',
    statusTone: 'muted',
    progress: null,
    sections: [
      {
        title: 'Kontrollpunkt',
        items: [
          { label: 'Bekreft bemanningsplan for helg', meta: 'Fredag', state: 'todo' },
          { label: 'Gjennomga lager for alkoholfrie alternativ', meta: 'Fredag', state: 'todo' }
        ]
      }
    ]
  }
])

function recalcCardProgress(cardIndex) {
  const card = cards.value[cardIndex]
  if (!card) return

  const sections = Array.isArray(card.sections) ? card.sections : []
  const total = sections.reduce((acc, section) => acc + (Array.isArray(section.items) ? section.items.length : 0), 0)
  const completed = sections.reduce((acc, section) => {
    if (!Array.isArray(section.items)) return acc
    return acc + section.items.filter((t) => t.state === 'completed').length
  }, 0)

  if (total <= 0) {
    card.progress = null
    return
  }

  card.progress = Math.round((completed / total) * 100)

  const lower = String(card.statusLabel ?? '').toLowerCase()
  const doneWord = lower.includes('fullfort') ? 'fullfort' : 'completed'
  card.statusLabel = completed === total ? (doneWord === 'fullfort' ? 'Fullfort' : 'Completed') : `${completed}/${total} ${doneWord}`
}

function toggleTask({ cardIndex, sectionIndex, taskIndex }) {
  const task = cards.value[cardIndex].sections[sectionIndex].items[taskIndex]
  if (task.state === 'completed') {
    task.state = 'todo'
  } else {
    task.state = 'completed'
    task.highlighted = false
  }

  recalcCardProgress(cardIndex)
}

function togglePending({ cardIndex, sectionIndex, taskIndex }) {
  const task = cards.value[cardIndex].sections[sectionIndex].items[taskIndex]
  const isPending = task.state === 'pending'

  if (isPending) {
    task.state = 'todo'
    task.highlighted = false
  } else {
    task.state = 'pending'
    task.highlighted = true
  }

  recalcCardProgress(cardIndex)
}

cards.value.forEach((card, idx) => {
  const hasCountLabel = /\d+\s*\/\s*\d+/.test(String(card.statusLabel ?? ''))
  if (card.progress !== null || hasCountLabel) recalcCardProgress(idx)
})

function editChecklist({ cardIndex }) {
  console.log("Placeholder: open UI for checklist editing| cardIndex: ", cardIndex)
}
</script>

<template>
  <ChecklistDashboard
    module-label="IK-Alkohol"
    title="Sjekklister"
    date-label="Torsdag 26. mars 2026"
    active-period="Daglig"
    :cards="cards"
    @toggle-task="toggleTask"
    @toggle-pending="togglePending"
    @edit-checklist="editChecklist"
  />
</template>
