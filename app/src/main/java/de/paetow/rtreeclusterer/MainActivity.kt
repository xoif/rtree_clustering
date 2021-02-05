package de.paetow.rtreeclusterer

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import org.osmdroid.config.Configuration
import androidx.preference.PreferenceManager
import com.eazypermissions.common.model.PermissionResult
import com.eazypermissions.coroutinespermission.PermissionManager
import de.paetow.rtreeclusterer.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView( binding.root)

        binding.map.setTileSource(TileSourceFactory.MAPNIK)
        binding.map.controller.animateTo(GeoPoint(48.144667, 11.548861))
        binding.map.controller.zoomTo(5.0)

        lifecycleScope.launch {
            val permissionResult = PermissionManager.requestPermissions(           //Suspends the coroutine
                    this@MainActivity,
                    1,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            //Resume coroutine once result is ready
            when(permissionResult) {
                is PermissionResult.PermissionGranted -> {

                }
                is PermissionResult.PermissionDenied -> {
                    //Add your logic to handle permission denial
                }
                is PermissionResult.PermissionDeniedPermanently -> {
                    //Add your logic here if user denied permission(s) permanently.
                    //Ideally you should ask user to manually go to settings and enable permission(s)
                }
                is PermissionResult.ShowRational -> {
                    //If user denied permission frequently then she/he is not clear about why you are asking this permission.
                    //This is your chance to explain them why you need permission.
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()

        //val markers = RadiusMarkerClusterer(this)
        val markers = RtreeClusterer(this, lifecycleScope)
        markers.setMaxClusteringZoomLevel(17)

        val clusterIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_fiber_manual_record_24, null)?.toBitmap() ?: return
        markers.setIcon(clusterIcon)

        markers.mAnchorU = Marker.ANCHOR_CENTER
        markers.mAnchorV =  Marker.ANCHOR_CENTER
        markers.mTextAnchorU = Marker.ANCHOR_CENTER;
        markers.mTextAnchorV = Marker.ANCHOR_CENTER
        markers.textPaint.textSize = 11 * resources.displayMetrics.density

        binding.map.overlays.add(markers)

        val icon = ResourcesCompat.getDrawable(resources, R.drawable.marker_default_focused_base, null)

        for (i in 0 .. 10000) {
            markers.add(
                    Marker(binding.map).apply {
                        setIcon(icon)
                        val random1 = Random.nextDouble(0.000000, 0.099999)
                        val random2 = Random.nextDouble(0.000000, 0.099999)
                        position = GeoPoint(48.14 + random1, 11.548861 + random2)
                    }
            )
        }
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }
}