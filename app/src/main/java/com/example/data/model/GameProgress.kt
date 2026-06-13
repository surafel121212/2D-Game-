package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_progress")
data class GameProgress(
    @PrimaryKey val id: Int = 1,
    val diamonds: Int = 100, // Starts with some diamonds as a welcome gift!
    val currentLevel: Int = 1,
    val selectedHeroId: String = "std_green"
)
