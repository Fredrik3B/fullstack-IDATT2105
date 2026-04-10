import { useToastStore } from '@/stores/toast'

/**
 * Convenience composable returning the shared toast store instance.
 *
 * @returns {ReturnType<typeof useToastStore>}
 */
export function useToast() {
  return useToastStore()
}
