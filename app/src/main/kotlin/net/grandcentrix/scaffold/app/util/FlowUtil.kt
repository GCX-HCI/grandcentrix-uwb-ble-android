package net.grandcentrix.scaffold.app.util

import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

fun <T> Flow<T>.collectWhenCreated(
    lifecycleScope: LifecycleCoroutineScope,
    block: suspend CoroutineScope.(T) -> Unit
) = lifecycleScope.launchWhenCreated {
    collect { block(it) }
}

fun <T> Flow<T>.collectWhenStarted(
    lifecycleScope: LifecycleCoroutineScope,
    block: suspend CoroutineScope.(T) -> Unit
) = lifecycleScope.launchWhenStarted {
    collect { block(it) }
}

fun <T> Flow<T>.collectWhenResumed(
    lifecycleScope: LifecycleCoroutineScope,
    block: suspend CoroutineScope.(T) -> Unit
) = lifecycleScope.launchWhenResumed {
    collect { block(it) }
}
