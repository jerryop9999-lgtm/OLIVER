# Architecture

```
MainActivity ── AppRoot (3 tabs) ── MainViewModel
                                       │
                 ┌─────────────────────┼───────────────────┐
           ConversionEngine        data/*              ui/*Screen
   copy → detect → extract →     Prefs, HistoryStore,   Convert, History,
   merge → sign → (obb)          OutputSaver, Installer  Settings
```

core/
- BundleDetector  : xapk / apks / apkm / apk by zip contents
- BundleExtractor : zip-slip-safe extraction, reads manifest.json, collects OBB
- SplitMerger     : calls APKEditor (app/libs/APKEditor.jar) reflectively
- ApkSignerService: apksig v1+v2+v3 with PKCS12 keystore (asset or imported)

Ideas to add next: batch convert, foreground service for very large files,
split-selection (arch/density/language), app-info preview, Khmer UI strings.
