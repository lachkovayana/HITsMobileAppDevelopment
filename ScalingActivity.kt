package com.example.photoeditor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.style.LineHeightSpan
import android.widget.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ScalingActivity : AppCompatActivity() {
    private lateinit var imageUri: Uri
    private lateinit var finalBitmap: Bitmap
    private lateinit var bitmapBefore: Bitmap
    private lateinit var bitmap: Bitmap
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scaling)
        init()
    }

    private fun init() {
        val changeButton = findViewById<Button>(R.id.changeButton)
        val returnBackButton = findViewById<ImageButton>(R.id.returnBackButton)
        val backButton = findViewById<Button>(R.id.backButton)
        val applyButton = findViewById<Button>(R.id.applyButton)
        val scalingFactorEditText = findViewById<EditText>(R.id.scalingFactorEditText)
        val maxSize = 20000000

        //получение и установка изображения из ChooseActivity
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
            val scalingFactor = scalingFactorEditText.text.toString().toDoubleOrNull()
            println(scalingFactor)
            when {
                scalingFactor == null || (bitmap.width*scalingFactor.toInt()*bitmap.height*scalingFactor.toInt() > maxSize)-> {
                    Toast.makeText(this, "Введите корректные данные", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    imageScaling (scalingFactor)
                }
            }
        }

        // кнопка отмены изменений
        returnBackButton.setOnClickListener {
            imageView.setImageBitmap(bitmapBefore)
            finalBitmap = bitmapBefore.also { bitmapBefore = finalBitmap }
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

    private fun imageScaling(scalingFactor: Double) {
        val width = bitmap.width
        val height = bitmap.height
        val newWidth = (width*scalingFactor).toInt()
        val newHeight = (height*scalingFactor).toInt()

        /*println(width)
        println(height)
        println(newWidth)
        println(newHeight)*/

        val firstPixels = IntArray(width * height)
        val resultPixels = IntArray(newWidth * newHeight)
        val imageMask = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

        bitmap.getPixels(firstPixels, 0, width, 0, 0, width, height)
        when {
            scalingFactor > 1 -> {
                // вызов функции увеличение размера изображения
                imageEnlargement(firstPixels, resultPixels, width, newWidth, newHeight, scalingFactor)
                imageMask.setPixels(resultPixels, 0, newWidth, 0, 0, newWidth, newHeight)
            }
            scalingFactor.toInt() == 1 -> {
                imageMask.setPixels(firstPixels, 0, newWidth, 0, 0, newWidth, newHeight)
            }
            scalingFactor < 1 -> {
                // вызов функции уменьшения размера изображения
                imageReduction(firstPixels, resultPixels, width, newWidth, newHeight, scalingFactor)
                imageMask.setPixels(resultPixels, 0, newWidth, 0, 0, newWidth, newHeight)
            }
        }

        imageView.setImageBitmap(imageMask)
        bitmapBefore = finalBitmap
        finalBitmap = imageMask
    }

    private fun imageEnlargement (first: IntArray, result: IntArray, width: Int, newWidth: Int, newHeight: Int, scalingFactor: Double ) {
        var indexResult = 0
        val helpPixels = IntArray(newWidth * newHeight)

        for (i in first.indices) {
            when {
                i % width == 0 && i != 0 -> {
                    indexResult += newWidth*(scalingFactor.toInt()-1)
                }
            }

            var r = (first[i] and 0x00FF0000 shr 16)
            var g = (first[i] and 0x0000FF00 shr 8)
            var b = (first[i] and 0x000000FF)

            for (j in 1..scalingFactor.toInt()) {
                helpPixels[indexResult] = -0x1000000 or (r shl 16) or (g shl 8) or b
                for (n in 1 until scalingFactor.toInt()) {
                    helpPixels[indexResult+newWidth*n] = -0x1000000 or (r shl 16) or (g shl 8) or b
                }
                indexResult += 1
            }
        }

        var helpNumber = 0
        for (i in helpPixels.indices) {
            when (i) {
                newWidth -> {
                    helpNumber = 1
                }
                newWidth*(newHeight-1) -> {
                    helpNumber = 2
                }
            }

            var r = 0
            var g = 0
            var b = 0

            when {
                i % newWidth == 0 -> {
                    when (helpNumber) {
                        0 -> {
                            r = (helpPixels[i+newWidth+1] and 0x00FF0000 shr 16)
                            g = (helpPixels[i+newWidth+1] and 0x0000FF00 shr 8)
                            b = (helpPixels[i+newWidth+1] and 0x000000FF)
                        }
                        2 -> {
                            r = (helpPixels[i-newWidth+1] and 0x00FF0000 shr 16)
                            g = (helpPixels[i-newWidth+1] and 0x0000FF00 shr 8)
                            b = (helpPixels[i-newWidth+1] and 0x000000FF)
                        }
                        else -> {
                            r = ((helpPixels[i+newWidth+1] and 0x00FF0000 shr 16)+(helpPixels[i-newWidth+1] and 0x00FF0000 shr 16))/2
                            g = ((helpPixels[i+newWidth+1] and 0x0000FF00 shr 8)+(helpPixels[i-newWidth+1] and 0x0000FF00 shr 8))/2
                            b = ((helpPixels[i+newWidth+1] and 0x000000FF)+(helpPixels[i-newWidth+1] and 0x000000FF))/2
                        }
                    }
                }
                i % newWidth == newWidth-1 -> {
                    when (helpNumber) {
                        0 -> {
                            r = (helpPixels[i+newWidth-1] and 0x00FF0000 shr 16)
                            g = (helpPixels[i+newWidth-1] and 0x0000FF00 shr 8)
                            b = (helpPixels[i+newWidth-1] and 0x000000FF)
                        }
                        2 -> {
                            r = (helpPixels[i-newWidth-1] and 0x00FF0000 shr 16)
                            g = (helpPixels[i-newWidth-1] and 0x0000FF00 shr 8)
                            b = (helpPixels[i-newWidth-1] and 0x000000FF)
                        }
                        else -> {
                            r = ((helpPixels[i+newWidth-1] and 0x00FF0000 shr 16)+(helpPixels[i-newWidth-1] and 0x00FF0000 shr 16))/2
                            g = ((helpPixels[i+newWidth-1] and 0x0000FF00 shr 8)+(helpPixels[i-newWidth-1] and 0x0000FF00 shr 8))/2
                            b = ((helpPixels[i+newWidth-1] and 0x000000FF)+(helpPixels[i-newWidth-1] and 0x000000FF))/2
                        }
                    }
                }
                else -> {
                    when (helpNumber) {
                        0 -> {
                            r = ((helpPixels[i+newWidth+1] and 0x00FF0000 shr 16)+(helpPixels[i+newWidth-1] and 0x00FF0000 shr 16))/2
                            g = ((helpPixels[i+newWidth+1] and 0x0000FF00 shr 8)+(helpPixels[i+newWidth-1] and 0x0000FF00 shr 8))/2
                            b = ((helpPixels[i+newWidth+1] and 0x000000FF)+(helpPixels[i+newWidth-1] and 0x000000FF))/2
                        }
                        2 -> {
                            r = ((helpPixels[i-newWidth+1] and 0x00FF0000 shr 16)+(helpPixels[i-newWidth-1] and 0x00FF0000 shr 16))/2
                            g = ((helpPixels[i-newWidth+1] and 0x0000FF00 shr 8)+(helpPixels[i-newWidth-1] and 0x0000FF00 shr 8))/2
                            b = ((helpPixels[i-newWidth+1] and 0x000000FF)+(helpPixels[i-newWidth-1] and 0x000000FF))/2
                        }
                        else -> {
                            r = ((helpPixels[i+newWidth+1] and 0x00FF0000 shr 16)+(helpPixels[i+newWidth-1] and 0x00FF0000 shr 16)+(helpPixels[i-newWidth+1] and 0x00FF0000 shr 16)+(helpPixels[i-newWidth-1] and 0x00FF0000 shr 16))/4
                            g = ((helpPixels[i+newWidth+1] and 0x0000FF00 shr 8)+(helpPixels[i+newWidth-1] and 0x0000FF00 shr 8)+(helpPixels[i-newWidth+1] and 0x0000FF00 shr 8)+(helpPixels[i-newWidth-1] and 0x0000FF00 shr 8))/4
                            b = ((helpPixels[i+newWidth+1] and 0x000000FF)+(helpPixels[i+newWidth-1] and 0x000000FF)+(helpPixels[i-newWidth+1] and 0x000000FF)+(helpPixels[i-newWidth-1] and 0x000000FF))/4
                        }
                    }
                }
            }

            result[i] = -0x1000000 or (r shl 16) or (g shl 8) or b

        }
    }

    private fun imageReduction (first: IntArray, result: IntArray, width: Int, newWidth: Int, newHeight: Int, scalingFactor: Double ) {
        var indexResult = 0
        //var numberPixel = 0
        var helper = 0
        val helpPixels = IntArray(newWidth * newHeight)

        for (i in helpPixels.indices) {
            /*
            when {
                i % newWidth == 0 && i != 0 -> {
                    indexResult += width*((width/newWidth).toInt()-1)
                }
            }

            var r = 0
            var g = 0
            var b = 0

            for (j in 1..(width/newWidth).toInt()) {
                r += (first[indexResult] and 0x00FF0000 shr 16)
                g += (first[indexResult] and 0x0000FF00 shr 8)
                b += (first[indexResult] and 0x000000FF)
                numberPixel += 1

                for (n in 1 until (width/newWidth).toInt()) {
                    r += (first[indexResult+width*n] and 0x00FF0000 shr 16)
                    g += (first[indexResult+width*n] and 0x0000FF00 shr 8)
                    b += (first[indexResult+width*n] and 0x000000FF)
                    numberPixel += 1
                }
                indexResult += 1
            }

            r /= numberPixel
            g /= numberPixel
            b /= numberPixel
            result[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
            numberPixel = 0*/

            if (i % newWidth == 0) {
                indexResult = width * helper
                helper += (width/newWidth)
            }

            //var r = 0
            //var g = 0
            //var b = 0

            var r = (first[indexResult] and 0x00FF0000 shr 16)
            var g = (first[indexResult] and 0x0000FF00 shr 8)
            var b = (first[indexResult] and 0x000000FF)
            result[i] = -0x1000000 or (r shl 16) or (g shl 8) or b

            indexResult += (width/newWidth)

        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "/JPEG_$timeStamp.jpg"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File(storageDir.toString() + imageFileName)
    }
}