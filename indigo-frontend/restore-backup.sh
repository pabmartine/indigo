#!/bin/bash

# Script de restauración desde backup
# Proyecto: Indigo Frontend

set -e

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}======================================"
echo "  Restauración desde Backup"
echo "  Indigo Frontend"
echo "======================================${NC}"
echo ""

# Verificar si existen los backups
if [ ! -f "package.json.backup" ] || [ ! -f "package-lock.json.backup" ]; then
    echo -e "${RED}Error: No se encontraron los archivos de backup${NC}"
    echo "Asegúrate de estar en el directorio correcto del proyecto"
    exit 1
fi

echo "Se encontraron los siguientes backups:"
echo "  - package.json.backup"
echo "  - package-lock.json.backup"
echo ""

read -p "¿Deseas restaurar desde el backup? (s/n): " confirm
if [[ ! $confirm =~ ^[Ss]$ ]]; then
    echo "Restauración cancelada"
    exit 0
fi

echo ""
echo -e "${GREEN}[1/4] Restaurando package.json...${NC}"
mv package.json package.json.failed 2>/dev/null || true
cp package.json.backup package.json
echo "✓ package.json restaurado"

echo ""
echo -e "${GREEN}[2/4] Restaurando package-lock.json...${NC}"
mv package-lock.json package-lock.json.failed 2>/dev/null || true
cp package-lock.json.backup package-lock.json
echo "✓ package-lock.json restaurado"

echo ""
echo -e "${GREEN}[3/4] Limpiando node_modules...${NC}"
rm -rf node_modules
echo "✓ node_modules eliminado"

echo ""
echo -e "${GREEN}[4/4] Reinstalando dependencias...${NC}"
npm install --legacy-peer-deps
echo "✓ Dependencias instaladas"

echo ""
echo -e "${GREEN}======================================"
echo "  ✓ Restauración Completada"
echo "======================================${NC}"
echo ""
echo "El proyecto ha sido restaurado al estado anterior."
echo ""
echo "Archivos con problemas guardados como:"
echo "  - package.json.failed"
echo "  - package-lock.json.failed"
echo ""
echo "Próximos pasos:"
echo "  1. Ejecuta: npm run build"
echo "  2. Si funciona, puedes eliminar los archivos .failed"
echo ""
