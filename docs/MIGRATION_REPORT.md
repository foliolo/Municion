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
  - [ ] 0.5 Theme + fuentes + drawables + i18n en composeResources (gate CMP-9547)
  - [ ] 0.6 Esqueleto Room KMP (DatabaseBuilder expect/actual)
  - **Verificado:** `:androidApp:assembleDebug` ✅ · `:shared:linkDebugFrameworkIosSimulatorArm64` ✅
- [ ] Fase 1 — Capa Firebase común (GitLive) + init iOS
- [ ] Fase 2 — Entidades, DAOs, migraciones Room
- [ ] Fase 3 — Repositorios + sync + SyncScheduler
- [ ] Fase 4 — Autenticación
- [ ] Fase 5 — Navegación MP
- [ ] Fase 6 — Features por entidad
- [ ] Fase 7 — Plataforma (imagen, AdMob, RevenueCat, calendario, FCM)
- [ ] Fase 8 — Settings, tutorial, calidad de datos
- [ ] Fase 9 — Tests
- [ ] Fase 10 — CI/CD
- [ ] Fase 11 — Finalización Xcode

---

## 2. Problemas encontrados (y resolución)

| # | Problema | Resolución |
|---|----------|------------|
| 1 | AGP 9: `resValue` falla con "feature is disabled" | Habilitado `buildFeatures { resValues = true }` en `:androidApp`. |
| 2 | Deps gestionadas por BOM sin versión en `:androidApp` (`koin-android`, `firebase-appcheck-debug`) | Importado el BOM de Koin en `:androidApp`; App Check Debug diferido a Fase 1 (con firebase BOM). |
| 3 | `play-services-ads` crashea al arrancar si `AdMob APPLICATION_ID` está vacío | `resValue admob_app_id` con fallback al **AdMob test app id** hasta tener el real. |
| 4 | Sin `google-services.json` (proyecto `municion-95caa` no accesible) | Creado **placeholder** gitignored para builds locales; el real lo aporta el usuario / CI (ver §4). |
| 5 | `keystore.properties` no estaba en `.gitignore` (contiene contraseñas) | Añadido a `.gitignore`. |

**Avisos (no bloqueantes, vigilar):**
- Skiko: `coil3 3.4.0` arrastra skiko 0.9.22.2 vs Compose MP 0.144.6 (resuelve a la mayor; vigilar render de imágenes en iOS).
- `androidLibrary {}` deprecado a favor de `android {}` en el plugin KMP-library (solo warning; mantenido como lo generó el wizard).
- iOS framework: bundleId no inferido (cosmético; se puede fijar con `-Xbinary=bundleId`).

---

## 3. Mejoras hechas (no rompedoras)

- `deterministicSyncId` se replanteará con MD5/v3 multiplataforma + golden tests (Fase 3) para garantizar convergencia cross-device (Java `UUID.nameUUIDFromBytes` no existe en KMP).
- Navegación: se sustituirá el paso de entidad completa (Parcelable+Base64, Android-only) por paso de `id`/`syncId` y carga en el ViewModel (Fase 5).
- Eliminadas dependencias declaradas sin uso (biometric, security-crypto) — pendiente confirmar.

---

## 4. Guías de configuración externa (paso a paso)

> Pendiente de completar al avanzar las fases. Resumen de lo necesario:

- **Firebase** (`municion-95caa`, no accesible desde la cuenta CLI/MCP actual): registrar app iOS (bundle `al.ahgitdevelopment.municion`) → `GoogleService-Info.plist`; aportar `google-services.json` real (Android) → reemplaza el placeholder y se publica como secret `FIREBASE_JSON` (base64); habilitar RTDB/Storage/Auth(Email+Anónimo)/Crashlytics/Analytics/FCM.
- **AdMob:** apps iOS+Android, unidades banner → `ADMOB_APPLICATION_ID(_IOS)`, `ADMOB_BOTTOM_BANNER_ID(_IOS)` en `local.properties`/secrets.
- **RevenueCat:** proyecto, productos remove-ads (Play + App Store) → entitlement `ad_free`; claves SDK.
- **Apple Developer / App Store Connect, Google Play Console, GitHub secrets, Xcode (SPM Firebase/AdMob), keystore:** detalle en fases 7/10/11.
