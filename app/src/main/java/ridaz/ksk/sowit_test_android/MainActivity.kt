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
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.turf.TurfMeasurement
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import ridaz.ksk.sowit_test_android.adapter.PolygonSpinnerAdapter
import ridaz.ksk.sowit_test_android.model.MyPolygon
import ridaz.ksk.sowit_test_android.model.Points
import ridaz.ksk.sowit_test_android.viewmodel.PolygonViewModel


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnMapReadyCallback, MapboxMap.OnMapClickListener,
    CustomSpinner.OnSpinnerEventsListener {

    private var mapboxMap: MapboxMap? = null

    private var lineManager: LineManager? = null
    private var lineOptions: LineOptions? = null

    private var fillManager: FillManager? = null
    private var fillOptions: FillOptions? = null

    private var symbolManager: SymbolManager? = null
    private var symbolManagerText: SymbolManager? = null
    private var symbolOptions: SymbolOptions? = null
    private var symbolOptionsText: SymbolOptions? = null


    private val MAKI_ICON_CIRCLE = "fire-station-15"

    private var mesPoints = ArrayList<LatLng>()
    private val outPoints = ArrayList<List<LatLng>>()

    private var adapter: PolygonSpinnerAdapter? = null

    private lateinit var polygonViewModel: PolygonViewModel

    private var p: Points? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        polygonViewModel = ViewModelProvider(this).get(PolygonViewModel::class.java)
        setContentView(R.layout.activity_main)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)



        spinner!!.setSpinnerEventsListener(this)



        adapter = PolygonSpinnerAdapter(this)
        spinner!!.adapter = adapter


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
        mapboxMap.setStyle(Style.SATELLITE_STREETS) { style: Style ->

            fillManager = FillManager(mapView, mapboxMap, style)
            lineManager = LineManager(mapView, mapboxMap, style)
            symbolManager = SymbolManager(mapView, mapboxMap, style)
            symbolManagerText = SymbolManager(mapView, mapboxMap, style)

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

            val textFont = arrayOf("Open Sans Bold", "Arial Unicode MS Bold")
            symbolOptionsText = SymbolOptions()
                .withTextColor("#FFFFFF")
                .withTextFont(textFont)
                .withDraggable(false) as SymbolOptions



            mapboxMap.addOnMapClickListener(this)

            spinner!!.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {

                    goToMyPolygon(position)

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            })




            fab.setOnClickListener {

                lineManager?.deleteAll()
                fillManager?.deleteAll()
                symbolManager?.deleteAll()
                symbolManagerText?.deleteAll()

                mesPoints.clear()
                outPoints.clear()
            }

            fab2.setOnClickListener {
                val selectedItemPosition = spinner.selectedItemPosition
                goToMyPolygon(selectedItemPosition)
            }
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


            } else {
                mesPoints.add(point)
                lineOptions!!.withLatLngs(mesPoints)
                lineManager!!.create(lineOptions)

                symbolOptions!!.withLatLng(point)
                symbolManager!!.create(symbolOptions)

            }
        }
        return true
    }

    fun nearOfFirstPoint(point0: LatLng, pointEnd: LatLng): Boolean {

        var near = false

        val currentCameraPosition = mapboxMap?.cameraPosition
        val currentZoom = currentCameraPosition?.zoom


        Log.d("here", "nearOfFirstPoint zoom: $currentZoom")


        val dm = TurfMeasurement.distance(
            Point.fromLngLat(point0.longitude, point0.latitude),
            Point.fromLngLat(pointEnd.longitude, pointEnd.latitude),
            "metres"
        )

        if (currentZoom?.compareTo(16.0)!! >= 0) {
            if (dm < 5) near = true
        }
        if (currentZoom.compareTo(16.0) <= 0 && currentZoom.compareTo(15.5) >= 0) {
            if (dm < 10) near = true
        }
        if (currentZoom.compareTo(15.5) <= 0 && currentZoom.compareTo(15.0) >= 0) {
            if (dm < 20) near = true
        }

        if (currentZoom.compareTo(15.0) <= 0 && currentZoom.compareTo(14.5) >= 0) {
            if (dm < 50) near = true
        }
        if (currentZoom.compareTo(14.5) <= 0 && currentZoom.compareTo(14.0) >= 0) {
            if (dm < 80) near = true
        }

        if (currentZoom.compareTo(14.0) <= 0 && currentZoom.compareTo(13.5) >= 0) {
            if (dm < 100) near = true
        }

        if (currentZoom.compareTo(13.5) <= 0 && currentZoom.compareTo(13.0) >= 0) {
            if (dm < 150) near = true
        }

        if (currentZoom.compareTo(13.0) <= 0 && currentZoom.compareTo(12.5) >= 0) {
            if (dm < 300) near = true
        }
        if (currentZoom.compareTo(12.5) <= 0 && currentZoom.compareTo(12.0) >= 0) {
            if (dm < 600) near = true
        }
        if (currentZoom.compareTo(12.0) <= 0 && currentZoom.compareTo(11.5) >= 0) {
            if (dm < 600) near = true
        }

        if (currentZoom.compareTo(11.5) <= 0 && currentZoom.compareTo(11.0) >= 0) {
            if (dm < 800) near = true
        }
        if (currentZoom.compareTo(11.0) <= 0 && currentZoom.compareTo(10.5) >= 0) {
            if (dm < 1200) near = true
        }
        if (currentZoom.compareTo(10.5) <= 0 && currentZoom.compareTo(10.0) >= 0) {
            if (dm < 1800) near = true
        }
        if (currentZoom.compareTo(10.0) <= 0 && currentZoom.compareTo(9.0) >= 0) {
            if (dm < 3000) near = true
        }
        return near
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

                if (name.isNotEmpty()) {


                    /*********************************************************************************************/

                    p = Points(mesPoints)


                    val mp = MyPolygon(name, p!!)

                    Log.d("aly", "ana hna mp= : $mp")


                    polygonViewModel.insertPolygonInRoom(mp)
                    /*********************************************************************************************/


                    lineManager?.deleteAll()
                    fillManager?.deleteAll()
                    symbolManager?.deleteAll()
                    symbolManagerText?.deleteAll()




                    Toast.makeText(applicationContext, "Saved", Toast.LENGTH_SHORT).show()
                    alertDialog?.dismiss()
                } else Toast.makeText(applicationContext, "Name is messing !!", Toast.LENGTH_SHORT)
                    .show()

            }
            .setNegativeButton("No") { _, _ ->


                lineManager?.deleteAll()
                fillManager?.deleteAll()
                symbolManager?.deleteAll()
                symbolManagerText?.deleteAll()

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


    /////////////////////////////////////////////////////////////

    fun goToMyPolygon(p: Int) {
        lineManager?.deleteAll()
        fillManager?.deleteAll()
        symbolManager?.deleteAll()
        symbolManagerText?.deleteAll()

        mesPoints.clear()
        outPoints.clear()

        /////////////////////////////////////////////////////////////

        val polygon = adapter!!.getItem(p)
        mesPoints.addAll(polygon.points.points)
        outPoints.add(mesPoints)

        /////////////////////////////////////////////////////////////

        for (lt in mesPoints) {
            symbolOptions!!.withLatLng(lt)
            symbolManager!!.create(symbolOptions)
        }

        /////////////////////////////////////////////////////////////

        lineOptions!!.withLatLngs(mesPoints)
        lineManager!!.create(lineOptions)

        /////////////////////////////////////////////////////////////

        fillOptions!!.withLatLngs(outPoints)
        fillManager!!.create(fillOptions)

        /////////////////////////////////////////////////////////////

        symbolOptionsText?.withTextField(calculateAreaMyPolygon())
        symbolOptionsText!!.withLatLng(getPointInsideFromMyPolygon())
        symbolManagerText?.create(symbolOptionsText)

        /////////////////////////////////////////////////////////////

        moveCameraToMyPolygon()

    }

    private fun moveCameraToMyPolygon() {
        val positionCam = CameraPosition.Builder()
            .target(mesPoints[0])
            .zoom(14.5)
            .tilt(20.0)
            .build()
        mapboxMap?.animateCamera(
            CameraUpdateFactory.newCameraPosition(positionCam),
            2000
        )

    }

    private fun calculateAreaMyPolygon(): String {
        val data = fillOptions!!.geometry
        val area = data?.let { TurfMeasurement.area(it) }
        val areaHk = area?.div(10000)
        val s1 = area.toString()
        val s2 = s1.split('.')
        val ds = s2[0]
        val fs = s2[1].subSequence(0, 2)
        val Sarea = ds + "." + fs + " MS"

        Log.d("here", "goToMyPolygon: area = $area M*2")
        Log.d("here", "goToMyPolygon: area = $areaHk Hk")
        Log.d("here", "goToMyPolygon: area = $Sarea")

        return Sarea
    }

    private fun getPointInsideFromMyPolygon(): LatLng {
        val a = mesPoints[0]
        val b = mesPoints[1]
        val c = mesPoints[2]

        val ab: LatLng
        val abc: LatLng

        var latab: Double = a.latitude + ((b.latitude - a.latitude) / 2)
        val lngab: Double = a.longitude + ((b.longitude - a.longitude) / 2)

        ab = LatLng(latab, lngab)

        val latabc: Double = ab.latitude + ((c.latitude - ab.latitude) / 2)
        val lngabc: Double = ab.longitude + ((c.longitude - ab.longitude) / 2)

        abc = LatLng(latabc, lngabc)

        return abc
    }

}