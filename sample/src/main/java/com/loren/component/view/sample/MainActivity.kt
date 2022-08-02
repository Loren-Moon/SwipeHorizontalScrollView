package com.loren.component.view.sample

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.loren.component.view.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val mBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var stockAdapter: StockAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
    }

    fun hideLeft(view: View) {
        startActivity(Intent(this, HideLeftActivity::class.java))
    }

    fun defaultShowLeft(view: View) {
        startActivity(Intent(this, DefaultShowLeftActivity::class.java))
    }

    fun needFixItemPosition(view: View) {
        startActivity(Intent(this, NeedFixItemPositionActivity::class.java))
    }

}

class StockAdapter(private val data: List<StockModel>?) : RecyclerView.Adapter<StockAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTv: TextView = itemView.findViewById(R.id.tvName)
        val nameTv1: TextView = itemView.findViewById(R.id.tvName1)
        val nameTv2: TextView = itemView.findViewById(R.id.tvName2)
        val nameTv3: TextView = itemView.findViewById(R.id.tvName3)
        val nameTv4: TextView = itemView.findViewById(R.id.tvName4)
        val nameTv5: TextView = itemView.findViewById(R.id.tvName5)
        val nameTv6: TextView = itemView.findViewById(R.id.tvName6)

        init {
            itemView.setOnClickListener {
                Log.v("Loren", "click--------->")
                Snackbar.make(itemView, "click", Snackbar.LENGTH_SHORT).show()
            }
            itemView.setOnLongClickListener {
                Log.v("Loren", "long click--------->")
                Snackbar.make(itemView, "long click", Snackbar.LENGTH_SHORT).show()
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_stock, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        data?.let {
            holder.itemView.setBackgroundColor(if (position % 2 == 0) Color.rgb(245, 245, 245) else Color.WHITE)
            val model = it[position]
            holder.nameTv.text = model.name
            holder.nameTv1.text = model.params[0]
            holder.nameTv2.text = model.params[1]
            holder.nameTv3.text = model.params[2]
            holder.nameTv4.text = model.params[3]
            holder.nameTv5.text = model.params[4]
            holder.nameTv6.text = model.params[5]
        }
    }

    override fun getItemCount() = data?.size ?: 0
}

data class StockModel(
    val name: String,
    val params: List<String>
)