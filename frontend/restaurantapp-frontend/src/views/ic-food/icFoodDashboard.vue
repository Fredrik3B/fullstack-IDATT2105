<script setup>
import ChecklistDashboard from '../../components/checklists/ChecklistDashboard.vue'

const cards = [
  {
    title: 'Before opening',
    subtitle: 'Daily - opening',
    statusLabel: '4/5 completed',
    statusTone: 'success',
    progress: 80,
    sections: [
      {
        title: 'Temperaturkontroll',
        items: [
          { label: 'Sjekk kjolerom 1 (mal: 2 - 4 C)', meta: '2.1 C', state: 'completed' },
          { label: 'Sjekk fryser (mal: under -18 C)', meta: '-19 C', state: 'completed' }
        ]
      },
      {
        title: 'Hygiene',
        items: [
          { label: 'Rengjoring av arbeidsflater', meta: '08:30', state: 'completed' },
          { label: 'Kontroller varmholder-temperatur', meta: 'Gjenstar', state: 'pending', highlighted: true },
          { label: 'Sjekk merking og datoer pa varer', meta: '09:00', state: 'completed' }
        ]
      }
    ]
  },
  {
    title: 'Kveldslukking',
    subtitle: 'Daglig - stenging',
    statusLabel: '0/6 fullfort',
    statusTone: 'muted',
    progress: null,
    sections: [
      {
        title: 'Rengjoring',
        items: [
          { label: 'Vask og desinfiser alle arbeidsflater', meta: '', state: 'todo' },
          { label: 'Tom og vask soppeldunker', meta: '', state: 'todo' },
          { label: 'Rengjor stekeovn og frityrkokere', meta: '', state: 'todo' }
        ]
      },
      {
        title: 'Lagerkontroll',
        items: [
          { label: 'Sjekk utlopsdatoer pa ravarer', meta: '', state: 'todo' },
          { label: 'Kveldsmaaling kjolerom og fryser', meta: '', state: 'todo' },
          { label: 'Las og aktiver alarm', meta: '', state: 'todo' }
        ]
      }
    ]
  },
  {
    title: 'Ukentlig vedlikehold',
    subtitle: 'Ukentlig - mandag',
    statusLabel: 'Fullfort',
    statusTone: 'success',
    progress: null,
    sections: [
      {
        title: 'Utstyr',
        items: [
          { label: 'Dyprengjoring kjokkenvifte og filter', meta: 'Man', state: 'completed' },
          { label: 'Sjekk og kalibrer termometre', meta: 'Man', state: 'completed' }
        ]
      },
      {
        title: 'Dokumentasjon',
        items: [
          { label: 'Gjennomgang av opplaeringslogg', meta: 'Man', state: 'completed' },
          { label: 'Kontroller sertifikater ansatte', meta: 'Man', state: 'completed' }
        ]
      }
    ]
  },
  {
    title: 'Skjenkerutine',
    subtitle: 'IK-Alkohol - daglig',
    statusLabel: '2/4 fullfort',
    statusTone: 'warning',
    progress: 50,
    featured: true,
    moduleChip: 'IK-Alkohol',
    sections: [
      {
        title: 'Opplaering og sertifisering',
        items: [
          { label: 'Opplaering aldersverifisering bekreftet', meta: '08:00', state: 'completed' }
        ]
      },
      {
        title: 'Daglig logging',
        items: [
          { label: 'Loggfor antall avviste gjester', meta: '', state: 'todo' },
          { label: 'Bekreft ansvarlig skjenking gjennomgaatt', meta: '', state: 'todo' }
        ]
      },
      {
        title: 'Dokumentasjon',
        items: [
          { label: 'Sjekk gyldighet skjenkesertifikat', meta: '', state: 'todo' }
        ]
      }
    ]
  }
]
</script>

<template>
  <ChecklistDashboard
    module-label="IC-Food"
    title="Checklists"
    date-label="Insert-date"
    active-period="Daglig"
    :cards="cards"
  />
</template>
