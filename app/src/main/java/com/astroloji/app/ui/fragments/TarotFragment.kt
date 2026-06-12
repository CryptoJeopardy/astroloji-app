package com.astroloji.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.astroloji.app.data.FirebaseManager
import com.astroloji.app.databinding.FragmentTarotBinding
import com.astroloji.app.model.TarotCards
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.launch

class TarotFragment : Fragment() {

    private var _binding: FragmentTarotBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTarotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Mobile Ads
        MobileAds.initialize(requireContext())
        loadBannerAd()

        setupButtons()
    }

    private fun setupButtons() {
        binding.apply {
            // Single Card
            singleCardButton.setOnClickListener {
                drawSingleCard()
            }

            // Three Card Reading
            threeCardButton.setOnClickListener {
                draw3CardReading()
            }

            // Ten Card Reading
            tenCardButton.setOnClickListener {
                draw10CardReading()
            }

            // Card Meanings
            cardMeaningsButton.setOnClickListener {
                // Show card meanings dialog
            }
        }
    }

    private fun drawSingleCard() {
        val card = TarotCards.drawRandomCard()

        binding.apply {
            cardNameText.text = card.turkishName
            cardArcanaText.text = "${card.arcana} Arcana"
            cardMeaningUprightText.text = "Doğru: ${card.meaningUpright}"
            cardMeaningReversedText.text = "Ters: ${card.meaningReversed}"
            cardDescriptionText.text = card.description

            // Save to history
            saveTarotReading(listOf(card.turkishName), "Tek Kart", card.meaningUpright)
        }
    }

    private fun draw3CardReading() {
        val (card1, card2, card3) = TarotCards.draw3CardReading()

        binding.apply {
            card1Text.text = "Geçmiş: ${card1.turkishName}\n${card1.meaningUpright}"
            card2Text.text = "Şimdi: ${card2.turkishName}\n${card2.meaningUpright}"
            card3Text.text = "Gelecek: ${card3.turkishName}\n${card3.meaningUpright}"

            // Save to history
            saveTarotReading(
                listOf(card1.turkishName, card2.turkishName, card3.turkishName),
                "3 Kart Desen",
                "Geçmiş-Şimdi-Gelecek Okuması"
            )
        }
    }

    private fun draw10CardReading() {
        // Keltic Cross Pattern (10 kart)
        val cards = TarotCards.getAllCards().shuffled().take(10)

        val interpretation = """
            1. Kendiniz (Vazgeçilmez): ${cards[0].turkishName}
            2. Engeller: ${cards[1].turkishName}
            3. Gizli Etkenler: ${cards[2].turkishName}
            4. Geçmiş: ${cards[3].turkishName}
            5. Mümkün Sonuç: ${cards[4].turkishName}
            6. Yakın Gelecek: ${cards[5].turkishName}
            7. Sizin Tutumunuz: ${cards[6].turkishName}
            8. Diğerlerinin Tutumu: ${cards[7].turkishName}
            9. Umutlar ve Korkular: ${cards[8].turkishName}
            10. Nihai Sonuç: ${cards[9].turkishName}
        """.trimIndent()

        binding.apply {
            readingResultText.text = interpretation

            // Save to history
            saveTarotReading(
                cards.map { it.turkishName },
                "10 Kart Keltic Cross",
                interpretation
            )
        }
    }

    private fun saveTarotReading(cardsDrawn: List<String>, readingType: String, interpretation: String) {
        lifecycleScope.launch {
            try {
                firebaseManager.saveTarotReading(cardsDrawn, readingType, interpretation)
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