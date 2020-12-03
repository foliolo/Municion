package al.ahgitdevelopment.municion.repository.firebase

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.repository.DataSourceContract
import al.ahgitdevelopment.municion.repository.database.KEY_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject

// @ExperimentalCoroutinesApi
// private fun DatabaseReference.perform(): Flow<ArrayList<License>> = callbackFlow {
//     addValueEventListener(object : ValueEventListener {
//         override fun onDataChange(snapshot: DataSnapshot) {
//             val licenses = arrayListOf<License>()
//             snapshot.children.forEach { dataSnapshot ->
//                 dataSnapshot.getValue(License::class.java)?.let { license ->
//                     licenses.add(license)
//                 }
//             }
//             offer(licenses)
//         }
//
//         override fun onCancelled(error: DatabaseError) {
//             offer(listOf<ArrayList<License>>())
//         }
//     })
// }

@ExperimentalCoroutinesApi
inline fun <reified T : Any> DatabaseReference.queryAsFlow(nodeSelector: DatabaseReference.() -> DatabaseReference = { this }): Flow<T> =
    callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val licenses = arrayListOf<License>()
                snapshot.children.forEach { dataSnapshot ->
                    dataSnapshot.getValue(License::class.java)?.let { license ->
                        licenses.add(license)
                    }
                }
                offer(licenses)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        addValueEventListener(valueEventListener)

        awaitClose { removeEventListener(valueEventListener) }
        // nodeSelector.removeEventListener(valueEventListener)
    }

@ExperimentalCoroutinesApi
class RemoteDataSource @Inject internal constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) : DataSourceContract {

    // override var licenses: Flow<List<License>> = auth.currentUser?.let { userId ->
    //     database.reference.child(ROOT).child(userId.uid).child(LICENSES).queryAsFlow()
    // }
    override var licenses: Flow<List<License>> =
        database.reference.child(ROOT).child(auth.currentUser!!.uid).child(LICENSES).queryAsFlow()

    override var properties: Flow<List<Property>>
        get() = TODO("Not yet implemented")
        set(value) {}

    override var purchases: Flow<List<Purchase>>
        get() = TODO("Not yet implemented")
        set(value) {}

    // override var licenses: Flow<List<License>> = flow<List<License>> {
    //     auth.currentUser?.let { userId ->
    //         database.reference.child(ROOT).child(userId.uid).child(LICENSES)
    //             .addValueEventListener(object : ValueEventListener {
    //                 override fun onDataChange(snapshot: DataSnapshot) {
    //                     val licenses = arrayListOf<License>()
    //                     snapshot.children.forEach { dataSnapshot ->
    //                         dataSnapshot.getValue(License::class.java)?.let { license ->
    //                             licenses.add(license)
    //                         }
    //                     }
    //                     GlobalScope.launch() {
    //                         emit(licenses)
    //                     }
    //                 }
    //
    //                 override fun onCancelled(error: DatabaseError) {
    //                     GlobalScope.launch() {
    //                         emit(listOf())
    //                     }
    //                 }
    //             })
    //     }
    // }.flowOn(Dispatchers.IO)

    // override suspend fun getLicenses(): Flow<List<License>> =  flow<List<License>> {
    //     auth.currentUser?.let { userId ->
    //         database.reference.child(ROOT).child(userId.uid).child(LICENSES)
    //             .addValueEventListener(object : ValueEventListener {
    //                 override fun onDataChange(snapshot: DataSnapshot) {
    //                     val licenses = arrayListOf<License>()
    //                     snapshot.children.forEach { dataSnapshot ->
    //                         dataSnapshot.getValue(License::class.java)?.let { license ->
    //                             licenses.add(license)
    //                         }
    //                     }
    //                     this@flow.emit(value = licenses)
    //                 }
    //
    //                 override fun onCancelled(error: DatabaseError) {
    //                     this@flow.emit( listOf())
    //                 }
    //             })
    //     }
    // }.flowOn(Dispatchers.IO)

// override var licenses: Flow<List<License>> = channelFlow {
//     auth.currentUser?.let { userId ->
//         database.reference.child(ROOT).child(userId.uid).child(LICENSES)
//             .addValueEventListener(object : ValueEventListener {
//                 override fun onDataChange(snapshot: DataSnapshot) {
//                     val licenses = arrayListOf<License>()
//                     snapshot.children.forEach { dataSnapshot ->
//                         dataSnapshot.getValue(License::class.java)?.let { license ->
//                             licenses.add(license)
//                         }
//                     }
//                     GlobalScope.launch(Dispatchers.Default) {
//                         this@channelFlow.send(licenses)
//                     }
//                 }
//
//                 override fun onCancelled(error: DatabaseError) {
//                     GlobalScope.launch(Dispatchers.Default) {
//                         this@channelFlow.send(listOf())
//                     }
//                 }
//             })
//     }
//     this.channel.invokeOnClose { it?.stackTrace }
// }

