package team449.frc.refereeappbase.helpers

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.util.DisplayMetrics
import android.view.ContextThemeWrapper
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import team449.frc.refereeappbase.R
import team449.frc.refereeappbase.fragment.PageChanger
import team449.frc.refereeappbase.main.Editor
import team449.frc.refereeappbase.model.StaticResources


fun info(context: Context, title: String, body: String) {
    AlertDialog.Builder(ContextThemeWrapper(context, R.style.AlertDialogCustom))
        .setTitle(title)
        .setMessage(body)
        .setPositiveButton("Okay", null)
        .show()
        .findViewById<TextView>(android.R.id.message)?.textSize = StaticResources.dialogTextSize
}

fun editDialog(context: Activity, matches: List<Pair<String,String>>, editor: Editor) {
    val dm = DisplayMetrics()
    context.windowManager.defaultDisplay.getMetrics(dm)
    AlertDialog.Builder(ContextThemeWrapper(context, R.style.AlertDialogCustom))
        .setTitle("Select a match to edit")
        .setItems(matches.map { it.first }.toTypedArray()) { _, which -> editor.edit(matches[which].second) }
        .create().let {
            it.show()
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(it.window?.attributes)
            lp.width = (dm.widthPixels * 0.8).toInt()
            if (matches.size > 10) lp.height = (dm.heightPixels * 0.55).toInt()
            it.window?.attributes = lp
    }
}

fun confirmationDialog(context: Activity, title: String, body: String, positiveButtonName: String, positiveAction: (dialog: DialogInterface, id: Int) -> Unit) {
    AlertDialog.Builder(ContextThemeWrapper(context, R.style.AlertDialogCustom))
        .setTitle(title)
        .setMessage(body)
        .setPositiveButton(positiveButtonName, positiveAction)
        .setNegativeButton("Cancel", null)
        .show()
        .findViewById<TextView>(android.R.id.message)?.textSize = StaticResources.dialogTextSize
}

fun hardValidationDialog(context: Activity, body: String, pageChanger: PageChanger?, page: Int) {
    AlertDialog.Builder(ContextThemeWrapper(context, R.style.AlertDialogCustom))
        .setTitle("Data validation failed")
        .setMessage(body)
        .setPositiveButton("Fix errors") { _, _ -> pageChanger?.changePage(page) }
        .show()
        .findViewById<TextView>(android.R.id.message)?.textSize = StaticResources.dialogTextSize
}

fun softValidationDialog(context: Activity, body: String, pageChanger: PageChanger?, page: Int, submit: () -> Unit) {
    AlertDialog.Builder(ContextThemeWrapper(context, R.style.AlertDialogCustom))
        .setTitle("Data validation failed")
        .setMessage(body)
        .setPositiveButton("Submit anyway") { _, _ -> submit() }
        .setNegativeButton("Fix errors") { _, _ -> pageChanger?.changePage(page) }
        .show()
        .findViewById<TextView>(android.R.id.message)?.textSize = StaticResources.dialogTextSize
}