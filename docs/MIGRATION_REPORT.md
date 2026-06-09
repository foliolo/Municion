# Informe de migración Munición → KMP

Informe vivo (esquemático). Se actualiza al cerrar cada fase. Plan completo: `~/.claude/plans/a-continuacion-hay-que-scalable-music.md`.

Stack destino: Kotlin 2.4.0 · Compose MP 1.11.1 · AGP 9.2.1 · módulos `:shared` (KMP lib) + `:androidApp` + `iosApp`. Koin · Room KMP · GitLive Firebase (RTDB) · Navigation MP · Coil3 · multiplatform-settings · kotlinx-datetime/serialization · BuildKonfig (gmazzo) · FileKit · AdMob expect/actual · RevenueCat `purchases-kmp`.

---

## 1. Qué se ha migrado (checklist por fase)

- [ ] **Fase 0 — Fundación**
  - [x] 0.0 Limpieza de boilerplate de la plantilla (App demo, Greeting, tests dummy, drawable demo)
  - [x] 0.1 Catálogo de versiones alineado a FamilyFilm (adaptado: RTDB, purchases-kmp, WorkManager, ExifInterface)
  - [x] 0.2 `:shared` configurado (JVM17, Compose resources, BuildKonfig, Room/KSP por target, framework iOS estático)
  - [x] 0.3 `:androidApp` configurado (google-services + crashlytics, firma desde keystore.properties, manifest, AdMob)
  - [x] 0.4 Esqueleto Koin (`initKoin` idempotente, `platformModule` expect/actual, `MunicionApplication`, init iOS)
  - [x] 0.5 Theme (Color/Theme/Type) + drawables + i18n (es default / en) en composeResources; `Res` en `al.ahgitdevelopment.municion.resources`
  - [~] 0.6 Esqueleto Room KMP → **fusionado en Fase 2** (Room exige ≥1 entidad en `@Database`)
  - **Verificado:** `:androidApp:assembleDebug` ✅ · `:shared:linkDebugFrameworkIosSimulatorArm64` ✅
- [x] **Fase 1 — Capa Firebase común (GitLive)** (Kotlin) + init iOS Swift
  - `CrashReporter`, `AnalyticsTracker`/`FirebaseAnalyticsTracker` (lean), `CurrentUserIdProvider` (GitLive crashlytics/analytics/auth) cableados en Koin
  - `iOSApp.swift`: `FirebaseApp.configure()` + persistencia RTDB + App Check + Analytics
  - **Pendiente acción usuario:** añadir paquetes SPM de Firebase en Xcode + `GoogleService-Info.plist` (ver §4). El datasource RTDB se hace en Fase 3 (con el sync).
  - **Verificado (Kotlin):** Android ✅ · iOS link ✅. (Swift compila al añadir SPM.)
- [x] **Fase 2 — Entidades, DAOs, migraciones Room** (incluye 0.6)
  - 6 entidades a commonMain (sin `@Parcelize`/`Context`/Java dates; `@Serializable` + Room intactos; columnas idénticas para compatibilidad de schema)
  - utils comunes: `Time` (`nowMillis`), `DateUtils` (parse/format/diff "dd/MM/yyyy" con kotlinx-datetime), `NumberFormat`, **`Md5`** (MD5 puro) + `SyncIdGenerator` (v4 con `kotlin.uuid`, v3 byte-idéntico a Java)
  - 6 DAOs (Flow/suspend/@Transaction) + proyecciones `*SyncMeta`
  - `MunicionDatabase` v33 (@ConstructedBy) + 10 migraciones v23→v33 portadas a `SQLiteConnection`; builder con `BundledSQLiteDriver`; Room builder por plataforma (Android `getDatabasePath`, iOS `NSDocumentDirectory`)
  - **Verificado:** `:androidApp:assembleDebug` ✅ · `:shared:linkDebugFrameworkIosSimulatorArm64` ✅ · `:shared:testAndroidHostTest` (golden UUID) ✅
