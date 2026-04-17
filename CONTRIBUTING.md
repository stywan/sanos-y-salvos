# Guía de contribución — Sanos y Salvos

## Estrategia de ramas (GitHub Flow simplificado)

```
main          ← producción / entrega final (protegida, requiere PR)
develop       ← integración de features (base para PRs)
feature/xxx   ← desarrollo de una funcionalidad
fix/xxx       ← corrección de bugs
```

### Flujo de trabajo diario

```bash
# 1. Siempre partir desde develop actualizado
git checkout develop
git pull origin develop

# 2. Crear rama para la tarea
git checkout -b feature/pet-management-crud

# 3. Desarrollar, hacer commits frecuentes
git add <archivos específicos>
git commit -m "feat(pet): agregar endpoint POST /api/pets"

# 4. Subir la rama
git push origin feature/pet-management-crud

# 5. Abrir Pull Request hacia develop en GitHub
# 6. Esperar revisión de al menos 1 compañero
# 7. Mergear y eliminar la rama
```

---

## Convención de commits

Seguimos **Conventional Commits**:

```
<tipo>(<scope>): <descripción corta>
```

| Tipo | Cuándo usarlo |
|------|--------------|
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de bug |
| `docs` | Solo documentación |
| `refactor` | Refactorización sin cambio funcional |
| `test` | Agregar o corregir tests |
| `chore` | Configuración, dependencias |

**Ejemplos:**
```
feat(pet): agregar endpoint de búsqueda por zona
fix(auth): corregir validación de token expirado
test(matching): agregar tests unitarios al SimilarityCalculator
docs(readme): actualizar instrucciones de instalación
```

---

## Nombrado de ramas

```
feature/ms-user-auth-jwt
feature/ms-pet-crud
feature/frontend-login-form
fix/gateway-routing-404
docs/diagrama-arquitectura
```

---

## Pull Requests

- Título claro con el scope: `feat(pet): CRUD completo de mascotas`
- Descripción con qué hace, qué cambia y cómo probarlo
- Mínimo **1 aprobación** antes de mergear a `develop`
- El pipeline de CI debe pasar (tests + cobertura ≥ 60%)
- Eliminar la rama después del merge

---

## Ramas protegidas

| Rama | Regla |
|------|-------|
| `main` | Solo merge desde `develop`, requiere PR + 1 aprobación |
| `develop` | Requiere PR + 1 aprobación, CI debe pasar |

---

## Configurar el repo localmente

```bash
git clone https://github.com/stywan/sanos-y-salvos.git
cd sanos-y-salvos
cp .env.example .env      # completar con tus valores locales
docker compose up postgres -d
```
