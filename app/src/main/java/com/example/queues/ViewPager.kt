package com.example.queues

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class ViewPager(activity: FragmentActivity): FragmentStateAdapter(activity) {
    override fun getItemCount() = 2
    override fun createFragment(position: Int): Fragment {
        when(position){
            0 -> return  HomeFragment()
            else ->return  SettingsFragment()
        }
    }
}