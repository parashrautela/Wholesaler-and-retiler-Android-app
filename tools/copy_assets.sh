#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IOS_FONTS="$DIR/../wholesaler ios/JewelIndia/Resources/Fonts"
IOS_ASSETS="$DIR/../wholesaler ios/JewelIndia/Resources/Assets.xcassets"

FONT_DEST="$DIR/app/src/main/res/font"
SVG_DEST="$DIR/app/src/main/assets/svg"
DRAWABLE_DEST="$DIR/app/src/main/res/drawable-nodpi"
DEBUG_DRAWABLE_DEST="$DIR/app/src/debug/res/drawable-nodpi"

mkdir -p "$FONT_DEST"
mkdir -p "$SVG_DEST"
mkdir -p "$DRAWABLE_DEST"
mkdir -p "$DEBUG_DRAWABLE_DEST"

echo "Copying fonts..."
python3 - <<EOF
import os, shutil

font_src = "$IOS_FONTS"
font_dest = "$FONT_DEST"

mapping = {
    "Cirka-Bold.ttf": "cirka_bold.ttf",
    "Cirka-Light.ttf": "cirka_light.ttf",
    "Cirka-Regular.ttf": "cirka_regular.ttf",
    "EmployeeMatter-Bold.ttf": "employee_matter_bold.ttf",
    "EmployeeMatter-Medium.ttf": "employee_matter_medium.ttf",
    "EmployeeMatter-Regular.ttf": "employee_matter_regular.ttf",
    "EmployeeSeasonMix-Regular.ttf": "employee_season_mix_regular.ttf",
    "GildaDisplay-Regular.ttf": "gilda_display_regular.ttf",
    "Gilroy-Bold.ttf": "gilroy_bold.ttf",
    "Gilroy-Medium.ttf": "gilroy_medium.ttf",
    "Gilroy-Regular.ttf": "gilroy_regular.ttf",
    "Gilroy-SemiBold.ttf": "gilroy_semibold.ttf",
    "Manrope-Bold.ttf": "manrope_bold.ttf",
    "Manrope-ExtraBold.ttf": "manrope_extrabold.ttf",
    "Manrope-ExtraLight.ttf": "manrope_extralight.ttf",
    "Manrope-Light.ttf": "manrope_light.ttf",
    "Manrope-Medium.ttf": "manrope_medium.ttf",
    "Manrope-Regular.ttf": "manrope_regular.ttf",
    "Manrope-SemiBold.ttf": "manrope_semibold.ttf",
    "Satoshi-Black.ttf": "satoshi_black.ttf",
    "Satoshi-Bold.ttf": "satoshi_bold.ttf",
    "Satoshi-Light.ttf": "satoshi_light.ttf",
    "Satoshi-Medium.ttf": "satoshi_medium.ttf",
    "Satoshi-Regular.ttf": "satoshi_regular.ttf",
}

for src_name, dst_name in mapping.items():
    src_file = os.path.join(font_src, src_name)
    if not os.path.exists(src_file):
        raise FileNotFoundError(f"Missing font: {src_file}")
    shutil.copy2(src_file, os.path.join(font_dest, dst_name))

print(f"Copied {len(mapping)} fonts successfully.")
EOF

echo "Copying xcassets..."
python3 - <<EOF
import os, json, shutil, re

assets_src = "$IOS_ASSETS"
svg_dest = "$SVG_DEST"
drawable_dest = "$DRAWABLE_DEST"
debug_drawable_dest = "$DEBUG_DRAWABLE_DEST"

def camel_to_snake(name):
    s = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
    return re.sub('([a-z0-9])([A-Z])', r'\1_\2', s).lower()

# Artwork used by the app and debug previews.
png_mapping = {
    "AuthPanelImage": "auth_panel_image.jpg",
    "CatBangles": "cat_bangles.png",
    "CatChains": "cat_chains.png",
    "CatEarrings": "cat_earrings.png",
    "CatHaram": "cat_haram.png",
    "CatMangalsutras": "cat_mangalsutras.png",
    "CatNecklace": "cat_necklace.png",
    "CatNosepins": "cat_nosepins.png",
    "CatPendants": "cat_pendants.png",
    "CatRings": "cat_rings.png",
    "ChamakStudioArtwork": "chamak_studio_artwork.png",
    "GoogleG": "google_g.png",
    "InviteCardArtwork": "invite_card_artwork.png",
    "InviteRewardArtwork": "invite_reward_artwork.png",
    "UploadDesignCoin": "upload_design_coin.png",
    "UploadDesignLeftHand": "upload_design_left_hand.png",
    "UploadDesignRightHand": "upload_design_right_hand.png",
}

svg_expected = [
    "ChamakStudioArrow", "EmpNavCatalogue", "EmpNavHome", "EmpNavOrders", "EmpNavPerson", "EmpNavQueries",
    "InviteChecked", "InviteConnector", "InviteCopy", "InviteMinus", "InvitePlus", "InviteStepDivider",
    "InviteUserPlus", "JewelLogo", "NavAddRetailer", "NavCatalogue", "NavChat", "NavHome", "NavOrders",
    "NavUpload", "UploadDesignIcon", "UploadDesignWash"
]

svg_copied = 0
png_copied = 0

for item in sorted(os.listdir(assets_src)):
    if not item.endswith('.imageset'):
        continue
    set_name = item[:-9]
    item_path = os.path.join(assets_src, item)
    contents_path = os.path.join(item_path, "Contents.json")
    if not os.path.exists(contents_path):
        continue
    with open(contents_path) as f:
        meta = json.load(f)
    images = [img for img in meta.get("images", []) if img.get("filename")]
    if not images:
        continue
    
    # Check if SVG
    svg_file = next((img["filename"] for img in images if img["filename"].endswith(".svg")), None)
    if svg_file:
        src = os.path.join(item_path, svg_file)
        dst = os.path.join(svg_dest, f"{set_name}.svg")
        shutil.copy2(src, dst)
        svg_copied += 1
    else:
        # Pick largest or map directly
        dst_name = png_mapping.get(set_name)
        if dst_name:
            # Pick 3x if present, otherwise 2x, 1x, or the single file
            selected = None
            for scale in ["3x", "2x", "1x"]:
                selected = next((img["filename"] for img in images if img.get("scale") == scale), None)
                if selected:
                    break
            if not selected:
                selected = images[-1]["filename"]
            src = os.path.join(item_path, selected)
            # Category artwork is only used by debug preview fixtures.
            dest = debug_drawable_dest if dst_name.startswith("cat_") else drawable_dest
            dst = os.path.join(dest, dst_name)
            shutil.copy2(src, dst)
            png_copied += 1

print(f"Copied {svg_copied} SVGs and {png_copied} PNG/JPGs.")
if svg_copied != len(svg_expected):
    raise ValueError(f"Expected {len(svg_expected)} SVGs, got {svg_copied}")
if png_copied != len(png_mapping):
    raise ValueError(f"Expected {len(png_mapping)} PNGs, got {png_copied}")
EOF

echo "All assets copied successfully."
