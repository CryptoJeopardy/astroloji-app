package com.astroloji.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.astroloji.app.databinding.FragmentZodiacBinding
import com.astroloji.app.model.ZodiacSigns
import com.astroloji.app.ui.adapters.ZodiacAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

class ZodiacFragment : Fragment() {

    private var _binding: FragmentZodiacBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentZodiacBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Mobile Ads
        MobileAds.initialize(requireContext())
        loadBannerAd()

        setupZodiacRecyclerView()
    }

    private fun setupZodiacRecyclerView() {
        val adapter = ZodiacAdapter(ZodiacSigns.ALL_SIGNS)
        binding.zodiacRecyclerView.adapter = adapter
    }

    private fun loadBannerAd() {
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}