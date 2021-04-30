package com.example.jetpack
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.widget.SeekBar
import com.example.HitsAppDevelopment
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException


class RotationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bitmap = assetsToBitmap("C:\Users\Acer\Desktop\roawekdwa.jpg")

        bitmap?.apply {
  
            imageView.setImageBitmap(this)

            imageView2.setImageBitmap(rotate(seekBar.progress.toFloat()))
            textView2.text = "Rotate Bitmap (Degrees ${seekBar.progress})"


            seekBar.setOnSeekBarChangeListener(
                object: SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    imageView2.setImageBitmap(rotate(progress.toFloat()))
                    textView2.text = "Rotate Bitmap (Degrees $progress)"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }
    }
}



fun Context.assetsToBitmap(fileName:String):Bitmap?{
    return try {
        val stream = assets.open(fileName)
        BitmapFactory.decodeStream(stream)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun Bitmap.rotate(degrees:Float = 180F):Bitmap?{
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
}