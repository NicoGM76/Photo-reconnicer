# AnimalScan 🐾

Aplicación Android Studio que clasifica fotografías como **Perro, Gato, Vaca y Conejo**. En V3 rechaza imágenes que no son de las cuatro clases, con un aviso **rojo**: NO SE PUEDE CLASIFICAR.

## Tres versiones para el taller

| Versión | Variante (Build Variants) | Evidencia |
|---|---|---|
| V1 | v1Debug | Captura/selección de foto |
| V2 | v2Debug | IA muestra especie identificada |
| V3 | v3Debug | Clasifica y rechaza con alerta roja |

## Abrir y sincronizar en PowerShell

Primera vez:

    cd "$HOME\Downloads"
    git clone https://github.com/NicoGM76/Photo-reconnicer.git
    cd Photo-reconnicer

Si ya lo clonaste:

    cd Photo-reconnicer
    git switch main
    git pull origin main

Android Studio > **Open** > selecciona la carpeta clonada > espera **Gradle Sync**. Usar JDK 17 y conexión a Internet la primera vez (Gradle descarga dependencias y el modelo EfficientNet-Lite0). Seleccionar **Build Variants > v3Debug** y ejecutar en celular o emulador.

## Compilar APK final

PowerShell:

    .\gradlew.bat :app:assembleV3Debug

El archivo de entrega queda en:

    app/build/outputs/apk/v3/debug/app-v3-debug.apk

Para generar todas las evidencias, compilar también V1 y V2:

    .\gradlew.bat :app:assembleV1Debug :app:assembleV2Debug :app:assembleV3Debug

Alternativa desde el navegador: GitHub > **Actions** > **Compilar AnimalScan APK** > ejecución exitosa > **Artifacts** > **AnimalScan-APK-v1-v2-v3**.

## Documentación y limitaciones

- Consultar docs/GUIA_ENTREGA.md y docs/PLANTILLA_INFORME.md.
- Modelo preentrenado EfficientNet-Lite0 para ImageNet, integrado con MediaPipe.
- El modelo se descarga al compilar; luego queda incluido en el APK.
- La clasificación no es perfecta: probar con razas, colores, conejos y vacas diferentes y con objetos que deban rechazarse. No confundir el resultado de una predicción con certeza absoluta.
- Las capturas y foto grupal deben ser reales. No están incluidas en este repositorio.

## Fuentes
- https://developers.google.com/edge/mediapipe/solutions/vision/image_classifier/android
- https://developers.google.com/ml-kit/vision/image-labeling
