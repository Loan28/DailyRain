package com.example.dailyrain

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class RainChecker (base: Context) : ContextWrapper(base) {

    @SuppressLint("MissingPermission")
    fun rainCheck() {
        val startTime = Date()
        val endTime = Date().add(Calendar.HOUR, 2)
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'") // Quoted "Z" to indicate UTC, no timezone
        val nowAsISO: String = df.format(startTime)
        val endAsISO: String = df.format(endTime)
        var jsonArray = JSONArray()
        if (requestPermission()) {
            var fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    // Got last known location. In some rare situations this can be null.
                    val latitude = location?.latitude ?: fail("No location found on the device")
                    val longitude = location?.longitude
                    val urlAPI =
                        "https://api.climacell.co/v3/weather/nowcast?lat=$latitude&lon=$longitude&unit_system=si&timestep=20&start_time=$nowAsISO&end_time=$endAsISO&fields=precipitation&apikey=MqMyllKaVWogvwKtmLssipzyqbJ5Yqod"
                    val urlNeuchatel =
                        "https://api.climacell.co/v3/weather/nowcast?lat=46.992979&lon=6.931933&unit_system=si&timestep=20&start_time=$nowAsISO&end_time=$endAsISO&fields=precipitation&apikey=MqMyllKaVWogvwKtmLssipzyqbJ5Yqod"
                    doAsync {
                        val result = Request(urlNeuchatel).run()
                        uiThread {
                            jsonArray = result
                        }
                    }
                    val forecastRain = isItRaining(jsonArray)
                    val notificationUtils = NotificationUtils(this.baseContext)
                    val notification = notificationUtils.getNotificationBuilder().setContentText(forecastRain).setContentTitle("DailyRain").build()
                    notificationUtils.getManager().notify(150, notification)
                }
        }
    }

    private fun requestPermission(): Boolean {
        val hasForegroundPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasForegroundPermission) {
            return true
        } else {
            ActivityCompat.requestPermissions(MainActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
        return false
    }

    fun Date.add(field: Int, amount: Int): Date {
        Calendar.getInstance().apply {
            time = this@add
            add(field, amount)
            return time
        }
    }

    private fun isItRaining(jsonArray: JSONArray): String{
        for (i in 0 until jsonArray.length()){
            val item = jsonArray.getJSONObject(i)
            val rainMMHR  = item.getJSONObject("precipitation").getDouble("value")
            if(rainMMHR >= 0.2){
                return "Raining"
            }
        }
        return "None"
    }
    private fun fail(message: String): Nothing {
        throw IllegalArgumentException(message)
    }
}