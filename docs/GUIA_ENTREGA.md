# Guía de entrega del taller AnimalScan

## Requisitos
Android Studio con JDK 17, Android SDK 35, acceso a Internet la primera vez (Gradle descarga EfficientNet-Lite0), teléfono Android 8+ o emulador.

## Abrir el proyecto
1. Clonar `https://github.com/NicoGM76/Photo-reconnicer.git`.
2. Abrir la carpeta en Android Studio, no crear un proyecto encima.
3. Esperar Gradle Sync y primera descarga del modelo.
4. En **Build Variants**, elegir `v1Debug`, `v2Debug` o `v3Debug`.
5. Ejecutar con ▶ Run. No se requiere entrenamiento ni API key.

## Las tres versiones / capturas
- **v1Debug**: cámara/galería y vista previa. Tomar captura de foto cargada.
- **v2Debug**: modelo integrado, predicción y etiqueta de especie. Captura con perro/gato.
- **v3Debug**: validación con umbral y error rojo. Captura con un objeto que no sea animal.
Las variantes son versiones de software distintas, no tres capturas simuladas.

## Generar el APK final
Android Studio > Build > Generate App Bundles or APKs > Build APK(s) con `v3Debug` seleccionado.
Ruta: `app/build/outputs/apk/v3/debug/app-v3-debug.apk`.
De ser necesario en PowerShell: `.\gradlew.bat assembleV3Debug` (si hay wrapper Gradle) o usar Android Studio.

## Casos de prueba para el documento
| Prueba | Resultado esperado |
|---|---|
| Perro de otra raza/color | PERRO en verde |
| Gato | GATO en verde |
| Vaca | VACA en verde |
| Conejo | CONEJO en verde |
| Taza, silla o computador | NO SE PUEDE CLASIFICAR en rojo |

**Importante:** EfficientNet es un clasificador ImageNet genérico, no un detector de mundo abierto. Puede confundirse con razas, vacas y objetos parecidos. El filtro por confianza ayuda, pero no garantiza rechazo perfecto. La foto de cámara se recibe como miniatura para reducir tiempo de desarrollo.

## Entregables
1. APK `app-v3-debug.apk` (instalable, compilado en Android Studio).
2. Tres capturas reales de las variantes v1/v2/v3.
3. Documento en PDF o Word con objetivo, tecnología, 5 pruebas, capturas y conclusión.
4. Fotografía auténtica del grupo trabajando en el taller.

## Problemas comunes
- **No descarga el modelo:** descargar manualmente el archivo desde `https://storage.googleapis.com/mediapipe-models/image_classifier/efficientnet_lite0/int8/latest/efficientnet_lite0.tflite` y guardarlo como `app/src/main/assets/efficientnet_lite0.tflite`.
- **Sin cámara en emulador:** usar el botón Abrir galería.
- **Gradle no sincroniza:** verificar JDK 17 y conexión a Google/Maven Central.
- **No existe gradlew:** el repositorio no incluye el wrapper binario; usar las herramientas Gradle integradas de Android Studio.
