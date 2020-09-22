package com.example.dailyrain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class Alarm : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val rainChecker = RainChecker(context)
        rainChecker.rainCheck()
    }


}