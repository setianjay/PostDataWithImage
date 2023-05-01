package com.setianjay.postdataandimageexample.application

import android.app.Application
import com.setianjay.postdataandimageexample.di.initial.InitialApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication: Application() {
    @Inject lateinit var initialApp: InitialApp

    override fun onCreate() {
        super.onCreate()
        initialApp.startTimber()
    }
}