import { useMutation, useQueryClient } from '@tanstack/react-query';
import { reportsApi } from './api';

export function useCreateReport() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload) => reportsApi.create(payload),
    onSuccess: () => {
      // Invalida el caché de reportes para que la lista se actualice automáticamente
      queryClient.invalidateQueries({ queryKey: ['pets'] });
    },
  });
}
