package br.com.tosin.samplefilesstorage.util

import android.content.Context
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.IOException

object ImageUtils {
    @Throws(IOException::class)
    fun copyExif(context: Context, fromImageUri: Uri, toImageUri: Uri) {
        val fromExif: ExifInterface =
            loadExif(context, fromImageUri)
        val toExif: ExifInterface =
            loadExif(context, toImageUri)
        val attributes = arrayOf(
            ExifInterface.TAG_F_NUMBER,
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_DATETIME_DIGITIZED,
            ExifInterface.TAG_EXPOSURE_TIME,
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_DATESTAMP,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_PROCESSING_METHOD,
            ExifInterface.TAG_GPS_TIMESTAMP,
            ExifInterface.TAG_IMAGE_LENGTH,
            ExifInterface.TAG_IMAGE_WIDTH,
            ExifInterface.TAG_ISO_SPEED_RATINGS,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.TAG_SUBSEC_TIME,
            ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
            ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
            ExifInterface.TAG_WHITE_BALANCE
        )
        for (attribute in attributes) {
            val value = fromExif.getAttribute(attribute)
            if (value != null) {
                toExif.setAttribute(attribute, value)
            }
        }
        toExif.saveAttributes()
    }

    @Throws(IOException::class)
    private fun loadExif(context: Context, imageUri: Uri): ExifInterface {
        val ei: ExifInterface
        if (imageUri.toString().startsWith("file")) {
            // this will be the case when the user has take a photo from the cel camera
            ei = ExifInterface(imageUri.path!!)
        } else {
            // if not "file", it is a "content"
            // thi will be the case when the user has selected a photo from the cel gallery
            ei = ExifInterface(
                getImagePathFromContentProvider(
                    context,
                    imageUri
                )
            )
        }
        return ei
    }

    private fun getImagePathFromContentProvider(context: Context, uri: Uri): String {
        var cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor!!.moveToFirst()
        var documentId = cursor.getString(0)
        documentId = documentId.substring(documentId.lastIndexOf(":") + 1)
        cursor.close()
        cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null, MediaStore.Images.Media._ID + " = ? ", arrayOf(documentId), null
        )
        cursor!!.moveToFirst()
        //        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        // TODO FIX ME - chuncho elevado pra corrigir problemas de acesso ao
        var path: String? = ""
        if (cursor.count > 0) {
            cursor.moveToFirst()
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        } else if (uri.toString().startsWith("content://")) {
            val base = Environment.getExternalStorageDirectory().absolutePath + "/"
            val aux = uri.toString().split("/external_files".toRegex()).toTypedArray()
            var ends: String? = base
            if (aux.size > 1) {
                val temp = aux[1]
                ends += temp
            }
            path = ends
        }
        cursor.close()
        return path!!
    }
}