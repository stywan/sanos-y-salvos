import { Outlet } from 'react-router-dom';

// Los flujos gestionan su propio header (step counter + progress bar)
export function FlowLayout() {
  return <Outlet />;
}
