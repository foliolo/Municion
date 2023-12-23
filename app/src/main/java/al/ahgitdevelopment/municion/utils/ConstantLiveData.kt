package al.ahgitdevelopment.municion.utils

import androidx.lifecycle.LiveData

class ConstantLiveData<T>(value: T) : LiveData<T>(value)
