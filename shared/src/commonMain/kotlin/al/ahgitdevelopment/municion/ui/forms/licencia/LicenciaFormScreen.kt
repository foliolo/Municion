package al.ahgitdevelopment.municion.ui.forms.licencia

import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.categorias
import al.ahgitdevelopment.municion.resources.ccaa
import al.ahgitdevelopment.municion.resources.tipo_escala
import al.ahgitdevelopment.municion.resources.tipo_licencias
import al.ahgitdevelopment.municion.resources.tipo_permiso_conducir
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
fun LicenciaFormScreen(
    licenciaId: Int?,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onRegisterSaveCallback: ((() -> Unit)?) -> Unit,
    viewModel: LicenciaFormViewModel = koinViewModel(),
) {
    LaunchedEffect(licenciaId) { viewModel.initialize(licenciaId) }
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

    val tipos = stringArrayResource(Res.array.tipo_licencias)
    val escalas = stringArrayResource(Res.array.tipo_escala)
    val autonomias = stringArrayResource(Res.array.ccaa)
    val permisos = stringArrayResource(Res.array.tipo_permiso_conducir)
    val categorias = stringArrayResource(Res.array.categorias)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DropdownField(
            label = "Tipo de licencia",
            options = tipos.toList(),
            selectedOption = tipos.getOrElse(state.tipoLicencia) { "" },
            onOptionSelected = { viewModel.onTipo(tipos.indexOf(it).coerceAtLeast(0)) },
        )

        OutlinedTextField(
            value = state.numLicencia,
            onValueChange = viewModel::onNumLicencia,
            label = { Text("Número de licencia") },
            isError = state.numLicenciaError != null,
            supportingText = state.numLicenciaError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        DatePickerField(
            label = "Fecha de expedición",
            value = state.fechaExpedicion,
            error = state.fechaExpedicionError,
            onValueChange = viewModel::onFechaExpedicion,
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.showFechaCaducidad) {
            DatePickerField(
                label = "Fecha de caducidad",
                value = state.fechaCaducidad,
                error = null,
                onValueChange = viewModel::onFechaCaducidad,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (state.showEscala) {
            DropdownField(
                label = "Escala",
                options = escalas.toList(),
                selectedOption = escalas.getOrElse(state.escala) { "" },
                onOptionSelected = { viewModel.onEscala(escalas.indexOf(it).coerceAtLeast(0)) },
            )
        }

        if (state.showCategoria) {
            DropdownField(
                label = "Categoría",
                options = categorias.toList(),
                selectedOption = categorias.getOrElse(state.categoria) { "" },
                onOptionSelected = { viewModel.onCategoria(categorias.indexOf(it).coerceAtLeast(0)) },
            )
        }

        if (state.showAutonomia) {
            DropdownField(
                label = "Comunidad autónoma",
                options = autonomias.toList(),
                selectedOption = autonomias.getOrElse(state.autonomia) { "" },
                onOptionSelected = { viewModel.onAutonomia(autonomias.indexOf(it).coerceAtLeast(0)) },
            )
        }

        if (state.showNumAbonado) {
            OutlinedTextField(
                value = state.numAbonado,
                onValueChange = viewModel::onNumAbonado,
                label = { Text("Número de abonado") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (state.showNumSeguro) {
            OutlinedTextField(
                value = state.numSeguro,
                onValueChange = viewModel::onNumSeguro,
                label = { Text("Número de póliza") },
                isError = state.numSeguroError != null,
                supportingText = state.numSeguroError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (state.showPermisoConducir) {
            DropdownField(
                label = "Permiso de conducir",
                options = permisos.toList(),
                selectedOption = permisos.getOrElse(state.tipoPermisoConducir) { "" },
                onOptionSelected = { viewModel.onTipoPermiso(permisos.indexOf(it).coerceAtLeast(0)) },
            )
        }

        if (state.showEdad) {
            OutlinedTextField(
                value = state.edad,
                onValueChange = viewModel::onEdad,
                label = { Text("Edad") },
                isError = state.edadError != null,
                supportingText = state.edadError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
