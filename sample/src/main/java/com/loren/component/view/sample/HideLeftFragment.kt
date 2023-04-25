package com.loren.component.view.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.loren.component.view.sample.databinding.RecyclerHideLeftBinding
import com.loren.component.view.widget.HorizontalRecyclerView
import com.loren.component.view.widget.SwipeHorizontalScrollView

class HideLeftFragment : Fragment() {

    private val mBinding by lazy { RecyclerHideLeftBinding.inflate(layoutInflater) }
    private var itemAdapter: ItemAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = mutableListOf<List<StockModel>>()
        for (j in 0 until 4) {
            val list = mutableListOf<StockModel>()
            for (index in 0 until 5) {
                val childList = mutableListOf<String>()
                for (i in 0 until 10) {
                    childList.add("${index}行${i + 1}列")
                }
                list.add(StockModel("$index", childList))
            }
            data.add(list)
        }

        itemAdapter = ItemAdapter(data)
        mBinding.rv.adapter = itemAdapter
    }

}

class ItemAdapter(private val data: List<List<StockModel>>?) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rvStock: HorizontalRecyclerView = itemView.findViewById(R.id.rvStock)
        val swipeHorizontalView: SwipeHorizontalScrollView = itemView.findViewById(R.id.swipeHorizontalView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_hide_left, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = mutableListOf<StockModel>()
        for (index in 0 until 5) {
            val childList = mutableListOf<String>()
            for (i in 0 until 10) {
                childList.add("${index}行${i + 1}列")
            }
            list.add(StockModel("$index", childList))
        }

        val stockAdapter = StockAdapter(list)
        holder.rvStock.adapter = stockAdapter
        holder.rvStock.bindHeadScrollView(holder.swipeHorizontalView)
        holder.rvStock.setOnHorizontalRecyclerViewStateListener(object : HorizontalRecyclerView.OnHorizontalRecyclerViewStateListener {
            override fun extend() {
                Snackbar.make(holder.rvStock, "extend", Snackbar.LENGTH_SHORT).show()
            }

            override fun fold() {
                Snackbar.make(holder.rvStock, "fold", Snackbar.LENGTH_SHORT).show()
            }

        })
    }

    override fun getItemCount() = data?.size ?: 0
}