package al.ahgitdevelopment.municion.ui

import al.ahgitdevelopment.municion.ui.licencias.LicenseAdapter
import androidx.recyclerview.widget.RecyclerView

interface RecyclerInterface {
    fun RecyclerView?.undoDelete(viewHolder: LicenseAdapter.LicenseViewHolder)
}
