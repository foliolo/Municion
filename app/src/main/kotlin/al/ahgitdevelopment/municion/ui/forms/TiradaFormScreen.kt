package al.ahgitdevelopment.municion.ui.forms

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.ui.components.DatePickerField
import al.ahgitdevelopment.municion.ui.components.getCurrentDateFormatted
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.TiradaViewModel
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
 * Contenido del formulario de Tirada para Single Scaffold Architecture.
 *
 * NO contiene Scaffold, TopBar ni FAB - estos estan en MainScreen.
 * Registra su funcion de guardado con MainScreen mediante onRegisterSaveCallback.
 *
 * @param tiradaId ID de tirada a editar (null para nueva)
 * @param navController Controlador de navegacion
 * @param snackbarHostState Estado del snackbar compartido desde MainScreen
 * @param onRegisterSaveCallback Callback para registrar funcion de guardado con MainScreen
 * @param viewModel ViewModel de Tiradas
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@Composable
fun TiradaFormContent(
    tirada: Tirada?,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onRegisterSaveCallback: ((() -> Unit)?) -> Unit,
    viewModel: TiradaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val isEditing = tirada != null

    // Form state - inicializar directamente desde el objeto
    var descripcion by rememberSaveable { mutableStateOf(tirada?.descripcion ?: "") }
    var rango by rememberSaveable { mutableStateOf(tirada?.rango ?: "") }
    var fecha by rememberSaveable { mutableStateOf(tirada?.fecha ?: getCurrentDateFormatted()) }
    var puntuacion by rememberSaveable { mutableFloatStateOf(tirada?.puntuacion?.toFloat() ?: 0f) }

    // Error states
    var descripcionError by remember { mutableStateOf<String?>(null) }
    var fechaError by remember { mutableStateOf<String?>(null) }

    // Strings para validaciones (capturadas para uso en lambda)
    val errorFieldRequired = stringResource(R.string.error_field_required)

    // Funcion de guardado
    val saveFunction: () -> Unit = {
        // Validaciones
        var isValid = true
        if (descripcion.isBlank()) {
            descripcionError = errorFieldRequired
            isValid = false
        }
        if (fecha.isBlank()) {
            fechaError = errorFieldRequired
            isValid = false
        }

        if (isValid) {
            val tiradaToSave = Tirada(
                id = tirada?.id ?: 0,
                descripcion = descripcion,
                rango = rango.ifBlank { null },
                fecha = fecha,
                puntuacion = puntuacion.toInt()
            )
            if (isEditing) {
                viewModel.updateTirada(tiradaToSave)
            } else {
                viewModel.saveTirada(tiradaToSave)
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
            is TiradaViewModel.TiradaUiState.Success -> {
                val message = (uiState as TiradaViewModel.TiradaUiState.Success).message
                viewModel.resetUiState()
                navController.popBackStack()
                // Show snackbar in background - don't block navigation
                launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
            is TiradaViewModel.TiradaUiState.Error -> {
                snackbarHostState.showSnackbar(
                    "Error: ${(uiState as TiradaViewModel.TiradaUiState.Error).message}"
                )
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    TiradaFormFields(
        descripcion = descripcion,
        descripcionError = descripcionError,
        rango = rango,
        fecha = fecha,
        fechaError = fechaError,
        puntuacion = puntuacion,
        onDescripcionChange = { descripcion = it; descripcionError = null },
        onRangoChange = { rango = it },
        onFechaChange = { fecha = it; fechaError = null },
        onPuntuacionChange = { puntuacion = it.coerceIn(0f, 600f) }
    )
}

/**
 * Campos del formulario de Tirada (Stateless).
 *
 * Sin Scaffold - solo los campos del formulario.
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@Composable
fun TiradaFormFields(
    descripcion: String,
    descripcionError: String?,
    rango: String,
    fecha: String,
    fechaError: String?,
    puntuacion: Float,
    onDescripcionChange: (String) -> Unit,
    onRangoChange: (String) -> Unit,
    onFechaChange: (String) -> Unit,
    onPuntuacionChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Descripcion
        OutlinedTextField(
            value = descripcion,
            onValueChange = onDescripcionChange,
            label = { Text(stringResource(R.string.label_description)) },
            placeholder = { Text(stringResource(R.string.placeholder_weekly_practice)) },
            isError = descripcionError != null,
            supportingText = descripcionError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // Rango / Lugar
        OutlinedTextField(
            value = rango,
            onValueChange = onRangoChange,
            label = { Text(stringResource(R.string.label_tirada_localizacion)) },
            placeholder = { Text(stringResource(R.string.placeholder_municipal_gallery)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // Fecha
        DatePickerField(
            label = stringResource(R.string.fecha),
            value = fecha,
            error = fechaError,
            onValueChange = onFechaChange
        )

        // Puntuacion
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.info_score_display, puntuacion.toInt()),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = puntuacion,
                onValueChange = onPuntuacionChange,
                valueRange = 0f..600f,
                steps = 59, // Incrementos de 10
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Campo de texto alternativo para puntuacion exacta
        OutlinedTextField(
            value = puntuacion.toInt().toString(),
            onValueChange = { newValue ->
                val intValue = newValue.toIntOrNull()
                if (intValue != null) {
                    onPuntuacionChange(intValue.coerceIn(0, 600).toFloat())
                } else if (newValue.isEmpty()) {
                    onPuntuacionChange(0f)
                }
            },
            label = { Text(stringResource(R.string.label_exact_score)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            supportingText = { Text(stringResource(R.string.hint_score_range)) }
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun TiradaFormFieldsPreview() {
    MunicionTheme {
        TiradaFormFields(
            descripcion = "Practica semanal",
            descripcionError = null,
            rango = "Galeria Municipal",
            fecha = "01/01/2024",
            fechaError = null,
            puntuacion = 450f,
            onDescripcionChange = {},
            onRangoChange = {},
            onFechaChange = {},
            onPuntuacionChange = {}
        )
    }
}
