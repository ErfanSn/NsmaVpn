package ir.erfansn.nsmavpn.data.repository

import ir.erfansn.nsmavpn.data.model.Profile
import ir.erfansn.nsmavpn.data.source.local.UserPreferencesLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.model.toProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userPreferencesLocalDataSource: UserPreferencesLocalDataSource,
    private val externalScope: CoroutineScope,
) {
    val userProfile = userPreferencesLocalDataSource.userPreferences.map {
        it.toProfile()
    }

    suspend fun saveUserProfile(profile: Profile?) {
        externalScope.launch {
            userPreferencesLocalDataSource.saveUserProfile(profile)
        }.join()
    }
}
