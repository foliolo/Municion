package al.ahgitdevelopment.municion.ui.forms

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.ui.components.DatePickerField
import al.ahgitdevelopment.municion.ui.components.DropdownField
import al.ahgitdevelopment.municion.ui.components.getCurrentDateFormatted
import al.ahgitdevelopment.municion.ui.theme.LicenseExpired
import al.ahgitdevelopment.municion.ui.theme.LicenseValid
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.CompraViewModel
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
 * NO contiene Scaffold, TopBar ni FAB - estos estan en MainScreen.
 * Registra su funcion de guardado con MainScreen mediante onRegisterSaveCallback.
 *
 * @param compra Objeto completo para editar (null para nueva)
 * @param guia Guia asociada (siempre necesaria para validacion de cupo y calibres)
 * @param navController Controlador de navegacion
 * @param snackbarHostState Estado del snackbar compartido desde MainScreen
 * @param onRegisterSaveCallback Callback para registrar funcion de guardado con MainScreen
 * @param viewModel ViewModel de Compras
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@Composable
fun CompraFormContent(
    compra: Compra?,
    guia: al.ahgitdevelopment.municion.data.local.room.entities.Guia,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onRegisterSaveCallback: ((() -> Unit)?) -> Unit,
    viewModel: CompraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val isEditing = compra != null

    // Extraer info de cupo desde el objeto guia
    val cupoDisponible = guia.disponible()
    val cupoTotal = guia.cupo

    // Form state - inicializar directamente desde el objeto
    // Si es nueva, calibres se inicializan con los calibres de la guia
    val lugarCompraArray = stringArrayResource(id = R.array.lugar_compra)
    var calibre1 by rememberSaveable { mutableStateOf(compra?.calibre1 ?: guia.calibre1) }
    var calibre2 by rememberSaveable { mutableStateOf(compra?.calibre2 ?: guia.calibre2 ?: "") }
    var showCalibre2 by rememberSaveable {
        mutableStateOf(
            !compra?.calibre2.isNullOrBlank() || !guia.calibre2.isNullOrBlank()
        )
    }
    var unidades by rememberSaveable { mutableStateOf(compra?.unidades?.toString() ?: "") }
    var precio by rememberSaveable { mutableStateOf(compra?.precio?.toString() ?: "") }
    var fecha by rememberSaveable { mutableStateOf(compra?.fecha ?: getCurrentDateFormatted()) }
    var tipo by rememberSaveable { mutableStateOf(compra?.tipo ?: "") }
    var peso by rememberSaveable { mutableStateOf(compra?.peso?.toString() ?: "") }
    var marca by rememberSaveable { mutableStateOf(compra?.marca ?: "") }
    var tienda by rememberSaveable { mutableStateOf(compra?.tienda ?: lugarCompraArray[0]) }

    // Error states
    var calibre1Error by remember { mutableStateOf<String?>(null) }
    var unidadesError by remember { mutableStateOf<String?>(null) }
    var precioError by remember { mutableStateOf<String?>(null) }
    var fechaError by remember { mutableStateOf<String?>(null) }
    var tipoError by remember { mutableStateOf<String?>(null) }
    var pesoError by remember { mutableStateOf<String?>(null) }
    var marcaError by remember { mutableStateOf<String?>(null) }
    var tiendaError by remember { mutableStateOf<String?>(null) }

    // Validar cupo en tiempo real
    val unidadesInt = unidades.toIntOrNull() ?: 0
    val excedeCupo = unidadesInt > cupoDisponible

    // Strings para validaciones (capturadas para uso en lambda)
    val errorFieldRequired = stringResource(R.string.error_field_required)
    val errorValidUnits = stringResource(R.string.error_valid_units)
    val errorExceedsQuota = stringResource(R.string.error_exceeds_quota, cupoDisponible)
    val errorWeightRequired = stringResource(R.string.error_weight_required)

    // Funcion de guardado
    val saveFunction: () -> Unit = {
        // Validaciones
        var isValid = true
        if (calibre1.isBlank()) {
            calibre1Error = errorFieldRequired
            isValid = false
        }
        if (unidades.isBlank() || unidades.toIntOrNull() == null || unidades.toInt() <= 0) {
            unidadesError = errorValidUnits
            isValid = false
        } else if (excedeCupo) {
            unidadesError = errorExceedsQuota
            isValid = false
        }
        if (precio.isBlank()) {
            precioError = errorFieldRequired
            isValid = false
        }
        if (fecha.isBlank()) {
            fechaError = errorFieldRequired
            isValid = false
        }
        if (tipo.isBlank()) {
            tipoError = errorFieldRequired
            isValid = false
        }
        if (marca.isBlank()) {
            marcaError = errorFieldRequired
            isValid = false
        }
        if (peso.isBlank() || peso.toIntOrNull() == null || peso.toInt() <= 0) {
            pesoError = errorWeightRequired
            isValid = false
        }
        if (tienda.isBlank()) {
            tiendaError = errorFieldRequired
            isValid = false
        }

        if (isValid) {
            val compraToSave = Compra(
                id = compra?.id ?: 0,
                idPosGuia = guia.id,
                calibre1 = calibre1,
                calibre2 = if (showCalibre2) calibre2 else null,
                unidades = unidades.toInt(),
                precio = precio.replace(",", ".").toDoubleOrNull() ?: 0.0,
                fecha = fecha,
                tipo = tipo,
                peso = peso.toIntOrNull() ?: 0,
                marca = marca,
                tienda = tienda
            )
            if (isEditing) {
                viewModel.updateCompra(compraToSave)
            } else {
                viewModel.createCompra(compraToSave)
            }
        }
    }

    // Registrar funcion de guardado con MainScreen
    DisposableEffect(Unit) {
        onRegisterSaveCallback(saveFunction)
        onDispose {
            onRegisterSaveCallback(null)
        }
    }

    // Mostrar mensajes de UiState y navegar al exito
    LaunchedEffect(uiState) {
        when (uiState) {
            is CompraViewModel.CompraUiState.Success -> {
                val message = (uiState as CompraViewModel.CompraUiState.Success).message
                viewModel.resetUiState()
                navController.popBackStack()
                // Show snackbar in background - don't block navigation
                launch {
                    snackbarHostState.showSnackbar(message)
                }
            }

            is CompraViewModel.CompraUiState.Error -> {
                snackbarHostState.showSnackbar(
                    "Error: ${(uiState as CompraViewModel.CompraUiState.Error).message}"
                )
                viewModel.resetUiState()
            }

            else -> {}
        }
    }

    CompraFormFields(
        cupoDisponible = cupoDisponible,
        cupoTotal = cupoTotal,
        excedeCupo = excedeCupo,
        calibre1 = calibre1,
        calibre1Error = calibre1Error,
        calibre2 = calibre2,
        showCalibre2 = showCalibre2,
        unidades = unidades,
        unidadesError = unidadesError,
        precio = precio,
        precioError = precioError,
        fecha = fecha,
        fechaError = fechaError,
        tipo = tipo,
        tipoError = tipoError,
        peso = peso,
        pesoError = pesoError,
        marca = marca,
        marcaError = marcaError,
        tienda = tienda,
        lugarCompraArray = lugarCompraArray,
        tiendaError = tiendaError,
        onCalibre1Change = { calibre1 = it; calibre1Error = null },
        onCalibre2Change = { calibre2 = it },
        onShowCalibre2Change = { showCalibre2 = it; if (!it) calibre2 = "" },
        onUnidadesChange = { unidades = it; unidadesError = null },
        onPrecioChange = { precio = it; precioError = null },
        onFechaChange = { fecha = it; fechaError = null },
        onTipoChange = { tipo = it; tipoError = null },
        onPesoChange = { peso = it; pesoError = null },
        onMarcaChange = { marca = it; marcaError = null },
        onTiendaChange = { tienda = it }
    )
}

