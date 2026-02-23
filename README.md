<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android&logoColor=white" alt="Platform" />
  <img src="https://img.shields.io/badge/minSdk-26-green?style=flat" alt="Min SDK" />
  <img src="https://img.shields.io/badge/Kotlin-2.3-7F52FF?style=flat&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-2026.02-4285F4?style=flat&logo=jetpackcompose&logoColor=white" alt="Compose" />
  <img src="https://img.shields.io/badge/Firebase-RTDB%20%7C%20Auth%20%7C%20Storage-FFCA28?style=flat&logo=firebase&logoColor=black" alt="Firebase" />
</p>

# Tu Armeria - My Armory: Spain Gun Manager

The essential tool for gun owners, hunters, and sport shooters in Spain. **Tu Armeria** helps you manage your firearm inventory, track ammunition quotas, and stay compliant with Spanish Gun Laws (*Reglamento de Armas*).

Generic gun apps don't understand the Spanish *Guia de Pertenencia* system or the strict ammo limits enforced by the *Guardia Civil*. This app does.

<a href="https://play.google.com/store/apps/details?id=al.ahgitdevelopment.municion">
  <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="80" alt="Get it on Google Play" />
</a>

<!--
## Screenshots

<p align="center">
  <img src="docs/screenshots/licencias.png" width="200" />
  <img src="docs/screenshots/guias.png" width="200" />
  <img src="docs/screenshots/compras.png" width="200" />
  <img src="docs/screenshots/tiradas.png" width="200" />
</p>
-->

## Features

- **Digital Firearm Inventory** — Organize all your firearms with make, model, caliber, serial number, and photos. Each weapon linked to its *Guia de Pertenencia* and License Type (B, C, D, E, F, AEM).
- **Ammunition Quota Tracker** — Track purchases against your legal yearly limit. Visual alerts when approaching storage caps. Shooting range purchases are automatically exempt from quota per Spanish regulation.
- **License & Renewal Management** — Calendar integration with reminders for license expiry, mandatory inspections (*Revista de Armas*), insurance, and federation cards.
- **Championship & Shooting Log** — Manage competition dates, log scores (Precision 0-600 / IPSC 0-100%), and track participation for F License renewals.
- **Offline-First with Cloud Sync** — Works without internet. Data syncs automatically to Firebase when connectivity is available.
- **Multi-language** — Spanish and English.

## Architecture

The app follows **MVVM + Clean Architecture** with an offline-first approach:

```
UI Layer (Compose)          Domain Layer              Data Layer
┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│ Screens          │    │ Use Cases        │    │ Repositories     │
│ ViewModels       │───>│ Business Logic   │───>│ Room (SQLite)    │
│ Navigation       │    │ Validation       │    │ Firebase RTDB    │
│ State (MVI)      │    │                  │    │ Firebase Storage │
└──────────────────┘    └──────────────────┘    └──────────────────┘
```

**Key design decisions:**
- **Room is the source of truth.** Firebase provides cloud backup and cross-device sync.
- **Type-safe navigation** with custom `NavType` and `@Serializable` routes — full objects are passed between screens, eliminating race conditions from ID-based lookups.
- **MVI pattern** in form screens — unidirectional data flow with events, state, and one-shot effects.

## Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | [Kotlin 2.3](https://kotlinlang.org/) (K2 compiler) |
| **UI** | [Jetpack Compose](https://developer.android.com/jetpack/compose) + [Material Design 3](https://m3.material.io/) |
| **Navigation** | [Navigation Compose 2.9](https://developer.android.com/guide/navigation/navigation-type-safety) (type-safe routes) |
| **DI** | [Hilt 2.59](https://dagger.dev/hilt/) |
| **Local DB** | [Room 2.8](https://developer.android.com/training/data-storage/room) |
| **Async** | [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) + Flow |
| **Images** | [Coil 2.7](https://coil-kt.github.io/coil/) (loading) + Firebase Storage (persistence) |
| **Serialization** | [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) |
| **Background** | [WorkManager 2.11](https://developer.android.com/topic/libraries/architecture/workmanager) |
| **Security** | [AndroidX Biometric](https://developer.android.com/jetpack/androidx/releases/biometric) + [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences) |
| **Testing** | [JUnit 5](https://junit.org/junit5/) + [MockK](https://mockk.io/) + [Espresso](https://developer.android.com/training/testing/espresso) |
| **Leak Detection** | [LeakCanary](https://square.github.io/leakcanary/) (debug only) |

### Firebase Services

| Service | Purpose |
|---------|---------|
| **Authentication** | Email/password and anonymous sign-in |
| **Realtime Database** | Cloud sync with offline persistence |
| **Storage** | Full-size firearm and purchase photos |
| **Crashlytics** | Crash reporting with PII-redacted metadata |
| **Analytics** | Usage tracking |
| **Cloud Messaging** | Push notifications |

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- JDK 17+
- A Firebase project ([setup guide](https://firebase.google.com/docs/android/setup))

### Build

```bash
# Clone the repository
git clone https://github.com/foliolo/Municion.git
cd Municion

# Place your Firebase config
cp /path/to/your/google-services.json app/

# Create keystore.properties for release builds (optional)
cat > keystore.properties << EOF
keyAlias=your_alias
keyPassword=your_password
storeFile=/path/to/keystore.jks
storePassword=your_store_password
EOF

# Build debug
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### Firebase Setup

The app requires a Firebase project with:
1. **Authentication** — Enable Email/Password and Anonymous providers
2. **Realtime Database** — Create a database instance
3. **Storage** — Enable Cloud Storage for image uploads
4. **Crashlytics** — Enable in the Firebase console

Place your `google-services.json` in the `app/` directory.

## Data Model

The app manages 4 entities stored as arrays under `users/{uid}/db/` in Firebase:

| Entity | Spanish | Description |
|--------|---------|-------------|
| **Licencia** | Licencia | Firearms license with expiration tracking |
| **Guia** | Guia | Firearm permit linked to a license, with annual ammo quota |
| **Compra** | Compra | Ammunition purchase linked to a Guia |
| **Tirada** | Tirada | Shooting competition/event record |

**Key relationship:** Each *Guia* has an annual quota (`cupo`). Purchases in stores consume quota; purchases at shooting ranges are exempt per Spanish law. Quota resets every January 1st.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request against `develop`

## License

This project is for personal and educational use. See the repository for details.

---

<p align="center">
  Made with :coffee: in Spain
</p>
