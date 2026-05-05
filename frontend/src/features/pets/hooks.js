import { useQuery } from '@tanstack/react-query';
import { petsApi } from './api';

export function usePets(filters = {}) {
  return useQuery({
    queryKey: ['pets', filters],
    queryFn:  () => petsApi.list(filters),
  });
}

export function usePetDetail(id) {
  return useQuery({
    queryKey: ['pets', id],
    queryFn:  () => petsApi.getById(id),
    enabled:  !!id,
  });
}

export function useDashboard() {
  return useQuery({
    queryKey: ['dashboard'],
    queryFn:  petsApi.dashboard,
  });
}
