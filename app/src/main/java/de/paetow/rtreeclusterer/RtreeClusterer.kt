package de.paetow.rtreeclusterer

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.github.davidmoten.rtree2.Entry
import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.Geometries
import com.github.davidmoten.rtree2.geometry.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.bonuspack.clustering.StaticCluster
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.*


class RtreeClusterer(ctx: Context, val coroutineScope: LifecycleCoroutineScope): RadiusMarkerClusterer(ctx) {

    private var tree: RTree<Marker, Point> =  RTree.star().maxChildren(4).create()
    private var currentCluster = arrayListOf<StaticCluster>()

    override fun add(marker: Marker?) {
        if (marker == null) return
        tree = tree.add(marker, marker.position.toPoint())
        super.add(marker)
    }

    override fun clusterer(mapView: MapView?): ArrayList<StaticCluster> {
        if (mapView == null) return arrayListOf()
        if (mapView.zoomLevelDouble > mMaxClusteringZoomLevel) return arrayListOf()

        //Todo: find a way to clone the tree
        //Todo: debounce clustering
        convertRadiusToMeters(mapView)

        // tree.visualize(1024, 1024).createImage()

        coroutineScope.launch(Dispatchers.IO) {
            val newCluster = arrayListOf<StaticCluster>()
            val currentMarkerIterable = tree.search(Geometries.rectangleGeographic(mapView.boundingBox.lonWest, mapView.boundingBox.lonEast, mapView.boundingBox.latNorth, mapView.boundingBox.latSouth))
            while (!currentMarkerIterable.iterator().hasNext()) {
                currentMarkerIterable.firstOrNull()?.let { centerMarker ->
                    val cluster = StaticCluster(centerMarker.asGeoPoint())  //the first item in the current result set will be the center of the new marker
                    cluster.add(centerMarker.value())
                    tree.delete(centerMarker)

                    val currentClusterItems = tree.search(centerMarker.geometry(), mRadiusInMeters)
                    currentClusterItems.forEach {  //add all markers in the neighbourhood of the center marker
                        cluster.add(it.value())
                        tree.delete(it)           //and remove them form the boundingBox search set
                    }
                    newCluster.add(cluster)
                }
            }
            currentCluster = newCluster
        }
        return currentCluster
    }

    private fun Entry<Marker, Point>.asGeoPoint() = geometry().let { GeoPoint(it.x(), it.y()) }
    private fun GeoPoint.toPoint() = Geometries.point(latitude, longitude)


    private fun convertRadiusToMeters(mapView: MapView) {
        val mScreenRect = mapView.getIntrinsicScreenRect(null)
        val screenWidth = mScreenRect.right - mScreenRect.left
        val screenHeight = mScreenRect.bottom - mScreenRect.top
        val bb = mapView.boundingBox
        val diagonalInMeters = bb.diagonalLengthInMeters
        val diagonalInPixels = Math.sqrt((screenWidth * screenWidth + screenHeight * screenHeight).toDouble())
        val metersInPixel = diagonalInMeters / diagonalInPixels
        mRadiusInMeters = mRadiusInPixels * metersInPixel
    }
}