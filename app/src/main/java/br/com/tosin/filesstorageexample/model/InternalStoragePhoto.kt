package br.com.tosin.filesstorageexample.model

import android.graphics.Bitmap

data class InternalStoragePhoto(
    val folderName: String,
    val name: String,
    val bmp: Bitmap,
    val urlString: String
)