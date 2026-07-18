package com.wwwescape.photoslideshow.data.music

import androidx.annotation.RawRes
import com.wwwescape.photoslideshow.R

/**
 * A bundled, licensed-for-use background track. Names/credit text are proper nouns and license
 * copy, not UI chrome — kept as plain Kotlin strings rather than translatable string resources,
 * same treatment as the library names in LicensesScreen.
 */
enum class MusicTrack(
    @RawRes val rawResId: Int,
    val displayName: String,
    val creditLine: String,
    val creditUrl: String,
) {
    // Uppbeat license code NNF6SGYGV7AFCTJA
    RENEW(
        rawResId = R.raw.track_renew,
        displayName = "Renew — Tranquilium",
        creditLine = "Music from #Uppbeat (free for Creators!)",
        creditUrl = "https://uppbeat.io/t/tranquilium/renew",
    ),

    // Uppbeat license code S1AKWJTAENBTP1RP
    EQUILIBRIUM(
        rawResId = R.raw.track_equilibrium,
        displayName = "Equilibrium — Sanchii",
        creditLine = "Music from #Uppbeat (free for Creators!)",
        creditUrl = "https://uppbeat.io/t/sanchii/equilibrium",
    ),
}
