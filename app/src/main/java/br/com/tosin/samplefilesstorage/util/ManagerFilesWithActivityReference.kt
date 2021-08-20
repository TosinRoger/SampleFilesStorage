package br.com.tosin.samplefilesstorage.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import br.com.tosin.samplefilesstorage.delegate.StorageFileDelegate
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.*

object ManagerFilesWithActivityReference {

    private const val FOLDER_ONE = "FolderOne"

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
        fileNameToSave: String
    ) {
        // From camera
        // fileNameToSave === content://br.com.tosin.samplefilesstorage/TakePictureFromCamera/2021-08-20_-_14%3A33%3A31.jpg
        // From gallery
        // fileNameToSave === content://com.android.providers.media.documents/document/image%3A33
        Log.d("", "")
    }

    private fun createFileFolder(context: Context, folderName: StorageFolder): File {
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

    // =============================================================================================
    //  OLD METHODS
    // =============================================================================================

    /**
     * Call .launch(null) to start camera
     */
    fun createCallTakePhoto(
        fragment: Fragment,
        fromWhere:  ActivityResultContracts.TakePicturePreview,
        delegate: StorageFileDelegate
    ): ActivityResultLauncher<Void> {
        return fragment.registerForActivityResult(fromWhere) {
            if (it == null) {
                delegate.onError("Bitmap is null. Can't provider image from camera", null)
            } else {
                val isSavedSuccessfully = savePhotoToInternalStorage(
                    fragment.requireActivity(),
                    FOLDER_ONE,
                    UUID.randomUUID().toString(),
                    it
                )

                if (isSavedSuccessfully)
                    delegate.onSuccess()
                else
                    delegate.onError("Fail to sava photo", null)
            }
        }
    }

    fun deleteFileFromInternalStorage(
        fragment: Fragment,
        fileName: String,
        delegate: StorageFileDelegate
    ): Boolean {
        try {
            val rootApp = fragment.requireActivity().filesDir
            val folderOne = File(rootApp, fileName)

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

    // =============================================================================================
    //          ACCESS STORAGE METHODS
    // =============================================================================================

    private fun savePhotoToInternalStorage(
        fragmentActivity: FragmentActivity,
        folderName: String,
        fileName: String,
        bmp: Bitmap
    ): Boolean {
        return try {
            val rootApp = fragmentActivity.filesDir
            // rootApp == /data/user/0/APP_PACKAGE_NAME/files
            val newFolder = File(rootApp, folderName)
            // newFolder == /data/user/0/APP_PACKAGE_NAME/files/FOLDER_NAME

            newFolder.mkdirs()

            FileOutputStream(
                File(
                    newFolder, "$fileName.jpg"
                )
            ).use { steam ->
                if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, steam)) {
                    throw  IOException("Couldn't save bitmap. with: FileName: $fileName, in path: ${newFolder.path}")
                }
            }

            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}