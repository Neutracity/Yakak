package com.kayak.yakak.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavHostController
import com.kayak.yakak.data.Location
import com.kayak.yakak.data.Task
import com.kayak.yakak.ui.tasklist.TaskEvent
import com.kayak.yakak.ui.tasklist.TaskListVM
import com.kayak.yakak.utils.ReminderReceiver
import kotlinx.coroutines.delay
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
    taskListVM: TaskListVM,
    onMapReady: (MapView, MyLocationNewOverlay) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val uiState by taskListVM.uiState.collectAsState()

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

    // fonction de clic long pour ajouter le repere.
    LaunchedEffect(mapView) {
        val eventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false

            override fun longPressHelper(p: GeoPoint?): Boolean {
                p?.let {
                    val newTask = Task(
                        name = "Nouveau repère",
                        location = Location(it.latitude, it.longitude)
                    )
                    taskListVM.onEvent(TaskEvent.NewTask(newTask))
                    navController.navigate("edit-task/${newTask.id}")
                }
                return true
            }
        }
        mapView.overlays.add(0, MapEventsOverlay(eventsReceiver))
    }

    //Mise à jour des marqueurs quand les task sont finies ou modifiées
    LaunchedEffect(uiState.pendingTasks, uiState.finishedTasks) {
        // Nettoyer les anciens marqueurs
        val overlaysToRemove = mapView.overlays.filterIsInstance<Marker>()
        mapView.overlays.removeAll(overlaysToRemove)

        val allTasks = uiState.pendingTasks + uiState.finishedTasks
        allTasks.filter { it.location.latitude != 0.0 || it.location.longitude != 0.0 }.forEach { task ->
            val marker = Marker(mapView)
            marker.position = GeoPoint(task.location.latitude, task.location.longitude)
            marker.title = task.name
            marker.snippet = if (task.isCompleted) "Terminée" else task.description
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            //Si la tâche est terminée, on grise le marqueur
            if (task.isCompleted) {
                val icon = ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.marker_default)?.mutate()
                icon?.setTint(android.graphics.Color.GRAY)
                marker.icon = icon
                marker.alpha = 0.5f //Transparence pour les tâches finies
            }

            marker.setOnMarkerClickListener { m, _ ->
                navController.navigate("edit-task/${task.id}")
                true
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate() // Rafraîchir la carte
    }

    val locationOverlay = remember(mapView) {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            if (hasLocationPermission) {
                enableMyLocation()
            }
        }
    }

    //Logique de proximité (Notification 300m)
    val notifiedTasks = remember { mutableSetOf<Int>() }

    LaunchedEffect(locationOverlay, uiState.pendingTasks) {
        while (true) {
            val myLocation = locationOverlay.myLocation
            if (myLocation != null) {
                uiState.pendingTasks.forEach { task ->
                    if (task.location.latitude != 0.0 && task.location.longitude != 0.0) {
                        val taskPos = GeoPoint(task.location.latitude, task.location.longitude)
                        val distance = myLocation.distanceToAsDouble(taskPos)

                        if (distance < 300.0) {
                            if (!notifiedTasks.contains(task.id)) {
                                // On déclenche la notification via le Receiver existant
                                val intent = Intent(context, ReminderReceiver::class.java).apply {
                                    putExtra("TASK_NAME", "À proximité : ${task.name}")
                                    putExtra("TASK_ID", task.id)
                                    putExtra("TASK_DESC", "Vous êtes à moins de 300m de cet objectif.")
                                }
                                context.sendBroadcast(intent)
                                notifiedTasks.add(task.id)
                            }
                        } else if (distance > 500.0) {
                            // On autorise à nouveau la notification si on s'éloigne (hystérésis)
                            notifiedTasks.remove(task.id)
                        }
                    }
                }
            }
            delay(5000) // Vérification toutes les 5 secondes pour économiser la batterie
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
    }
}
