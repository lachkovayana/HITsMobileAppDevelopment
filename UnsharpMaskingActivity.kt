package com.example.photoeditor

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView

class UnsharpMaskingActivity : AppCompatActivity() {
    private lateinit var imageUri: Uri
    private lateinit var finalBitmap: Bitmap
    private lateinit var bitmapBefore: Bitmap
    private lateinit var bitmap: Bitmap
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {

        val changeButton = findViewById<Button>(R.id.changeButton)
        val amountEditText = findViewById<EditText>(R.id.amountEditText)
        val radiusEditText = findViewById<EditText>(R.id.radiusEditText)
        val thresholdEditText = findViewById<EditText>(R.id.thresholdEditText)

        val uriStr = intent.getStringExtra("imgUri")
        val uri = Uri.parse(uriStr)
        imageView = findViewById(R.id.imageViewEdit)
        imageView.setImageURI(uri)

        val drawable = imageView.drawable as BitmapDrawable
        bitmap = drawable.bitmap
        bitmapBefore = bitmap
        finalBitmap = bitmap

        changeButton.setOnClickListener {
            val amount = amountEditText.text.toString().toDouble()
            val radius = radiusEditText.text.toString().toInt()
            val threshold = thresholdEditText.text.toString().toInt()
            println(amount)
            println(radius)
            println(threshold)
            unsharpMasking(radius, threshold)
        }
    }

    private fun unsharpMasking(radius: Int, threshold: Int){
        val width = bitmap.width
        println(width)
        val height = bitmap.height
        println(height)
        val startPixels = IntArray(width * height)
        val maskPixels = IntArray(width * height)
        val resultPixels = IntArray(width * height)
        bitmap.getPixels(startPixels, 0, width, 0, 0, width, height)
        val imageMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)


        //grey(srcPixels, destPixels)
        gaussianBlur(startPixels, maskPixels, width, radius)
        comparison(startPixels, maskPixels, resultPixels, threshold)

        imageMask.setPixels(maskPixels, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(imageMask)
        bitmapBefore = finalBitmap
        finalBitmap = imageMask


    }

