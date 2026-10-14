<p align="center">
  <img src="store/icon-512.png" alt="Icono de Privacy Shield AI" width="128">
</p>

<h1 align="center">Privacy Shield AI</h1>

<p align="center">
  App Android gratuita, sin anuncios y de código abierto que te guía para impedir que tus datos se usen para entrenar inteligencias artificiales y para desactivar las funciones de IA de tus apps.
</p>

---

## ¿Qué es?

Cada vez más plataformas usan tus publicaciones, fotos y conversaciones para entrenar sus modelos de IA. Desactivarlo es posible, pero cada una lo esconde en un sitio distinto y las opciones cambian a menudo.

**Privacy Shield AI** reúne en una sola app, para cada plataforma, dos guías:

1. **No usar mis datos para entrenar IA:** los pasos para que la plataforma no entrene sus modelos con tu contenido.
2. **Desactivar funciones de IA:** cómo apagar u ocultar sus asistentes y funciones de IA (Meta AI, Grok, Gemini, la memoria de ChatGPT y Claude, AI Assistant de Acrobat...). Si algo no se puede desactivar, la app te lo dice y te da alternativas.

Además guarda tu progreso y te avisa cuando alguna plataforma cambia.

> La app **no entra en tus cuentas ni pide contraseñas**. Te indica qué hacer y te lleva al ajuste; el cambio lo haces tú.

## Plataformas incluidas

| Plataforma | Entrenamiento de IA | Funciones de IA |
| --- | --- | --- |
| Instagram | Formulario de objeción de Meta (UE / Reino Unido) | Solo en parte (silenciar Meta AI) |
| Facebook | Formulario de objeción de Meta (UE / Reino Unido) | Solo en parte (silenciar Meta AI, resúmenes) |
| WhatsApp | Sin interruptor: recomendaciones de uso | No se puede: alternativas |
| LinkedIn | Interruptor en los ajustes | No se puede: alternativas |
| X | Interruptor en los ajustes | No se puede: alternativas |
| Google Gemini | Interruptor en los ajustes | Solo en parte (asistente del móvil, Gmail) |
| ChatGPT | Interruptor en los ajustes | Interruptor (memoria) |
| Claude | Interruptor en los ajustes | Interruptor (memoria) |
| Reddit | Solicitud RGPD por correo (plantilla incluida) | No se puede: alternativas |
| Adobe | Interruptor en los ajustes | Interruptor (IA generativa de Acrobat) |

## Funcionalidades

- **Pasos claros** para cada plataforma, con la fuente de la información.
- **Botón directo al ajuste** cuando la plataforma tiene enlace; se abre en su app si está instalada.
- **Detección de apps instaladas:** las plataformas que tienes en el móvil aparecen primero, con su icono.
- **Dos tareas por plataforma** (entrenamiento y funciones de IA), cada una con su progreso: *Hecho*, *Pendiente* o *Revisar*.
- **Avisos de cambios:** si una plataforma cambia su política después de que la marcaras, pasa a *Revisar* y recibes una notificación.
- **Plantilla RGPD** lista para enviar a las plataformas sin interruptor (Reddit).
- **Modo claro y oscuro.**

## Privacidad

Es una app de privacidad, así que se aplica a sí misma lo que pide a los demás:

- Sin cuenta de usuario, sin servidor propio, sin anuncios y sin analítica.
- Sin SDKs de terceros que envíen datos.
- Tu progreso y la lista de apps instaladas **se quedan en tu móvil**.
- Solo dos permisos: **Internet** (para descargar la lista de plataformas actualizada) y **notificaciones** (para los avisos de cambios).
- La detección de apps se limita a las 16 apps de estas plataformas (no usa el permiso para ver todas las apps instaladas).

## Requisitos

- Android 8.0 (API 26) o superior.

## Compilar el proyecto

Requisitos: Android Studio reciente con JDK 17 o superior.

```bash
git clone https://github.com/ricard-cairo/PrivacyShieldAI.git
cd PrivacyShieldAI
```

Abre la carpeta en Android Studio, espera a que sincronice Gradle y pulsa **Run**. Desde la línea de comandos:

```bash
./gradlew assembleDebug         # compilar (gradlew.bat en Windows)
./gradlew testDebugUnitTest     # tests unitarios
```

### Tecnologías

- Kotlin (integrado en AGP 9) y Jetpack Compose con Material 3
- kotlinx.serialization, DataStore, WorkManager y Custom Tabs
- Sin dependencias de terceros fuera de AndroidX y JetBrains

## Cómo actualizar la información de una plataforma

Toda la información está en [`app/src/main/assets/plataformas.json`](app/src/main/assets/plataformas.json). Para corregir o añadir una plataforma:

1. Edita el JSON (pasos, enlace, fuente).
2. Si la plataforma ha cambiado de verdad su política, actualiza su campo `updatedOn`; así los usuarios que ya la tenían hecha recibirán un aviso.
3. Si añades paquetes Android, decláralos también en `<queries>` del `AndroidManifest.xml`.
4. Ejecuta los tests: comprueban que el JSON es válido y coincide con el manifest.

Las normas completas del proyecto están en [`CLAUDE.md`](CLAUDE.md).

## Contribuir

Las plataformas cambian sus menús a menudo. Si ves una ruta desactualizada o un enlace que ya no funciona, abre un *issue* indicando la plataforma, qué ha cambiado y, si es posible, un enlace a la fuente oficial.

## Estado

Proyecto en desarrollo. Pendiente, entre otros:

- Verificar en las apps en español las rutas de LinkedIn, X y ChatGPT y los pasos de funciones de IA.
- Publicar el catálogo en línea para recibir actualizaciones sin nueva versión de la app.
- Publicación en Google Play y F-Droid.
- Elegir licencia de código abierto.

## Aviso

Privacy Shield AI no está afiliada a ninguna de las plataformas mencionadas. Los nombres de las plataformas son marcas de sus respectivos propietarios. La información se ofrece como guía y puede quedar desactualizada; no constituye asesoramiento legal.
