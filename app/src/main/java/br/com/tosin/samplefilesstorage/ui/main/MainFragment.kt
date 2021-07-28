package br.com.tosin.samplefilesstorage.ui.main

import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.tosin.samplefilesstorage.databinding.MainFragmentBinding
import br.com.tosin.samplefilesstorage.model.InternalStoragePhoto
import br.com.tosin.samplefilesstorage.ui.main.adapter.InternalStoragePhotoAdapter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.*

class MainFragment : Fragment() {

    companion object {
        private const val ROOT_FOLDER = "MyImages"
        private const val TAG = "Debug_tag"
        fun newInstance() = MainFragment()
    }

    private var _binding: MainFragmentBinding? = null
    private val binding: MainFragmentBinding get() = _binding!!

    private lateinit var internalStoragePhotoAdapter: InternalStoragePhotoAdapter
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        internalStoragePhotoAdapter = InternalStoragePhotoAdapter {
            val isDeletionSuccessful = deletePhotoFromInternalStorage(it.name)
            if(isDeletionSuccessful) {
                loadPhotosFromInternalStorageIntoRecyclerView()
                Toast.makeText(requireContext(), "Photo successfully deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to delete photo", Toast.LENGTH_SHORT).show()
            }
        }

        _binding?.recyclerViewFilesInApp?.apply {
            adapter = internalStoragePhotoAdapter
            layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
        }

        createRootFolder()
        prepareToTakePhoto()

        loadPhotosFromInternalStorageIntoRecyclerView()
    }

    private fun createRootFolder() {
        val rootApp = requireActivity().filesDir // /data/user/0/br.com.tosin.samplefilesstorage/files
        val newFolder = File(rootApp, ROOT_FOLDER)
        newFolder.mkdirs()
        if (newFolder.exists())
            Log.d(TAG, "Root folder create successfully => $newFolder")
        else
            Log.d(TAG, "Cannot be create root folder")
    }


    // =============================================================================================
    //          CALL STORAGE METHODS FROM 'VIEW'
    // =============================================================================================

    private fun prepareToTakePhoto() {
        val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            val isPrivate = true
            if(isPrivate) {
                val isSavedSuccessfully = savePhotoToInternalStorage(UUID.randomUUID().toString(), it)
                if(isSavedSuccessfully) {
                    loadPhotosFromInternalStorageIntoRecyclerView()
                    Toast.makeText(requireContext(), "Photo saved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to save photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
        _binding?.buttonTakePhoto?.setOnClickListener {
            takePhoto.launch()
        }
    }

    private fun loadPhotosFromInternalStorageIntoRecyclerView() {
        lifecycleScope.launch {
            val photos = loadPhotosFromInternalStorage()
            internalStoragePhotoAdapter.submitList(photos)
        }
    }

    // =============================================================================================
    //          ACCESS STORAGE METHODS
    // =============================================================================================

    private fun savePhotoToInternalStorage(filename: String, bmp: Bitmap): Boolean {
        return try {
            requireActivity().openFileOutput("$filename.jpg", MODE_PRIVATE).use { stream ->
                if(!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                    throw IOException("Couldn't save bitmap.")
                }
            }
            true
        } catch(e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun deletePhotoFromInternalStorage(filename: String): Boolean {
        return try {
            requireActivity().deleteFile(filename)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun loadPhotosFromInternalStorage(): List<InternalStoragePhoto> {
        return withContext(IO) {
            val files = requireActivity().filesDir.listFiles()
            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
                val bytes = it.readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                InternalStoragePhoto(it.name, bmp)
            } ?: listOf()
        }
    }

}