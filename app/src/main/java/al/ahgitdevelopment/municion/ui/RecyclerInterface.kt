package al.ahgitdevelopment.municion.ui

import al.ahgitdevelopment.municion.ui.licenses.LicenseAdapter
import androidx.recyclerview.widget.RecyclerView

interface RecyclerInterface {
    fun RecyclerView?.undoDelete(viewHolder: LicenseAdapter.LicenseViewHolder)
}
