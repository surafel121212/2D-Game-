package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hero_state")
data class HeroState(
    @PrimaryKey val heroId: String,
    val isUnlocked: Boolean = false
)
