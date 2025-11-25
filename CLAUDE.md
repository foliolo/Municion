# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Munición** is an Android ammunition management application for Spanish firearms license holders. It tracks ammunition purchases (Compras), firearms permits (Guias), licenses (Licencias), and shooting competitions (Tiradas). The app is written in Java and uses Firebase for backend services.

**Package name**: `al.ahgitdevelopment.municion`

## Build Commands

Build the project:
```bash
./gradlew assembleRelease
```

Build debug version:
```bash
./gradlew assembleDebug
```

Clean build:
```bash
./gradlew clean
```

Install debug build on connected device:
```bash
./gradlew installDebug
```

## Build Configuration

- **Min SDK**: 26
- **Target SDK**: 34
- **Compile SDK**: 34
- **Current Version**: 2.0.1 (versionCode 33)
- **Build Tools**: 34.0.0
- **Gradle Plugin**: 8.2.0

The project requires a `keystore.properties` file in the root directory for signing release builds with properties: `keyAlias`, `keyPassword`, `storeFile`, `storePassword`.

## Architecture

### Core Data Models (`datamodel/`)

The app manages four primary entities, all implementing `Parcelable`:

1. **Licencia** (License): Represents firearms licenses with expiration tracking, calendar integration, and notifications
2. **Guia** (Firearm Permit): Tracks individual firearms with ammunition quotas ("cupo") and usage tracking ("gastado")
3. **Compra** (Purchase): Ammunition purchases linked to a specific Guia, tracking caliber, quantity, price, date, and store
4. **Tirada** (Competition/Shooting Event): Competition records with scores and dates

### Data Flow

- **Primary persistence**: Firebase Realtime Database (when online)
- **Local persistence**: SQLite via `DataBaseSQLiteHelper` (when offline)
- **Synchronization**: On app pause, local data is synced to Firebase if network is available
- **Image storage**: Firebase Storage for full-size images, local thumbnail cache

Key relationship: Compras reference Guias by position in the list (`idPosGuia`), not by database ID. When deleting a Compra, the corresponding Guia's `gastado` field is decremented.

### Main Activity Structure

`FragmentMainActivity` is the hub activity with a `ViewPager` containing 4 tabs:
- Tab 0: Guias list
- Tab 1: Compras list
- Tab 2: Licencias list (default starting tab)
- Tab 3: Tiradas list

Each tab uses `PlaceholderFragment` with different ArrayAdapters for displaying data. Context Action Bar (CAB) provides edit/delete functionality on long-press.

### Authentication

Uses Firebase Authentication with three modes:
1. Email/password authentication (user-created accounts)
2. Anonymous authentication (default for users without accounts)
3. User data is stored under `users/{uid}/db/` in Firebase

The app attempts to create a user account, then signs in, falling back to anonymous auth if no email is configured.

### Special Features

**Quota Management**:
- Each Guia has an annual ammunition quota (`cupo`) that resets on January 1st
- The `updateGastoMunicion()` method recalculates usage only for the current year
- Purchases from previous years don't count against current quota

**License Expiration Notifications**:
- Calendar events created for license expiration (same day and one month prior)
- Uses system Calendar API with proper permission handling
- Notification data stored in SharedPreferences

**Camera Integration**:
- Captures photos for Guias and Compras
- Stores thumbnails locally (20% scaled) and full images in Firebase Storage
- Uses FileProvider for camera intents

## Firebase Structure

User data structure in Firebase Realtime Database:
```
users/
  {uid}/
    email: String
    pass: String
    db/
      guias: ArrayList<Guia>
      compras: ArrayList<Compra>
      licencias: ArrayList<Licencia>
      tiradas: ArrayList<Tirada>
```

## Critical Implementation Details

1. **Year tracking**: SharedPreferences stores current year for quota calculations
2. **List position dependencies**: Compra.idPosGuia references the ArrayList position of its parent Guia (not database ID)
3. **Dual storage**: Always save to both SQLite (immediate) and Firebase (on pause when online)
4. **Deletion validation**: Licenses cannot be deleted if they have associated Guias (`Utils.licenseCanBeDeleted()`)

## Form Activities

Each entity has a dedicated form activity in the `forms/` package:
- `LicenciaFormActivity`: Handles calendar permissions and event creation
- `GuiaFormActivity`: Requires license selection dialog before creation
- `CompraFormActivity`: Requires Guia selection dialog and updates parent Guia's `gastado` field
- `TiradaFormActivity`: Includes scoring and date management

All forms use result codes (COMPLETED/UPDATED) to communicate changes back to `FragmentMainActivity`.

## Common Utilities

`Utils.java` contains shared helper methods including:
- Date parsing and formatting
- License type string lookups
- Calendar event management
- Image handling (save to disk, upload to Firebase)
- Network connectivity checks
- SharedPreferences management for notifications

## Authentication Flow (TRACK B Modernization)

The app uses Firebase Authentication with automatic recovery for seamless user experience.

