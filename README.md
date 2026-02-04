# LinkIO Android

[![JitPack](https://jitpack.io/v/pt-nakul-sharma/LinkIO-Android.svg)](https://jitpack.io/#pt-nakul-sharma/LinkIO-Android)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![16KB Compatible](https://img.shields.io/badge/16KB%20Page%20Size-Compatible-green.svg)](https://developer.android.com/about/versions/15/behavior-changes-15#16kb-page-size)

Self-hosted deep linking SDK for Android. Open-source alternative to Branch.io.

## 🚀 Installation

Add JitPack repository to your `settings.gradle`:

```gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Add dependency to your module `build.gradle`:

```gradle
dependencies {
    implementation 'com.github.pt-nakul-sharma:LinkIO-Android:1.1.0'
}
```

## 📱 Quick Start

### 1. Configure in Application

```kotlin
import io.linkio.android.LinkIO
import io.linkio.android.LinkIOConfig

class MyApplication : Application() {

    // Configure your backend URL with API version
    private val apiVersion = "api/v1/"  // Match your backend route structure
    private val baseURL = "https://api.yourdomain.com/"

    override fun onCreate() {
        super.onCreate()

        val config = LinkIOConfig(
            domain = "yourdomain.com",
            backendURL = baseURL + apiVersion,  // Full path: https://api.yourdomain.com/api/v1/
            appScheme = "yourapp"  // Custom URL scheme (e.g., yourapp://)
        )

        LinkIO.configure(this, config)

        LinkIO.setDeepLinkListener { deepLink ->
            Log.d("LinkIO", "Received: ${deepLink.params}")

            deepLink.params["referralCode"]?.let { referralCode ->
                // Auto-fill referral code in your UI
            }
        }
    }
}
```

### 2. Add Intent Filters

In your `AndroidManifest.xml`:

```xml
<activity android:name=".MainActivity">
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="https"
            android:host="yourdomain.com" />
    </intent-filter>
</activity>
```

### 3. Handle Deep Links in Activity

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LinkIO.handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { LinkIO.handleIntent(it) }
    }
}
```

## 📚 API Reference

### Configuration

```kotlin
// backendURL should include the full API path
// SDK appends: pending-link/, track-referral
val config = LinkIOConfig(
    domain = "yourdomain.com",
    backendURL = "https://api.yourdomain.com/api/v1/",  // Trailing slash required
    appScheme = "yourapp",  // Custom URL scheme (e.g., yourapp://)
    autoCheckPendingLinks = true
)
LinkIO.configure(application, config)

// SDK will call:
// - https://api.yourdomain.com/api/v1/pending-link/:deviceId
// - https://api.yourdomain.com/api/v1/track-referral
```

### Set Deep Link Listener

```kotlin
LinkIO.setDeepLinkListener { deepLink ->
    println("URL: ${deepLink.url}")
    println("Params: ${deepLink.params}")
    println("Deferred: ${deepLink.isDeferred}")

    // Handle params directly - no "type" field needed
    deepLink.params["referralCode"]?.let { code ->
        // Handle referral
    }
    deepLink.params["userId"]?.let { userId ->
        // Show user profile
    }
    deepLink.params["carId"]?.let { carId ->
        // Show car details (Ejaro)
    }
}
```

### Deep Link URL Format

Use a generic `/link` endpoint with query params:

```
https://rokart.in/link?referralCode=ABC123
https://speekfeed.in/link?userId=456
https://ejaro.com/link?carId=789
```

### Track Referrals

```kotlin
LinkIO.trackReferral(
    referralCode = "ABC123",
    userId = "user123",
    metadata = mapOf("source" to "android")
)
```

### Manual Check for Deferred Links

```kotlin
LinkIO.checkPendingLink()
```

## 🔧 Setup Digital Asset Links

Generate SHA256 fingerprint:

```bash
keytool -list -v -keystore your-keystore.jks -alias your-alias
```

Your backend will automatically serve the `assetlinks.json` at:

```
https://yourdomain.com/.well-known/assetlinks.json
```

## 🔗 Related Packages

- **Backend**: [LinkIO-Backend](https://github.com/pt-nakul-sharma/LinkIO-Backend)
- **iOS**: [LinkIO-iOS](https://github.com/pt-nakul-sharma/LinkIO-iOS)
- **React Native**: [LinkIO-React-Native](https://github.com/pt-nakul-sharma/LinkIO-React-Native)

## 🛠️ Requirements

### For the Library

- **Min SDK**: Android API 24+ (Nougat)
- **Compile SDK**: 35+ (recommended)
- Kotlin 1.9+

### For Your Project

- **Your project settings are inherited** - library adapts to your minSdk, compileSdk, and targetSdk
- Gradle 8.13+
- AGP 8.3.0+
- **16KB page size compatible** (automatically works on Android 15+)

## 📄 License

MIT

## 🤝 Contributing

Contributions are welcome! Please read the [contributing guidelines](CONTRIBUTING.md) first.

## ⭐ Show Your Support

Give a ⭐️ if this project helped you!
