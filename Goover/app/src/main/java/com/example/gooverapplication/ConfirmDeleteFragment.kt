package com.example.gooverapplication

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ConfirmDeleteFragment
    (private val category: String, private val onDeleteConfirmed: () -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage("삭제하면 $category 관련 문제들이 삭제됩니다. 삭제하시겠습니까?")
                .setPositiveButton("Yes") { _, _ ->
                    onDeleteConfirmed.invoke()
                }
                .setNegativeButton("No") { _, _ ->
                    // 아무것도 안하고 창모드 끄기
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}