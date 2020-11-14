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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
class FakeRepository : RepositoryContract {

    var retrieveLocalData: Boolean = true
    var shouldReturnError = false

    val properties = ArrayList<Property>()
    val purchases = ArrayList<Purchase>()
    val licenses = ArrayList<License>()
    val competitions = ArrayList<Competition>()

    override fun getProperties(): Flow<List<Property>> {
        return if (shouldReturnError && !retrieveLocalData) {
            throw Exception(ERROR_MESSAGE)
        } else {
            properties.getFlow()
        }
    }

    override fun getPurchases(): Flow<List<Purchase>> {
        return if (shouldReturnError && !retrieveLocalData) {
            throw Exception(ERROR_MESSAGE)
        } else {
            purchases.getFlow()
        }
    }

    override fun getLicenses(forceUpdate: Boolean): Flow<List<License>> {
        return if (shouldReturnError && !retrieveLocalData) {
            throw Exception(ERROR_MESSAGE)
        } else {
            licenses.getFlow()
        }
    }

    override fun getCompetitions(): Flow<List<Competition>> {
        return if (shouldReturnError && !retrieveLocalData) {
            throw Exception(ERROR_MESSAGE)
        } else {
            competitions.getFlow()
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

    override suspend fun removeProperty(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun removePurchase(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun removeLicense(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun removeCompetition(id: Long) {
        // Thread {
        competitions.clear()
        // }.start()
    }

    companion object {
        val FAKE_COMPETITION = Competition(
            1,
            "Description",
            "12345",
            "Ranking",
            100,
            "Place"
        )
        val FAKE_LICENSE = License(
            1,
            "License 1",
            "12345",
            "10/10/2014",
            "15/15/2020",
            "98765"
        )
        val FAKE_PROPERTY = Property(
            1,
            "Nickname",
            "Brand",
            "Model",
            "Bore 1",
            "Bore 2",
            "Num id",
            "Image"
        )
        val FAKE_PURCHASE = Purchase(
            1,
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

private fun <E> ArrayList<E>.getFlow(): Flow<List<E>> = flow {
    emit(this@getFlow)
}.flowOn(Dispatchers.IO)
