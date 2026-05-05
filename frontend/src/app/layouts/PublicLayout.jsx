import { Outlet } from 'react-router-dom';
import { Nav } from '../../shared/components/Nav';
import { Footer } from '../../shared/components/Footer';

export function PublicLayout() {
  return (
    <>
      <Nav />
      <main>
        <Outlet />
      </main>
      <Footer />
    </>
  );
}
