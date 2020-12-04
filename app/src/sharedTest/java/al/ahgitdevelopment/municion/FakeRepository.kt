/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.utils.toFlow
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
class FakeRepository : RepositoryContract {

    var retrieveLocalData: Boolean = true
    private var shouldReturnError = false

    val properties = ArrayList<Property>()
    val purchases = ArrayList<Purchase>()
    val licenses = ArrayList<License>()
    val competitions = ArrayList<Competition>()

    override fun getProperties(): Flow<List<Property>> {
        return if (shouldReturnError && !retrieveLocalData) {
            throw Exception(ERROR_MESSAGE)
        } else {
            properties.toFlow()
        }
    }

    override fun getPurchases(): Flow<List<Purchase>> {
        return if (shouldReturnError && !retrieveLocalData) {
            throw Exception(ERROR_MESSAGE)
        } else {
            purchases.toFlow()
        }
    }

    override fun getLicenses(): Flow<List<License>> {
        return if (shouldReturnError && !retrieveLocalData) {
            throw Exception(ERROR_MESSAGE)
        } else {
            licenses.toFlow()
        }
    }

    override fun getCompetitions(): Flow<List<Competition>> {
        return if (shouldReturnError && !retrieveLocalData) {
            throw Exception(ERROR_MESSAGE)
        } else {
            competitions.toFlow()
        }
    }

    override suspend fun saveProperty(property: Property) {
        TODO("Not yet implemented")
    }

    override suspend fun savePurchase(purchase: Purchase) {
        TODO("Not yet implemented")
    }

    override suspend fun saveLicense(license: License) {
        TODO("Not yet implemented")
    }

    override suspend fun saveCompetition(competition: Competition) {
        competitions.add(FAKE_COMPETITION)
    }

    override suspend fun removeProperty(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removePurchase(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removeLicense(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removeCompetition(id: String) {
        // Thread {
        competitions.clear()
        // }.start()
    }

    companion object {
        val FAKE_COMPETITION = Competition(
            "id",
            "Description",
            "12345",
            "Ranking",
            100,
            "Place"
        )
        val FAKE_LICENSE = License(
            "id",
            "License 1",
            "12345",
            "10/10/2014",
            "15/15/2020",
            "98765"
        )
        val FAKE_PROPERTY = Property(
            "id",
            "Nickname",
            "Brand",
            "Model",
            "Bore 1",
            "Bore 2",
            "Num id",
            "Image"
        )
        val FAKE_PURCHASE = Purchase(
            "id",
            "Brand",
            "Store",
            "Bore",
            10,
            10.10,
            "15/15/2020",
            3.4F,
            10,
            "Image"
        )

        val FAKE_LICENSES = arrayListOf(FAKE_LICENSE)
        val FAKE_COMPETITIONS = arrayListOf(FAKE_COMPETITION)
        val FAKE_PROPERTIES = arrayListOf(FAKE_PROPERTY)
        val FAKE_PURCHASES = arrayListOf(FAKE_PURCHASE)
        val ERROR_MESSAGE = "Exception test"
    }
}
