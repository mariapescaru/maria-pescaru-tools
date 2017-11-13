package com.escudesignatrezzi.commons.samples.activities

import android.os.Bundle
import com.escudesignatrezzi.commons.activities.BaseSimpleActivity
import com.escudesignatrezzi.commons.complicate.R
import com.escudesignatrezzi.commons.extensions.storeStoragePaths

class MainActivity : BaseSimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        storeStoragePaths()
    }
}
