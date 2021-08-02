package br.com.tosin.samplefilesstorage.util

import android.graphics.Bitmap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import br.com.tosin.samplefilesstorage.delegate.StorageFileDelegate
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

object ManagerFilesWithActivityReference {

    private const val FOLDER_ONE = "FolderOne"
    private const val FOLDER_TWO = "FolderTwo"
    private const val FOLDER_THREE = "FolderThree"

    fun createCallTakePhoto(
        fragment: Fragment,
        delegate: StorageFileDelegate
    ): ActivityResultLauncher<Void> {

        return fragment.registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
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