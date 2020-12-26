package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.ads.RewardedAdCallbackManager
import al.ahgitdevelopment.municion.ads.RewardedAdLoadCallbackManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AdsModule {

    @Provides
    fun providesRewardedAdCallbackManager() = RewardedAdCallbackManager()

    @Provides
    fun providesRewardedAdLoadCallbackManager() = RewardedAdLoadCallbackManager()
}
