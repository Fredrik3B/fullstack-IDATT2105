export const IC_MODULE_CONFIG = {
  IC_FOOD: {
    module: 'IC_FOOD',
    moduleLabel: 'IC-Food',
    routeName: 'ic-food-dashboard',
    routePath: '/ic-food'
  },
  IC_ALCOHOL: {
    module: 'IC_ALCOHOL',
    moduleLabel: 'IC-Alcohol',
    routeName: 'ic-alcohol-dashboard',
    routePath: '/ic-alcohol'
  }
}

export function getIcModuleConfig(module) {
  return IC_MODULE_CONFIG[module] ?? IC_MODULE_CONFIG.IC_FOOD
}
