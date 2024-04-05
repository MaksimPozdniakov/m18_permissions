package project.moms.attractions.data

import android.app.Application
import androidx.room.Room

class App : Application() {
    lateinit var db: GalleryDataBase
    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            applicationContext,
            GalleryDataBase::class.java,
            "db"
        ).build()
    }
}