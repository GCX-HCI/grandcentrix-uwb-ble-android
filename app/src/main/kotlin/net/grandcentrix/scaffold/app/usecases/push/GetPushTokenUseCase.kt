package net.grandcentrix.scaffold.app.usecases.push

import kotlinx.coroutines.flow.Flow
import net.grandcentrix.scaffold.app.data.push.PushRepository

class GetPushTokenUseCase(private val repository: PushRepository) {

    operator fun invoke(): Flow<String?> = repository.getToken()
}
