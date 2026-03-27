import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../components/layout/AppLayout.vue'

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
          component: () => import('../views/MainDashboard.vue')
        },
        {
          path: 'ic-food',
          name: 'ic-food',
          component: () => import('../views/ic-food/icFoodDashboard.vue')
        },
        {
          path: 'ic-alcohol',
          name: 'ic-alcohol',
          component: () => import('../views/ic-alcohol/icAlcoholDashboard.vue')
        },
        {
          path: 'documents',
          name: 'documents',
          component: () => import('../views/DocumentsView.vue')
        },
        {
          path: 'reports',
          name: 'reports',
          component: () => import('../views/ReportsView.vue')
        }
      ]
    }
  ],
})

export default router
