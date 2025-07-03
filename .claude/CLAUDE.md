# CLAUDE.local.md

## Development Commands

### Build and Compile
```bash
# RECOMMENDED: Desktop-only compilation for development and verification
gradle :composeApp:compileKotlinDesktop

# DO NOT USE: Android builds will fail due to environment issues
# gradle build
# gradle assembleDebug
# gradle installDebug
```

### Testing
```bash
# RECOMMENDED: Desktop-specific tests (reliable in current environment)
gradle desktopTest

# Run desktop application
gradle run
```

### Notes
- Android SDK not configured in current environment
- Always use desktop-only builds for verification
- Full builds may fail due to Android SDK environment issues