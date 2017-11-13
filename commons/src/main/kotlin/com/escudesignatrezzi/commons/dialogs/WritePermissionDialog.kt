package com.escudesignatrezzi.commons.dialogs

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import com.escudesignatrezzi.commons.R
import com.escudesignatrezzi.commons.activities.BaseSimpleActivity
import com.escudesignatrezzi.commons.extensions.setupDialogStuff


class WritePermissionDialog(context: Context, val callback: () -> Unit) {
    var dialog: AlertDialog

    init {
        val view = LayoutInflater.from(context).inflate(
                R.layout.dialog_write_permission,
                null
        )

        dialog = AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, { dialog, which -> dialogConfirmed() })
                .setOnCancelListener { BaseSimpleActivity.funAfterSAFPermission = null }
                .create().apply {
            context.setupDialogStuff(view, this, R.string.confirm_storage_access_title)
        }
    }

    private fun dialogConfirmed() {
        dialog.dismiss()
        callback()
    }
}
