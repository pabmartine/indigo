#!/bin/bash

# Script de actualización - Fase 1: Correcciones Críticas
# Proyecto: Indigo Frontend
# Fecha: 2025-10-30

set -e  # Exit on error

echo "======================================"
echo "  INDIGO - Actualización Fase 1"
echo "  Correcciones Críticas y Seguridad"
echo "======================================"
echo ""

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para preguntar
ask_question() {
    while true; do
        read -p "$1 (s/n): " yn
        case $yn in
            [Ss]* ) return 0;;
            [Nn]* ) return 1;;
            * ) echo "Por favor responde s o n.";;
        esac
    done
}

echo -e "${YELLOW}Este script actualizará las librerías a versiones seguras y compatibles${NC}"
echo ""
echo "Cambios que se aplicarán:"
echo "  1. Angular 16.2.12 → 16.2.14 (parches de seguridad)"
echo "  2. TypeScript 5.1.6 → 5.2.2"
echo "  3. PrimeIcons 6.0.1 → 7.0.0"
echo "  4. Zone.js 0.13.3 → 0.14.10"
echo "  5. Corregir Bootstrap incompatibilidad"
echo "  6. Eliminar jQuery y rxjs-compat"
echo ""

if ! ask_question "¿Deseas continuar?"; then
    echo "Actualización cancelada."
    exit 0
fi

echo ""
echo -e "${GREEN}[1/7] Creando backup de package.json...${NC}"
cp package.json package.json.backup
cp package-lock.json package-lock.json.backup 2>/dev/null || true
echo "✓ Backup creado"

echo ""
echo -e "${GREEN}[2/7] Actualizando Angular a 16.2.14...${NC}"
npm install \
    @angular/animations@^16.2.14 \
    @angular/common@^16.2.14 \
    @angular/compiler@^16.2.14 \
    @angular/core@^16.2.14 \
    @angular/forms@^16.2.14 \
    @angular/platform-browser@^16.2.14 \
    @angular/platform-browser-dynamic@^16.2.14 \
    @angular/router@^16.2.14 \
    @angular/cdk@^16.2.14 \
    @angular/localize@^16.2.14
echo "✓ Angular actualizado"

echo ""
echo -e "${GREEN}[3/7] Actualizando Angular CLI y devDependencies...${NC}"
npm install --save-dev \
    @angular/cli@^16.2.16 \
    @angular-devkit/build-angular@^16.2.16 \
    @angular/compiler-cli@^16.2.14 \
    typescript@~5.2.2
echo "✓ CLI y devDependencies actualizadas"

echo ""
echo -e "${GREEN}[4/7] Actualizando PrimeNG ecosystem...${NC}"
npm install primeicons@^7.0.0
echo "✓ PrimeNG ecosystem actualizado"

echo ""
echo -e "${GREEN}[5/7] Actualizando zone.js...${NC}"
npm install zone.js@~0.14.10
echo "✓ Zone.js actualizado"

echo ""
echo -e "${YELLOW}[6/7] Bootstrap: Eligiendo opción de actualización...${NC}"
echo ""
echo "Opciones para Bootstrap:"
echo "  A) Actualizar a Bootstrap 5 (RECOMENDADO pero requiere cambios en CSS)"
echo "  B) Mantener Bootstrap 4 y bajar ng-bootstrap a v12 (sin cambios necesarios)"
echo "  C) Omitir (hacer manualmente después)"
echo ""

read -p "Selecciona opción (A/B/C): " bootstrap_option

case ${bootstrap_option^^} in
    A)
        echo "Actualizando a Bootstrap 5..."
        npm uninstall bootstrap jquery @ng-bootstrap/ng-bootstrap
        npm install bootstrap@^5.3.3 @ng-bootstrap/ng-bootstrap@^15.1.2 @popperjs/core@^2.11.8
        echo "✓ Bootstrap 5 instalado"
        echo ""
        echo -e "${YELLOW}IMPORTANTE: Revisa UPGRADE_RECOMMENDATIONS.md para los cambios CSS necesarios${NC}"
        ;;
    B)
        echo "Bajando ng-bootstrap a v12 compatible con Bootstrap 4..."
        npm install @ng-bootstrap/ng-bootstrap@12.1.2
        npm uninstall jquery
        echo "✓ ng-bootstrap downgraded a v12"
        ;;
    C)
        echo "Omitiendo Bootstrap, hazlo manualmente más tarde."
        ;;
    *)
        echo "Opción inválida. Omitiendo Bootstrap."
        ;;
esac

echo ""
echo -e "${GREEN}[7/7] Limpiando dependencias obsoletas...${NC}"
npm uninstall rxjs-compat 2>/dev/null || echo "rxjs-compat ya no está instalado"
echo "✓ Limpieza completada"

echo ""
echo -e "${GREEN}======================================"
echo "  ✓ Actualización Fase 1 Completada"
echo "======================================${NC}"
echo ""
echo "Próximos pasos:"
echo "  1. Ejecuta: npm install"
echo "  2. Ejecuta: ng build"
echo "  3. Ejecuta: ng serve"
echo "  4. Prueba todas las funcionalidades"
echo "  5. Si todo funciona, elimina los backups:"
echo "     rm package.json.backup package-lock.json.backup"
echo ""
echo "Si algo sale mal, restaura el backup:"
echo "  mv package.json.backup package.json"
echo "  mv package-lock.json.backup package-lock.json"
echo "  npm install"
echo ""
