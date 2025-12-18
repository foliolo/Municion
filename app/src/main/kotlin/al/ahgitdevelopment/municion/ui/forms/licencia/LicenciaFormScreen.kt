package al.ahgitdevelopment.municion.ui.forms.licencia

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.ui.components.DatePickerField
import al.ahgitdevelopment.municion.ui.components.imagepicker.ImagePickerWithCamera
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
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
import kotlinx.coroutines.launch

/**
 * Pantalla del formulario de Licencia con arquitectura Single Scaffold.
 *
 * Características:
 * - Usa LicenciaFormViewModel para gestión de estado
 * - Patrón MVI con eventos y efectos
 * - Soporte de imagen con cámara/galería
 * - NO contiene Scaffold/TopBar/FAB (están en MainScreen)
 *
 * @param licencia Licencia existente para editar (null = nueva)
 * @param navController Controlador de navegación
 * @param snackbarHostState Estado del snackbar compartido
 * @param onRegisterSaveCallback Callback para registrar función de guardado
 * @param viewModel ViewModel del formulario
 *
 * @since v3.2.2 (Form Architecture Refactor)
 */
@Composable
fun LicenciaFormScreen(
    licencia: Licencia?,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onRegisterSaveCallback: ((() -> Unit)?) -> Unit,
    viewModel: LicenciaFormViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Inicializar ViewModel con la licencia
    LaunchedEffect(licencia) {
        viewModel.initialize(licencia)
    }

    // Manejar efectos (navegación, snackbar)
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LicenciaFormEffect.NavigateBack -> navController.popBackStack()
                is LicenciaFormEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is LicenciaFormEffect.ShowError -> snackbarHostState.showSnackbar("Error: ${effect.message}")
            }
        }
    }

    // Manejar estados de UI
    LaunchedEffect(uiState) {
        when (uiState) {
            is LicenciaFormUiState.Success -> {
                val message = (uiState as LicenciaFormUiState.Success).message
                snackbarHostState.showSnackbar(message)
                viewModel.resetUiState()
            }
            is LicenciaFormUiState.Error -> {
                val message = (uiState as LicenciaFormUiState.Error).message
                snackbarHostState.showSnackbar("Error: $message")
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    // Registrar función de guardado
    DisposableEffect(Unit) {
        onRegisterSaveCallback { viewModel.onEvent(LicenciaFormEvent.Save) }
        onDispose { onRegisterSaveCallback(null) }
    }

    // Obtener datos de configuración
    val tiposLicencia = context.resources.getStringArray(R.array.tipo_licencias).toList()
    val tiposPermisoConducir = context.resources.getStringArray(R.array.tipo_permiso_conducir).toList()
    val autonomias = context.resources.getStringArray(R.array.ccaa).toList()
    val escalas = context.resources.getStringArray(R.array.tipo_escala).toList()
    val categorias = context.resources.getStringArray(R.array.categorias).toList()

    // Estado de carga
    val isUploading = uiState is LicenciaFormUiState.Uploading
    val uploadProgress = (uiState as? LicenciaFormUiState.Uploading)?.progress ?: 0f

    LicenciaFormContent(
        state = formState,
        tiposLicencia = tiposLicencia,
        tiposPermisoConducir = tiposPermisoConducir,
        autonomias = autonomias,
        escalas = escalas,
        categorias = categorias,
        isUploading = isUploading,
        uploadProgress = uploadProgress,
        onEvent = viewModel::onEvent
    )
}

/**
 * Contenido del formulario (Stateless).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LicenciaFormContent(
    state: LicenciaFormState,
    tiposLicencia: List<String>,
    tiposPermisoConducir: List<String>,
    autonomias: List<String>,
    escalas: List<String>,
    categorias: List<String>,
    isUploading: Boolean,
    uploadProgress: Float,
    onEvent: (LicenciaFormEvent) -> Unit,
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
            label = stringResource(R.string.label_license_type),
            selectedIndex = state.tipoLicencia,
            options = tiposLicencia,
            onSelectionChange = { onEvent(LicenciaFormEvent.TipoLicenciaChanged(it)) }
        )

        // Número de licencia
        OutlinedTextField(
            value = state.numLicencia,
            onValueChange = { onEvent(LicenciaFormEvent.NumLicenciaChanged(it)) },
            label = {
                Text(
                    if (state.tipoLicencia == 12) stringResource(R.string.lbl_num_dni)
                    else stringResource(R.string.label_dni_license_number)
                )
            },
            isError = state.numLicenciaError != null,
            supportingText = state.numLicenciaError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // Fecha de expedición
        DatePickerField(
            label = stringResource(R.string.label_issue_date),
            value = state.fechaExpedicion,
            error = state.fechaExpedicionError,
            onValueChange = { onEvent(LicenciaFormEvent.FechaExpedicionChanged(it)) }
        )

        // Fecha de caducidad (solo lectura, calculada automáticamente)
        if (state.showFechaCaducidad) {
            OutlinedTextField(
                value = state.fechaCaducidad,
                onValueChange = {},
                label = { Text(stringResource(R.string.label_expiry_date)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                singleLine = true
            )
        }

        // Escala (solo para Licencia A)
        if (state.showEscala) {
            DropdownField(
                label = stringResource(R.string.label_scale),
                selectedIndex = state.escala,
                options = escalas,
                onSelectionChange = { onEvent(LicenciaFormEvent.EscalaChanged(it)) }
            )
        }

        // Permiso de conducir (solo para tipo 12)
        if (state.showPermisoConducir) {
            DropdownField(
                label = stringResource(R.string.label_permit_type),
                selectedIndex = state.tipoPermisoConducir,
                options = tiposPermisoConducir,
                onSelectionChange = { onEvent(LicenciaFormEvent.TipoPermisoConducirChanged(it)) }
            )
        }

        // Edad (solo para permiso de conducir)
        if (state.showEdad) {
            OutlinedTextField(
                value = state.edad,
                onValueChange = { onEvent(LicenciaFormEvent.EdadChanged(it)) },
                label = { Text(stringResource(R.string.label_age)) },
                isError = state.edadError != null,
                supportingText = state.edadError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Número de abonado
        if (state.showNumAbonado) {
            OutlinedTextField(
                value = state.numAbonado,
                onValueChange = { onEvent(LicenciaFormEvent.NumAbonadoChanged(it)) },
                label = { Text(stringResource(R.string.label_subscriber_number)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Número de póliza de seguro
        if (state.showNumSeguro) {
            OutlinedTextField(
                value = state.numSeguro,
                onValueChange = { onEvent(LicenciaFormEvent.NumSeguroChanged(it)) },
                label = { Text(stringResource(R.string.label_policy_number)) },
                isError = state.numSeguroError != null,
                supportingText = state.numSeguroError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
        }

        // Comunidad Autónoma
        if (state.showAutonomia) {
            DropdownField(
                label = stringResource(R.string.label_autonomous_community),
                selectedIndex = state.autonomia,
                options = autonomias,
                onSelectionChange = { onEvent(LicenciaFormEvent.AutonomiaChanged(it)) }
            )
        }

        // Categoría (solo para Federativa)
        if (state.showCategoria) {
            DropdownField(
                label = stringResource(R.string.label_category),
                selectedIndex = state.categoria,
                options = categorias,
                onSelectionChange = { onEvent(LicenciaFormEvent.CategoriaChanged(it)) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selector de imagen con cámara y galería
        ImagePickerWithCamera(
            currentImageUrl = state.currentImageUrl,
            isUploading = isUploading,
            uploadProgress = uploadProgress,
            onImageSelected = { uri -> onEvent(LicenciaFormEvent.ImageSelected(uri)) },
            onImageRemoved = { onEvent(LicenciaFormEvent.ImageRemoved) },
            label = stringResource(R.string.label_license_photo)
        )

        Spacer(modifier = Modifier.height(80.dp)) // Espacio para FAB
    }
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

// ============== PREVIEWS ==============

@Preview(showBackground = true)
@Composable
private fun LicenciaFormContentPreview() {
    MunicionTheme {
        LicenciaFormContent(
            state = LicenciaFormState(),
            tiposLicencia = listOf("Licencia A", "Licencia B", "Licencia C"),
            tiposPermisoConducir = listOf("AM", "A1", "A2", "A", "B"),
            autonomias = listOf("Andalucía", "Madrid", "Cataluña"),
            escalas = listOf("1ª", "2ª", "3ª"),
            categorias = listOf("Cat A", "Cat B"),
            isUploading = false,
            uploadProgress = 0f,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LicenciaFormContentEditingPreview() {
    MunicionTheme {
        LicenciaFormContent(
            state = LicenciaFormState(
                tipoLicencia = 1,
                numLicencia = "B-12345678",
                fechaExpedicion = "01/01/2024",
                fechaCaducidad = "01/01/2029",
                isEditing = true
            ),
            tiposLicencia = listOf("Licencia A", "Licencia B", "Licencia C"),
            tiposPermisoConducir = listOf("AM", "A1", "A2", "A", "B"),
            autonomias = listOf("Andalucía", "Madrid", "Cataluña"),
            escalas = listOf("1ª", "2ª", "3ª"),
            categorias = listOf("Cat A", "Cat B"),
            isUploading = false,
            uploadProgress = 0f,
            onEvent = {}
        )
    }
}
