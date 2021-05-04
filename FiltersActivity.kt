package com.example.photoeditor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class FiltersActivity : AppCompatActivity() {
    private lateinit var imageUri: Uri
//    private lateinit var finalBitmap:Bitmap
//    private val imageView = findViewById<ImageView>(R.id.imageViewEdit)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters)
        init()
//        if (savedInstanceState != null) {
//            imageView.setImageBitmap(savedInstanceState.getParcelable("image"))
//        }
    }

    private fun init() {
        val uriStr = intent.getStringExtra("imgUri")
        val uri = Uri.parse(uriStr)
        val imageView = findViewById<ImageView>(R.id.imageViewEdit)

        imageView?.setImageURI(uri)

        val drawable = imageView?.drawable as BitmapDrawable
        val bitmap = drawable.bitmap

        var finalBitmap = bitmap
        lateinit var bitmapBefore: Bitmap

        val applyButton = findViewById<Button>(R.id.applyButton)
        val returnButton = findViewById<ImageButton>(R.id.returnButton)
        val returnBackButton = findViewById<ImageButton>(R.id.returnBackButton)

        returnBackButton.setOnClickListener {
            imageView.setImageBitmap(bitmapBefore)
            returnBackButton.isEnabled = false
            returnButton.isEnabled = true
        }
        returnButton.setOnClickListener {
            imageView.setImageBitmap(finalBitmap)
            returnButton.isEnabled = false
            returnBackButton.isEnabled = true
        }

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            val editIntent = Intent(this, ChooseActivity::class.java)
            editIntent.putExtra("imgUri", uri.toString())
            startActivity(editIntent)
        }

        val orig = findViewById<ImageButton>(R.id.oroginal)
        orig.setOnClickListener {
            bitmapBefore = finalBitmap
            imageView.setImageBitmap(bitmap)
        }

        val firstFilter = findViewById<ImageButton>(R.id.swap)
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
            bitmapBefore = finalBitmap
            finalBitmap = bmDublicated
            val photoFile = createImageFile()
            imageUri = Uri.fromFile(photoFile)
            applyButton.isEnabled = true
        }

        val secondFilter = findViewById<ImageButton>(R.id.grey)
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
            bitmapBefore = finalBitmap
            finalBitmap = bmDublicated
            val photoFile = createImageFile()
            imageUri = Uri.fromFile(photoFile)
            applyButton.isEnabled = true
        }

        val thirdFilter = findViewById<ImageButton>(R.id.sepia)
        thirdFilter.setOnClickListener {
            val width = bitmap.width
            val height = bitmap.height
            val srcPixels = IntArray(width * height)
            val destPixels = IntArray(width * height)
            bitmap.getPixels(srcPixels, 0, width, 0, 0, width, height)
            val bmDublicated = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            sepia(srcPixels, destPixels)
            bmDublicated.setPixels(destPixels, 0, width, 0, 0, width, height);
            imageView.setImageBitmap(bmDublicated)
            bitmapBefore = finalBitmap
            finalBitmap = bmDublicated
            val photoFile = createImageFile()
            imageUri = Uri.fromFile(photoFile)
            applyButton.isEnabled = true
        }

        applyButton.setOnClickListener {
            val outFile = createImageFile()
            try {
                FileOutputStream(outFile).use { out ->
                    finalBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, out)
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

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putParcelable("image", finalBitmap);
//    }

    private fun sepia(src: IntArray, dest: IntArray) {
        // конвертировать в сепию
        for (i in src.indices) {
//                val a = p shr 24 and 0xff
            var r = (src[i] and 0x00FF0000 shr 16)
            var g = (src[i] and 0x0000FF00 shr 8)
            var b = (src[i] and 0x000000FF)

            // вычисляем newRed, newGreen, newBlue
            val newRed = (0.393 * r + 0.769 * g + 0.189 * b).toInt()
            val newGreen = (0.349 * r + 0.686 * g + 0.168 * b).toInt()
            val newBlue = (0.272 * r + 0.534 * g + 0.131 * b).toInt()

            // проверка состояния
            r = if (newRed > 255) 255 else newRed
            g = if (newGreen > 255) 255 else newGreen
            b = if (newBlue > 255) 255 else newBlue

            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
        }
    }


    private fun grey(src: IntArray, dest: IntArray) {
        for (i in src.indices) {
            // получаем компоненты цветов пикселя
            var r = (src[i] and 0x00FF0000 shr 16)
            var g = (src[i] and 0x0000FF00 shr 8)
            var b = (src[i] and 0x000000FF)
            // делаем цвет черно-белым (оттенки серого) - находим среднее арифметическое
            r = (((r + g + b) / 3.0f).toInt()).also { b = it }.also { g = it }
            // собираем новый пиксель по частям (по каналам)
            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
        }
    }

    private fun swapRB(src: IntArray, dest: IntArray) {
        for (i in src.indices) {
            dest[i] = (src[i] and -0xff0100 or (src[i] and 0x000000ff shl 16)
                    or (src[i] and 0x00ff0000 shr 16))
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