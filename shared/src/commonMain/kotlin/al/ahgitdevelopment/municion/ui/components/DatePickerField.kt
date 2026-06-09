package al.ahgitdevelopment.municion.ui.components

import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.action_select_date
import al.ahgitdevelopment.municion.resources.cancelar
import al.ahgitdevelopment.municion.util.ddMmYyyyToUtcMillis
import al.ahgitdevelopment.municion.util.todayDdMmYyyy
import al.ahgitdevelopment.municion.util.utcMillisToDdMmYyyy
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource

/**
 * Read-only date field backed by the Material 3 [DatePicker] (multiplatform). Dates are
 * "dd/MM/yyyy". Replaces the Android `android.app.DatePickerDialog`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
        ) { showDialog = true },
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = false,
            singleLine = true,
            trailingIcon = {
                Icon(Icons.Default.CalendarToday, contentDescription = stringResource(Res.string.action_select_date))
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }

    if (showDialog) {
        val state = rememberDatePickerState(initialSelectedDateMillis = ddMmYyyyToUtcMillis(value))
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onValueChange(utcMillisToDdMmYyyy(it)) }
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text(stringResource(Res.string.cancelar)) }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

/** Current date as "dd/MM/yyyy". */
fun getCurrentDateFormatted(): String = todayDdMmYyyy()
