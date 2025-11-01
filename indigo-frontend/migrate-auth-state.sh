#!/bin/bash

# Script para migrar archivos a AuthStateService
# Uso: ./migrate-auth-state.sh

FILES=(
  "src/app/pages/profile/profile.component.ts"
  "src/app/pages/author/author.component.ts"
  "src/app/pages/settings/settings.component.ts"
  "src/app/pages/search/search.component.ts"
  "src/app/app.component.ts"
  "src/app/layouts/sidebar/sidebar.component.ts"
  "src/app/layouts/header/header.component.ts"
)

for file in "${FILES[@]}"; do
  if [ -f "$file" ]; then
    echo "Processing: $file"

    # Check if already has AuthStateService import
    if grep -q "AuthStateService" "$file"; then
      echo "  ✓ Already migrated"
      continue
    fi

    # Add imports after last import statement
    sed -i "/^import.*from.*;$/a import { AuthStateService } from 'src/app/services/auth-state.service';\nimport { User } from 'src/app/domain/user';" "$file" | head -1

    # Replace user = JSON.parse(sessionStorage.user)
    sed -i 's/user = JSON\.parse(sessionStorage\.user)/user: User/g' "$file"
    sed -i 's/private user = JSON\.parse(sessionStorage\.user)/private user: User/g' "$file"

    echo "  ✓ Imports added and user declaration updated"
    echo "  ⚠️  Manual: Add authState to constructor and initialize user"
  else
    echo "  ✗ File not found: $file"
  fi
done

echo ""
echo "Migration script completed!"
echo "⚠️  Remember to manually:"
echo "  1. Add 'private authState: AuthStateService' to each constructor"
echo "  2. Initialize user in constructor: this.user = this.authState.getCurrentUser() || ..."
echo "  3. Replace any remaining 'const user = JSON.parse(sessionStorage.user)' with 'this.user'"
