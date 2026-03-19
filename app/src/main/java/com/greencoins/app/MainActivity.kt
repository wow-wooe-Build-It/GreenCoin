package com.greencoins.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.greencoins.app.data.ThemePreferences
import com.greencoins.app.theme.GreenCoinsTheme
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

object RazorpayController {
    var paymentCallback: ((appOrderId: String, razorpayPaymentId: String?, razorpayOrderId: String?, razorpaySignature: String?, error: String?) -> Unit)? = null
    var currentAppOrderId: String? = null
}

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle deep link for Supabase Auth
        com.greencoins.app.data.SupabaseManager.handleDeepLink(intent)

        setContent {
            val isDarkTheme by ThemePreferences.isDarkThemeState
            LaunchedEffect(Unit) {
                ThemePreferences.load(this@MainActivity)
            }
            GreenCoinsTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    GreenCoinsApp()
                }
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        RazorpayController.paymentCallback?.invoke(
            RazorpayController.currentAppOrderId ?: "",
            paymentData?.paymentId,
            paymentData?.orderId,
            paymentData?.signature,
            null
        )
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        RazorpayController.paymentCallback?.invoke(
            RazorpayController.currentAppOrderId ?: "",
            null, null, null, response ?: "Payment Error $code"
        )
    }
}
