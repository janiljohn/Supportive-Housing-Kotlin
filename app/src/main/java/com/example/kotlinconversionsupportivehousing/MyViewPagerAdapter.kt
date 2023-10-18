package com.example.kotlinconversionsupportivehousing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.kotlinconversionsupportivehousing.fragments.HomeFragment
import com.example.kotlinconversionsupportivehousing.fragments.NotificationFragment
import com.example.kotlinconversionsupportivehousing.fragments.WelcomeFragment

class MyViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> HomeFragment()
            1 -> NotificationFragment()
            else -> HomeFragment()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }

}

//public class MyViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
//    FragmentStateAdapter(fragmentManager, lifecycle) {
//
//    override fun createFragment(position: Int): Fragment {
//        return when (position) {
//            0 -> WelcomeFragment()
//            1 -> HomeFragment()
//            2 -> NotificationFragment()
//            else -> WelcomeFragment()
//        }
//
////        return when (position) {
////            0 -> HomeFragment()
////            1 -> NotificationFragment()
////            else -> HomeFragment()
////        }
//    }
//
//    override fun getItemCount(): Int {
//        return 3
//    }
//
//}