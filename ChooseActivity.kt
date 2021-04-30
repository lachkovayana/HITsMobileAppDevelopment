package com.example.photoeditor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class ChooseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)
        init()
    }

    private fun init() {
        var imageView: ImageView? = null
        imageView = findViewById(R.id.imageViewEdit)
        val uriStr = intent.getStringExtra("imgUri")
        val uri = Uri.parse(uriStr)
        imageView.setImageURI(uri)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        val col = findViewById<Button>(R.id.COL)
        col.setOnClickListener {
            val newIntent = Intent(this, FiltersActivity::class.java)
            val imageToTransfer = uri
            newIntent.putExtra("imgUri", imageToTransfer.toString())
            startActivity(newIntent)
        }
    }

}
