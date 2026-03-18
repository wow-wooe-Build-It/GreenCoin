package com.greencoins.app

import android.app.Application
import com.greencoins.app.data.SupabaseManager

class GreenCoinsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseManager.initialize(this)
    }
}
