import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../components/layout/AppLayout.vue'
import MainDashboard from '../views/MainDashboard.vue'
import DocumentsView from '../views/DocumentsView.vue'
import ReportsView from '../views/ReportsView.vue'
import icFoodDashboard from '../views/ic-food/icFoodDashboard.vue'
import icAlcoholDashboard from '../views/ic-alcohol/icAlcoholDashboard.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: AppLayout,
      children: [
        {
          path: '',
          name: 'dashboard',
          component: MainDashboard
        },
        {
          path: 'ic-food',
          name: 'ic-food',
          component: icFoodDashboard
        },
        {
          path: 'ic-alcohol',
          name: 'ic-alcohol',
          component: icAlcoholDashboard
        },
        {
          path: 'documents',
          name: 'documents',
          component: DocumentsView
        },
        {
          path: 'reports',
          name: 'reports',
          component: ReportsView
        }
      ]
    }
  ],
})

export default router
