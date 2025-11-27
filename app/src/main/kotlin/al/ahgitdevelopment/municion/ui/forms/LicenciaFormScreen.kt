package al.ahgitdevelopment.municion.ui.forms

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.LicenciaViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Contenido del formulario de Licencia para Single Scaffold Architecture.
 *
 * NO contiene Scaffold, TopBar ni FAB - estos están en MainScreen.
 * Registra su función de guardado con MainScreen mediante onRegisterSaveCallback.
 *
 * @param licencia Objeto completo para editar (null para nueva)
 * @param navController Controlador de navegación
 * @param snackbarHostState Estado del snackbar compartido desde MainScreen
 * @param onRegisterSaveCallback Callback para registrar función de guardado con MainScreen
 * @param viewModel ViewModel de Licencias
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@Composable
fun LicenciaFormContent(
    licencia: Licencia?,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onRegisterSaveCallback: ((() -> Unit)?) -> Unit,
    viewModel: LicenciaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val isEditing = licencia != null

    // Form state - inicializar directamente desde el objeto
    var tipoLicencia by rememberSaveable { mutableIntStateOf(licencia?.tipo ?: 0) }
    var numLicencia by rememberSaveable { mutableStateOf(licencia?.numLicencia ?: "") }
    var fechaExpedicion by rememberSaveable { mutableStateOf(licencia?.fechaExpedicion ?: "") }
    var fechaCaducidad by rememberSaveable { mutableStateOf(licencia?.fechaCaducidad ?: "") }
    var numAbonado by rememberSaveable { mutableStateOf(licencia?.numAbonado?.takeIf { it >= 0 }?.toString() ?: "") }
    var numSeguro by rememberSaveable { mutableStateOf(licencia?.numSeguro ?: "") }
    var autonomia by rememberSaveable { mutableIntStateOf(licencia?.autonomia?.takeIf { it >= 0 } ?: 0) }
    var tipoPermisoConducir by rememberSaveable { mutableIntStateOf(licencia?.tipoPermisoConduccion?.takeIf { it >= 0 } ?: 0) }
    var edad by rememberSaveable { mutableStateOf(licencia?.edad?.toString() ?: "") }
    var escala by rememberSaveable { mutableIntStateOf(licencia?.escala?.takeIf { it >= 0 } ?: 0) }
    var categoria by rememberSaveable { mutableIntStateOf(licencia?.categoria?.takeIf { it >= 0 } ?: 0) }

    // Error states
    var numLicenciaError by remember { mutableStateOf<String?>(null) }
    var fechaExpedicionError by remember { mutableStateOf<String?>(null) }
    var numSeguroError by remember { mutableStateOf<String?>(null) }
    var edadError by remember { mutableStateOf<String?>(null) }

    // Arrays de recursos
    val tiposLicencia = context.resources.getStringArray(R.array.tipo_licencias).toList()
    val tiposPermisoConducir = context.resources.getStringArray(R.array.tipo_permiso_conducir).toList()
    val autonomias = context.resources.getStringArray(R.array.ccaa).toList()
    val escalas = context.resources.getStringArray(R.array.tipo_escala).toList()
    val categorias = context.resources.getStringArray(R.array.categorias).toList()

    // Determinar visibilidad de campos según tipo de licencia
    val showEscala = tipoLicencia == 0
    val showFechaCaducidad = tipoLicencia != 0
    val showNumAbonado = tipoLicencia in 9..11
    val showNumSeguro = tipoLicencia in 9..10
    val showAutonomia = tipoLicencia in 9..11
    val showPermisoConducir = tipoLicencia == 12
    val showEdad = tipoLicencia == 12
    val showCategoria = tipoLicencia == 11

    // Función de guardado
    val saveFunction: () -> Unit = {
        // Validaciones
        var isValid = true
        if (numLicencia.isBlank()) {
            numLicenciaError = "Introduce el número de licencia"
            isValid = false
        }
        if (fechaExpedicion.isBlank()) {
            fechaExpedicionError = "Introduce la fecha de expedición"
            isValid = false
        }
        if (showNumSeguro && numSeguro.isBlank()) {
            numSeguroError = "Introduce el número de póliza"
            isValid = false
        }
        if (showEdad && edad.isBlank()) {
            edadError = "Introduce tu edad"
            isValid = false
        }

        if (isValid) {
            val licenciaToSave = Licencia(
                id = licencia?.id ?: 0,
                tipo = tipoLicencia,
                nombre = tiposLicencia.getOrNull(tipoLicencia),
                tipoPermisoConduccion = if (showPermisoConducir) tipoPermisoConducir else -1,
                edad = edad.toIntOrNull() ?: 30,
                fechaExpedicion = fechaExpedicion,
                fechaCaducidad = fechaCaducidad.ifBlank { "31/12/3000" },
                numLicencia = numLicencia,
                numAbonado = if (showNumAbonado) numAbonado.toIntOrNull() ?: -1 else -1,
                numSeguro = if (showNumSeguro) numSeguro else null,
                autonomia = if (showAutonomia) autonomia else -1,
                escala = if (showEscala) escala else -1,
                categoria = if (showCategoria) categoria else -1
            )
            if (isEditing) {
                viewModel.updateLicencia(licenciaToSave)
            } else {
                viewModel.saveLicencia(licenciaToSave)
            }
        }
    }

    // Registrar función de guardado con MainScreen
    DisposableEffect(Unit) {
        onRegisterSaveCallback(saveFunction)
        onDispose {
            onRegisterSaveCallback(null)
        }
    }

    // Actualizar fecha de caducidad automáticamente
    LaunchedEffect(fechaExpedicion, tipoLicencia, tipoPermisoConducir, edad) {
        fechaCaducidad = calculateFechaCaducidad(fechaExpedicion, tipoLicencia, tipoPermisoConducir, edad)
    }

    // Mostrar mensajes de UiState y navegar al éxito
    LaunchedEffect(uiState) {
        when (uiState) {
            is LicenciaViewModel.LicenciaUiState.Success -> {
                val message = (uiState as LicenciaViewModel.LicenciaUiState.Success).message
                viewModel.resetUiState()
                navController.popBackStack()
                // Show snackbar in background - don't block navigation
                launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
            is LicenciaViewModel.LicenciaUiState.Error -> {
                snackbarHostState.showSnackbar(
                    "Error: ${(uiState as LicenciaViewModel.LicenciaUiState.Error).message}"
                )
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    LicenciaFormFields(
        tipoLicencia = tipoLicencia,
        tiposLicencia = tiposLicencia,
        numLicencia = numLicencia,
        numLicenciaError = numLicenciaError,
        fechaExpedicion = fechaExpedicion,
        fechaExpedicionError = fechaExpedicionError,
        fechaCaducidad = fechaCaducidad,
        numAbonado = numAbonado,
        numSeguro = numSeguro,
        numSeguroError = numSeguroError,
        autonomia = autonomia,
        autonomias = autonomias,
        tipoPermisoConducir = tipoPermisoConducir,
        tiposPermisoConducir = tiposPermisoConducir,
        edad = edad,
        edadError = edadError,
        escala = escala,
        escalas = escalas,
        categoria = categoria,
        categorias = categorias,
        showEscala = showEscala,
        showFechaCaducidad = showFechaCaducidad,
        showNumAbonado = showNumAbonado,
        showNumSeguro = showNumSeguro,
        showAutonomia = showAutonomia,
        showPermisoConducir = showPermisoConducir,
        showEdad = showEdad,
        showCategoria = showCategoria,
        onTipoLicenciaChange = { tipoLicencia = it },
        onNumLicenciaChange = { numLicencia = it; numLicenciaError = null },
        onFechaExpedicionChange = { fechaExpedicion = it; fechaExpedicionError = null },
        onNumAbonadoChange = { numAbonado = it },
        onNumSeguroChange = { numSeguro = it; numSeguroError = null },
        onAutonomiaChange = { autonomia = it },
        onTipoPermisoConducirChange = { tipoPermisoConducir = it },
        onEdadChange = { edad = it; edadError = null },
        onEscalaChange = { escala = it },
        onCategoriaChange = { categoria = it }
    )
}

/**
 * Campos del formulario de Licencia (Stateless).
 *
 * Sin Scaffold - solo los campos del formulario.
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenciaFormFields(
    tipoLicencia: Int,
    tiposLicencia: List<String>,
    numLicencia: String,
    numLicenciaError: String?,
    fechaExpedicion: String,
    fechaExpedicionError: String?,
    fechaCaducidad: String,
    numAbonado: String,
    numSeguro: String,
    numSeguroError: String?,
    autonomia: Int,
    autonomias: List<String>,
    tipoPermisoConducir: Int,
    tiposPermisoConducir: List<String>,
    edad: String,
    edadError: String?,
    escala: Int,
    escalas: List<String>,
    categoria: Int,
    categorias: List<String>,
    showEscala: Boolean,
    showFechaCaducidad: Boolean,
    showNumAbonado: Boolean,
    showNumSeguro: Boolean,
    showAutonomia: Boolean,
    showPermisoConducir: Boolean,
    showEdad: Boolean,
    showCategoria: Boolean,
    onTipoLicenciaChange: (Int) -> Unit,
    onNumLicenciaChange: (String) -> Unit,
    onFechaExpedicionChange: (String) -> Unit,
    onNumAbonadoChange: (String) -> Unit,
    onNumSeguroChange: (String) -> Unit,
    onAutonomiaChange: (Int) -> Unit,
    onTipoPermisoConducirChange: (Int) -> Unit,
    onEdadChange: (String) -> Unit,
    onEscalaChange: (Int) -> Unit,
    onCategoriaChange: (Int) -> Unit,
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

        // Tipo de licencia (Dropdown)
        DropdownField(
            label = "Tipo de licencia",
            selectedIndex = tipoLicencia,
            options = tiposLicencia,
            onSelectionChange = onTipoLicenciaChange
        )

        // Número de licencia
        OutlinedTextField(
            value = numLicencia,
            onValueChange = onNumLicenciaChange,
            label = { Text(if (tipoLicencia == 12) "DNI" else "Número de licencia") },
            isError = numLicenciaError != null,
            supportingText = numLicenciaError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // Fecha de expedición
        DateField(
            label = "Fecha de expedición",
            value = fechaExpedicion,
            error = fechaExpedicionError,
            onValueChange = onFechaExpedicionChange
        )

        // Fecha de caducidad (solo lectura, calculada automáticamente)
        if (showFechaCaducidad) {
            OutlinedTextField(
                value = fechaCaducidad,
                onValueChange = {},
                label = { Text("Fecha de caducidad") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                singleLine = true
            )
        }

        // Escala (solo para Licencia A)
        if (showEscala) {
            DropdownField(
                label = "Escala",
                selectedIndex = escala,
                options = escalas,
                onSelectionChange = onEscalaChange
            )
        }

        // Permiso de conducir (solo para tipo 12)
        if (showPermisoConducir) {
            DropdownField(
                label = "Tipo de permiso",
                selectedIndex = tipoPermisoConducir,
                options = tiposPermisoConducir,
                onSelectionChange = onTipoPermisoConducirChange
            )
        }

        // Edad (solo para permiso de conducir)
        if (showEdad) {
            OutlinedTextField(
                value = edad,
                onValueChange = onEdadChange,
                label = { Text("Edad") },
                isError = edadError != null,
                supportingText = edadError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Número de abonado
        if (showNumAbonado) {
            OutlinedTextField(
                value = numAbonado,
                onValueChange = onNumAbonadoChange,
                label = { Text("Número de abonado") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Número de póliza de seguro
        if (showNumSeguro) {
            OutlinedTextField(
                value = numSeguro,
                onValueChange = onNumSeguroChange,
                label = { Text("Número de póliza") },
                isError = numSeguroError != null,
                supportingText = numSeguroError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
        }

        // Comunidad Autónoma
        if (showAutonomia) {
            DropdownField(
                label = "Comunidad Autónoma",
                selectedIndex = autonomia,
                options = autonomias,
                onSelectionChange = onAutonomiaChange
            )
        }

        // Categoría (solo para Federativa)
        if (showCategoria) {
            DropdownField(
                label = "Categoría",
                selectedIndex = categoria,
                options = categorias,
                onSelectionChange = onCategoriaChange
            )
        }

        Spacer(modifier = Modifier.height(80.dp)) // Espacio para FAB
    }
}

/**
 * Campo de fecha con DatePicker
 */
@Composable
private fun DateField(
    label: String,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit
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

    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
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
}

/**
 * Campo de selección desplegable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    selectedIndex: Int,
    options: List<String>,
    onSelectionChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = options.getOrElse(selectedIndex) { "" },
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelectionChange(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Calcula la fecha de caducidad según el tipo de licencia
 */
private fun calculateFechaCaducidad(
    fechaExpedicion: String,
    tipoLicencia: Int,
    tipoPermisoConducir: Int,
    edadStr: String
): String {
    if (fechaExpedicion.isBlank()) return ""

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance()

    try {
        calendar.time = dateFormat.parse(fechaExpedicion) ?: return ""
    } catch (e: Exception) {
        return ""
    }

    val edad = edadStr.toIntOrNull() ?: 30

    when (tipoLicencia) {
        0 -> { // Licencia A - No caduca
            calendar.set(3000, Calendar.DECEMBER, 31)
        }
        1, 5 -> { // Licencia B, F - 3 años
            calendar.add(Calendar.YEAR, 3)
        }
        2, 3, 4, 6, 7, 8 -> { // Licencia C, D, E, AE, AER, Libre Coleccionista - 5 años
            calendar.add(Calendar.YEAR, 5)
        }
        9, 10, 11 -> { // Autonómica Caza, Pesca, Federativa - Fin de año
            calendar.set(calendar.get(Calendar.YEAR), Calendar.DECEMBER, 31)
        }
        12 -> { // Permiso de conducir
            if (edad < 65) {
                when (tipoPermisoConducir) {
                    in 0..4 -> calendar.add(Calendar.YEAR, 10)
                    else -> calendar.add(Calendar.YEAR, 5)
                }
            } else {
                when (tipoPermisoConducir) {
                    in 0..4 -> calendar.add(Calendar.YEAR, 5)
                    else -> calendar.add(Calendar.YEAR, 3)
                }
            }
        }
    }

    return dateFormat.format(calendar.time)
}

@Preview(showBackground = true)
@Composable
private fun LicenciaFormFieldsPreview() {
    MunicionTheme {
        LicenciaFormFields(
            tipoLicencia = 1,
            tiposLicencia = listOf("Licencia A", "Licencia B", "Licencia C"),
            numLicencia = "",
            numLicenciaError = null,
            fechaExpedicion = "01/01/2024",
            fechaExpedicionError = null,
            fechaCaducidad = "01/01/2027",
            numAbonado = "",
            numSeguro = "",
            numSeguroError = null,
            autonomia = 0,
            autonomias = listOf("Andalucía", "Madrid", "Cataluña"),
            tipoPermisoConducir = 0,
            tiposPermisoConducir = listOf("AM", "A1", "A2", "A", "B"),
            edad = "",
            edadError = null,
            escala = 0,
            escalas = listOf("1ª", "2ª", "3ª"),
            categoria = 0,
            categorias = listOf("Cat A", "Cat B"),
            showEscala = false,
            showFechaCaducidad = true,
            showNumAbonado = false,
            showNumSeguro = false,
            showAutonomia = false,
            showPermisoConducir = false,
            showEdad = false,
            showCategoria = false,
            onTipoLicenciaChange = {},
            onNumLicenciaChange = {},
            onFechaExpedicionChange = {},
            onNumAbonadoChange = {},
            onNumSeguroChange = {},
            onAutonomiaChange = {},
            onTipoPermisoConducirChange = {},
            onEdadChange = {},
            onEscalaChange = {},
            onCategoriaChange = {}
        )
    }
}
