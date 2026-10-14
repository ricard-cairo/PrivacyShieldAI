# Privacy Shield AI

App Android **gratuita, sin anuncios y de código abierto** que guía al público general en 10 plataformas (Instagram, Facebook, WhatsApp, LinkedIn, X, Gemini, ChatGPT, Claude, Reddit y Adobe) para dos tareas:

1. **Entrenamiento:** impedir que sus datos se usen para entrenar IAs.
2. **Funciones de IA:** desactivar u ocultar los asistentes y funciones de IA (Meta AI, Grok, Gemini, memoria de ChatGPT/Claude, AI Assistant de Acrobat...). Cuando no se pueden desactivar, la guía lo dice claramente y da alternativas.

La app **no automatiza** nada en las cuentas del usuario: muestra los pasos, abre el ajuste exacto cuando hay enlace directo, guarda qué ha hecho y avisa cuando una plataforma cambia.

## Principios (no negociables)

- **Privacidad primero:** sin cuenta de usuario, sin servidor propio, sin analítica, sin anuncios, sin SDKs de terceros que envíen datos (nada de Firebase Analytics, Crashlytics, Facebook SDK, etc.).
- **Nunca pedir ni guardar credenciales** de otras plataformas. Nada de WebView con login ni *scraping*.
- **Permisos:** solo `INTERNET` y `POST_NOTIFICATIONS`. Cualquier permiso nuevo requiere justificación explícita.
- **Detección de apps instaladas:** solo con `<queries>` + `<package>` en el manifest para los paquetes del catálogo. **Nunca `QUERY_ALL_PACKAGES`** (Google Play lo restringe y no encaja con una app de privacidad). El resultado no sale del móvil.
- **Sin logos de marcas en el repositorio:** el icono de cada plataforma es el de su app instalada, leído del propio móvil. Si no está instalada, se muestra la inicial.
- **Datos del usuario solo en el dispositivo** (DataStore).
- **Red:** solo HTTPS y solo para descargar el catálogo público (`AppConfig.REMOTE_CATALOG_URL`).
- **Textos:** español, frases cortas, sin tecnicismos. Siempre en `res/values/strings.xml`, nunca en el código.

## Stack

- Kotlin con **Kotlin integrado en AGP 9.4** (no añadir el plugin `org.jetbrains.kotlin.android`). Gradle 9.6.
- Jetpack Compose + Material 3. Sin librería de iconos ni de navegación (navegación lista/detalle con estado en `MainActivity`).
- kotlinx.serialization (JSON), DataStore Preferences (progreso), WorkManager (comprobación diaria), Custom Tabs (enlaces web).
- Red con `HttpURLConnection` (`RemoteSource`). Si se porta a Kotlin Multiplatform, sustituir por Ktor Client.
- minSdk 26, targetSdk/compileSdk 37, Java 11.
- Versiones en `gradle/libs.versions.toml`. Los plugins `kotlin-compose` y `kotlin-serialization` comparten la versión `kotlin`: actualizarlos siempre juntos. Si la sincronización da un error de versión de Kotlin, ejecutar `gradlew.bat buildEnvironment`, buscar la versión de `kotlin-gradle-plugin` que trae AGP y poner esa en `kotlin`.

## Estructura

```
PrivacyShieldAI/
├── CLAUDE.md
├── store/icon-512.png                 Icono para la ficha de Google Play
└── app/src/main/
    ├── AndroidManifest.xml            Permisos, <queries> (Custom Tabs + paquetes del catálogo)
    ├── assets/plataformas.json        Catálogo incluido en la app (fuente de verdad)
    ├── res/
    │   ├── drawable/ic_launcher_*.xml Icono adaptativo: fondo, primer plano y monocromo
    │   ├── drawable/ic_notification.xml  Icono de notificación (escudo monocromo)
    │   └── values/strings.xml         Todos los textos
    └── java/com/example/privacyshieldai/
        ├── AppConfig.kt               URL del catálogo remoto (null = sin actualizaciones)
        ├── PrivacyShieldApp.kt        Application: crea dependencias, canal de avisos y worker
        ├── MainActivity.kt            Compose, navegación, detección al volver a la app, permiso de avisos
        ├── data/
        │   ├── Models.kt              PlatformCatalog, Platform, Mechanism
        │   ├── CatalogParser.kt       Parseo, validación y comparación (sin Android, testeable)
        │   ├── RemoteSource.kt        Descarga HTTPS con límite de 512 KB
        │   ├── PlatformRepository.kt  Caché local > assets; refresh() remoto
        │   ├── ProgressStore.kt       DataStore: plataformas hechas y último aviso enviado
        │   └── InstalledAppsDetector.kt  Qué apps del catálogo están instaladas y activas
        ├── domain/PlatformStatus.kt   PENDING / DONE / REVIEW (función pura)
        ├── ui/
        │   ├── MainViewModel.kt       Estado de la UI (combina catálogo, progreso y apps instaladas)
        │   ├── PlatformListScreen.kt  Lista en dos secciones: instaladas / otras
        │   ├── PlatformDetailScreen.kt  Pasos, botones de acción y marcar como hecho
        │   ├── PlatformIcon.kt        Icono real de la app instalada o inicial
        │   ├── StatusBadge.kt         Etiqueta Hecho / Pendiente / Revisar
        │   └── theme/Theme.kt         Paleta azul de la marca (claro y oscuro)
        ├── util/Links.kt              Abrir enlaces (app o Custom Tab), apps y correo
        └── work/                      UpdateCheckWorker (diario) y Notifications
```

