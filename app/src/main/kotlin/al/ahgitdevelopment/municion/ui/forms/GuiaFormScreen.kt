package al.ahgitdevelopment.municion.ui.forms

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.GuiaViewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

/**
 * Contenido del formulario de Guia para Single Scaffold Architecture.
 *
 * NO contiene Scaffold, TopBar ni FAB - estos estan en MainScreen.
 * Registra su funcion de guardado con MainScreen mediante onRegisterSaveCallback.
 *
 * @param guia Objeto completo para editar (null para nueva)
 * @param tipoLicencia Tipo de licencia seleccionado (nombre, para determinar tipos de arma)
 * @param navController Controlador de navegacion
 * @param snackbarHostState Estado del snackbar compartido desde MainScreen
 * @param onRegisterSaveCallback Callback para registrar funcion de guardado con MainScreen
 * @param viewModel ViewModel de Guias
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 * @since v3.2.2 (Image Upload Feature)
 */
@Composable
fun GuiaFormContent(
    guia: Guia?,
    tipoLicencia: String,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onRegisterSaveCallback: ((() -> Unit)?) -> Unit,
    viewModel: GuiaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedImageUri by viewModel.selectedImageUri.collectAsStateWithLifecycle()

    val isEditing = guia != null

    // Form state - inicializar directamente desde el objeto
    var marca by rememberSaveable { mutableStateOf(guia?.marca ?: "") }
    var modelo by rememberSaveable { mutableStateOf(guia?.modelo ?: "") }
    var apodo by rememberSaveable { mutableStateOf(guia?.apodo ?: "") }
    var tipoArma by rememberSaveable { mutableIntStateOf(guia?.tipoArma ?: 0) }
    var calibre1 by rememberSaveable { mutableStateOf(guia?.calibre1 ?: "") }
    var calibre2 by rememberSaveable { mutableStateOf(guia?.calibre2 ?: "") }
    var showCalibre2 by rememberSaveable { mutableStateOf(!guia?.calibre2.isNullOrBlank()) }
    var numGuia by rememberSaveable { mutableStateOf(guia?.numGuia ?: "") }
    var numArma by rememberSaveable { mutableStateOf(guia?.numArma ?: "") }
    var cupo by rememberSaveable { mutableStateOf(guia?.cupo?.toString() ?: "") }
    var gastado by rememberSaveable { mutableStateOf(guia?.gastado?.toString() ?: "0") }
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

    // Strings para validaciones (capturadas para uso en lambda)
    val errorFieldRequired = stringResource(R.string.error_field_required)
    val errorValidQuota = stringResource(R.string.error_valid_quota)

    // Image picker launcher usando el contrato moderno PickVisualMedia
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.setSelectedImageUri(it) }
    }

    // Determinar qué imagen mostrar: nueva selección, existente en Storage, o ninguna
    val currentImageUrl = selectedImageUri?.toString() ?: guia?.fotoUrl

    // Funcion de guardado
    val saveFunction: () -> Unit = {
        // Validaciones
        var isValid = true
        if (marca.isBlank()) {
            marcaError = errorFieldRequired
            isValid = false
        }
        if (modelo.isBlank()) {
            modeloError = errorFieldRequired
            isValid = false
        }
        if (apodo.isBlank()) {
            apodoError = errorFieldRequired
            isValid = false
        }
        if (calibre1.isBlank()) {
            calibre1Error = errorFieldRequired
            isValid = false
        }
        if (numGuia.isBlank()) {
            numGuiaError = errorFieldRequired
            isValid = false
        }
        if (numArma.isBlank()) {
            numArmaError = errorFieldRequired
            isValid = false
        }
        if (cupo.isBlank() || cupo.toIntOrNull() == null || cupo.toInt() <= 0) {
            cupoError = errorValidQuota
            isValid = false
        }

        if (isValid) {
            // Obtener el tipo de licencia como Int
            val tipoLicenciaInt = getLicenciaTypeFromString(context, tipoLicencia)

            val guiaToSave = Guia(
                id = guia?.id ?: 0,
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
                gastado = gastado.toIntOrNull() ?: 0,
                // Preservar imagen existente si no se ha seleccionado nueva
                fotoUrl = guia?.fotoUrl,
                storagePath = guia?.storagePath
            )

            // Decidir si guardar con imagen nueva o sin cambios en imagen
            val newImageUri = selectedImageUri
            if (newImageUri != null) {
                // Hay nueva imagen seleccionada
                if (isEditing) {
                    viewModel.updateGuiaWithImage(guiaToSave, newImageUri)
                } else {
                    viewModel.saveGuiaWithImage(guiaToSave, newImageUri)
                }
            } else {
                // Sin cambios en imagen
                if (isEditing) {
                    viewModel.updateGuia(guiaToSave)
                } else {
                    viewModel.saveGuia(guiaToSave)
                }
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

            is GuiaViewModel.GuiaUiState.Uploading -> {
                // El estado Uploading se maneja en el UI mostrando el progreso
            }

            else -> {}
        }
    }

    // Determinar si está subiendo
    val isUploading = uiState is GuiaViewModel.GuiaUiState.Uploading
    val uploadProgress = if (uiState is GuiaViewModel.GuiaUiState.Uploading) {
        (uiState as GuiaViewModel.GuiaUiState.Uploading).progress
    } else 0f

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
        // Parámetros de imagen
        currentImageUrl = currentImageUrl,
        isUploading = isUploading,
        uploadProgress = uploadProgress,
        onSelectImage = {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onRemoveImage = {
            viewModel.setSelectedImageUri(null)
            // Si estamos editando y hay imagen en Storage, eliminarla
            guia?.let { existingGuia ->
                if (existingGuia.fotoUrl != null) {
                    viewModel.removeGuiaImage(existingGuia)
                }
            }
        },
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
 * @since v3.2.2 (Image Upload Feature)
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
    // Parámetros de imagen
    currentImageUrl: String?,
    isUploading: Boolean,
    uploadProgress: Float,
    onSelectImage: () -> Unit,
    onRemoveImage: () -> Unit,
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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Tipo de arma
        if (tiposArma.isNotEmpty()) {
            DropdownFieldGuia(
                label = stringResource(R.string.label_weapon_type),
                selectedIndex = tipoArma,
                options = tiposArma,
                onSelectionChange = onTipoArmaChange
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

        // Modelo
        OutlinedTextField(
            value = modelo,
            onValueChange = onModeloChange,
            label = { Text(stringResource(R.string.modelo)) },
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
            label = { Text(stringResource(R.string.label_nickname)) },
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
            label = stringResource(R.string.calibre),
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
            Text(stringResource(R.string.check_segundo_calibre))
        }

        // Calibre 2
        if (showCalibre2) {
            AutoCompleteTextField(
                value = calibre2,
                onValueChange = onCalibre2Change,
                label = stringResource(R.string.calibre2),
                suggestions = calibres,
                error = null
            )
        }

        // Numero de guia
        OutlinedTextField(
            value = numGuia,
            onValueChange = onNumGuiaChange,
            label = { Text(stringResource(R.string.label_guide_number)) },
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
            label = { Text(stringResource(R.string.label_weapon_number)) },
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
            Text(stringResource(R.string.label_custom_quota))
        }

        // Cupo
        OutlinedTextField(
            value = cupo,
            onValueChange = onCupoChange,
            label = { Text(stringResource(R.string.label_annual_quota)) },
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
                label = { Text(stringResource(R.string.label_spent_ammunition)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selector de imagen del arma
        GuiaImagePicker(
            currentImageUrl = currentImageUrl,
            isUploading = isUploading,
            uploadProgress = uploadProgress,
            onSelectImage = onSelectImage,
            onRemoveImage = onRemoveImage
        )

        Spacer(modifier = Modifier.height(12.dp))
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
    val filteredSuggestions = suggestions.filter { it.contains(value, ignoreCase = true) }.take(10)

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
                .fillMaxWidth()
                .menuAnchor(
                    ExposedDropdownMenuAnchorType.PrimaryEditable,
                    false
                ),
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

/**
 * Componente para seleccionar y mostrar la imagen del arma
 *
 * @param currentImageUrl URL de la imagen actual (local Uri o Firebase URL)
 * @param isUploading Si se está subiendo la imagen
 * @param uploadProgress Progreso de subida (0-1)
 * @param onSelectImage Callback para seleccionar nueva imagen
 * @param onRemoveImage Callback para eliminar la imagen
 *
 * @since v3.2.2 (Image Upload Feature)
 */
@Composable
private fun GuiaImagePicker(
    currentImageUrl: String?,
    isUploading: Boolean,
    uploadProgress: Float,
    onSelectImage: () -> Unit,
    onRemoveImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageSize = 180.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.label_weapon_photo),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(imageSize)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = !isUploading) { onSelectImage() },
            contentAlignment = Alignment.Center
        ) {
            if (currentImageUrl != null) {
                // Mostrar imagen seleccionada o existente
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(currentImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.content_description_weapon_image),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Botón para eliminar imagen (solo si no está subiendo)
                if (!isUploading) {
                    IconButton(
                        onClick = onRemoveImage,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(32.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_remove_image),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Overlay de subida
                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
//                            .background(Color.Black.copy(alpha = 0.5f)),
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
//                            progress = { uploadProgress },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Placeholder para añadir imagen
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.action_add_photo),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Barra de progreso de subida
        if (isUploading) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { uploadProgress },
                modifier = Modifier.fillMaxWidth(0.6f)
            )
            Text(
                text = "${(uploadProgress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
            currentImageUrl = null,
            isUploading = false,
            uploadProgress = 0f,
            onSelectImage = {},
            onRemoveImage = {},
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

@Preview(showBackground = true)
@Composable
private fun GuiaImagePickerPreview() {
    MunicionTheme {
        Column {
            // Sin imagen
            GuiaImagePicker(
                currentImageUrl = null,
                isUploading = false,
                uploadProgress = 0f,
                onSelectImage = {},
                onRemoveImage = {}
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Subiendo
            GuiaImagePicker(
                currentImageUrl = "https://example.com/image.jpg",
                isUploading = true,
                uploadProgress = 0.65f,
                onSelectImage = {},
                onRemoveImage = {}
            )
        }
    }
}
