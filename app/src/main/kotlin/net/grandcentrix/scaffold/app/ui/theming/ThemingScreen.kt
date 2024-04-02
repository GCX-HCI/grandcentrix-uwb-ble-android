package net.grandcentrix.scaffold.app.ui.theming

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import net.grandcentrix.scaffold.app.ui.components.PrimaryAppButton
import net.grandcentrix.scaffold.app.ui.components.SecondaryAppButton
import net.grandcentrix.scaffold.app.ui.components.SimpleTextDialog
import net.grandcentrix.scaffold.app.ui.components.TextAppButton
import net.grandcentrix.scaffold.app.ui.theme.Dimen

@Composable
fun ThemingScreen() {
    var showDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(Dimen.screenPadding)
    ) {
        Text(text = "Headline 1", style = MaterialTheme.typography.h1)
        Text(text = "Headline 2", style = MaterialTheme.typography.h2)
        Text(text = "Headline 3", style = MaterialTheme.typography.h3)
        Text(text = "Headline 4", style = MaterialTheme.typography.h4)
        Text(text = "Headline 5", style = MaterialTheme.typography.h5)
        Text(text = "Headline 6", style = MaterialTheme.typography.h6)
        Text(text = "Subtitle 1", style = MaterialTheme.typography.subtitle1)
        Text(text = "Subtitle 2", style = MaterialTheme.typography.subtitle2)
        Text(text = "Body 1", style = MaterialTheme.typography.body1)
        Text(text = "Body 2", style = MaterialTheme.typography.body2)
        Text(text = "OVERLINE", style = MaterialTheme.typography.overline)
        Text(text = "Caption", style = MaterialTheme.typography.caption)

        PrimaryAppButton(
            onClick = { showDialog = true },
            text = "Primary button",
            modifier = Modifier.fillMaxWidth()
        )
        SecondaryAppButton(
            onClick = { /*TODO*/ },
            text = "Secondary button",
            modifier = Modifier.fillMaxWidth()
        )
        TextAppButton(
            onClick = { /*TODO*/ },
            text = "Text button",
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showDialog) {
        SimpleTextDialog(
            title = "This is a dialog",
            text = "You've pressed the button to show this dialog.",
            onConfirm = { showDialog = false },
            onDismiss = { showDialog = false }
        )
    }
}
