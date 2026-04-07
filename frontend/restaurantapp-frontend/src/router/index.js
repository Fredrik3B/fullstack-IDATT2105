import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../components/layout/AppLayout.vue'
import { useAuthStore } from '../stores/auth'

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
          component: () => import('../views/ic-views/IcModuleLayout.vue'),
          children: [
            {
              path: '',
              name: 'ic-food-dashboard',
              component: () => import('../views/ic-views/IcModuleDashboardView.vue'),
              props: { module: 'IC_FOOD' }
            }
          ]
        },
        {
          path: 'ic-alcohol',
          name: 'ic-alcohol',
          component: () => import('../views/ic-views/IcModuleLayout.vue'),
          children: [
            {
              path: '',
              name: 'ic-alcohol-dashboard',
              component: () => import('../views/ic-views/IcModuleDashboardView.vue'),
              props: { module: 'IC_ALCOHOL' }
            }
          ]
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
        },
        {
          path: 'admin/requests',
          name: 'admin-requests',
          component: () => import('../views/admin/AdminPanel.vue')
        }
      ]
    }
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  await auth.initAuth()

  const isPublic = ['login', 'register'].includes(to.name)
  const isOnboarding = ['onboarding', 'create-restaurant'].includes(to.name)
  const isAdminOnly = ['admin-requests'].includes(to.name)

  if (!auth.isAuthenticated) {
    if (isPublic) return true
    return { name: 'login' }
  }

  if (isPublic) {
    return auth.hasActiveRestaurant
      ? { name: 'dashboard' }
      : { name: 'onboarding' }
  }

  if (!auth.hasActiveRestaurant) {
    if (isOnboarding) return true
    return { name: 'onboarding' }
  }

  if (isOnboarding) return { name: 'dashboard' }

  if (isAdminOnly && !auth.isAdminOrManager) return { name: 'dashboard' }

  return true
})

export default router
