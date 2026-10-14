# Tareas de Romario2404 — Módulo de escucha en línea

## Alcance

Implementar el módulo al que se entra desde **Escuchar en línea**, separado de la biblioteca local y preparado para consumir una fuente externa autorizada.

## Módulo A — Fuente online

- [ ] Investigar qué fuente ofrece una API oficial y permite consultar su catálogo.
- [ ] Elegir una sola fuente inicial.
- [ ] Documentar autenticación, límites, disponibilidad regional y permisos.
- [ ] No usar scraping, endpoints privados, descargas no autorizadas ni evasión de DRM.
- [ ] Definir el manejo de errores de red y límites de solicitudes.
- [ ] No guardar claves API, contraseñas ni tokens en Git.

## Módulo B — Arquitectura online

- [ ] Crear modelos online separados de `Song` local.
- [ ] Crear la interfaz `OnlineMusicRepository`.
- [ ] Crear el cliente o adaptador de la API oficial.
- [ ] Crear el repositorio con coroutines y `Dispatchers.IO`.
- [ ] Crear `OnlineMusicViewModel` usando `StateFlow`.
- [ ] Definir estados inicial, cargando, resultados, vacío, error y sin conexión.
- [ ] Implementar cancelación de solicitudes.
- [ ] Mantener las clases online fuera de `MusicRepository.kt`.

Archivos previstos:

```text
online/model/OnlineTrack.kt
online/data/OnlineMusicRepository.kt
online/data/OnlineApiService.kt
online/viewmodel/OnlineMusicViewModel.kt
```

## Módulo C — Interfaz «Escuchar en línea»

- [ ] Implementar la pantalla dentro de la ruta creada por Duncan.
- [ ] Crear buscador de música online.
- [ ] Mostrar portada, título, artista, álbum y duración si están disponibles.
- [ ] Mostrar estados de carga, vacío, error y sin conexión.
- [ ] Añadir botón para volver a la biblioteca local.
- [ ] Mantener la identidad visual EKO.
- [ ] Añadir animaciones discretas y respetar animaciones reducidas.
- [ ] No modificar `LibraryScreen.kt` sin un contrato explícito.

Archivos previstos:

```text
ui/screens/OnlineMusicScreen.kt
ui/components/OnlineTrackItem.kt
ui/components/OnlineSearchBar.kt
```

## Módulo D — Reproducción o descarga permitida

- [ ] Confirmar qué acciones permite legalmente la fuente elegida.
- [ ] Si permite reproducción mediante SDK/API, integrarla de forma aislada.
- [ ] Si permite descargas offline, usar únicamente el flujo autorizado.
- [ ] Si no permite descargar archivos, mostrar solo las acciones permitidas por el servicio.
- [ ] No guardar audio protegido en la biblioteca local sin autorización.
- [ ] Mantener la cola online separada de la cola local.
- [ ] Documentar qué funciona offline y qué requiere internet.

## Verificación de Romario

- [ ] Probar búsqueda con conexión.
- [ ] Probar ausencia de conexión.
- [ ] Probar respuesta vacía.
- [ ] Probar error de autenticación o API.
- [ ] Probar cancelación de una búsqueda.
- [ ] Probar que la biblioteca local funcione aunque falle la fuente online.
- [ ] Ejecutar `:app:assembleDebug`.
- [ ] Ejecutar `:app:testDebugUnitTest`.
