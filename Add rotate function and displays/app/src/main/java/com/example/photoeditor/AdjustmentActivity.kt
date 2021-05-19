package com.example.photoeditor

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

class AdjustmentActivity : AppCompatActivity() {

    companion object {
        val currentImage = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adjustment)

        var imageView: ImageView? = null
        imageView = findViewById(R.id.imageViewEdit)
        val uriStr = intent.getStringExtra("imgUri")
        val uri = Uri.parse(uriStr)

        imageView.setImageURI(uri)

        val rotationButton =  findViewById<Button>(R.id.rotationButton);
        rotationButton.setOnClickListener{
            val rotationIntent = Intent(this, RotateActivity::class.java)
            val imageToTransfer = uri;
            rotationIntent.putExtra("imgUri", imageToTransfer.toString())
            startActivity(rotationIntent)
        }
    }
}