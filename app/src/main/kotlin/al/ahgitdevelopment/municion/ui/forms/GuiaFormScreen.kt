package al.ahgitdevelopment.municion.ui.forms

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.Alignment
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
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.GuiaViewModel

/**
 * Contenido del formulario de Guia para Single Scaffold Architecture.
 *
 * NO contiene Scaffold, TopBar ni FAB - estos estan en MainScreen.
 * Registra su funcion de guardado con MainScreen mediante onRegisterSaveCallback.
 *
 * @param tipoLicencia Tipo de licencia seleccionado
 * @param guiaId ID de guia a editar (null para nueva)
 * @param navController Controlador de navegacion
 * @param snackbarHostState Estado del snackbar compartido desde MainScreen
 * @param onRegisterSaveCallback Callback para registrar funcion de guardado con MainScreen
 * @param viewModel ViewModel de Guias
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@Composable
fun GuiaFormContent(
    tipoLicencia: String,
    guiaId: Int?,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onRegisterSaveCallback: ((() -> Unit)?) -> Unit,
    viewModel: GuiaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val guias by viewModel.guias.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val isEditing = guiaId != null

    // Cargar guia existente si estamos editando
    val existingGuia = remember(guiaId, guias) {
        guiaId?.let { id -> guias.find { it.id == id } }
    }

    // Form state
    var marca by rememberSaveable { mutableStateOf(existingGuia?.marca ?: "") }
    var modelo by rememberSaveable { mutableStateOf(existingGuia?.modelo ?: "") }
    var apodo by rememberSaveable { mutableStateOf(existingGuia?.apodo ?: "") }
    var tipoArma by rememberSaveable { mutableIntStateOf(existingGuia?.tipoArma ?: 0) }
    var calibre1 by rememberSaveable { mutableStateOf(existingGuia?.calibre1 ?: "") }
    var calibre2 by rememberSaveable { mutableStateOf(existingGuia?.calibre2 ?: "") }
    var showCalibre2 by rememberSaveable { mutableStateOf(!existingGuia?.calibre2.isNullOrBlank()) }
    var numGuia by rememberSaveable { mutableStateOf(existingGuia?.numGuia ?: "") }
    var numArma by rememberSaveable { mutableStateOf(existingGuia?.numArma ?: "") }
    var cupo by rememberSaveable { mutableStateOf(existingGuia?.cupo?.toString() ?: "") }
    var gastado by rememberSaveable { mutableStateOf(existingGuia?.gastado?.toString() ?: "0") }
    var customCupo by rememberSaveable { mutableStateOf(false) }

    // Error states
    var marcaError by remember { mutableStateOf<String?>(null) }
    var modeloError by remember { mutableStateOf<String?>(null) }
    var apodoError by remember { mutableStateOf<String?>(null) }
    var calibre1Error by remember { mutableStateOf<String?>(null) }
    var numGuiaError by remember { mutableStateOf<String?>(null) }
    var numArmaError by remember { mutableStateOf<String?>(null) }
    var cupoError by remember { mutableStateOf<String?>(null) }

    // Obtener tipos de arma segun licencia
    val tiposArma = remember(tipoLicencia) {
        getWeaponTypesForLicense(context, tipoLicencia)
    }

    // Calibres disponibles
    val calibres = context.resources.getStringArray(R.array.calibres).toList()

    // Funcion de guardado
    val saveFunction: () -> Unit = {
        // Validaciones
        var isValid = true
        if (marca.isBlank()) {
            marcaError = "Campo obligatorio"
            isValid = false
        }
        if (modelo.isBlank()) {
            modeloError = "Campo obligatorio"
            isValid = false
        }
        if (apodo.isBlank()) {
            apodoError = "Campo obligatorio"
            isValid = false
        }
        if (calibre1.isBlank()) {
            calibre1Error = "Campo obligatorio"
            isValid = false
        }
        if (numGuia.isBlank()) {
            numGuiaError = "Campo obligatorio"
            isValid = false
        }
        if (numArma.isBlank()) {
            numArmaError = "Campo obligatorio"
            isValid = false
        }
        if (cupo.isBlank() || cupo.toIntOrNull() == null || cupo.toInt() <= 0) {
            cupoError = "Introduce un cupo valido"
            isValid = false
        }

        if (isValid) {
            // Obtener el tipo de licencia como Int
            val tipoLicenciaInt = getLicenciaTypeFromString(context, tipoLicencia)

            val guia = Guia(
                id = guiaId ?: 0,
                tipoLicencia = tipoLicenciaInt,
                marca = marca,
                modelo = modelo,
                apodo = apodo,
                tipoArma = tipoArma,
                calibre1 = calibre1,
                calibre2 = if (showCalibre2) calibre2 else null,
                numGuia = numGuia,
                numArma = numArma,
                cupo = cupo.toInt(),
                gastado = gastado.toIntOrNull() ?: 0
            )
            if (isEditing) {
                viewModel.updateGuia(guia)
            } else {
                viewModel.saveGuia(guia)
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

    // Actualizar cupo por defecto segun tipo de arma (si no es personalizado)
    LaunchedEffect(tipoArma, customCupo) {
        if (!customCupo && !isEditing) {
            cupo = getDefaultCupo(tiposArma.getOrElse(tipoArma) { "" }).toString()
        }
    }

    // Mostrar mensajes de UiState y navegar al exito
    LaunchedEffect(uiState) {
        when (uiState) {
            is GuiaViewModel.GuiaUiState.Success -> {
                val message = (uiState as GuiaViewModel.GuiaUiState.Success).message
                viewModel.resetUiState()
                navController.popBackStack()
                // Show snackbar in background - don't block navigation
                launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
            is GuiaViewModel.GuiaUiState.Error -> {
                snackbarHostState.showSnackbar(
                    "Error: ${(uiState as GuiaViewModel.GuiaUiState.Error).message}"
                )
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    GuiaFormFields(
        isEditing = isEditing,
        tiposArma = tiposArma,
        calibres = calibres,
        marca = marca,
        marcaError = marcaError,
        modelo = modelo,
        modeloError = modeloError,
        apodo = apodo,
        apodoError = apodoError,
        tipoArma = tipoArma,
        calibre1 = calibre1,
        calibre1Error = calibre1Error,
        calibre2 = calibre2,
        showCalibre2 = showCalibre2,
        numGuia = numGuia,
        numGuiaError = numGuiaError,
        numArma = numArma,
        numArmaError = numArmaError,
        cupo = cupo,
        cupoError = cupoError,
        customCupo = customCupo,
        gastado = gastado,
        onMarcaChange = { marca = it; marcaError = null },
        onModeloChange = { modelo = it; modeloError = null },
        onApodoChange = { apodo = it; apodoError = null },
        onTipoArmaChange = { tipoArma = it },
        onCalibre1Change = { calibre1 = it; calibre1Error = null },
        onCalibre2Change = { calibre2 = it },
        onShowCalibre2Change = { showCalibre2 = it; if (!it) calibre2 = "" },
        onNumGuiaChange = { numGuia = it; numGuiaError = null },
        onNumArmaChange = { numArma = it; numArmaError = null },
        onCupoChange = { if (it.all { c -> c.isDigit() }) cupo = it; cupoError = null },
        onCustomCupoChange = { customCupo = it },
        onGastadoChange = { if (it.all { c -> c.isDigit() }) gastado = it }
    )
}

/**
 * Campos del formulario de Guia (Stateless).
 *
 * Sin Scaffold - solo los campos del formulario.
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuiaFormFields(
    isEditing: Boolean,
    tiposArma: List<String>,
    calibres: List<String>,
    marca: String,
    marcaError: String?,
    modelo: String,
    modeloError: String?,
    apodo: String,
    apodoError: String?,
    tipoArma: Int,
    calibre1: String,
    calibre1Error: String?,
    calibre2: String,
    showCalibre2: Boolean,
    numGuia: String,
    numGuiaError: String?,
    numArma: String,
    numArmaError: String?,
    cupo: String,
    cupoError: String?,
    customCupo: Boolean,
    gastado: String,
    onMarcaChange: (String) -> Unit,
    onModeloChange: (String) -> Unit,
    onApodoChange: (String) -> Unit,
    onTipoArmaChange: (Int) -> Unit,
    onCalibre1Change: (String) -> Unit,
    onCalibre2Change: (String) -> Unit,
    onShowCalibre2Change: (Boolean) -> Unit,
    onNumGuiaChange: (String) -> Unit,
    onNumArmaChange: (String) -> Unit,
    onCupoChange: (String) -> Unit,
    onCustomCupoChange: (Boolean) -> Unit,
    onGastadoChange: (String) -> Unit,
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

        // Tipo de arma
        if (tiposArma.isNotEmpty()) {
            DropdownFieldGuia(
                label = "Tipo de arma",
                selectedIndex = tipoArma,
                options = tiposArma,
                onSelectionChange = onTipoArmaChange
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
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // Modelo
        OutlinedTextField(
            value = modelo,
            onValueChange = onModeloChange,
            label = { Text("Modelo") },
            isError = modeloError != null,
            supportingText = modeloError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // Apodo
        OutlinedTextField(
            value = apodo,
            onValueChange = onApodoChange,
            label = { Text("Apodo") },
            isError = apodoError != null,
            supportingText = apodoError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // Calibre 1
        AutoCompleteTextField(
            value = calibre1,
            onValueChange = onCalibre1Change,
            label = "Calibre",
            suggestions = calibres,
            error = calibre1Error
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
            AutoCompleteTextField(
                value = calibre2,
                onValueChange = onCalibre2Change,
                label = "Segundo calibre",
                suggestions = calibres,
                error = null
            )
        }

        // Numero de guia
        OutlinedTextField(
            value = numGuia,
            onValueChange = onNumGuiaChange,
            label = { Text("Numero de guia") },
            isError = numGuiaError != null,
            supportingText = numGuiaError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // Numero de arma
        OutlinedTextField(
            value = numArma,
            onValueChange = onNumArmaChange,
            label = { Text("Numero de arma") },
            isError = numArmaError != null,
            supportingText = numArmaError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // Checkbox cupo personalizado
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = customCupo,
                onCheckedChange = onCustomCupoChange
            )
            Text("Cupo personalizado")
        }

        // Cupo
        OutlinedTextField(
            value = cupo,
            onValueChange = onCupoChange,
            label = { Text("Cupo anual") },
            isError = cupoError != null,
            supportingText = cupoError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = customCupo,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Gastado (solo en edicion)
        if (isEditing) {
            OutlinedTextField(
                value = gastado,
                onValueChange = onGastadoChange,
                label = { Text("Municion gastada") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * Campo de texto con autocompletado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoCompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }
    val filteredSuggestions = suggestions.filter { it.contains(value, ignoreCase = true) }.take(5)

    ExposedDropdownMenuBox(
        expanded = expanded && filteredSuggestions.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth(),
//                .menuAnchor(),
            singleLine = true,
        )
        ExposedDropdownMenu(
            expanded = expanded && filteredSuggestions.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            filteredSuggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = {
                        onValueChange(suggestion)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Campo de seleccion desplegable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownFieldGuia(
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
 * Obtiene los tipos de arma disponibles segun el tipo de licencia
 */
