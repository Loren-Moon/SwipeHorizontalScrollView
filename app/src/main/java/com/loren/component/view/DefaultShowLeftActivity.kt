package com.loren.component.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.loren.component.view.databinding.ActivityDefaultShowLeftBinding
import com.loren.component.view.widget.HorizontalRecyclerView

class DefaultShowLeftActivity : AppCompatActivity() {

    private val mBinding by lazy { ActivityDefaultShowLeftBinding.inflate(layoutInflater) }
    private var stockAdapter: StockAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        val list = mutableListOf<StockModel>()
        for (index in 0 until 500) {
            val childList = mutableListOf<String>()
            for (i in 0 until 10) {
                childList.add("${index}行${i + 1}列")
            }
            list.add(StockModel("$index", childList))
        }

        stockAdapter = StockAdapter(list)
        mBinding.rvStock.adapter = stockAdapter
        mBinding.rvStock.bindHeadScrollView(mBinding.swipeHorizontalView)
        mBinding.rvStock.setOnHorizontalRecyclerViewStateListener(object : HorizontalRecyclerView.OnHorizontalRecyclerViewStateListener {
            override fun extend() {
                Snackbar.make(mBinding.rvStock, "extend", Snackbar.LENGTH_SHORT).show()
            }

            override fun fold() {
                Snackbar.make(mBinding.rvStock, "fold", Snackbar.LENGTH_SHORT).show()
            }

        })
    }

}