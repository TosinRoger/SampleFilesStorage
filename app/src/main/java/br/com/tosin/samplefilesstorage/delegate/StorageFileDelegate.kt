package br.com.tosin.samplefilesstorage.delegate

import java.lang.Exception

interface StorageFileDelegate {

    fun onSuccess()
    fun onError(msgError: String, exception: Exception?)
}