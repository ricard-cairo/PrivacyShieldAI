# Publicar en Google Play

Guía paso a paso para la primera publicación de Privacy Shield AI (`io.github.rc.privacyshieldai`).
Textos de la ficha y respuestas de los cuestionarios: [ficha-google-play.md](ficha-google-play.md).

## 1. Crear la clave de subida (una sola vez)

La clave firma los `.aab` que subes a Play. Google guarda la clave final (Play App Signing).

En el terminal de Android Studio, desde la carpeta del proyecto:

```
"C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -genkeypair -v -keystore ..\privacyshieldai-upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
```

- Crea `privacyshieldai-upload.jks` **fuera** del proyecto (en `AndroidStudioProjects`), así nunca llega a GitHub.
- Pide dos contraseñas y tus datos (nombre, país). Usa contraseñas largas.
- **Haz copia de seguridad** del `.jks` y de las contraseñas (gestor de contraseñas). Si se pierden, hay que pedir a Google un cambio de clave.

Después copia `keystore.properties.example` como `keystore.properties` (en la raíz del proyecto) y rellena las contraseñas. Ese fichero está en `.gitignore`.

## 2. Publicar la política de privacidad (GitHub Pages)

1. Sube los cambios a GitHub (`git push`).
2. En GitHub: repositorio > **Settings > Pages** > *Source*: **Deploy from a branch** > rama `main`, carpeta `/docs` > **Save**.
3. En unos minutos estará en: https://ricard-cairo.github.io/PrivacyShieldAI/privacy-policy.html

## 3. Generar el `.aab`

1. Sube `versionCode` en `app/build.gradle.kts` en cada nueva subida (1, 2, 3...).
2. Ejecuta `gradlew.bat testDebugUnitTest` y prueba la app en un móvil.
3. `gradlew.bat bundleRelease` (o **Build > Generate Signed App Bundle**).
4. El fichero queda en `app\build\outputs\bundle\release\app-release.aab`.

## 4. Play Console

1. Crea la cuenta de desarrollador personal (25 $) y completa la verificación de identidad.
2. **Crear app**: nombre "Privacy Shield AI", idioma español (España), App, Gratis.
3. **Contenido de la app**: política de privacidad, anuncios, acceso, clasificación, público objetivo y seguridad de los datos (respuestas en `ficha-google-play.md`).
4. **Ficha de Play Store**: textos, icono `store/icon-512.png`, gráfico `store/feature-graphic.png` y capturas.
5. **Pruebas > Prueba cerrada**: crea la pista, añade a los 12+ testers, sube el `.aab` y envía a revisión. Comparte con ellos el enlace de participación.
6. Tras **14 días seguidos** con 12+ testers: **Panel > Solicitar acceso a producción**.
7. Con el acceso aprobado: **Producción > Crear versión**, sube el `.aab` y envía a revisión.

## Antes de cada versión

- [ ] `versionCode` incrementado y `versionName` actualizado
- [ ] Tests pasados
- [ ] Probada en un móvil real (modo claro y oscuro)
- [ ] Notas de la versión escritas
