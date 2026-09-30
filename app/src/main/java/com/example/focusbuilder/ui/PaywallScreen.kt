package com.example.focusbuilder.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.models.StoreTransaction
import com.example.focusbuilder.billing.RevenueCatManager

@Composable
fun PaywallScreen(
    onNavigateBack: () -> Unit,
    onUnlockSuccess: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    
    val isProUnlocked by RevenueCatManager.isLoviProUnlocked.collectAsState()
    val currentOffering by RevenueCatManager.currentOffering.collectAsState()
    val offeringsError by RevenueCatManager.offeringsError.collectAsState()

    var selectedPackage by remember { mutableStateOf<Package?>(null) }

    LaunchedEffect(isProUnlocked) {
        if (isProUnlocked) {
            onUnlockSuccess()
        }
    }

    // Auto-select the first available package once offerings load
    LaunchedEffect(currentOffering) {
        if (selectedPackage == null && currentOffering?.current?.availablePackages?.isNotEmpty() == true) {
            selectedPackage = currentOffering?.current?.availablePackages?.firstOrNull()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFDFBF7)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Premium Hero Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // Soft glow background bubble
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(Color(0xFF5CB895).copy(alpha = 0.15f), CircleShape)
                )
                
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.focusbuilder.R.drawable.illust_deer),
                        contentDescription = null,
                        modifier = Modifier
                            .size(110.dp)
                            .offset(x = 16.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.focusbuilder.R.drawable.illust_fox),
                        contentDescription = null,
                        modifier = Modifier
                            .size(90.dp)
                            .offset(x = (-16).dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Unlock Lovi Plus",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A2F)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Get access to all focus games and help your child grow.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color(0xFF1E3A2F).copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (offeringsError != null && currentOffering == null) {
                Text(
                    text = "Failed to load offers:\n$offeringsError",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { RevenueCatManager.fetchOfferings() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5CB895))
                ) {
                    Text("Try Again")
                }
            } else if (currentOffering == null) {
                CircularProgressIndicator(color = Color(0xFF5CB895))
            } else {
                val packages = currentOffering?.current?.availablePackages ?: emptyList()
                
                if (packages.isEmpty()) {
                    Text(
                        text = "No packages available right now.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    packages.forEach { pack ->
                        val isSelected = selectedPackage == pack
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { selectedPackage = pack },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(2.dp, if (isSelected) Color(0xFF5CB895) else Color.Transparent),
                            color = if (isSelected) Color(0xFF5CB895).copy(alpha = 0.1f) else Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = pack.product.title.replace(Regex("\\(.*\\)"), "").trim(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E3A2F)
                                    )
                                    Text(
                                        text = pack.product.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                Text(
                                    text = pack.product.price.formatted,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5CB895)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = Color(0xFF5CB895))
                    } else {
                        Button(
                            onClick = {
                                selectedPackage?.let { pack ->
                                    isLoading = true
                                    Purchases.sharedInstance.purchasePackage(
                                        context as Activity,
                                        pack,
                                        object : PurchaseCallback {
                                            override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                                                isLoading = false
                                                RevenueCatManager.refreshProStatus()
                                            }

                                            override fun onError(error: PurchasesError, userCancelled: Boolean) {
                                                isLoading = false
                                                if (!userCancelled) {
                                                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5CB895)),
                            shape = CircleShape,
                            enabled = selectedPackage != null
                        ) {
                            Text(
                                text = "Continue",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Restore Purchases",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5CB895),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            isLoading = true
                            Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
                                override fun onReceived(customerInfo: CustomerInfo) {
                                    isLoading = false
                                    RevenueCatManager.refreshProStatus()
                                    if (customerInfo.entitlements["lovi_pro"]?.isActive != true) {
                                        Toast.makeText(context, "No previous purchases found", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onError(error: PurchasesError) {
                                    isLoading = false
                                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Not now",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier
                        .clickable { onNavigateBack() }
                        .padding(8.dp)
                )
            }
        }
    }
}
