package al.ahgitdevelopment.municion.repository

import al.ahgitdevelopment.municion.datamodel.Compra
import al.ahgitdevelopment.municion.datamodel.Guia
import al.ahgitdevelopment.municion.datamodel.Licencia
import al.ahgitdevelopment.municion.datamodel.Tirada
import androidx.lifecycle.LiveData

interface RepositoryInterface {
    fun getGuias(): LiveData<List<Guia>>?
    fun getCompras(): LiveData<List<Compra>>?
    fun getLicencias(): LiveData<List<Licencia>>?
    fun getTiradas(): LiveData<List<Tirada>>?

    fun saveGuias(guia: Guia)
    fun saveCompras(compra: Compra)
    fun saveLicencias(licencia: Licencia)
    fun saveTiradas(tirada: Tirada)

    fun fetchDataFromFirebase()
    fun uploadDataToFirebase()
}
