package net.grandcentrix.scaffold.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.grandcentrix.scaffold.app.ui.theme.Dimen

@Composable
fun PrimaryAppButton(onClick: () -> Unit, text: String, modifier: Modifier = Modifier) {
    Button(
        onClick = { onClick() },
        modifier = modifier.padding(vertical = Dimen.verticalPadding)
    ) {
        Text(text = text)
    }
}

@Composable
fun SecondaryAppButton(onClick: () -> Unit, text: String, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = { onClick() },
        modifier = modifier.padding(vertical = Dimen.verticalPadding)
    ) {
        Text(text = text)
    }
}

@Composable
fun TextAppButton(onClick: () -> Unit, text: String, modifier: Modifier = Modifier) {
    TextButton(
        onClick = { onClick() },
        modifier = modifier.padding(vertical = Dimen.verticalPadding)
    ) {
        Text(text = text)
    }
}
