package com.escudesignatrezzi.commons.dialogs

import android.os.Environment
import android.os.Parcelable
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import android.view.LayoutInflater
import com.escudesignatrezzi.commons.R
import com.escudesignatrezzi.commons.activities.BaseSimpleActivity
import com.escudesignatrezzi.commons.adapters.FilepickerItemsAdapter
import com.escudesignatrezzi.commons.extensions.beVisible
import com.escudesignatrezzi.commons.extensions.getFilenameFromPath
import com.escudesignatrezzi.commons.extensions.internalStoragePath
import com.escudesignatrezzi.commons.extensions.setupDialogStuff
import com.escudesignatrezzi.commons.models.FileDirItem
import com.escudesignatrezzi.commons.views.Breadcrumbs
import kotlinx.android.synthetic.main.dialog_filepicker.view.*
import java.io.File
import java.util.*


class FilePickerDialog(val activity: BaseSimpleActivity,
                       var currPath: String = Environment.getExternalStorageDirectory().toString(),
                       val pickFile: Boolean = true,
                       val showHidden: Boolean = false,
                       val showFAB: Boolean = false,
                       val callback: (pickedPath: String) -> Unit) : Breadcrumbs.BreadcrumbsListener {

    var mFirstUpdate = true
    var mPrevPath = ""
    var mScrollStates = HashMap<String, Parcelable>()

    lateinit var mDialog: AlertDialog
    var mDialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_filepicker, null)

    init {
        if (!File(currPath).exists()) {
            currPath = activity.internalStoragePath
        }

        if (File(currPath).isFile) {
            currPath = File(currPath).parent
        }

        mDialogView.filepicker_breadcrumbs.listener = this
        updateItems()

        val builder = AlertDialog.Builder(activity)
                .setNegativeButton(R.string.cancel, null)
                .setOnKeyListener({ dialogInterface, i, keyEvent ->
                    if (keyEvent.action == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_BACK) {
                        val breadcrumbs = mDialogView.filepicker_breadcrumbs
                        if (breadcrumbs.childCount > 1) {
                            breadcrumbs.removeBreadcrumb()
                            currPath = breadcrumbs.getLastItem().path
                            updateItems()
                        } else {
                            mDialog.dismiss()
                        }
                    }
                    true
                })

        if (!pickFile)
            builder.setPositiveButton(R.string.ok, null)

        if (showFAB) {
            mDialogView.filepicker_fab.apply {
                beVisible()
                setOnClickListener { createNewFolder() }
            }
        }

        mDialog = builder.create().apply {
            context.setupDialogStuff(mDialogView, this, getTitle())
        }

        if (!pickFile) {
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
                verifyPath()
            })
        }
    }

    private fun getTitle() = if (pickFile) R.string.select_file else R.string.select_folder

    private fun createNewFolder() {
        CreateNewFolderDialog(activity, currPath) {
            callback(it.trimEnd('/'))
            mDialog.dismiss()
        }
    }

    private fun updateItems() {
        var items = getItems(currPath)
        if (!containsDirectory(items) && !mFirstUpdate && !pickFile && !showFAB) {
            verifyPath()
            return
        }

        items = items.sortedWith(compareBy({ !it.isDirectory }, { it.name.toLowerCase() }))

        val adapter = FilepickerItemsAdapter(activity, items) {
            if (it.isDirectory) {
                currPath = it.path
                updateItems()
            } else if (pickFile) {
                currPath = it.path
                verifyPath()
            }
        }

        val layoutManager = mDialogView.filepicker_list.layoutManager as LinearLayoutManager
        mScrollStates.put(mPrevPath.trimEnd('/'), layoutManager.onSaveInstanceState())

        mDialogView.apply {
            if (filepicker_list.adapter == null) {
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(context.resources.getDrawable(R.drawable.divider))
                    filepicker_list.addItemDecoration(this)
                }
            }

            filepicker_list.adapter = adapter
            filepicker_breadcrumbs.setBreadcrumb(currPath)
            filepicker_fastscroller.setViews(filepicker_list)
        }

        layoutManager.onRestoreInstanceState(mScrollStates[currPath.trimEnd('/')])
        mFirstUpdate = false
        mPrevPath = currPath
    }

    private fun verifyPath() {
        val file = File(currPath)
        if ((pickFile && file.isFile) || (!pickFile && file.isDirectory)) {
            sendSuccess()
        }
    }

    private fun sendSuccess() {
        callback(if (currPath.length == 1) currPath else currPath.trimEnd('/'))
        mDialog.dismiss()
    }

    private fun getItems(path: String): List<FileDirItem> {
        val items = ArrayList<FileDirItem>()
        val base = File(path)
        val files = base.listFiles() ?: return items
        for (file in files) {
            if (!showHidden && file.isHidden) {
                continue
            }

            val curPath = file.absolutePath
            val curName = curPath.getFilenameFromPath()
            val size = file.length()
            items.add(FileDirItem(curPath, curName, file.isDirectory, getChildren(file), size))
        }
        return items
    }

    private fun getChildren(file: File): Int {
        return if (file.listFiles() == null || !file.isDirectory) {
            0
        } else {
            file.listFiles().filter { !it.isHidden || (it.isHidden && showHidden) }.size
        }
    }

    private fun containsDirectory(items: List<FileDirItem>) = items.any { it.isDirectory }

    override fun breadcrumbClicked(id: Int) {
        if (id == 0) {
            StoragePickerDialog(activity, currPath) {
                currPath = it
                updateItems()
            }
        } else {
            val item = mDialogView.filepicker_breadcrumbs.getChildAt(id).tag as FileDirItem
            if (currPath != item.path.trimEnd('/')) {
                currPath = item.path
                updateItems()
            }
        }
    }
}
