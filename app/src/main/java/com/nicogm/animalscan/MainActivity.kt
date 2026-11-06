package com.nicogm.animalscan

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var preview: ImageView
    private lateinit var status: TextView
    private lateinit var subtitle: TextView
    private var classifier: ImageClassifier? = null
    private val stage = BuildConfig.STAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(28), dp(24), dp(24))
            setBackgroundColor(Color.rgb(248, 250, 252))
        }
        val scroll = ScrollView(this).apply { isFillViewport = true; addView(root) }
        setContentView(scroll)

        addText(root, "AnimalScan 🐾", 30, Color.rgb(15, 23, 42), true)
        addText(root, "Clasificador de animales · Versión $stage", 15, Color.DKGRAY, false)
        addText(root, "Perros  •  Gatos  •  Vacas  •  Conejos", 14, Color.rgb(71, 85, 105), false)
        preview = ImageView(this).apply {
            setBackgroundColor(Color.rgb(226, 232, 240))
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Vista previa de la fotografía"
        }
        root.addView(preview, LinearLayout.LayoutParams(-1, dp(290)).apply { topMargin = dp(22); bottomMargin = dp(14) })
        status = TextView(this).apply {
            gravity = Gravity.CENTER
            textSize = 19f
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(12), dp(18), dp(12), dp(18))
        }
        root.addView(status, LinearLayout.LayoutParams(-1, -2))
        subtitle = addText(root, "", 14, Color.DKGRAY, false)
        addButton(root, "📷  Tomar fotografía", Color.rgb(30, 64, 175)) {
            try { startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 100) }
            catch (ex: Exception) { showError("No se encontró una aplicación de cámara") }
        }
        addButton(root, "🖼  Abrir galería", Color.rgb(15, 118, 110)) {
            startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }, 101)
        }
        if (stage == 1) {
            setStatus("LISTO PARA TOMAR FOTOS", Color.rgb(30, 64, 175), "Versión 1: interfaz, cámara y galería.")
        } else {
            setStatus("LISTO PARA CLASIFICAR", Color.rgb(30, 64, 175), "Selecciona una fotografía para analizarla.")
            Thread {
                try {
                    val options = ImageClassifier.ImageClassifierOptions.builder()
                        .setBaseOptions(BaseOptions.builder().setModelAssetPath("efficientnet_lite0.tflite").build())
                        .setRunningMode(RunningMode.IMAGE).setMaxResults(1000).build()
                    classifier = ImageClassifier.createFromOptions(this, options)
                } catch (ex: Exception) {
                    runOnUiThread { showError("No se pudo abrir el modelo: ${ex.message}") }
                }
            }.start()
        }
    }

    @Deprecated("Compatibilidad con la cámara del sistema")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        try {
            val bitmap: Bitmap = when (requestCode) {
                100 -> (data?.extras?.get("data") as? Bitmap) ?: throw Exception("La cámara no entregó una imagen")
                101 -> {
                    val uri = data?.data ?: throw Exception("Imagen no seleccionada")
                    if (Build.VERSION.SDK_INT >= 28) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri)) { decoder, _, _ ->
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    }
                }
                else -> return
            }
            val ratio = minOf(1f, 768f / maxOf(bitmap.width, bitmap.height))
            val image = Bitmap.createScaledBitmap(bitmap, maxOf(1, (bitmap.width * ratio).toInt()), maxOf(1, (bitmap.height * ratio).toInt()), true)
            preview.setImageBitmap(image)
            if (stage == 1) {
                setStatus("FOTO CARGADA ✓", Color.rgb(22, 101, 52), "Evidencia v1: captura o selección correcta.")
            } else {
                setStatus("ANALIZANDO...", Color.rgb(30, 64, 175), "Procesando imagen en el dispositivo")
                Thread { classify(image) }.start()
            }
        } catch (ex: Exception) { showError(ex.message ?: "Error al leer la imagen") }
    }

    private fun classify(bitmap: Bitmap) {
        try {
            var model = classifier
            var attempts = 0
            while (model == null && attempts++ < 40) {
                Thread.sleep(150)
                model = classifier
            }
            if (model == null) throw Exception("El modelo todavía no está disponible")
            val results = model.classify(BitmapImageBuilder(bitmap).build())
            val predictions = results.classificationResult().classifications()
                .flatMap { it.categories() }
                .sortedByDescending { it.score() }
            // No basta con que una especie aparezca en algún lugar del top:
            // exigimos que la categoría MÁS probable pertenezca a nuestras cuatro especies.
            val best = predictions.firstOrNull()
            val label = best?.categoryName().orEmpty().lowercase(Locale.ROOT)
            val animal = species(label)
            val confidence = best?.score() ?: 0f
            val accepted = animal != null && (stage == 2 || confidence >= 0.30f)
            runOnUiThread {
                if (accepted) {
                    setStatus("CLASIFICADO COMO ${animal!!.uppercase(Locale.ROOT)} ✓", Color.rgb(22, 101, 52),
                        "Confianza: ${(confidence * 100).toInt()} % · Etiqueta original: ${best!!.categoryName()}")
                } else if (stage == 3) {
                    setStatus("NO SE PUEDE CLASIFICAR", Color.rgb(185, 28, 28),
                        "La imagen no cumple con las cuatro categorías permitidas. Etiqueta: ${best?.categoryName() ?: "desconocida"}")
                } else {
                    setStatus("SIN COINCIDENCIA", Color.rgb(185, 28, 28), "Versión 2: no se encontró una especie válida.")
                }
            }
        } catch (ex: Exception) { runOnUiThread { showError("Error de clasificación: ${ex.message}") } }
    }

    private fun species(s: String): String? {
        // ImageNet usa nombres de razas. Se agrupan por especie y NO por color o sexo.
        val dogTerms = listOf("dog", "terrier", "retriever", "spaniel", "poodle", "husky", "shepherd", "collie", "bulldog", "chihuahua", "pug", "beagle", "boxer", "mastiff", "pinscher", "dachshund", "malamute", "samoyed", "dalmatian", "papillon", "pekinese", "shih-tzu", "affenpinscher", "weimaraner", "vizsla", "greyhound", "whippet", "saluki", "borzoi", "basenji", "keeshond", "schipperke", "newfoundland", "great dane", "saint bernard", "rottweiler", "doberman", "corgi", "schnauzer", "setter", "hound", "spitz", "lhasa", "briard", "komondor", "kuvasz", "bouvier", "kelpie", "clumber", "malinois", "groenendael", "affenpinscher", "affenpinscher", "dingo")
        return when {
            listOf("cat", "tabby", "egyptian mau", "siamese", "persian").any { it in s } -> "Gato"
            listOf("wood rabbit", "rabbit", "angora").any { it in s } -> "Conejo"
            listOf("ox", "cow", "bull", "cattle", "bovine").any { it in s } -> "Vaca"
            dogTerms.any { it in s } -> "Perro"
            else -> null
        }
    }

    private fun setStatus(message: String, color: Int, detail: String) {
        status.text = message
        status.setTextColor(Color.WHITE)
        status.background = GradientDrawable().apply { setColor(color); cornerRadius = dp(14).toFloat() }
        subtitle.text = detail
    }
    private fun showError(message: String) = setStatus("ERROR", Color.rgb(185, 28, 28), message)
    private fun addText(root: LinearLayout, text: String, size: Int, color: Int, bold: Boolean): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
            root.addView(this, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(9) })
        }
    }
    private fun addButton(root: LinearLayout, text: String, color: Int, action: () -> Unit) {
        val button = Button(this).apply {
            this.text = text
            isAllCaps = false
            setTextColor(Color.WHITE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(color)
            setOnClickListener { action() }
        }
        root.addView(button, LinearLayout.LayoutParams(-1, dp(58)).apply { topMargin = dp(10) })
    }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    override fun onDestroy() { classifier?.close(); super.onDestroy() }
}
