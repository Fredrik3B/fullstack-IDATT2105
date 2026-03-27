import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../components/layout/AppLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/auth/LoginView.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/auth/RegisterView.vue')
    },
    {
      path: '/onboarding',
      name: 'onboarding',
      component: () => import('../views/auth/RestaurantOnboardingView.vue')
    },
    {
      path: '/onboarding/create',
      name: 'create-restaurant',
      component: () => import('../views/auth/CreateRestaurantView.vue')
    },
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
