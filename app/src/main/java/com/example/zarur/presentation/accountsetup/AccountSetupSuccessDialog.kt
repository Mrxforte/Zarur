package com.example.zarur.presentation.accountsetup

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.zarur.R

class AccountSetupSuccessDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.dialog_setup_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Mock redirection after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded) {
                dismiss()
                findNavController().navigate(R.id.action_faceRecognitionScanFragment_to_exploreFragment)
            }
        }, 3000)
    }
}
