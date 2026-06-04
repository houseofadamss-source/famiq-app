package com.famiq.app.ui.screen

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.famiq.app.R
import com.famiq.app.data.model.Kategori
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Helper untuk format mata uang Rupiah yang konsisten di seluruh aplikasi.
 */
fun formatRupiah(nominal: Long): String {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    formatRp.maximumFractionDigits = 0
    return formatRp.format(nominal).replace("Rp", "").trim()
}

/**
 * Helper untuk format tanggal yang konsisten.
 */
fun formatTanggal(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(timestamp)
}

/**
 * Helper untuk mendapatkan String Resource ID dari Kategori
 */
fun getKategoriStringRes(kategori: Kategori): Int {
    return when (kategori) {
        Kategori.MAKAN -> R.string.cat_food
        Kategori.TRANSPORT -> R.string.cat_transport
        Kategori.BELANJA -> R.string.cat_shopping
        Kategori.RUMAH -> R.string.cat_home
        Kategori.KESEHATAN -> R.string.cat_health
        Kategori.LAINNYA -> R.string.cat_others
        Kategori.GAJI -> R.string.cat_salary
        Kategori.BONUS -> R.string.cat_bonus
        Kategori.INVESTASI -> R.string.cat_investment
        Kategori.HADIAH -> R.string.cat_gift
        Kategori.PENDAPATAN_LAIN -> R.string.cat_other_income
    }
}

/**
 * Visual Transformation untuk format Rupiah otomatis saat ngetik (1.000.000)
 */
class RupiahVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val formattedText = formatRupiah(originalText.toLongOrNull() ?: 0L)
        
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val originalSub = originalText.substring(0, offset.coerceAtMost(originalText.length))
                val transformedSub = formatRupiah(originalSub.toLongOrNull() ?: 0L)
                return transformedSub.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                val originalLength = originalText.length
                val transformedTextActual = formatRupiah(originalText.toLongOrNull() ?: 0L)
                
                if (offset >= transformedTextActual.length) return originalLength
                
                var originalOffset = 0
                var transformedOffset = 0
                while (originalOffset < originalLength && transformedOffset < offset) {
                    if (transformedTextActual[transformedOffset].isDigit()) {
                        originalOffset++
                    }
                    transformedOffset++
                }
                return originalOffset
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}
