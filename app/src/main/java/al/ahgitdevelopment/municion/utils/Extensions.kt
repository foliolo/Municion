package al.ahgitdevelopment.municion.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

fun <E> List<E>.toFlow() = flow {
    emit(this@toFlow)
}.flowOn(Dispatchers.IO)
