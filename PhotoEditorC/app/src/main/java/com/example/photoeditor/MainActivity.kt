package com.example.photoeditor

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_PERMISSIONS = 123
        private val PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private const val PERMISSIONS_COUNT = 2
        private const val REQUEST_PICK_IMAGE = 1234
        private const val REQUEST_IMAGE_CAPTURE = 12345
    }

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        val takePhotoButton = findViewById<Button>(R.id.takePhotoButton)
        val selectImageById = findViewById<Button>(R.id.selectImageButton)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val builder = VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }

        if (!this@MainActivity.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            takePhotoButton.visibility = View.GONE
        }

        selectImageById.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            val pickIntent = Intent(Intent.ACTION_PICK)
            pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            val chooserIntent = Intent.createChooser(intent, this.getString(R.string.selectImage))
            startActivityForResult(chooserIntent, REQUEST_PICK_IMAGE)
        }

        takePhotoButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                //create a file for the photo that was just taken
                val photoFile = createImageFile()
                imageUri = Uri.fromFile(photoFile)
                val myPrefs = getSharedPreferences(this.getString(R.string.appID), 0)
                myPrefs.edit().putString("path", photoFile.absolutePath).apply()
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(
                    takePictureIntent,
                    REQUEST_IMAGE_CAPTURE
                )
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    this@MainActivity,
                    this.getString(R.string.error1),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        when {
            requestCode == REQUEST_IMAGE_CAPTURE -> {
                if (imageUri == null) {
                    val p = getSharedPreferences(this.getString(R.string.appID), 0)
                    val path = p.getString("path", "")
                    if (path!!.isEmpty()) {
                        recreate()
                        return
                    }
                    imageUri = Uri.parse("file://$path")

                }
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri))
            }
            data == null -> {
                recreate()
                return
            }
            requestCode == REQUEST_PICK_IMAGE -> {
                imageUri = data.data
            }
        }
        val editIntent = Intent(this@MainActivity, ChooseActivity::class.java)
        val imageToTransfer = imageUri
        editIntent.putExtra(this.getString(R.string.imageUri), imageToTransfer.toString())
        startActivity(editIntent)
    }

    override fun onResume() {
        super.onResume()
        if (notPermissions()) {
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS && grantResults.isNotEmpty() && notPermissions()) {
            Toast.makeText(
                this,
                this.getString(R.string.onDeny),
                Toast.LENGTH_LONG
            ).show()
        }
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

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "/JPEG_$timeStamp.jpg"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File(storageDir.toString() + imageFileName)
    }
}