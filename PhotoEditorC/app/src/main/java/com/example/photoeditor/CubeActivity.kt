package com.example.photoeditor

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.photoeditor.databinding.ActivityCubeBinding

class CubeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCubeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCubeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {

        val uriStr = intent.getStringExtra(getString(R.string.imageUri))

        binding.backButton.setOnClickListener {
            val intent = Intent(this, ChooseActivity::class.java)
            intent.putExtra(this.getString(R.string.imageUri), uriStr)
            startActivity(intent)

        }
        var pr = 0.0
        var oldPr = 0.0
        binding.zAxisProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textOfProgress.text = progress.toString()
                pr = progress.toDouble()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                oldPr = pr
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                binding.cubeView.rotateZ3D(Math.toRadians(pr-oldPr).toFloat())
            }
        })

    }
}
