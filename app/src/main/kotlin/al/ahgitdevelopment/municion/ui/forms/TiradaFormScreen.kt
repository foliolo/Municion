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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.TiradaViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Pantalla de formulario de Tirada (Stateful).
 *
 * @param navController Controlador de navegación
 * @param tiradaId ID de tirada a editar (null para nueva)
 * @param viewModel ViewModel de Tiradas
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun TiradaFormScreen(
    navController: NavHostController,
    tiradaId: Int? = null,
    viewModel: TiradaViewModel = hiltViewModel()
) {
    val tiradas by viewModel.tiradas.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val isEditing = tiradaId != null

    // Cargar tirada existente si estamos editando
    val existingTirada = remember(tiradaId, tiradas) {
        tiradaId?.let { id -> tiradas.find { it.id == id } }
    }

    // Form state
    var descripcion by rememberSaveable { mutableStateOf(existingTirada?.descripcion ?: "") }
    var rango by rememberSaveable { mutableStateOf(existingTirada?.rango ?: "") }
    var fecha by rememberSaveable { mutableStateOf(existingTirada?.fecha ?: getCurrentDateTirada()) }
    var puntuacion by rememberSaveable { mutableFloatStateOf(existingTirada?.puntuacion?.toFloat() ?: 0f) }

    // Error states
    var descripcionError by remember { mutableStateOf<String?>(null) }
    var fechaError by remember { mutableStateOf<String?>(null) }

    // Mostrar mensajes de UiState
    LaunchedEffect(uiState) {
        when (uiState) {
            is TiradaViewModel.TiradaUiState.Success -> {
                snackbarHostState.showSnackbar(
                    (uiState as TiradaViewModel.TiradaUiState.Success).message
                )
                viewModel.resetUiState()
                navController.popBackStack()
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

    TiradaFormScreenContent(
        isEditing = isEditing,
        snackbarHostState = snackbarHostState,
        descripcion = descripcion,
        descripcionError = descripcionError,
        rango = rango,
        fecha = fecha,
        fechaError = fechaError,
        puntuacion = puntuacion,
        onDescripcionChange = { descripcion = it; descripcionError = null },
        onRangoChange = { rango = it },
        onFechaChange = { fecha = it; fechaError = null },
        onPuntuacionChange = { puntuacion = it.coerceIn(0f, 600f) },
        onBackClick = { navController.popBackStack() },
        onSaveClick = {
            // Validaciones
            var isValid = true
            if (descripcion.isBlank()) {
                descripcionError = "Campo obligatorio"
                isValid = false
            }
            if (fecha.isBlank()) {
                fechaError = "Campo obligatorio"
                isValid = false
            }

            if (isValid) {
                val tirada = Tirada(
                    id = tiradaId ?: 0,
                    descripcion = descripcion,
                    rango = rango.ifBlank { null },
                    fecha = fecha,
                    puntuacion = puntuacion.toInt()
                )
                if (isEditing) {
                    viewModel.updateTirada(tirada)
                } else {
                    viewModel.saveTirada(tirada)
                }
            }
        }
    )
}

/**
 * Contenido del formulario de Tirada (Stateless).
 *
 * @since v3.0.0 (Compose Migration)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiradaFormScreenContent(
    isEditing: Boolean,
    snackbarHostState: SnackbarHostState,
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
            TopAppBar(
                title = { Text(if (isEditing) "Editar tirada" else "Nueva tirada") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Save, contentDescription = "Guardar")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = onDescripcionChange,
                label = { Text("Descripción") },
                placeholder = { Text("Ej: Práctica semanal") },
                isError = descripcionError != null,
                supportingText = descripcionError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Rango / Lugar
            OutlinedTextField(
                value = rango,
                onValueChange = onRangoChange,
                label = { Text("Lugar / Galería") },
                placeholder = { Text("Ej: Galería Municipal") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
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

            // Puntuación
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Puntuación: ${puntuacion.toInt()} / 600",
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

            // Campo de texto alternativo para puntuación exacta
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
                label = { Text("Puntuación exacta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("Valor entre 0 y 600") }
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

private fun getCurrentDateTirada(): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(Calendar.getInstance().time)
}

@Preview(showBackground = true)
@Composable
private fun TiradaFormScreenContentPreview() {
    MunicionTheme {
        TiradaFormScreenContent(
            isEditing = false,
            snackbarHostState = SnackbarHostState(),
            descripcion = "Práctica semanal",
            descripcionError = null,
            rango = "Galería Municipal",
            fecha = "01/01/2024",
            fechaError = null,
            puntuacion = 450f,
            onDescripcionChange = {},
            onRangoChange = {},
            onFechaChange = {},
            onPuntuacionChange = {},
            onBackClick = {},
            onSaveClick = {}
        )
    }
}
