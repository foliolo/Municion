package al.ahgitdevelopment.municion.ui.compras

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.databinding.ItemCompraBinding
import java.text.NumberFormat
import java.util.Locale

/**
 * RecyclerView Adapter para Compras con DiffUtil
 *
 * FASE 4: Migración UI a Kotlin
 * - ListAdapter con DiffUtil para eficiencia
 * - ViewBinding en ViewHolder
 * - Click listeners modernos
 *
 * Reemplaza CompraArrayAdapter y CompraCursorAdapter
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
class ComprasAdapter(
    private val onItemClick: (Compra) -> Unit,
    private val onItemLongClick: (Compra) -> Unit
) : ListAdapter<Compra, ComprasAdapter.CompraViewHolder>(CompraDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompraViewHolder {
        val binding = ItemCompraBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CompraViewHolder(binding, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: CompraViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CompraViewHolder(
        private val binding: ItemCompraBinding,
        private val onItemClick: (Compra) -> Unit,
        private val onItemLongClick: (Compra) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

        fun bind(compra: Compra) {
            binding.apply {
                // Marca y tipo
                textMarca.text = "${compra.marca} - ${compra.tipo}"

                // Unidades y peso
                textUnidades.text = root.context.getString(
                    R.string.compra_unidades_format,
                    compra.unidades
                )

                textPeso.text = root.context.getString(
                    R.string.compra_peso_format,
                    compra.peso
                )

                // Precio total y unitario
                textPrecioTotal.text = currencyFormatter.format(compra.precio)

                val precioUnitario = compra.precioUnitario()
                textPrecioUnitario.text = root.context.getString(
                    R.string.compra_precio_unitario_format,
                    currencyFormatter.format(precioUnitario)
                )

                // Fecha
                textFecha.text = compra.fecha

                // Click listener
                root.setOnClickListener {
                    onItemClick(compra)
                }

                // Long-press listener para edición
                root.setOnLongClickListener {
                    onItemLongClick(compra)
                    true  // Consumir el evento
                }
            }
        }
    }

    private class CompraDiffCallback : DiffUtil.ItemCallback<Compra>() {
        override fun areItemsTheSame(oldItem: Compra, newItem: Compra): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Compra, newItem: Compra): Boolean {
            return oldItem == newItem
        }
    }
}
