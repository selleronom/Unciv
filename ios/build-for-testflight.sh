#!/bin/bash
set -e

echo "=========================================="
echo "Building Unciv for TestFlight/App Store"
echo "=========================================="
echo ""

cd "$(dirname "$0")/.."

echo "Step 1: Clean previous builds..."
./gradlew clean

echo ""
echo "Step 2: Compiling asset catalog..."
./ios/compile-assets.sh

echo ""
echo "Step 3: Building iOS app with distribution certificate..."
echo "(This will fail to install on device - that's expected for App Store builds)"
./gradlew ios:launchIOSDevice || true

echo ""
echo "Step 4: Checking if .app was created..."
if [ ! -d "ios/build/robovm.tmp/Unciv.app" ]; then
    echo "ERROR: Unciv.app was not created!"
    exit 1
fi

echo "✅ Unciv.app created successfully"
echo ""
echo "Step 5: Verifying code signature..."
codesign -dvv ios/build/robovm.tmp/Unciv.app 2>&1 | grep "Authority" | head -3

echo ""
echo "Step 6: Packaging IPA..."
./ios/package-ipa.sh

echo ""
echo "=========================================="
echo "✅ IPA created successfully!"
echo "=========================================="
echo ""
echo "Location: ios/build/robovm/Unciv.ipa"
echo ""
echo "To upload to TestFlight:"
echo "  fastlane ios beta"
echo ""
echo "Or manually upload to App Store Connect:"
echo "  open https://appstoreconnect.apple.com"
echo ""
