package ridaz.ksk.sowit_test_android


import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import ridaz.ksk.sowit_test_android.adapter.PolygonSpinnerAdapter
import ridaz.ksk.sowit_test_android.model.MyPolygon
import ridaz.ksk.sowit_test_android.model.Points
import ridaz.ksk.sowit_test_android.viewmodel.PolygonViewModel
import kotlin.collections.ArrayList

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnMapReadyCallback, MapboxMap.OnMapClickListener,
    CustomSpinner.OnSpinnerEventsListener {

    private var mapboxMap: MapboxMap? = null

    private var lineManager: LineManager? = null
    private var lineOptions: LineOptions? = null

    private var fillManager: FillManager? = null
    private var fillOptions: FillOptions? = null

    private var symbolManager: SymbolManager? = null
    private var symbolOptions: SymbolOptions? = null


    private val MAKI_ICON_CIRCLE = "fire-station-15"

    private var mesPoints = ArrayList<LatLng>()
    private val outPoints = ArrayList<List<LatLng>>()


    private var spinner: CustomSpinner? = null
    private var adapter: PolygonSpinnerAdapter? = null

    private lateinit var polygonViewModel: PolygonViewModel


    var point: Points? = null
    var p : Points? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        polygonViewModel = ViewModelProvider(this).get(PolygonViewModel::class.java)
        setContentView(R.layout.activity_main)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)


        spinner = findViewById(R.id.polygonSpinner) as CustomSpinner

        spinner!!.setSpinnerEventsListener(this)



        adapter = PolygonSpinnerAdapter(this)
        spinner!!.adapter = adapter


        val arr= ArrayList<LatLng>()
        arr.add(LatLng(58.87,75.68))
        arr.add(LatLng(58.87,75.68))
        arr.add(LatLng(58.87,75.68))
        arr.add(LatLng(58.87,75.68))

         point = Points(arr)

        val mypoly = MyPolygon("rida", point!!)


        polygonViewModel.insertPolygonInRoom(mypoly)


        polygonViewModel.readMyPolygonFromRoom.observe(this, {
            Log.d("aly", "onCreate: $it")
            if (it.isNotEmpty()) {
                mesPoints.clear()
                outPoints.clear()
                adapter!!.setData(it)

            }
        })


    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.SATELLITE) { style: Style ->

            fillManager = FillManager(mapView, mapboxMap, style)
            lineManager = LineManager(mapView, mapboxMap, style)
            symbolManager = SymbolManager(mapView, mapboxMap, style)

            fillOptions = FillOptions()
                .withFillColor("#e55e5e")
                .withFillOpacity(.5f) as FillOptions

            lineOptions = LineOptions()
                .withLineColor("#e55e5e")
                .withLineWidth(3f)

            symbolOptions = SymbolOptions()
                .withIconImage(MAKI_ICON_CIRCLE)
                .withIconColor("#e55e5e")
                .withIconSize(1.0f)
                .withDraggable(false) as SymbolOptions


            mapboxMap.addOnMapClickListener(this)

            spinner!!.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {

                    Log.d("herep", "onItemSelected: " + parent.getItemAtPosition(position))
                    Log.d("herep", "onItemSelected: " + adapter!!.getItem(position))


                    lineManager?.deleteAll()
                    fillManager?.deleteAll()
                    symbolManager?.deleteAll()

                    mesPoints.clear()
                    outPoints.clear()

                    val polygon = adapter!!.getItem(position)
                    mesPoints.addAll(polygon.points.points)
                    outPoints.add(mesPoints)

                    for (lt in mesPoints) {
                        symbolOptions!!.withLatLng(lt)
                        symbolManager!!.create(symbolOptions)
                    }



                    lineOptions!!.withLatLngs(mesPoints)
                    lineManager!!.create(lineOptions)


                    fillOptions!!.withLatLngs(outPoints)
                    fillManager!!.create(fillOptions)


/////////////////////////////////////////////////////////////////////
                    val positionCam = CameraPosition.Builder()
                        .target(mesPoints[0])
                        .zoom(14.0)
                        .tilt(20.0)
                        .build()
                    mapboxMap.animateCamera(
                        CameraUpdateFactory.newCameraPosition(positionCam),
                        2000
                    )


                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            })

        }
    }

    override fun onMapClick(point: LatLng): Boolean {

        Log.d("here", "coordinates: $point")

        if (mesPoints.isEmpty()) {
            mesPoints.add(point)
            symbolOptions!!.withLatLng(point)
            symbolManager!!.create(symbolOptions)
        } else {


            if (nearOfFirstPoint(mesPoints[0], point)) {


                mesPoints.add(mesPoints[0])
                lineOptions!!.withLatLngs(mesPoints)
                lineManager!!.create(lineOptions)


                outPoints.add(mesPoints)
                fillOptions?.withLatLngs(outPoints)
                fillManager!!.create(fillOptions)


                showDialogSave()


                val dk = getDistanceFromLatLonInKm(mesPoints[0], point)
                Log.d("here", "distanceKm: $dk km")
                Log.d("here", "distanceM: ${getDistanceFromLatLonInMeter(dk)} m")
                Log.d("here", "near : ${nearOfFirstPoint(mesPoints[0], point)} ")

            } else {
                mesPoints.add(point)
                lineOptions!!.withLatLngs(mesPoints)
                lineManager!!.create(lineOptions)

                symbolOptions!!.withLatLng(point)
                symbolManager!!.create(symbolOptions)

                val dk = getDistanceFromLatLonInKm(mesPoints[0], point)
                Log.d("here", "distanceKm: $dk km")
                Log.d("here", "distanceM: ${getDistanceFromLatLonInMeter(dk)} m")
                Log.d("here", "near : ${nearOfFirstPoint(mesPoints[0], point)} ")
            }
        }
        return true
    }

    fun nearOfFirstPoint(point0: LatLng, pointEnd: LatLng): Boolean {

        var near = false

        val currentCameraPosition = mapboxMap?.cameraPosition
        val currentZoom = currentCameraPosition?.zoom


        val dkm = getDistanceFromLatLonInKm(point0, pointEnd)
        val dm = getDistanceFromLatLonInMeter(dkm)
        if (currentZoom?.compareTo(13.0)!! >= 0) {

            if (dm < 60) near = true
        } else {
            if (currentZoom.compareTo(12.0)!! <= 0) {
                if (dm < 500) near = true
            }
        }

        return near
    }

    fun getDistanceFromLatLonInMeter(dkm: Double): Double = dkm * 1000


    fun getDistanceFromLatLonInKm(point0: LatLng, pointEnd: LatLng): Double {

        val lat1 = point0.latitude
        val lon1 = point0.longitude

        val lat2 = pointEnd.latitude
        val lon2 = pointEnd.longitude

        var R = 6371; // Radius of the earth in km
        var dLat = deg2rad(lat2 - lat1)  // deg2rad below
        var dLon = deg2rad(lon2 - lon1)
        var a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                    Math.sin(dLon / 2) * Math.sin(dLon / 2)

        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        var d = R * c; // Distance in km
        return d
    }

    fun deg2rad(deg: Double): Double {
        return deg * (Math.PI / 180)
    }


    fun showDialogSave() {


        val dialogBuilder =
            AlertDialog.Builder(this, R.style.Base_ThemeOverlay_MaterialComponents_Dialog_Alert)

        val view = layoutInflater.inflate(R.layout.dialog_save_polygon, null)
        var alertDialog: AlertDialog? = null



        dialogBuilder.setView(view)
            .setTitle("Save polygon")
            .setPositiveButton(
                "Save"
            ) { _, _ ->

                val nameEd = view.findViewById<EditText>(R.id.namePolygonEditT)
                val name = nameEd.text.toString()

                if (name != "") {


                    /*********************************************************************************************/

                    p = Points(mesPoints)


                    val mp =MyPolygon("name", p!!)

                    Log.d("aly", "ana hna mp= : $mp")


                    polygonViewModel.insertPolygonInRoom(mp)
                    /*********************************************************************************************/


                    lineManager?.deleteAll()
                    fillManager?.deleteAll()
                    symbolManager?.deleteAll()




                    Toast.makeText(applicationContext, "Saved", Toast.LENGTH_SHORT).show()
                    alertDialog?.dismiss()
                } else Toast.makeText(applicationContext, "Name is messing !!", Toast.LENGTH_SHORT)
                    .show()

            }
            .setNegativeButton("No") { _, _ ->


        lineManager?.deleteAll()
        fillManager?.deleteAll()
        symbolManager?.deleteAll()

        mesPoints.clear()
        outPoints.clear()


                Toast.makeText(applicationContext, "No", Toast.LENGTH_SHORT).show()
                alertDialog?.dismiss()
            }



        alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    /////////////////////////////

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }


    override fun onPopupWindowOpened(spinner: Spinner?) {
        spinner!!.background =
            ContextCompat.getDrawable(applicationContext, R.drawable.bg_spinner_fruit_up)

    }

    override fun onPopupWindowClosed(spinner: Spinner?) {
        spinner!!.background =
            ContextCompat.getDrawable(applicationContext, R.drawable.bg_spinner_fruit)
    }


}