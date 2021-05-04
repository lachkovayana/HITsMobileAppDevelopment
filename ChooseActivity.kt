package com.example.photoeditor

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ChooseActivity : AppCompatActivity() {
    private var REQUEST_CODE_FILTER = 1
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)
        init()
    }

    private fun init() {
        var imageView: ImageView? = findViewById(R.id.imageViewEdit)
        val uriStr = intent.getStringExtra("imgUri")
        val uri = Uri.parse(uriStr)
        imageView?.setImageURI(uri)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        val col = findViewById<Button>(R.id.COL)
        col.setOnClickListener {
            val newIntent = Intent(this, FiltersActivity::class.java)
            val imageToTransfer = uri
            newIntent.putExtra("imgUri", imageToTransfer.toString())
            //startActivity(newIntent)
            startActivity(newIntent)
        }

        val help = findViewById<Button>(R.id.help)
        help.setOnClickListener {
            Toast.makeText(this, "$uri", Toast.LENGTH_SHORT).show()
        }

        val saveImageButton = findViewById<ImageButton>(R.id.saveButton)
        saveImageButton.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this)
            val outFile = createImageFile()
            val dialogOnClickListener =
                    DialogInterface.OnClickListener { _, which ->
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            try {
                                FileOutputStream(outFile).use {
                                    sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
                                    Toast.makeText(
                                            this,
                                            "Сохранено",
                                            Toast.LENGTH_SHORT
                                    ).show()
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
    }
}

private fun createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "/JPEG_$timeStamp.jpg"
    val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    return File(storageDir.toString() + imageFileName)
}


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == RESULT_OK) {
//            when (requestCode) {
//                REQUEST_CODE_FILTER -> {
//                    val uriStr = intent.getStringExtra("imgUri")
//                    val uri = Uri.parse(uriStr)
//                    imageView.setImageURI(uri)
//                }
//            }
//        }
//    }
//}


//

//    private fun createImageFile(): File {
//        val timeStamp = SimpleDateFormat("yyyyMMdd_HHMMSS", Locale.getDefault()).format(Date())
//        val imageFileName = "/JPEG_$timeStamp.jpg"
//        val storageDir =
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//        return File(storageDir.toString() + imageFileName)
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == RESULT_OK) {
//            when (requestCode) {
//                REQUEST_CODE_FILTER -> {
//                    val uriStr = intent.getStringExtra("imgUri")
//                    val uri = Uri.parse(uriStr)
//                    imageView.setImageURI(uri)
//                }
//            }
//        }
//    }
