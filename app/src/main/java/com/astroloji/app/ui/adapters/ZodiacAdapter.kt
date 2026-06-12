package com.astroloji.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.astroloji.app.databinding.ItemZodiacBinding
import com.astroloji.app.model.ZodiacSign

class ZodiacAdapter(private val zodiacSigns: List<ZodiacSign>) :
    RecyclerView.Adapter<ZodiacAdapter.ZodiacViewHolder>() {

    inner class ZodiacViewHolder(private val binding: ItemZodiacBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(zodiacSign: ZodiacSign) {
            binding.apply {
                zodiacNameText.text = "${zodiacSign.emoji} ${zodiacSign.turkishName}"
                zodiacSymbolText.text = zodiacSign.symbol
                zodiacElementText.text = "Element: ${zodiacSign.element}"
                zodiacRulerText.text = "Yönetici: ${zodiacSign.ruler}"
                zodiacPersonalityText.text = zodiacSign.personality

                zodiacDatesText.text = "${zodiacSign.startDate} - ${zodiacSign.endDate}"

                strengthsText.text = "Güçlü Yönler: ${zodiacSign.strengths.joinToString(", ")}"
                weaknessesText.text = "Zayıf Yönler: ${zodiacSign.weaknesses.joinToString(", ")}"

                luckyColorText.text = "Şanslı Renk: ${zodiacSign.luckyColor}"
                luckyNumberText.text = "Şanslı Sayı: ${zodiacSign.luckyNumber}"
                luckyDayText.text = "Şanslı Gün: ${zodiacSign.luckyDay}"

                compatibleText.text = "Uyumlu: ${zodiacSign.compatibleSigns.joinToString(", ")}"
                incompatibleText.text = "Uyumsuz: ${zodiacSign.incompatibleSigns.joinToString(", ")}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZodiacViewHolder {
        val binding = ItemZodiacBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ZodiacViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ZodiacViewHolder, position: Int) {
        holder.bind(zodiacSigns[position])
    }

    override fun getItemCount(): Int = zodiacSigns.size
}