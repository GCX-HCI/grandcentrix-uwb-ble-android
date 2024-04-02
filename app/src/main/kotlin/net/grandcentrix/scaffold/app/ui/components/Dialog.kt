package net.grandcentrix.scaffold.app.ui.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.grandcentrix.scaffold.app.R

@Composable
fun SimpleTextDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(text = stringResource(id = R.string.generic_dialog_confirm))
            }
        },
        onDismissRequest = { onDismiss() },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = stringResource(id = R.string.generic_dialog_dismiss))
            }
        },
        modifier = modifier
    )
}