- [x] **Fase 3 — Repositorios + sync + SyncScheduler**
  - `MunicionRtdbDatasource` (GitLive RTDB): escritura tipada de entidad, lectura tipada a DTOs tolerantes
  - `SyncOutboxConfig`/`SyncOutboxEnqueuer`/`SyncOutboxDrainer` (drenado en commonMain), `TolerantParsers` (DTO→entidad), DTOs `@Serializable`
  - 4 repos (write: Room+outbox+drain; read: merge no destructivo, pending-wins, newer-wins)
  - `SyncScheduler` (interfaz común): `AndroidSyncScheduler` (WorkManager: periódico 15min + cleanup diario + on-demand) / `IosSyncScheduler` (foreground + on-demand)
  - `SyncDataUseCase` (download paralelo 4 colecciones), casos de uso Compra (cupo), `ClearLocalDataUseCase`
  - **Verificado:** Android `assembleDebug` ✅ · iOS link ✅ · host tests ✅
- [x] **Fase 4 — Autenticación (GitLive auth)**
  - `FirebaseAuthRepository` (create/signIn/link/migrateFromLegacy/reset/delete/reauth/signOut) sobre GitLive
  - `AuthViewModel` reacciona al `Flow authStateChanged` de GitLive (estados Loading/NotAuthenticated/RequiresMigration/Authenticated)
  - `Legacy{Converter,MigrationHelper}` (v2.x Android-only, código muerto) **descartados**
  - **Verificado:** Android ✅ · iOS link ✅
- [x] **Fase 5 — Navegación MP** — `NavRoutes` `@Serializable` con `id` (sin NavType custom Parcelable). NavHost real va con las pantallas (Fase 6).
- [x] **Fase 6 — Features por entidad (UI completa)**
  - ViewModels lista + `MainViewModel` + `EntityUiState`; componentes compartidos (TopBar/BottomBar/FAB/DatePicker MP/Dropdown/Delete/EmptyState/DataQualityBanner) con APIs Compose MP
  - pantallas lista + items (Coil3, swipe-delete, barra cupo) + diálogos de selección
  - formularios de las 4 entidades (State+ViewModel+Screen, validación, recálculo caducidad) + `FormUiState`
  - Login + Migration (GitLive auth)
  - `MainScreen` (Scaffold único) + `MunicionNavHost` (rutas por `id`) + `App()` gate de auth
  - **Imagen diferida a Fase 7** (en edición se preserva `fotoUrl`/`storagePath`); calibre con dropdown (sin autocomplete); algunas labels de auth/forms en texto literal (no externalizadas aún)
  - **Verificado:** Android ✅ · iOS link ✅ · app navegable end-to-end
  - *Subagentes:* listas (Guia/Compra/Tirada) y formularios (Guia/Compra/Tirada) portados por agentes en paralelo, integrados y compilados centralmente.
- [~] **Fase 7 — Plataforma (parcial)**
  - [x] Banner AdMob `expect/actual` (`ads/AdaptiveBanner.kt` + Android `AdView` en `AndroidView` con test unit id / iOS stub no-op), gateado por `RemoveAdsManager.hasRemovedAds`
  - [x] `RemoveAdsManager` (interfaz común + `NoOpRemoveAdsManager`) cableado en Koin
  - [x] Imagen: picker de galería FileKit (común, sin cinterop) + `ImageProcessor` expect/actual (Android: EXIF + downscale + JPEG; iOS: passthrough) + `StorageData` expect/actual (ByteArray→GitLive `Data`) + `ImageStorageRepository` (sube a `v3_userdata/{uid}/{armas|licencias|compras}/{syncId}.jpg`, devuelve downloadUrl+path). `ImagePickerField` en los 3 formularios (Guía/Licencia/Compra); subida al guardar; FileKit Core init en `MunicionApplication`. *(Cámara directa diferida; la galería del sistema cubre captura en ambas plataformas.)*
  - [ ] RevenueCat `purchases-kmp` (compra/restore + reconcile `AppPurchase`/`ads_removed`) — **pendiente** (spike de link iOS antes)
  - [ ] Calendario `expect/actual` (CalendarContract / EventKit) — **pendiente**
  - [ ] FCM (servicio Android / coordinator iOS + APNs) — **pendiente**
  - **Verificado (lo hecho):** Android ✅ · iOS link ✅
