package al.ahgitdevelopment.municion.ui.forms.tirada

import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.categoria_tirada
import al.ahgitdevelopment.municion.resources.modalidad_tirada
import al.ahgitdevelopment.municion.ui.components.DatePickerField
import al.ahgitdevelopment.municion.ui.components.DropdownField
import al.ahgitdevelopment.municion.ui.forms.FormUiState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringArrayResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TiradaFormScreen(
    tiradaId: Int?,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onRegisterSaveCallback: ((() -> Unit)?) -> Unit,
    viewModel: TiradaFormViewModel = koinViewModel(),
) {
    LaunchedEffect(tiradaId) { viewModel.initialize(tiradaId) }
    DisposableEffect(Unit) {
        onRegisterSaveCallback { viewModel.save() }
        onDispose { onRegisterSaveCallback(null) }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        when (val s = uiState) {
            is FormUiState.Saved -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetUiState()
                navController.popBackStack()
            }
            is FormUiState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetUiState()
            }
            else -> Unit
        }
    }

    val categorias = stringArrayResource(Res.array.categoria_tirada)
    val modalidades = stringArrayResource(Res.array.modalidad_tirada)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = state.descripcion,
            onValueChange = viewModel::onDescripcion,
            label = { Text("Descripción") },
            isError = state.descripcionError != null,
            supportingText = state.descripcionError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = state.localizacion,
            onValueChange = viewModel::onLocalizacion,
            label = { Text("Localización") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        DropdownField(
            label = "Categoría",
            options = categorias.toList(),
            selectedOption = state.categoria,
            onOptionSelected = viewModel::onCategoria,
        )

        DropdownField(
            label = "Modalidad de puntuación",
            options = modalidades.toList(),
            selectedOption = state.modalidad,
            onOptionSelected = viewModel::onModalidad,
        )

        DatePickerField(
            label = "Fecha",
            value = state.fecha,
            error = state.fechaError,
            onValueChange = viewModel::onFecha,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = state.puntuacion,
            onValueChange = viewModel::onPuntuacion,
            label = { Text("Puntuación") },
            supportingText = { Text("Máximo ${state.maxPuntuacion}") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
