import { createRouter, createWebHistory } from 'vue-router'
import icFoodLayout from '../views/ic-food/icFoodLayout.vue'
import icFoodDashboard from '../views/ic-food/icFoodDashboard.vue'
import icAlcoholLayout from '../views/ic-alcohol/icAlcoholLayout.vue'
import icAlcoholDashboard from '../views/ic-alcohol/icAlcoholDashboard.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/ic-alcohol'
    },
    {
      path: '/ic-food',
      component: icFoodLayout,
      children: [
        {
          path: '',
          name: 'ic-food',
          component: icFoodDashboard
        }
      ]
    },
    {
      path: '/ic-alcohol',
      component: icAlcoholLayout,
      children: [
        {
          path: '',
          name: 'ic-alcohol',
          component: icAlcoholDashboard
        }
      ]
    }
  ],
})

export default router