- [x] **Fase 8 — Settings, tutorial, calidad de datos**
  - `AccountSettingsScreen` + `AccountSettingsViewModel` (sign-out, delete, clear-local, force-sync, remove-ads placeholder)
  - `TutorialDialog` paginado (composeResources es/en), `DataQualityBanner` con conteos `needsAttention`
  - **Verificado:** Android ✅ · iOS link ✅
- [x] **Fase 9 — Tests (commonTest)**
  - `SyncIdGeneratorTest` (golden UUID v3 + v4), `SyncOutboxConfigTest`, `TolerantParsersTest` (round-trip DTO→entidad ok/degraded/lost)
  - mokkery no puede mockear repos `final` → `CreateCompraUseCaseTest` descartado (documentado: extraer interfaces de repo como mejora futura)
  - `iosSimulatorArm64Test` no enlaza (GitLive exige frameworks Firebase, solo en Xcode SPM) → validado vía host test Android (la lógica es común)
  - **Verificado:** `:shared:testAndroidHostTest` ✅
- [~] **Fase 10 — CI/CD (en curso)**
  - GitHub Actions: `build.yml` (ktlint + android assembleDebug/test + iOS framework link), `deploy-android.yml`, `deploy-ios.yml`
  - Fastlane: `androidApp/fastlane/{Appfile,Fastfile}`, `iosApp/fastlane/{Appfile,Matchfile,Fastfile}`, `Gemfile`
  - `.editorconfig` (`ktlint_standard_function-naming = disabled` para @Composable/factories PascalCase); `:shared:ktlintCheck` ✅
  - **Verificado:** `ktlintCheck` ✅ · Android `assembleDebug` ✅ · iOS link ✅ (los workflows se validan al primer push con los secrets configurados)
- [ ] Fase 11 — Finalización Xcode (SPM, Info.plist, entitlements, iconos)

---

## 2. Problemas encontrados (y resolución)

| # | Problema | Resolución |
|---|----------|------------|
| 1 | AGP 9: `resValue` falla con "feature is disabled" | Habilitado `buildFeatures { resValues = true }` en `:androidApp`. |
| 2 | Deps gestionadas por BOM sin versión en `:androidApp` (`koin-android`, `firebase-appcheck-debug`) | Importado el BOM de Koin en `:androidApp`; App Check Debug diferido a Fase 1 (con firebase BOM). |
| 3 | `play-services-ads` crashea al arrancar si `AdMob APPLICATION_ID` está vacío | `resValue admob_app_id` con fallback al **AdMob test app id** hasta tener el real. |
| 4 | Sin `google-services.json` (proyecto `municion-95caa` no accesible) | Creado **placeholder** gitignored para builds locales; el real lo aporta el usuario / CI (ver §4). |
| 5 | `keystore.properties` no estaba en `.gitignore` (contiene contraseñas) | Añadido a `.gitignore`. |
| 6 | `.mcp.json` con tokens GitHub en claro, `.backups/` (RTDB prod) y `docs/affected_users.txt` (PII) sin trackear | Excluidos del commit + `.gitignore`. **Rotar tokens GitHub (acción del usuario).** |
| 7 | `UUID.nameUUIDFromBytes` (UUID v3/MD5) no existe en KMP | MD5 puro en Kotlin (`util/Md5.kt`) + golden test contra vectores de Java. ✅ idéntico. |
| 8 | `getAllTimestamps()` usaba `Map`/`@MapColumn` (soporte dudoso en Room KMP, además @deprecated) | Eliminado de los DAOs (el sync v3.5+ usa `getAllSyncMetadata`). |
| 9 | `Dispatchers.IO` es JVM-only (interno en Native) | `setQueryCoroutineContext(Dispatchers.Default)` (multiplataforma). |
| 10 | Lectura RTDB cruda corrompería datos en iOS (`NSNumber` no es `kotlin.Number`) | Lectura con **deserialización tipada GitLive a DTOs tolerantes**; `TolerantParsers` opera sobre el DTO. (mejora) |
| 11 | `withTransaction` (room-ktx) es Android-only; no hay transacción cross-DAO en Room KMP | insert+enqueue **secuencial** (Room es source of truth; sin pérdida — entidad reenviada en siguiente edición si el proceso muere entre medias). Endurecer con `useWriterConnection` más adelante. |
| 12 | `SyncDataUseCase` dependía de auth/billing/FirebaseFormatMigrator (otras fases) | Simplificado a 4 repos + `CurrentUserIdProvider` + `SyncScheduler`. Reconcile de ads se cablea en Fase 7; `FirebaseFormatMigrator` descartado (parsing tolerante + syncId determinista lo subsumen). |
| 13 | `@HiltWorker` (Hilt) | Workers `CoroutineWorker` + `KoinComponent` (factory por defecto de WorkManager; sin Configuration.Provider). |
| 14 | `iosApp/iosApp/GoogleService-Info.plist` se generó en Fase 8 con `API_KEY`/`GOOGLE_APP_ID`/`*CLIENT_ID` **inventados** (la app iOS no está registrada y `municion-95caa` no es accesible) | Reescrito como **placeholder explícito**: valores reales y derivables conservados (`PROJECT_ID`/`GCM_SENDER_ID`/`BUNDLE_ID`/`STORAGE_BUCKET`/`DATABASE_URL`, contrastados con el `google-services.json` real de `develop`); credenciales específicas de iOS → `REPLACE_WITH_REAL_IOS_*` + comentario de cabecera. El usuario debe registrar la app iOS y sustituir el fichero por el real (§4). |

