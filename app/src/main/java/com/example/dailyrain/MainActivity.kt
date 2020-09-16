package com.example.dailyrain

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant.now
import java.time.LocalDate
import java.util.*
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result;

class MainActivity : AppCompatActivity() {

    private val channelId = "com.example.dailyrain"
    private var lastLocation = null
    private val client = OkHttpClient()

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val startTime = Date()
        val endTime = Date().add(Calendar.HOUR, 2)
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'") // Quoted "Z" to indicate UTC, no timezone
        val nowAsISO: String = df.format(startTime)
        val endAsISO: String = df.format(endTime)
        createNotificationChannel();
        if(requestPermission()){
            var fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    // Got last known location. In some rare situations this can be null.
                    val latitude = location?.latitude ?: fail("No location found on the device")
                    val longitude = location?.longitude
                    this.NotifyTImeText.setText(longitude.toString() + " " + latitude.toString())
                    //val urlAPI = "https://api.climacell.co/v3/weather/realtime?lat=" + latitude + "&lon=" + longitude + "&unit_system=si&fields=precipitation&apikey=MqMyllKaVWogvwKtmLssipzyqbJ5Yqod"
                    val urlAPI = "https://api.climacell.co/v3/weather/nowcast?lat=" + latitude + "&lon=" + longitude + "&unit_system=si&timestep=20&start_time=" + nowAsISO + "&end_time=" + endAsISO + "&fields=precipitation&apikey=MqMyllKaVWogvwKtmLssipzyqbJ5Yqod"
                    val urlNeuch = "https://api.climacell.co/v3/weather/nowcast?lat=46.992979&lon=6.931933&unit_system=si&timestep=20&start_time=" + nowAsISO + "&end_time=" + endAsISO + "&fields=precipitation&apikey=MqMyllKaVWogvwKtmLssipzyqbJ5Yqod"
                    run(urlNeuch)
                    val test = "ok"
                    //val response : khttp.responses.Response = khttp.get(urlNeuch)
                    //val obj : JSONObject = response.jsonObject


                }
        }

        this.btnNotify1.setOnClickListener{
            var builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("My notification")
                .setContentText("Much longer text that cannot fit one line...")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line...")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            with(NotificationManagerCompat.from(this)) {
                // notificationId is a unique int for each notification that you must define
                notify(0, builder.build())
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
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
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

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun run(url: String) {

        val (request, response, result) = url
            .httpGet()
            .responseString()

        when (result) {
            is Result.Failure -> {
                val ex = result.getException()
                println(ex)
            }
            is Result.Success -> {
                val data = result.get()
                println(data)
            }
        }
    }

    fun isItRaining(jsonArray: JSONArray): String{
        for (i in 0 until jsonArray.length()){
            val item = jsonArray.getJSONObject(i)
            val rainMMHR  = item.getJSONObject("precipitation").getDouble("value")
            if(rainMMHR >= 0.2){
                return "Raining"
            }
        }
        return "None"
    }
    fun fail(message: String): Nothing {
        throw IllegalArgumentException(message)
    }
}