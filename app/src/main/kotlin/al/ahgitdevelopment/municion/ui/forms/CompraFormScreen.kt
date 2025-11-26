package al.ahgitdevelopment.municion.ui.forms

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.ui.components.FormScreenTopBar
import al.ahgitdevelopment.municion.ui.theme.LicenseExpired
import al.ahgitdevelopment.municion.ui.theme.LicenseValid
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.CompraViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Pantalla de formulario de Compra (Stateful).
 *
 * Incluye validación de cupo en tiempo real.
 *
 * @param navController Controlador de navegación
 * @param guiaId ID de la guía asociada
 * @param compraId ID de compra a editar (null para nueva)
 * @param cupoDisponible Cupo disponible para esta guía
 * @param cupoTotal Cupo total de la guía
 * @param viewModel ViewModel de Compras
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun CompraFormScreen(
    navController: NavHostController,
    guiaId: Int,
    compraId: Int? = null,
    cupoDisponible: Int = Int.MAX_VALUE,
    cupoTotal: Int = 0,
    viewModel: CompraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val compras by viewModel.compras.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val isEditing = compraId != null

    // Cargar compra existente si estamos editando
    val existingCompra = remember(compraId, compras) {
        compraId?.let { id -> compras.find { it.id == id } }
    }

    // Form state
    var calibre1 by rememberSaveable { mutableStateOf(existingCompra?.calibre1 ?: "") }
    var calibre2 by rememberSaveable { mutableStateOf(existingCompra?.calibre2 ?: "") }
    var showCalibre2 by rememberSaveable { mutableStateOf(!existingCompra?.calibre2.isNullOrBlank()) }
    var unidades by rememberSaveable { mutableStateOf(existingCompra?.unidades?.toString() ?: "") }
    var precio by rememberSaveable { mutableStateOf(existingCompra?.precio?.toString() ?: "") }
    var fecha by rememberSaveable { mutableStateOf(existingCompra?.fecha ?: getCurrentDate()) }
    var tipo by rememberSaveable { mutableStateOf(existingCompra?.tipo ?: "") }
    var peso by rememberSaveable { mutableStateOf(existingCompra?.peso?.toString() ?: "") }
    var marca by rememberSaveable { mutableStateOf(existingCompra?.marca ?: "") }
    var tienda by rememberSaveable { mutableStateOf(existingCompra?.tienda ?: "") }

    // Error states
    var calibre1Error by remember { mutableStateOf<String?>(null) }
    var unidadesError by remember { mutableStateOf<String?>(null) }
    var precioError by remember { mutableStateOf<String?>(null) }
    var fechaError by remember { mutableStateOf<String?>(null) }
    var tipoError by remember { mutableStateOf<String?>(null) }
    var marcaError by remember { mutableStateOf<String?>(null) }

    // Validar cupo en tiempo real
    val unidadesInt = unidades.toIntOrNull() ?: 0
    val excedeCupo = unidadesInt > cupoDisponible

    // Mostrar mensajes de UiState
    LaunchedEffect(uiState) {
        when (uiState) {
            is CompraViewModel.CompraUiState.Success -> {
                snackbarHostState.showSnackbar(
                    (uiState as CompraViewModel.CompraUiState.Success).message
                )
                viewModel.resetUiState()
                navController.popBackStack()
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

    CompraFormScreenContent(
        isEditing = isEditing,
        snackbarHostState = snackbarHostState,
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
        marca = marca,
        marcaError = marcaError,
        tienda = tienda,
        onCalibre1Change = { calibre1 = it; calibre1Error = null },
        onCalibre2Change = { calibre2 = it },
        onShowCalibre2Change = { showCalibre2 = it; if (!it) calibre2 = "" },
        onUnidadesChange = { if (it.all { c -> c.isDigit() }) unidades = it; unidadesError = null },
        onPrecioChange = { precio = it; precioError = null },
        onFechaChange = { fecha = it; fechaError = null },
        onTipoChange = { tipo = it; tipoError = null },
        onPesoChange = { peso = it },
        onMarcaChange = { marca = it; marcaError = null },
        onTiendaChange = { tienda = it },
        onBackClick = { navController.popBackStack() },
        onSaveClick = {
            // Validaciones
            var isValid = true
            if (calibre1.isBlank()) {
                calibre1Error = "Campo obligatorio"
                isValid = false
            }
            if (unidades.isBlank() || unidades.toIntOrNull() == null || unidades.toInt() <= 0) {
                unidadesError = "Introduce unidades válidas"
                isValid = false
            } else if (excedeCupo) {
                unidadesError = "Excede cupo disponible ($cupoDisponible)"
                isValid = false
            }
            if (precio.isBlank()) {
                precioError = "Campo obligatorio"
                isValid = false
            }
            if (fecha.isBlank()) {
                fechaError = "Campo obligatorio"
                isValid = false
            }
            if (tipo.isBlank()) {
                tipoError = "Campo obligatorio"
                isValid = false
            }
            if (marca.isBlank()) {
                marcaError = "Campo obligatorio"
                isValid = false
            }

            if (isValid) {
                val compra = Compra(
                    id = compraId ?: 0,
                    idPosGuia = guiaId,
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
                    viewModel.updateCompra(compra)
                } else {
                    viewModel.createCompra(compra)
                }
            }
        }
    )
}

/**
 * Contenido del formulario de Compra (Stateless).
 *
 * @since v3.0.0 (Compose Migration)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompraFormScreenContent(
    isEditing: Boolean,
    snackbarHostState: SnackbarHostState,
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
    marca: String,
    marcaError: String?,
    tienda: String,
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
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val datePickerDialog = remember {
        val calendar = Calendar.getInstance()
        if (fecha.isNotBlank()) {
            try {
                dateFormat.parse(fecha)?.let { calendar.time = it }
            } catch (e: Exception) { /* ignore */ }
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                onFechaChange(dateFormat.format(cal.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FormScreenTopBar(
                title = if (isEditing) "Editar compra" else "Nueva compra",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Save, contentDescription = "Guardar")
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Info de cupo disponible
            if (cupoDisponible < Int.MAX_VALUE) {
                Text(
                    text = "Cupo disponible: $cupoDisponible / $cupoTotal unidades",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (excedeCupo) LicenseExpired else LicenseValid
                )
            }

            // Calibre 1
            OutlinedTextField(
                value = calibre1,
                onValueChange = onCalibre1Change,
                label = { Text("Calibre") },
                isError = calibre1Error != null,
                supportingText = calibre1Error?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Checkbox segundo calibre
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = showCalibre2,
                    onCheckedChange = onShowCalibre2Change
                )
                Text("Segundo calibre")
            }

            // Calibre 2
            if (showCalibre2) {
                OutlinedTextField(
                    value = calibre2,
                    onValueChange = onCalibre2Change,
                    label = { Text("Segundo calibre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Marca
            OutlinedTextField(
                value = marca,
                onValueChange = onMarcaChange,
                label = { Text("Marca") },
                isError = marcaError != null,
                supportingText = marcaError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Tipo de munición
            OutlinedTextField(
                value = tipo,
                onValueChange = onTipoChange,
                label = { Text("Tipo de munición") },
                isError = tipoError != null,
                supportingText = tipoError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Peso
            OutlinedTextField(
                value = peso,
                onValueChange = onPesoChange,
                label = { Text("Peso (gr)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Unidades con validación de cupo
            OutlinedTextField(
                value = unidades,
                onValueChange = onUnidadesChange,
                label = { Text("Unidades") },
                isError = unidadesError != null || excedeCupo,
                supportingText = {
                    when {
                        unidadesError != null -> Text(unidadesError)
                        excedeCupo -> Text("Excede cupo disponible ($cupoDisponible)")
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
                label = { Text("Precio (€)") },
                isError = precioError != null,
                supportingText = precioError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            // Fecha
            OutlinedTextField(
                value = fecha,
                onValueChange = {},
                label = { Text("Fecha") },
                isError = fechaError != null,
                supportingText = fechaError?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() },
                enabled = false,
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                    }
                }
            )

            // Tienda
            OutlinedTextField(
                value = tienda,
                onValueChange = onTiendaChange,
                label = { Text("Tienda") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

private fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(Calendar.getInstance().time)
}

@Preview(showBackground = true)
@Composable
private fun CompraFormScreenContentPreview() {
    MunicionTheme {
        CompraFormScreenContent(
            isEditing = false,
            snackbarHostState = SnackbarHostState(),
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
            marca = "Federal",
            marcaError = null,
            tienda = "Armería Local",
            onCalibre1Change = {},
            onCalibre2Change = {},
            onShowCalibre2Change = {},
            onUnidadesChange = {},
            onPrecioChange = {},
            onFechaChange = {},
            onTipoChange = {},
            onPesoChange = {},
            onMarcaChange = {},
            onTiendaChange = {},
            onBackClick = {},
            onSaveClick = {}
        )
    }
}
