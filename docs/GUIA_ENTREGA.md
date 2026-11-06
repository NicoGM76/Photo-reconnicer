# Guía de entrega · AnimalScan

## Pasos rápidos
1. Clonar el repositorio o ejecutar git pull origin main.
2. Abrir carpeta raíz en Android Studio (JDK 17, Android SDK 35).
3. Esperar Gradle Sync. La primera compilación descarga el modelo desde Google.
4. En el panel Build Variants elegir v1Debug, v2Debug o v3Debug.
5. Ejecutar las tres versiones, tomar capturas reales y registrar las pruebas.
6. Compilar el APK final con el comando de PowerShell siguiente:

    .\gradlew.bat :app:assembleV3Debug

APK final: app/build/outputs/apk/v3/debug/app-v3-debug.apk

## Evidencias requeridas
- Tres capturas reales de la aplicación: V1 (foto), V2 (clasificación), V3 (rechazo rojo).
- Documento de evidencias: usar docs/PLANTILLA_INFORME.md como base.
- Foto real del grupo trabajando.
- APK instalable V3.

## Validar
- Probar distintos perros/gatos y también vaca y conejo.
- Probar una taza, silla u objeto y comprobar que en V3 aparezca NO SE PUEDE CLASIFICAR en rojo.
- Si el modelo falla con una foto real, registrar el caso y ajustar el umbral o etiquetas.

## Alternativa para obtener APK
GitHub > Actions > Compilar AnimalScan APK > ejecución exitosa > Artifacts > AnimalScan-APK-v1-v2-v3.

## Precaución técnica
EfficientNet-Lite0 es un clasificador ImageNet general, no un detector de clases desconocidas. Puede confundir imágenes; no prometer exactitud universal.
