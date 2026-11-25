# Yomu 2.1 verification

The development branch remains separate from the published application.

## Feature entry points

| Feature | Where to find it |
| --- | --- |
| Unified search | Browse search bar or Library overflow â†’ Search library and sources |
| Next unread and spine view | Play on an unread volume. In Display, choose Classic Bookshelf, Collector Edition, or Book Spine |
| Tablet split view | Select a book in a wide Library window. Drag the divider, use D-pad Left/Right, or open full screen from the inspector toolbar |
| Next-chapter downloads | Settings â†’ Downloads â†’ Download ahead. Choose how many unread chapters to queue while reading a library title |
| Appearance presets | Settings â†’ Appearance â†’ Shelf appearance presets |
| Category shelf material | Library â†’ Display â†’ This categoryâ€™s shelf material |
| Random manga | Home â†’ Quick Access â†’ Surprise me, or Library overflow for the current category |
| Goals and collection statistics | Home â†’ Reading Overview â†’ Stats. The goal counts distinct titles with reading history in the past seven days |
| Reading widget | Android launcher widget picker â†’ Yomu â†’ Continue Reading |
| AMOLED reading room | Settings â†’ Reader â†’ Background, also available in the readerâ€™s settings |

## Automated checks

The pull request runs formatting, unit tests, SQLDelight migration validation, the full Gradle check, release APK compilation, dependency review, and an Android emulator smoke test.

The emulator installs the compiled x86_64 APK and captures phone and tablet startup. It completes onboarding, restores a synthetic library through the app's backup handler, opens a local chapter, and exercises the tablet inspector with touch and D-pad input. It checks full-screen navigation, closing the inspector, and the restored shelf after restart. The workflow uploads screenshots and logs, including the last accessibility hierarchy.

The backup fixture in `app/src/test/resources/yomu-2.0-library.tachibk` was written by Yomu's 2.0 backup writer using invented series data. The unit test checks its saved page, bookmark, reading history, and categories. The emulator test creates local CBZ files for that series.

GitHub dependency review requires the repository's dependency graph to be enabled. If the job returns HTTP 403, check Settings → Security / Code security → Dependency graph. Keep the review job enabled; do not mark it successful by skipping the scan.

## Before publishing

- [ ] Verify all build and security checks on the final commit.
- [ ] Download the emulator artifacts and inspect the actual screenshots.
- [ ] Install a test build with a populated, non-sensitive library on a phone and tablet.
- [ ] Test classic shelves, book spines, category filters, multi-selection, inspector resizing/closing, and controller focus.
- [ ] Verify downloaded chapter availability and source errors with and without internet.
- [ ] Test reader directions, bookmarks, progress, history, backup export/import, and restart.
- [ ] Measure library scroll/startup performance with a large synthetic library.
- [ ] Compare all major screens to the Yomu visual references, including themes and tablet layouts.
- [ ] Verify signed upgrade compatibility with v2.0.0; preserve the existing signing certificate.
- [ ] Obtain approval before merging to main or publishing a production release.

A successful compilation or fresh-install smoke test is not evidence that all these workflows have been verified.
