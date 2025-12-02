package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.AppPurchaseDao
import al.ahgitdevelopment.municion.data.local.room.entities.AppPurchase
import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPurchaseDao: AppPurchaseDao
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingRepository"

        // TODO: Confirm this SKU with Play Console
        const val SKU_REMOVE_ADS = "remove_ads"
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    val productDetails: StateFlow<ProductDetails?>
        field = MutableStateFlow<ProductDetails?>(null)

    // Observes if the "remove_ads" SKU exists in the local database
    val isAdsRemoved: Flow<Boolean> = appPurchaseDao.getPurchaseFlow(SKU_REMOVE_ADS)
        .map { it != null }

    init {
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing Setup Finished")
                    queryProductDetails()
                    queryPurchases()
                } else {
                    Log.e(TAG, "Billing Setup Failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing Service Disconnected")
            }
        })
    }

    private fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Query Purchases: Found ${purchasesList.size} purchases")
                for (purchase in purchasesList) {
                    handlePurchase(purchase)
                }
            } else {
                Log.e(TAG, "Query Purchases Failed: ${billingResult.debugMessage}")
            }
        }
    }

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_REMOVE_ADS)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (productDetails in productDetailsList.productDetailsList) {
                    Log.d(TAG, "Product Details Found: ${productDetails.name} - ${productDetails.productId}")
                    this@BillingRepository.productDetails.update { productDetails }
                }

                for (unfetchedProduct in productDetailsList.unfetchedProductList) {
                    // Handle any unfetched products as appropriate.
                    Log.w(
                        TAG, "No product details found for $SKU_REMOVE_ADS. Check Play Console configuration."
                    )
                }
            } else {
                Log.e(TAG, "Query Product Details Failed: ${billingResult.debugMessage}")
            }
        }
    }

    fun launchBillingFlow(activity: Activity) {
        val details = productDetails.value
        Log.i(TAG, "Product Detail token: ${details?.oneTimePurchaseOfferDetails?.offerToken.orEmpty()}")

        if (details != null) {
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .setOfferToken(details.oneTimePurchaseOfferDetails?.offerToken.orEmpty())
                    .build()
            )

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            billingClient.launchBillingFlow(activity, billingFlowParams)
        } else {
            Log.e(TAG, "Cannot launch billing flow: Product Details not loaded. Retrying query...")
            // Try to query again if details are missing, maybe connection was lost/restored
            if (billingClient.isReady) {
                queryProductDetails()
            } else {
                startConnection()
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled purchase")
        } else {
            Log.e(TAG, "Purchase update failed: ${billingResult.debugMessage}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase acknowledged")
                        savePurchaseToRoom(purchase)
                    }
                }
            } else {
                savePurchaseToRoom(purchase)
            }
        }
    }

    private fun savePurchaseToRoom(purchase: Purchase) {
        CoroutineScope(Dispatchers.IO).launch {
            purchase.products.forEach { sku ->
                if (sku == SKU_REMOVE_ADS) {
                    val appPurchase = AppPurchase(
                        sku = sku,
                        purchaseToken = purchase.purchaseToken,
                        purchaseTime = purchase.purchaseTime,
                        isAcknowledged = purchase.isAcknowledged
                    )
                    appPurchaseDao.insert(appPurchase)
                }
            }
        }
    }
}
