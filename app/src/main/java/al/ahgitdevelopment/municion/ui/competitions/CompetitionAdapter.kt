package al.ahgitdevelopment.municion.ui.competitions

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.datamodel.Competition
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_item_competition.view.*

class CompetitionAdapter : ListAdapter<Competition, CompetitionAdapter.CompetitionViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompetitionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_item_competition, parent, false) as ViewGroup

        return CompetitionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompetitionViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class CompetitionViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent) {

        fun bindTo(item: Competition) {
            itemView.item_competition_description.text = item.description
            itemView.item_competition_date.text = item.date
            itemView.item_competition_ranking.text = item.ranking
            itemView.item_competition_points.text = item.points.toString()
            itemView.item_competition_place.text = item.place
        }
    }

    override fun getItemId(position: Int): Long = currentList[position].id

    companion object {
        @JvmStatic
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Competition>() {
            override fun areItemsTheSame(oldItem: Competition, newItem: Competition): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Competition, newItem: Competition): Boolean {
                return oldItem == newItem
            }
        }
    }
}
