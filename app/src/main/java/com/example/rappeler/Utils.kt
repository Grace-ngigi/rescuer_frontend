package com.example.rappeler

import android.util.Base64
import org.json.JSONObject

class Utils {
    fun decodeJwt(token: String): JSONObject? {
        val parts = token.split('.')
        if (parts.size != 3) {
            return null
        }
        val payload = parts[1]
        val decodedPayload = String(Base64.decode(payload, Base64.DEFAULT))
        return JSONObject(decodedPayload)
    }
}