    private fun gaussianBlur(start: IntArray, mask: IntArray, width: Int, radius: Int) {
        var numberString = 0
        for (i in start.indices) {
            var r = 0
            var g = 0
            var b = 0
            var numberRGB = 0


            if (i % width == 0) {
                numberString += 1
                //println(numberString)
            }

            //размытие строки
            for (j in 0..radius) {
                if (i + j < numberString * width) {
                    r += (start[i + j] and 0x00FF0000 shr 16)
                    g += (start[i + j] and 0x0000FF00 shr 8)
                    b += (start[i + j] and 0x000000FF)
                    numberRGB += 1
                }

                if (i - j >= (numberString - 1) * width) {
                    r += (start[i - j] and 0x00FF0000 shr 16)
                    g += (start[i - j] and 0x0000FF00 shr 8)
                    b += (start[i - j] and 0x000000FF)
                    numberRGB += 1
                }

            }

            r /= numberRGB
            g /= numberRGB
            b /= numberRGB
            mask[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
            //размытие строки радиус 3
            /*when {
                i - 3 >= (numberString - 1) * width -> {
                    when {
                        i + 3 < numberString * width -> {
                            var r = ((src[i - 3] and 0x00FF0000 shr 16) + (src[i - 2] and 0x00FF0000 shr 16) + (src[i - 1] and 0x00FF0000 shr 16) + (src[i] and 0x00FF0000 shr 16) + (src[i + 1] and 0x00FF0000 shr 16) + (src[i + 2] and 0x00FF0000 shr 16) + (src[i + 3] and 0x00FF0000 shr 16)) / 7
                            var g = ((src[i - 3] and 0x0000FF00 shr 8) + (src[i - 2] and 0x0000FF00 shr 8) + (src[i - 1] and 0x0000FF00 shr 8) + (src[i] and 0x0000FF00 shr 8) + (src[i + 1] and 0x0000FF00 shr 8) + (src[i + 2] and 0x0000FF00 shr 8) + (src[i + 3] and 0x0000FF00 shr 8)) / 7
                            var b = ((src[i - 3] and 0x000000FF) + (src[i - 2] and 0x000000FF) + (src[i - 1] and 0x000000FF) + (src[i] and 0x000000FF) + (src[i + 1] and 0x000000FF) + (src[i + 2] and 0x000000FF) + (src[i + 3] and 0x000000FF)) / 7
                            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                        }
                        i + 2 < numberString * width -> {
                            var r = ((src[i - 3] and 0x00FF0000 shr 16) + (src[i - 2] and 0x00FF0000 shr 16) + (src[i - 1] and 0x00FF0000 shr 16) + (src[i] and 0x00FF0000 shr 16) + (src[i + 1] and 0x00FF0000 shr 16) + (src[i + 2] and 0x00FF0000 shr 16)) / 6
                            var g = ((src[i - 3] and 0x0000FF00 shr 8) + (src[i - 2] and 0x0000FF00 shr 8) + (src[i - 1] and 0x0000FF00 shr 8) + (src[i] and 0x0000FF00 shr 8) + (src[i + 1] and 0x0000FF00 shr 8) + (src[i + 2] and 0x0000FF00 shr 8)) / 6
                            var b = ((src[i - 3] and 0x000000FF) + (src[i - 2] and 0x000000FF) + (src[i - 1] and 0x000000FF) + (src[i] and 0x000000FF) + (src[i + 1] and 0x000000FF) + (src[i + 2] and 0x000000FF)) / 6
                            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                        }
                        i + 1 < numberString * width -> {
                            var r = ((src[i - 3] and 0x00FF0000 shr 16) + (src[i - 2] and 0x00FF0000 shr 16) + (src[i - 1] and 0x00FF0000 shr 16) + (src[i] and 0x00FF0000 shr 16) + (src[i + 1] and 0x00FF0000 shr 16)) / 5
                            var g = ((src[i - 3] and 0x0000FF00 shr 8) + (src[i - 2] and 0x0000FF00 shr 8) + (src[i - 1] and 0x0000FF00 shr 8) + (src[i] and 0x0000FF00 shr 8) + (src[i + 1] and 0x0000FF00 shr 8)) / 5
                            var b = ((src[i - 3] and 0x000000FF) + (src[i - 2] and 0x000000FF) + (src[i - 1] and 0x000000FF) + (src[i] and 0x000000FF) + (src[i + 1] and 0x000000FF)) / 5
                            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                        }
                        i < numberString * width -> {
                            var r = ((src[i - 3] and 0x00FF0000 shr 16) + (src[i - 2] and 0x00FF0000 shr 16) + (src[i - 1] and 0x00FF0000 shr 16) + (src[i] and 0x00FF0000 shr 16)) / 4
                            var g = ((src[i - 3] and 0x0000FF00 shr 8) + (src[i - 2] and 0x0000FF00 shr 8) + (src[i - 1] and 0x0000FF00 shr 8) + (src[i] and 0x0000FF00 shr 8)) / 4
                            var b = ((src[i - 3] and 0x000000FF) + (src[i - 2] and 0x000000FF) + (src[i - 1] and 0x000000FF) + (src[i] and 0x000000FF)) / 4
                            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                        }
                    }
                }
                i - 2 >= (numberString - 1) * width -> {
                    when {
                        i + 3 < numberString * width -> {
                            var r = ((src[i - 2] and 0x00FF0000 shr 16) + (src[i - 1] and 0x00FF0000 shr 16) + (src[i] and 0x00FF0000 shr 16) + (src[i + 1] and 0x00FF0000 shr 16) + (src[i + 2] and 0x00FF0000 shr 16) + (src[i + 3] and 0x00FF0000 shr 16)) / 6
                            var g = ((src[i - 2] and 0x0000FF00 shr 8) + (src[i - 1] and 0x0000FF00 shr 8) + (src[i] and 0x0000FF00 shr 8) + (src[i + 1] and 0x0000FF00 shr 8) + (src[i + 2] and 0x0000FF00 shr 8) + (src[i + 3] and 0x0000FF00 shr 8)) / 6
                            var b = ((src[i - 2] and 0x000000FF) + (src[i - 1] and 0x000000FF) + (src[i] and 0x000000FF) + (src[i + 1] and 0x000000FF) + (src[i + 2] and 0x000000FF) + (src[i + 3] and 0x000000FF)) / 6
                            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                        }
                        i + 2 < numberString * width -> {
                            var r = ((src[i - 2] and 0x00FF0000 shr 16) + (src[i - 1] and 0x00FF0000 shr 16) + (src[i] and 0x00FF0000 shr 16) + (src[i + 1] and 0x00FF0000 shr 16) + (src[i + 2] and 0x00FF0000 shr 16)) / 5
                            var g = ((src[i - 2] and 0x0000FF00 shr 8) + (src[i - 1] and 0x0000FF00 shr 8) + (src[i] and 0x0000FF00 shr 8) + (src[i + 1] and 0x0000FF00 shr 8) + (src[i + 2] and 0x0000FF00 shr 8)) / 5
                            var b = ((src[i - 2] and 0x000000FF) + (src[i - 1] and 0x000000FF) + (src[i] and 0x000000FF) + (src[i + 1] and 0x000000FF) + (src[i + 2] and 0x000000FF)) / 5
                            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                        }
                        i + 1 < numberString * width -> {
                            var r = ((src[i - 2] and 0x00FF0000 shr 16) + (src[i - 1] and 0x00FF0000 shr 16) + (src[i] and 0x00FF0000 shr 16) + (src[i + 1] and 0x00FF0000 shr 16)) / 4
                            var g = ((src[i - 2] and 0x0000FF00 shr 8) + (src[i - 1] and 0x0000FF00 shr 8) + (src[i] and 0x0000FF00 shr 8) + (src[i + 1] and 0x0000FF00 shr 8)) / 4
                            var b = ((src[i - 2] and 0x000000FF) + (src[i - 1] and 0x000000FF) + (src[i] and 0x000000FF) + (src[i + 1] and 0x000000FF)) / 4
                            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                        }
                        i < numberString * width -> {
                            var r = ((src[i - 2] and 0x00FF0000 shr 16) + (src[i - 1] and 0x00FF0000 shr 16) + (src[i] and 0x00FF0000 shr 16)) / 3
                            var g = ((src[i - 2] and 0x0000FF00 shr 8) + (src[i - 1] and 0x0000FF00 shr 8) + (src[i] and 0x0000FF00 shr 8)) / 3
                            var b = ((src[i - 2] and 0x000000FF) + (src[i - 1] and 0x000000FF) + (src[i] and 0x000000FF)) / 3
                            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                        }
                    }
                }
                i - 1 >= (numberString - 1) * width -> {
                    when {
                        i + 3 < numberString * width -> {
                            var r = ((src[i - 1] and 0x00FF0000 shr 16) + (src[i] and 0x00FF0000 shr 16) + (src[i + 1] and 0x00FF0000 shr 16) + (src[i + 2] and 0x00FF0000 shr 16) + (src[i + 3] and 0x00FF0000 shr 16)) / 5
                            var g = ((src[i - 1] and 0x0000FF00 shr 8) + (src[i] and 0x0000FF00 shr 8) + (src[i + 1] and 0x0000FF00 shr 8) + (src[i + 2] and 0x0000FF00 shr 8) + (src[i + 3] and 0x0000FF00 shr 8)) / 5
                            var b = ((src[i - 1] and 0x000000FF) + (src[i] and 0x000000FF) + (src[i + 1] and 0x000000FF) + (src[i + 2] and 0x000000FF) + (src[i + 3] and 0x000000FF)) / 5
                            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                        }
                        i + 2 < numberString * width -> {
                            var r = ((src[i - 1] and 0x00FF0000 shr 16) + (src[i] and 0x00FF0000 shr 16) + (src[i + 1] and 0x00FF0000 shr 16) + (src[i + 2] and 0x00FF0000 shr 16)) / 4
                            var g = ((src[i - 1] and 0x0000FF00 shr 8) + (src[i] and 0x0000FF00 shr 8) + (src[i + 1] and 0x0000FF00 shr 8) + (src[i + 2] and 0x0000FF00 shr 8)) / 4
                            var b = ((src[i - 1] and 0x000000FF) + (src[i] and 0x000000FF) + (src[i + 1] and 0x000000FF) + (src[i + 2] and 0x000000FF)) / 4
                            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                        }
                        i + 1 < numberString * width -> {
                            var r = ((src[i - 1] and 0x00FF0000 shr 16) + (src[i] and 0x00FF0000 shr 16) + (src[i + 1] and 0x00FF0000 shr 16)) / 3
                            var g = ((src[i - 1] and 0x0000FF00 shr 8) + (src[i] and 0x0000FF00 shr 8) + (src[i + 1] and 0x0000FF00 shr 8)) / 3
                            var b = ((src[i - 1] and 0x000000FF) + (src[i] and 0x000000FF) + (src[i + 1] and 0x000000FF)) / 3
                            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                        }
                        i < numberString * width -> {
                            var r = ((src[i - 1] and 0x00FF0000 shr 16) + (src[i] and 0x00FF0000 shr 16)) / 2
                            var g = ((src[i - 1] and 0x0000FF00 shr 8) + (src[i] and 0x0000FF00 shr 8)) / 2
                            var b = ((src[i - 1] and 0x000000FF) + (src[i] and 0x000000FF)) / 2
                            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                        }
                    }
                }
                i >= (numberString - 1) * width -> {
                    when {
                        i + 3 < numberString * width -> {
                            var r = ((src[i] and 0x00FF0000 shr 16) + (src[i + 1] and 0x00FF0000 shr 16) + (src[i + 2] and 0x00FF0000 shr 16) + (src[i + 3] and 0x00FF0000 shr 16)) / 4
                            var g = ((src[i] and 0x0000FF00 shr 8) + (src[i + 1] and 0x0000FF00 shr 8) + (src[i + 2] and 0x0000FF00 shr 8) + (src[i + 3] and 0x0000FF00 shr 8)) / 4
                            var b = ((src[i] and 0x000000FF) + (src[i + 1] and 0x000000FF) + (src[i + 2] and 0x000000FF) + (src[i + 3] and 0x000000FF)) / 4
                            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                        }
                        i + 2 < numberString * width -> {
                            var r = ((src[i] and 0x00FF0000 shr 16) + (src[i + 1] and 0x00FF0000 shr 16) + (src[i + 2] and 0x00FF0000 shr 16)) / 3
                            var g = ((src[i] and 0x0000FF00 shr 8) + (src[i + 1] and 0x0000FF00 shr 8) + (src[i + 2] and 0x0000FF00 shr 8)) / 3
                            var b = ((src[i] and 0x000000FF) + (src[i + 1] and 0x000000FF) + (src[i + 2] and 0x000000FF)) / 3
                            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                        }
                        i + 1 < numberString * width -> {
                            var r = ((src[i] and 0x00FF0000 shr 16) + (src[i + 1] and 0x00FF0000 shr 16)) / 2
                            var g = ((src[i] and 0x0000FF00 shr 8) + (src[i + 1] and 0x0000FF00 shr 8)) / 2
                            var b = ((src[i] and 0x000000FF) + (src[i + 1] and 0x000000FF)) / 2
                            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                        }
                        i < numberString * width -> {
                            var r = ((src[i] and 0x00FF0000 shr 16))
                            var g = ((src[i] and 0x0000FF00 shr 8))
                            var b = ((src[i] and 0x000000FF))
                            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
                        }
                    }
                }

            }*/
        }
    }

    private fun comparison(start: IntArray, mask: IntArray, result: IntArray, threshold: Int)
    {
        for (i in start.indices) {
            var rSrc = (start[i] and 0x00FF0000 shr 16)
            var gSrc = (start[i] and 0x0000FF00 shr 8)
            var bSrc = (start[i] and 0x000000FF)

            var rMask = (mask[i] and 0x00FF0000 shr 16)
            var gMask = (mask[i] and 0x0000FF00 shr 8)
            var bMask = (mask[i] and 0x000000FF)

            rMask = rSrc - rMask
            gMask = gSrc - gMask
            bMask = bSrc - bMask
            //mask[i] = -0x1000000 or (rMask shl 16) or (gMask shl 8) or bMask
            //mask[i] = mask[i] - src[i]
            //print(rMask)
            //print(gMask)
            //print(bMask)
            //println(mask[i])
            //var threshold = - threshold
            //println(threshold)

            /*if (mask[i] < threshold)
            {
                //mask[i] = -0x1000000 or (rSrc shl 16) or (gSrc shl 8) or bSrc
                result[i] = -0x1000000 or (rSrc shl 16) or (gSrc shl 8) or bSrc
                println("привет")
            }
            else{
                result[i] = -0x1000000 or (rMask shl 16) or (gMask shl 8) or bMask
                println("пока")

            }*/
        }
    }
}