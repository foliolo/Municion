package al.ahgitdevelopment.municion.ui.components

import al.ahgitdevelopment.municion.R
import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Campo de fecha con DatePicker, optimizado para una mejor UX.
 * Este campo no es editable por teclado y siempre abre el selector de fecha al pulsarlo.
 *
 * El componente usa un Box para capturar los clics en toda el área del campo,
 * incluyendo el label, solucionando el problema común donde el label intercepta los clics.
 *
 * @param label Etiqueta del campo
 * @param value Valor actual de la fecha en formato "dd/MM/yyyy"
 * @param error Mensaje de error opcional
 * @param onValueChange Callback cuando cambia la fecha
 * @param modifier Modificador opcional
 */
@Composable
fun DatePickerField(
    label: String,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val datePickerDialog = remember {
        val calendar = Calendar.getInstance()
        if (value.isNotBlank()) {
            try {
                dateFormat.parse(value)?.let { calendar.time = it }
            } catch (e: Exception) { /* ignore */ }
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                onValueChange(dateFormat.format(cal.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Usamos un Box para capturar los clics en toda el área del campo.
    Box(
        modifier = modifier.clickable(
            // Deshabilitamos la indicación visual de clic (ripple) para que no parezca un botón.
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            datePickerDialog.show()
        }
    ) {
        // El OutlinedTextField se vuelve un elemento puramente visual.
        OutlinedTextField(
            value = value,
            onValueChange = {}, // La lambda está vacía porque es de solo lectura.
            label = { Text(label) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true, // Impide la edición por teclado.
            // Lo deshabilitamos para que no pueda obtener el foco ni mostrar el cursor,
            // pero su apariencia no cambiará gracias a la sobreescritura de colores.
            enabled = false,
            singleLine = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = stringResource(R.string.action_select_date)
                )
            },
            // Sobreescribimos los colores para que parezca habilitado.
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPrefixColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledSuffixColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

/**
 * Obtiene la fecha actual formateada como "dd/MM/yyyy".
 *
 * @return Fecha actual en formato "dd/MM/yyyy"
 */
fun getCurrentDateFormatted(): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(Calendar.getInstance().time)
}
