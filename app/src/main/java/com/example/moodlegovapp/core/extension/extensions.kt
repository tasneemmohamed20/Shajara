package com.example.moodlegovapp.core.extension

import android.text.format.DateUtils
import java.util.Calendar


val Long.toRelativeTimeDisplay: String
    get() = DateUtils.getRelativeTimeSpanString(
        this,
        Calendar.getInstance().timeInMillis,
        DateUtils.MINUTE_IN_MILLIS
    ).toString()