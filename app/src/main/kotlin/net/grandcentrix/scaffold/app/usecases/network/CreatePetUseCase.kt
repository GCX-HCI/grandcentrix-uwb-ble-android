package net.grandcentrix.scaffold.app.usecases.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.grandcentrix.either.Either
import net.grandcentrix.scaffold.backend.apis.PetsApi

class CreatePetUseCase(
    private val petsApi: PetsApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Either<Throwable, Unit> {
        return withContext(dispatcher) {
            petsApi.createPets()
        }
    }
}
