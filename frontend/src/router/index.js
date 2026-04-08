import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../components/layout/AppLayout.vue'
import { useAuthStore } from '../stores/auth'

/**
 * Route names grouped by access policy.
 *
 * Using constants keeps guard checks centralized and avoids repeating
 * string literals across condition branches.
 */
const PUBLIC_ROUTE_NAMES = ['login', 'register']
const ONBOARDING_ROUTE_NAMES = ['onboarding', 'create-restaurant']
const ADMIN_ONLY_ROUTE_NAMES = ['admin-requests']

/** @type {import('vue-router').Router} */
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
          component: () => import('../views/dashboard/MainDashboard.vue')
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
          component: () => import('../views/documents/DocumentsView.vue')
        },
        {
          path: 'reports',
          name: 'reports',
          component: () => import('../views/reports/ReportsView.vue')
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

/**
 * Global auth/access guard.
 *
 * Flow:
 * 1. Restore auth state (idempotent in auth store).
 * 2. Allow/redirect based on login state.
 * 3. Enforce onboarding completion.
 * 4. Enforce admin-only pages.
 *
 * @param {import('vue-router').RouteLocationNormalized} to
 * @returns {Promise<true | import('vue-router').RouteLocationRaw>}
 */
router.beforeEach(async (to) => {
  const auth = useAuthStore()
  await auth.initAuth()

  const isPublic = PUBLIC_ROUTE_NAMES.includes(to.name)
  const isOnboarding = ONBOARDING_ROUTE_NAMES.includes(to.name)
  const isAdminOnly = ADMIN_ONLY_ROUTE_NAMES.includes(to.name)

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

/** Shared application router with access control guard. */
export default router
