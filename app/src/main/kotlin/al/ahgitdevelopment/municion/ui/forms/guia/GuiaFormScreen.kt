package al.ahgitdevelopment.municion.ui.forms.guia

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.ui.components.imagepicker.ImagePickerWithCamera
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.collectLatest

/**
 * Pantalla del formulario de Guía - Arquitectura MVI.
 * 
 * Composable principal que:
 * - Consume estado del ViewModel
 * - Envía eventos al ViewModel
 * - Maneja efectos secundarios (navegación, snackbars)
 *
 * @param guia Guía a editar (null para crear nueva)
 * @param tipoLicencia Tipo de licencia seleccionado
 * @param navController Controlador de navegación
 * @param snackbarHostState Estado del snackbar
 * @param onRegisterSaveCallback Callback para registrar función de guardado
 * @param viewModel ViewModel del formulario
 *
 * @since v3.3.0 (Form Architecture Refactor)
 */
@Composable
fun GuiaFormScreen(
    guia: Guia?,
    tipoLicencia: String,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onRegisterSaveCallback: ((() -> Unit)?) -> Unit,
    viewModel: GuiaFormViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Inicializar formulario
    LaunchedEffect(guia?.id) {
        viewModel.initialize(guia, tipoLicencia)
    }
    
    // Manejar efectos secundarios
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is GuiaFormEffect.NavigateBack -> navController.popBackStack()
                is GuiaFormEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is GuiaFormEffect.ShowError -> snackbarHostState.showSnackbar("Error: ${effect.message}")
            }
        }
    }
    
    // Manejar estados de UI
    LaunchedEffect(uiState) {
        when (uiState) {
            is GuiaFormUiState.Success -> {
                val message = (uiState as GuiaFormUiState.Success).message
                snackbarHostState.showSnackbar(message)
                viewModel.resetUiState()
            }
            is GuiaFormUiState.Error -> {
                val message = (uiState as GuiaFormUiState.Error).message
                snackbarHostState.showSnackbar("Error: $message")
                viewModel.resetUiState()
            }
            else -> {}
        }
    }
    
    // Registrar función de guardado
    DisposableEffect(Unit) {
        onRegisterSaveCallback { viewModel.onEvent(GuiaFormEvent.Save) }
        onDispose { onRegisterSaveCallback(null) }
    }
    
    // Obtener datos de configuración
    val tiposArma = remember(tipoLicencia) {
        getWeaponTypesForLicense(context, tipoLicencia)
    }
    val calibres = context.resources.getStringArray(R.array.calibres).toList()
    
    // Estado de carga
    val isUploading = uiState is GuiaFormUiState.Uploading
    val uploadProgress = (uiState as? GuiaFormUiState.Uploading)?.progress ?: 0f
    
    GuiaFormContent(
        state = formState,
        tiposArma = tiposArma,
        calibres = calibres,
        isUploading = isUploading,
        uploadProgress = uploadProgress,
        onEvent = viewModel::onEvent
    )
}