```
┌─────────────────────────────────────────────────────┐
│                    APP STARTUP                       │
│                  MainActivity.onCreate()             │
└─────────────────────────┬───────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│          MainViewModel.checkAuthState()             │
└─────────────────────────┬───────────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │ firebaseAuth.currentUser │
              │        != null?        │
              └───────────┬───────────┘
                    ╱           ╲
                  YES            NO
                  │               │
                  ▼               ▼
       ┌──────────────────┐  ┌──────────────────────┐
       │ State=Authenticated│  │ attemptFirebaseRecovery│
       │ syncFromFirebase()│  │ signInAnonymously()   │
       └──────────────────┘  └──────────┬───────────┘
                                        │
                                        ▼
                              ┌─────────────────┐
                              │ Recovery OK?    │
                              └────────┬────────┘
                                 ╱          ╲
                               YES           NO
                               │              │
                               ▼              ▼
                    ┌──────────────┐  ┌──────────────┐
                    │ Authenticated│  │Unauthenticated│
                    │ sync...      │  │→ Login Screen │
                    └──────────────┘  └──────────────┘
```

**Key Files:**
- `MainViewModel.kt` - Authentication state management
- `FirebaseAuthRepository.kt` - Firebase Auth wrapper
- `LoginActivity.kt` - Login UI

## Data Synchronization Flow (TRACK B Modernization)

Room is the **source of truth**. Firebase provides cloud backup and cross-device sync.

```
┌─────────────────────────────────────────────────────┐
│       syncFromFirebaseWithAutoFix(userId)           │
│              SyncDataUseCase.kt                     │
└─────────────────────────┬───────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│  Download from Firebase (4 entities in parallel)    │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐   │
│  │Licencias│ │ Guías   │ │ Compras │ │ Tiradas │   │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘   │
└─────────────────────────┬───────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│  Manual field-by-field parsing                      │
│  If error → Crashlytics.recordException()           │
│             (userId, entity, field, value)          │
└─────────────────────────┬───────────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │ Parse errors exist    │
              │ AND Room has data?    │
              └───────────┬───────────┘
                    ╱           ╲
                  YES            NO
                  │               │
                  ▼               ▼
       ┌──────────────────┐  ┌──────────────────┐
       │ AUTO-FIX:        │  │ Sync completed   │
       │ syncToFirebase() │  │ normally         │
       │ Room → Firebase  │  │                  │
       └──────────────────┘  └──────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│  Room updated → Flow emits → UI updates             │
└─────────────────────────────────────────────────────┘
```

### Sync Strategy: Cloud Wins with Auto-Fix

1. **Download**: Firebase data replaces Room data
2. **Parse Errors**: If Firebase has corrupt data, report to Crashlytics with:
   - `userId` - Firebase Auth UID
   - `entity` - Which entity failed (Licencia, Guia, etc.)
   - `failed_field` - Which field failed validation
   - `error_type` - Type of error (Missing, Blank, Invalid)
   - `field_value` - Raw value (REDACTED for PII fields)
3. **Auto-Fix**: If Firebase has errors but Room has valid data, upload Room → Firebase

### Sensitive Fields (Never sent to Crashlytics)
- `numLicencia`, `numGuia`, `numArma`
- `nombre`, `dni`
- `numAbonado`, `numSeguro`

**Key Files:**
- `SyncDataUseCase.kt` - Orchestrates sync with auto-fix
- `*Repository.kt` - Individual entity sync with manual parsing
- `SyncModels.kt` - ParseError, SyncResultWithErrors classes

## Entity Creation Flow (TRACK B Modernization)

All 4 entities follow the same pattern: Fragment → FormActivity → ViewModel → Repository → Room + Firebase

```
┌──────────────────────────────────────────────────────────────────┐
│ User taps FAB                                                     │
└─────────────────────────┬────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│ [Guías/Compras only] Show selection dialog                        │
│  - Guías: Select Licencia to associate                           │
│  - Compras: Select Guía to associate                             │
└─────────────────────────┬────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│ Launch FormActivity with Intent extras                           │
│  - tipo_licencia (Guías)                                         │
│  - guia + position_guia (Compras)                                │
└─────────────────────────┬────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│ User fills form → fabSaveOnClick()                               │
│ bundle.putParcelable("modify_xxx", getCurrentXxx())              │
│ setResult(RESULT_OK, intent) → finish()                          │
└─────────────────────────┬────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│ Fragment.handleXxxFormResult()                                   │
│ getParcelableExtra("modify_xxx") → Legacy entity                 │
│ convertLegacyToRoom(legacy) → Room entity                        │
│ viewModel.saveXxx(roomEntity)                                    │
└─────────────────────────┬────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│ Repository.saveXxx() → Room DAO insert + syncToFirebase()        │
│ Flow emits change → UI updates automatically                     │
└──────────────────────────────────────────────────────────────────┘
```

### Entity Dependencies
| Entity | Requires Selection | Parent |
|--------|-------------------|--------|
| Licencia | None | - |
| Tirada | None | - |
| Guía | Licencia dialog | Licencia (by tipo) |
| Compra | Guía dialog | Guía (by idPosGuia) |

**Key Files:**
- `ui/*/Fragment.kt` - Modern Kotlin fragments with RecyclerView
- `forms/*FormActivity.java` - Legacy Java form activities
- `ui/viewmodel/*ViewModel.kt` - State management with Hilt
- `data/repository/*Repository.kt` - Data access with Room + Firebase

## Spanish Terms Reference

- **Guia** = Firearms permit/guide (for a specific weapon)
- **Compra** = Purchase (ammunition purchase)
- **Licencia** = License (firearms license)
- **Tirada** = Shooting event/competition
- **Cupo** = Quota (ammunition allowance)
- **Gastado** = Spent/used (ammunition consumed)
- **Calibre** = Caliber
- **Munición** = Ammunition