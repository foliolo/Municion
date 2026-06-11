package al.ahgitdevelopment.municion.ui.forms.compra

import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.lugar_compra
import al.ahgitdevelopment.municion.ui.components.DatePickerField
import al.ahgitdevelopment.municion.ui.components.DropdownField
import al.ahgitdevelopment.municion.ui.components.ImagePickerField
import al.ahgitdevelopment.municion.ui.forms.FormUiState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringArrayResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CompraFormScreen(
    compraId: Int?,
    guiaId: Int,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onRegisterSaveCallback: ((() -> Unit)?) -> Unit,
    viewModel: CompraFormViewModel = koinViewModel(),
) {
    LaunchedEffect(compraId, guiaId) { viewModel.initialize(compraId, guiaId) }
    DisposableEffect(Unit) {
        onRegisterSaveCallback { viewModel.save() }
        onDispose { onRegisterSaveCallback(null) }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pickedImage by viewModel.pickedImage.collectAsStateWithLifecycle()

    val lugarCompra = stringArrayResource(Res.array.lugar_compra)

    // For a new purchase, default the store to the first option if it is still blank.
    LaunchedEffect(state.isEditing, state.tienda) {
        if (!state.isEditing && state.tienda.isBlank() && lugarCompra.isNotEmpty()) {
            viewModel.onTienda(lugarCompra.first())
        }
    }

    LaunchedEffect(uiState) {
        when (val s = uiState) {
            is FormUiState.Saved -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetUiState()
                navController.popBackStack()
            }
            is FormUiState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetUiState()
            }
            else -> Unit
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ImagePickerField(
            currentImageUrl = state.fotoUrl ?: state.imagePath,
            pickedBytes = pickedImage,
            isBusy = uiState is FormUiState.Saving,
            onPick = viewModel::onImagePicked,
            onRemove = viewModel::onImageRemoved,
            label = "Foto de la compra",
        )

        // Available quota info
        Text(
            text = "Cupo disponible: ${state.cupoDisponible} / ${state.cupoTotal}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (state.excedeCupo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        )

        // Calibre 1 (inherited from the guía, read-only)
        OutlinedTextField(
            value = state.calibre1,
            onValueChange = {},
            label = { Text("Calibre") },
            isError = state.calibre1Error != null,
            supportingText = state.calibre1Error?.let { { Text(it) } },
            enabled = false,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Checkbox(checked = state.showCalibre2, onCheckedChange = null, enabled = false)
            Text("Segundo calibre")
        }

        if (state.showCalibre2) {
            OutlinedTextField(
                value = state.calibre2,
                onValueChange = {},
                label = { Text("Segundo calibre") },
                enabled = false,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        OutlinedTextField(
            value = state.marca,
            onValueChange = viewModel::onMarca,
            label = { Text("Marca") },
            isError = state.marcaError != null,
            supportingText = state.marcaError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = state.tipo,
            onValueChange = viewModel::onTipo,
            label = { Text("Tipo de munición") },
            isError = state.tipoError != null,
            supportingText = state.tipoError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = state.peso,
            onValueChange = viewModel::onPeso,
            label = { Text("Peso (grains)") },
            isError = state.pesoError != null,
            supportingText = state.pesoError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = state.unidades,
            onValueChange = viewModel::onUnidades,
            label = { Text("Unidades") },
            isError = state.unidadesError != null,
            supportingText = state.unidadesError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = state.precio,
            onValueChange = viewModel::onPrecio,
            label = { Text("Precio (€)") },
            isError = state.precioError != null,
            supportingText = state.precioError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        DatePickerField(
            label = "Fecha",
            value = state.fecha,
            error = state.fechaError,
            onValueChange = viewModel::onFecha,
            modifier = Modifier.fillMaxWidth(),
        )

        DropdownField(
            label = "Lugar de compra",
            options = lugarCompra.toList(),
            selectedOption = state.tienda.ifBlank { lugarCompra.firstOrNull() ?: "" },
            onOptionSelected = viewModel::onTienda,
            error = state.tiendaError,
        )
    }
}
