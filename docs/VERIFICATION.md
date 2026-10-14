# Verificación — Eko (Fases 1 y 2)

Resultados de compilación, lint, tests unitarios y pruebas del reproductor local.

- **Fecha de la última verificación automática:** 14 oct 2026.
- **Alcance:** biblioteca local y reproducción Media3 con cola, segundo plano y controles del sistema.

## Entorno

| Elemento | Valor |
|---|---|
| Dispositivo físico | Samsung **SM-S721B** (Android 16 / API 36, arm64-v8a), serial `R5GL107YFYA` |
| Emulador | `Eko_API_35` |
| SDK | compileSdk/targetSdk 37 · minSdk 26 · Build Tools 36.0.0 |
| Toolchain | AGP 9.4.1 · Gradle 9.6.0 · Kotlin 2.4.21 · KSP 2.3.12 · Compose BOM 2026.09.00 |
| Reproductor | Media3 ExoPlayer/Session 1.11.0 |
| JDK | Gradle JDK 21 (Java 17 source/target) |

## Verificación automática (reproducible)

Ejecutado en limpio desde la raíz del proyecto:

```powershell
.\gradlew.bat :app:clean :app:assembleDebug
.\gradlew.bat :app:clean :app:lintDebug
.\gradlew.bat :app:testDebugUnitTest
```

| Tarea | Resultado |
|---|---|
| `:app:assembleDebug` | ✅ BUILD SUCCESSFUL — APK `app/build/outputs/apk/debug/app-debug.apk` |
| `:app:lintDebug` | ✅ BUILD SUCCESSFUL — sin errores (con `lint { abortOnError = true }`) |
| `:app:testDebugUnitTest` | ✅ 29 tests, 0 fallos |

Reporte de lint: `app/build/reports/lint-results-debug.html`.

### Tests unitarios (JVM / Robolectric)

| Suite | Tests | Fallos | Cubre |
|---|---|---|---|
| `MetadataMapperTest` | 5 | 0 | Transformación de metadatos y valores de respaldo |
| `LibraryOrganizerTest` | 3 | 0 | Filtrado, búsqueda y ordenamiento |
| `MusicScannerTest` | 5 | 0 | Formatos admitidos, subcarpetas, duplicados y errores |
| `MusicRepositoryTest` | 2 | 0 | Permisos/índice |
| `MusicDatabaseTest` | 3 | 0 | Índice Room |
| `EkoScreensTest` | 9 | 0 | Estados de biblioteca y reproductor en móvil, compacto y horizontal |
| `PlaybackMediaItemTest` | 2 | 0 | Conservación y valores de respaldo de metadatos de sesión |
| **Total** | **29** | **0** | |

## Estados verificados en dispositivo

Capturas en `docs/screenshots/`:

| Estado | Captura |
|---|---|
| Primera apertura / sin carpeta (bienvenida) | [`screenshots/android-welcome.png`](screenshots/android-welcome.png) |
| Biblioteca cargada | [`screenshots/android-library.png`](screenshots/android-library.png) |
| Escaneo cancelado | [`screenshots/android-scan-cancelled.png`](screenshots/android-scan-cancelled.png) |
| Información de canción | [`screenshots/android-details.png`](screenshots/android-details.png) |
| Error de acceso / permiso revocado | [`screenshots/android-access-error.png`](screenshots/android-access-error.png) |
| Reproductor completo compacto | [`screenshots/android-player.png`](screenshots/android-player.png) |

## Reproducción verificada en emulador

En `Eko_API_35` se instaló el APK sobre una biblioteca SAF ya indexada y se comprobó:

- ✅ Tocar una fila crea una cola real e inicia ExoPlayer desde su URI local.
- ✅ El minirreproductor refleja canción, reproducción, progreso y siguiente pista.
- ✅ La pantalla completa abre sin interrumpir el audio y sus controles quedan visibles a 360 × 640 dp.
- ✅ La sesión del sistema permanece `PLAYING` después de enviar la app al inicio.
- ✅ Los eventos multimedia del sistema cambiaron la sesión a `PAUSED`, reanudaron `PLAYING` y avanzaron del elemento 42 al 43.
- ✅ `MediaSessionService` se mantiene como foreground service de tipo `mediaPlayback` con notificación pública.
- ✅ La sesión publica título, artista, álbum, cola y acciones de reproducir/pausar/anterior/siguiente para controles externos.
- ✅ Una cola de 1002 elementos avanzó automáticamente entre pistas locales de prueba sin crear otro reproductor.

## Matriz de pruebas (Etapa 11)

Escenarios contemplados por el diseño y la implementación. Los marcados con ✅ se validaron en dispositivo; el resto queda como comportamiento cubierto por lógica/tests o pendiente de repetir en hardware.

- ✅ Primera apertura.
- ✅ Selección de carpeta.
- ✅ Cancelación del selector (conserva estado).
- ✅ Carpeta con subcarpetas (recorrido recursivo).
- ✅ Archivos sin metadatos (valores de respaldo).
- ✅ Canciones sin carátula (imagen predeterminada).
- ✅ Archivos dañados (omisión individual, sin detener el escaneo).
- ✅ Bibliotecas grandes (LazyColumn, caché de carátulas).
- ✅ Búsqueda y ordenamiento (sin repetir el escaneo).
- ✅ Funcionamiento offline.
- ⬜ Eliminación de canciones entre escaneos (limpieza del índice al actualizar).
- ⬜ Revocación del permiso de carpeta (re-detección y re-selección).
- ⬜ Reinicio de aplicación (persistencia de la carpeta e índice en DataStore/Room).

### Escenarios de Fase 2 pendientes de hardware

- ⬜ Control mediante auriculares o Bluetooth reales.
- ⬜ Interrupción por llamada telefónica real.
- ⬜ Pantalla de bloqueo de un fabricante concreto.
- ⬜ Reproducción continua durante varias horas y perfil de memoria/batería.
- ⬜ Eliminación o revocación SAF exactamente mientras el archivo afectado se reproduce.
- ⬜ Recuperación después de que Android mate el proceso; un *force stop* del usuario detiene la sesión por diseño del sistema.

## Límites conocidos

- El escaneo pertenece al `LibraryViewModel`: sobrevive a cambios de configuración, pero se cancela al finalizar el proceso. El siguiente inicio conserva el último índice completo; si no existe, arranca otro escaneo.
- SAF no ofrece un observador universal fiable: los cambios de contenido se incorporan con **Actualizar biblioteca**; el permiso/directorio se comprueba también al volver a la app.
- Las llamadas nativas de proveedores y `MediaMetadataRetriever` no siempre son interrumpibles: cancelar se hace efectivo al terminar la llamada en curso, sin bloquear la UI.
- Si un directorio no puede enumerarse, se conserva el índice anterior para no confundir un fallo con una carpeta vacía.
- Miniaturas de hasta 384 px, presupuesto ~96 MiB, limpieza de archivos sin referencias. Se descartan carátulas comprimidas > 16 MiB.
- No se midió FPS en hardware físico ni se garantiza compatibilidad con todos los proveedores SAF o códecs.
- El APK **no contiene canciones de demostración**: las muestras y proveedores ficticios viven solo en los tests.
- La posición se publica a la UI cada 500 ms solo mientras se reproduce. Media3 administra audio focus y la pausa al desconectar una salida de audio.

## Nota sobre lint

Hubo un reporte de crash de `lintAnalyzeDebug` en `scanner/MetadataExtractor.kt`. **No se reproduce** en la verificación actual (incluida una ejecución en limpio). Si vuelve a aparecer, documentarlo aquí con el stacktrace antes de asumir que persiste.
