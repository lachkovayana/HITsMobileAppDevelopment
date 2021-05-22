package com.example.photoeditor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.*
import com.example.photoeditor.databinding.ActivityRotateBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RotateActivity : AppCompatActivity() {
    private lateinit var finalBitmap: Bitmap
    private lateinit var bitmapBefore: Bitmap
    private lateinit var bitmap: Bitmap

    //private lateinit var imageView: ImageView
    private lateinit var binding: ActivityRotateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRotateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {

        //получение и установка изображения из ChooseActivity
        val uriStr = intent.getStringExtra("imgUri")
        val uri = Uri.parse(uriStr)
        binding.imageViewEdit.setImageURI(uri)
        val drawable = binding.imageViewEdit.drawable as BitmapDrawable
        bitmap = drawable.bitmap
        bitmapBefore = bitmap
        finalBitmap = bitmap

        // кнопка запуска алгоритма
        binding.changeButton.setOnClickListener {
            val angleRotation = binding.angleRotationEditText.text.toString().toIntOrNull()
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
        binding.mirrorButton.setOnClickListener {
            mirrorImage()
        }

        // кнопка отмены всех изменения
        binding.returnBackButton.setOnClickListener {
            binding.imageViewEdit.setImageBitmap(bitmap)
            finalBitmap = bitmap
            bitmapBefore = bitmap
        }

        // кнопка отмены последнего изменения
        binding.returnButton.setOnClickListener {
            binding.imageViewEdit.setImageBitmap(bitmapBefore)
            finalBitmap = bitmapBefore
        }

        // кнопка возвращения без сохранения
        binding.backButton.setOnClickListener {
            val editIntent = Intent(this, ChooseActivity::class.java)
            editIntent.putExtra("imgUri", uri.toString())
            startActivity(editIntent)
        }

        // сохранения и перехода на другую страницу
        binding.applyButton.setOnClickListener {
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
        val width = finalBitmap.width
        val height = finalBitmap.height
        val firstPixels = IntArray(width * height)
        val resultPixels = IntArray(width * height)

        finalBitmap.getPixels(firstPixels, 0, width, 0, 0, width, height)

        var angle = angleRotation
        if (angleRotation < 0) angle += ((-angleRotation / 360) + 1) * 360

        when {
            (angle / 90) % 4 == 2 -> {
                val imageMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                rotation(firstPixels, resultPixels, angle, height, width)
                imageMask.setPixels(resultPixels, 0, width, 0, 0, width, height)
                binding.imageViewEdit.setImageBitmap(imageMask)
                bitmapBefore = finalBitmap
                finalBitmap = imageMask
            }
            (angle / 90) % 2 == 1 -> {
                val imageMask = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888)
                rotation(firstPixels, resultPixels, angle, height, width)
                imageMask.setPixels(resultPixels, 0, height, 0, 0, height, width)
                binding.imageViewEdit.setImageBitmap(imageMask)
                bitmapBefore = finalBitmap
                finalBitmap = imageMask
            }
            else -> {
                val imageMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                imageMask.setPixels(firstPixels, 0, width, 0, 0, width, height)
                binding.imageViewEdit.setImageBitmap(imageMask)
                bitmapBefore = finalBitmap
                finalBitmap = imageMask
            }
        }

    }

    private fun rotation(first: IntArray, result: IntArray, angle: Int, height: Int, width: Int) {
        when {
            (angle / 90) % 4 == 2 -> {
                for (i in first.indices) {
                    val r = (first[i] and 0x00FF0000 shr 16)
                    val g = (first[i] and 0x0000FF00 shr 8)
                    val b = (first[i] and 0x000000FF)

                    result[height * width - 1 - i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                }
            }
            (angle / 90) % 4 == 1 -> {
                var auxiliaryVariable = (height - 1) * width
                var numberString = 0
                for (i in first.indices) {
                    val r = (first[auxiliaryVariable - width * numberString] and 0x00FF0000 shr 16)
                    val g = (first[auxiliaryVariable - width * numberString] and 0x0000FF00 shr 8)
                    val b = (first[auxiliaryVariable - width * numberString] and 0x000000FF)

                    result[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                    numberString += 1
                    if (numberString == height) {
                        numberString = 0
                        auxiliaryVariable += 1
                    }
                }
            }
            (angle / 90) % 4 == 3 -> {
                var auxiliaryVariable = width - 1
                var numberString = 0
                for (i in first.indices) {
                    val r = (first[auxiliaryVariable + width * numberString] and 0x00FF0000 shr 16)
                    val g = (first[auxiliaryVariable + width * numberString] and 0x0000FF00 shr 8)
                    val b = (first[auxiliaryVariable + width * numberString] and 0x000000FF)

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
        val width = finalBitmap.width
        val height = finalBitmap.height
        val firstPixels = IntArray(width * height)
        val resultPixels = IntArray(width * height)

        finalBitmap.getPixels(firstPixels, 0, width, 0, 0, width, height)
        reflection(firstPixels, resultPixels, width, height)

        val imageMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        imageMask.setPixels(resultPixels, 0, width, 0, 0, width, height)
        binding.imageViewEdit.setImageBitmap(imageMask)
        bitmapBefore = finalBitmap
        finalBitmap = imageMask
    }

    private fun reflection(first: IntArray, result: IntArray, width: Int, height: Int) {
        var auxiliaryVariable = width - 1
        var numberString = 1
        for (i in first.indices) {
            val r = (first[auxiliaryVariable] and 0x00FF0000 shr 16)
            val g = (first[auxiliaryVariable] and 0x0000FF00 shr 8)
            val b = (first[auxiliaryVariable] and 0x000000FF)

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