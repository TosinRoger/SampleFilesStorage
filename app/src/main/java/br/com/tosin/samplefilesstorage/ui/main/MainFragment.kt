package br.com.tosin.samplefilesstorage.ui.main

import android.Manifest
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.tosin.samplefilesstorage.databinding.MainFragmentBinding
import br.com.tosin.samplefilesstorage.delegate.StorageFileDelegate
import br.com.tosin.samplefilesstorage.model.InternalStoragePhoto
import br.com.tosin.samplefilesstorage.ui.main.adapter.InternalStoragePhotoAdapter
import br.com.tosin.samplefilesstorage.util.ManagerFilesWithActivityReference
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainFragment : Fragment() {

    companion object {
        private const val ROOT_FOLDER = "MyImages"
        private const val TAG = "Debug_tag"
        fun newInstance() = MainFragment()
    }

    private var _binding: MainFragmentBinding? = null
    private val binding: MainFragmentBinding get() = _binding!!

    private lateinit var internalStoragePhotoAdapter: InternalStoragePhotoAdapter

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

        configView()
    }

    private fun configView() {
        internalStoragePhotoAdapter = InternalStoragePhotoAdapter {
            val isDeletionSuccessful = deletePhotoFromInternalStorage(it.name)
            if (isDeletionSuccessful) {
                loadPhotosFromInternalStorageIntoRecyclerView()
                Toast.makeText(requireContext(), "Photo successfully deleted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Failed to delete photo", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        _binding?.recyclerViewFilesInApp?.apply {
            adapter = internalStoragePhotoAdapter
            layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
        }

        prepareToTakePhoto()

        loadPhotosFromInternalStorageIntoRecyclerView()

        val stringPermissions = arrayOf(Manifest.permission.CAMERA)

        ActivityCompat.requestPermissions(requireActivity(), stringPermissions, 169)
    }

    // =============================================================================================
    //          CALL STORAGE METHODS FROM 'VIEW'
    // =============================================================================================

    private fun prepareToTakePhoto() {
        val delegate = object : StorageFileDelegate {
            override fun onSuccess() {
                loadPhotosFromInternalStorageIntoRecyclerView()
                Toast
                    .makeText(requireContext(), "Photo saved successfully", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onError(msgError: String, exception: java.lang.Exception?) {
                Toast.makeText(
                    requireContext(),
                    msgError,
                    Toast.LENGTH_LONG
                ).show()
            }

        }
        val takePhoto =  ManagerFilesWithActivityReference
            .createCallTakePhoto(this, delegate)

        _binding?.buttonTakePhoto?.setOnClickListener {
           takePhoto.launch(null)
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
            if (files?.isNotEmpty() == true) {
                val myFolder = files.first().listFiles()
                println(myFolder)
                myFolder?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
                    val bytes = it.readBytes()
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    InternalStoragePhoto(it.name, bmp)
                } ?: listOf()
            } else
                listOf()
        }
    }
}