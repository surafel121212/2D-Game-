package com.example.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.model.GameProgress
import com.example.data.model.HeroState

@Database(entities = [GameProgress::class, HeroState::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
