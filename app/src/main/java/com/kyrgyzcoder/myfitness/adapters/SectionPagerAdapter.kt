package com.kyrgyzcoder.myfitness.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.kyrgyzcoder.myfitness.history.HistoryFragment
import com.kyrgyzcoder.myfitness.run.RunFragment


class SectionPagerAdapter(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {

        return when (position) {
            0 -> RunFragment()
            else -> HistoryFragment()
        }
    }

    override fun getCount(): Int {
        return 2
    }

}