/**
 * Contenido del formulario (Stateless).
 * 
 * Recibe todo el estado como parámetro y emite eventos.
 * Fácil de testear y previsualizar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuiaFormContent(
    state: GuiaFormState,
    tiposArma: List<String>,
    calibres: List<String>,
    isUploading: Boolean,
    uploadProgress: Float,
    onEvent: (GuiaFormEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tipo de arma
        if (tiposArma.isNotEmpty()) {
            DropdownField(
                label = stringResource(R.string.label_weapon_type),
                selectedIndex = state.tipoArma,
                options = tiposArma,
                onSelectionChange = { onEvent(GuiaFormEvent.TipoArmaChanged(it)) }
            )
        }
        
        // Marca
        FormTextField(
            value = state.marca,
            onValueChange = { onEvent(GuiaFormEvent.MarcaChanged(it)) },
            label = stringResource(R.string.marca),
            error = state.marcaError
        )
        
        // Modelo
        FormTextField(
            value = state.modelo,
            onValueChange = { onEvent(GuiaFormEvent.ModeloChanged(it)) },
            label = stringResource(R.string.modelo),
            error = state.modeloError
        )
        
        // Apodo
        FormTextField(
            value = state.apodo,
            onValueChange = { onEvent(GuiaFormEvent.ApodoChanged(it)) },
            label = stringResource(R.string.label_nickname),
            error = state.apodoError
        )
        
        // Calibre 1
        AutoCompleteField(
            value = state.calibre1,
            onValueChange = { onEvent(GuiaFormEvent.Calibre1Changed(it)) },
            label = stringResource(R.string.calibre),
            suggestions = calibres,
            error = state.calibre1Error
        )
        
        // Checkbox segundo calibre
        FormCheckbox(
            checked = state.showCalibre2,
            onCheckedChange = { onEvent(GuiaFormEvent.ShowCalibre2Changed(it)) },
            label = stringResource(R.string.check_segundo_calibre)
        )
        
        // Calibre 2
        if (state.showCalibre2) {
            AutoCompleteField(
                value = state.calibre2,
                onValueChange = { onEvent(GuiaFormEvent.Calibre2Changed(it)) },
                label = stringResource(R.string.calibre2),
                suggestions = calibres,
                error = null
            )
        }
        
        // Número de guía
        FormTextField(
            value = state.numGuia,
            onValueChange = { onEvent(GuiaFormEvent.NumGuiaChanged(it)) },
            label = stringResource(R.string.label_guide_number),
            error = state.numGuiaError
        )
        
        // Número de arma
        FormTextField(
            value = state.numArma,
            onValueChange = { onEvent(GuiaFormEvent.NumArmaChanged(it)) },
            label = stringResource(R.string.label_weapon_number),
            error = state.numArmaError
        )
        
        // Checkbox cupo personalizado
        FormCheckbox(
            checked = state.customCupo,
            onCheckedChange = { onEvent(GuiaFormEvent.CustomCupoChanged(it)) },
            label = stringResource(R.string.label_custom_quota)
        )
        
        // Cupo
        FormTextField(
            value = state.cupo,
            onValueChange = { onEvent(GuiaFormEvent.CupoChanged(it)) },
            label = stringResource(R.string.label_annual_quota),
            error = state.cupoError,
            enabled = state.customCupo,
            keyboardType = KeyboardType.Number
        )
        
        // Gastado (solo en edición)
        if (state.isEditing) {
            FormTextField(
                value = state.gastado,
                onValueChange = { onEvent(GuiaFormEvent.GastadoChanged(it)) },
                label = stringResource(R.string.label_spent_ammunition),
                keyboardType = KeyboardType.Number
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Selector de imagen con cámara y galería
        ImagePickerWithCamera(
            currentImageUrl = state.currentImageUrl,
            isUploading = isUploading,
            uploadProgress = uploadProgress,
            onImageSelected = { uri -> onEvent(GuiaFormEvent.ImageSelected(uri)) },
            onImageRemoved = { onEvent(GuiaFormEvent.ImageRemoved) }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
    }
}

// ============== COMPONENTES REUTILIZABLES ==============

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            capitalization = if (keyboardType == KeyboardType.Text) 
                KeyboardCapitalization.Sentences else KeyboardCapitalization.None,
            keyboardType = keyboardType
        )
    )
}

@Composable
private fun FormCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    selectedIndex: Int,
    options: List<String>,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoCompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    error: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val filteredSuggestions = suggestions.filter { it.contains(value, ignoreCase = true) }.take(10)
    
    ExposedDropdownMenuBox(
        expanded = expanded && filteredSuggestions.isNotEmpty(),
        onExpandedChange = { expanded = it },
        modifier = modifier
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
                .fillMaxWidth()
                .menuAnchor(),
            singleLine = true
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

// ============== UTILIDADES ==============

private fun getWeaponTypesForLicense(
    context: android.content.Context,
    tipoLicencia: String
): List<String> {
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

// ============== PREVIEWS ==============

@Preview(showBackground = true)
@Composable
private fun GuiaFormContentPreview() {
    MunicionTheme {
        GuiaFormContent(
            state = GuiaFormState(),
            tiposArma = listOf("Pistola", "Revolver", "Escopeta"),
            calibres = listOf("9mm Para", "12/70", ".22 LR"),
            isUploading = false,
            uploadProgress = 0f,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GuiaFormContentEditingPreview() {
    MunicionTheme {
        GuiaFormContent(
            state = GuiaFormState(
                marca = "Glock",
                modelo = "17",
                apodo = "Mi pistola",
                calibre1 = "9mm Para",
                numGuia = "12345",
                numArma = "ABC123",
                cupo = "100",
                isEditing = true
            ),
            tiposArma = listOf("Pistola", "Revolver", "Escopeta"),
            calibres = listOf("9mm Para", "12/70", ".22 LR"),
            isUploading = false,
            uploadProgress = 0f,
            onEvent = {}
        )
    }
}
