package net.grandcentrix.scaffold.app.usecases.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.grandcentrix.either.Either
import net.grandcentrix.scaffold.backend.apis.PetsApi
import net.grandcentrix.scaffold.backend.models.Pet

class GetAllPetsUseCase(
    private val petsApi: PetsApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(limit: Int): Either<Throwable, List<Pet>> {
        return withContext(dispatcher) {
            petsApi.listPets(limit)
        }
    }
}
