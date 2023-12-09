package ir.erfansn.nsmavpn.data.util

import android.app.usage.NetworkStats.Bucket
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.erfansn.nsmavpn.data.model.NetworkUsage
import ir.erfansn.nsmavpn.di.IoDispatcher
import ir.erfansn.nsmavpn.feature.home.util.isGrantedGetUsageStatsPermission
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class DefaultNetworkUsageTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NetworkUsageTracker {

    private val networkStatsManager = context.getSystemService<NetworkStatsManager>()!!
    override val hasPermission: Boolean
        get() = context.isGrantedGetUsageStatsPermission

    override fun trackUsage(startEpochTime: Long): Flow<NetworkUsage> =
        flow {
            while (true) {
                val wifiStatsBucket = queryWifiStatsBucket(startEpochTime)
                val mobileStatsBucket = queryMobileStatsBucket(startEpochTime)

                emit(
                    value = NetworkUsage(
                        received = wifiStatsBucket.rxBytes + mobileStatsBucket.rxBytes,
                        transmitted = wifiStatsBucket.txBytes + mobileStatsBucket.txBytes,
                    )
                )
                delay(1.seconds)
            }
        }.flowOn(ioDispatcher)

    private fun queryWifiStatsBucket(startEpochTime: Long) = queryStatsBucket(
        networkType = ConnectivityManager.TYPE_WIFI,
        startEpochTime = startEpochTime,
    )

    private fun queryMobileStatsBucket(startEpochTime: Long) = queryStatsBucket(
        networkType = ConnectivityManager.TYPE_MOBILE,
        startEpochTime = startEpochTime,
    )

    private fun queryStatsBucket(
        networkType: Int,
        startEpochTime: Long,
    ): Bucket {
        check(hasPermission)

        return networkStatsManager.querySummaryForDevice(
            networkType,
            null,
            startEpochTime,
            Long.MAX_VALUE,
        ) ?: Bucket()
    }
}

interface NetworkUsageTracker {
    val hasPermission: Boolean

    fun trackUsage(startEpochTime: Long): Flow<NetworkUsage>
}
