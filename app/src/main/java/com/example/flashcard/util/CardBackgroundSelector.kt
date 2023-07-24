package com.example.flashcard.util

import com.example.flashcard.R

class CardBackgroundSelector {
    private val backgrounds = mapOf(
        "map_pattern" to R.drawable.map_pattern,
        "curve_pattern" to R.drawable.curve_pattern,
        "square_pattern" to R.drawable.square_pattern,
        "floral_pattern" to R.drawable.floral_pattern,
        "dates_pattern" to R.drawable.dates_minimal_halftone_patterns,
        "painted_pattern" to R.drawable.painted_wall_texture
    )

    fun selectPattern(patternName: String): Int? {
        return if (patternName in backgrounds.keys) {
            backgrounds[patternName]
        } else {
            null
        }
    }
}