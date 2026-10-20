# iOS app

The SwiftUI app is defined in `SahaaraApp.swift`. A minimal Xcode project is generated from `project.yml` with XcodeGen, so CI can build the app for the iOS Simulator without signing credentials.

## Build locally

On macOS with Xcode and XcodeGen installed:

```sh
brew install xcodegen
xcodegen generate --spec apps/ios/project.yml --project apps/ios
xcodebuild -project apps/ios/Sahaara.xcodeproj -scheme Sahaara -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO build
```

Reminders support a native date picker and are persisted locally through UserDefaults. They are not encrypted as a dedicated vault, user-scoped, or synced to a server. Authentication, secure uploads, notifications, and official service integrations are not enabled.
