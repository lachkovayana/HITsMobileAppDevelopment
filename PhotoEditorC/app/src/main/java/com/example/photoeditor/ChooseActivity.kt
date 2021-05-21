package com.example.photoeditor

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.photoeditor.databinding.ActivityChooseBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ChooseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChooseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {

        //установка переданного изображения
        val uriStr = intent.getStringExtra(getString(R.string.imageUri))
        val uri = Uri.parse(uriStr)
        binding.imageViewEdit.setImageURI(uri)


        // возвращение к экрану выбора фото
        binding.backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // поворот
        binding.rotationButton.setOnClickListener {
            val rotationIntent = Intent(this, RotateActivity::class.java)
            rotationIntent.putExtra(this.getString(R.string.imageUri), uri.toString())
            startActivity(rotationIntent)
        }

        // масштабирование
        binding.scalingButton.setOnClickListener {
            val intent = Intent(this, ScalingActivity::class.java)
            intent.putExtra(this.getString(R.string.imageUri), uri.toString())
            startActivity(intent)
        }

        // фильтры
        binding.colorFiltersButton.setOnClickListener {
            val newIntent = Intent(this, FiltersActivity::class.java)
            newIntent.putExtra(this.getString(R.string.imageUri), uri.toString())
            startActivity(newIntent)
        }

        // маскирование
        binding.maskingButton.setOnClickListener {
            val newIntent = Intent(this, UnsharpMaskingActivity::class.java)
            newIntent.putExtra(this.getString(R.string.imageUri), uri.toString())
            startActivity(newIntent)
        }

        // куб
        binding.cubeButton.setOnClickListener {
            val newIntent = Intent(this, CubeActivity::class.java)
            newIntent.putExtra(this.getString(R.string.imageUri), uri.toString())
            startActivity(newIntent)
        }

        // сохранение полученного фото
        binding.saveButton.setOnClickListener {
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
                                    this.getString(R.string.saved),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            builder.setMessage(this.getString(R.string.saveQuest))
                .setPositiveButton(this.getString(R.string.yes), dialogOnClickListener)
                .setNegativeButton(this.getString(R.string.no), dialogOnClickListener).show()
        }

        // "поделиться" полученным изображением
        binding.share.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            try {
                startActivity(Intent.createChooser(intent, this.getString(R.string.share)))
            } catch (e: Exception) {
                Toast.makeText(
                    this@ChooseActivity,
                    this.getString(R.string.noApps),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // создание файла из текущего изображения
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "/JPEG_$timeStamp.jpg"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File(storageDir.toString() + imageFileName)
    }
}
