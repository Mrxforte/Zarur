package com.example.zarur.presentation.booking

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.zarur.R
import com.example.zarur.databinding.FragmentEReceiptBinding
import com.example.zarur.domain.model.Receipt
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.usecase.GetUserProfileUseCase
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class EReceiptFragment : Fragment(R.layout.fragment_e_receipt) {

    private var _binding: FragmentEReceiptBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var getUserProfileUseCase: GetUserProfileUseCase

    private var currentUserName: String = "User"
    private var currentUserPhone: String = ""
    private var currentReceipt: Receipt? = null
    private var isLiked: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEReceiptBinding.bind(view)

        @Suppress("DEPRECATION")
        currentReceipt = arguments?.getSerializable("receipt") as? Receipt

        setupReceiptViews()
        setupClickListeners()
        observeUserProfile()
    }

    private fun setupReceiptViews() {
        val receipt = currentReceipt ?: return
        binding.tvPropertyName.text = receipt.productName
        binding.tvPropertyCategory.text = receipt.category
        binding.tvPropertyPrice.text = receipt.priceFormatted
        binding.tvTransactionId.text = receipt.transactionId
        binding.tvDate.text = receipt.date
        binding.tvPaymentMethod.text = receipt.paymentMethod
        binding.tvStatus.text = receipt.status

        if (receipt.productImage.isNotBlank()) {
            Glide.with(this)
                .load(receipt.productImage)
                .placeholder(R.drawable.ic_e_receipt)
                .into(binding.ivProperty)
        } else {
            binding.ivProperty.setImageResource(R.drawable.ic_e_receipt)
        }
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnLike.setOnClickListener {
            isLiked = !isLiked
            if (isLiked) {
                binding.btnLike.setColorFilter(ContextCompat.getColor(requireContext(), R.color.red_uzum))
                Toast.makeText(requireContext(), "Saved to favorites", Toast.LENGTH_SHORT).show()
            } else {
                binding.btnLike.clearColorFilter()
                Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDelete.setOnClickListener {
            confirmDeleteReceipt()
        }

        binding.btnShare.setOnClickListener {
            showShareChoiceDialog()
        }

        binding.btnShareReceipt.setOnClickListener {
            showShareChoiceDialog()
        }

        binding.btnReview.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.EReceiptFragment) {
                findNavController().navigate(R.id.action_EReceiptFragment_to_leaveReviewFragment)
            }
        }
    }

    private fun observeUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                getUserProfileUseCase().collect { resource ->
                    if (resource is Resource.Success && resource.data != null) {
                        val user = resource.data
                        currentUserName = when {
                            user.fullName.isNotBlank() -> user.fullName
                            user.nickname.isNotBlank() -> user.nickname
                            user.email.isNotBlank() -> user.email.substringBefore("@")
                            else -> getString(R.string.profile_name)
                        }
                        if (user.phone.isNotBlank()) {
                            currentUserPhone = user.phone
                            binding.tvUserPhone.text = user.phone
                        }
                        binding.tvUserName.text = currentUserName
                    }
                }
            }
        }
    }

    private fun showShareChoiceDialog() {
        val options = arrayOf(
            "Share as Text (SMS / Messages)",
            "Share as Screenshot (Image)"
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Share Receipt")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> shareAsText()
                    1 -> shareAsImage()
                }
            }
            .show()
    }

    private fun shareAsText() {
        val receipt = currentReceipt
        val shareText = """
            🧾 Zarur E-Receipt
            --------------------------
            Item: ${receipt?.productName ?: "La Grand Maison"}
            Price: ${receipt?.priceFormatted ?: "1 200 000 uzs"}
            Name: $currentUserName
            Phone: ${currentUserPhone.ifEmpty { "+998 90 123 45 67" }}
            Transaction ID: ${receipt?.transactionId ?: "5457383979"}
            Date: ${receipt?.date ?: "24 Dec 2024"}
            Status: ${receipt?.status ?: "PAID"}
            --------------------------
            Downloaded via Zarur Mobile App
        """.trimIndent()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Zarur E-Receipt - ${receipt?.transactionId ?: "5457383979"}")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Receipt via SMS / Messages"))
    }

    private fun shareAsImage() {
        val viewToCapture = binding.containerReceiptContent
        val bitmap = captureViewBitmap(viewToCapture)
        val imageUri = saveBitmapToCache(bitmap)

        if (imageUri != null) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Receipt Screenshot"))
        } else {
            Toast.makeText(requireContext(), "Failed to generate receipt image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun captureViewBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        return try {
            val cacheFolder = File(requireContext().cacheDir, "receipt_screenshots")
            if (!cacheFolder.exists()) {
                cacheFolder.mkdirs()
            }
            val file = File(cacheFolder, "receipt_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun confirmDeleteReceipt() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Receipt")
            .setMessage("Are you sure you want to delete this receipt safely?")
            .setPositiveButton("Delete") { _, _ ->
                Toast.makeText(requireContext(), "Receipt deleted successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
