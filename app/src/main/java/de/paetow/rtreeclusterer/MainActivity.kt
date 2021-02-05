package de.paetow.rtreeclusterer

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import org.osmdroid.config.Configuration
import androidx.preference.PreferenceManager
import com.eazypermissions.common.model.PermissionResult
import com.eazypermissions.coroutinespermission.PermissionManager
import de.paetow.rtreeclusterer.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView( binding.root)

        binding.map.setTileSource(TileSourceFactory.MAPNIK)

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
                    //Add your logic here after user grants permission(s)
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
        binding.map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }
}