## Funcionamiento de la app

### Pantalla de lista

- Cabecera con el progreso global ("X de 20 ajustes completados"), barra de progreso y desglose "Entrenamiento: a/10 · Funciones de IA: b/10".
- Cada fila muestra el estado global (el más urgente de sus dos tareas) y debajo "Entrenamiento ✓ · Funciones IA •".
- Sección **"Instaladas en tu móvil"** arriba y **"Otras plataformas"** debajo. Dentro de cada una, orden por estado: Revisar > Pendiente > Hecho.
- La detección de apps instaladas se repite cada vez que el usuario vuelve a la app (`LifecycleResumeEffect`) y tras descargar un catálogo nuevo. Una app preinstalada pero desactivada no cuenta como instalada.
- Botón **"Actualizar"** (barra superior): fuerza la descarga del catálogo remoto. Mensajes: información nueva / ya está al día / error / no configurado. La misma comprobación se hace sola al abrir la app (sin mensaje salvo si hay novedades) y una vez al día en segundo plano.

### Pantalla de detalle

- Cabecera: icono, estado global, categoría (y "Instalada en este móvil") y botón **"Abrir la app de X"** si está instalada.
- Dos bloques (`TaskSection`), cada uno con su estado, aviso de Revisar, resumen, pasos, notas, botones, "Ya lo he hecho"/"Marcar como pendiente", fecha y "Ver fuente":
  1. **"No usar mis datos para entrenar IA"** (campos de primer nivel de la plataforma).
  2. **"Desactivar funciones de IA"** (`aiFeatures`), con un subtítulo según `FeatureMechanism`: `TOGGLE` "Se puede desactivar", `PARTIAL` "Solo se puede desactivar en parte", `NONE` "No se puede desactivar: te damos alternativas". Si tiene `url`, botón "Abrir el ajuste" (también en `NONE`, p. ej. para borrar el historial de Grok).
- Botones del bloque de entrenamiento según `mechanism`:

| `mechanism` | Botón | Comportamiento |
| --- | --- | --- |
| `TOGGLE` | "Abrir el ajuste" | `Links.openPreferApp`: abre la app de la plataforma si gestiona el enlace; si no, el navegador |
| `FORM` | **Ninguno** | Sin botón de formulario: el usuario llega al formulario desde la propia app siguiendo los pasos. `url`, `extraUrl` y `extraUrlLabel` se conservan en el JSON pero la UI no los usa |
| `REQUEST` | "Enviar solicitud por correo" | Abre el correo con la plantilla RGPD (art. 21) en inglés de `strings.xml` |
| `NONE` | Ninguno | Solo pasos y recomendaciones |

- Si `linkVerified` es `false`, aviso bajo el botón de ajuste.
- La primera vez que se marca una tarea como hecha se pide el permiso de notificaciones (Android 13+).

### Estados y avisos

- Dos tareas por plataforma (`domain.Task`: `TRAINING`, `FEATURES`), cada una con su propio progreso y su propio `updatedOn`.
- `ProgressStore` guarda, por tarea marcada, el `updatedOn` de ese momento. Claves: `done_<id>` para entrenamiento (se mantiene por compatibilidad con versiones anteriores) y `feat_<id>` para funciones de IA.
- `computeStatus(updatedOn, marcado)` compara con el catálogo actual: sin marca = Pendiente; igual = Hecho; catálogo con fecha posterior = Revisar. `overallStatus()` da el más urgente (Revisar > Pendiente > Hecho).
- `UpdateCheckWorker` (diario, con red): si alguna tarea está en Revisar y no se ha avisado ya de esa `dataVersion`, envía una notificación. Solo reintenta errores de red.

### Tema e icono

- Etiquetas de estado con colores semánticos propios (en `StatusBadge.kt`, no en el tema): verde = Hecho, ámbar = Pendiente, rojo = Revisar, con variante oscura y un símbolo (✓ • !) para no depender solo del color.
- Colores fijos de la marca en `Theme.kt` (azul `#1B5E9E`, degradado `#2A7BC8` → `#0D3B66`). **No usar colores dinámicos** (Material You): cambiaban los textos a marrón o morado según el fondo de pantalla. Todos los roles de color están definidos para no heredar los morados por defecto de Material 3.
- Icono adaptativo: escudo blanco con un destello de IA tachado en rojo (`#E5484D`) sobre el degradado azul. Todo dentro de la zona segura de 66 dp. La versión monocroma recorta el destello y deja el tachado en negativo.

