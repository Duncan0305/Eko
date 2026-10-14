# EKO · Biblioteca musical local

Aplicación nativa Android en Kotlin y Jetpack Compose. Fase 1: selecciona una carpeta, descubre audio real y organiza sus metadatos. **No incluye reproducción.**

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
- Biblioteca con LazyColumn, búsqueda por título/artista/álbum sin volver al almacenamiento, búsqueda insensible a acentos y mayúsculas, orden por título, duración o modificación descendente.
- Progreso con archivos revisados y canciones encontradas, cancelación, actualización manual, información de canción y ajustes de carpeta.
- Estados sin carpeta, vacío, escaneando, sin coincidencias y acceso perdido; transiciones breves y movimiento reducido según Android.
- Índice Room persistente. Cada escaneo escribe lotes de hasta 50 filas en una generación temporal. Solo se publica al completar la exploración: cancelar o fallar conserva el índice anterior. Una actualización exitosa elimina referencias a archivos borrados.

No se solicitan permisos de almacenamiento global, audio multimedia ni internet. No hay servidores, cuentas, streaming, favoritos, playlists o dependencias de reproducción.

## Estructura y responsabilidades

```text
app/src/main/java/com/musicplayer/
├── MainActivity.kt               Selector SAF y ciclo de vida
├── EkoApplication.kt             Construcción de dependencias
├── data/
│   ├── model/Song.kt             Canción, carpeta y orden
│   ├── local/                    DataStore, Room y transacción del índice
│   └── repository/               Permisos, escaneo e índice
├── domain/                      Metadatos, formatos, búsqueda y orden
├── scanner/                     Exploración, MediaMetadataRetriever y miniaturas
├── viewmodel/                   Estado StateFlow y operaciones cancelables
└── ui/
    ├── theme/                   Color.kt, Theme.kt, Type.kt, Motion.kt
    ├── components/              Marca, iconos, carátula, fila, búsqueda y estados
    ├── screens/                 Bienvenida, biblioteca, ajustes e información
    └── navigation/              Navigation Compose
```

La UI recibe estado y callbacks; no lee archivos. Para la Fase 2 se puede añadir un controlador de reproducción separado que reciba `Song.uri`, sin introducir el motor en el escáner ni en los componentes.

## Diseño e integración de skills

Se aplicaron `frontend-design` y `motion-design` tras confirmar el alcance completo. Los puntos de integración están identificados en `Theme.kt` y `Motion.kt` para ajustes posteriores.

- Paleta oficial centralizada en `Color.kt`, sin colores dinámicos del fondo de pantalla.
- Degradado reservado a la marca y su ilustración de bienvenida; carátulas sin filtros decorativos.
- Plus Jakarta Sans local y licencia SIL OFL incluida en `docs/PlusJakartaSans-OFL.txt`.
- Bienvenida dividida a partir de 700 dp, contenido con ancho máximo y desplazamiento para pantallas pequeñas/texto grande.
- Acciones con áreas táctiles Material, etiquetas accesibles y errores explicados con texto.
- Duraciones de 160/240 ms. Android controla la escala temporal de Compose; con animaciones desactivadas se omiten desplazamientos, fades y progreso indeterminado animado.
- El selector de carpetas pertenece a Android: respeta su apariencia y restricciones del sistema.

## Verificación y límites

Consulta `docs/VERIFICATION.md` para resultados, capturas y matriz de pruebas. Las muestras y proveedores ficticios están únicamente en pruebas; **el APK no contiene canciones de demostración**.

- El escaneo pertenece al ViewModel: sobrevive a cambios de configuración, pero se cancela al finalizar el proceso. El siguiente inicio conserva el último índice completo; si todavía no existe, inicia otro escaneo.
- SAF no ofrece un observador universal fiable. Los cambios de contenido se incorporan con **Actualizar biblioteca**; el permiso/directorio se comprueba también al volver a la app.
- Las llamadas nativas de proveedores y MediaMetadataRetriever no siempre son interrumpibles. Cancelar se hace efectivo al terminar la llamada en curso, sin bloquear la UI.
- Si un directorio no puede enumerarse, se conserva el índice anterior para no confundir un fallo con una carpeta vacía.
- Miniaturas de hasta 384 px, presupuesto de aproximadamente 96 MiB y limpieza de archivos sin referencias. Se descartan carátulas comprimidas mayores de 16 MiB; MediaMetadataRetriever puede asignar transitoriamente el bloque incrustado antes de conocer su tamaño.
- No se ha medido FPS en hardware físico ni se garantiza compatibilidad con todos los proveedores SAF o códecs.

Referencias de compatibilidad: [AGP 9.4](https://developer.android.com/build/releases/agp-9-4-0-release-notes), [versiones AndroidX](https://developer.android.com/jetpack/androidx/versions), [Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files), [Coil](https://coil-kt.github.io/coil/getting_started/). Los artefactos se comprobaron también en Google Maven y Maven Central.
