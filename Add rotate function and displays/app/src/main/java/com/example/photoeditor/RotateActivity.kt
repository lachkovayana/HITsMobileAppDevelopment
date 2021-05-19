package com.example.photoeditor

import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class RotateActivity : AppCompatActivity() {

    companion object {
        val currentImage = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rotate)

        var imageView: ImageView? = null
        imageView = findViewById(R.id.imageViewEdit)
        val uriStr = intent.getStringExtra("imgUri")
        val uri = Uri.parse(uriStr)

        imageView.setImageURI(uri)

        val doubleRightButton =  findViewById<Button>(R.id.doubleRight);
        doubleRightButton.setOnClickListener{
            imageView.rotation = imageView.rotation + 180f
        }

        val rightButton =  findViewById<Button>(R.id.right);
        rightButton.setOnClickListener{
            imageView.rotation = imageView.rotation + 90f
        }

        val leftButton =  findViewById<Button>(R.id.left);
        leftButton.setOnClickListener{
            imageView.rotation = imageView.rotation - 90f
        }

        val doubleLeftButton =  findViewById<Button>(R.id.doubleLeft);
        doubleLeftButton.setOnClickListener{
            imageView.rotation = imageView.rotation - 180f
        }

    }
}