package com.example.photoeditor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MaskingActivity : AppCompatActivity() {
//    private lateinit var imageUri: Uri
    private lateinit var finalBitmap: Bitmap
    private lateinit var bitmapBefore: Bitmap
    private lateinit var bitmap: Bitmap
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_masking)
        init()
    }

    private fun init() {
        val applyButton = findViewById<Button>(R.id.applyButton)
        val backButton = findViewById<Button>(R.id.backButton)
        val changeButton = findViewById<Button>(R.id.changeButton)
//        val amountEditText = findViewById<EditText>(R.id.amountEditText)
        val radiusEditText = findViewById<EditText>(R.id.radiusEditText)
        val thresholdEditText = findViewById<EditText>(R.id.thresholdEditText)
//        val returnBackButton = findViewById<ImageButton>(R.id.returnBackButton)

        val uriStr = intent.getStringExtra(this.getString(R.string.imageUri))
        val uri = Uri.parse(uriStr)
        imageView = findViewById(R.id.imageViewEdit)
        imageView.setImageURI(uri)

        val drawable = imageView.drawable as BitmapDrawable
        bitmap = drawable.bitmap
        bitmapBefore = bitmap
        finalBitmap = bitmap

        changeButton.setOnClickListener {
//            val amount = amountEditText.text.toString().toDouble()
            val radius = radiusEditText.text.toString().toDouble()
            val threshold = thresholdEditText.text.toString().toInt()

            unsharpMasking(radius, threshold)
        }

        // возвращение к ChooseActivity без сохранения изменений
        backButton.setOnClickListener {
            val editIntent = Intent(this, ChooseActivity::class.java)
            editIntent.putExtra(this.getString(R.string.imageUri), uri.toString())
            startActivity(editIntent)
        }

        backButton.setOnClickListener {
            val editIntent = Intent(this, ChooseActivity::class.java)
            editIntent.putExtra(this.getString(R.string.imageUri), uri.toString())
            startActivity(editIntent)
        }

        // применение изменений и возврат на экран выбора действий
        applyButton.setOnClickListener {
            val outFile = createImageFile()
            try {
                FileOutputStream(outFile).use { out ->
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    val imageUri = Uri.parse("file://" + outFile.absolutePath)
                    val intent = Intent(this, ChooseActivity::class.java)
                    intent.putExtra(this.getString(R.string.imageUri), imageUri.toString())
                    startActivity(intent)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun unsharpMasking(radius: Double, threshold: Int){
        val width = bitmap.width
//        println(width)
        val height = bitmap.height
//        println(height)
        val startPixels = IntArray(width * height)
        val maskPixels = IntArray(width * height)
        val resultPixels = IntArray(width * height)
        bitmap.getPixels(startPixels, 0, width, 0, 0, width, height)
        val imageMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        gaussianBlur(startPixels, maskPixels, width, radius)
        comparison(startPixels, maskPixels, resultPixels, threshold)

        imageMask.setPixels(maskPixels, 0, width, 0, 0, width, height)
        imageView.setImageBitmap(imageMask)
        bitmapBefore = finalBitmap
        finalBitmap = imageMask

    }

    private fun gaussianBlur(start: IntArray, mask: IntArray, width: Int, radius: Double) {
        var numberString = 0
        for (i in start.indices) {
            var r = 0
            var g = 0
            var b = 0
            var numberRGB = 0


            if (i % width == 0) {
                numberString += 1
                //println(numberString)
            }

            //размытие строки
            for (j in 0..radius.toInt()) {
                if (i + j < numberString * width) {
                    r += (start[i + j] and 0x00FF0000 shr 16)
                    g += (start[i + j] and 0x0000FF00 shr 8)
                    b += (start[i + j] and 0x000000FF)
                    numberRGB += 1
                }

                if (i - j >= (numberString - 1) * width) {
                    r += (start[i - j] and 0x00FF0000 shr 16)
                    g += (start[i - j] and 0x0000FF00 shr 8)
                    b += (start[i - j] and 0x000000FF)
                    numberRGB += 1
                }

            }

            r /= numberRGB
            g /= numberRGB
            b /= numberRGB
            mask[i] = -0x1000000 or (r shl 16) or (g shl 8) or b

        }
    }

    private fun comparison(start: IntArray, mask: IntArray, result: IntArray, threshold: Int)
    {
        for (i in start.indices) {
            val rSrc = (start[i] and 0x00FF0000 shr 16)
            val gSrc = (start[i] and 0x0000FF00 shr 8)
            val bSrc = (start[i] and 0x000000FF)

            var rMask = (mask[i] and 0x00FF0000 shr 16)
            var gMask = (mask[i] and 0x0000FF00 shr 8)
            var bMask = (mask[i] and 0x000000FF)

            rMask = rSrc - rMask
            gMask = gSrc - gMask
            bMask = bSrc - bMask
            //mask[i] = -0x1000000 or (rMask shl 16) or (gMask shl 8) or bMask
            //mask[i] = mask[i] - src[i]
            //var threshold = - threshold

            /*if (mask[i] < threshold)
            {
                //mask[i] = -0x1000000 or (rSrc shl 16) or (gSrc shl 8) or bSrc
                result[i] = -0x1000000 or (rSrc shl 16) or (gSrc shl 8) or bSrc
            }
            else{
                result[i] = -0x1000000 or (rMask shl 16) or (gMask shl 8) or bMask
            }*/
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