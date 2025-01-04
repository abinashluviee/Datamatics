package com.testapp.service

import java.text.SimpleDateFormat
import java.util.Locale


fun convertDate(input:String):String{

   return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yy", Locale.getDefault())

        // Parse and format
        val date = inputFormat.parse(input)
        val formattedDate = outputFormat.format(date)

       formattedDate
    }catch (e:Exception){
        ""
    }

}