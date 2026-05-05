# ============================================================
#  Sanos y Salvos — comandos rápidos de desarrollo
#  Uso: make <target>
#  Ejemplo: make up | make logs s=bff | make restart s=bff
# ============================================================

.DEFAULT_GOAL := help
COMPOSE        := docker compose

# ── Colores ─────────────────────────────────────────────────
GREEN  := \033[0;32m
YELLOW := \033[0;33m
CYAN   := \033[0;36m
RESET  := \033[0m

# ─────────────────────────────────────────────────────────────
# Ciclo de vida principal
# ─────────────────────────────────────────────────────────────

.PHONY: up
up: ## Inicia todos los servicios en segundo plano (construye si no existen las imágenes)
	@echo "$(GREEN)▶ Iniciando Sanos y Salvos...$(RESET)"
	$(COMPOSE) up -d
	@echo "$(GREEN)✔ Sistema arriba. MinIO crea el bucket automáticamente. Usa 'make status' para ver el estado.$(RESET)"

.PHONY: down
down: ## Detiene todos los servicios (conserva los datos)
	@echo "$(YELLOW)■ Deteniendo servicios...$(RESET)"
	$(COMPOSE) down
	@echo "$(YELLOW)✔ Servicios detenidos.$(RESET)"

.PHONY: restart
restart: ## Reinicia UN servicio: make restart s=bff
	@[ "$(s)" ] || (echo "$(YELLOW)Uso: make restart s=<servicio>$(RESET)" && exit 1)
	@echo "$(CYAN)↺ Reiniciando $(s)...$(RESET)"
	$(COMPOSE) restart $(s)

.PHONY: clean
clean: ## ⚠️  Detiene todo y BORRA los volúmenes (base de datos y archivos)
	@echo "$(YELLOW)⚠ Esto eliminará la base de datos y los archivos de MinIO.$(RESET)"
	@read -p "¿Continuar? [s/N] " ans && [ "$$ans" = "s" ] || (echo "Cancelado." && exit 1)
	$(COMPOSE) down -v
	@echo "$(YELLOW)✔ Todo eliminado.$(RESET)"

# ─────────────────────────────────────────────────────────────
# Construcción de imágenes
# ─────────────────────────────────────────────────────────────

.PHONY: build
build: ## Construye todas las imágenes Docker (usa caché)
	@echo "$(CYAN)🔨 Construyendo imágenes...$(RESET)"
	$(COMPOSE) build
	@echo "$(CYAN)✔ Imágenes listas.$(RESET)"

.PHONY: rebuild
rebuild: ## Reconstruye TODAS las imágenes sin caché (más lento, más limpio)
	@echo "$(CYAN)🔨 Reconstruyendo sin caché...$(RESET)"
	$(COMPOSE) build --no-cache
	@echo "$(CYAN)✔ Imágenes reconstruidas.$(RESET)"

.PHONY: rebuild-svc
rebuild-svc: ## Reconstruye y reinicia UN servicio: make rebuild-svc s=bff
	@[ "$(s)" ] || (echo "$(YELLOW)Uso: make rebuild-svc s=<servicio>$(RESET)" && exit 1)
	@echo "$(CYAN)🔨 Reconstruyendo $(s)...$(RESET)"
	$(COMPOSE) up -d --build $(s)

# ─────────────────────────────────────────────────────────────
# Observabilidad
# ─────────────────────────────────────────────────────────────

.PHONY: status
status: ## Muestra el estado de todos los contenedores
	$(COMPOSE) ps

.PHONY: logs
logs: ## Sigue los logs de todos los servicios (Ctrl+C para salir)
	$(COMPOSE) logs -f

.PHONY: logs-svc
logs-svc: ## Sigue los logs de UN servicio: make logs-svc s=bff
	@[ "$(s)" ] || (echo "$(YELLOW)Uso: make logs-svc s=<servicio>$(RESET)" && exit 1)
	$(COMPOSE) logs -f $(s)

.PHONY: health
health: ## Consulta el actuator/health de cada microservicio
	@echo "$(CYAN)── Healthchecks ──────────────────────────$(RESET)"
	@curl -sf http://localhost:8084/actuator/health | python3 -c "import sys,json; d=json.load(sys.stdin); print('ms-user-auth    :', d['status'])" 2>/dev/null || echo "ms-user-auth    : ✗ no responde"
	@curl -sf http://localhost:8081/actuator/health | python3 -c "import sys,json; d=json.load(sys.stdin); print('ms-pet-mgmt     :', d['status'])" 2>/dev/null || echo "ms-pet-mgmt     : ✗ no responde"
	@curl -sf http://localhost:8082/actuator/health | python3 -c "import sys,json; d=json.load(sys.stdin); print('ms-geolocation  :', d['status'])" 2>/dev/null || echo "ms-geolocation  : ✗ no responde"
	@curl -sf http://localhost:8083/actuator/health | python3 -c "import sys,json; d=json.load(sys.stdin); print('ms-matching     :', d['status'])" 2>/dev/null || echo "ms-matching     : ✗ no responde"
	@curl -sf http://localhost:8085/actuator/health | python3 -c "import sys,json; d=json.load(sys.stdin); print('ms-notification :', d['status'])" 2>/dev/null || echo "ms-notification : ✗ no responde"
	@curl -sf http://localhost:8086/actuator/health | python3 -c "import sys,json; d=json.load(sys.stdin); print('bff             :', d['status'])" 2>/dev/null || echo "bff             : ✗ no responde"
	@curl -sf http://localhost:8080/actuator/health | python3 -c "import sys,json; d=json.load(sys.stdin); print('api-gateway     :', d['status'])" 2>/dev/null || echo "api-gateway     : ✗ no responde"
	@echo "$(CYAN)── Frontend ──────────────────────────────$(RESET)"
	@curl -sf -o /dev/null -w "frontend (nginx) : HTTP %{http_code}\n" http://localhost:5173/ 2>/dev/null || echo "frontend        : ✗ no responde"

# ─────────────────────────────────────────────────────────────
# Tests locales (fuera de Docker)
# ─────────────────────────────────────────────────────────────

.PHONY: test-all
test-all: ## Corre los tests de todos los microservicios con Maven
	@echo "$(CYAN)🧪 Ejecutando tests...$(RESET)"
	@for svc in ms-user-auth ms-pet-management ms-geolocation ms-matching-engine ms-notification bff api-gateway; do \
		echo ""; \
		echo "$(CYAN)── $$svc ──$(RESET)"; \
		(cd $$svc && ./mvnw test -q 2>&1 | tail -5) || echo "$(YELLOW)⚠ Falló en $$svc$(RESET)"; \
	done
	@echo ""
	@echo "$(GREEN)✔ Tests completados.$(RESET)"

# ─────────────────────────────────────────────────────────────
# Ayuda
# ─────────────────────────────────────────────────────────────

.PHONY: help
help: ## Muestra esta ayuda
	@echo ""
	@echo "  $(GREEN)Sanos y Salvos$(RESET) — comandos disponibles"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "  $(CYAN)%-16s$(RESET) %s\n", $$1, $$2}'
	@echo ""
	@echo "  $(YELLOW)Puertos:$(RESET)"
	@echo "    Frontend   → http://localhost:5173"
	@echo "    API Gateway→ http://localhost:8080"
	@echo "    BFF        → http://localhost:8086"
	@echo "    MinIO UI   → http://localhost:9001"
	@echo ""
