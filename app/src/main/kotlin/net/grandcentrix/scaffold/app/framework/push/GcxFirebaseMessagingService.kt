package net.grandcentrix.scaffold.app.framework.push

import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.grandcentrix.scaffold.app.usecases.push.OnNewPushTokenUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class GcxFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    private val onNewPushToken by inject<OnNewPushTokenUseCase>()

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)

        CoroutineScope(Dispatchers.IO).launch {
            onNewPushToken(newToken)
        }
    }
}