**Avisos (no bloqueantes, vigilar):**
- Skiko: `coil3 3.4.0` arrastra skiko 0.9.22.2 vs Compose MP 0.144.6 (resuelve a la mayor; vigilar render de imágenes en iOS).
- `androidLibrary {}` deprecado a favor de `android {}` en el plugin KMP-library (solo warning; mantenido como lo generó el wizard).
- iOS framework: bundleId no inferido (cosmético; se puede fijar con `-Xbinary=bundleId`).

---

## 3. Mejoras hechas (no rompedoras)

- `deterministicSyncId` se replanteará con MD5/v3 multiplataforma + golden tests (Fase 3) para garantizar convergencia cross-device (Java `UUID.nameUUIDFromBytes` no existe en KMP).
- Navegación: se sustituirá el paso de entidad completa (Parcelable+Base64, Android-only) por paso de `id`/`syncId` y carga en el ViewModel (Fase 5).
- Eliminadas dependencias declaradas sin uso (biometric, security-crypto) — pendiente confirmar.
- Fuentes **Raleway omitidas**: el theme de `develop` (`Type.kt`) usa `FontFamily.Default`, no las fuentes de assets (eran del XML antiguo). Sin pérdida visual.
- `selector.xml` (drawable `<selector>` del bottom-nav XML antiguo) no portado (Compose no usa state-list drawables).

---

## 4. Guías de configuración externa (paso a paso)

> Todo lo de esta sección es **acción del usuario** (consolas externas / Xcode). El código ya está
> preparado para consumir estos valores; aquí solo hay que provisionarlos. Orden recomendado:
> A → B → C → (D, E en paralelo) → F → G.

### A. Firebase (proyecto `municion-95caa`)

> ⚠️ El proyecto **no es accesible** desde la cuenta Firebase CLI/MCP actual → todo esto lo hace el dueño.

