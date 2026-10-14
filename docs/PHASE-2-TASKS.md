# EKO — Plan de trabajo de la Fase 2

Esta fase se divide en dos módulos independientes:

- **Biblioteca local:** canciones almacenadas en el dispositivo. Responsable: `Duncan0305`.
- **Escucha en línea:** catálogo y reproducción desde una fuente externa autorizada. Responsable: `Romario2404`.

## Contrato entre módulos

- `Duncan0305` crea el acceso del sidebar llamado **Escuchar en línea** y deja una ruta/pantalla inicial estable.
- `Romario2404` trabaja dentro de esa ruta y no modifica la biblioteca local ni sus pantallas.
- El módulo online debe quedar aislado en paquetes propios (`online/`, `playback/online/` o equivalente).
- Las integraciones externas deben usar APIs, SDKs o métodos de acceso permitidos por cada servicio.
- No implementar scraping, descarga de contenido protegido, evasión de DRM ni extracción de audio desde servicios que no lo permitan.
- No guardar credenciales, tokens ni claves API en el repositorio.
- Los cambios compartidos en navegación se hacen primero por `Duncan0305`; Romario consume la ruta existente.

## Orden recomendado

1. Duncan crea el punto de entrada **Escuchar en línea**.
2. Romario implementa el módulo online dentro de esa pantalla.
3. Duncan continúa con la organización visual de la biblioteca local.
4. Cada módulo se prueba por separado.
5. Se realiza una integración final sin cambiar el contrato de navegación.

Consulta los detalles en:

- [Tareas de Duncan0305](tasks/DUNCAN-TASKS.md)
- [Tareas de Romario2404](tasks/ROMARIO-TASKS.md)
