package com.alberto.tradepop.Extensions

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.alberto.tradepop.R

class DialogUtils {

    fun showSimpleMessage(title: String, description: String, buttonMessage: String, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(description)
        builder.setPositiveButton(buttonMessage, null)
        builder.show()
    }

    fun showLoadingDialog(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setView(R.layout.loading_dialog)
        return builder.show()
    }
}