package com.jzhao.pixelartconverter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*
import kotlin.math.floor


const val IMAGE_PICK_CODE = 1000
const val START_PROCESSING_CODE = 1001
var ORIGINAL_IMAGE_URI:Uri = Uri.EMPTY
var selectedImage = false

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /** Called when the user taps the Send button */
    fun pickFromGallery(view: View) {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
        imageView2.visibility = View.INVISIBLE
    }


    //handle result of picked image
    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            if(data != null){
                ORIGINAL_IMAGE_URI = data.data!!
                imageView.setImageURI(ORIGINAL_IMAGE_URI)
                selectedImage = true
            }
        }
    }

    fun startProcessing(view: View) {
        if(!selectedImage){
            toast("Please select an image!")
            return
        }
        var horiPixelCount = findViewById<EditText>(R.id.HorizontalPixelCount).text.toString().toInt()
        var vertiPixelCount = findViewById<EditText>(R.id.VerticalPixelCount).text.toString().toInt()
        var original = (imageView.drawable as BitmapDrawable).bitmap

        var originalPixelsArr: IntArray = IntArray(original.height * original.width)
        original.getPixels(originalPixelsArr, 0, original.width, 0, 0, original.width, original.height)

        val pixelWidth = floor((original.width / horiPixelCount.toDouble())).toInt()
        val pixelHeight = floor((original.height / vertiPixelCount.toDouble())).toInt()

        var pixelArt = Bitmap.createBitmap(pixelWidth * horiPixelCount, pixelHeight * vertiPixelCount, Bitmap.Config.ARGB_8888, true)
        pixelArt.eraseColor(Color.WHITE)
        //printing original over pixel art's canvas
        print(pixelArt.width)
        print(original.width)
        pixelArt.setPixels(originalPixelsArr, 0, original.width, 0,0, pixelArt.width, pixelArt.height)

        var i = 0
        var ii = 0
        while(i < vertiPixelCount){
            ii = 0
            while(ii < horiPixelCount){
                var A: Long = 0
                var R: Long = 0
                var G: Long = 0
                var B: Long = 0

                var x = 0
                var y = 0
                while(y < pixelHeight){
                    x = 0
                    while(x < pixelWidth){
                        var curPixelColor = pixelArt.getPixel(ii * pixelWidth + x, i * pixelHeight + y)
                        A += Color.alpha(curPixelColor)
                        R += Color.red(curPixelColor) as Int
                        G += Color.green(curPixelColor) as Int
                        B += Color.blue(curPixelColor) as Int
                        x++
                    }
                    y++
                }


                A /= pixelHeight * pixelWidth
                R /= pixelHeight * pixelWidth
                G /= pixelHeight * pixelWidth
                B /= pixelHeight * pixelWidth


                var a = A.toInt()
                var r = R.toInt()
                var g = G.toInt()
                var b = B.toInt()

                if(!original.hasAlpha()){
                    a = 255
                }

                var newPixelColor = Color.argb(a, r, g, b)
                x = 0
                y = 0
                while(y < pixelHeight){
                    x = 0
                    while(x < pixelWidth){
                        pixelArt.setPixel(ii * pixelWidth + x, i * pixelHeight + y, newPixelColor)
                        x++
                    }
                    y++
                }

                x = 0
                y = 0
                while(y < pixelHeight){
                    pixelArt.setPixel(ii * pixelWidth, i * pixelHeight + y, Color.BLACK)
                    y++
                }
                while(x < pixelWidth){
                    pixelArt.setPixel(ii * pixelWidth + x, i * pixelHeight, Color.BLACK)
                    x++
                }
                ii++
            }
            i++
        }


        var savedImageURI = saveImage(pixelArt, "PixelArt_${UUID.randomUUID()}")
        imageView2.setImageURI(savedImageURI)
        imageView2.visibility = View.VISIBLE
    }

    // Method to save an image to gallery and return uri
    private fun saveImage(bitmap:Bitmap, title:String):Uri{
        // Save image to gallery
        var savedImageURL = ""
        try {
            savedImageURL = MediaStore.Images.Media.insertImage(
                contentResolver,
                bitmap,
                title,
                "Image of $title"
            )
            toast("Image saved to " + savedImageURL)
        }catch(e: IOException){
            e.printStackTrace()
            toast("Error to save image")
        }
        // Parse the gallery image url to uri
        return Uri.parse(savedImageURL)
    }


    // Extension function to show toast message
    private fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

//    fun isStoragePermissionGranted(): Boolean {
//        val TAG = "Storage Permission"
//        return if (Build.VERSION.SDK_INT >= 23) {
//            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                == PackageManager.PERMISSION_GRANTED
//            ) {
//                Log.v(TAG, "Permission is granted")
//                true
//            } else {
//                Log.v(TAG, "Permission is revoked")
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                    1
//                )
//                false
//            }
//        } else { //permission is automatically granted on sdk<23 upon installation
//            Log.v(TAG, "Permission is granted")
//            true
//        }
//    }
}


