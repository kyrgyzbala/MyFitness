package com.kyrgyzcoder.myfitness.utils

import androidx.room.TypeConverter
import androidx.versionedparcelable.VersionedParcel
import com.kyrgyzcoder.myfitness.DATE_FORMAT
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class DateConverter {

    var dateFormat: DateFormat = SimpleDateFormat(DATE_FORMAT, Locale.ROOT)

    @TypeConverter
    fun fromTimestamp(value: String?): Date? {
        if (value != null) {
            try {
                return dateFormat.parse(value)!!
            } catch (e: VersionedParcel.ParcelException) {
                e.printStackTrace()
            }
            return null
        }
        return null
    }

    @TypeConverter()
    fun dateToTimeStamp(value: Date?): String? {
        return if (value == null)
            null
        else
            dateFormat.format(value)
    }

}