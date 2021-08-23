package br.com.tosin.filesstorageexample.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object ProviderFileName {

    fun createImageNameToJPG(): String {
        val aux = createImageName()
        return "$aux.jpg"
    }

    fun createImageName(): String {
        val calendar = Calendar.getInstance()
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd_-_HH:mm:ss", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}