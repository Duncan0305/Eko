# Tareas de Duncan0305 — Biblioteca local y entrada al módulo online

## Módulo A — Entrada «Escuchar en línea»

- [ ] Crear la opción **Escuchar en línea** en el sidebar.
- [ ] Crear una ruta de Navigation Compose exclusiva para el módulo online.
- [ ] Crear una pantalla inicial con estados de carga, vacío y error.
- [ ] Definir callbacks para volver a la biblioteca local.
- [ ] Documentar la ruta para que Romario no modifique `EkoNavigation.kt`.
- [ ] Verificar que abrir el módulo online no inicie un escaneo local.

Archivos previstos:

```text
ui/components/SidebarMenu.kt
ui/navigation/EkoNavigation.kt
ui/screens/OnlineHomeScreen.kt
```

## Módulo B — Biblioteca local por álbum, artista, género y carpeta

- [ ] Crear el menú de agrupación de la biblioteca local.
- [ ] Implementar la agrupación por álbum.
- [ ] Implementar la agrupación por artista.
- [ ] Implementar la agrupación por género.
- [ ] Implementar la agrupación por carpeta.
- [ ] Mostrar carátula, nombre y cantidad de canciones en cada grupo.
- [ ] Crear las vistas de detalle para cada agrupación.
- [ ] Mantener búsqueda y ordenamiento dentro de las agrupaciones.
- [ ] Mantener estados de biblioteca vacía, error y sin resultados.
- [ ] Evitar que cambiar de agrupación vuelva a escanear el almacenamiento.
- [ ] Mantener la paleta y los componentes visuales EKO.
- [ ] Añadir pruebas unitarias para la lógica de agrupación local.

Archivos previstos:

```text
domain/AlbumOrganizer.kt
domain/ArtistOrganizer.kt
domain/GenreOrganizer.kt
domain/FolderOrganizer.kt
ui/screens/AlbumsScreen.kt
ui/screens/ArtistsScreen.kt
ui/screens/GenresScreen.kt
ui/screens/FoldersScreen.kt
```

## Verificación de Duncan

- [ ] Compilar `:app:assembleDebug`.
- [ ] Ejecutar `:app:testDebugUnitTest`.
- [ ] Probar biblioteca local con subcarpetas.
- [ ] Probar búsqueda y ordenamiento sin internet.
- [ ] Probar que el sidebar conserve los accesos local y online.
