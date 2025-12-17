package al.ahgitdevelopment.municion.ui.forms.compra

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.ui.components.DatePickerField
import al.ahgitdevelopment.municion.ui.components.DropdownField
import al.ahgitdevelopment.municion.ui.components.getCurrentDateFormatted
import al.ahgitdevelopment.municion.ui.components.imagepicker.ImagePickerWithCamera
import al.ahgitdevelopment.municion.ui.theme.LicenseExpired
import al.ahgitdevelopment.municion.ui.theme.LicenseValid
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

/**
 * Contenido del formulario de Compra para Single Scaffold Architecture.
 *
 * NO contiene Scaffold, TopBar ni FAB - estos están en MainScreen.
 * Usa el patrón MVI con CompraFormViewModel.
 *
 * @param compra Objeto completo para editar (null para nueva)
 * @param guia Guía asociada (siempre necesaria para validación de cupo y calibres)
 * @param navController Controlador de navegación
 * @param snackbarHostState Estado del snackbar compartido desde MainScreen
 * @param onRegisterSaveCallback Callback para registrar función de guardado con MainScreen
 * @param viewModel ViewModel del formulario
 *
 * @since v3.2.3 (Form Architecture Refactor)
 */
@Composable
fun CompraFormContent(
    compra: Compra?,
    guia: Guia,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onRegisterSaveCallback: ((() -> Unit)?) -> Unit,
    viewModel: CompraFormViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lugarCompraArray = stringArrayResource(id = R.array.lugar_compra)

    // Inicializar ViewModel con datos
    LaunchedEffect(compra, guia) {
        viewModel.initialize(compra, guia)
        // Si es nueva compra y tienda está vacía, establecer el primer valor
        if (compra == null && formState.tienda.isBlank() && lugarCompraArray.isNotEmpty()) {
            viewModel.onEvent(CompraFormEvent.TiendaChanged(lugarCompraArray[0]))
        }
    }

    // Registrar función de guardado
    DisposableEffect(Unit) {
        onRegisterSaveCallback { viewModel.onEvent(CompraFormEvent.Save) }
        onDispose { onRegisterSaveCallback(null) }
    }

    // Manejar efectos (navegación, snackbar)
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CompraFormEffect.NavigateBack -> navController.popBackStack()
                is CompraFormEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is CompraFormEffect.ShowError -> snackbarHostState.showSnackbar("Error: ${effect.message}")
            }
        }
    }

    // Manejar estados de UI
    LaunchedEffect(uiState) {
        when (uiState) {
            is CompraFormUiState.Success -> {
                val message = (uiState as CompraFormUiState.Success).message
                viewModel.resetUiState()
                navController.popBackStack()
                launch { snackbarHostState.showSnackbar(message) }
            }

            is CompraFormUiState.Error -> {
                snackbarHostState.showSnackbar("Error: ${(uiState as CompraFormUiState.Error).message}")
                viewModel.resetUiState()
            }

            else -> {}
        }
    }

    // Calcular estados de subida
    val isUploading = uiState is CompraFormUiState.Uploading || uiState is CompraFormUiState.Loading
    val uploadProgress = (uiState as? CompraFormUiState.Uploading)?.progress ?: 0f

    CompraFormFields(
        formState = formState,
        lugarCompraArray = lugarCompraArray,
        onEvent = viewModel::onEvent,
        isUploading = isUploading,
        uploadProgress = uploadProgress
    )
}

/**
 * Campos del formulario de Compra (Stateless).
 *
 * @since v3.2.3 (Form Architecture Refactor)
 */
