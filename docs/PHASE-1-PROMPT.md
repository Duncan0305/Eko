# PLAN DE IMPLEMENTACIÓN — FASE 1

## Reproductor de música local para Android

> Documento de referencia. Es el prompt original con el que se construyó la Fase 1 de Eko.
> Se conserva tal cual para consultar el alcance, requisitos y criterios de aceptación.

### 1. Objetivo general

Desarrollar la primera fase de una aplicación nativa para Android que permita al usuario seleccionar una carpeta de su dispositivo y visualizar automáticamente todos los archivos musicales almacenados en ella.

La aplicación debe detectar las canciones, extraer sus metadatos y mostrarlas en una biblioteca musical moderna, intuitiva y optimizada.

**Importante:** en esta fase NO se implementará la reproducción de audio. El objetivo es construir una biblioteca musical funcional y preparar la arquitectura para las siguientes fases.

### 2. Tecnologías obligatorias

- Lenguaje: Kotlin.
- Interfaz: Jetpack Compose.
- Sistema de diseño: Material Design 3 personalizado.
- Arquitectura: MVVM + Repository.
- Gestión de estados: StateFlow.
- Procesamiento asíncrono: Kotlin Coroutines.
- Lectura de archivos: Storage Access Framework (SAF).
- Exploración de carpetas: DocumentFile.
- Extracción de metadatos: MediaMetadataRetriever.
- Imágenes y carátulas: Coil compatible con Compose.
- Preferencias: DataStore.
- Base de datos local: Room, si es necesaria para almacenar el índice musical.
- Navegación: Navigation Compose.
- Compilación: Gradle con Kotlin DSL.

Utilizar versiones estables, compatibles y actuales de las dependencias.

No implementar backend, APIs externas, servidores ni servicios en la nube.

---

### 3. Etapa 1 — Configuración inicial

Crear o adaptar el proyecto Android con la siguiente estructura:

```text
app/
└── src/main/
    └── java/com/musicplayer/
        ├── MainActivity.kt
        ├── ui/
        │   ├── screens/
        │   │   ├── WelcomeScreen.kt
        │   │   └── LibraryScreen.kt
        │   ├── components/
        │   │   ├── SongItem.kt
        │   │   ├── SongArtwork.kt
        │   │   ├── SearchBar.kt
        │   │   └── FolderHeader.kt
        │   ├── navigation/
        │   └── theme/
        ├── data/
        │   ├── model/
        │   │   └── Song.kt
        │   ├── repository/
        │   │   └── MusicRepository.kt
        │   └── local/
        │       └── FolderPreferences.kt
        ├── domain/
        ├── scanner/
        │   ├── MusicScanner.kt
        │   └── MetadataExtractor.kt
        └── viewmodel/
            └── LibraryViewModel.kt
```

La estructura es una propuesta inicial. Adaptarla cuando sea necesario para mantener una arquitectura limpia, sin generar capas o archivos innecesarios.

**Requisitos:**
- Separar la interfaz de la lógica de negocio.
- Evitar lógica de lectura de archivos dentro de los componentes Compose.
- Mantener el código modular y reutilizable.
- Preparar la arquitectura para integrar posteriormente Media3.

### 4. Etapa 2 — Pantalla de bienvenida

Al abrir la aplicación por primera vez, mostrar una pantalla de bienvenida.

Debe contener:
- Identidad visual de la aplicación.
- Nombre y logotipo.
- Una breve descripción de su funcionamiento.
- Botón principal: "Seleccionar mi música".
- Texto indicando que funciona sin conexión a internet.

No solicitar permisos innecesarios.

Al pulsar el botón, abrir el selector de carpetas nativo de Android.

**Espacio para invocar mi skill de diseño:**

`[INVOCAR SKILL: frontend-design]`

La skill definirá la identidad visual, composición, tipografía, jerarquía, colores y diseño de la pantalla.

### 5. Etapa 3 — Selección de carpeta musical

Implementar el selector de carpetas mediante `ACTION_OPEN_DOCUMENT_TREE`.

**Comportamiento requerido:**

1. El usuario pulsa "Seleccionar mi música".
2. Android abre su selector nativo de carpetas.
3. El usuario selecciona una carpeta permitida.
4. La aplicación obtiene su URI.
5. Se conserva el permiso persistente de lectura.
6. Se guarda la referencia de la carpeta mediante DataStore.
7. Se inicia el escaneo musical.

La aplicación debe permitir:
- Seleccionar una carpeta.
- Cambiar la carpeta seleccionada.
- Cancelar la selección sin provocar errores.
- Recordar la carpeta entre sesiones.
- Detectar si el acceso a la carpeta fue revocado.

