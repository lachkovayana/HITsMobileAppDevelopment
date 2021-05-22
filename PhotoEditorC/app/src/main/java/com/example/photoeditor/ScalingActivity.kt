package com.example.photoeditor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.*
import com.example.photoeditor.databinding.ActivityChooseBinding
import com.example.photoeditor.databinding.ActivityScalingBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ScalingActivity : AppCompatActivity() {
    private lateinit var finalBitmap: Bitmap
    private lateinit var bitmapBefore: Bitmap
    private lateinit var bitmap: Bitmap
    private lateinit var binding: ActivityScalingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScalingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        val maxSize = 20000000

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
            val scalingFactor = binding.scalingFactorEditText.text.toString().toDoubleOrNull()
            println(scalingFactor)
            when {
                scalingFactor == null || (bitmap.width * scalingFactor.toInt() * bitmap.height * scalingFactor.toInt() > maxSize) -> {
                    Toast.makeText(this, "Введите корректные данные", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    imageScaling(scalingFactor)

                    if (finalBitmap.width != bitmapBefore.width && finalBitmap.height != bitmapBefore.height) {
                        Toast.makeText(this, "Алгоритм сработал корректно", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        // кнопка отмены изменений
        binding.returnBackButton.setOnClickListener {
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

    private fun imageScaling(scalingFactor: Double) {
        val width = bitmap.width
        val height = bitmap.height
        val newWidth = (width * scalingFactor).toInt()
        val newHeight = (height * scalingFactor).toInt()

        val firstPixels = IntArray(width * height)
        val resultPixels = IntArray(newWidth * newHeight)
        val imageMask = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

        bitmap.getPixels(firstPixels, 0, width, 0, 0, width, height)

        changeSize(firstPixels, resultPixels, width, newWidth, newHeight, scalingFactor)
        imageMask.setPixels(resultPixels, 0, newWidth, 0, 0, newWidth, newHeight)

        binding.imageViewEdit.setImageBitmap(imageMask)
        bitmapBefore = finalBitmap
        finalBitmap = imageMask
    }

    private fun changeSize(
        first: IntArray,
        result: IntArray,
        width: Int,
        newWidth: Int,
        newHeight: Int,
        scalingFactor: Double
    ) {
        var numberStringFirst: Int
        var numberStringHelp = 0
        var indexPixel: Int
        val helpPixels = IntArray(newWidth * newHeight)

        for (i in result.indices) {
            numberStringFirst = (numberStringHelp / scalingFactor).toInt()
            indexPixel = ((i % newWidth) / scalingFactor).toInt() + numberStringFirst * width

            if (i % newWidth == 0 && i != 0) {
                numberStringHelp += 1
            }

            val r = (first[indexPixel] and 0x00FF0000 shr 16)
            val g = (first[indexPixel] and 0x0000FF00 shr 8)
            val b = (first[indexPixel] and 0x000000FF)
            helpPixels[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
        }

        if (scalingFactor > 3) {
            var helpNumber = 0
            for (i in helpPixels.indices) {
                when (i) {
                    newWidth -> {
                        helpNumber = 1
                    }
                    newWidth * (newHeight - 1) -> {
                        helpNumber = 2
                    }
                }

                var r: Int
                var g: Int
                var b: Int

                when {
                    i % newWidth == 0 -> {
                        when (helpNumber) {
                            0 -> {
                                r = (helpPixels[i + newWidth + 1] and 0x00FF0000 shr 16)
                                g = (helpPixels[i + newWidth + 1] and 0x0000FF00 shr 8)
                                b = (helpPixels[i + newWidth + 1] and 0x000000FF)
                            }
                            2 -> {
                                r = (helpPixels[i - newWidth + 1] and 0x00FF0000 shr 16)
                                g = (helpPixels[i - newWidth + 1] and 0x0000FF00 shr 8)
                                b = (helpPixels[i - newWidth + 1] and 0x000000FF)
                            }
                            else -> {
                                r =
                                    ((helpPixels[i + newWidth + 1] and 0x00FF0000 shr 16) + (helpPixels[i - newWidth + 1] and 0x00FF0000 shr 16)) / 2
                                g =
                                    ((helpPixels[i + newWidth + 1] and 0x0000FF00 shr 8) + (helpPixels[i - newWidth + 1] and 0x0000FF00 shr 8)) / 2
                                b =
                                    ((helpPixels[i + newWidth + 1] and 0x000000FF) + (helpPixels[i - newWidth + 1] and 0x000000FF)) / 2
                            }
                        }
                    }
                    i % newWidth == newWidth - 1 -> {
                        when (helpNumber) {
                            0 -> {
                                r = (helpPixels[i + newWidth - 1] and 0x00FF0000 shr 16)
                                g = (helpPixels[i + newWidth - 1] and 0x0000FF00 shr 8)
                                b = (helpPixels[i + newWidth - 1] and 0x000000FF)
                            }
                            2 -> {
                                r = (helpPixels[i - newWidth - 1] and 0x00FF0000 shr 16)
                                g = (helpPixels[i - newWidth - 1] and 0x0000FF00 shr 8)
                                b = (helpPixels[i - newWidth - 1] and 0x000000FF)
                            }
                            else -> {
                                r =
                                    ((helpPixels[i + newWidth - 1] and 0x00FF0000 shr 16) + (helpPixels[i - newWidth - 1] and 0x00FF0000 shr 16)) / 2
                                g =
                                    ((helpPixels[i + newWidth - 1] and 0x0000FF00 shr 8) + (helpPixels[i - newWidth - 1] and 0x0000FF00 shr 8)) / 2
                                b =
                                    ((helpPixels[i + newWidth - 1] and 0x000000FF) + (helpPixels[i - newWidth - 1] and 0x000000FF)) / 2
                            }
                        }
                    }
                    else -> {
                        when (helpNumber) {
                            0 -> {
                                r =
                                    ((helpPixels[i + newWidth + 1] and 0x00FF0000 shr 16) + (helpPixels[i + newWidth - 1] and 0x00FF0000 shr 16)) / 2
                                g =
                                    ((helpPixels[i + newWidth + 1] and 0x0000FF00 shr 8) + (helpPixels[i + newWidth - 1] and 0x0000FF00 shr 8)) / 2
                                b =
                                    ((helpPixels[i + newWidth + 1] and 0x000000FF) + (helpPixels[i + newWidth - 1] and 0x000000FF)) / 2
                            }
                            2 -> {
                                r =
                                    ((helpPixels[i - newWidth + 1] and 0x00FF0000 shr 16) + (helpPixels[i - newWidth - 1] and 0x00FF0000 shr 16)) / 2
                                g =
                                    ((helpPixels[i - newWidth + 1] and 0x0000FF00 shr 8) + (helpPixels[i - newWidth - 1] and 0x0000FF00 shr 8)) / 2
                                b =
                                    ((helpPixels[i - newWidth + 1] and 0x000000FF) + (helpPixels[i - newWidth - 1] and 0x000000FF)) / 2
                            }
                            else -> {
                                r =
                                    ((helpPixels[i + newWidth + 1] and 0x00FF0000 shr 16) + (helpPixels[i + newWidth - 1] and 0x00FF0000 shr 16) + (helpPixels[i - newWidth + 1] and 0x00FF0000 shr 16) + (helpPixels[i - newWidth - 1] and 0x00FF0000 shr 16)) / 4
                                g =
                                    ((helpPixels[i + newWidth + 1] and 0x0000FF00 shr 8) + (helpPixels[i + newWidth - 1] and 0x0000FF00 shr 8) + (helpPixels[i - newWidth + 1] and 0x0000FF00 shr 8) + (helpPixels[i - newWidth - 1] and 0x0000FF00 shr 8)) / 4
                                b =
                                    ((helpPixels[i + newWidth + 1] and 0x000000FF) + (helpPixels[i + newWidth - 1] and 0x000000FF) + (helpPixels[i - newWidth + 1] and 0x000000FF) + (helpPixels[i - newWidth - 1] and 0x000000FF)) / 4
                            }
                        }
                    }
                }
                result[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
            }
        } else {
            for (i in helpPixels.indices) {
                val r = (helpPixels[i] and 0x00FF0000 shr 16)
                val g = (helpPixels[i] and 0x0000FF00 shr 8)
                val b = (helpPixels[i] and 0x000000FF)
                result[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
            }
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