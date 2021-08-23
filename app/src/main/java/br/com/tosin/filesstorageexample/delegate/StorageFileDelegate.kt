package br.com.tosin.filesstorageexample.delegate

import java.lang.Exception

interface StorageFileDelegate {
    fun onSuccess()
    fun onError(msgError: String, exception: Exception?)
}