package net.grandcentrix.scaffold.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import net.grandcentrix.scaffold.app.usecases.network.CreatePetUseCase
import net.grandcentrix.scaffold.app.usecases.network.GetAllPetsUseCase
import net.grandcentrix.scaffold.app.usecases.network.GetSinglePetUseCase

class HomeViewModel(
    private val getPets: GetAllPetsUseCase,
    private val getSinglePet: GetSinglePetUseCase,
    private val createPet: CreatePetUseCase
) : ViewModel() {
    private val _result = MutableStateFlow("Response will be visible here.")
    val result: Flow<String> = _result
    private fun setResult(result: String) = _result.tryEmit(result)

    private val _showServerExplanation = MutableStateFlow(false)
    val showServerExplanation: Flow<Boolean> = _showServerExplanation
    private fun setShowServerExplanation(show: Boolean) = _showServerExplanation.tryEmit(show)

    fun onRequestPetsSelected() {
        viewModelScope.launch {
            setResult("Loading...")
            try {
                getPets(limit = 5)
                    .map { pets ->
                        var resultString = "Found ${pets.size} pets: \n\n\n"
                        pets.forEach {
                            resultString += "id: ${it.id} name: ${it.name} tag: ${it.tag}\n\n"
                        }
                        setResult(resultString)
                    }
            } catch (e: Exception) {
                setResult(
                    """
                    There was a error while requesting all pets:
                    ${e.message}
                    
                    Is your local server running?
                    """.trimIndent()
                )
            }
        }
    }

    fun onRequestSinglePetSelected() {
        viewModelScope.launch {
            setResult("Loading...")
            try {
                getSinglePet("128901231129")
                    .map { pet ->
                        setResult(
                            """
                        Found pet:
                        
                        id: ${pet.id}
                        name: ${pet.name}
                        tag: ${pet.tag}
                            """.trimIndent()
                        )
                    }
            } catch (e: Exception) {
                setResult(
                    """
                    There was a error while requesting a pet:
                    ${e.message}
                    
                    Is your local server running?
                    """.trimIndent()
                )
            }
        }
    }

    fun onCreatePetSelected() {
        viewModelScope.launch {
            setResult("Loading...")
            try {
                createPet()
                    .map { setResult("Pet created!") }
            } catch (e: Exception) {
                setResult(
                    """
                    There was a error while creating a pet:
                    ${e.message}
                    
                    Is your local server running?
                    """.trimIndent()
                )
            }
        }
    }

    fun onShowServerExplanation() = setShowServerExplanation(true)
    fun onDismissServerExplanation() = setShowServerExplanation(false)
}
