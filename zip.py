#!/usr/bin/env python3
import os
import zipfile

EXCLUDE_DIRS = {".git", "manager", "build"}
EXCLUDE_FILES = {".gitignore", "README.md", "LICENSE", "zip.py", "update.json"}

def main():
    root = os.path.dirname(os.path.abspath(__file__))
    out_name = input("Nama file zip [rclone-v1.75.0-fusermount-arm64.zip]: ").strip()
    out_name = out_name or "rclone-v1.75.0-fusermount-arm64.zip"
    out_path = os.path.join(root, out_name)

    with zipfile.ZipFile(out_path, "w", zipfile.ZIP_DEFLATED) as zf:
        for dirpath, dirs, files in os.walk(root):
            dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS]
            for f in files:
                if f in EXCLUDE_FILES or f == out_name:
                    continue
                full = os.path.join(dirpath, f)
                arcname = os.path.relpath(full, root).replace(os.sep, "/")
                zf.write(full, arcname)
                print(f"  + {arcname}")

    print(f"\nZip selesai: {out_name} ({os.path.getsize(out_path) / 1024 / 1024:.2f} MB)")

if __name__ == "__main__":
    main()