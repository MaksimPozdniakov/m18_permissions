package project.moms.attractions.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import project.moms.attractions.databinding.FragmentTakePhotoBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import com.bumptech.glide.Glide

private const val FILE_FORMAT = "yyyy-MM-dd-HH-mm-ss"

class FragmentTakePhoto : Fragment() {

    private var _binding : FragmentTakePhotoBinding? = null
    private val binding : FragmentTakePhotoBinding
        get() { return _binding!! }

    private val viewModel: FragmentTakePhotoViewModel by viewModels()

    private lateinit var executor: Executor
    private val name = SimpleDateFormat(FILE_FORMAT, Locale.US)
        .format(System.currentTimeMillis())
    private var imageCapture: ImageCapture? = null

    private val launcher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {map ->
            if (map.values.all { it }) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "permission is not Granted", Toast.LENGTH_SHORT).show()
            }

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTakePhotoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        executor = ContextCompat.getMainExecutor(requireContext())

        binding.takePhotoButton.setOnClickListener { takePhoto() }

        checkPermissions()
    }

    private fun checkPermissions() {
        val isAllGranted = REQUEST_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) ==
                    PackageManager.PERMISSION_GRANTED
        }

        if (isAllGranted) {
            startCamera()
            Toast.makeText(requireContext(), "permission is Granted", Toast.LENGTH_SHORT).show()
        } else {
            launcher.launch(REQUEST_PERMISSIONS)
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture?: return
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ).build()
        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(requireContext(),
                        "Photo saved on: ${outputFileResults.savedUri}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Glide
                        .with(requireContext())
                        .load(outputFileResults.savedUri)
                        .circleCrop()
                        .into(binding.imagePreview)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(requireContext(),
                        "Photo failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    exception.printStackTrace()
                }

            }
        )
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

            imageCapture = ImageCapture.Builder().build()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
        }, executor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val REQUEST_PERMISSIONS: Array<String> = buildList {
            add(Manifest.permission.CAMERA)
        }.toTypedArray()
    }

}