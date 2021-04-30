package com.example.photoeditor

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class EditActivity : AppCompatActivity() {

    companion object {
        val currentImage = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        init()
    }

    private fun init() {
        var imageView: ImageView? = null
        imageView = findViewById(R.id.imageViewEdit)

        //val imageToShow = "content://com.android.providers.media.documents/document/image%3A74"

        val uriStr = intent.getStringExtra("imgUri")
        val uri = Uri.parse(uriStr)
        Log.d("CURIMG", uriStr.toString())

        imageView.setImageURI(uri)
    }


}