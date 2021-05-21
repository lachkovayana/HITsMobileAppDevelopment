package com.example.photoeditor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class UnsharpMaskingActivity : AppCompatActivity() {
    private lateinit var imageUri: Uri
    private lateinit var finalBitmap: Bitmap
    private lateinit var bitmapBefore: Bitmap
    private lateinit var bitmap: Bitmap
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unsharp_masking)
        init()
    }

    private fun init() {

        val changeButton = findViewById<Button>(R.id.changeButton)
        val amountEditText = findViewById<EditText>(R.id.amountEditText)
        val radiusEditText = findViewById<EditText>(R.id.radiusEditText)
        val thresholdEditText = findViewById<EditText>(R.id.thresholdEditText)
        val returnBackButton = findViewById<ImageButton>(R.id.returnBackButton)
        val backButton = findViewById<Button>(R.id.backButton)
        val applyButton = findViewById<Button>(R.id.applyButton)

        val uriStr = intent.getStringExtra("imgUri")
        val uri = Uri.parse(uriStr)
        imageView = findViewById(R.id.imageViewEdit)
        imageView.setImageURI(uri)

        val drawable = imageView.drawable as BitmapDrawable
        bitmap = drawable.bitmap
        bitmapBefore = bitmap
        finalBitmap = bitmap

        // кнопка запуска алгоритма
        changeButton.setOnClickListener {
            val amount = amountEditText.text.toString().toDoubleOrNull()
            val radius = radiusEditText.text.toString().toDoubleOrNull()
            val threshold = thresholdEditText.text.toString().toIntOrNull()
            when {
                amount == null || radius == null || threshold == null -> {
                    Toast.makeText(this, "Введите корректные данные", Toast.LENGTH_SHORT).show()
                }
                amount < 0 || amount > 1 || threshold <= 0 || radius <= 0 -> {
                    Toast.makeText(this, "Введите корректные данные", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    unsharpMasking(radius, threshold, amount)
                }
            }

        }

        // кнопка отмены изменений
        returnBackButton.setOnClickListener {
            imageView.setImageBitmap(bitmapBefore)
            finalBitmap = bitmapBefore
        }

        // кнопка возвращения без сохранения
        backButton.setOnClickListener {
            val editIntent = Intent(this, ChooseActivity::class.java)
            editIntent.putExtra("imgUri", uri.toString())
            startActivity(editIntent)
        }

        // сохранения и перехода на другую страницу
        applyButton.setOnClickListener {
            val outFile = createImageFile()
            try {
                FileOutputStream(outFile).use { out ->
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    val imageUri = Uri.parse("file://" + outFile.absolutePath)
                    val intent = Intent(this, ChooseActivity::class.java)
                    intent.putExtra("imgUri", imageUri.toString())
                    startActivity(intent)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun unsharpMasking(radius: Double, threshold: Int, amount: Double) {
        val width = bitmap.width
        val height = bitmap.height

        val firstPixels = IntArray(width * height)
        val blurPixels = IntArray(width * height)
        val maskPixels = IntArray(width * height)
        val resultPixels = IntArray(width * height)

        bitmap.getPixels(firstPixels, 0, width, 0, 0, width, height)
        val imageMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        gaussianBlur(firstPixels, blurPixels, width, height, radius)
        maskCreation(firstPixels, blurPixels, maskPixels)
        maskApplication(firstPixels, maskPixels, resultPixels, amount, threshold)

        imageMask.setPixels(resultPixels, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(imageMask)
        bitmapBefore = finalBitmap
        finalBitmap = imageMask
    }

    // функция размытия
    private fun gaussianBlur(
        first: IntArray,
        blur: IntArray,
        width: Int,
        height: Int,
        radius: Double
    ) {
        var numberString = 0
        var r = 0
        var g = 0
        var b = 0
        var numberRGB = 0

        for (i in first.indices) {

            if (i % width == 0) {
                numberString += 1
            }

            for (j in 0..radius.toInt()) {
                // размытие строки
                if (i + j < numberString * width) {
                    r += (first[i + j] and 0x00F0000 shr 16)
                    g += (first[i + j] and 0x0000F00 shr 8)
                    b += (first[i + j] and 0x000000F)
                    numberRGB += 1
                }

                if (i - j >= (numberString - 1) * width) {
                    r += (first[i - j] and 0x00FF0000 shr 16)
                    g += (first[i - j] and 0x0000FF00 shr 8)
                    b += (first[i - j] and 0x000000FF)
                    numberRGB += 1
                }
                // размытие столбца
                if (i + j * width < height * width) {
                    r += (first[i + j * width] and 0x00FF0000 shr 16)
                    g += (first[i + j * width] and 0x0000FF00 shr 8)
                    b += (first[i + j * width] and 0x000000FF)
                    numberRGB += 1
                }

                if (i - j * width >= 0) {
                    r += (first[i - j * width] and 0x00FF0000 shr 16)
                    g += (first[i - j * width] and 0x0000FF00 shr 8)
                    b += (first[i - j * width] and 0x000000FF)
                    numberRGB += 1
                }
            }

            r /= numberRGB
            g /= numberRGB
            b /= numberRGB

            blur[i] = -0x1000000 or (r shl 16) or (g shl 8) or b

            r = 0
            g = 0
            b = 0
            numberRGB = 0
        }

    }

    // функция создания маски
    private fun maskCreation(first: IntArray, blur: IntArray, mask: IntArray) {
        for (i in first.indices) {
            var rFirst = (first[i] and 0x00FF0000 shr 16)
            var gFirst = (first[i] and 0x0000FF00 shr 8)
            var bFirst = (first[i] and 0x000000FF)

            var rBlur = (blur[i] and 0x00FF0000 shr 16)
            var gBlur = (blur[i] and 0x0000FF00 shr 8)
            var bBlur = (blur[i] and 0x000000FF)

            var rMask = rFirst - rBlur
            var gMask = gFirst - gBlur
            var bMask = bFirst - bBlur

            //rMask = pixelCheck(rMask)
            //gMask = pixelCheck(gMask)
            //bMask = pixelCheck(bMask)

            if (rMask < 0) {
                rMask = 0
            }
            if (gMask < 0) {
                gMask = 0
            }
            if (bMask < 0) {
                bMask = 0
            }

            if (rMask > 255) {
                rMask = 255
            }
            if (gMask > 255) {
                gMask = 255
            }
            if (bMask > 255) {
                bMask = 255
            }

            mask[i] = -0x1000000 or (rMask shl 16) or (gMask shl 8) or bMask
        }
    }

    // функция примения маски
    private fun maskApplication(
        first: IntArray,
        mask: IntArray,
        result: IntArray,
        amount: Double,
        threshold: Int
    ) {
        for (i in first.indices) {
            var rFirst = (first[i] and 0x00FF0000 shr 16)
            var gFirst = (first[i] and 0x0000FF00 shr 8)
            var bFirst = (first[i] and 0x000000FF)

            var rMask = (mask[i] and 0x00FF0000 shr 16)
            var gMask = (mask[i] and 0x0000FF00 shr 8)
            var bMask = (mask[i] and 0x000000FF)

            var r = 0
            var g = 0
            var b = 0

            var brightness = (0.2126 * rMask + 0.7152 * gMask + 0.0722 * bMask)

            when {
                brightness > threshold -> {
                    r = rFirst + (amount * rMask).toInt()
                    g = gFirst + (amount * gMask).toInt()
                    b = bFirst + (amount * bMask).toInt()

                    //r = pixelCheck(r)
                    //g = pixelCheck(g)
                    //b = pixelCheck(b)
                    if (r > 255) {
                        r = 255
                    }
                    if (g > 255) {
                        g = 255
                    }
                    if (b > 255) {
                        b = 255
                    }

                    if (r < 0) {
                        r = 0
                    }
                    if (g < 0) {
                        g = 0
                    }
                    if (b < 0) {
                        b = 0
                    }
                }
                else -> {
                    r = rFirst
                    g = gFirst
                    b = bFirst
                }
            }
            result[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
        }
    }

    private fun pixelCheck(pixel: Int): Int {
        var resultPixel = 0
        if (pixel > 255) resultPixel = 255
        if (pixel < 0) resultPixel = 0
        return (resultPixel)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "/JPEG_$timeStamp.jpg"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File(storageDir.toString() + imageFileName)
    }
}
