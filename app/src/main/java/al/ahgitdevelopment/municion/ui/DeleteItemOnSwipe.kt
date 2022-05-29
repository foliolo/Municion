package al.ahgitdevelopment.municion.ui

import al.ahgitdevelopment.municion.R
import android.graphics.Canvas
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.RecyclerView

class DeleteItemOnSwipe(private val deleteCallback: DeleteCallback) : SimpleCallback(0, RIGHT) {

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
                viewHolder.itemView.findViewById<ImageView>(R.id.item_delete_icon).visibility = View.VISIBLE
            } else {
                viewHolder.itemView.findViewById<ImageView>(R.id.item_delete_icon).visibility = View.GONE
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
                deleteCallback.deleteOnSwipe(viewHolder)
            }
        }
    }

    interface DeleteCallback {
        fun deleteOnSwipe(viewHolder: RecyclerView.ViewHolder)
    }
}