/**
 * Campos del formulario de Compra (Stateless).
 *
 * Sin Scaffold - solo los campos del formulario.
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@Composable
fun CompraFormFields(
    cupoDisponible: Int,
    cupoTotal: Int,
    excedeCupo: Boolean,
    calibre1: String,
    calibre1Error: String?,
    calibre2: String,
    showCalibre2: Boolean,
    unidades: String,
    unidadesError: String?,
    precio: String,
    precioError: String?,
    fecha: String,
    fechaError: String?,
    tipo: String,
    tipoError: String?,
    peso: String,
    pesoError: String?,
    marca: String,
    marcaError: String?,
    tienda: String,
    lugarCompraArray: Array<String>,
    tiendaError: String?,
    onCalibre1Change: (String) -> Unit,
    onCalibre2Change: (String) -> Unit,
    onShowCalibre2Change: (Boolean) -> Unit,
    onUnidadesChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onFechaChange: (String) -> Unit,
    onTipoChange: (String) -> Unit,
    onPesoChange: (String) -> Unit,
    onMarcaChange: (String) -> Unit,
    onTiendaChange: (String) -> Unit,
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
        if (cupoDisponible < Int.MAX_VALUE) {
            Text(
                text = stringResource(R.string.info_available_quota, cupoDisponible, cupoTotal),
                style = MaterialTheme.typography.bodyMedium,
                color = if (excedeCupo) LicenseExpired else LicenseValid
            )
        }

        // Calibre 1
        OutlinedTextField(
            value = calibre1,
            onValueChange = { },
            label = { Text(stringResource(R.string.calibre)) },
            isError = calibre1Error != null,
            supportingText = calibre1Error?.let { { Text(it) } },
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
                checked = showCalibre2,
                onCheckedChange = onShowCalibre2Change,
                enabled = false
            )
            Text(stringResource(R.string.check_segundo_calibre))
        }

        // Calibre 2
        if (showCalibre2) {
            OutlinedTextField(
                value = calibre2,
                onValueChange = onCalibre2Change,
                label = { Text(stringResource(R.string.calibre2)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = false
            )
        }

        // Marca
        OutlinedTextField(
            value = marca,
            onValueChange = onMarcaChange,
            label = { Text(stringResource(R.string.marca)) },
            isError = marcaError != null,
            supportingText = marcaError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // Tipo de municion
        OutlinedTextField(
            value = tipo,
            onValueChange = onTipoChange,
            label = { Text(stringResource(R.string.label_ammunition_type)) },
            isError = tipoError != null,
            supportingText = tipoError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // Peso
        OutlinedTextField(
            value = peso,
            onValueChange = onPesoChange,
            label = { Text(stringResource(R.string.label_weight_grams)) },
            isError = pesoError != null,
            supportingText = pesoError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        // Unidades con validacion de cupo
        OutlinedTextField(
            value = unidades,
            onValueChange = onUnidadesChange,
            label = { Text(stringResource(R.string.unidades)) },
            isError = unidadesError != null || excedeCupo,
            supportingText = {
                when {
                    unidadesError != null -> Text(unidadesError)
                    excedeCupo -> Text(stringResource(R.string.error_exceeds_quota, cupoDisponible))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Precio
        OutlinedTextField(
            value = precio,
            onValueChange = onPrecioChange,
            label = { Text(stringResource(R.string.label_price_eur)) },
            isError = precioError != null,
            supportingText = precioError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        // Fecha
        DatePickerField(
            label = stringResource(R.string.fecha),
            value = fecha,
            error = fechaError,
            onValueChange = onFechaChange
        )

        // Tienda
        DropdownField(
            label = stringResource(R.string.label_store),
            options = lugarCompraArray.toList(),
            selectedOption = tienda,
            onOptionSelected = onTiendaChange,
            error = tiendaError
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun CompraFormFieldsPreview() {
    MunicionTheme {
        val lugarCompraArray = stringArrayResource(id = R.array.lugar_compra)

        CompraFormFields(
            cupoDisponible = 75,
            cupoTotal = 100,
            excedeCupo = false,
            calibre1 = "9mm Para",
            calibre1Error = null,
            calibre2 = "",
            showCalibre2 = false,
            unidades = "50",
            unidadesError = null,
            precio = "25.99",
            precioError = null,
            fecha = "01/01/2024",
            fechaError = null,
            tipo = "FMJ",
            tipoError = null,
            peso = "124",
            pesoError = null,
            marca = "Federal",
            marcaError = null,
            tienda = lugarCompraArray[0],
            lugarCompraArray = lugarCompraArray,
            tiendaError = null,
            onCalibre1Change = {},
            onCalibre2Change = {},
            onShowCalibre2Change = {},
            onUnidadesChange = {},
            onPrecioChange = {},
            onFechaChange = {},
            onTipoChange = {},
            onPesoChange = {},
            onMarcaChange = {},
            onTiendaChange = {}
        )
    }
}
