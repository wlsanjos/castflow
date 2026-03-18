package com.wlsanjos.castflow.samsung.di

import android.app.Application
import android.content.Context
import com.wlsanjos.castflow.samsung.api.SamsungConnectionService
import com.wlsanjos.castflow.samsung.api.SamsungDiscoveryService
import com.wlsanjos.castflow.samsung.impl.SamsungConnectionServiceImpl
import com.wlsanjos.castflow.samsung.impl.SsdpSamsungDiscoveryService
import com.wlsanjos.castflow.samsung.state.ConnectedDeviceStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SamsungModule {
    @Provides
    @Singleton
    fun provideSamsungConnectionService(): SamsungConnectionService = SamsungConnectionServiceImpl()

    @Provides
    @Singleton
    fun provideSamsungDiscoveryService(
        @ApplicationContext context: Context,
        connectionService: SamsungConnectionService
    ): SamsungDiscoveryService = SsdpSamsungDiscoveryService(context, connectionService)

    @Provides
    @Singleton
    fun provideConnectedDeviceStore(): ConnectedDeviceStore = ConnectedDeviceStore()
}