Respetar las restricciones del Storage Access Framework, especialmente en Android 11 y posteriores.

No solicitar permisos de acceso completo al almacenamiento ni utilizar `MANAGE_EXTERNAL_STORAGE`.

### 6. Etapa 4 — Escaneo de archivos musicales

Implementar un servicio `MusicScanner` encargado de explorar la carpeta seleccionada.

**Requisitos:**

- Detectar archivos musicales.
- Recorrer también las subcarpetas.
- Reconocer formatos habituales como MP3, FLAC, WAV, OGG, M4A y AAC cuando sean compatibles.
- Validar el tipo de archivo.
- Evitar procesar imágenes, documentos y archivos ajenos a la música.
- Evitar canciones duplicadas por URI.
- Capturar errores de lectura individualmente.
- No bloquear la interfaz durante el escaneo.

El escaneo debe utilizar Kotlin Coroutines con `Dispatchers.IO`.

Mostrar en la interfaz un indicador de progreso durante la exploración.

El usuario debe poder cancelar o repetir el escaneo.

No cargar toda la información multimedia simultáneamente en memoria.

### 7. Etapa 5 — Extracción de metadatos

Implementar `MetadataExtractor` utilizando `MediaMetadataRetriever`.

Extraer, cuando estén disponibles:

- Título.
- Artista.
- Álbum.
- Duración.
- Carátula incrustada.
- Género.
- Número de pista.
- Año.
- Nombre del archivo.
- URI del archivo.

Crear el modelo:

```kotlin
data class Song(
    val id: String,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val genre: String?,
    val year: String?,
    val trackNumber: Int?,
    val fileName: String
)
```

La referencia de la carátula debe gestionarse por separado si resulta más eficiente.

**Reglas importantes:**

- Si el título no existe, utilizar el nombre del archivo.
- Si el artista no existe, mostrar "Artista desconocido".
- Si el álbum no existe, mostrar "Álbum desconocido".
- Si falta una carátula, mostrar una imagen predeterminada.
- Si un archivo está dañado, omitirlo o marcarlo sin detener el escaneo.
- Liberar correctamente los recursos de `MediaMetadataRetriever`.

Evitar mantener imágenes originales de gran tamaño en memoria.

### 8. Etapa 6 — Pantalla principal de biblioteca

Diseñar una pantalla `LibraryScreen` que muestre las canciones encontradas.

**Elementos principales:**

1. Encabezado con el nombre de la aplicación.
2. Título "Mi música".
3. Contador de canciones encontradas.
4. Buscador.
5. Carpeta seleccionada.
6. Botón para actualizar biblioteca.
7. Opciones de ordenamiento.
8. Lista de canciones.

Cada elemento de la lista debe mostrar:

- Carátula del álbum.
- Nombre de la canción.
- Nombre del artista.
- Duración.
- Menú contextual de tres puntos.

Utilizar `LazyColumn` para evitar renderizar canciones fuera de la pantalla visible.

Los componentes deben ser reutilizables.

Evitar interfaces sobrecargadas de botones, tarjetas innecesarias o elementos decorativos.

**Espacio para invocar mi skill de diseño:**

`[INVOCAR SKILL: frontend-design]`

El diseño debe sentirse como una aplicación musical premium, sin copiar directamente la interfaz de Spotify, Apple Music u otras aplicaciones.

### 9. Etapa 7 — Búsqueda y organización

Implementar búsqueda de canciones utilizando los datos ya escaneados.

**Funciones:**
- Buscar por título.
- Buscar por artista.
- Buscar por álbum.
- Ordenar alfabéticamente.
- Ordenar por duración.
- Ordenar por fecha de modificación, si está disponible.

La búsqueda debe responder de manera fluida, sin repetir el escaneo del almacenamiento.

Preparar la arquitectura para incorporar posteriormente vistas por álbumes, artistas y carpetas.

No implementar todavía playlists ni favoritos.

### 10. Etapa 8 — Animaciones e interacciones

Incorporar animaciones discretas y optimizadas.

Ejemplos:
- Transición entre bienvenida y biblioteca.
- Aparición de estados de carga.
- Cambios suaves al filtrar canciones.
- Respuesta visual al pulsar elementos.
- Transiciones en estados vacíos y de error.

**Espacio para invocar mi skill de animaciones:**

`[INVOCAR SKILL: motion-design]`

Respetar las preferencias del sistema para reducir animaciones.

Evitar efectos que generen consumo excesivo de CPU o GPU.

