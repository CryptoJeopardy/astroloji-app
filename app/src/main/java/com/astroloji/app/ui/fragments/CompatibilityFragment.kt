package com.astroloji.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.astroloji.app.data.FirebaseManager
import com.astroloji.app.databinding.FragmentCompatibilityBinding
import com.astroloji.app.model.ZodiacSigns
import com.astroloji.app.util.ZodiacCompatibility
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.launch

class CompatibilityFragment : Fragment() {

    private var _binding: FragmentCompatibilityBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompatibilityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Mobile Ads
        MobileAds.initialize(requireContext())
        loadBannerAd()

        setupZodiacSelectors()
        setupCalculateButton()
    }

    private fun setupZodiacSelectors() {
        val zodiacNames = ZodiacSigns.ALL_SIGNS.map { it.turkishName }

        binding.apply {
            // First zodiac spinner
            zodiacSpinner1.setItems(zodiacNames)

            // Second zodiac spinner
            zodiacSpinner2.setItems(zodiacNames)
        }
    }

    private fun setupCalculateButton() {
        binding.calculateButton.setOnClickListener {
            val sign1 = binding.zodiacSpinner1.selectedItem.toString()
            val sign2 = binding.zodiacSpinner2.selectedItem.toString()

            calculateCompatibility(sign1, sign2)
        }
    }

    private fun calculateCompatibility(sign1: String, sign2: String) {
        val score = ZodiacCompatibility.getCompatibilityScore(sign1, sign2)
        val description = ZodiacCompatibility.getCompatibilityDescription(score)
        val advice = ZodiacCompatibility.getCompatibilityAdvice(sign1, sign2)
        val luckyDays = ZodiacCompatibility.getLuckyDays(sign1, sign2)

        binding.apply {
            compatibilityScoreText.text = "$score%"
            compatibilityScoreProgress.progress = score

            compatibilityDescriptionText.text = description
            compatibilityAdviceText.text = advice

            luckyDaysText.text = "Şanslı Günler: ${luckyDays.joinToString(", ")}"

            // Show zodiac details
            val zodiacData1 = ZodiacSigns.getSignByName(sign1)
            val zodiacData2 = ZodiacSigns.getSignByName(sign2)

            zodiacData1?.let {
                zodiac1DetailText.text = """
                    ${it.turkishName} ${it.emoji}
                    Element: ${it.element}
                    Yönetici: ${it.ruler}
                """.trimIndent()
            }

            zodiacData2?.let {
                zodiac2DetailText.text = """
                    ${it.turkishName} ${it.emoji}
                    Element: ${it.element}
                    Yönetici: ${it.ruler}
                """.trimIndent()
            }

            // Save result
            saveCompatibilityResult(sign1, sign2, score, description)
        }
    }

    private fun saveCompatibilityResult(sign1: String, sign2: String, score: Int, description: String) {
        lifecycleScope.launch {
            try {
                firebaseManager.saveCompatibilityResult(sign1, sign2, score, description)
            } catch (e: Exception) {
                e.printStackTrace()
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