## Catálogo `plataformas.json`

- `schemaVersion`: subir solo si hay cambios incompatibles (y actualizar `CatalogParser.SUPPORTED_SCHEMA_VERSION`). Campos nuevos opcionales no requieren subirlo (`ignoreUnknownKeys`).
- `dataVersion`: fecha ISO de la última revisión. **Subirla en cada publicación**; la app solo acepta catálogos más nuevos.
- Por plataforma: `id` (estable, nunca cambiarlo), `name`, `category`, `mechanism`, `packages` (paquetes Android, el principal primero), `regions`, `summary`, `steps`, `url`/`email`, `linkVerified`, `notes`, `sourceUrl`, `updatedOn` y `aiFeatures`.
- `aiFeatures` (funciones de IA): `mechanism` (`TOGGLE` | `PARTIAL` | `NONE`), `summary`, `steps`, `url`, `linkVerified`, `notes`, `sourceUrl`, `updatedOn` (independiente del de entrenamiento). **Ser honesto:** si algo no se puede desactivar, usar `NONE`/`PARTIAL` y decirlo en `summary`; nunca prometer un interruptor que no existe.
- **Los pasos no deben referirse a botones que no existen** (p. ej. "usa el enlace de abajo" en plataformas `FORM`).
- **`updatedOn` decide los avisos:** si cambia, quien tenía la plataforma marcada pasa a "Revisar" y recibe una notificación. Cambiarlo solo cuando la plataforma cambie de verdad su política o su ruta, no por correcciones de texto.
- Cada ruta debe tener `sourceUrl`; preferir fuentes oficiales. Si el enlace directo no se ha comprobado, `linkVerified: false`.
- **Al añadir o cambiar `packages`, añadir el mismo `<package>` en `<queries>` del `AndroidManifest.xml`.** `CatalogTest` falla si no coinciden. Un catálogo remoto no puede añadir paquetes nuevos sin publicar versión de la app.
- Publicación: copiar el mismo fichero a GitHub Pages y poner su URL en `AppConfig.REMOTE_CATALOG_URL`.

## Comandos

```
gradlew.bat assembleDebug        # compilar
gradlew.bat testDebugUnitTest    # tests unitarios
gradlew.bat lint                 # revisión estática
```

Tests en `app/src/test`:

- `CatalogTest`: 10 plataformas, ids únicos, https (también en `aiFeatures`), pasos, fechas, acción según `mechanism`, guía de funciones de IA con pasos y fuente, paquetes = `<queries>` del manifest, ida y vuelta del JSON, versión de esquema.
- `PlatformStatusTest`: Pendiente / Hecho / Revisar y estado global.

Ejecutarlos siempre después de tocar `plataformas.json` o el manifest.

## Forma de trabajar

- Cambios pequeños, una funcionalidad por vez; compilar y pasar tests antes de dar algo por terminado.
- Lógica nueva sin dependencias de Android cuando sea posible (para poder portarla a iOS con Kotlin Multiplatform).
- No añadir dependencias sin justificarlo; preferir las de AndroidX y JetBrains.
- No incluir claves, tokens ni datos personales en el repositorio.
- Actualizar este fichero cuando cambie el comportamiento de la app.

## Pendiente

- [ ] Cambiar `applicationId`/`namespace` de `com.example.privacyshieldai` antes de publicar (Google Play no acepta `com.example`).
- [ ] Borrar `res/layout/activity_main.xml` y quitar `appcompat` y `constraintlayout` (restos del esqueleto con Views).
- [ ] Borrar los `.webp` de `mipmap-hdpi` a `mipmap-xxxhdpi` (icono antiguo; con minSdk 26 se usa siempre el adaptativo).
- [ ] Ocultar el botón "Actualizar" mientras `REMOTE_CATALOG_URL` sea null (propuesto, sin decidir).
- [ ] Confirmar en un móvil real los nombres de paquete (sobre todo Adobe y Gemini; valorar añadir la app de Google para Gemini).
- [ ] Verificar en las apps en español los pasos de `aiFeatures` (todos tienen `linkVerified: false`; varias fuentes son de terceros, sobre todo Meta, LinkedIn y Reddit).
- [ ] Verificar en las apps en español las rutas de LinkedIn, X y ChatGPT y marcar `linkVerified`.
- [ ] Publicar el catálogo en GitHub Pages y configurar `REMOTE_CATALOG_URL`.
- [ ] Elegir licencia de código abierto (p. ej. GPL-3.0 o Apache-2.0) y añadir `LICENSE`.
- [ ] Revisar `backup_rules.xml` / `data_extraction_rules.xml` (decidir si el progreso se incluye en copias de seguridad).
