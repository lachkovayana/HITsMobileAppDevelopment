package com.example.photoeditor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.example.photoeditor.databinding.ActivityRotateBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class RotateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRotateBinding
    private lateinit var rotatedBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rotate)
        binding = ActivityRotateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        //установка переданного изображения
        val uriStr = intent.getStringExtra(getString(R.string.imageUri))
        val uri = Uri.parse(uriStr)
        binding.imageViewEdit.setImageURI(uri)

        // возвращение к ChooseActivity без сохранения изменений
        binding.backButton.setOnClickListener {
            val editIntent = Intent(this, ChooseActivity::class.java)
            editIntent.putExtra(this.getString(R.string.imageUri), uri.toString())
            startActivity(editIntent)
        }

        // применение изменений и возврат на экран выбора действий
        binding.applyButton.setOnClickListener {
            val outFile = createImageFile()
            try {
                FileOutputStream(outFile).use { out ->
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    val imageUri = Uri.parse("file://" + outFile.absolutePath)
                    val intent = Intent(this, ChooseActivity::class.java)
                    intent.putExtra(this.getString(R.string.imageUri), imageUri.toString())
                    startActivity(intent)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        binding.doubleRight.setOnClickListener{
            rotate(180F)
        }

        binding.right.setOnClickListener{
            rotate(90F)
        }

        binding.left.setOnClickListener{
            rotate(-90F)
        }

        binding.doubleLeft.setOnClickListener{
            rotate(-180F)
        }

    }
    private fun rotate(angle:Float){
        val bitmap = (binding.imageViewEdit.drawable as BitmapDrawable).bitmap
        val matrix = Matrix()
        matrix.postRotate(angle)
        rotatedBitmap =
            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
        binding.imageViewEdit.setImageBitmap(rotatedBitmap)
    }

    // создание файла с текущим изображением
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "/JPEG_$timeStamp.jpg"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File(storageDir.toString() + imageFileName)
    }
}