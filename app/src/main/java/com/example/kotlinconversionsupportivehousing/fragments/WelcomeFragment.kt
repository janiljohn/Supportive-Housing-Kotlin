package com.example.kotlinconversionsupportivehousing.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.kotlinconversionsupportivehousing.R


class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

}

//class WelcomeFragment : Fragment() {
//
//    private lateinit var myTextView: TextView
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//
//        val view: View = inflater.inflate(R.layout.fragment_welcome, container, false)
//        myTextView = view.findViewById<View>(R.id.welcomeMessage) as TextView
//
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_welcome, container, false)
//    }
//
//    fun setText(text: String){
//        myTextView.text = text
//    }
//
//}