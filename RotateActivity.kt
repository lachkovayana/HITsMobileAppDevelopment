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
import kotlin.math.abs

class RotateActivity : AppCompatActivity() {
    private lateinit var imageUri: Uri
    private lateinit var finalBitmap: Bitmap
    private lateinit var bitmapBefore: Bitmap
    private lateinit var bitmap: Bitmap
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rotate)
        init()
    }

    private fun init() {
        val changeButton = findViewById<Button>(R.id.changeButton)
        val returnBackButton = findViewById<ImageButton>(R.id.returnBackButton)
        val backButton = findViewById<Button>(R.id.backButton)
        val applyButton = findViewById<Button>(R.id.applyButton)
        val mirrorButton = findViewById<Button>(R.id.mirrorButton)
        val angleRotationEditText = findViewById<EditText>(R.id.angleRotationEditText)

        //получение и установка изображения из ChooseActivity
        val uriStr = intent.getStringExtra("imgUri")
        val uri = Uri.parse(uriStr)
        imageView = findViewById(R.id.imageViewEdit)
        imageView.setImageURI(uri)

        // кнопка запуска алгоритма
        changeButton.setOnClickListener {
            val drawable = imageView.drawable as BitmapDrawable
            bitmap = drawable.bitmap
            bitmapBefore = bitmap
            finalBitmap = bitmap
            val angleRotation = angleRotationEditText.text.toString().toIntOrNull()
            when {
                angleRotation == null || angleRotation % 90 != 0 -> {
                    Toast.makeText(this, "Введите корректные данные", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    imageRotation(angleRotation)
                }
            }
        }

        // кнопка отзеркаливания изображения
        mirrorButton.setOnClickListener {
            val drawable = imageView.drawable as BitmapDrawable
            bitmap = drawable.bitmap
            bitmapBefore = bitmap
            finalBitmap = bitmap
            mirrorImage()
        }

        // кнопка отмены последнего изменения
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

    private fun imageRotation(angleRotation: Int) {
        val width = bitmap.width
        val height = bitmap.height
        val firstPixels = IntArray(width * height)
        val resultPixels = IntArray(width * height)

        bitmap.getPixels(firstPixels, 0, width, 0, 0, width, height)

        var angle = angleRotation
        if (angleRotation < 0) angle += ((-angleRotation / 360) + 1) * 360

        when {
            (angle / 90) % 4 == 2 -> {
                val imageMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                rotation(firstPixels, resultPixels, angle, height, width)
                imageMask.setPixels(resultPixels, 0, width, 0, 0, width, height)
                imageView.setImageBitmap(imageMask)
                bitmapBefore = finalBitmap
                finalBitmap = imageMask
            }
            (angle / 90) % 2 == 1 -> {
                val imageMask = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888)
                rotation(firstPixels, resultPixels, angle, height, width)
                imageMask.setPixels(resultPixels, 0, height, 0, 0, height, width)
                imageView.setImageBitmap(imageMask)
                bitmapBefore = finalBitmap
                finalBitmap = imageMask
            }
            else -> {
                val imageMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                imageMask.setPixels(firstPixels, 0, width, 0, 0, width, height)
                imageView.setImageBitmap(imageMask)
                bitmapBefore = finalBitmap
                finalBitmap = imageMask
            }
        }

    }

    private fun rotation(first: IntArray, result: IntArray, angle: Int, height: Int, width: Int) {
        when {
            (angle / 90) % 4 == 2 -> {
                for (i in first.indices) {
                    var r = (first[i] and 0x00FF0000 shr 16)
                    var g = (first[i] and 0x0000FF00 shr 8)
                    var b = (first[i] and 0x000000FF)

                    result[height*width-1-i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                }
            }
            (angle / 90) % 4 == 1 -> {
                var auxiliaryVariable = (height-1)*width
                var numberString = 0
                for (i in first.indices) {
                    var r = (first[auxiliaryVariable-width*numberString] and 0x00FF0000 shr 16)
                    var g = (first[auxiliaryVariable-width*numberString] and 0x0000FF00 shr 8)
                    var b = (first[auxiliaryVariable-width*numberString] and 0x000000FF)

                    result[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                    numberString += 1
                    if (numberString == height) {
                        numberString = 0
                        auxiliaryVariable += 1
                    }
                }
            }
            (angle / 90) % 4 == 3 -> {
                var auxiliaryVariable = width-1
                var numberString = 0
                for (i in first.indices) {
                    var r = (first[auxiliaryVariable+width*numberString] and 0x00FF0000 shr 16)
                    var g = (first[auxiliaryVariable+width*numberString] and 0x0000FF00 shr 8)
                    var b = (first[auxiliaryVariable+width*numberString] and 0x000000FF)

                    result[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                    numberString += 1
                    if (numberString == height) {
                        numberString = 0
                        auxiliaryVariable -= 1
                    }
                }
            }
        }
    }

    private fun mirrorImage() {
        val width = bitmap.width
        val height = bitmap.height
        val firstPixels = IntArray(width * height)
        val resultPixels = IntArray(width * height)

        bitmap.getPixels(firstPixels, 0, width, 0, 0, width, height)
        reflection(firstPixels, resultPixels, width, height)

        val imageMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        imageMask.setPixels(resultPixels, 0, width, 0, 0, width, height)
        imageView.setImageBitmap(imageMask)
        bitmapBefore = finalBitmap
        finalBitmap = imageMask
    }

    private fun reflection(first: IntArray, result: IntArray, width: Int, height: Int) {
        var auxiliaryVariable = width-1
        var numberString = 1
        for (i in first.indices) {
            var r = (first[auxiliaryVariable] and 0x00FF0000 shr 16)
            var g = (first[auxiliaryVariable] and 0x0000FF00 shr 8)
            var b = (first[auxiliaryVariable] and 0x000000FF)

            result[i] = -0x1000000 or (r shl 16) or (g shl 8) or b

            if (auxiliaryVariable % width == 0) {
                numberString += 1
                auxiliaryVariable = width * numberString
            }
            auxiliaryVariable -= 1
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