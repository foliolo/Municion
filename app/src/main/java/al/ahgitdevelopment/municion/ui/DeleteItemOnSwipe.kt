package al.ahgitdevelopment.municion.ui

import al.ahgitdevelopment.municion.ui.licencias.LicenseAdapter.LicenseViewHolder
import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.license_item.view.*

class DeleteItemOnSwipe(private val deleteCallback: DeleteLicenseCallback) : SimpleCallback(0, RIGHT) {

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ACTION_STATE_SWIPE) {
            if (dX > 0) {
                (viewHolder as LicenseViewHolder).itemView.item_license_delete_icon.visibility = View.VISIBLE
            } else {
                (viewHolder as LicenseViewHolder).itemView.item_license_delete_icon.visibility = View.GONE
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        when (direction) {
            RIGHT -> {
                deleteCallback.deleteLicenseOnSwipe(viewHolder as LicenseViewHolder)
            }
        }
    }

    interface DeleteLicenseCallback {
        fun deleteLicenseOnSwipe(viewHolder: LicenseViewHolder)
    }
}