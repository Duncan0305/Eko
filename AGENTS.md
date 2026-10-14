# AGENTS.md — Guía de trabajo para Eko

Este archivo lo carga OpenCode automáticamente en cada sesión. Mantén aquí solo lo esencial y estable.

## Qué es Eko

- Reproductor de música **local** para Android: Kotlin + Jetpack Compose + Material 3 personalizado.
- Arquitectura **MVVM + Repository**, estado con **StateFlow**, asincronía con **Coroutines**, preferencias con **DataStore**, índice con **Room**, lectura de archivos por **SAF** (`ACTION_OPEN_DOCUMENT_TREE`) y **DocumentFile**, metadatos con **MediaMetadataRetriever**, carátulas con **Coil**.
- **Fase 1 (completa):** seleccionar una carpeta, escanear audio real, extraer metadatos y mostrar la biblioteca (búsqueda, ordenamiento, persistencia). **NO hay reproducción de audio.**
- **100% offline**: sin backend, cuentas, streaming ni permisos de almacenamiento global/internet.
- **Fase 2 (pendiente):** integrar el motor de reproducción (Media3/ExoPlayer). Punto de integración previsto: un `PlaybackController` que reciba `Song.uri`, sin meter el motor en el escáner ni en los componentes Compose.

El prompt y los requisitos originales de la Fase 1 están en `docs/PHASE-1-PROMPT.md`. El `README.md` describe funciones, estructura y límites en detalle.

## Identificadores y versiones

- `namespace` / paquete `R` = `com.musicplayer` · `applicationId` = `com.musicplayer.eko` · `versionName` 0.1.0.
- `minSdk` 26 · `compileSdk`/`targetSdk` 37 · Java 17 (source/target).
- Dependencias fijadas en `gradle/libs.versions.toml`: AGP **9.4.1**, Gradle **9.6.0**, Kotlin **2.4.21**, KSP **2.3.12**, Compose BOM **2026.09.00**, Room **2.8.5**, DataStore **1.2.1**, DocumentFile **1.1.0**, Coil **3.6.3**, Navigation **2.10.2**, Coroutines **1.11.0**.
- Requiere **Android Studio Rabbit 1 (2026.2.1)** o superior. **Quail 2 (2026.1.2) NO soporta AGP 9.4** → síntoma: no aparece el botón verde *Run*.
- **Gradle JDK: 21.** Build Tools 36.0.0 y Platform 37 instalados.

## Comandos (PowerShell, desde la raíz del proyecto)

```powershell
.\gradlew.bat :app:assembleDebug          # compilar
.\gradlew.bat :app:testDebugUnitTest      # tests JVM/Robolectric
.\gradlew.bat :app:lintDebug              # lint (ver limitación abajo)
```

- APK: `app/build/outputs/apk/debug/app-debug.apk`.
- adb: `C:\Users\user\AppData\Local\Android\Sdk\platform-tools\adb.exe`.
- Dispositivo físico: serial `R5GL107YFYA` (Samsung **SM-S721B**, Android 16 / API 36).
- Emulador disponible: `Eko_API_35`.

```powershell
& "C:\Users\user\AppData\Local\Android\Sdk\platform-tools\adb.exe" -s R5GL107YFYA install -r "app\build\outputs\apk\debug\app-debug.apk"
```

## Arquitectura (dónde va cada cosa)

```text
app/src/main/java/com/musicplayer/
├── MainActivity.kt          Selector SAF y ciclo de vida
├── EkoApplication.kt        Construcción de dependencias
├── data/
│   ├── model/Song.kt        Canción, carpeta y orden
│   ├── local/               DataStore (FolderPreferences), Room (MusicDatabase) y transacción del índice
│   └── repository/          MusicRepository: permisos, escaneo e índice
├── domain/                  Lógica pura y testeable: MetadataMapper, LibraryOrganizer, MusicFiles
├── scanner/                 MusicScanner, MetadataExtractor, ArtworkCache
├── viewmodel/               LibraryViewModel: estado StateFlow y operaciones cancelables
└── ui/
    ├── theme/               Color.kt, Theme.kt, Type.kt, Motion.kt
    ├── components/          Marca (BrandHeader, EkoIcon), SongItem, SongArtwork, SearchBar, FolderHeader, LibraryState
    ├── screens/             WelcomeScreen, LibraryScreen, SettingsScreen, SongDetailsDialog
    └── navigation/          EkoNavigation (Navigation Compose)
```

