package com.loren.component.view.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.loren.component.view.sample.databinding.ActivityDingColumnBinding
import com.loren.component.view.widget.HorizontalRecyclerView

class DingColumnActivity : AppCompatActivity() {

    private val mBinding by lazy { ActivityDingColumnBinding.inflate(layoutInflater) }
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

        mBinding.ivZfDing.setOnClickListener {
            // 仅模拟第2列，请根据实际情况
            if (mBinding.rvStock.dingColumn == 1) {
                mBinding.rvStock.dingColumn = null
                mBinding.ivZfDing.setImageResource(android.R.drawable.btn_star_big_off)
            } else {
                mBinding.rvStock.dingColumn = 1
                mBinding.ivZfDing.setImageResource(android.R.drawable.btn_star_big_on)
                mBinding.ivZfDing2.setImageResource(android.R.drawable.btn_star_big_off)
            }
            stockAdapter?.notifyDataSetChanged()
            mBinding.rvStock.bindHeadScrollView(mBinding.swipeHorizontalView)
        }

        mBinding.ivZfDing2.setOnClickListener {
            // 仅模拟第2列，请根据实际情况
            if (mBinding.rvStock.dingColumn == 2) {
                mBinding.rvStock.dingColumn = null
                mBinding.ivZfDing2.setImageResource(android.R.drawable.btn_star_big_off)
            } else {
                mBinding.rvStock.dingColumn = 2
                mBinding.ivZfDing2.setImageResource(android.R.drawable.btn_star_big_on)
                mBinding.ivZfDing.setImageResource(android.R.drawable.btn_star_big_off)
            }
            stockAdapter?.notifyDataSetChanged()
            mBinding.rvStock.bindHeadScrollView(mBinding.swipeHorizontalView)
        }
    }
}