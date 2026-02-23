package com.soaharisonstebane.mneme.helper

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

fun showSnackbar(rootView: View, anchor: View, message: String) {
    Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
        .setAnchorView(anchor)
        .show()
}

fun showSnackbar(rootView: View, anchor: View, @StringRes message: Int) {
    Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
        .setAnchorView(anchor)
        .show()
}