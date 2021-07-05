package ridaz.ksk.sowit_test_android.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import ridaz.ksk.sowit_test_android.R
import ridaz.ksk.sowit_test_android.model.MyPolygon

class PolygonSpinnerAdapter(private var context: Context?) :
    BaseAdapter()  {

    private var myPolygonList = emptyList<MyPolygon>()


    fun setData(myPolygonList: List<MyPolygon>){
        this.myPolygonList = myPolygonList
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return if (myPolygonList != null) myPolygonList!!.size else 0
    }

    override fun getItem(i: Int): MyPolygon {
        return myPolygonList[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
        val rootView = LayoutInflater.from(context).inflate(R.layout.polygone_spinner_item, viewGroup, false)
        val txtName = rootView.findViewById<TextView>(R.id.namePolygonTv)
        txtName.text = myPolygonList?.get(i)?.name
        return rootView
    }
}