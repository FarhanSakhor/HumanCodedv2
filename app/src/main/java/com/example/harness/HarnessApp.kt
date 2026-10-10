package com.example.harness

import android.app.Application

class HarnessApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Logic.init(this)
    }
}