@Composable
fun CompraFormFields(
    formState: CompraFormState,
    lugarCompraArray: Array<String>,
    onEvent: (CompraFormEvent) -> Unit,
    isUploading: Boolean = false,
    uploadProgress: Float = 0f,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Info de cupo disponible
        if (formState.cupoDisponible < Int.MAX_VALUE) {
            Text(
                text = stringResource(
                    R.string.info_available_quota,
                    formState.cupoDisponible,
                    formState.cupoTotal
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = if (formState.excedeCupo) LicenseExpired else LicenseValid
            )
        }

        // Calibre 1 (solo lectura, viene de la guía)
        OutlinedTextField(
            value = formState.calibre1,
            onValueChange = { },
            label = { Text(stringResource(R.string.calibre)) },
            isError = formState.calibre1Error != null,
            supportingText = formState.calibre1Error?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            singleLine = true
        )

        // Checkbox segundo calibre
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = formState.showCalibre2,
                onCheckedChange = { },
                enabled = false
            )
            Text(stringResource(R.string.check_segundo_calibre))
        }

        // Calibre 2 (solo lectura)
        if (formState.showCalibre2) {
            OutlinedTextField(
                value = formState.calibre2,
                onValueChange = { },
                label = { Text(stringResource(R.string.calibre2)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = false
            )
        }

        // Marca
        OutlinedTextField(
            value = formState.marca,
            onValueChange = { onEvent(CompraFormEvent.MarcaChanged(it)) },
            label = { Text(stringResource(R.string.marca)) },
            isError = formState.marcaError != null,
            supportingText = formState.marcaError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // Tipo de munición
        OutlinedTextField(
            value = formState.tipo,
            onValueChange = { onEvent(CompraFormEvent.TipoChanged(it)) },
            label = { Text(stringResource(R.string.label_ammunition_type)) },
            isError = formState.tipoError != null,
            supportingText = formState.tipoError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // Peso
        OutlinedTextField(
            value = formState.peso,
            onValueChange = { onEvent(CompraFormEvent.PesoChanged(it)) },
            label = { Text(stringResource(R.string.label_weight_grams)) },
            isError = formState.pesoError != null,
            supportingText = formState.pesoError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Unidades con validación de cupo
        OutlinedTextField(
            value = formState.unidades,
            onValueChange = { onEvent(CompraFormEvent.UnidadesChanged(it)) },
            label = { Text(stringResource(R.string.unidades)) },
            isError = formState.unidadesError != null || formState.excedeCupo,
            supportingText = {
                when {
                    formState.unidadesError != null -> Text(formState.unidadesError!!)
                    formState.excedeCupo -> Text(
                        stringResource(R.string.error_exceeds_quota, formState.cupoDisponible)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Precio
        OutlinedTextField(
            value = formState.precio,
            onValueChange = { onEvent(CompraFormEvent.PrecioChanged(it)) },
            label = { Text(stringResource(R.string.label_price_eur)) },
            isError = formState.precioError != null,
            supportingText = formState.precioError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        // Fecha
        DatePickerField(
            label = stringResource(R.string.fecha),
            value = formState.fecha.ifBlank { getCurrentDateFormatted() },
            error = formState.fechaError,
            onValueChange = { onEvent(CompraFormEvent.FechaChanged(it)) }
        )

        // Tienda
        DropdownField(
            label = stringResource(R.string.label_store),
            options = lugarCompraArray.toList(),
            selectedOption = formState.tienda.ifBlank { lugarCompraArray.firstOrNull() ?: "" },
            onOptionSelected = { onEvent(CompraFormEvent.TiendaChanged(it)) },
            error = formState.tiendaError
        )

        // Sección de imagen
        Text(
            text = stringResource(R.string.image_section_ammunition),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )

        ImagePickerWithCamera(
            currentImageUrl = formState.currentImageUrl,
            isUploading = isUploading,
            uploadProgress = uploadProgress,
            onImageSelected = { uri -> onEvent(CompraFormEvent.ImageSelected(uri)) },
            onImageRemoved = { onEvent(CompraFormEvent.ImageRemoved) },
            label = stringResource(R.string.label_ammunition_photo),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun CompraFormFieldsPreview() {
    MunicionTheme {
        val lugarCompraArray = arrayOf("Armería", "Federación", "Club", "Particular")

        CompraFormFields(
            formState = CompraFormState(
                calibre1 = "9mm Para",
                marca = "Federal",
                tipo = "FMJ",
                peso = "124",
                unidades = "50",
                precio = "25.99",
                fecha = "01/01/2024",
                tienda = "Armería",
                cupoDisponible = 75,
                cupoTotal = 100
            ),
            lugarCompraArray = lugarCompraArray,
            onEvent = {}
        )
    }
}