### 11. Etapa 9 — Persistencia y rendimiento

La aplicación debe recordar la carpeta seleccionada.

Implementar:

- Persistencia de la URI mediante DataStore.
- Reutilización del permiso de acceso concedido.
- Caché eficiente de carátulas.
- Conservación del índice musical cuando resulte necesario.
- Actualización manual de la biblioteca.
- Detección y manejo de archivos eliminados o inaccesibles.

Si se utiliza Room, emplearlo para almacenar metadatos e identificadores de canciones, no los archivos de música ni imágenes completas.

**Objetivos de optimización:**

- Minimizar el tiempo de apertura.
- Evitar bloqueos de la interfaz.
- Reducir consumo de memoria.
- Evitar decodificar repetidamente las mismas carátulas.
- Permitir desplazamiento fluido con bibliotecas grandes.
- Evitar escanear toda la carpeta en cada recomposición de Compose.

### 12. Etapa 10 — Estados de interfaz

Contemplar los siguientes estados:

**Sin carpeta seleccionada:** mostrar la pantalla de bienvenida.

**Escaneando:** mostrar progreso y permitir cancelar.

**Biblioteca vacía:** informar que no se encontraron canciones y ofrecer seleccionar otra carpeta.

**Biblioteca cargada:** mostrar canciones disponibles.

**Error de acceso:** permitir volver a seleccionar una carpeta.

**Sin resultados de búsqueda:** mostrar un mensaje y permitir limpiar los filtros.

### 13. Etapa 11 — Pruebas

Realizar pruebas de las siguientes situaciones:

- Primera apertura.
- Selección correcta de carpeta.
- Cancelación del selector.
- Carpeta sin canciones.
- Carpeta con subcarpetas.
- Archivos sin metadatos.
- Canciones sin carátula.
- Archivos dañados.
- Eliminación de canciones.
- Revocación del permiso de carpeta.
- Reinicio de aplicación.
- Bibliotecas de cientos o miles de canciones.
- Búsqueda y ordenamiento.
- Funcionamiento completamente offline.

Agregar pruebas unitarias para la lógica de filtrado, ordenamiento y transformación de metadatos.

Verificar que el proyecto compile correctamente.

### 14. Restricciones de esta fase

No implementar:

- Reproducción de música.
- Media3 o ExoPlayer todavía.
- Ecualizador.
- Reproducción en segundo plano.
- Notificaciones multimedia.
- Letras sincronizadas.
- Streaming.
- Inicio de sesión.
- Backend.
- Playlists.
- Favoritos.
- Sincronización en la nube.

No introducir dependencias que no sean necesarias para los objetivos de esta fase.

### 15. Criterios de aceptación

La implementación se considerará terminada cuando:

1. La aplicación abra correctamente en Android.
2. El usuario pueda seleccionar una carpeta musical.
3. El sistema encuentre archivos musicales reales dentro de la carpeta y sus subcarpetas.
4. Se extraigan correctamente los metadatos disponibles.
5. Se muestren carátulas reales o imágenes predeterminadas.
6. La biblioteca sea fluida y permita desplazarse.
7. La búsqueda y el ordenamiento funcionen.
8. La carpeta permanezca seleccionada después de cerrar la aplicación.
9. Se pueda actualizar la biblioteca.
10. No se produzcan bloqueos por archivos dañados o permisos revocados.
11. La aplicación funcione sin internet.
12. El código quede preparado para integrar el motor musical en la siguiente fase.

### 16. Instrucciones finales para Codex

Antes de programar, inspeccionar la estructura y configuración existente del proyecto.

Implementar las etapas en orden, reutilizando el código existente cuando corresponda.

No reemplazar archivos funcionales innecesariamente.

No crear datos musicales simulados como implementación definitiva.

No avanzar a funcionalidades de reproducción.

Mantener un código limpio, entendible y mantenible.

Cuando se alcance una etapa que requiera `frontend-design` o `motion-design`, dejar identificado el punto de integración para que yo pueda invocar manualmente la skill correspondiente.

Al finalizar:
- Resumir los archivos creados o modificados.
- Indicar las funcionalidades implementadas.
- Ejecutar las verificaciones disponibles.
- Informar errores o limitaciones pendientes.
- Explicar cómo ejecutar y probar la aplicación en Android Studio.

**Resultado esperado:** una aplicación Android que permite seleccionar una carpeta del teléfono y muestra automáticamente una biblioteca musical real con carátulas, títulos, artistas, álbumes y duración, lista para incorporar la reproducción musical en la Fase 2.
