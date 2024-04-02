package net.grandcentrix.scaffold.app.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.accompanist.glide.rememberGlidePainter
import net.grandcentrix.scaffold.app.R
import net.grandcentrix.scaffold.app.domain.auth.AuthorizationState
import net.grandcentrix.scaffold.app.ui.components.PrimaryAppButton
import net.grandcentrix.scaffold.app.ui.components.SecondaryAppButton
import net.grandcentrix.scaffold.app.ui.shared.AccountViewModel
import net.grandcentrix.scaffold.app.ui.theme.Dimen
import org.koin.androidx.compose.getViewModel

@Composable
fun ProfileScreen(accountViewModel: AccountViewModel = getViewModel()) {
    val authState by accountViewModel.authState
        .collectAsState(initial = AuthorizationState.LoggedOut)
    val profile by accountViewModel.profileData.collectAsState(initial = null)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(Dimen.screenPadding)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(
                id = R.string.profile_text_state_auth,
                authState.toString()
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimen.horizontalPadding)
        )

        when (authState) {
            AuthorizationState.LoggedIn -> SecondaryAppButton(
                onClick = { accountViewModel.onLogout() },
                text = stringResource(id = R.string.profile_button_logout),
                modifier = Modifier.fillMaxWidth()
            )
            AuthorizationState.LoggedOut -> PrimaryAppButton(
                onClick = { accountViewModel.onLogin(context) },
                text = stringResource(id = R.string.profile_button_login),
                modifier = Modifier.fillMaxWidth()
            )
        }

        profile?.run {
            Row(
                modifier = Modifier.padding(vertical = Dimen.verticalPadding)
            ) {
                Image(
                    painter = rememberGlidePainter(profileImageUrl),
                    contentDescription = stringResource(id = R.string.profile_image_description),
                    modifier = Modifier.size(Dimen.profileImageSize)
                )

                Column(
                    modifier = Modifier.padding(horizontal = Dimen.horizontalPadding)
                ) {
                    Text(
                        text = "$emailAddress",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "$userId",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
