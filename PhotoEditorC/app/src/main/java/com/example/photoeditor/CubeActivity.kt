package com.example.photoeditor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.cos
import kotlin.math.sin

class CubeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_cube)
        init()
    }

    private fun init() {
        val c = Cube(this)

        val uriStr = intent.getStringExtra(getString(R.string.imageUri))

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, ChooseActivity::class.java)
            intent.putExtra(this.getString(R.string.imageUri), uriStr)
            startActivity(intent)
            c.rotateZ3D(15.0f)

        }

        var pr = 0.0f
        val mt = findViewById<TextView>(R.id.textOfProgress)
        var oldPr = 0.0f
        val skBar = findViewById<SeekBar>(R.id.zAxisProgress)
        skBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                pr = progress.toFloat()
                mt.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                oldPr = pr
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                c.rotateZ3Dtheta = oldPr - pr
            }
        })
    }
}