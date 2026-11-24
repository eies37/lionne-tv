package com.lionne.tv

import android.app.Application

class LionneApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
