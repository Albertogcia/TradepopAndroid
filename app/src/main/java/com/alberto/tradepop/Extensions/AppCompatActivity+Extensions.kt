package com.alberto.tradepop.Extensions

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.alberto.tradepop.R

fun AppCompatActivity.showSimpleMessage(title: String, description: String, context: Context) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle(title)
    builder.setMessage(description)
    builder.setPositiveButton(resources.getString(R.string.generic_ok), null)
    builder.show()
}

fun AppCompatActivity.showLoadingDialog(context: Context): AlertDialog {
    val builder = AlertDialog.Builder(context)
    builder.setCancelable(false)
    builder.setView(R.layout.loading_dialog)
    return builder.show()
}

fun AppCompatActivity.showEditTextDialog(
    context: Context,
    function: (String) -> Unit
): AlertDialog {
    val builder = AlertDialog.Builder(context)
    val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null)
    builder.setView(dialogView)
    builder.setPositiveButton(resources.getString(R.string.generic_ok)) { _, _ ->
        val text = dialogView.findViewById<EditText>(R.id.resetPasswordEmail).text.toString()
        function(text)
    }
    builder.setNegativeButton(resources.getString(R.string.generic_cancel), null)
    return builder.show()
}