package net.grandcentrix.scaffold.app.usecases.push

import net.grandcentrix.scaffold.app.data.push.PushRepository

class OnNewPushTokenUseCase(private val repository: PushRepository) {

    suspend operator fun invoke(newPushToken: String) = repository.onNewToken(newPushToken)
}
