package com.escudesignatrezzi.commons.extensions

import android.widget.EditText

val EditText.value: String get() = text.toString().trim()
