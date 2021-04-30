package com.example.photoeditor
//
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.widget.Button
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

    //    private var imageUri: Uri? = null
    private fun init() {
        var imageUri: Uri? = null
        var imageView: ImageView? = null
        imageView = findViewById(R.id.imageViewEdit)
        val uriStr = intent.getStringExtra("imgUri")
        val uri = Uri.parse(uriStr)
        imageView?.setImageURI(uri)

        val saveImageButton = findViewById<Button>(R.id.saveButton)
        saveImageButton.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this@FiltersActivity)
            val dialogOnClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        val outFile = createImageFile()
                        try {
                            FileOutputStream(outFile).use { out ->
                                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, out)
                                imageUri = Uri.parse("file://" + outFile.absolutePath)
                                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri))
                                Toast.makeText(this@FiltersActivity, "Сохранено", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            builder.setMessage("Сохранить фото в галерею?")
                .setPositiveButton("Да", dialogOnClickListener)
                .setNegativeButton("Нет", dialogOnClickListener).show()
        }

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            val editIntent = Intent(this, ChooseActivity::class.java)
            editIntent.putExtra("imgUri", uri.toString())
            startActivity(editIntent)
        }
    }


    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "/JPEG_$timeStamp.jpg"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File(storageDir.toString() + imageFileName)
    }

    private var bitmap: Bitmap? = null
//
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
}