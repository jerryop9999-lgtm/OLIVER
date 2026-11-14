# OLIVER (Android app)

Convert `.xapk`, `.apks`, `.apkm` → single installable `.apk`, on the phone.

## Build with phone only
1. Create a GitHub repo and upload all these files (keep folder structure).
2. Actions → **Build APK** → Run workflow (downloads APKEditor, makes a keystore, builds).
3. Download artifact `OLIVER-debug`, unzip, install the APK.

No Gradle wrapper is needed; the workflow installs Gradle 8.9.

## Notes
- Merge engine: APKEditor jar (version set in `.github/workflows/build.yml`).
- Signing: built-in debug keystore, or import your own `.p12` in Settings.
- OBB files are exported to `Download/OLIVER/obb/<package>/`; move them to
  `/sdcard/Android/obb/<package>/` with a file manager.
- Open `.xapk` from a file manager with "Open with → OLIVER".
