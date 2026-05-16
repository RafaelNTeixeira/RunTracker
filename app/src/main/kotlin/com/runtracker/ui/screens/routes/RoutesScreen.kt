package com.runtracker.ui.screens.routes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtracker.data.db.entity.RouteEntity
import com.runtracker.ui.screens.logrun.RunField
import com.runtracker.ui.theme.*
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlin.math.*

@Composable
fun RoutesScreen(vm: RoutesViewModel = hiltViewModel()) {
    val routes by vm.routes.collectAsStateWithLifecycle()
    val waypoints = remember { mutableStateListOf<GeoPoint>() }
    var showSaveDialog by remember { mutableStateOf(false) }

    val distance = remember(waypoints.size) { calcDistanceKm(waypoints) }

    Column(Modifier.fillMaxSize()) {
        // Header
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("ROUTE PLANNER", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text("Tap the map to add waypoints", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }

        // Controls bar
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(color = GreenDim, shape = RoundedCornerShape(8.dp)) {
                Text("📍 ${waypoints.size} pts  •  ${"%.2f".format(distance)} km",
                    color = GreenPrimary, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { if (waypoints.isNotEmpty()) waypoints.removeLast() },
                contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text("↩ Undo", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = { waypoints.clear() },
                contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text("Clear", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = { if (waypoints.size >= 2) showSaveDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = GreenSecondary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) { Text("Save", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        }

        Spacer(Modifier.height(8.dp))

        // Map
        OsmMap(
            waypoints = waypoints,
            onTap = { gp -> waypoints.add(gp) },
            modifier = Modifier.fillMaxWidth().height(320.dp).padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Saved routes
        Text("SAVED ROUTES", style = MaterialTheme.typography.labelSmall, color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))

        if (routes.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No saved routes yet", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(routes, key = { it.id }) { route ->
                    RouteCard(route = route, onDelete = { vm.delete(route) },
                        onLoad = {
                            waypoints.clear()
                            parseWaypoints(route.waypoints).forEach { waypoints.add(it) }
                        })
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveRouteDialog(
            distanceKm = distance,
            onDismiss = { showSaveDialog = false },
            onSave = { name, desc ->
                val wp = waypoints.joinToString(";") { "${it.latitude},${it.longitude}" }
                vm.saveRoute(name, desc, distance, wp)
                waypoints.clear()
                showSaveDialog = false
            }
        )
    }
}

@Composable
private fun OsmMap(waypoints: List<GeoPoint>, onTap: (GeoPoint) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(14.0)
            controller.setCenter(GeoPoint(41.1579, -8.6291)) // Porto default
        }
    }

    // Lifecycle
    DisposableEffect(lifecycle) {
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE  -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(obs)
        onDispose { lifecycle.removeObserver(obs); mapView.onDetach() }
    }

    // Tap overlay
    DisposableEffect(onTap) {
        val overlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean { onTap(p); return true }
            override fun longPressHelper(p: GeoPoint) = false
        })
        mapView.overlays.add(0, overlay)
        onDispose { mapView.overlays.remove(overlay) }
    }

    // Draw waypoints & polyline
    LaunchedEffect(waypoints.size) {
        mapView.overlays.removeAll(mapView.overlays.filterIsInstance<Marker>())
        mapView.overlays.removeAll(mapView.overlays.filterIsInstance<Polyline>())

        waypoints.forEach { gp ->
            val marker = Marker(mapView).apply {
                position = gp
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            }
            mapView.overlays.add(marker)
        }

        if (waypoints.size > 1) {
            val line = Polyline().apply {
                setPoints(waypoints)
                outlinePaint.color = android.graphics.Color.parseColor("#39D353")
                outlinePaint.strokeWidth = 8f
            }
            mapView.overlays.add(line)
        }
        mapView.invalidate()
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}

@Composable
private fun RouteCard(route: RouteEntity, onDelete: () -> Unit, onLoad: () -> Unit) {
    Surface(color = DarkSurface, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🗺️", fontSize = 24.sp, modifier = Modifier.padding(end = 10.dp))
            Column(Modifier.weight(1f)) {
                Text(route.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("%.2f km".format(route.distanceKm) + if (route.description.isNotBlank()) " · ${route.description}" else "",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            TextButton(onClick = onLoad, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text("Load", color = GreenPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedAccent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun SaveRouteDialog(distanceKm: Double, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Save Route", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Distance: ${"%.2f".format(distanceKm)} km", color = GreenPrimary, fontWeight = FontWeight.SemiBold)
                RunField("Route Name", name, { name = it }, placeholder = "My 5K Park Loop")
                RunField("Description (optional)", desc, { desc = it }, placeholder = "Notes about the route…")
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, desc) },
                colors = ButtonDefaults.buttonColors(containerColor = GreenSecondary)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}

private fun calcDistanceKm(points: List<GeoPoint>): Double {
    var total = 0.0
    for (i in 1 until points.size) {
        val a = points[i - 1]; val b = points[i]
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val x = sin(dLat / 2).pow(2) + cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(dLon / 2).pow(2)
        total += 6371 * 2 * atan2(sqrt(x), sqrt(1 - x))
    }
    return total
}

private fun parseWaypoints(str: String): List<GeoPoint> = try {
    str.split(";").mapNotNull { part ->
        val (lat, lng) = part.split(",").map { it.toDouble() }
        GeoPoint(lat, lng)
    }
} catch (e: Exception) { emptyList() }


