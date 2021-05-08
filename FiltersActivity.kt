package com.example.photoeditor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class FiltersActivity : AppCompatActivity() {

    private lateinit var imageUri: Uri
    private lateinit var finalBitmap: Bitmap
    private lateinit var bitmapBefore: Bitmap
    private lateinit var bitmap: Bitmap
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters)
        init()
    }

    private fun init() {

        val applyButton = findViewById<Button>(R.id.applyButton)
        val returnButton = findViewById<ImageButton>(R.id.returnButton)
        val returnBackButton = findViewById<ImageButton>(R.id.returnBackButton)
        val backButton = findViewById<Button>(R.id.backButton)
        val orig = findViewById<ImageButton>(R.id.oroginal)
        val firstFilter = findViewById<ImageButton>(R.id.swap)
        val secondFilter = findViewById<ImageButton>(R.id.grey)
        val thirdFilter = findViewById<ImageButton>(R.id.sepia)
        val fourthFilter = findViewById<ImageButton>(R.id.negative)
        val fifthFilter = findViewById<ImageButton>(R.id.red)

        //получение и установка изображения из ChooseActivity
        val uriStr = intent.getStringExtra(this.getString(R.string.imageUri))
        val uri = Uri.parse(uriStr)
        imageView = findViewById(R.id.imageViewEdit)
        imageView.setImageURI(uri)
        val drawable = imageView.drawable as BitmapDrawable
        bitmap = drawable.bitmap
        bitmapBefore = bitmap
        finalBitmap = bitmap

        // возвращение к ChooseActivity без сохранения изменений
        backButton.setOnClickListener {
            val editIntent = Intent(this, ChooseActivity::class.java)
            editIntent.putExtra(this.getString(R.string.imageUri), uri.toString())
            startActivity(editIntent)
        }

        // возвращение предыдущего выбранного фильтра
        returnBackButton.setOnClickListener {
            imageView.setImageBitmap(bitmapBefore)
            val helper = finalBitmap
            finalBitmap = bitmapBefore
            bitmapBefore = helper
            returnBackButton.isEnabled = false
            returnButton.isEnabled = true
        }

        // возвращение последнего выбранного фильтра
        returnButton.setOnClickListener {
            imageView.setImageBitmap(bitmapBefore)
            val helper = finalBitmap
            finalBitmap = bitmapBefore
            bitmapBefore = helper
            returnButton.isEnabled = false
            returnBackButton.isEnabled = true
        }

        // применение изменений и возврат на экран выбора действий
        applyButton.setOnClickListener {
            val outFile = createImageFile()
            try {
                FileOutputStream(outFile).use { out ->
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    // try
                    //outFile.path
                    val imageUri = Uri.parse("file://" + outFile.absolutePath)
                    val intent = Intent(this, ChooseActivity::class.java)
                    intent.putExtra(this.getString(R.string.imageUri), imageUri.toString())
                    startActivity(intent)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        // фильтры
        orig.setOnClickListener {
            bitmapBefore = finalBitmap
            imageView.setImageBitmap(bitmap)
            finalBitmap = bitmap
        }

        firstFilter.setOnClickListener {
            filter(1)
        }

        secondFilter.setOnClickListener {
            filter(2)
        }

        thirdFilter.setOnClickListener {
            filter(3)
        }

        fourthFilter.setOnClickListener {
            filter(4)
        }

        fifthFilter.setOnClickListener {
            filter(5)
        }

    }

    // установка выбранного фильтра
    private fun filter(filterNum: Int) {
        val width = bitmap.width
        val height = bitmap.height
        val srcPixels = IntArray(width * height)
        val destPixels = IntArray(width * height)
        bitmap.getPixels(srcPixels, 0, width, 0, 0, width, height)
        val bmDublicated = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        when (filterNum) {
            1 -> swapRB(srcPixels, destPixels)
            2 -> grey(srcPixels, destPixels)
            3 -> sepia(srcPixels, destPixels)
            4 -> negative(srcPixels, destPixels)
            5 -> red(srcPixels, destPixels)
        }
        bmDublicated.setPixels(destPixels, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(bmDublicated)
        bitmapBefore = finalBitmap
        finalBitmap = bmDublicated
        val photoFile = createImageFile()
        imageUri = Uri.fromFile(photoFile)
    }

    // замена красного цвета на голубой и наоборот
    private fun swapRB(src: IntArray, dest: IntArray) {
        for (i in src.indices) {
            dest[i] = (src[i] and -0xff0100 or (src[i] and 0x000000ff shl 16)
                    or (src[i] and 0x00ff0000 shr 16))
        }
    }


    private fun grey(src: IntArray, dest: IntArray) {
        for (i in src.indices) {
            // получаем компоненты цветов пикселя
            var r = (src[i] and 0x00FF0000 shr 16)
            var g = (src[i] and 0x0000FF00 shr 8)
            var b = (src[i] and 0x000000FF)

            // делаем цвет черно-белым (оттенки серого) - находим среднее арифметическое
            r = (((r + g + b) / 3.0f).toInt()).also { b = it }.also { g = it }

            // собираем новый пиксель по частям
            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
        }
    }

    private fun sepia(src: IntArray, dest: IntArray) {
        for (i in src.indices) {
            var r = (src[i] and 0x00FF0000 shr 16)
            var g = (src[i] and 0x0000FF00 shr 8)
            var b = (src[i] and 0x000000FF)

            // вычисляем newRed, newGreen, newBlue
            val newRed = (0.393 * r + 0.769 * g + 0.189 * b).toInt()
            val newGreen = (0.349 * r + 0.686 * g + 0.168 * b).toInt()
            val newBlue = (0.272 * r + 0.534 * g + 0.131 * b).toInt()

            // проверка состояния
            r = if (newRed > 255) 255 else newRed
            g = if (newGreen > 255) 255 else newGreen
            b = if (newBlue > 255) 255 else newBlue

            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
        }
    }

    private fun negative(src: IntArray, dest: IntArray) {
        for (i in src.indices) {
            var r = (src[i] and 0x00FF0000 shr 16)
            var g = (src[i] and 0x0000FF00 shr 8)
            var b = (src[i] and 0x000000FF)

            r = 255 - r
            g = 255 - g
            b = 255 - b

            dest[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
        }
    }

    private fun red(src: IntArray, dest: IntArray) {
        for (i in src.indices) {
            val r = (src[i] and 0x00FF0000 shr 16)
            dest[i] = -0x1000000 or (r shl 16) or (0 shl 8) or 0
        }
    }

    // создание файла с текущим изображением
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "/JPEG_$timeStamp.jpg"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File(storageDir.toString() + imageFileName)
    }
}