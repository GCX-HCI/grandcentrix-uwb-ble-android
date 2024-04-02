package net.grandcentrix.scaffold.app.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.grandcentrix.scaffold.app.R
import net.grandcentrix.scaffold.app.ui.components.SimpleTextDialog
import net.grandcentrix.scaffold.app.ui.components.TextAppButton
import net.grandcentrix.scaffold.app.ui.theme.Dimen
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeScreen(homeViewModel: HomeViewModel = getViewModel()) {
    val showServerExplanation by homeViewModel.showServerExplanation.collectAsState(initial = false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimen.screenPadding)
    ) {

        Text(
            text = stringResource(id = R.string.home_fragment),
            style = MaterialTheme.typography.h4,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 64.dp)
        )

        Divider()

        NetworkTestSection(homeViewModel = homeViewModel)

        if (showServerExplanation) {
            SimpleTextDialog(
                title = stringResource(id = R.string.home_network_start_server_dialog_title),
                text = stringResource(id = R.string.home_network_start_server_dialog_text),
                onConfirm = { homeViewModel.onDismissServerExplanation() },
                onDismiss = { homeViewModel.onDismissServerExplanation() }
            )
        }
    }
}

@Composable
fun NetworkTestSection(homeViewModel: HomeViewModel) {
    val apiResult by homeViewModel.result.collectAsState(initial = "")

    Text(
        text = stringResource(id = R.string.home_network_headline),
        style = MaterialTheme.typography.h6,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
    )

    Text(
        text = stringResource(id = R.string.home_network_description),
        style = MaterialTheme.typography.body1,
        modifier = Modifier
            .clickable { homeViewModel.onShowServerExplanation() }
            .padding(vertical = Dimen.verticalPadding)
    )

    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextAppButton(
            onClick = { homeViewModel.onRequestPetsSelected() },
            text = stringResource(id = R.string.home_network_request_pets)
        )
        TextAppButton(
            onClick = { homeViewModel.onRequestSinglePetSelected() },
            text = stringResource(id = R.string.home_network_request_single_pet)
        )
        TextAppButton(
            onClick = { homeViewModel.onCreatePetSelected() },
            text = stringResource(id = R.string.home_network_request_create_pet)
        )
    }

    Card(
        modifier = Modifier
            .padding(Dimen.horizontalPadding)
            .animateContentSize()
    ) {
        Text(
            text = apiResult,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.horizontalPadding, vertical = Dimen.verticalPadding),
            fontFamily = FontFamily.Monospace
        )
    }
}
