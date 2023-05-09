package com.setianjay.postdataandimageexample.data.remote

import com.setianjay.postdataandimageexample.data.remote.service.sipela.UserService
import javax.inject.Inject

class RemoteUserDataSourceImpl @Inject constructor(private val userService: UserService) :
    RemoteUserDataSource {


}