package com.example.photoeditor

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView

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
import android.widget.ImageButton
import android.widget.ImageView
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

        changeButton.setOnClickListener {
            val amount = amountEditText.text.toString().toDouble()
            val radius = radiusEditText.text.toString().toDouble()
            val threshold = thresholdEditText.text.toString().toDouble()

            unsharpMasking(radius, threshold, amount)
        }

        returnBackButton.setOnClickListener {
            imageView.setImageBitmap(bitmapBefore)
            finalBitmap = bitmapBefore.also { bitmapBefore = finalBitmap }
        }

        backButton.setOnClickListener {
            val editIntent = Intent(this, ChooseActivity::class.java)
            editIntent.putExtra("imgUri", uri.toString())
            startActivity(editIntent)
        }

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

    private fun unsharpMasking(radius: Double, threshold: Double, amount: Double){
        val width = bitmap.width
        println(width)
        val height = bitmap.height
        println(height)
        val firstPixels = IntArray(width * height)
        val secondPixels = IntArray(width * height)
        val contrastPixels = IntArray(width * height)
        val maskPixels = IntArray(width * height)
        val resultPixels = IntArray(width * height)
        bitmap.getPixels(firstPixels, 0, width, 0, 0, width, height)
        val imageMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)


        gaussianBlur(firstPixels, secondPixels, width, radius)
        maskCreation(firstPixels, secondPixels, maskPixels)
        increaseContrast(firstPixels, maskPixels, contrastPixels, amount, threshold)
        comparison(firstPixels, secondPixels, maskPixels, contrastPixels, resultPixels, threshold)

        imageMask.setPixels(resultPixels, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(imageMask)
        bitmapBefore = finalBitmap
        finalBitmap = imageMask
    }

    private fun gaussianBlur(first: IntArray, second: IntArray, width: Int, radius: Double) {
        var numberString = 0
        for (i in first.indices) {
            var r = 0
            var g = 0
            var b = 0
            var numberRGB = 0


            if (i % width == 0) {
                numberString += 1
            }

            //размытие строки
            for (j in 0..radius.toInt()) {
                if (i + j < numberString * width) {
                    r += (first[i + j] and 0x00FF0000 shr 16)
                    g += (first[i + j] and 0x0000FF00 shr 8)
                    b += (first[i + j] and 0x000000FF)
                    numberRGB += 1
                }

                if (i - j >= (numberString - 1) * width) {
                    r += (first[i - j] and 0x00FF0000 shr 16)
                    g += (first[i - j] and 0x0000FF00 shr 8)
                    b += (first[i - j] and 0x000000FF)
                    numberRGB += 1
                }

            }

            r /= numberRGB
            g /= numberRGB
            b /= numberRGB
            second[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
        }
    }

    private fun maskCreation (first: IntArray, second: IntArray, mask: IntArray){
        for (i in first.indices) {
            var rFirst = (first[i] and 0x00FF0000 shr 16)
            var gFirst = (first[i] and 0x0000FF00 shr 8)
            var bFirst = (first[i] and 0x000000FF)

            var rSecond = (second[i] and 0x00FF0000 shr 16)
            var gSecond = (second[i] and 0x0000FF00 shr 8)
            var bSecond = (second[i] and 0x000000FF)

            var rMask = rFirst - rSecond
            var gMask = gFirst - gSecond
            var bMask = bFirst - bSecond

            mask[i] = -0x1000000 or (rMask shl 16) or (gMask shl 8) or bMask
        }
    }

    private fun increaseContrast (first: IntArray, mask: IntArray, contrast: IntArray, amount: Double, threshold: Double )
    {
        for (i in first.indices) {
            var rFirst = (first[i] and 0x00FF0000 shr 16)
            var gFirst = (first[i] and 0x0000FF00 shr 8)
            var bFirst = (first[i] and 0x000000FF)

            var rMask = (mask[i] and 0x00FF0000 shr 16)
            var gMask = (mask[i] and 0x0000FF00 shr 8)
            var bMask = (mask[i] and 0x000000FF)

            var brightness = (0.2126 * rMask + 0.7152 * gMask + 0.0722 * bMask)/100
            if (brightness > threshold) {
                var helper = rFirst
                rFirst = (rFirst * amount).toInt()
                if (rFirst > 255) {
                    rFirst = helper
                }

                helper = gFirst
                gFirst = (gFirst * amount).toInt()
                if (gFirst > 255) {
                    gFirst = helper
                }

                helper = bFirst
                bFirst = (bFirst * amount).toInt()
                if (bFirst > 255) {
                    bFirst = helper
                }
            }
            contrast[i] = -0x1000000 or (rFirst shl 16) or (gFirst shl 8) or bFirst
        }
    }

    private fun comparison(first: IntArray, second: IntArray, mask: IntArray, contrast: IntArray, result: IntArray, threshold: Double)
    {
        for (i in first.indices) {
            var rFirst = (first[i] and 0x00FF0000 shr 16)
            var gFirst = (first[i] and 0x0000FF00 shr 8)
            var bFirst = (first[i] and 0x000000FF)


            var rMask = (mask[i] and 0x00FF0000 shr 16)
            var gMask = (mask[i] and 0x0000FF00 shr 8)
            var bMask = (mask[i] and 0x000000FF)

            var brightness = (0.2126 * rMask + 0.7152 * gMask + 0.0722 * bMask)/100
            //println(brightness)

            var rContrast = (contrast[i] and 0x00FF0000 shr 16)
            var gContrast = (contrast[i] and 0x0000FF00 shr 8)
            var bContrast = (contrast[i] and 0x000000FF)

            rContrast = (rFirst - rContrast)
            gContrast = (gFirst - gContrast)
            bContrast = (bFirst - bContrast)

            //println(rContrast)
            //println(gContrast)
            //println(bContrast)

            if (brightness > threshold)
            {
                result[i] =-0x1000000 or (rContrast shl 16) or (gContrast shl 8) or bContrast
            }
            else{
                result[i] = -0x1000000 or (rFirst shl 16) or (gFirst shl 8) or bFirst
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
