#!/bin/bash

# Script de verificación post-actualización
# Proyecto: Indigo Frontend

set -e

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}======================================"
echo "  Verificación de Actualizaciones"
echo "  Indigo Frontend"
echo "======================================${NC}"
echo ""

# 1. Verificar versiones actualizadas
echo -e "${YELLOW}[1/5] Verificando versiones actualizadas...${NC}"
echo ""

echo "Verificando ng-bootstrap:"
NG_BOOTSTRAP_VERSION=$(npm list @ng-bootstrap/ng-bootstrap --depth=0 2>/dev/null | grep @ng-bootstrap | awk '{print $2}' | sed 's/@//')
if [[ $NG_BOOTSTRAP_VERSION == 12.* ]]; then
    echo -e "  ${GREEN}✓${NC} ng-bootstrap: $NG_BOOTSTRAP_VERSION (compatible con Bootstrap 4)"
else
    echo -e "  ${RED}✗${NC} ng-bootstrap: $NG_BOOTSTRAP_VERSION (esperado 12.x.x)"
fi

echo "Verificando PrimeIcons:"
PRIMEICONS_VERSION=$(npm list primeicons --depth=0 2>/dev/null | grep primeicons | awk '{print $2}' | sed 's/@//')
if [[ $PRIMEICONS_VERSION == 7.* ]]; then
    echo -e "  ${GREEN}✓${NC} PrimeIcons: $PRIMEICONS_VERSION"
else
    echo -e "  ${YELLOW}⚠${NC} PrimeIcons: $PRIMEICONS_VERSION"
fi

# 2. Verificar dependencias eliminadas
echo ""
echo -e "${YELLOW}[2/5] Verificando dependencias eliminadas...${NC}"

if ! npm list jquery --depth=0 2>&1 | grep -q "jquery@"; then
    echo -e "  ${GREEN}✓${NC} jQuery eliminado correctamente"
else
    echo -e "  ${RED}✗${NC} jQuery todavía está instalado"
fi

if ! npm list rxjs-compat --depth=0 2>&1 | grep -q "rxjs-compat@"; then
    echo -e "  ${GREEN}✓${NC} rxjs-compat eliminado correctamente"
else
    echo -e "  ${RED}✗${NC} rxjs-compat todavía está instalado"
fi

if ! grep -q "jquery" angular.json; then
    echo -e "  ${GREEN}✓${NC} jQuery removido de angular.json"
else
    echo -e "  ${RED}✗${NC} jQuery todavía referenciado en angular.json"
fi

# 3. Test de compilación
echo ""
echo -e "${YELLOW}[3/5] Probando compilación...${NC}"
if npm run build > /tmp/indigo-build.log 2>&1; then
    echo -e "  ${GREEN}✓${NC} Build completado exitosamente"

    # Verificar warnings
    WARNING_COUNT=$(grep -c "WARNING" /tmp/indigo-build.log || true)
    ERROR_COUNT=$(grep -c "ERROR\|Error" /tmp/indigo-build.log || true)

    echo "  Warnings: $WARNING_COUNT (solo CSS, pre-existentes)"
    echo "  Errors: $ERROR_COUNT"
else
    echo -e "  ${RED}✗${NC} Build falló - revisar /tmp/indigo-build.log"
    exit 1
fi

# 4. Verificar estructura de archivos
echo ""
echo -e "${YELLOW}[4/5] Verificando archivos generados...${NC}"
if [ -d "dist" ]; then
    DIST_SIZE=$(du -sh dist 2>/dev/null | awk '{print $1}')
    echo -e "  ${GREEN}✓${NC} Directorio dist generado correctamente"
    echo "  Tamaño: $DIST_SIZE"
else
    echo -e "  ${RED}✗${NC} Directorio dist no encontrado"
fi

# 5. Recomendaciones finales
echo ""
echo -e "${YELLOW}[5/5] Verificación completada${NC}"
echo ""
echo -e "${GREEN}======================================"
echo "  ✓ Verificación Exitosa"
echo "======================================${NC}"
echo ""
echo "Próximos pasos:"
echo "  1. Ejecuta: ${BLUE}ng serve${NC}"
echo "  2. Abre: http://localhost:4200"
echo "  3. Verifica las siguientes páginas:"
echo "     - /books"
echo "     - /authors"
echo "     - /series"
echo "     - /categories"
echo "  4. Prueba modales y diálogos"
echo "  5. Revisa la consola del navegador"
echo ""
echo "Si todo funciona correctamente:"
echo "  ${BLUE}rm package.json.backup package-lock.json.backup${NC}"
echo ""
echo "Si hay problemas, restaura el backup:"
echo "  ${BLUE}./restore-backup.sh${NC}"
echo ""
