package br.com.tosin.filesstorageexample.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import br.com.tosin.filesstorageexample.delegate.StorageFileDelegate
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ManagerFilesWithActivityReference {

    fun provideUriFileWithAuthority(
        context: Context,
        folderName: StorageFolder,
        fileName: String
    ): Uri {
        val folder = createCacheFolder(context, folderName)
        val newFile = File(folder, fileName)

        return FileProvider.getUriForFile(
            context,
            context.packageName,
            newFile
        )
    }

    fun openContentImageAndMoveToApp(
        context: Context,
        pathOrigin: Uri,
        folderNameToSave: StorageFolder,
        fileNameToSave: String,
        delegate: StorageFileDelegate
    ) {
        // From camera
        // fileNameToSave === content://br.com.tosin.filesstorageexample/TakePictureFromCamera/2021-08-20_-_14%3A33%3A31.jpg
        // From gallery
        // fileNameToSave === content://com.android.providers.media.documents/document/image%3A33
        Log.d("", "")

        val folderToSave = createFilesFolder(context, folderNameToSave)
        val fileToSave = File(folderToSave, fileNameToSave)

        try {
            val inputStream = context.contentResolver?.openInputStream(pathOrigin)
            inputStream.use { input ->
                FileOutputStream(fileToSave).use { output ->
                    val buffer = ByteArray(4 * 1024) // or other buffer size
                    var read: Int = -1
                    while (input?.read(buffer).also {
                        if (it != null) {
                            read = it
                        }
                    } != -1
                    ) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
            deleteTempImage(context)
            delegate.onSuccess()
        } catch (e: IOException) {
            deleteTempImage(context)
            delegate.onError(e.localizedMessage, null)
        }
    }

    private fun createFilesFolder(context: Context, folderName: StorageFolder): File {
        val rootApp = context.filesDir
        // rootApp == /data/user/0/APP_PACKAGE_NAME/files
        val newFolder = File(rootApp, folderName.folderName)
        // newFolder == /data/user/0/APP_PACKAGE_NAME/files/FOLDER_NAME
        newFolder.mkdirs()

        return newFolder
    }

    private fun createCacheFolder(context: Context, folderName: StorageFolder): File {
        val rootApp = context.cacheDir
        // rootApp == /data/user/0/APP_PACKAGE_NAME/cache
        val newFolder = File(rootApp, folderName.folderName)
        // newFolder == /data/user/0/APP_PACKAGE_NAME/cache/FOLDER_NAME
        newFolder.mkdirs()

        return newFolder
    }

    private fun deleteTempImage(context: Context): Boolean {
        val cache = createCacheFolder(context, StorageFolder.TEMP_IMAGE)
        return if (cache.exists())
            cache.deleteRecursively()
        else
            false
    }

    fun deleteFileFromInternalPath(
        pathFile: String,
        delegate: StorageFileDelegate
    ): Boolean {
        try {
            val folderOne = File(pathFile)
            val isDeleted = folderOne.delete()

            if (isDeleted)
                delegate.onSuccess()
            else
                delegate.onError("Problem to fetch file", null)
        } catch (e: Exception) {
            delegate.onError(e.localizedMessage, e)
        }

        return true
    }
}
