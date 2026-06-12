package com.astroloji.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.astroloji.app.data.FirebaseManager
import com.astroloji.app.databinding.FragmentHomeBinding
import com.astroloji.app.model.ZodiacSigns
import com.astroloji.app.util.ZodiacCompatibility
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseManager.getInstance()
    private val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("tr", "TR"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Mobile Ads
        MobileAds.initialize(requireContext())

        // Load Banner Ad
        loadBannerAd()

        // Setup greeting
        setupGreeting()

        // Load today's horoscope
        loadTodayHoroscope()

        // Setup quick action buttons
        setupQuickActions()
    }

    private fun setupGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Günaydın! ☀️"
            hour < 18 -> "İyi öğleden sonralar! 🌤️"
            else -> "İyi akşamlar! 🌙"
        }

        binding.greetingText.text = greeting
        binding.dateText.text = dateFormat.format(Calendar.getInstance().time)
    }

    private fun loadTodayHoroscope() {
        lifecycleScope.launch {
            try {
                val userProfile = firebaseManager.getUserProfile()
                val zodiacSign = userProfile?.get("zodiac_sign")?.toString() ?: "Koç"

                val horoscope = firebaseManager.getTodayHoroscope(zodiacSign)

                binding.apply {
                    zodiacNameText.text = zodiacSign
                    horoscopeText.text = horoscope

                    val zodiacData = ZodiacSigns.getSignByName(zodiacSign)
                    zodiacData?.let {
                        zodiacSymbolText.text = it.emoji + " " + it.symbol
                        zodiacLuckyColor.text = "Şanslı Renk: ${it.luckyColor}"
                        zodiacLuckyNumber.text = "Şanslı Sayı: ${it.luckyNumber}"
                    }
                }
            } catch (e: Exception) {
                binding.horoscopeText.text = "Horoscope yüklenemedi"
            }
        }
    }

    private fun setupQuickActions() {
        binding.apply {
            tarotButton.setOnClickListener {
                // Navigate to Tarot Fragment
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, TarotFragment())
                    .addToBackStack(null)
                    .commit()
            }

            compatibilityButton.setOnClickListener {
                // Navigate to Compatibility Fragment
            }

            moonCalendarButton.setOnClickListener {
                // Navigate to Moon Calendar Fragment
            }

            numerologyButton.setOnClickListener {
                // Navigate to Numerology Fragment
            }
        }
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