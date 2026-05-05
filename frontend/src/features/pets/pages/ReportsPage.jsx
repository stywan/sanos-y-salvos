import { useState } from 'react';
import { PetCard } from '../../../shared/ui/PetCard';
import { Icon } from '../../../shared/ui/Icon';
import { usePets } from '../hooks';
import s from './ReportsPage.module.css';

function SkeletonGrid() {
  return (
    <div className={s.petsGrid}>
      {Array.from({ length: 8 }).map((_, i) => (
        <div key={i} className={s.skeleton} />
      ))}
    </div>
  );
}

export function ReportsPage() {
  const [search,       setSearch]       = useState('');
  const [statusFilter, setStatusFilter] = useState('Todos');

  const statusParam =
    statusFilter === 'Perdidos'   ? 'PERDIDO'    :
    statusFilter === 'Encontrados'? 'ENCONTRADO' :
    statusFilter === 'Reunidos'   ? 'REUNIDO'    : undefined;
  const { data, isLoading, isError } = usePets({ status: statusParam, search: search || undefined });

  // El BFF puede devolver Page<> de Spring o un array directo
  const pets = Array.isArray(data) ? data : (data?.content ?? []);

  const filtered = pets.filter(p => {
    if (!search) return true;
    return (
      p.name?.toLowerCase().includes(search.toLowerCase()) ||
      p.location?.toLowerCase().includes(search.toLowerCase())
    );
  });

  return (
    <div className={s.page}>
      <div className={s.header}>
        <div className={s.headerInner}>
          <div className={s.eyebrow}>Directorio</div>
          <h1 className={s.title}>Todos los reportes</h1>
          <p className={s.sub}>Nuestra comunidad trabaja unida para que cada mascota regrese a casa.</p>

          <div className={s.filters}>
            <div className={s.searchBox}>
              <Icon.Search size={15} color="var(--text-light)" />
              <input
                value={search}
                onChange={e => setSearch(e.target.value)}
                placeholder="Nombre o sector..."
                className={s.searchInput}
              />
            </div>
            <div className={s.pills}>
              {['Todos', 'Perdidos', 'Encontrados', 'Reunidos'].map(f => (
                <button key={f} onClick={() => setStatusFilter(f)}
                  className={`${s.pill} ${statusFilter === f ? s.pillActive : ''}`}>{f}</button>
              ))}
            </div>
            {!isLoading && (
              <span className={s.count}><strong>{filtered.length}</strong> resultados</span>
            )}
          </div>
        </div>
      </div>

      <div className={s.grid}>
        {isLoading && <SkeletonGrid />}

        {isError && (
          <div className={s.empty}>
            <div className={s.emptyIcon}>⚠️</div>
            <h3>Error al cargar reportes</h3>
            <p>Revisa tu conexión o intenta más tarde.</p>
          </div>
        )}

        {!isLoading && !isError && filtered.length === 0 && (
          <div className={s.empty}>
            <div className={s.emptyIcon}>🔍</div>
            <h3>Sin resultados</h3>
            <p>Prueba con otros filtros.</p>
          </div>
        )}

        {!isLoading && !isError && filtered.length > 0 && (
          <div className={s.petsGrid}>
            {filtered.map(pet => <PetCard key={pet.id} pet={pet} />)}
          </div>
        )}
      </div>
    </div>
  );
}
