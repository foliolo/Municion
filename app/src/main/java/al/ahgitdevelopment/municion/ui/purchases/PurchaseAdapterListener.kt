package al.ahgitdevelopment.municion.ui.purchases

import al.ahgitdevelopment.municion.datamodel.Purchase

interface PurchaseAdapterListener {
    fun updateImage(item: Purchase)
}
