package al.ahgitdevelopment.municion.utils

import al.ahgitdevelopment.municion.BaseFragment
import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.datamodel.Purchase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

fun <E> List<E>.checkMaxFreeItems(): Boolean = this.size < BaseFragment.MAX_FREE_ITEMS

fun <E> List<E>.toFlow() = flow {
    emit(this@toFlow)
}.flowOn(Dispatchers.IO)

@ExperimentalCoroutinesApi
inline fun <reified T : Any> DatabaseReference.queryLicensesAsFlow(
    nodeSelector: DatabaseReference.() -> DatabaseReference = { this },
): Flow<T> =
    callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val licenses = arrayListOf<License>()
                snapshot.children.forEach { dataSnapshot ->
                    dataSnapshot.getValue(License::class.java)?.let { license ->
                        licenses.add(license)
                    }
                }
                trySend(licenses as T)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        addValueEventListener(valueEventListener)

        awaitClose { removeEventListener(valueEventListener) }
    }

@ExperimentalCoroutinesApi
inline fun <reified T : Any> DatabaseReference.queryPropertiesAsFlow(
    nodeSelector: DatabaseReference.() -> DatabaseReference = { this },
): Flow<T> =
    callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val properties = arrayListOf<Property>()
                snapshot.children.forEach { dataSnapshot ->
                    dataSnapshot.getValue(Property::class.java)?.let { property ->
                        properties.add(property)
                    }
                }
                trySend(properties as T)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        addValueEventListener(valueEventListener)

        awaitClose { removeEventListener(valueEventListener) }
    }

@ExperimentalCoroutinesApi
inline fun <reified T : Any> DatabaseReference.queryPurchasesAsFlow(
    nodeSelector: DatabaseReference.() -> DatabaseReference = { this },
): Flow<T> =
    callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val purchases = arrayListOf<Purchase>()
                snapshot.children.forEach { dataSnapshot ->
                    dataSnapshot.getValue(Purchase::class.java)?.let { purchase ->
                        purchases.add(purchase)
                    }
                }
                trySend(purchases as T)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        addValueEventListener(valueEventListener)

        awaitClose { removeEventListener(valueEventListener) }
    }

@ExperimentalCoroutinesApi
inline fun <reified T : Any> DatabaseReference.queryCompetitionsAsFlow(
    nodeSelector: DatabaseReference.() -> DatabaseReference = { this },
): Flow<T> =
    callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val competitions = arrayListOf<Competition>()
                snapshot.children.forEach { dataSnapshot ->
                    dataSnapshot.getValue(Competition::class.java)?.let { competition ->
                        competitions.add(competition)
                    }
                }
                trySend(competitions as T)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        addValueEventListener(valueEventListener)

        awaitClose { removeEventListener(valueEventListener) }
    }
