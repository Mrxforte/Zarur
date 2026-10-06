package com.example.zarur.domain.model

import com.example.zarur.R

enum class PaymentType {
    CARD,
    DIGITAL_WALLET
}

enum class CardBrand(val brandName: String, val iconResId: Int, val bgResId: Int) {
    VISA("Visa", R.drawable.ic_payment_visa, R.drawable.bg_card_visa),
    MASTERCARD("MasterCard", R.drawable.ic_payment_mastercard, R.drawable.bg_card_mastercard),
    AMEX("American Express", R.drawable.ic_payment_amex, R.drawable.bg_card_amex),
    DISCOVER("Discover", R.drawable.ic_payment_discover, R.drawable.bg_card_dark),
    UZCARD("Uzcard", R.drawable.ic_payment_uzcard, R.drawable.bg_card_uzcard),
    HUMO("Humo", R.drawable.ic_payment_humo, R.drawable.bg_card_humo),
    PAYPAL("PayPal", R.drawable.ic_payment_paypal, R.drawable.bg_card_visa),
    GOOGLE_PAY("Google Pay", R.drawable.ic_payment_googlepay, R.drawable.bg_card_dark),
    APPLE_PAY("Apple Pay", R.drawable.ic_payment_applepay, R.drawable.bg_card_dark),
    STRIPE("Stripe", R.drawable.ic_payment_stripe, R.drawable.bg_card_visa),
    KLARNA("Klarna", R.drawable.ic_payment_klarna, R.drawable.bg_card_mastercard),
    SKRILL("Skrill", R.drawable.ic_payment_skrill, R.drawable.bg_card_dark),
    ALIPAY("Alipay", R.drawable.ic_payment_alipay, R.drawable.bg_card_visa),
    WECHAT("WeChat Pay", R.drawable.ic_payment_wechat, R.drawable.bg_card_dark),
    OTHER("Payment Card", R.drawable.ic_payment, R.drawable.bg_card_dark);

    companion object {
        fun detectFromNumber(cardNumber: String): CardBrand {
            val clean = cardNumber.replace("\\s+".toRegex(), "")
            return when {
                clean.startsWith("4") -> VISA
                clean.startsWith("51") || clean.startsWith("52") || clean.startsWith("53") ||
                        clean.startsWith("54") || clean.startsWith("55") ||
                        (clean.length >= 4 && clean.substring(0, 4).toIntOrNull() in 2221..2720) -> MASTERCARD
                clean.startsWith("34") || clean.startsWith("37") -> AMEX
                clean.startsWith("6011") || clean.startsWith("65") -> DISCOVER
                clean.startsWith("8600") -> UZCARD
                clean.startsWith("9860") -> HUMO
                else -> OTHER
            }
        }
    }
}

data class PaymentMethod(
    val id: String,
    val type: PaymentType,
    val brand: CardBrand,
    val title: String,
    val cardHolderName: String = "",
    val cardNumber: String = "", // unmasked or formatted
    val maskedNumber: String = "",
    val expiryDate: String = "",
    val cvv: String = "",
    val isDefault: Boolean = false,
    val isConnected: Boolean = true,
    val balance: String? = null
) {
    val formattedDisplayNumber: String
        get() {
            if (type == PaymentType.DIGITAL_WALLET) return cardHolderName.ifEmpty { title }
            if (maskedNumber.isNotEmpty()) return maskedNumber
            val clean = cardNumber.replace("\\s+".toRegex(), "")
            return if (clean.length >= 4) {
                "•••• •••• •••• " + clean.takeLast(4)
            } else {
                "•••• •••• •••• ••••"
            }
        }

    val iconResId: Int
        get() = brand.iconResId

    val bgResId: Int
        get() = brand.bgResId
}
