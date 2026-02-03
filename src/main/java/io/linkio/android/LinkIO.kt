package io.linkio.android

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class LinkIO private constructor(private val config: LinkIOConfig) {
    
    private var deepLinkHandler: ((DeepLinkData) -> Unit)? = null
    private var pendingDeepLink: DeepLinkData? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    
    companion object {
        @Volatile
        private var instance: LinkIO? = null
        
        fun configure(application: Application, config: LinkIOConfig): LinkIO {
            return instance ?: synchronized(this) {
                instance ?: LinkIO(config).also {
                    instance = it
                    it.setupLifecycleObserver(application)
                }
            }
        }
        
        fun getInstance(): LinkIO {
            return instance ?: throw IllegalStateException("LinkIO not initialized. Call configure() first.")
        }
    }
    
    private fun setupLifecycleObserver(application: Application) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                if (config.autoCheckPendingLinks) {
                    checkPendingLink(application)
                }
            }
        })
    }
    
    fun handleDeepLink(intent: Intent?): Boolean {
        intent ?: return false
        
        val uri = intent.data ?: return false
        val host = uri.host ?: return false
        
        if (host != config.domain && host != "www.${config.domain}") {
            return false
        }
        
        val params = mutableMapOf<String, String>()
        uri.queryParameterNames.forEach { key ->
            uri.getQueryParameter(key)?.let { value ->
                params[key] = value
            }
        }
        
        val deepLink = DeepLinkData(
            url = uri.toString(),
            params = params,
            isDeferred = false
        )
        
        deepLinkHandler?.invoke(deepLink) ?: run {
            pendingDeepLink = deepLink
        }
        
        return true
    }
    
    fun setDeepLinkHandler(handler: (DeepLinkData) -> Unit) {
        this.deepLinkHandler = handler
        
        pendingDeepLink?.let {
            handler(it)
            pendingDeepLink = null
        }
    }
    
    fun checkPendingLink(context: Context) {
        val deviceId = getDeviceId(context)
        val url = "${config.backendURL}/api/pending-link/$deviceId"
        
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Silently fail - no pending link
            }
            
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { json ->
                        try {
                            val deepLink = gson.fromJson(json, DeepLinkData::class.java)
                            deepLinkHandler?.invoke(deepLink) ?: run {
                                pendingDeepLink = deepLink
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        })
    }
    
    fun trackReferral(
        referralCode: String,
        userId: String,
        metadata: Map<String, Any>? = null,
        callback: ((Boolean) -> Unit)? = null
    ) {
        val url = "${config.backendURL}/api/track-referral"
        
        val body = mutableMapOf<String, Any>(
            "referralCode" to referralCode,
            "userId" to userId
        )
        
        metadata?.let {
            body["metadata"] = it
        }
        
        val json = gson.toJson(body)
        val requestBody = json.toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback?.invoke(false)
            }
            
            override fun onResponse(call: Call, response: Response) {
                callback?.invoke(response.isSuccessful)
            }
        })
    }
    
    private fun getDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences("linkio_prefs", Context.MODE_PRIVATE)
        val key = "device_id"
        
        return prefs.getString(key, null) ?: run {
            val newId = java.util.UUID.randomUUID().toString()
            prefs.edit().putString(key, newId).apply()
            newId
        }
    }
}
