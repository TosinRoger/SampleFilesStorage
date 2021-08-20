package br.com.tosin.samplefilesstorage.ui.main

import android.Manifest
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import br.com.tosin.samplefilesstorage.util.ProviderFileName
import br.com.tosin.samplefilesstorage.util.StorageFolder
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainFragment : Fragment() {

    companion object {
        private const val TAG = "Debug_tag"
        fun newInstance() = MainFragment()
    }

    private var _binding: MainFragmentBinding? = null
    private val binding: MainFragmentBinding get() = _binding!!

    private lateinit var internalStoragePhotoAdapter: InternalStoragePhotoAdapter

    private lateinit var takePhoto: ActivityResultLauncher<Uri>
    private lateinit var openGallery: ActivityResultLauncher<String>
    private var cameraUriTemp: Uri? = null

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
            val delegate = object : StorageFileDelegate {
                override fun onSuccess() {
                    loadPhotosFromInternalStorageIntoRecyclerView()
                    Toast.makeText(
                        requireContext(),
                        "Photo successfully deleted",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onError(msgError: String, exception: java.lang.Exception?) {
                    Toast.makeText(requireContext(), msgError, Toast.LENGTH_SHORT)
                        .show()
                }

            }
            ManagerFilesWithActivityReference
                .deleteFileFromInternalStorage(
                    this,
                    "/FolderOne/${it.name}",
                    delegate
                )
        }

        _binding?.recyclerViewFilesInApp?.apply {
            adapter = internalStoragePhotoAdapter
            layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
        }

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

        val delegateTakePhoto = ActivityResultCallback<Boolean> { result ->
            if (result) {
                // result === /data/user/0/br.com.tosin.samplefilesstorage/cache/TEMP_IMAGE/2021-08-20_-_14:16:17.jpg
                ManagerFilesWithActivityReference
                    .openContentImageAndMoveToApp(
                        requireContext(),
                        cameraUriTemp!!,
                        StorageFolder.FROM_CAMERA,
                        "camera_${ProviderFileName.createImageName()}"
                    )
                cameraUriTemp = null
            }
            else {
                showMsgError("problemas ao tirar a foto")
            }
        }

        takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture(), delegateTakePhoto)

        val delegateOpenGallery = ActivityResultCallback<Uri> { result ->
            if (result == null) {
                showMsgError("problemas ao buscar imagen da galeria")
            }
            else {
                // result === content://com.android.providers.media.documents/document/image%3A33
                ManagerFilesWithActivityReference
                    .openContentImageAndMoveToApp(
                        requireContext(),
                        result,
                        StorageFolder.FROM_GALLERY,
                        "gallery_${ProviderFileName.createImageName()}"
                    )
            }
        }

        openGallery = registerForActivityResult(ActivityResultContracts.GetContent(), delegateOpenGallery)

        _binding?.buttonTakePhoto?.setOnClickListener {
            val fileName = ProviderFileName.createImageName()
            cameraUriTemp = ManagerFilesWithActivityReference.provideUriFileWithAuthority(
                requireContext(),
                StorageFolder.TEMP_IMAGE,
                fileName
            )
            takePhoto.launch(cameraUriTemp)
        }

        _binding?.buttonOpenGallery?.setOnClickListener {
            openGallery.launch("image/*")
        }

        loadPhotosFromInternalStorageIntoRecyclerView()

        val stringPermissions = arrayOf(Manifest.permission.CAMERA)

        ActivityCompat.requestPermissions(requireActivity(), stringPermissions, 169)
    }

    private fun showMsgError(msg: String) {
        Toast.makeText(
            requireContext(),
            msg,
            Toast.LENGTH_LONG
        ).show()
    }

    // =============================================================================================
    //          CALL STORAGE METHODS FROM 'VIEW'
    // =============================================================================================


    private fun loadPhotosFromInternalStorageIntoRecyclerView() {
        lifecycleScope.launch {
            val photos = loadPhotosFromInternalStorage()
            internalStoragePhotoAdapter.submitList(photos)
        }
    }

    // =============================================================================================
    //          ACCESS STORAGE METHODS
    // =============================================================================================


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