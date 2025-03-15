package com.example.gooverapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.Calendar
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import com.example.gooverapplication.databinding.ActivityImageViewerBinding
import gun0912.tedimagepicker.util.ToastUtil.context
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


class ImageViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageViewerBinding
    private var bitmapImage: Bitmap? = null
    private var scannedUri:Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val intent = Intent(this, ImageViewerActivity::class.java)
        val data1: String? = intent.getStringExtra("data1")
        val position: Int? = intent.getIntExtra("position", 0)
        val selectedUri: Uri = data1!!.toUri()
        Log.d("InImageViewerActivity", "$data1")
        if (data1 != null) {
            binding= ActivityImageViewerBinding.inflate(layoutInflater)
            binding.ImageView.setImageURI(selectedUri)
            // 나머지 코드 수행
        } else {
            Toast.makeText(context, "message", Toast.LENGTH_SHORT).show()
            // data1이 null인 경우 처리 방법을 정의
        }

        binding.scanButton.setOnClickListener {
            Toast.makeText(this, "이미지 처리 중 입니다", Toast.LENGTH_SHORT).show()
            val to_be_send: File = uriToFile(this, selectedUri!!)
            send2Server(to_be_send) { bitmap ->
                runOnUiThread {
                    if (bitmap != null) {
                        bitmapImage = bitmap
                        binding.ImageView.setImageBitmap(bitmap)
                        binding.undoBtn.visibility = View.VISIBLE
                        binding.okBtn.visibility = View.VISIBLE
                        }
                    else {Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show() }
                }
            }

        }

        binding.undoBtn.setOnClickListener {
            binding.ImageView.setImageURI(selectedUri)
            binding.undoBtn.visibility = View.GONE
            binding.okBtn.visibility = View.GONE
        }

        binding.okBtn.setOnClickListener {
            val returnIntent = Intent()
            //returnIntent.putExtra("scanned_image", bitmapImage)
            scannedUri = getImageUri(this, bitmapImage)
            returnIntent.putExtra("str",scannedUri.toString())
            returnIntent.putExtra("returnPosition", position)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }



        binding.backButton.setOnClickListener {
            finish() }
        setContentView(binding.root)
    }
}
fun getImageUri(inContext: Context?, inImage: Bitmap?): Uri? {
    val bytes = ByteArrayOutputStream()
    if (inImage != null) {
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    }
    val path = MediaStore.Images.Media.insertImage(inContext?.getContentResolver(), inImage, "Title" + " - " + Calendar.getInstance().getTime(), null)
    return Uri.parse(path)
}
fun uriToFile(context: Context, uri: Uri): File {
    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = context.contentResolver.query(uri, filePathColumn, null, null, null)
    cursor!!.moveToFirst()
    val columnIndex = cursor.getColumnIndex(filePathColumn[0])
    val filePath = cursor.getString(columnIndex)
    cursor.close()
    return File(filePath)
}
fun send2Server(file: File, callback: (Bitmap?) -> Unit ){
    val requestBody: RequestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("image", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
        .build()

    val request: Request = Request.Builder()
        .url("http://18.188.80.196:5000//upload")
        .post(requestBody)
        .build()

    val client = OkHttpClient()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            callback(null)
        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful){
                val response_body = response.body
                val inputStream = response_body.byteStream()
                val bitmapImage = BitmapFactory.decodeStream(inputStream)
                callback(bitmapImage)
            }
            else {
                callback(null)
            }

        }
    })
}