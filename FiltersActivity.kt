package com.example.photoeditor
//
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class FiltersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters)
        init()
    }

    private var bitmap: Bitmap? = null
    private fun init() {
        var imageView: ImageView? = null

        imageView = findViewById(R.id.imageViewEdit)
        val uriStr = intent.getStringExtra("imgUri")
        val uri = Uri.parse(uriStr)
        imageView.setImageURI(uri)
//        var imageUri:Uri = uri

//        val saveImageButton = findViewById<Button>(R.id.saveButton)
//        saveImageButton.setOnClickListener {
//            val builder = AlertDialog.Builder(this)
//            val dialogOnClickListener =
//                    DialogInterface.OnClickListener { dialog, which ->
//                        if (which == DialogInterface.BUTTON_POSITIVE) {
//                            val outFile = createImageFile()
//                            try {
//                                FileOutputStream(outFile).use { out ->
//                                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, out)
//                                    imageUri =
//                                            Uri.parse("file://" + outFile.absolutePath)
//                                    sendBroadcast(
//                                            Intent(
//                                                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
//                                                    imageUri
//                                            )
//                                    )
//                                    Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show()
//                                }
//                            } catch (e: IOException) {
//                                e.printStackTrace()
//                            }
//                        }
//                    }
//            builder.setMessage("Сохранить фото в галерею?")
//                    .setPositiveButton("Да", dialogOnClickListener)
//                    .setNegativeButton("Нет", dialogOnClickListener).show()
//        }
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            startActivity(Intent(this, ChooseActivity::class.java))
        }
    }

//    private fun createImageFile(): File {
//        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        val imageFileName = "/JPEG_$timeStamp.jpg"
//        val storageDir =
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//        return File(storageDir.toString() + imageFileName)
//    }
}