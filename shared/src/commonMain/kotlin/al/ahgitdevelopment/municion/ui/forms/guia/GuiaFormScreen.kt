package al.ahgitdevelopment.municion.ui.forms.guia

import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.calibres
import al.ahgitdevelopment.municion.resources.tipo_armas
import al.ahgitdevelopment.municion.ui.components.DropdownField
import al.ahgitdevelopment.municion.ui.forms.FormUiState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringArrayResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GuiaFormScreen(
    guiaId: Int?,
    tipoLicencia: Int,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onRegisterSaveCallback: ((() -> Unit)?) -> Unit,
    viewModel: GuiaFormViewModel = koinViewModel(),
) {
    LaunchedEffect(guiaId) { viewModel.initialize(guiaId, tipoLicencia) }
    LaunchedEffect(Unit) { onRegisterSaveCallback { viewModel.save() } }

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

    val tiposArma = stringArrayResource(Res.array.tipo_armas)
    val calibres = stringArrayResource(Res.array.calibres)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DropdownField(
            label = "Tipo de arma",
            options = tiposArma.toList(),
            selectedOption = tiposArma.getOrElse(state.tipoArma) { "" },
            onOptionSelected = { viewModel.onTipoArma(tiposArma.indexOf(it).coerceAtLeast(0)) },
        )

        OutlinedTextField(
            value = state.marca,
            onValueChange = viewModel::onMarca,
            label = { Text("Marca") },
            isError = state.marcaError != null,
            supportingText = state.marcaError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = state.modelo,
            onValueChange = viewModel::onModelo,
            label = { Text("Modelo") },
            isError = state.modeloError != null,
            supportingText = state.modeloError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = state.apodo,
            onValueChange = viewModel::onApodo,
            label = { Text("Apodo") },
            isError = state.apodoError != null,
            supportingText = state.apodoError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        DropdownField(
            label = "Calibre",
            options = calibres.toList(),
            selectedOption = state.calibre1,
            error = state.calibre1Error,
            onOptionSelected = viewModel::onCalibre1,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Checkbox(checked = state.showCalibre2, onCheckedChange = viewModel::onShowCalibre2)
            Text("Segundo calibre")
        }

        if (state.showCalibre2) {
            DropdownField(
                label = "Segundo calibre",
                options = calibres.toList(),
                selectedOption = state.calibre2,
                onOptionSelected = viewModel::onCalibre2,
            )
        }

        OutlinedTextField(
            value = state.numGuia,
            onValueChange = viewModel::onNumGuia,
            label = { Text("Número de guía") },
            isError = state.numGuiaError != null,
            supportingText = state.numGuiaError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = state.numArma,
            onValueChange = viewModel::onNumArma,
            label = { Text("Número de arma") },
            isError = state.numArmaError != null,
            supportingText = state.numArmaError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Checkbox(checked = state.customCupo, onCheckedChange = viewModel::onCustomCupo)
            Text("Cupo personalizado")
        }

        OutlinedTextField(
            value = state.cupo,
            onValueChange = viewModel::onCupo,
            label = { Text("Cupo anual") },
            isError = state.cupoError != null,
            supportingText = state.cupoError?.let { { Text(it) } },
            enabled = state.customCupo,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.showGastado) {
            OutlinedTextField(
                value = state.gastado,
                onValueChange = viewModel::onGastado,
                label = { Text("Munición gastada") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
