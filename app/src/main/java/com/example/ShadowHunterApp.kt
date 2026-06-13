package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.db.AppDatabase
import com.example.data.repository.GameRepository

class ShadowHunterApp : Application() {
    companion object {
        lateinit var database: AppDatabase
        lateinit var repository: GameRepository
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "shadow_hunter_db"
        )
        .fallbackToDestructiveMigration()
        .build()

        repository = GameRepository(database.gameDao())
    }
}
