package com.escudesignatrezzi.commons.interfaces

interface SecurityTab {
    fun initTab(requiredHash: String, listener: HashListener)

    fun visibilityChanged(isVisible: Boolean)
}
