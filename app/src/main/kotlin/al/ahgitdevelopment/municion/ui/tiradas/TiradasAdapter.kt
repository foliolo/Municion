package al.ahgitdevelopment.municion.ui.tiradas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.databinding.ItemTiradaBinding

/**
 * RecyclerView Adapter para Tiradas con DiffUtil
 *
 * FASE 4: Migración UI a Kotlin
 * - ListAdapter con DiffUtil para eficiencia
 * - ViewBinding en ViewHolder
 * - Click listeners modernos
 *
 * Reemplaza TiradaArrayAdapter
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
class TiradasAdapter(
    private val onItemClick: (Tirada) -> Unit,
    private val onItemLongClick: (Tirada) -> Unit
) : ListAdapter<Tirada, TiradasAdapter.TiradaViewHolder>(TiradaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TiradaViewHolder {
        val binding = ItemTiradaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TiradaViewHolder(binding, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: TiradaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TiradaViewHolder(
        private val binding: ItemTiradaBinding,
        private val onItemClick: (Tirada) -> Unit,
        private val onItemLongClick: (Tirada) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tirada: Tirada) {
            binding.apply {
                // Descripción
                textNombre.text = tirada.descripcion

                // Fecha
                textFecha.text = tirada.fecha

                // Puntuación [0-600]
                textPuntuacion.text = root.context.getString(
                    R.string.tirada_puntuacion_format,
                    tirada.puntuacion
                )

                // Rango/lugar (si existe)
                if (tirada.tieneRango()) {
                    textDisparos.text = "Lugar: ${tirada.rango}"
                } else {
                    textDisparos.text = "Práctica"
                }

                // Progress bar basado en puntuación (máximo 600 pts)
                val porcentaje = ((tirada.puntuacion.toFloat() / 600f) * 100f).toInt()
                progressBar.progress = porcentaje

                textPorcentaje.text = root.context.getString(
                    R.string.tirada_porcentaje_format,
                    porcentaje.toFloat()
                )

                // Color del progress bar según puntuación
                when {
                    tirada.puntuacion >= 480 -> {  // 80% de 600
                        progressBar.setIndicatorColor(
                            root.context.getColor(android.R.color.holo_green_dark)
                        )
                    }
                    tirada.puntuacion >= 360 -> {  // 60% de 600
                        progressBar.setIndicatorColor(
                            root.context.getColor(android.R.color.holo_orange_light)
                        )
                    }
                    else -> {
                        progressBar.setIndicatorColor(
                            root.context.getColor(android.R.color.holo_red_light)
                        )
                    }
                }

                // Click listener
                root.setOnClickListener {
                    onItemClick(tirada)
                }

                // Long-press listener para edición
                root.setOnLongClickListener {
                    onItemLongClick(tirada)
                    true  // Consumir el evento
                }
            }
        }
    }

    private class TiradaDiffCallback : DiffUtil.ItemCallback<Tirada>() {
        override fun areItemsTheSame(oldItem: Tirada, newItem: Tirada): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tirada, newItem: Tirada): Boolean {
            return oldItem == newItem
        }
    }
}
