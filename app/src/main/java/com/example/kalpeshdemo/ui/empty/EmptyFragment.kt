package com.example.kalpeshdemo.ui.empty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.kalpeshdemo.R
import com.example.kalpeshdemo.databinding.FragmentEmptyBinding

class EmptyFragment : Fragment(R.layout.fragment_empty) {

    private var _binding: FragmentEmptyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmptyBinding.inflate(inflater, container, false)
        return binding.root
    }

}