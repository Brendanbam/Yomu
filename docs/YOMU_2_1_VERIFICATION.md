# Yomu 2.1 verification

The development branch remains separate from the published application.

## Automated checks

The pull request runs formatting, unit tests, SQLDelight migration validation, release APK compilation, dependency review, and an Android emulator smoke test. The emulator installs the actual x86_64 APK, verifies startup and a resumed application activity, changes the window size/density, restarts the app, checks for crashes, and uploads screenshots and logs. These are **fresh-install smoke checks**, not full manga-library or reader UI coverage.

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
