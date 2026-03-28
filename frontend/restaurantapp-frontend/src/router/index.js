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
          component: () => import('../views/ic-views/ic-food/icFoodLayout.vue'),
          children: [
            {
              path: '',
              name: 'ic-food-dashboard',
              component: () => import('../views/ic-views/ic-food/icFoodDashboard.vue')
            }
          ]
        },
        {
          path: 'ic-alcohol',
          name: 'ic-alcohol',
          component: () => import('../views/ic-views/ic-alcohol/icAlcoholLayout.vue'),
          children: [
            {
              path: '',
              name: 'ic-alcohol-dashboard',
              component: () => import('../views/ic-views/ic-alcohol/icAlcoholDashboard.vue')
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
        }
      ]
    }
  ],
})

// ── Navigation guards ──────────────────────────────────────────────────────
//
// Route categories:
//   public      — /login, /register  (redirect away if already logged in)
//   onboarding  — /onboarding, /onboarding/create  (require auth, no restaurant yet)
//   app         — everything else  (require auth + active restaurant)

/* router.beforeEach((to) => {
  const auth = useAuthStore()

  const isPublic     = ['login', 'register'].includes(to.name)
  const isOnboarding = ['onboarding', 'create-restaurant'].includes(to.name)

  // ── Unauthenticated user ──
  if (!auth.isAuthenticated) {
    if (isPublic) return true
    return { name: 'login' }
  }

  // ── Authenticated, visiting login/register — send them into the app ──
  if (isPublic) {
    return auth.hasActiveRestaurant
      ? { name: 'dashboard' }
      : { name: 'onboarding' }
  }

  // ── Authenticated, no active restaurant ──
  if (!auth.hasActiveRestaurant) {
    if (isOnboarding) return true
    return { name: 'onboarding' }
  }

  // ── Authenticated with active restaurant, trying to visit onboarding ──
  if (isOnboarding) return { name: 'dashboard' }

  // ── All clear ──
  return true
})
*/
export default router
