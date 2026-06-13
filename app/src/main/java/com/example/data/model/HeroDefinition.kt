package com.example.data.model

import androidx.compose.ui.graphics.Color

data class HeroDefinition(
    val id: String,
    val name: String,
    val isPremium: Boolean,
    val healthBonus: Float, // e.g. 0.20 for +20% HP
    val speedBonus: Float,  // e.g. 0.30 for +30% Speed
    val cost: Int,
    val baseColor: Color,
    val accentColor: Color,
    val desc: String
) {
    companion object {
        val HEROES = listOf(
            HeroDefinition(
                id = "std_green",
                name = "Standard (Green)",
                isPremium = false,
                healthBonus = 0f,
                speedBonus = 0f,
                cost = 0,
                baseColor = Color(0xFF4CAF50),       // Green vest
                accentColor = Color(0xFF3E2723),     // Dark brown hair
                desc = "Ready for the hunt!"
            ),
            HeroDefinition(
                id = "std_yellow",
                name = "Standard (Yellow)",
                isPremium = false,
                healthBonus = 0f,
                speedBonus = 0f,
                cost = 0,
                baseColor = Color(0xFFFFEB3B),       // Yellow jumpsuit
                accentColor = Color(0xFF212121),     // Black armor straps
                desc = "Quick & agile seeker."
            ),
            HeroDefinition(
                id = "assassin_hood",
                name = "Assassin Hood",
                isPremium = false,
                healthBonus = 0.20f,
                speedBonus = 0f,
                cost = 150,
                baseColor = Color(0xFF9E9E9E),       // Grey camouflage
                accentColor = Color(0xFF37474F),     // Charcoal steel
                desc = "+20% Health"
            ),
            HeroDefinition(
                id = "desert_ranger",
                name = "Desert Ranger",
                isPremium = false,
                healthBonus = 0f,
                speedBonus = 0.20f,
                cost = 200,
                baseColor = Color(0xFF81C784),       // Army green light
                accentColor = Color(0xFFE6EE9C),     // Tan details
                desc = "+20% Speed"
            ),
            HeroDefinition(
                id = "sheriff_cowboy",
                name = "Sheriff Cowboy",
                isPremium = false,
                healthBonus = 0f,
                speedBonus = 0.30f,
                cost = 300,
                baseColor = Color(0xFF8D6E63),       // Brown leather
                accentColor = Color(0xFFFFCA28),     // Gold badge
                desc = "+30% Speed"
            ),
            HeroDefinition(
                id = "doc_hazard",
                name = "Doctor Hazard",
                isPremium = true,
                healthBonus = 0f,
                speedBonus = 0.20f,
                cost = 250,
                baseColor = Color(0xFFE0F7FA),       // Hazmat white suit
                accentColor = Color(0xFF00ACC1),     // Toxic teal gear
                desc = "+20% Speed (Biohazard Suit)"
            ),
            HeroDefinition(
                id = "shadow_ninja",
                name = "Shadow Ninja",
                isPremium = true,
                healthBonus = 0.40f,
                speedBonus = 0f,
                cost = 600,
                baseColor = Color(0xFF263238),       // Stealth black suit
                accentColor = Color(0xFFD84315),     // Crimson headband
                desc = "+40% Health"
            ),
            HeroDefinition(
                id = "neon_blade",
                name = "Neon Blade",
                isPremium = true,
                healthBonus = 0.60f,
                speedBonus = 0f,
                cost = 800,
                baseColor = Color(0xFFE040FB),       // Neon violet
                accentColor = Color(0xFF00E5FF),     // Electric cyan
                desc = "+60% Health"
            ),
            HeroDefinition(
                id = "crimson_reaper",
                name = "Crimson Reaper",
                isPremium = true,
                healthBonus = 0f,
                speedBonus = 0.40f,
                cost = 1000,
                baseColor = Color(0xFFD50000),      // Grim scarlet Red
                accentColor = Color(0xFFEEEEEE),      // Pale bone mask
                desc = "+40% Speed"
            ),
            HeroDefinition(
                id = "frost_golem",
                name = "Frost Golem",
                isPremium = true,
                healthBonus = 0.80f,
                speedBonus = 0f,
                cost = 1500,
                baseColor = Color(0xFF80DEEA),      // Frosted ice blue
                accentColor = Color(0xFF0288D1),      // Frozen glacial crystal
                desc = "+80% Health"
            )
        )
    }
}
