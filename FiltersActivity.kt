package com.example.photoeditor

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class FiltersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters)
        init()
    }

    private fun init() {
        val imageView = findViewById<ImageView>(R.id.imageViewEdit)
        val uriStr = intent.getStringExtra("imgUri")
        val uri = Uri.parse(uriStr)

        imageView?.setImageURI(uri)

        val drawable = imageView?.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        var finalBitmap = bitmap

        //imageView?.setImageBitmap(bitmap)

        val returnBackButton = findViewById<ImageButton>(R.id.returnBackButton)
        returnBackButton.setOnClickListener {
            imageView.setImageBitmap(bitmap)
        }
        val returnButton = findViewById<ImageButton>(R.id.returnButton)
        returnButton.setOnClickListener {
            imageView.setImageBitmap(finalBitmap)
        }

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            val editIntent = Intent(this, ChooseActivity::class.java)
            editIntent.putExtra("imgUri", uri.toString())
            startActivity(editIntent)
        }

        val firstFilter = findViewById<Button>(R.id.swap)
        firstFilter.setOnClickListener {
            val width = bitmap.width
            val height = bitmap.height
            val srcPixels = IntArray(width * height)
            val destPixels = IntArray(width * height)
            bitmap.getPixels(srcPixels, 0, width, 0, 0, width, height)
            val bmDublicated = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            swapRB(srcPixels, destPixels)
            bmDublicated.setPixels(destPixels, 0, width, 0, 0, width, height)
            imageView.setImageBitmap(bmDublicated)
            finalBitmap = bmDublicated
            //findViewById<Button>(R.id.saveImage).isEnabled = true
        }

        val secondFilter = findViewById<Button>(R.id.F2)
        secondFilter.setOnClickListener {
            val width = bitmap.width
            val height = bitmap.height
            val srcPixels = IntArray(width * height)
            val destPixels = IntArray(width * height)
            bitmap.getPixels(srcPixels, 0, width, 0, 0, width, height)
            val bmDublicated = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            grey(srcPixels, destPixels)
            bmDublicated.setPixels(destPixels, 0, width, 0, 0, width, height);
            imageView.setImageBitmap(bmDublicated)
            finalBitmap = bmDublicated
        }
        val applyButton = findViewById<Button>(R.id.applyButton)
        applyButton.setOnClickListener {

        }
    }

    private fun grey(src: IntArray, dest: IntArray){
        for (i in src.indices) {
            // получаем компоненты цветов пикселя
            var r = (src[i] and 0x00FF0000 shr 16)
            var g = (src[i] and 0x0000FF00 shr 8)
            var b = (src[i] and 0x000000FF)
            // делаем цвет черно-белым (оттенки серого) - находим среднее арифметическое
            r = (((r + g + b) / 3.0f).toInt()).also { b = it }.also { g = it }
            // собираем новый пиксель по частям (по каналам)
            dest[i] = -0x1000000 or (r shl 16) or (g  shl 8) or b
        }
    }

    private fun swapRB(src: IntArray, dest: IntArray) {
        for (i in src.indices) {
            (src[i] and -0xff0100 or (src[i] and 0x000000ff shl 16)
                    or (src[i] and 0x00ff0000 shr 16)).also { dest[i] = it }
        }
    }


}

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == RESULT_OK) {
//            val imageUri = data?.data
//            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
//            finalBitmap = bitmap
//        }
//    }

//    private var width = 0
//    private var height = 0
//    private lateinit var pixels: IntArray
//    private var pixelCount = 0
//    private val MAX_PIXEL_COUNT = 2048
//    private val REQUEST_PICK_IMAGE = 12345
//    private val REQUEST_IMAGE_CAPTURE = 1012
//    private val appID = "photoEditor"
//    val dialog = ProgressDialog.show(
//        this, "Loading",
//        "Please, wait", true
//    )

//    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        object : Thread() {
//            override fun run() {
//                bitmap = null
//                val bmpOptions = BitmapFactory.Options()
//                bmpOptions.inBitmap = bitmap
//                bmpOptions.inJustDecodeBounds = true
//                try {
//                    contentResolver.openInputStream(imageUri!!).use { input ->
//                        bitmap = BitmapFactory.decodeStream(input, null, bmpOptions)
//finalBitmap = bitmap
//                    }
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//                bmpOptions.inJustDecodeBounds = false
//                width = bmpOptions.outWidth
//                height = bmpOptions.outHeight
//                var resizeScale = 1
//                if (width > MAX_PIXEL_COUNT) {
//                    resizeScale = width / MAX_PIXEL_COUNT
//                } else if (height > MAX_PIXEL_COUNT) {
//                    resizeScale = height / MAX_PIXEL_COUNT
//                }
//                if (width / resizeScale > MAX_PIXEL_COUNT || height / resizeScale > MAX_PIXEL_COUNT) {
//                    resizeScale++
//                }
//                bmpOptions.inSampleSize = resizeScale
//                var input: InputStream? = null
//                try {
//                    input = contentResolver.openInputStream(imageUri!!)
//                } catch (e: FileNotFoundException) {
//                    e.printStackTrace()
//                    recreate()
//                }
//                bitmap = BitmapFactory.decodeStream(input, null, bmpOptions)
//                runOnUiThread {
//                    imageView!!.setImageBitmap(bitmap)
//                    dialog.cancel()
//                }
//                width = bitmap!!.width
//                height = bitmap!!.height
//                bitmap = bitmap!!.copy(Bitmap.Config.ARGB_8888, true)
//                pixelCount = width * height
//                pixels = IntArray(pixelCount)
//                bitmap?.getPixels(pixels, 0, width, 0, 0, width, height)
//            }
//        }.start()
//    }
//}