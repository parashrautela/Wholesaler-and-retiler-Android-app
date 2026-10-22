#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ICON_SRC="$DIR/../wholesaler ios/JewelIndia/Resources/Assets.xcassets/AppIcon.appiconset/AppIcon-1024.png"

if [ ! -f "$ICON_SRC" ]; then
    echo "Error: AppIcon-1024.png not found at $ICON_SRC"
    exit 1
fi

TMP_DIR=$(mktemp -d)
trap 'rm -rf "$TMP_DIR"' EXIT
mkdir -p "$DIR/app/src/main/res/drawable-nodpi"

# minSdk is 26; supported devices use the adaptive icons in mipmap-anydpi.
# Foreground 432px
cp "$ICON_SRC" "$TMP_DIR/padded.png"
sips --padToHeightWidth 1552 1552 --padColor FEFEFE "$TMP_DIR/padded.png" > /dev/null
sips -z 432 432 "$TMP_DIR/padded.png" --out "$DIR/app/src/main/res/drawable-nodpi/ic_launcher_foreground.png" > /dev/null

echo "Launcher icons generated successfully."
