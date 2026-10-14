# Ficha de Google Play – Privacy Shield AI

Textos y respuestas listos para copiar en Play Console. Idioma predeterminado: **español (España) – es-ES**.

## Detalles de la app

**Nombre de la app** (máx. 30 caracteres):

```
Privacy Shield AI
```

**Descripción breve** (máx. 80 caracteres):

```
Evita que usen tus datos para entrenar IA y desactiva la IA de tus apps.
```

**Descripción completa** (máx. 4000 caracteres):

```
Cada vez más plataformas usan tus publicaciones, fotos y conversaciones para entrenar sus modelos de inteligencia artificial, y añaden asistentes de IA a sus apps. Desactivarlo es posible, pero cada una lo esconde en un sitio distinto y las opciones cambian a menudo.

Privacy Shield AI reúne en una sola app, para cada plataforma, dos guías paso a paso:

• No usar mis datos para entrenar IA: cómo pedir que no entrenen sus modelos con tu contenido.
• Desactivar funciones de IA: cómo apagar u ocultar sus asistentes y funciones de IA. Si algo no se puede desactivar, te lo decimos claramente y te damos alternativas.

PLATAFORMAS INCLUIDAS
Instagram, Facebook, WhatsApp, LinkedIn, X, Google Gemini, ChatGPT, Claude, Reddit y Adobe.

FUNCIONES
• Pasos claros y la fuente de cada instrucción.
• Botón directo al ajuste cuando existe enlace; se abre en la app de la plataforma si la tienes instalada.
• Las plataformas que tienes en el móvil aparecen primero.
• Tu progreso de un vistazo: Hecho, Pendiente o Revisar.
• Avisos cuando una plataforma cambia sus instrucciones después de que lo hayas hecho.
• Plantilla de solicitud RGPD lista para enviar a las plataformas sin interruptor.
• Modo claro y oscuro.

PRIVACIDAD DE VERDAD
• Gratis, sin anuncios y de código abierto.
• Sin registro ni cuenta.
• No pedimos contraseñas ni entramos en tus cuentas: tú haces cada cambio.
• Sin analítica ni rastreadores. Tu progreso se queda en tu móvil.
• Solo dos permisos: internet (para descargar las instrucciones actualizadas) y notificaciones (para los avisos).

La información se revisa y se actualiza sin necesidad de actualizar la app.

AVISO
Privacy Shield AI es un proyecto independiente y no está afiliado, patrocinado ni respaldado por Meta, WhatsApp, LinkedIn, X, Google, OpenAI, Anthropic, Reddit, Adobe ni ninguna otra plataforma mencionada. Los nombres de las plataformas son marcas de sus respectivos propietarios y se usan solo para identificarlas. La información es una guía y no constituye asesoramiento legal.

Código fuente: github.com/ricard-cairo/PrivacyShieldAI
```

**Notas de la versión 1.0:**

```
Primera versión: guías para 10 plataformas, progreso, avisos de cambios y detección de apps instaladas.
```

## Configuración de la tienda

| Campo | Valor |
|---|---|
| Categoría | Herramientas |
| Etiquetas sugeridas | Privacidad, Seguridad |
| Correo de contacto | Tu correo personal (obligatorio, se muestra en la ficha) |
| Sitio web | https://github.com/ricard-cairo/PrivacyShieldAI |
| Política de privacidad | https://ricard-cairo.github.io/PrivacyShieldAI/privacy-policy.html |
| Precio | Gratis |
| Anuncios | No contiene anuncios |
| Compras en la app | No |

## Recursos gráficos

| Recurso | Fichero | Estado |
|---|---|---|
| Icono 512×512 | `store/icon-512.png` | Listo |
| Gráfico de funciones 1024×500 | `store/feature-graphic.png` | Listo |
| Capturas de móvil (mín. 2, recomendado 4–8) | — | Pendiente: hacerlas en el emulador o móvil (lista, detalle con las dos tareas, modo oscuro) |

## Seguridad de los datos (Data safety)

| Pregunta | Respuesta |
|---|---|
| ¿Tu app recoge o comparte alguno de los tipos de datos de usuario obligatorios? | **No** |
| ¿Todos los datos se cifran en tránsito? | Sí (solo HTTPS); no se transmiten datos de usuario |
| ¿Los usuarios pueden pedir que se eliminen sus datos? | No aplica: no se recogen datos. El progreso se borra al desinstalar |

Justificación: el progreso y la lista de apps instaladas se procesan solo en el dispositivo, y Google no considera "recogida" lo que no sale del dispositivo. La única petición de red descarga un JSON público sin datos del usuario.

## Contenido de la app (cuestionarios)

| Apartado | Respuesta |
|---|---|
| Acceso a la app | Toda la funcionalidad está disponible sin restricciones (sin inicio de sesión) |
| Anuncios | No |
| Clasificación de contenido | Categoría "Utilidad, productividad, comunicación u otra"; responder "No" a violencia, sexo, lenguaje, drogas, apuestas, interacción entre usuarios y compras |
| Público objetivo | 18 años o más (no dirigida a niños) |
| Aplicación de noticias | No |
| Apps de salud / financieras / gubernamentales | No |
| Visibilidad de paquetes | No requiere declaración: la app usa `<queries>` con 16 paquetes concretos, no `QUERY_ALL_PACKAGES` |

## Prueba cerrada

- Pista: **Prueba cerrada – Alpha**.
- Al menos **12 testers** con cuenta de Google, en una lista de correos o un Grupo de Google.
- Deben seguir dentro **14 días seguidos**. Después: Panel > Solicitar acceso a producción.
