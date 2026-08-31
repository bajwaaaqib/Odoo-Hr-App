package com.example.odoohr.util

import com.example.odoohr.data.model.GeofenceLocationPreset
import com.example.odoohr.data.model.GeofenceZone
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object GeofenceCalculator {

    private const val EARTH_RADIUS_METERS = 6371000.0

    val DEFAULT_OFFICE_PRESETS = listOf(
        GeofenceLocationPreset(
            id = "preset_dubai_hq",
            name = "Dubai HQ - Main Campus",
            description = "Business Bay, Tower 1 - Level 14",
            latitude = 25.2048,
            longitude = 55.2708,
            radiusMeters = 100.0,
            isOfficeLocation = true
        ),
        GeofenceLocationPreset(
            id = "preset_showroom",
            name = "Sheikh Zayed Showroom",
            description = "Flagship Retail & Experience Center",
            latitude = 25.1972,
            longitude = 55.2744,
            radiusMeters = 80.0,
            isOfficeLocation = true
        ),
        GeofenceLocationPreset(
            id = "preset_logistics",
            name = "Jebel Ali Logistics Hub",
            description = "Distribution & Warehousing Complex",
            latitude = 24.9857,
            longitude = 55.0273,
            radiusMeters = 250.0,
            isOfficeLocation = true
        ),
        GeofenceLocationPreset(
            id = "preset_abu_dhabi",
            name = "Abu Dhabi Regional Branch",
            description = "Corniche Office Center - Floor 4",
            latitude = 24.4539,
            longitude = 54.3773,
            radiusMeters = 150.0,
            isOfficeLocation = true
        )
    )

    data class MockLocationPoint(
        val name: String,
        val description: String,
        val latitudeOffsetMeters: Double,
        val longitudeOffsetMeters: Double,
        val isInsideExpected: Boolean
    )

    val SIMULATION_SCENARIOS = listOf(
        MockLocationPoint("At Office Desk", "Inside building, 12m from beacon", 8.0, 8.0, true),
        MockLocationPoint("Main Entrance / Gate", "At reception lobby, 35m away", 25.0, 25.0, true),
        MockLocationPoint("Outside Perimeter (Parking)", "Visitor parking lot, 125m away", 90.0, 90.0, false),
        MockLocationPoint("Offsite / Remote Meeting", "Client location, 2.4 km away", 1800.0, 1600.0, false)
    )

    /**
     * Calculates the great-circle distance between two points on the Earth using the Haversine formula.
     * Returns distance in meters.
     */
    fun calculateDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val originLatRad = Math.toRadians(lat1)
        val destLatRad = Math.toRadians(lat2)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLon / 2) * sin(dLon / 2) * cos(originLatRad) * cos(destLatRad)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Calculates the compass bearing from point 1 to point 2 (in degrees, 0..360).
     */
    fun calculateBearingDegrees(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLon = Math.toRadians(lon2 - lon1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)

        val y = sin(dLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)

        val bearingRad = atan2(y, x)
        return (Math.toDegrees(bearingRad) + 360.0) % 360.0
    }

    /**
     * Evaluates whether a user's location falls within the geofence radius.
     */
    fun evaluateGeofence(
        userLat: Double,
        userLon: Double,
        officeLat: Double,
        officeLon: Double,
        radiusMeters: Double,
        accuracyMeters: Double = 6.0,
        zoneName: String = "Office Zone",
        zoneId: String = "zone-hq-1",
        isMock: Boolean = false
    ): GeofenceZone {
        val distance = calculateDistanceMeters(userLat, userLon, officeLat, officeLon)
        val isInside = distance <= radiusMeters

        val statusText = if (isInside) {
            "$zoneName (Inside ${radiusMeters.roundToInt()}m perimeter • ${formatDistance(distance)})"
        } else {
            "Outside Perimeter (${formatDistance(distance - radiusMeters)} beyond boundary)"
        }

        return GeofenceZone(
            id = zoneId,
            name = zoneName,
            latitude = officeLat,
            longitude = officeLon,
            radiusMeters = radiusMeters,
            isInside = isInside,
            distanceMeters = distance,
            accuracyMeters = accuracyMeters,
            locationStatusText = statusText,
            userLatitude = userLat,
            userLongitude = userLon,
            isMockLocation = isMock
        )
    }

    fun formatDistance(meters: Double): String {
        return if (meters < 1000) {
            "${meters.roundToInt()}m"
        } else {
            String.format(java.util.Locale.US, "%.1f km", meters / 1000.0)
        }
    }

    /**
     * Helper to offset a coordinate by delta meters for simulation testing.
     */
    fun offsetCoordinates(
        baseLat: Double,
        baseLon: Double,
        northMeters: Double,
        eastMeters: Double
    ): Pair<Double, Double> {
        val latOffset = (northMeters / EARTH_RADIUS_METERS) * (180.0 / Math.PI)
        val lonOffset = (eastMeters / (EARTH_RADIUS_METERS * cos(Math.toRadians(baseLat)))) * (180.0 / Math.PI)
        return Pair(baseLat + latOffset, baseLon + lonOffset)
    }
}
