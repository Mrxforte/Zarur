package com.example.zarur.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zarur.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LogoutBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_logout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dismiss()
        }
        view.findViewById<View>(R.id.btnLogout).setOnClickListener {
            // Handle Logout
            dismiss()
        }
    }
}
