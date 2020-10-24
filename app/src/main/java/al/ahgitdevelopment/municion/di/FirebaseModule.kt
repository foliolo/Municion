package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.firebase.FirebaseImageRepository
import al.ahgitdevelopment.municion.ui.tutorial.TutorialImagesRepository
import android.content.Context
import android.net.ConnectivityManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ApplicationComponent::class)
class FirebaseModule {

    @Provides
    @Reusable
    fun provideCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @Provides
    @Reusable
    fun provideAnalytics(): FirebaseAnalytics = Firebase.analytics

    @Provides
    @Reusable
    fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Reusable
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Reusable
    fun provideStorage(): FirebaseStorage = Firebase.storage

    @Provides
    @Reusable
    fun provideConnectivityManager(@ApplicationContext appContext: Context): ConnectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides
    @Reusable
    fun provideTutorialImagesRepository(@ApplicationContext appContext: Context): TutorialImagesRepository =
        FirebaseImageRepository(
            appContext,
            provideAuth(),
            provideStorage(),
            provideCrashlytics(),
            provideConnectivityManager(appContext)
        )
}
