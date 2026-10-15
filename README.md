# EKO · Reproductor musical local

Aplicación nativa Android en Kotlin y Jetpack Compose. Selecciona una carpeta, organiza su audio real y lo reproduce con controles propios y del sistema, incluso en segundo plano. Todo funciona sin conexión.

## Abrir y ejecutar

1. Abre esta carpeta con **Open** en una versión de Android Studio compatible con AGP 9.4.
2. Selecciona JDK 21 como **Gradle JDK** (la compilación se verificó con Java 21). Instala Android SDK Platform 37 y Build Tools 36 desde SDK Manager si faltan.
3. Sincroniza Gradle. El wrapper descarga Gradle 9.6.0 con comprobación SHA-256. Las versiones están fijadas en `gradle/libs.versions.toml`.
4. Elige un dispositivo Android 8.0 / API 26 o superior y ejecuta la configuración `app`.
5. Copia archivos de audio a una carpeta del teléfono, por ejemplo `Music/Mi colección`, y pulsa **Seleccionar mi música**. Concede acceso a esa carpeta en el selector de Android.

La primera sincronización necesita internet para descargar herramientas/dependencias; **la aplicación instalada funciona offline**. `local.properties` contiene una ruta local ignorada por Git; Android Studio la configura para cada equipo.

En PowerShell, desde la raíz:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

En este equipo también quedó disponible el emulador **Eko_API_35** en Device Manager.

## Funciones

- Bienvenida e identidad oscura EKO; tipografía Plus Jakarta Sans empaquetada, sin fuentes remotas.
- Selector `ACTION_OPEN_DOCUMENT_TREE`, permiso persistente solo de lectura y URI/nombre en DataStore. Cancelar el selector conserva el estado.
- Recorrido de subcarpetas en `Dispatchers.IO`. DocumentFile valida la raíz; DocumentsContract consulta los atributos con un cursor por directorio.
- Filtrado por MIME/extensión para MP3, FLAC, WAV, OGG, M4A, AAC y Opus. MediaMetadataRetriever valida que exista audio. El soporte efectivo depende de los códecs del dispositivo.
- Título, artista, álbum, duración, género, año, pista, archivo y URI. Valores legibles de respaldo; archivos ilegibles omitidos individualmente.
- Carátulas incrustadas reducidas y guardadas por hash, con Coil para carga y caché en memoria. Imagen de respaldo si faltan o no pueden decodificarse.
- Biblioteca adaptable con vistas de Canciones y Álbumes. La cuadrícula de álbumes agrupa por disco y artista, abre sus pistas y usa su propia cola; la lista permite buscar por título/artista/álbum sin volver al almacenamiento y ordenar por título, duración o modificación descendente.
- Progreso con archivos revisados y canciones encontradas, cancelación, actualización manual, información de canción y ajustes de carpeta.
- Estados sin carpeta, vacío, escaneando, sin coincidencias y acceso perdido; transiciones breves y movimiento reducido según Android.
- Índice Room persistente. Cada escaneo escribe lotes de hasta 50 filas en una generación temporal. Solo se publica al completar la exploración: cancelar o fallar conserva el índice anterior. Una actualización exitosa elimina referencias a archivos borrados.
- Un único ExoPlayer alojado en `MediaSessionService`, separado de la UI y sincronizado mediante `MediaController` + `StateFlow`.
- Reproducción, pausa, búsqueda temporal, anterior, siguiente, cola, modo aleatorio, repetir una y repetir todo.
- Minirreproductor persistente dentro de la navegación y reproductor completo adaptable, con cola consultable y carátula protagonista.
- Pantalla de Inicio con estadísticas de uso: reproducciones acumuladas, canciones escuchadas, artista y género más escuchados, lo más escuchado, un top 10 y una recomendación de playlist derivada de tu artista o género favorito. Cada contador se guarda por `uri` y sobrevive a los reescaneos y cambios de carpeta.
- Reproducción en segundo plano, audio focus, pausa al desconectar la salida de audio y controles de notificación, pantalla bloqueada, auriculares y Bluetooth compatibles.
- Recuperación por archivo ilegible: se informa el error y, si hay otra pista disponible, la cola continúa.

No se solicitan permisos de almacenamiento global, audio multimedia ni internet. Android 13 o posterior solicita notificaciones al iniciar la primera reproducción; rechazarlo no bloquea el audio. No hay servidores, cuentas, streaming, favoritos ni listas de reproducción creadas o guardadas por el usuario.

## Estructura y responsabilidades

