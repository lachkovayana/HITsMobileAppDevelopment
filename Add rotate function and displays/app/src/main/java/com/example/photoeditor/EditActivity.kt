package com.example.photoeditor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class EditActivity : AppCompatActivity() {


    companion object {
        val currentImage = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        var imageView: ImageView? = null
        imageView = findViewById(R.id.imageViewEdit)
        val uriStr = intent.getStringExtra("imgUri")
        val uri = Uri.parse(uriStr)

        imageView.setImageURI(uri)

        val adjustmentButton = findViewById<Button>(R.id.adjustmentButton);
        adjustmentButton.setOnClickListener {
            val adjustmentIntent = Intent(this, AdjustmentActivity::class.java)
            val imageToTransfer = uri;
            adjustmentIntent.putExtra("imgUri", imageToTransfer.toString())
            startActivity(adjustmentIntent)
        }
    }



}