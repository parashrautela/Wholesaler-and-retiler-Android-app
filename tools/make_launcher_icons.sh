#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ICON_SRC="$DIR/../wholesaler ios/JewelIndia/Resources/Assets.xcassets/AppIcon.appiconset/AppIcon-1024.png"

if [ ! -f "$ICON_SRC" ]; then
    echo "Error: AppIcon-1024.png not found at $ICON_SRC"
    exit 1
fi

TMP_DIR=$(mktemp -d)

mkdir -p "$DIR/app/src/main/res/mipmap-mdpi"
mkdir -p "$DIR/app/src/main/res/mipmap-hdpi"
mkdir -p "$DIR/app/src/main/res/mipmap-xhdpi"
mkdir -p "$DIR/app/src/main/res/mipmap-xxhdpi"
mkdir -p "$DIR/app/src/main/res/mipmap-xxxhdpi"
mkdir -p "$DIR/app/src/main/res/mipmap-anydpi-v26"
mkdir -p "$DIR/app/src/main/res/drawable-nodpi"

# Legacy mipmaps
sips -z 48 48 "$ICON_SRC" --out "$DIR/app/src/main/res/mipmap-mdpi/ic_launcher.png" > /dev/null
cp "$DIR/app/src/main/res/mipmap-mdpi/ic_launcher.png" "$DIR/app/src/main/res/mipmap-mdpi/ic_launcher_round.png"

sips -z 72 72 "$ICON_SRC" --out "$DIR/app/src/main/res/mipmap-hdpi/ic_launcher.png" > /dev/null
cp "$DIR/app/src/main/res/mipmap-hdpi/ic_launcher.png" "$DIR/app/src/main/res/mipmap-hdpi/ic_launcher_round.png"

sips -z 96 96 "$ICON_SRC" --out "$DIR/app/src/main/res/mipmap-xhdpi/ic_launcher.png" > /dev/null
cp "$DIR/app/src/main/res/mipmap-xhdpi/ic_launcher.png" "$DIR/app/src/main/res/mipmap-xhdpi/ic_launcher_round.png"

sips -z 144 144 "$ICON_SRC" --out "$DIR/app/src/main/res/mipmap-xxhdpi/ic_launcher.png" > /dev/null
cp "$DIR/app/src/main/res/mipmap-xxhdpi/ic_launcher.png" "$DIR/app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png"

sips -z 192 192 "$ICON_SRC" --out "$DIR/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" > /dev/null
cp "$DIR/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" "$DIR/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png"

# Foreground 432px
cp "$ICON_SRC" "$TMP_DIR/padded.png"
sips --padToHeightWidth 1552 1552 --padColor FEFEFE "$TMP_DIR/padded.png" > /dev/null
sips -z 432 432 "$TMP_DIR/padded.png" --out "$DIR/app/src/main/res/drawable-nodpi/ic_launcher_foreground.png" > /dev/null

rm -rf "$TMP_DIR"
echo "Launcher icons generated successfully."
