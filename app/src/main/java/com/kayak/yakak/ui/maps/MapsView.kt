package com.kayak.yakak.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/*
* Pour les coordonnes faire une data class pour longitude et latitude
* */


@SuppressLint("ClickableViewAccessibility")
@Composable
fun MapsView(
    modifier: Modifier = Modifier,
    onMapReady: (MapView, MyLocationNewOverlay) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        }
    )

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_pref", 0))
        Configuration.getInstance().userAgentValue = context.packageName

        if (!hasLocationPermission) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(48.8583, 2.2945))
            setOnTouchListener { v, event ->
                if (this.overlays.filterIsInstance<MyLocationNewOverlay>().firstOrNull()?.isFollowLocationEnabled == true) {
                    this.overlays.filterIsInstance<MyLocationNewOverlay>().firstOrNull()?.disableFollowLocation()
                }
                false
            }
        }
    }

    val locationOverlay = remember(mapView) {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            if (hasLocationPermission) {
                enableMyLocation()
            }
        }
    }

    LaunchedEffect(locationOverlay, mapView) {
        onMapReady(mapView, locationOverlay)
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            locationOverlay.enableMyLocation()
            locationOverlay.enableFollowLocation()
            if (!mapView.overlays.contains(locationOverlay)) {
                mapView.overlays.add(locationOverlay)
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView.onResume()
                    if (hasLocationPermission) {
                        locationOverlay.enableMyLocation()
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    mapView.onPause()
                    locationOverlay.disableMyLocation()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = modifier.fillMaxSize().clipToBounds()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        /*if (hasLocationPermission) {
            FloatingActionButton(
                onClick = {
                    locationOverlay.enableFollowLocation()
                    mapView.controller.animateTo(locationOverlay.myLocation)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My Location"
                )
            }
        }*/
    }
}