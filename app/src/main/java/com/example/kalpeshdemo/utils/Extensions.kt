package com.example.kalpeshdemo.utils

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.example.kalpeshdemo.R
import com.google.android.material.snackbar.Snackbar
import kotlin.math.absoluteValue

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun TextView.setTextColorRes(@ColorRes colorRes: Int) {
    setTextColor(ContextCompat.getColor(context, colorRes))
}

fun View.showSnackBar(
    msg: String,
    actionText: String? = null,
    action: ((View) -> Unit)? = null
) {
    val snackBar = Snackbar.make(this, msg, Snackbar.LENGTH_LONG)
    if (actionText != null && action != null) {
        snackBar.setAction(actionText) {
            action(this)
        }
    }
    snackBar.show()
}

fun Context.formatAsCurrency(amount: Double): String {
    val string = if (amount > 0) R.string.lbl_pos_amt else R.string.lbl_neg_amt
    return this.getString(string, amount.absoluteValue)
}
