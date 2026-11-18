package org.setu.laundrytracker.main

import android.content.Context
import android.content.res.Configuration
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import org.setu.laundrytracker.R
import timber.log.Timber

// Google Maps doesn't follow dark mode by itself, so in dark mode I give it Google's "night" colours
// (res/raw/map_style_night.json), used by both map screens
fun GoogleMap.useNightStyleIfDark(context: Context) {
    val nightMask = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

    if (nightMask == Configuration.UI_MODE_NIGHT_YES) {
        val ok = setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_night))
        Timber.d("Night map style applied: $ok")

        // the indoor floor plans of buildings ignore the style and stay bright white, so hide them at night
        isIndoorEnabled = false
    }
}
