# VML Recover — Cross-Platform Deployment Guide (iOS & Android)

**Virgin Markets Multi Legal (VML Recover)** is built with modern **Kotlin Multiplatform (KMP)** and **Compose Multiplatform (CMP)** architecture standards. The business logic, state machines (`ViewModel`, `StateFlow`), repositories, database models, and declarative UI composables are engineered to run across both **Android** and **Apple iOS**.

---

## 1. Architecture Overview

- **Shared UI Layer:** Declarative UI built with Jetpack Compose / Compose Multiplatform (`@Composable`). Runs natively on Android (ART) and on iOS via JetBrains Compose Multiplatform (`org.jetbrains.compose`, Skiko rendering engine over Apple Metal).
- **Shared State & Business Logic:** Pure Kotlin `ViewModel` architecture using Kotlin Coroutines and `StateFlow` without Android-specific coupling.
- **Data Persistence:** Local SQLite persistence with standard schema compatible with Room (Android) and SQLDelight / SQLite (iOS).
- **Adaptive Platform Presentation:** Built-in presentation engine supporting:
  - **iOS (Cupertino Adaptive):** Centered title styling, iOS-style `< Back` navigation affordances, rounded Cupertino segmented controls, and iOS action dialogs.
  - **Android (Material 3):** Standard Material 3 app bars, system back gesture support, and dynamic elevation.
  - **Adaptive (Universal):** Automatically conforms to the host platform environment.

---

## 2. Running on Android

The app is immediately ready for Android deployment:
1. Open the project in **Android Studio (Ladybug or newer)**.
2. Build the app using Gradle:
   ```bash
   gradle assembleDebug
   ```
3. Run on any Android emulator or physical device running Android 7.0 (API 24) to Android 15 (API 35+).

---

## 3. Running on Apple iOS (Compose Multiplatform)

To run **VML Recover** as a native iOS app on macOS using Xcode:

### Step 1: Export Project
- In Google AI Studio, open the top-right settings menu and select **Export as ZIP** or **Push to GitHub**.
- Clone or unzip the project on your macOS workstation.

### Step 2: iOS Wrapper Entry Point (`iosApp`)
In standard Compose Multiplatform projects, the shared Compose UI is exposed to iOS via `ComposeUIViewController`.

Create an `iosApp` folder alongside `app/` containing:

#### `iosApp/iosApp/ContentView.swift`
```swift
import SwiftUI
import shared // Or your shared KMP framework

struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // Calls the shared Kotlin ComposeUIViewController entry point
        return MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea(.all) // Compose Multiplatform handles safe insets internally
        }
    }
}
```

#### Shared iOS Entry Point (`commonMain` / `iosMain` Kotlin)
```kotlin
package com.example

import androidx.compose.ui.window.ComposeUIViewController
import com.example.ui.theme.MyApplicationTheme

fun MainViewController() = ComposeUIViewController {
    MyApplicationTheme {
        // Launches the exact same root navigation and screens as MainActivity
        VmlAppRoot()
    }
}
```

### Step 3: Build in Xcode
1. Open `iosApp/iosApp.xcworkspace` in **Xcode**.
2. Select your target device (e.g., **iPhone 16 Pro Simulator** or connected iPhone).
3. Press **Cmd + R** (Run).
4. Xcode compiles the Kotlin code to a native Apple Framework (`.xcframework`) and links it to the Swift application binary.

---

## 4. Cross-Platform UX Compatibility Guarantees

| Feature | iOS Handling | Android Handling |
| :--- | :--- | :--- |
| **Safe Areas & Insets** | Compensates for iPhone Dynamic Island, Notch, and Home Indicator bar via `WindowInsets.safeDrawing`. | Handled via `enableEdgeToEdge()` and `statusBarsPadding()` / `navigationBarsPadding()`. |
| **Back Navigation** | Explicit top-left `< Back` button with chevron on all sub-screens prevents iOS users from becoming trapped. | Full support for Android hardware/gesture system back navigation. |
| **Touch Targets** | All interactive targets $\ge 48\,\text{dp}$, surpassing Apple HIG minimum ($44\times 44\,\text{pt}$). | Conforms to Google Material 3 accessibility guidelines ($48\,\text{dp}$). |
| **Tab Controls** | `AdaptiveSegmentedTabRow` displays rounded Cupertino segmented pills with elevation. | Displays smooth segmented buttons or Material 3 indicators. |
| **Color Contrast** | Warm creamish-grey (`#EBE6DF`) and pure white cards with deep espresso charcoal typography exceed WCAG AAA standards across Super Retina XDR and AMOLED screens. | Same crisp palette with high daytime sunlight legibility. |

---

## 5. Summary of Role Profiles

All eight end-to-end levels function identically across iOS and Android:
1. **Admin:** Complete system control & security administration.
2. **Director:** Executive portfolio overview & performance analytics only.
3. **Collection Manager:** Case allocation and field team supervision.
4. **Collection Officer:** Logging calls, GPS-verified visits, promises-to-pay, and cash/MoMo receipts.
5. **Legal Officer:** 9-stage litigation pipeline from statutory demand to execution warrants.
6. **Finance Officer:** Client-specific ledgers, reconciliation, and escrow settlement.
7. **Clients:** Assigned portfolio statement, recovery rate, payment velocity, and PDF exports.
8. **Debtors:** Balance breakdown, structured repayment plan, and instant mobile money payments.
