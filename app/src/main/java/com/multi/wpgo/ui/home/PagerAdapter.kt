package com.multi.wpgo.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.multi.wpgo.data.Pharmacies
import com.multi.wpgo.ui.home.map.MapFragment
import com.multi.wpgo.ui.home.medicine.MedicineFragment
import java.io.Serializable

class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val TAG: String? = PagerAdapter::class.simpleName


    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> MapFragment()
            else -> MedicineFragment()
        }
    }

    override fun getCount(): Int = 2

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position) {
            0 -> "약국 검색"
            else -> "약품 검색"
        }
    }
}