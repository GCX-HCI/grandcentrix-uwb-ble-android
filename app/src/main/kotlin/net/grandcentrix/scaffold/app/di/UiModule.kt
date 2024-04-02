package net.grandcentrix.scaffold.app.di

import net.grandcentrix.scaffold.app.ui.home.HomeViewModel
import net.grandcentrix.scaffold.app.usecases.network.CreatePetUseCase
import net.grandcentrix.scaffold.app.usecases.network.GetAllPetsUseCase
import net.grandcentrix.scaffold.app.usecases.network.GetSinglePetUseCase
import net.grandcentrix.scaffold.backend.apis.PetsApi
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val uiModule = module {
    single { PetsApi() }
    single { GetAllPetsUseCase(petsApi = get()) }
    single { GetSinglePetUseCase(petsApi = get()) }
    single { CreatePetUseCase(petsApi = get()) }

    viewModel {
        HomeViewModel(
            getPets = get(),
            getSinglePet = get(),
            createPet = get()
        )
    }
}