```text
app/src/main/java/com/musicplayer/
├── MainActivity.kt               Selector SAF y ciclo de vida
├── EkoApplication.kt             Construcción de dependencias
├── data/
│   ├── model/Song.kt             Canción, carpeta, orden e historial de reproducción
│   ├── local/                    DataStore, Room (índice y estadísticas) y transacción del índice
│   └── repository/               Permisos, escaneo, índice y registro de reproducciones
├── domain/                      Metadatos, formatos, búsqueda, orden y estadísticas de uso
├── scanner/                     Exploración, MediaMetadataRetriever y miniaturas
├── playback/                    ExoPlayer, MediaSessionService, controlador y estado
├── viewmodel/                   Estado StateFlow y operaciones cancelables
└── ui/
    ├── theme/                   Color.kt, Theme.kt, Type.kt, Motion.kt
    ├── components/              Marca, iconos, filas, minirreproductor, controles y estadísticas
    ├── screens/                 Bienvenida, inicio, biblioteca, reproductor, ajustes e información
    └── navigation/              Navigation Compose
```

La UI recibe estado y callbacks; no lee archivos ni controla ExoPlayer directamente. `PlaybackController` traduce el estado de la sesión multimedia para Compose y `PlaybackService` conserva el único motor fuera de la navegación.

## Diseño e integración de skills

Se aplicaron `frontend-design` y `motion-design` tras confirmar el alcance completo. Los puntos de integración están identificados en `Theme.kt` y `Motion.kt` para ajustes posteriores.

- Paleta oficial centralizada en `Color.kt`, sin colores dinámicos del fondo de pantalla.
- Degradado reservado a la marca y su ilustración de bienvenida; carátulas sin filtros decorativos.
- Plus Jakarta Sans local y licencia SIL OFL incluida en `docs/PlusJakartaSans-OFL.txt`.
- Bienvenida dividida a partir de 700 dp, contenido con ancho máximo y desplazamiento para pantallas pequeñas/texto grande.
- Acciones con áreas táctiles Material, etiquetas accesibles y errores explicados con texto.
- Duraciones de 160/240 ms. Android controla la escala temporal de Compose; con animaciones desactivadas se omiten desplazamientos, fades y progreso indeterminado animado.
- El reproductor utiliza transiciones de 320 ms para la entrada y el cambio de carátula, feedback de presión de 140 ms y una composición compacta específica para pantallas de poca altura.
- El selector de carpetas pertenece a Android: respeta su apariencia y restricciones del sistema.

## Verificación y límites

Consulta `docs/VERIFICATION.md` para resultados, capturas y matriz de pruebas. Las muestras y proveedores ficticios están únicamente en pruebas; **el APK no contiene canciones de demostración**.

- El escaneo pertenece al ViewModel: sobrevive a cambios de configuración, pero se cancela al finalizar el proceso. El siguiente inicio conserva el último índice completo; si todavía no existe, inicia otro escaneo.
- SAF no ofrece un observador universal fiable. Los cambios de contenido se incorporan con **Actualizar biblioteca**; el permiso/directorio se comprueba también al volver a la app.
- Las llamadas nativas de proveedores y MediaMetadataRetriever no siempre son interrumpibles. Cancelar se hace efectivo al terminar la llamada en curso, sin bloquear la UI.
- Si un directorio no puede enumerarse, se conserva el índice anterior para no confundir un fallo con una carpeta vacía.
- Carátulas guardadas en alta resolución (hasta 1280 px en el lado mayor, WebP ~92) para que el reproductor, el fondo a pantalla completa y la notificación del sistema se vean nítidos; las miniaturas de la lista las reduce Coil al dibujarlas. Presupuesto de aproximadamente 128 MiB y limpieza de archivos sin referencias. Se descartan carátulas comprimidas mayores de 16 MiB; MediaMetadataRetriever puede asignar transitoriamente el bloque incrustado antes de conocer su tamaño.
- Las estadísticas de uso viven en la tabla Room `play_stats`, con `uri` como clave, separada de las generaciones del índice. La migración 1 → 2 es aditiva (crea la tabla sin tocar canciones ni índice) y se comprobó sobre una instalación real con más de mil pistas: el índice se conserva y los contadores empiezan en cero.
- Una reproducción se cuenta cuando una canción empieza a sonar (inicio manual, avance automático o cambio de pista) y no al reanudar la misma pista. Reconectar con un servicio que ya está sonando no infla el contador.
- No se ha medido FPS en hardware físico ni se garantiza compatibilidad con todos los proveedores SAF o códecs.
- El servicio conserva la sesión durante la reproducción y las pausas recientes. Una detención forzada de la app por el usuario borra la sesión activa; no se implementó reanudación automática tras *force stop*.
- Las llamadas, controles Bluetooth/auriculares y sesiones de muchas horas dependen de hardware real y deben verificarse en los dispositivos objetivo.

Referencias de compatibilidad: [AGP 9.4](https://developer.android.com/build/releases/agp-9-4-0-release-notes), [versiones AndroidX](https://developer.android.com/jetpack/androidx/versions), [Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files), [Coil](https://coil-kt.github.io/coil/getting_started/). Los artefactos se comprobaron también en Google Maven y Maven Central.
