package ir.erfansn.nsmavpn.feature.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.data.model.Profile
import ir.erfansn.nsmavpn.data.repository.ServersRepository
import ir.erfansn.nsmavpn.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val serversRepository: ServersRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignInUiState>(SignInUiState.SignedOut)
    val uiState = _uiState.asStateFlow()

    init {
        serversRepository.stopVpnServersWorker()
    }

    fun verifyVpnGateSubscription(userAccount: GoogleSignInAccount) {
        viewModelScope.launch {
            _uiState.update {
                if (serversRepository.isSubscribedToVpnGateDailyMail(userAccount.email!!)) {
                    SignInUiState.SignIn(userAccount = userAccount)
                } else {
                    SignInUiState.Error(messageId = R.string.not_being_subscribed_to_vpngate)
                }
            }
        }
    }

    fun saveUserAccountInfo(account: GoogleSignInAccount) {
        viewModelScope.launch {
            val profile = Profile(
                avatarUrl = account.photoUrl(size = 512u),
                displayName = account.displayName!!,
                emailAddress = account.email!!
            )

            profileRepository.saveUserProfile(profile)
        }
    }
}

private fun GoogleSignInAccount.photoUrl(size: UInt) = photoUrl?.let {
    it.toString().substringBeforeLast('=') + "=s$size"
}

sealed interface SignInUiState {
    object SignedOut : SignInUiState
    data class SignIn(val userAccount: GoogleSignInAccount) : SignInUiState
    data class Error(val messageId: Int) : SignInUiState
}
