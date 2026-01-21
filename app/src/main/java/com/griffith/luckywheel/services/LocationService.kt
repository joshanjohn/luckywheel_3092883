package com.griffith.luckywheel.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

// Service to handle location fetching and geocoding
class LocationService(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    companion object {
        private const val TAG = "LocationService"
    }

    // Check if location permissions are granted
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Get current location and convert to city/country
    // Returns Triple(city, countryName, countryCode)
    suspend fun getCurrentLocation(): Result<Triple<String, String, String>> = withContext(Dispatchers.IO) {
        try {
            if (!hasLocationPermission()) {
                Log.w(TAG, "Location permission not granted")
                return@withContext Result.failure(Exception("Location permission not granted"))
            }

            // Get current location
            Log.d(TAG, "Fetching current location...")
            val location = suspendCancellableCoroutine { continuation ->
                val cancellationTokenSource = CancellationTokenSource()
                
                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }

                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    Log.d(TAG, "Location received: lat=${location?.latitude}, lon=${location?.longitude}")
                    continuation.resume(location)
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to get location", exception)
                    continuation.resume(null)
                }
            }

            if (location == null) {
                Log.w(TAG, "Location is null")
                return@withContext Result.failure(Exception("Unable to get location"))
            }

            // Check if Geocoder is available
            if (!Geocoder.isPresent()) {
                Log.e(TAG, "Geocoder is not present on this device")
                return@withContext Result.failure(Exception("Geocoder not available"))
            }

            // Convert coordinates to city/country using Geocoder
            Log.d(TAG, "Starting geocoding for: ${location.latitude}, ${location.longitude}")
            val geocoder = Geocoder(context, Locale.getDefault())
            
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For Android 13+ use the new async API
                suspendCancellableCoroutine { continuation ->
                    try {
                        geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        ) { addresses ->
                            Log.d(TAG, "Geocoder returned ${addresses?.size ?: 0} addresses")
                            continuation.resume(addresses)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Geocoder exception", e)
                        continuation.resume(null)
                    }
                }
            } else {
                // For older versions use the deprecated sync API
                try {
                    @Suppress("DEPRECATION")
                    val result = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    Log.d(TAG, "Geocoder returned ${result?.size ?: 0} addresses")
                    result
                } catch (e: Exception) {
                    Log.e(TAG, "Geocoder exception", e)
                    null
                }
            }

            if (addresses.isNullOrEmpty()) {
                Log.w(TAG, "Geocoder returned no addresses")
                return@withContext Result.failure(Exception("Unable to geocode location"))
            }

            val address = addresses[0]
            
            // Log all available address components for debugging
            Log.d(TAG, "Address components:")
            Log.d(TAG, "  locality: ${address.locality}")
            Log.d(TAG, "  subAdminArea: ${address.subAdminArea}")
            Log.d(TAG, "  adminArea: ${address.adminArea}")
            Log.d(TAG, "  subLocality: ${address.subLocality}")
            Log.d(TAG, "  countryName: ${address.countryName}")
            Log.d(TAG, "  countryCode: ${address.countryCode}")
            
            // Try multiple fields to get city name
            val city = address.locality 
                ?: address.subLocality
                ?: address.subAdminArea 
                ?: address.adminArea 
                ?: "Unknown"
            
            val country = address.countryName ?: "Unknown"
            val countryCode = address.countryCode ?: "XX" // XX for unknown

            Log.d(TAG, "Final location: city=$city, country=$country, code=$countryCode")
            Result.success(Triple(city, country, countryCode))
        } catch (e: Exception) {
            Log.e(TAG, "Exception in getCurrentLocation", e)
            Result.failure(e)
        }
    }

    // Get location with fallback to "Unknown" if fails
    suspend fun getLocationOrDefault(): Triple<String, String, String> {
        return getCurrentLocation().getOrElse { exception ->
            Log.w(TAG, "Using default location due to: ${exception.message}")
            Triple("Unknown", "Unknown", "XX")
        }
    }
}
