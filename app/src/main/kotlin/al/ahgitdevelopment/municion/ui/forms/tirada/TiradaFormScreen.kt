package al.ahgitdevelopment.municion.ui.forms.tirada

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.ui.components.DatePickerField
import al.ahgitdevelopment.municion.ui.components.DropdownField
import al.ahgitdevelopment.municion.ui.components.getCurrentDateFormatted
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
 * Contenido del formulario de Tirada con patrón MVI.
 *
 * NO contiene Scaffold, TopBar ni FAB - estos están en MainScreen.
 * Usa TiradaFormViewModel para manejo de estado centralizado.
 *
 * @param tirada Tirada a editar (null para nueva)
 * @param navController Controlador de navegación
 * @param snackbarHostState Estado del snackbar compartido desde MainScreen
 * @param onRegisterSaveCallback Callback para registrar función de guardado con MainScreen
 * @param viewModel ViewModel del formulario (inyectado por Hilt)
 *
 * @since v3.2.2 (Form Architecture Refactor - MVI Pattern)
 */
@Composable
fun TiradaFormContent(
    tirada: Tirada?,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onRegisterSaveCallback: ((() -> Unit)?) -> Unit,
    viewModel: TiradaFormViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    
    // Opciones de categoría y modalidad
    val categoriaOptions = remember {
        context.resources.getStringArray(R.array.categoria_tirada).toList()
    }
    val modalidadOptions = remember {
        context.resources.getStringArray(R.array.modalidad_tirada).toList()
    }
    
    // Inicializar formulario
    LaunchedEffect(tirada) {
        viewModel.initialize(tirada, getCurrentDateFormatted())
    }
    
    // Registrar función de guardado con MainScreen
    DisposableEffect(Unit) {
        onRegisterSaveCallback { viewModel.onEvent(TiradaFormEvent.Save) }
        onDispose { onRegisterSaveCallback(null) }
    }
    
    // Manejar efectos secundarios
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TiradaFormEffect.NavigateBack -> navController.popBackStack()
                is TiradaFormEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is TiradaFormEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }
    
    // Mostrar mensajes de UiState
    LaunchedEffect(uiState) {
        when (uiState) {
            is TiradaFormUiState.Success -> {
                val message = (uiState as TiradaFormUiState.Success).message
                viewModel.resetUiState()
                // Show snackbar in background
                launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
            is TiradaFormUiState.Error -> {
                snackbarHostState.showSnackbar(
                    "Error: ${(uiState as TiradaFormUiState.Error).message}"
                )
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    TiradaFormFields(
        state = formState,
        categoriaOptions = categoriaOptions,
        modalidadOptions = modalidadOptions,
        onEvent = viewModel::onEvent
    )
}

/**
 * Campos del formulario de Tirada (Stateless).
 *
 * Recibe el estado completo y emite eventos.
 * Fácil de previsualizar y testear.
 *
 * @param state Estado actual del formulario
 * @param categoriaOptions Lista de opciones de categoría
 * @param modalidadOptions Lista de opciones de modalidad
 * @param onEvent Callback para emitir eventos
 * @param modifier Modificador opcional
 *
 * @since v3.2.2 (Form Architecture Refactor - MVI Pattern)
 */
@Composable
fun TiradaFormFields(
    state: TiradaFormState,
    categoriaOptions: List<String>,
    modalidadOptions: List<String>,
    onEvent: (TiradaFormEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    // Hint de puntuación según modalidad
    val scoreHint = if (state.isIpsc) R.string.hint_score_range_ipsc else R.string.hint_score_range_precision

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Descripción
        OutlinedTextField(
            value = state.descripcion,
            onValueChange = { onEvent(TiradaFormEvent.DescripcionChanged(it)) },
            label = { Text(stringResource(R.string.label_description)) },
            placeholder = { Text(stringResource(R.string.placeholder_weekly_practice)) },
            isError = state.descripcionError != null,
            supportingText = state.descripcionError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // Localización / Lugar
        OutlinedTextField(
            value = state.localizacion,
            onValueChange = { onEvent(TiradaFormEvent.LocalizacionChanged(it)) },
            label = { Text(stringResource(R.string.label_tirada_localizacion)) },
            placeholder = { Text(stringResource(R.string.placeholder_municipal_gallery)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // Categoría (Nacional, Autonómica, Local/Social)
        DropdownField(
            label = stringResource(R.string.lbl_categoria_tirada),
            options = categoriaOptions,
            selectedOption = state.categoria,
            onOptionSelected = { onEvent(TiradaFormEvent.CategoriaChanged(it)) },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Fecha
        DatePickerField(
            label = stringResource(R.string.fecha),
            value = state.fecha,
            error = state.fechaError,
            onValueChange = { onEvent(TiradaFormEvent.FechaChanged(it)) }
        )

        // Modalidad de puntuación (Precisión o IPSC)
        DropdownField(
            label = stringResource(R.string.lbl_modalidad_tirada),
            options = modalidadOptions,
            selectedOption = state.modalidad,
            onOptionSelected = { onEvent(TiradaFormEvent.ModalidadChanged(it)) },
            modifier = Modifier.fillMaxWidth()
        )

        // Puntuación con rango dinámico según modalidad
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${state.puntuacion.toInt()} ${state.scoreSuffix}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = state.puntuacion,
                onValueChange = { onEvent(TiradaFormEvent.PuntuacionChanged(it)) },
                valueRange = 0f..state.maxPuntuacion,
                steps = if (state.isIpsc) 100 else 59, // Incrementos de 10
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Campo de texto alternativo para puntuación exacta
        OutlinedTextField(
            value = state.puntuacion.toInt().toString(),
            onValueChange = { newValue ->
                val intValue = newValue.toIntOrNull()
                if (intValue != null) {
                    onEvent(TiradaFormEvent.PuntuacionChanged(intValue.toFloat()))
                } else if (newValue.isEmpty()) {
                    onEvent(TiradaFormEvent.PuntuacionChanged(0f))
                }
            },
            label = { Text(stringResource(R.string.label_exact_score)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            supportingText = { Text(stringResource(scoreHint)) }
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun TiradaFormFieldsPreview() {
    MunicionTheme {
        TiradaFormFields(
            state = TiradaFormState(
                descripcion = "Práctica semanal",
                localizacion = "Galería Municipal",
                categoria = "Nacional",
                modalidad = Tirada.MODALIDAD_PRECISION,
                fecha = "01/01/2024",
                puntuacion = 450f
            ),
            categoriaOptions = listOf("Nacional", "Autonómica", "Local/Social"),
            modalidadOptions = listOf("Precisión", "IPSC"),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TiradaFormFieldsIpscPreview() {
    MunicionTheme {
        TiradaFormFields(
            state = TiradaFormState(
                descripcion = "Competición IPSC",
                localizacion = "Club de Tiro",
                categoria = "Autonómica",
                modalidad = Tirada.MODALIDAD_IPSC,
                fecha = "15/01/2024",
                puntuacion = 85f
            ),
            categoriaOptions = listOf("Nacional", "Autonómica", "Local/Social"),
            modalidadOptions = listOf("Precisión", "IPSC"),
            onEvent = {}
        )
    }
}
