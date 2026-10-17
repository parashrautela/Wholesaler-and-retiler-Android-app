#!/usr/bin/env python3
"""
Compare Android debug peek screens with iOS reference screenshots.
Usage:
    python3 tools/compare_screens.py --id <peekId> [--serial <adbSerial>]
    python3 tools/compare_screens.py --all [--serial <adbSerial>]
    python3 tools/compare_screens.py --generate-only  # only stitch existing screenshots
"""

import argparse
import os
import subprocess
import sys
import time
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ALL_PEEK_IDS = [
    # Shell & Home
    "wholesaler",
    "upload-card",
    "chamak-card",
    "home-creations",
    "home-lowbalance",
    "report-teaser",
    "report",
    # Catalogue
    "catalogue-cards",
    "productdetail",
    # Add / Edit / Upload
    "addproduct",
    "addproduct-sheet",
    # Orders
    "wholesaler-orders",
    "order-detail-new",
    "order-detail-active",
    "reject-order",
    # Chat
    "chats-wholesaler",
    "chat-thread",
    # Profile & Misc
    "profile-wholesaler",
    "invite-card",
    "invite-gift",
    "invite-guide",
    "manufacturing-offer",
    # Chamak
    "chamak-hub",
    "chamak-hub-empty",
    "chamak-picker",
    "chamak-picker-sample",
    "chamak-slider-retry",
    "chamak-result",
    "chamak-result-attempts",
    "chamak-viewer",
    "setcreation-picker",
    "setcreation-styling",
    # Credits
    "daily-credits-sheet",
    "daily-credits",
    "nocredits",
    # Onboarding & Auth
    "onboard",
    "onboard2",
    "onboard3",
    "submitted",
    "onboarding-fee",
    "doors",
    "entry-wholesaler",
    "signin",
    "otp",
    "setpassword"
]

def find_adb():
    candidate = "/Users/parashrautela/Library/Android/sdk/platform-tools/adb"
    if os.path.exists(candidate):
        return candidate
    return "adb"

def capture_screen(adb_path, serial, peek_id, out_png, wait_sec=3.5):
    cmd_prefix = [adb_path]
    if serial:
        cmd_prefix.extend(["-s", serial])

    # Launch intent
    start_cmd = cmd_prefix + [
        "shell", "am", "start",
        "-n", "com.jewelindia.app/.MainActivity",
        "--es", "jewelPeek", peek_id
    ]
    subprocess.run(start_cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    time.sleep(wait_sec)

    # Screencap
    screencap_cmd = cmd_prefix + ["exec-out", "screencap", "-p"]
    res = subprocess.run(screencap_cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if res.returncode != 0 or len(res.stdout) < 1000:
        print(f"[-] Screencap failed for {peek_id}: returncode={res.returncode}")
        return False

    out_png.parent.mkdir(parents=True, exist_ok=True)
    with open(out_png, "wb") as f:
        f.write(res.stdout)
    return True

def make_side_by_side(ios_path, android_path, out_compare_path, target_height=1000):
    if not ios_path.exists():
        print(f"[-] iOS reference not found: {ios_path}")
        return False
    if not android_path.exists():
        print(f"[-] Android capture not found: {android_path}")
        return False

    ios_img = Image.open(ios_path).convert("RGBA")
    android_img = Image.open(android_path).convert("RGBA")

    # Scale both to target_height preserving aspect ratio
    ios_w = int(ios_img.width * (target_height / ios_img.height))
    ios_resized = ios_img.resize((ios_w, target_height), Image.Resampling.LANCZOS)

    and_w = int(android_img.width * (target_height / android_img.height))
    and_resized = android_img.resize((and_w, target_height), Image.Resampling.LANCZOS)

    header_height = 40
    divider_width = 8
    total_width = ios_w + and_w + divider_width
    total_height = target_height + header_height

    canvas = Image.new("RGBA", (total_width, total_height), (242, 242, 247, 255))
    draw = ImageDraw.Draw(canvas)

    # Headers
    # Left header: iOS Reference
    draw.rectangle([0, 0, ios_w, header_height], fill=(230, 230, 235, 255))
    draw.text((16, 12), f"iOS Reference ({ios_img.width}x{ios_img.height})", fill=(60, 60, 67, 255))

    # Divider
    draw.rectangle([ios_w, 0, ios_w + divider_width, total_height], fill=(210, 210, 215, 255))

    # Right header: Android Output
    draw.rectangle([ios_w + divider_width, 0, total_width, header_height], fill=(230, 230, 235, 255))
    draw.text((ios_w + divider_width + 16, 12), f"Android Preview ({android_img.width}x{android_img.height})", fill=(60, 60, 67, 255))

    # Paste images
    canvas.paste(ios_resized, (0, header_height), ios_resized)
    canvas.paste(and_resized, (ios_w + divider_width, header_height), and_resized)

    out_compare_path.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(out_compare_path, format="PNG")
    return True

def main():
    parser = argparse.ArgumentParser(description="Capture & compare Android screens with iOS references")
    parser.add_argument("--id", type=str, help="Specific peek ID to capture and compare")
    parser.add_argument("--all", action="store_true", help="Capture and compare all peek screens")
    parser.add_argument("--serial", type=str, default="", help="ADB device serial")
    parser.add_argument("--generate-only", action="store_true", help="Only stitch existing captured images")
    parser.add_argument("--wait", type=float, default=3.5, help="Seconds to wait after am start")
    args = parser.parse_args()

    project_root = Path(__file__).resolve().parent.parent
    ios_dir = project_root / "docs" / "ios-reference" / "wholesaler"
    android_dir = project_root / "docs" / "android-parity" / "wholesaler"

    adb_path = find_adb()

    target_ids = []
    if args.id:
        target_ids = [args.id]
    elif args.all or args.generate_only:
        target_ids = ALL_PEEK_IDS
    else:
        parser.print_help()
        sys.exit(1)

    print(f"[*] Processing {len(target_ids)} peek id(s)...")

    success_count = 0
    for peek_id in target_ids:
        and_png = android_dir / f"{peek_id}.png"
        ios_png = ios_dir / f"{peek_id}.png"
        compare_png = android_dir / f"{peek_id}-compare.png"

        if not args.generate_only:
            print(f"[+] Capturing peek '{peek_id}' via adb...")
            ok = capture_screen(adb_path, args.serial, peek_id, and_png, wait_sec=args.wait)
            if not ok:
                print(f"[-] Failed to capture {peek_id}")
                continue

        if ios_png.exists() and and_png.exists():
            print(f"[+] Generating comparison: {compare_png.name}")
            ok = make_side_by_side(ios_png, and_png, compare_png)
            if ok:
                success_count += 1
        else:
            print(f"[!] Skipped comparison for '{peek_id}': ios={ios_png.exists()}, android={and_png.exists()}")

    print(f"\n[✓] Done. Successfully generated {success_count} comparison images in {android_dir}.")

if __name__ == "__main__":
    main()
