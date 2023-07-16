package com.pjm.cours.data.remote

sealed interface ApiResponse<T : Any>

class ApiResultSuccess<T : Any>(val data: T) : ApiResponse<T>
class ApiResultError<T : Any>(val code: Int, val message: String) : ApiResponse<T>
class ApiResultException<T : Any>(val throwable: Throwable) : ApiResponse<T>