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
import al.ahgitdevelopment.municion.repository.database.RepositoryInterface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
class FakeRepository : RepositoryInterface {

    var licenses: ArrayList<License> = arrayListOf()
    var property: ArrayList<Property> = arrayListOf()
    var purchase: ArrayList<Purchase> = arrayListOf()
    var competition: ArrayList<Competition> = arrayListOf()

    private var shouldReturnError = false

    // private val observableTasks = MutableLiveData<Result<List<T>>>()

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }
    //
    // override suspend fun refreshTask(taskId: String) {
    //     refreshTasks()
    // }
    //
    // override fun observeTasks(): LiveData<Result<List<Task>>> {
    //     runBlocking { refreshTasks() }
    //     return observableTasks
    // }
    //
    // override fun observeTask(taskId: String): LiveData<Result<Task>> {
    //     runBlocking { refreshTasks() }
    //     return observableTasks.map { tasks ->
    //         when (tasks) {
    //             is Result.Loading -> Result.Loading
    //             is Error -> Error(tasks.exception)
    //             is Success -> {
    //                 val task = tasks.data.firstOrNull() { it.id == taskId }
    //                     ?: return@map Error(Exception("Not found"))
    //                 Success(task)
    //             }
    //         }
    //     }
    // }
    //
    // override suspend fun getTask(taskId: String, forceUpdate: Boolean): Result<Task> {
    //     if (shouldReturnError) {
    //         return Error(Exception("Test exception"))
    //     }
    //     tasksServiceData[taskId]?.let {
    //         return Success(it)
    //     }
    //     return Error(Exception("Could not find task"))
    // }
    //
    // override suspend fun getTasks(forceUpdate: Boolean): Result<List<Task>> {
    //     if (shouldReturnError) {
    //         return Error(Exception("Test exception"))
    //     }
    //     return Success(tasksServiceData.values.toList())
    // }
    //
    // override suspend fun saveTask(task: Task) {
    //     tasksServiceData[task.id] = task
    // }
    //
    // override suspend fun completeTask(task: Task) {
    //     val completedTask = Task(task.title, task.description, true, task.id)
    //     tasksServiceData[task.id] = completedTask
    //     refreshTasks()
    // }
    //
    // override suspend fun completeTask(taskId: String) {
    //     // Not required for the remote data source.
    //     throw NotImplementedError()
    // }
    //
    // override suspend fun activateTask(task: Task) {
    //     val activeTask = Task(task.title, task.description, false, task.id)
    //     tasksServiceData[task.id] = activeTask
    //     refreshTasks()
    // }
    //
    // override suspend fun activateTask(taskId: String) {
    //     throw NotImplementedError()
    // }
    //
    // override suspend fun clearCompletedTasks() {
    //     tasksServiceData = tasksServiceData.filterValues {
    //         !it.isCompleted
    //     } as LinkedHashMap<String, Task>
    // }
    //
    // override suspend fun deleteTask(taskId: String) {
    //     tasksServiceData.remove(taskId)
    //     refreshTasks()
    // }
    //
    // override suspend fun deleteAllTasks() {
    //     tasksServiceData.clear()
    //     refreshTasks()
    // }
    //
    // @VisibleForTesting
    // fun addTasks(vararg tasks: Task) {
    //     for (task in tasks) {
    //         tasksServiceData[task.id] = task
    //     }
    //     runBlocking { refreshTasks() }
    // }

    override suspend fun getProperties(): LiveData<List<Property>>? {
        TODO("Not yet implemented")
    }

    override suspend fun getPurchases(): LiveData<List<Purchase>>? {
        TODO("Not yet implemented")
    }

    override suspend fun getLicenses(): LiveData<List<License>>? {
        if (shouldReturnError) {
            return null
        }

        return MutableLiveData<List<License>>().apply {
            value = licenses
        }
    }

    override suspend fun getCompetition(): LiveData<List<Competition>>? {
        TODO("Not yet implemented")
    }

    override suspend fun saveProperty(property: Property) {
        TODO("Not yet implemented")
    }

    override suspend fun savePurchase(purchase: Purchase) {
        TODO("Not yet implemented")
    }

    override suspend fun saveLicense(license: License) {
        licenses.add(license)
    }

    override suspend fun saveCompetition(competition: Competition) {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun fetchDataFromFirebase() {
        TODO("Not yet implemented")
    }

    override fun uploadDataToFirebase() {
        TODO("Not yet implemented")
    }
}
