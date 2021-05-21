package com.example.photoeditor

import android.R.attr.angle
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
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
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val matrix = Matrix()
            val angle = 180;
            matrix.postRotate(angle.toFloat())
            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            imageView.setImageBitmap(rotatedBitmap);
        }

        val rightButton =  findViewById<Button>(R.id.right);
        rightButton.setOnClickListener{
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val matrix = Matrix()
            val angle = 90;
            matrix.postRotate(angle.toFloat())
            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            imageView.setImageBitmap(rotatedBitmap);
        }

        val leftButton =  findViewById<Button>(R.id.left);
        leftButton.setOnClickListener{
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val matrix = Matrix()
            val angle = -90;
            matrix.postRotate(angle.toFloat())
            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            imageView.setImageBitmap(rotatedBitmap);
        }

        val doubleLeftButton =  findViewById<Button>(R.id.doubleLeft);
        doubleLeftButton.setOnClickListener{
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val matrix = Matrix()
            val angle = -180;
            matrix.postRotate(angle.toFloat())
            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            imageView.setImageBitmap(rotatedBitmap);
        }

    }
}