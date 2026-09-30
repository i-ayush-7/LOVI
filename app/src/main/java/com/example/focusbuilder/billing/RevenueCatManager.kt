package com.example.focusbuilder.billing

import android.content.Context
import android.util.Log
import com.example.focusbuilder.BuildConfig
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.Store
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object RevenueCatManager {
    const val DEMO_MODE_UNLOCK_ALL = false

    private val _isLoviProUnlocked = MutableStateFlow(DEMO_MODE_UNLOCK_ALL)
    val isLoviProUnlocked: StateFlow<Boolean> = _isLoviProUnlocked.asStateFlow()

    private val _currentOffering = MutableStateFlow<Offerings?>(null)
    val currentOffering: StateFlow<Offerings?> = _currentOffering.asStateFlow()

    private val _offeringsError = MutableStateFlow<String?>(null)
    val offeringsError: StateFlow<String?> = _offeringsError.asStateFlow()

    fun init(context: Context) {
        val apiKey = BuildConfig.REVENUECAT_API_KEY
        if (apiKey.isBlank()) {
            Log.e("RevenueCat", "RevenueCat API Key is missing. Check local.properties.")
            return
        }

        Purchases.configure(
            PurchasesConfiguration.Builder(context, apiKey)
                .store(Store.PLAY_STORE)
                .build()
        )
        
        refreshProStatus()
        fetchOfferings()
    }

    fun fetchOfferings() {
        if (!Purchases.isConfigured) return
        _offeringsError.value = null
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                _currentOffering.value = offerings
                _offeringsError.value = null
            }

            override fun onError(error: PurchasesError) {
                Log.e("RevenueCat", "Error fetching offerings: ${error.message} - ${error.underlyingErrorMessage}")
                _offeringsError.value = "${error.message}\nUnderlying: ${error.underlyingErrorMessage ?: "None"}"
            }
        })
    }

    fun refreshProStatus() {
        if (!Purchases.isConfigured) return
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                _isLoviProUnlocked.value = DEMO_MODE_UNLOCK_ALL || customerInfo.entitlements["lovi_pro"]?.isActive == true
            }

            override fun onError(error: PurchasesError) {
                Log.e("RevenueCat", "Error fetching customer info: \${error.message}")
            }
        })
    }
}
