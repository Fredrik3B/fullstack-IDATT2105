/**
 * Static config per IC module used by routing and dashboard UI.
 *
 * @type {Record<string, {
 *   module: string,
 *   moduleLabel: string,
 *   routeName: string,
 *   routePath: string,
 *   moduleDescription: string,
 *   summaryHint: string
 * }>}
 */
export const IC_MODULE_CONFIG = {
  IC_FOOD: {
    module: 'IC_FOOD',
    moduleLabel: 'IC-Food',
    routeName: 'ic-food-dashboard',
    routePath: '/ic-food',
    moduleDescription:
      'Food safety routines, hygiene controls, and temperature follow-up for daily operations.',
    summaryHint:
      'Track checklist completion, flagged follow-up, and temperature deviations in one workbench.',
  },
  IC_ALCOHOL: {
    module: 'IC_ALCOHOL',
    moduleLabel: 'IC-Alcohol',
    routeName: 'ic-alcohol-dashboard',
    routePath: '/ic-alcohol',
    moduleDescription:
      'Alcohol service routines, compliance checkpoints, and operational follow-up for staff.',
    summaryHint:
      'Keep the service team aligned on active controls, recurring checklists, and documented readings.',
  },
}

/**
 * Resolve module config, falling back to IC_FOOD.
 *
 * @param {string|null|undefined} module
 * @returns {typeof IC_MODULE_CONFIG[keyof typeof IC_MODULE_CONFIG]}
 */
export function getIcModuleConfig(module) {
  return IC_MODULE_CONFIG[module] ?? IC_MODULE_CONFIG.IC_FOOD
}
