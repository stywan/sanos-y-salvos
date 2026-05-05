import { createBrowserRouter } from 'react-router-dom';
import { PublicLayout }  from './layouts/PublicLayout';
import { AuthLayout }    from './layouts/AuthLayout';
import { FlowLayout }    from './layouts/FlowLayout';

import { HomePage }       from '../features/home/pages/HomePage';
import { ReportsPage }    from '../features/pets/pages/ReportsPage';
import { PetDetailPage }  from '../features/pets/pages/PetDetailPage';
import { MapPage }        from '../features/map/pages/MapPage';
import { LoginPage }      from '../features/auth/pages/LoginPage';
import { RegisterPage }   from '../features/auth/pages/RegisterPage';
import { ReportLostPage } from '../features/reports/pages/ReportLostPage';
import { ReportFoundPage} from '../features/reports/pages/ReportFoundPage';
import { ProfilePage }    from '../features/auth/pages/ProfilePage';

export const router = createBrowserRouter([
  {
    element: <PublicLayout />,
    children: [
      { path: '/',           element: <HomePage /> },
      { path: '/reports',    element: <ReportsPage /> },
      { path: '/pets/:id',   element: <PetDetailPage /> },
      { path: '/map',        element: <MapPage /> },
      { path: '/profile',    element: <ProfilePage /> },
    ],
  },
  {
    element: <AuthLayout />,
    children: [
      { path: '/login',      element: <LoginPage /> },
      { path: '/register',   element: <RegisterPage /> },
    ],
  },
  {
    element: <FlowLayout />,
    children: [
      { path: '/report/lost',  element: <ReportLostPage /> },
      { path: '/report/found', element: <ReportFoundPage /> },
    ],
  },
]);
