package com.kayak.yakak.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.kayak.yakak.R
import com.kayak.yakak.data.Location
import com.kayak.yakak.data.Task
import com.kayak.yakak.utils.setIconFromVector
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@SuppressLint("ClickableViewAccessibility")
@Composable
fun MapsView(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    mapsVM: MapsVM,
    onMapReady: (MapView, MyLocationNewOverlay) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val uiState by mapsVM.uiState.collectAsState()
    var isMapLoading by remember { mutableStateOf(true) }

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
            mapsVM.onEvent(MapsEvent.OnPermissionResult(hasLocationPermission))
        }
    )


    val mapPrefs = remember { context.getSharedPreferences("osm_pref", 0) }
    val lastLat = remember { mapPrefs.getFloat("last_lat", 48.8583f).toDouble() }
    val lastLon = remember { mapPrefs.getFloat("last_lon", 2.2945f).toDouble() }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, mapPrefs)
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
            setBuiltInZoomControls(false)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(lastLat, lastLon))
            setOnTouchListener { _, _ ->
                this.overlays.filterIsInstance<MyLocationNewOverlay>().firstOrNull()?.disableFollowLocation()
                false
            }
            addOnFirstLayoutListener { _, _, _, _, _ ->
                isMapLoading = false
            }
        }
    }

    fun createDotIcon(color: Int, sizeDp: Int = 16): GradientDrawable {
        val sizePx = (sizeDp * context.resources.displayMetrics.density).toInt()
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            setSize(sizePx, sizePx)
            setStroke(2, android.graphics.Color.WHITE)
        }
    }


    LaunchedEffect(mapView) {
        val eventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false
            override fun longPressHelper(p: GeoPoint?): Boolean {
                p?.let {
                    val task  = Task(
                        location = Location(it.latitude, it.longitude)
                    )
                    mapsVM.onEvent(MapsEvent.OnNewTask(task))
                    navController.navigate("edit-task/${task.id}")
                }
                return true
            }
        }
        mapView.overlays.add(0, MapEventsOverlay(eventsReceiver))
    }

    val iconColor = colorScheme.primary.toArgb()
    val completedText = stringResource(R.string.map_completed)

    LaunchedEffect(uiState.pendingTasks, uiState.finishedTasks) {
        mapView.overlays.removeAll(mapView.overlays.filterIsInstance<Marker>())

        val allTasks = uiState.pendingTasks + uiState.finishedTasks
        allTasks.filter { it.location.latitude != 0.0 || it.location.longitude != 0.0 }.forEach { task ->
            val marker = Marker(mapView)
            marker.position = GeoPoint(task.location.latitude, task.location.longitude)
            marker.title = task.name
            marker.snippet = if (task.isCompleted) completedText else task.description
            
            if (task.isCompleted) {
                marker.icon = createDotIcon(android.graphics.Color.GRAY, 12)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                marker.alpha = 0.6f
            } else {
                marker.setIconFromVector(context, R.drawable.ic_material_pin, iconColor )
                marker.alpha
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            }

            marker.setOnMarkerClickListener { _, _ ->
                navController.navigate("edit-task/${task.id}")
                true
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    val locationOverlay = remember(mapView) {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            if (hasLocationPermission) enableMyLocation()
        }
    }

    LaunchedEffect(locationOverlay.myLocation) {
        locationOverlay.myLocation?.let { loc ->
            mapPrefs.edit()
                .putFloat("last_lat", loc.latitude.toFloat())
                .putFloat("last_lon", loc.longitude.toFloat())
                .apply()
            mapsVM.onLocationUpdate(loc.latitude, loc.longitude)
        }
    }

    LaunchedEffect(locationOverlay, mapView) { onMapReady(mapView, locationOverlay) }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            locationOverlay.enableMyLocation()
            locationOverlay.enableFollowLocation()
            if (!mapView.overlays.contains(locationOverlay)) mapView.overlays.add(locationOverlay)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView.onResume()
                    if (hasLocationPermission) locationOverlay.enableMyLocation()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    mapView.onPause()
                    locationOverlay.disableMyLocation()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = modifier.fillMaxSize().clipToBounds(), contentAlignment = Alignment.Center) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
        AnimatedVisibility(visible = isMapLoading, enter = fadeIn(), exit = fadeOut()) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}
