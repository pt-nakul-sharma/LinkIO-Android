# Changelog

## [1.1.0] - 2026-02-03

### Fixed
- Removed hardcoded `/api/` prefix from URL construction
- `backendURL` now controls the full API path (including version prefix)

### Changed
- SDK now appends `pending-link/` and `track-referral` directly to `backendURL`
- Updated documentation with correct `backendURL` configuration examples

### Migration
If you were using:
```kotlin
backendURL = "https://api.example.com"
```

Update to include your API version:
```kotlin
backendURL = "https://api.example.com/api/v1/"  // Trailing slash required
```

## [1.0.0] - 2026-02-03

### Added
- Initial release
- App Links support
- Deferred deep linking
- Referral tracking
- JitPack distribution
- 16KB page size compatible
- Min SDK 24, inherits project settings
