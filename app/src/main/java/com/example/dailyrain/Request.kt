package com.example.dailyrain
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonElement
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class Request(private val url: String) {
    fun run(): JSONArray {
        val forecastJsonStr = URL(url).readText()
        val jsonArray = JSONArray(forecastJsonStr)
        return jsonArray
    }

}