    // override var licenses: Flow<List<License>> = flow<List<License>> {
    //     auth.currentUser?.let { userId ->
    //         database.reference.child(ROOT).child(userId.uid).child("licenses")
    //         object : ValueEventListener {
    //             override fun onDataChange(snapshot: DataSnapshot) {
    //                 // this@flow.emit(snapshot.getValue(List::class.java) as List<License> )
    //                 this@flow.emit(listOf<License>())
    //                 // snapshot.children.forEach{
    //                 //     arrayListOf<License>().add(it.getValue<License>())
    //                 // }
    //             }
    //
    //             override fun onCancelled(error: DatabaseError) {
    //                 TODO("Not yet implemented")
    //             }
    //         }
    //     }
    // }.flowOn(Dispatchers.IO)

    override var competitions: Flow<List<Competition>>
        get() = TODO("Not yet implemented")
        set(value) {}

    override suspend fun saveProperty(property: Property) {
        Timber.v("Save Purchase on Firebase with id: ${property.id}")
        auth.currentUser?.let { userId ->
            database.reference.child(ROOT).child(userId.uid).child(PROPERTIES).push().setValue(property)
        }
    }

    override suspend fun savePurchase(purchase: Purchase) {
        Timber.v("Save Purchase on Firebase with id: ${purchase.id}")
        auth.currentUser?.let { userId ->
            database.reference.child(ROOT).child(userId.uid).child(PURCHASES).push().setValue(purchase)
        }
    }

    override suspend fun saveLicense(license: License) {
        Timber.v("Save License on Firebase with id: ${license.id}")
        auth.currentUser?.let { userId ->
            database.reference.child(ROOT).child(userId.uid).child(LICENSES).push().setValue(license)
        }
    }

    override suspend fun saveCompetition(competition: Competition) {
        Timber.v("Save License on Firebase with id: ${competition.id}")
        auth.currentUser?.let { userId ->
            database.reference.child(ROOT).child(userId.uid).child(COMPETITIONS).push().setValue(competition)
        }
    }

    override suspend fun removeProperty(id: String) {
        Timber.v("Remove Competition on Property with id: $id")
        auth.currentUser?.let { userId ->
            database.reference.child(ROOT).child(userId.uid).child(PROPERTIES)
                .orderByChild(KEY_ID)
                .equalTo(id)
                .removeItem()
        }
    }

    override suspend fun removePurchase(id: String) {
        Timber.v("Remove Competition on Purchase with id: $id")
        auth.currentUser?.let { userId ->
            database.reference.child(ROOT).child(userId.uid).child(PURCHASES)
                .orderByChild(KEY_ID)
                .equalTo(id)
                .removeItem()
        }
    }

    override suspend fun removeCompetition(id: String) {
        Timber.v("Remove Competition on Firebase with id: $id")
        auth.currentUser?.let { userId ->
            database.reference.child(ROOT).child(userId.uid).child(COMPETITIONS)
                .orderByChild(KEY_ID)
                .equalTo(id)
                .removeItem()
        }
    }

    override suspend fun removeLicense(id: String) {
        Timber.v("Remove License on Firebase with id: $id")
        auth.currentUser?.let { userId ->
            database.reference.child(ROOT).child(userId.uid).child(LICENSES)
                .orderByChild(KEY_ID)
                .equalTo(id)
                .removeItem()
        }
    }

    override suspend fun removeAllLicenses() {
        // TODO: to be implemented and tested
        Timber.v("Remove All License on Firebase")
        auth.currentUser?.let { userId ->
            val ref = database.reference.child(ROOT).child(userId.uid).child(LICENSES).ref.removeValue()
            // val query = ref.orderByChild(License::id.name)
            //
            // query.ref.removeValue { error, _ ->
            //     if (error != null) {
            //         Timber.v("License removed from Firebase")
            //     } else {
            //         Timber.v("Error removing license from Firebase")
            //     }
            // }
        }
    }
// override var properties: Flow<List<Property>> = db.propertyDao().getProperties()
// override var purchases: Flow<List<Purchase>> = db.purchaseDao().getPurchases()
// override var licenses: Flow<List<License>> = db.licenseDao().getLicenses()
// override var competitions: Flow<List<Competition>> = db.competitionDao().getCompetitions()
//
// override suspend fun saveProperty(property: Property) = db.propertyDao().insert(property)
// override suspend fun savePurchase(purchase: Purchase) = db.purchaseDao().insert(purchase)
//
// @WorkerThread
// override suspend fun saveLicense(license: License) = db.licenseDao().insert(license)
// override suspend fun saveCompetition(competition: Competition) = db.competitionDao().insert(competition)
//
// override suspend fun removeProperty(id: Long) = db.propertyDao().delete(id)
// override suspend fun removePurchase(id: Long) = db.purchaseDao().delete(id)
//
// @WorkerThread
// override suspend fun removeLicense(id: Long) = db.licenseDao().delete(id)
// override suspend fun removeCompetition(id: Long) = db.competitionDao().delete(id)

    companion object {
        private const val ROOT = "global_database"
        private const val LICENSES = "licenses"
        private const val PURCHASES = "purchases"
        private const val COMPETITIONS = "competitions"
        private const val PROPERTIES = "properties"
    }
}

private fun Query.removeItem() = this.limitToFirst(1)
    .addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            Timber.d("Item deleted")
            snapshot.children.forEach { it.ref.removeValue() }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Timber.e(databaseError.toException(), "Failed deleting item")
        }
    })
