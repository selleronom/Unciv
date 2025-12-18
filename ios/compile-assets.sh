#!/bin/bash
set -e

echo "Compiling Assets.xcassets to Assets.car..."

cd "$(dirname "$0")"

# Create temporary output directory
mkdir -p build/compiled-assets

# Compile the asset catalog
actool \
  --compile build/compiled-assets \
  --platform iphoneos \
  --minimum-deployment-target 12.0 \
  --app-icon AppIcon \
  --output-partial-info-plist build/compiled-assets/partial-Info.plist \
  Assets.xcassets

echo "✅ Asset catalog compiled successfully"
echo "Output: ios/build/compiled-assets/Assets.car"