1. **Registrar la app iOS:** Firebase console → ⚙️ → *Tus apps* → *Añadir app* → **iOS**.
   - *Apple bundle ID*: `al.ahgitdevelopment.municion` (idéntico al Android para reusar proyecto).
   - Descargar **`GoogleService-Info.plist`** y **reemplazar** `iosApp/iosApp/GoogleService-Info.plist`
     (el del repo es un **placeholder** con `REPLACE_WITH_REAL_IOS_*`, ver Problema #14).
2. **Refrescar `google-services.json` (Android):** *Añadir app* → Android (o usar la existente) → descargar
   → colocar en `androidApp/google-services.json` (gitignored). Para CI: `base64 -i androidApp/google-services.json | tr -d '\n'` → secret **`FIREBASE_JSON`**.
3. **Habilitar servicios:** Authentication (*Email/Password* **y** *Anónimo*) · Realtime Database (reglas
   `users/{uid}` autenticado) · Storage · Crashlytics · Analytics · Cloud Messaging.
4. **FCM/APNs (iOS):** Cloud Messaging → *Apple app configuration* → subir la **APNs Auth Key (.p8)** (Key ID + Team ID).
5. **App Check (opcional v1):** registrar *App Attest* (iOS) y *Play Integrity* (Android); en debug, el token de depuración.

### B. AdMob

> Los IDs **Android** ya están en `strings.xml` (reales, portados de `develop`). Faltan los de **iOS**.

1. AdMob console → crear/usar app **iOS** (bundle `al.ahgitdevelopment.municion`) y app **Android**.
2. Crear unidad **Banner** por plataforma. Apuntar:
   - Android: `ADMOB_APPLICATION_ID` (`ca-app-pub-…~…`), `ADMOB_BOTTOM_BANNER_ID` (`ca-app-pub-…/…`).
   - iOS: `ADMOB_APPLICATION_ID_IOS`, `ADMOB_BOTTOM_BANNER_ID_IOS`.
3. Local: añadirlos a `local.properties` (los lee BuildKonfig en `:shared`). CI: como secrets (tabla G).
4. iOS: el `GADApplicationIdentifier` va en `Info.plist` (paso F) y los paquetes SPM de AdMob/UMP (paso F).
   *(Hoy el banner Android usa el test unit id `…/6300978111`; el real entra al cablear BuildKonfig→AdaptiveBanner.)*

### C. RevenueCat (compra "quitar anuncios")

1. RevenueCat → crear proyecto; añadir app **Google Play** y app **App Store**.
2. Crear el producto no consumible *remove-ads* **en ambas stores** (Play Console + App Store Connect) y
   asociarlo a un **entitlement** llamado **`ad_free`**; crear un *offering* por defecto.
3. Copiar las API keys → secrets **`REVENUECAT_PLAY_SDK_KEY`**, **`REVENUECAT_PLAY_SDK_KEY_TEST`**, **`REVENUECAT_APPSTORE_SDK_KEY`**.
4. **Sin paso en Xcode/SPM:** la dependencia iOS la trae `purchases-kmp` vía Gradle (cinterop pregenerado).
   Requisitos ya satisfechos en el proyecto: Kotlin ≥2.3.20, framework iOS `isStatic`, `launchMode` standard/singleTop.

### D. Apple Developer / App Store Connect

1. Apple Developer → *Identifiers* → App ID `al.ahgitdevelopment.municion` con capabilities **Push Notifications** y **App Attest**.
2. App Store Connect → crear la app (mismo bundle id) + el IAP no consumible *remove-ads*.
3. **API key** (*Users and Access → Integrations → App Store Connect API*): generar `.p8` → secrets
   **`APP_STORE_CONNECT_API_KEY_ID`**, **`APP_STORE_CONNECT_API_KEY_ISSUER_ID`**, **`APP_STORE_CONNECT_API_KEY_BASE64`** (`base64 -i AuthKey_XXXX.p8`).
4. **fastlane match:** crear un repo git **privado** para certificados → secret **`MATCH_GIT_URL`**; elegir
   **`MATCH_PASSWORD`**; PAT del repo en base64 (`echo -n user:token | base64`) → **`MATCH_GIT_BASIC_AUTHORIZATION`**.
   Bootstrap local una vez: `cd iosApp && bundle exec fastlane match appstore`.
5. IDs de equipo → **`FASTLANE_TEAM_ID`** (Developer Portal), **`FASTLANE_ITC_TEAM_ID`** (ASC), **`FASTLANE_APPLE_ID`** (email de la cuenta).

### E. Google Play Console

1. Play Console → *Setup → API access* → crear/enlazar una **service account** con permiso de *release* (Internal testing).
2. Descargar su JSON → secret **`SERVICE_ACCOUNT_GOOGLE_PLAY_CONSOLE_JSON`** (el workflow lo escribe en `androidApp/play-service-account.json`).
3. Crear el producto gestionado *remove-ads* (espejo del de RevenueCat).

### F. Xcode (proyecto `iosApp`) — Fase 11

1. **SPM** (File → Add Package Dependencies), target `iosApp`:
   - `https://github.com/firebase/firebase-ios-sdk` → `FirebaseCore, FirebaseAuth, FirebaseDatabase, FirebaseStorage, FirebaseCrashlytics, FirebaseAnalytics, FirebaseMessaging, FirebaseAppCheck`.
   - `https://github.com/googleads/swift-package-manager-google-mobile-ads` + `…/swift-package-manager-google-user-messaging-platform`.
   - **RevenueCat NO se añade aquí** (lo gestiona Gradle vía `purchases-kmp`).
2. **`GoogleService-Info.plist`** real (paso A.1) arrastrado a `iosApp/iosApp/` con *Target Membership = iosApp*.
3. **`Config.xcconfig`**: `DEVELOPMENT_TEAM = <TEAM_ID>`, `PRODUCT_BUNDLE_IDENTIFIER = al.ahgitdevelopment.municion`, `MARKETING_VERSION = 4.0.0`.
4. **Run Script Phase** (antes de *Compile Sources*): `./gradlew :shared:embedAndSignAppleFrameworkForXcode`.
5. **`Info.plist`**: `GADApplicationIdentifier` (= AdMob iOS app id) · `NSCameraUsageDescription` ·
   `NSPhotoLibraryUsageDescription` · `NSCalendarsUsageDescription` · `NSUserTrackingUsageDescription` (ATT) ·
   `UIBackgroundModes` → `remote-notification`.
6. **Entitlements** (`iosApp.entitlements`): `aps-environment` (push) · App Attest.
7. **App icon** en `Assets.xcassets/AppIcon` · **StoreKit** config (`*.storekit`) para probar el IAP en simulador.

### G. GitHub secrets (Settings → Secrets and variables → Actions)

| Secret | Lo usa | Cómo obtenerlo |
|---|---|---|
| `FIREBASE_JSON` | build, deploy-android | `base64` del `google-services.json` (A.2) |
| `ADMOB_APPLICATION_ID`, `ADMOB_BOTTOM_BANNER_ID` | build, deploy-android | AdMob Android (B) |
| `ADMOB_APPLICATION_ID_IOS`, `ADMOB_BOTTOM_BANNER_ID_IOS` | build, deploy-ios | AdMob iOS (B) |
| `REVENUECAT_PLAY_SDK_KEY`, `REVENUECAT_PLAY_SDK_KEY_TEST` | build, deploy-android | RevenueCat (C) |
| `REVENUECAT_APPSTORE_SDK_KEY` | build, deploy-ios | RevenueCat (C) |
| `SIGNING_KEY_B64`, `SIGNING_KEY_STORE_PASSWORD`, `SIGNING_ALIAS`, `SIGNING_KEY_PASSWORD` | deploy-android | keystore de release (`base64 -i release.keystore`) |
| `SERVICE_ACCOUNT_GOOGLE_PLAY_CONSOLE_JSON` | deploy-android | Play Console (E) |
| `APP_STORE_CONNECT_API_KEY_ID`, `…_ISSUER_ID`, `…_BASE64` | deploy-ios | ASC API key (D.3) |
| `MATCH_GIT_URL`, `MATCH_PASSWORD`, `MATCH_GIT_BASIC_AUTHORIZATION` | deploy-ios | fastlane match (D.4) |
| `FASTLANE_TEAM_ID`, `FASTLANE_ITC_TEAM_ID`, `FASTLANE_APPLE_ID` | deploy-ios | Apple (D.5) |

> `build.yml` corre en cada PR/push (ktlint + Android + framework iOS). `deploy-*` corren al pushear un tag `v*`.
> *(No hacen falta TMDB/WEB_ID_CLIENT: v3.4 quitó el login con Google.)*

### H. Desarrollo local (`local.properties`, gitignored)

```properties
# Firma release (opcional en local; el debug se autofirma)
# y BuildKonfig:
ADMOB_APPLICATION_ID=ca-app-pub-XXXX~XXXX
ADMOB_BOTTOM_BANNER_ID=ca-app-pub-XXXX/XXXX
ADMOB_APPLICATION_ID_IOS=ca-app-pub-XXXX~XXXX
ADMOB_BOTTOM_BANNER_ID_IOS=ca-app-pub-XXXX/XXXX
REVENUECAT_PLAY_SDK_KEY=goog_XXXX
REVENUECAT_PLAY_SDK_KEY_TEST=goog_XXXX
REVENUECAT_APPSTORE_SDK_KEY=appl_XXXX
```
Además: `androidApp/google-services.json` (real) y `keystore.properties` para builds de release locales. **Todos gitignored.**
