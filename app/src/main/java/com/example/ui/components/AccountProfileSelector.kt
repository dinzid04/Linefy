package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.auth.GoogleAccountSession
import com.example.auth.YouTubeProfile

/** Shared compact selector for every visible YouTube account avatar. */
@Composable
fun AccountProfileSelector(
    accounts: List<GoogleAccountSession>,
    activeAccountId: String?,
    activeProfileId: String?,
    onSelect: (GoogleAccountSession, YouTubeProfile) -> Unit,
    onAddAccount: () -> Unit,
    onRemoveAccount: (GoogleAccountSession) -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var managing by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.56f))
            .clickable(onClick = onDismiss),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            color = Color(0xFF1C1C1E),
            contentColor = Color.White,
            shape = shape,
            modifier = Modifier
                .padding(top = 56.dp, start = 20.dp, end = 20.dp)
                .fillMaxWidth()
                .clip(shape)
                .border(0.5.dp, Color.White.copy(alpha = 0.12f), shape)
                .clickable(onClick = {}),
        ) {
            LazyColumn {
                item {
                    Text(
                        text = "Switch account",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(20.dp, 18.dp, 20.dp, 8.dp),
                    )
                }
                accounts.forEach { account ->
                    item {
                        Text(
                            text = account.email.ifBlank { account.name.ifBlank { "Accounts" } },
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF8E8E93),
                            modifier = Modifier.padding(20.dp, 12.dp, 20.dp, 4.dp),
                        )
                    }
                    items(account.profiles.size) { index ->
                        val profile = account.profiles[index]
                        ProfileRow(
                            profile = profile,
                            selected = account.accountId == activeAccountId && profile.profileId == activeProfileId,
                            managing = managing,
                            onClick = {
                                onSelect(account, profile)
                                onDismiss()
                            },
                            onRemove = { onRemoveAccount(account) },
                        )
                    }
                }
                item { SelectorAction(Icons.Rounded.Add, "Add account", onAddAccount) }
                item {
                    SelectorAction(Icons.Rounded.ManageAccounts, "Manage accounts") {
                        managing = !managing
                    }
                }
                item { SelectorAction(Icons.Rounded.Settings, "Settings", onOpenSettings) }
            }
        }
    }
}

@Composable
private fun ProfileRow(
    profile: YouTubeProfile,
    selected: Boolean,
    managing: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    val description = if (selected) "Selected account" else "Switch account"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.RadioButton, onClick = if (managing) onRemove else onClick)
            .semantics { contentDescription = description }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (profile.avatar != null) {
            AsyncImage(
                model = profile.avatar,
                contentDescription = null,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape),
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                modifier = Modifier.size(38.dp),
                tint = Color(0xFF8E8E93),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(profile.name, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White)
            Text(
                text = profile.handle.ifBlank {
                    if (profile.isBrandAccount) "Brand account" else "Personal"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8E8E93),
            )
        }
        if (selected && !managing) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Selected account",
                tint = Color.White,
            )
        }
        if (managing) {
            Text("Sign out", color = Color(0xFFFA2D48))
        }
    }
}

@Composable
private fun SelectorAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Color.White)
        Spacer(Modifier.width(16.dp))
        Text(label, color = Color.White)
    }
}
