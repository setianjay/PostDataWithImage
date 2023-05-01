package com.setianjay.postdataandimageexample.di.initial

import com.setianjay.postdataandimageexample.BuildConfig
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
class InitialApp @Inject constructor() {

    fun startTimber(){
        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
    }
}