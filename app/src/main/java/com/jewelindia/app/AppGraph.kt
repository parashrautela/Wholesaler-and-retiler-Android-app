package com.jewelindia.app

import android.app.Application

/**
 * Manual dependency injection container for Jewel India Android app.
 * Created once in [JewelApplication.onCreate].
 */
class AppGraph(val app: Application) {
    // Services and repositories will be registered as phases progress.
}