Reglas de arquitectura:
- **La UI no lee archivos.** Recibe estado + callbacks. Nada de acceso a disco en composables.
- El **escaneo vive en `LibraryViewModel`**: sobrevive cambios de configuración; se cancela al terminar el proceso.
- **Room** guarda solo metadatos/índice (nunca archivos ni imágenes completas). Cada escaneo escribe en una **generación temporal** y solo publica al completar; cancelar/fallar conserva el índice anterior.
- Mantener la lógica de negocio en `domain/` (pura) para poder testearla.

## Convenciones

- Idioma del proyecto: **español** (UI, comentarios, mensajes, docs). Mantener consistencia.
- Estilo Kotlin/Compose propio del proyecto; reutilizar componentes existentes antes de crear nuevos.
- Diseño: paleta centralizada en `ui/theme/Color.kt` (sin colores dinámicos del fondo). Tipografía **Plus Jakarta Sans** local (licencia en `docs/PlusJakartaSans-OFL.txt`), sin fuentes remotas. Movimiento/duraciones en `ui/theme/Motion.kt`, respetando animaciones reducidas del sistema.
- No introducir dependencias innecesarias ni nada fuera del alcance de la fase.

## Marca e identidad

- Ícono de app = `@mipmap/ic_launcher` (mipmaps por densidad generados desde `docs/eko_icon.png`). Fuente original preservada en `docs/eko_icon.png`; copia en `Logo/eko_icon.png`.
- `EkoMark` (`ui/components/EkoIcon.kt`) dibuja `R.mipmap.ic_launcher` recortado en `CircleShape` (logo de bienvenida y encabezado compartido).
- `app_name` = **"Eko"** (solo el nombre externo: launcher, Ajustes, multitarea). Los textos **internos** de la app siguen mostrando **"EKO"** (están escritos en el código).

## Git

- Remoto: `https://github.com/Duncan0305/Eko` (rama `main`). Repo **público**.
- Identidad git local: `Duncan` / `adunroga03@gmail.com`. Credenciales vía Git Credential Manager (usuario **Duncan0305**); no hace falta token manual.
- El `.gitignore` excluye `.gradle/`, `.kotlin/`, `.idea/`, `**/build/`, `local.properties`, `*.iml`, `*.log`, `.tools/`, keystores. **Nunca** commitear `local.properties`, `build/` ni logs.

## Errores conocidos y pendientes

- **Lint crashea** (`lintAnalyzeDebug`) en `scanner/MetadataExtractor.kt`; con `lint { abortOnError = true }` haciendo fallar `lint`/`check`. Pendiente de investigar.
- **No hay `androidTest`**; solo tests JVM/Robolectric en `app/src/test` (`EkoScreensTest`, `MusicScannerTest`, `MetadataMapperTest`, `LibraryOrganizerTest`, `MusicDatabaseTest`, `MusicRepositoryTest`).
- El `README.md` referencia `docs/VERIFICATION.md`, que **no existe** todavía. Crearlo o corregir el enlace.
- Pendiente de confirmación visual: nuevo ícono en bienvenida/encabezado en el dispositivo físico.

## Cómo empezar una sesión

1. Leer `README.md` (funciones, estructura, límites) y, si hace falta el alcance, `docs/PHASE-1-PROMPT.md`.
2. Comprobar `git status` antes de tocar nada; el repo no es de varios colaboradores, pero evita sobrescribir trabajo.
3. Tras cambios de código: compilar (`:app:assembleDebug`) y, cuando aplique, correr `:app:testDebugUnitTest`. Instalar en `R5GL107YFYA` para validar en hardware.
4. No avanzar a reproducción (Fase 2) sin que se pida explícitamente.
