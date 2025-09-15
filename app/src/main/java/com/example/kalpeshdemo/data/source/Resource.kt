package com.example.kalpeshdemo.data.source

sealed class Resource<out R> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val exception: Any) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}