private fun getWeaponTypesForLicense(context: android.content.Context, tipoLicencia: String): List<String> {
    return try {
        val licenseName = tipoLicencia.split(" ").firstOrNull() ?: return emptyList()
        val resourceId = context.resources.getIdentifier(licenseName, "array", context.packageName)
        if (resourceId != 0) {
            context.resources.getStringArray(resourceId).toList()
        } else {
            listOf("Pistola", "Revolver", "Escopeta", "Rifle")
        }
    } catch (e: Exception) {
        listOf("Pistola", "Revolver", "Escopeta", "Rifle")
    }
}

/**
 * Obtiene el cupo por defecto segun el tipo de arma
 */
private fun getDefaultCupo(tipoArma: String): Int {
    return when (tipoArma.lowercase()) {
        "pistola", "gun" -> 100
        "revolver" -> 100
        "escopeta", "shotgun" -> 5000
        "rifle" -> 1000
        "avancarga" -> 1000
        else -> 100
    }
}

/**
 * Convierte el nombre de licencia a su indice
 */
private fun getLicenciaTypeFromString(context: android.content.Context, tipoLicencia: String): Int {
    return try {
        val tipos = context.resources.getStringArray(R.array.tipo_licencias)
        tipos.indexOfFirst { it.equals(tipoLicencia, ignoreCase = true) }.takeIf { it >= 0 } ?: 0
    } catch (e: Exception) {
        0
    }
}

@Preview(showBackground = true)
@Composable
private fun GuiaFormFieldsPreview() {
    MunicionTheme {
        GuiaFormFields(
            isEditing = false,
            tiposArma = listOf("Pistola", "Revolver", "Escopeta"),
            calibres = listOf("9mm Para", "12/70", ".22 LR"),
            marca = "",
            marcaError = null,
            modelo = "",
            modeloError = null,
            apodo = "",
            apodoError = null,
            tipoArma = 0,
            calibre1 = "",
            calibre1Error = null,
            calibre2 = "",
            showCalibre2 = false,
            numGuia = "",
            numGuiaError = null,
            numArma = "",
            numArmaError = null,
            cupo = "100",
            cupoError = null,
            customCupo = false,
            gastado = "0",
            onMarcaChange = {},
            onModeloChange = {},
            onApodoChange = {},
            onTipoArmaChange = {},
            onCalibre1Change = {},
            onCalibre2Change = {},
            onShowCalibre2Change = {},
            onNumGuiaChange = {},
            onNumArmaChange = {},
            onCupoChange = {},
            onCustomCupoChange = {},
            onGastadoChange = {}
        )
    }
}
