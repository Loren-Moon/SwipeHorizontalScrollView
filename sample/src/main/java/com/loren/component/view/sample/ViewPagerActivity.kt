package com.loren.component.view.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.loren.component.view.sample.databinding.ActivityViewPagerBinding

class ViewPagerActivity : AppCompatActivity() {

    private val mBinding by lazy { ActivityViewPagerBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        mBinding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 1
            }

            override fun createFragment(position: Int): Fragment {
                return HideLeftFragment()
            }

        }
    }

}