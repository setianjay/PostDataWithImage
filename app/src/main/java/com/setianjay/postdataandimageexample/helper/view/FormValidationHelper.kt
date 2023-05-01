package com.setianjay.postdataandimageexample.helper.view

import android.content.Context
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.StringRes
import com.setianjay.postdataandimageexample.R
import com.setianjay.postdataandimageexample.helper.view.model.FormValidationHelperModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * di jadiin class, inject context dan ubah parameter fungsi menjadi data class yang menampung
 * view dan view_name agar pesan message bisa sesuai dengan nama view nya.
 * */
class FormValidationHelper @Inject constructor(@ApplicationContext private val context: Context) {

    fun checkIsEmpty(vararg viewsValidation: FormValidationHelperModel): Boolean {
        var isAllPassed = true
        for (viewValidation in viewsValidation) {
            val view = viewValidation.view
            val viewName = viewValidation.viewName

            when (view) {
                // when View is EditText
                is EditText -> {
                    if (view.text.isEmpty()) {
                        view.error = setMessage(R.string.input_empty, viewName)
                        isAllPassed = false
                    }
                }
                // when View is Spinner
                is Spinner -> {
                    if (view.selectedItemPosition == 0) {
                        (view.selectedView as TextView).error =
                            setMessage(R.string.input_empty, viewName)
                        isAllPassed = false
                    }
                }
            }
        }

        return isAllPassed
    }

    private fun setMessage(@StringRes strId: Int, vararg stringArg: String): String {
        return context.getString(strId, stringArg)
    }
}