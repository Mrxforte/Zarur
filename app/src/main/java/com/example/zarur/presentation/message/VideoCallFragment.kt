package com.example.zarur.presentation.message

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.os.SystemClock
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoCallFragment : Fragment(R.layout.fragment_video_call) {

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private lateinit var textureView: TextureView
    private var isMuted = false
    private var isCameraOn = true
    private var facingLens = CameraCharacteristics.LENS_FACING_FRONT

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                if (textureView.isAvailable) {
                    openCamera()
                } else {
                    textureView.surfaceTextureListener = surfaceTextureListener
                }
            } else {
                Toast.makeText(requireContext(), "Camera permission required for video call", Toast.LENGTH_SHORT).show()
            }
        }

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textureView = view.findViewById(R.id.textureViewLocalCamera)

        val chronometer = view.findViewById<Chronometer>(R.id.tvVideoCallTimer)
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()

        checkPermissionAndStartCamera()

        val btnMuteVideo = view.findViewById<ImageView>(R.id.btnMuteVideo)
        btnMuteVideo.setOnClickListener {
            isMuted = !isMuted
            btnMuteVideo.alpha = if (isMuted) 0.5f else 1.0f
        }

        val btnToggleCamera = view.findViewById<ImageView>(R.id.btnToggleCamera)
        btnToggleCamera.setOnClickListener {
            isCameraOn = !isCameraOn
            val cvLocal = view.findViewById<View>(R.id.cvLocalCamera)
            cvLocal.visibility = if (isCameraOn) View.VISIBLE else View.GONE
            btnToggleCamera.alpha = if (isCameraOn) 1.0f else 0.5f
        }

        view.findViewById<View>(R.id.btnEndVideoCall).setOnClickListener {
            chronometer.stop()
            closeCamera()
            findNavController().popBackStack()
        }
    }

    private fun checkPermissionAndStartCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (textureView.isAvailable) {
                openCamera()
            } else {
                textureView.surfaceTextureListener = surfaceTextureListener
            }
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        val manager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            var cameraId = manager.cameraIdList.firstOrNull()
            for (id in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing == facingLens) {
                    cameraId = id
                    break
                }
            }

            if (cameraId == null) return

            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    startCameraPreview()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    closeCamera()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    closeCamera()
                }
            }, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startCameraPreview() {
        val camera = cameraDevice ?: return
        val texture = textureView.surfaceTexture ?: return
        val surface = Surface(texture)

        try {
            val previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder.addTarget(surface)

            camera.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        try {
                            previewRequestBuilder.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                            )
                            session.setRepeatingRequest(previewRequestBuilder.build(), null, null)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {}
                },
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun closeCamera() {
        try {
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        closeCamera()
    }
}
