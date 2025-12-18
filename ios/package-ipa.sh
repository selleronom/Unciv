#!/bin/bash
# Simple IPA packager - packages an existing .app into an IPA
# Usage: After running ./gradlew ios:launchIOSDevice, run this script

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="$SCRIPT_DIR/build"
APP_NAME="Unciv"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 Simple IPA Packager"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Find the .app bundle
echo "🔍 Searching for ${APP_NAME}.app..."
APP_PATH=$(find "$BUILD_DIR" -name "${APP_NAME}.app" -type d 2>/dev/null | head -1)

if [ -z "$APP_PATH" ] || [ ! -d "$APP_PATH" ]; then
    echo ""
    echo "❌ Error: ${APP_NAME}.app not found!"
    echo ""
    echo "Please build the app first using:"
    echo "   cd $(dirname "$SCRIPT_DIR")"
    echo "   ./gradlew ios:launchIOSDevice"
    echo ""
    echo "(Press Ctrl+C when the app launches, we just need the .app bundle)"
    echo ""
    exit 1
fi

echo "✅ Found: $APP_PATH"

# Create Payload directory
echo ""
echo "📁 Creating IPA structure..."
PAYLOAD_DIR="$BUILD_DIR/Payload"
rm -rf "$PAYLOAD_DIR"
mkdir -p "$PAYLOAD_DIR"
cp -r "$APP_PATH" "$PAYLOAD_DIR/"

# Create IPA
OUTPUT_DIR="$BUILD_DIR/robovm"
mkdir -p "$OUTPUT_DIR"
IPA_PATH="$OUTPUT_DIR/${APP_NAME}.ipa"

echo ""
echo "🗜️  Creating IPA archive..."
cd "$BUILD_DIR"
rm -f "$IPA_PATH"
zip -qr "$IPA_PATH" Payload

# Cleanup
rm -rf "$PAYLOAD_DIR"

# Show results
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ SUCCESS!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📦 IPA: $IPA_PATH"
IPA_SIZE=$(du -h "$IPA_PATH" | awk '{print $1}')
echo "📊 Size: $IPA_SIZE"
echo ""
echo "Next: Upload to TestFlight"
echo "   fastlane ios beta"
echo ""
