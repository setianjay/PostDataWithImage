package com.setianjay.postdataandimageexample.data.repository

import com.setianjay.postdataandimageexample.data.remote.RemoteUserDataSource
import com.setianjay.postdataandimageexample.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(private val remoteUserDataSource: RemoteUserDataSource) :
    UserRepository {


}