package com.astroloji.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.astroloji.app.data.FirebaseManager
import com.astroloji.app.databinding.FragmentProfileBinding
import com.astroloji.app.model.ZodiacSigns
import com.astroloji.app.util.NumerologyCalculator
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Mobile Ads
        MobileAds.initialize(requireContext())
        loadBannerAd()

        loadUserProfile()
        setupButtons()
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            try {
                val profile = firebaseManager.getUserProfile()
                val user = firebaseManager.getCurrentUser()

                binding.apply {
                    usernameText.text = user?.email ?: "Bilinmiyor"

                    profile?.let {
                        val zodiacSign = it["zodiac_sign"]?.toString() ?: "Seçiniz"
                        zodiacText.text = zodiacSign

                        val zodiacData = ZodiacSigns.getSignByName(zodiacSign)
                        zodiacData?.let { zod ->
                            zodiacDetailText.text = """
                                ${zod.turkishName} ${zod.emoji}
                                Element: ${zod.element}
                                Yönetici Gezegen: ${zod.ruler}
                                Kişilik: ${zod.personality}
                            """.trimIndent()
                        }

                        val birthDate = it["birth_date"]?.toString() ?: ""
                        if (birthDate.isNotEmpty()) {
                            // Calculate numerology
                            calculateNumerology(user?.email ?: "")
                        }
                    }

                    // Check premium status
                    val isPremium = firebaseManager.checkPremiumStatus()
                    premiumStatusText.text = if (isPremium) "Premium Üyesi 🌟" else "Ücretsiz Üye"
                    premiumStatusText.setTextColor(
                        if (isPremium) android.graphics.Color.parseColor("#FFD700")
                        else android.graphics.Color.GRAY
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun calculateNumerology(name: String) {
        try {
            val nameNumber = NumerologyCalculator.calculateNameNumber(name)
            val meaning = NumerologyCalculator.getNumberMeaning(nameNumber)

            binding.numerologyText.text = """
                Ad Numarası: $nameNumber
                Anlamı: $meaning
            """.trimIndent()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupButtons() {
        binding.apply {
            editProfileButton.setOnClickListener {
                // Show edit profile dialog
            }

            buyPremiumButton.setOnClickListener {
                // Show premium purchase dialog
            }

            logoutButton.setOnClickListener {
                firebaseManager.signOut()
                // Navigate to login screen
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