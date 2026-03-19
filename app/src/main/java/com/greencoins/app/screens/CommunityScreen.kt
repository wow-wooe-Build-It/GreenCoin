package com.greencoins.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.greencoins.app.components.GlassCard
import com.greencoins.app.data.AuthRepository
import com.greencoins.app.theme.AppColors
import com.greencoins.app.theme.themeOnSurfaceTextColor
import com.greencoins.app.theme.themeOnSurfaceVariantTextColor
import com.greencoins.app.theme.themePageBgColor

@Composable
fun CommunityScreen(
    onBack: () -> Unit,
    onVerifyReward: () -> Unit = {},
    scope: kotlinx.coroutines.CoroutineScope,
) {
    val viewModel: CommunityViewModel = viewModel()
    val submissions by viewModel.submissions.collectAsState()
    val commentsBySubmission by viewModel.commentsBySubmission.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedPreviewImageUrl by remember { mutableStateOf<String?>(null) }

    val pageBg = themePageBgColor()
    val textColor = themeOnSurfaceTextColor()
    val textSecondaryColor = themeOnSurfaceVariantTextColor()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .padding(16.dp),
    ) {
        Text(
            text = "Community Verification",
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Verify eco actions and earn +5 GC per verification",
            color = textSecondaryColor,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(modifier = Modifier.size(24.dp))

        if (selectedPreviewImageUrl != null) {
            Dialog(
                onDismissRequest = { selectedPreviewImageUrl = null },
                properties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable { selectedPreviewImageUrl = null },
                ) {
                    AsyncImage(
                        model = selectedPreviewImageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (submissions.isEmpty()) {
                item {
                    Text(
                        "No submissions yet",
                        color = textSecondaryColor,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(32.dp),
                    )
                }
            } else {
                items(submissions) { sub ->
                    var commentText by remember { mutableStateOf("") }

                    LaunchedEffect(sub.id) {
                        viewModel.loadComments(sub.id)
                    }

                    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = null) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        Icons.Filled.VerifiedUser,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Text(
                                        sub.title,
                                        color = textColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val userVote = sub.votesBy.values.firstOrNull()
                                    Icon(
                                        Icons.Filled.ThumbUp,
                                        contentDescription = "Upvote",
                                        tint = if (userVote == "upvote") MaterialTheme.colorScheme.primary else textSecondaryColor,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { viewModel.vote(sub.id, "upvote") },
                                    )
                                    Text("${sub.upvotes}", color = textSecondaryColor, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Icon(
                                        Icons.Filled.ThumbDown,
                                        contentDescription = "Downvote",
                                        tint = if (userVote == "downvote") AppColors.redLogout else textSecondaryColor,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { viewModel.vote(sub.id, "downvote") },
                                    )
                                    Text("${sub.downvotes}", color = textSecondaryColor, fontSize = 12.sp)
                                }
                            }
                            Text(
                                sub.description,
                                color = textSecondaryColor,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                            if (sub.beforeImageUrl != null || sub.afterImageUrl != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    sub.beforeImageUrl?.let { url ->
                                        Column(modifier = Modifier.weight(1f)) {
                                            AsyncImage(
                                                model = url,
                                                contentDescription = "Before",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(100.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { selectedPreviewImageUrl = url },
                                                contentScale = ContentScale.Crop,
                                            )
                                            Text(
                                                "Before",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = textSecondaryColor,
                                                modifier = Modifier.padding(top = 4.dp),
                                            )
                                        }
                                    }
                                    sub.afterImageUrl?.let { url ->
                                        Column(modifier = Modifier.weight(1f)) {
                                            AsyncImage(
                                                model = url,
                                                contentDescription = "After",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(100.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { selectedPreviewImageUrl = url },
                                                contentScale = ContentScale.Crop,
                                            )
                                            Text(
                                                "After",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = textSecondaryColor,
                                                modifier = Modifier.padding(top = 4.dp),
                                            )
                                        }
                                    }
                                }
                            }
                            if (sub.location.isNotBlank()) {
                                Row(
                                    modifier = Modifier.padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        Icons.Filled.Place,
                                        contentDescription = null,
                                        tint = textSecondaryColor,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        sub.location,
                                        color = textSecondaryColor,
                                        fontSize = 11.sp,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Add a comment...", color = textSecondaryColor) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                ),
                                shape = RoundedCornerShape(12.dp),
                            )
                            Button(
                                onClick = {
                                    viewModel.addComment(sub.id, commentText)
                                    commentText = ""
                                },
                                modifier = Modifier.padding(top = 8.dp),
                            ) {
                                Text("Post")
                            }
                            val comments = commentsBySubmission[sub.id] ?: emptyList()
                            val currentUserId = AuthRepository.currentUser?.id ?: ""
                            if (comments.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    comments.forEach { comment ->
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = comment.userName,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = textColor,
                                                )
                                                Text(
                                                    text = comment.comment,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = textSecondaryColor,
                                                )
                                            }
                                            if (comment.userId == currentUserId) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clickable {
                                                            viewModel.deleteComment(sub.id, comment.id)
                                                        },
                                                    tint = textSecondaryColor,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
