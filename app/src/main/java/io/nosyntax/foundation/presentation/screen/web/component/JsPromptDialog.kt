package io.nosyntax.foundation.presentation.screen.web.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.nosyntax.foundation.R
import io.nosyntax.foundation.core.component.TextField
import io.nosyntax.foundation.core.util.Previews
import io.nosyntax.foundation.presentation.theme.FoundationTheme

@Composable
fun JsPromptDialog(
    message: String,
    defaultValue: String,
    onCancel: () -> Unit,
    onConfirm: (result: String) -> Unit
) {
    val promptValue = remember { mutableStateOf(defaultValue) }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        ),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.large
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                TextField(defaultValue = defaultValue, onValueChange = {
                    promptValue.value = it
                })
                Spacer(modifier = Modifier.height(20.dp))
                Row {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Button(
                        onClick = { onConfirm(promptValue.value) },
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text(text = stringResource(id = R.string.confirm))
                    }
                }
            }
        }
    )
}

@Previews
@Composable
fun JsPromptDialogPreview() {
    FoundationTheme {
        JsPromptDialog(
            message = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
            defaultValue = "Default Value",
            onCancel = { },
            onConfirm = { }
        )
    }
}