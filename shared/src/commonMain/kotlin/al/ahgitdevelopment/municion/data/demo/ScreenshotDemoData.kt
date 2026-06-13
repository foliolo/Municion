package al.ahgitdevelopment.municion.data.demo

import al.ahgitdevelopment.municion.data.local.room.MunicionDatabase
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada

/**
 * Seeds Room with fictional demo data for App Store screenshots
 * ([al.ahgitdevelopment.municion.util.ScreenshotMode]). Wipes local tables first so repeated
 * launches are idempotent. Every value is invented — no real personal data.
 */
suspend fun seedScreenshotDemoData(database: MunicionDatabase) {
    database.licenciaDao().deleteAll()
    database.guiaDao().deleteAll()
    database.compraDao().deleteAll()
    database.tiradaDao().deleteAll()
    database.syncOperationDao().deleteAll()

    database.licenciaDao().insertAll(
        listOf(
            // tipo indices match the tipo_licencias string-array: 4 = E - Escopeta, 5 = F - Tiro olímpico.
            Licencia(
                tipo = 4,
                edad = 38,
                fechaExpedicion = "15/03/2024",
                fechaCaducidad = "15/03/2029",
                numLicencia = "E-58214",
                numSeguro = "POL-771234",
                syncId = "demo-lic-escopeta",
            ),
            Licencia(
                tipo = 5,
                edad = 38,
                fechaExpedicion = "02/09/2024",
                fechaCaducidad = "02/09/2027",
                numLicencia = "F-30417",
                numAbonado = 1042,
                syncId = "demo-lic-tiro",
            ),
        ),
    )

    database.guiaDao().insertAll(
        listOf(
            // tipoArma indices match the tipo_armas string-array: 0 = Pistola, 1 = Escopeta, 2 = Rifle.
            Guia(
                tipoLicencia = 4,
                marca = "Beretta",
                modelo = "686 Silver Pigeon I",
                apodo = "Escopeta de caza",
                tipoArma = 1,
                calibre1 = "12/70",
                numGuia = "G-104872",
                numArma = "N75321B",
                cupo = 5000,
                gastado = 1250,
                syncId = "demo-guia-beretta",
            ),
            Guia(
                tipoLicencia = 5,
                marca = "Walther",
                modelo = "GSP Expert",
                apodo = "Pistola de precisión",
                tipoArma = 0,
                calibre1 = ".22 LR",
                numGuia = "G-220045",
                numArma = "WX10448",
                cupo = 1000,
                gastado = 320,
                syncId = "demo-guia-walther",
            ),
            Guia(
                tipoLicencia = 5,
                marca = "Feinwerkbau",
                modelo = "800 X",
                apodo = "Carabina de aire",
                tipoArma = 2,
                calibre1 = "4,5 mm",
                numGuia = "G-198320",
                numArma = "FW88412",
                cupo = 800,
                gastado = 75,
                syncId = "demo-guia-fwb",
            ),
        ),
    )

    database.compraDao().insertAll(
        listOf(
            Compra(
                idPosGuia = 0,
                calibre1 = "12/70",
                unidades = 250,
                precio = 89.50,
                fecha = "12/04/2026",
                tipo = "Perdigón",
                peso = 28,
                marca = "Winchester",
                tienda = "Armería El Monte",
                valoracion = 4.5f,
                guiaSyncId = "demo-guia-beretta",
                syncId = "demo-compra-1",
            ),
            Compra(
                idPosGuia = 1,
                calibre1 = ".22 LR",
                unidades = 500,
                precio = 64.00,
                fecha = "28/03/2026",
                tipo = "Plomo",
                peso = 40,
                marca = "RWS",
                tienda = "Armería Diana",
                valoracion = 5f,
                guiaSyncId = "demo-guia-walther",
                syncId = "demo-compra-2",
            ),
            Compra(
                idPosGuia = 0,
                calibre1 = "12/70",
                unidades = 100,
                precio = 38.90,
                fecha = "05/02/2026",
                tipo = "Bala",
                peso = 32,
                marca = "Fiocchi",
                tienda = "Armería El Monte",
                valoracion = 4f,
                guiaSyncId = "demo-guia-beretta",
                syncId = "demo-compra-3",
            ),
        ),
    )

    database.tiradaDao().insertAll(
        listOf(
            Tirada(
                descripcion = "Campeonato regional",
                localizacion = "Club de Tiro Norte",
                modalidad = Tirada.MODALIDAD_PRECISION,
                fecha = "26/04/2026",
                puntuacion = 540,
                syncId = "demo-tirada-1",
            ),
            Tirada(
                descripcion = "Tirada social",
                localizacion = "Club de Tiro Norte",
                modalidad = Tirada.MODALIDAD_PRECISION,
                fecha = "15/03/2026",
                puntuacion = 495,
                syncId = "demo-tirada-2",
            ),
            Tirada(
                descripcion = "Liga IPSC",
                localizacion = "Campo Las Encinas",
                modalidad = Tirada.MODALIDAD_IPSC,
                fecha = "01/02/2026",
                puntuacion = 82,
                syncId = "demo-tirada-3",
            ),
        ),
    )
}
