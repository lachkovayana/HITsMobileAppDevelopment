package com.example.photoeditor

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    @SuppressLint("NewApi")
    private fun notPermissions(): Boolean {
        for (i in 0 until PERMISSIONS_COUNT) {
            if (checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                return true
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notPermissions()) {
            requestPermissions(PERMISSIONS, REQUEST_PERMISSONS)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSONS && grantResults.size > 0) {
            if (notPermissions()) {
                val toast = Toast.makeText(
                        this,
                        "Редактор не может работать без доступа к Вашим фото. Пожалуйста, выберите 'Разрешить'",
                        Toast.LENGTH_LONG
                )
                toast.show()
                //                ((ActivityManager) this.getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
//                recreate();
            }
        }
    }

    private var imageView: ImageView? = null
    private fun init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val builder = VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }
        imageView = findViewById(R.id.imageView)
        if (!this@MainActivity.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            findViewById<View>(R.id.takePhotoButton).visibility = View.GONE
        }
        val selectImageById = findViewById<Button>(R.id.selectImageButton)
        selectImageById.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            val pickIntent = Intent(Intent.ACTION_PICK)
            pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            val chooserIntent = Intent.createChooser(intent, "Select Image")
            startActivityForResult(chooserIntent, REQUEST_PICK_IMAGE)
        }
        val takePhotoButton = findViewById<Button>(R.id.takePhotoButton)
        takePhotoButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                //create a file for the photo that was just taken
                val photoFile = createImageFile()
                imageUri = Uri.fromFile(photoFile)
                val myPrefs = getSharedPreferences(appID, 0)
                myPrefs.edit().putString("path", photoFile.absolutePath).apply()
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(
                        takePictureIntent,
                        REQUEST_IMAGE_CAPTURE
                )
            } else {
                Toast.makeText(this@MainActivity, "Камера не существует", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var imageUri: Uri? = null
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "/JPEG_$timeStamp.jpg"
        val storageDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File(storageDir.toString() + imageFileName)
    }

    private var editMode = false
    private var bitmap: Bitmap? = null
    private var width = 0
    private var height = 0
    private lateinit var pixels: IntArray
    private var pixelCount = 0
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (imageUri == null) {
                val p = getSharedPreferences(appID, 0)
                val path = p.getString("path", "")
                if (path!!.length < 1) {
                    recreate()
                    return
                }
                imageUri = Uri.parse("file://$path")
            }
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri))
        } else if (data == null) {
            recreate()
            return
        } else if (requestCode == REQUEST_PICK_IMAGE) {
            imageUri = data.data
        }

        editMode = true


        //отображение после выбора
        /*
        object : Thread() {
            override fun run() {
                bitmap = null
                val bmpOptions = BitmapFactory.Options()
                bmpOptions.inBitmap = bitmap
                bmpOptions.inJustDecodeBounds = true
                try {
                    contentResolver.openInputStream(imageUri!!).use { input ->
                        bitmap = BitmapFactory.decodeStream(input, null, bmpOptions)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                bmpOptions.inJustDecodeBounds = false
                width = bmpOptions.outWidth
                height = bmpOptions.outHeight
                var resizeScale = 1
                if (width > MAX_PIXEL_COUNT) {
                    resizeScale = width / MAX_PIXEL_COUNT
                } else if (height > MAX_PIXEL_COUNT) {
                    resizeScale = height / MAX_PIXEL_COUNT
                }
                if (width / resizeScale > MAX_PIXEL_COUNT || height / resizeScale > MAX_PIXEL_COUNT) {
                    resizeScale++
                }
                bmpOptions.inSampleSize = resizeScale
                var input: InputStream? = null
                try {
                    input = contentResolver.openInputStream(imageUri!!)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    recreate()
                }
                bitmap = BitmapFactory.decodeStream(input, null, bmpOptions)
                runOnUiThread {
                    imageView!!.setImageBitmap(bitmap)
                    dialog.cancel()
                }
                width = bitmap!!.width
                height = bitmap!!.height
                bitmap = bitmap!!.copy(Bitmap.Config.ARGB_8888, true)
                pixelCount = width * height
                pixels = IntArray(pixelCount)
                bitmap?.getPixels(pixels, 0, width, 0, 0, width, height)


            }
        }.start()
*/


        //тут переход
        //findViewById<View>(R.id.welcomeScreen).visibility = View.GONE
        //findViewById<View>(R.id.editScreen).visibility = View.VISIBLE
        val editIntent = Intent(this, EditActivity::class.java)
        val imageToTransfer = imageUri
        //val text = imageUri.toString()
        //val duration = Toast.LENGTH_SHORT
        //Log.d("TAG", text)
        //Toast.makeText(applicationContext, text, duration).show()

        editIntent.putExtra("imgUri", imageToTransfer.toString())
        startActivity(editIntent)

    }

    companion object {
        private const val REQUEST_PERMISSONS = 1234
        private val PERMISSIONS = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private const val PERMISSIONS_COUNT = 2
        private const val REQUEST_PICK_IMAGE = 12345
        private const val REQUEST_IMAGE_CAPTURE = 1012
        private const val appID = "photoEditor"
        private const val MAX_PIXEL_COUNT = 2048
    }
}