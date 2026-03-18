package com.greencoins.app.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.greencoins.app.components.GlassCard
import com.greencoins.app.data.AuthRepository
import com.greencoins.app.data.VerificationRepository
import com.greencoins.app.theme.AppColors
import com.greencoins.app.theme.themeCardBgColor
import com.greencoins.app.theme.themeMutedBgColor
import com.greencoins.app.theme.themeOnSurfaceTextColor
import com.greencoins.app.theme.themeOnSurfaceVariantTextColor
import com.greencoins.app.theme.themePageBgColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private data class CommunitySubmission(
    val id: String,
    val title: String,
    val description: String,
    val location: String = "",
    val votesBy: Map<String, String> = emptyMap(), // userId -> "upvote" | "downvote"
    val verifiedBy: Set<String> = emptySet(),
) {
    val upvotes: Int get() = votesBy.values.count { it == "upvote" }
    val downvotes: Int get() = votesBy.values.count { it == "downvote" }
}

@Composable
fun CommunityScreen(
    onBack: () -> Unit,
    onVerifyReward: () -> Unit = {},
    scope: CoroutineScope,
) {
    val userId = AuthRepository.currentUser?.id
    val submissions = remember {
        mutableStateListOf(
            CommunitySubmission(
                "1", "Beach Cleanup", "Cleaned 2kg plastic from local beach",
                location = "Marina Beach, Chennai",
                votesBy = (1..12).associate { "u$it" to "upvote" } + mapOf("u13" to "downvote"),
            ),
            CommunitySubmission(
                "2", "Tree Planting", "Planted 5 saplings in park",
                location = "Sector 21, Gurgaon",
                votesBy = (1..8).associate { "u$it" to "upvote" },
            ),
            CommunitySubmission(
                "3", "Recycling Drive", "Organized neighborhood recycling",
                location = "Indiranagar, Bangalore",
                votesBy = (1..15).associate { "u$it" to "upvote" } + mapOf("u16" to "downvote", "u17" to "downvote"),
            ),
        )
    }
    var reasonInput by remember { mutableStateOf("") }
    var selectedSubmissionId by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var verificationStatusBySubmission by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            selectedImageBitmap = null
        }
        showImageSourceDialog = false
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            selectedImageBitmap = it
            selectedImageUri = null
        }
        showImageSourceDialog = false
    }

    LaunchedEffect(userId, submissions) {
        if (userId != null) {
            val statuses = submissions.associate { sub ->
                sub.id to (VerificationRepository.getVerificationStatus(userId, sub.id) ?: "")
            }.filter { it.value.isNotEmpty() }
            if (statuses.isNotEmpty()) {
                verificationStatusBySubmission = verificationStatusBySubmission + statuses
            }
        }
    }

    val pageBg = themePageBgColor()
    val textColor = themeOnSurfaceTextColor()
    val textSecondaryColor = themeOnSurfaceVariantTextColor()
    val cardBg = themeCardBgColor()
    val mutedBg = themeMutedBgColor()
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Attach image", color = textColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Camera")
                    }
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Gallery")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("Cancel", color = textSecondaryColor)
                }
            },
            containerColor = cardBg,
        )
    }

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

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(submissions) { sub ->
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val userVote = userId?.let { sub.votesBy[it] }
                                IconButton(
                                    onClick = {
                                        if (userId != null) {
                                            val idx = submissions.indexOf(sub)
                                            val newVotesBy = sub.votesBy + (userId to "upvote")
                                            submissions[idx] = sub.copy(votesBy = newVotesBy)
                                        }
                                    },
                                ) {
                                    Icon(
                                        Icons.Filled.ThumbUp,
                                        contentDescription = "Upvote",
                                        tint = if (userVote == "upvote") MaterialTheme.colorScheme.primary else AppColors.textSecondary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                Text("${sub.upvotes}", color = textSecondaryColor, fontSize = 12.sp)
                                Spacer(modifier = Modifier.size(8.dp))
                                IconButton(
                                    onClick = {
                                        if (userId != null) {
                                            val idx = submissions.indexOf(sub)
                                            val newVotesBy = sub.votesBy + (userId to "downvote")
                                            submissions[idx] = sub.copy(votesBy = newVotesBy)
                                        }
                                    },
                                ) {
                                    Icon(
                                        Icons.Filled.ThumbDown,
                                        contentDescription = "Downvote",
                                        tint = if (userVote == "downvote") AppColors.redLogout else AppColors.textSecondary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                Text("${sub.downvotes}", color = textSecondaryColor, fontSize = 12.sp)
                            }
                        }
                        Text(
                            sub.description,
                            color = textSecondaryColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
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
                        if (selectedSubmissionId == sub.id) {
                            OutlinedTextField(
                                value = reasonInput,
                                onValueChange = {
                                    reasonInput = it
                                    if (it.isNotBlank()) showError = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                placeholder = { Text("Reason for verification...", color = textSecondaryColor) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                ),
                                shape = RoundedCornerShape(12.dp),
                            )
                            OutlinedButton(
                                onClick = { showImageSourceDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Attach image", fontSize = 12.sp)
                            }
                            if (selectedImageUri != null || selectedImageBitmap != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .size(80.dp, 80.dp)
                                        .background(mutedBg, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (selectedImageUri != null) {
                                        AsyncImage(
                                            model = selectedImageUri,
                                            contentDescription = "Selected image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                        )
                                    } else if (selectedImageBitmap != null) {
                                        androidx.compose.foundation.Image(
                                            bitmap = selectedImageBitmap!!.asImageBitmap(),
                                            contentDescription = "Selected image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                        )
                                    }
                                }
                            }
                            val currentUserId = AuthRepository.currentUser?.id
                            val status = verificationStatusBySubmission[sub.id]
                            if (currentUserId != null && status != "approved") {
                                if (showError) {
                                    Text(
                                        "Please enter a reason and attach an image before verifying",
                                        color = AppColors.redLogout,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                                Row(
                                    modifier = Modifier.padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    androidx.compose.material3.Button(
                                        onClick = {
                                            if (reasonInput.isBlank()) {
                                                showError = true
                                                return@Button
                                            }
                                            if (selectedImageUri == null && selectedImageBitmap == null) {
                                                showError = true
                                                return@Button
                                            }
                                            showError = false
                                            isSubmitting = true
                                            scope.launch {
                                                try {
                                                    val imageBytes = when {
                                                        selectedImageUri != null -> context.contentResolver.openInputStream(selectedImageUri!!)?.use { it.readBytes() }
                                                        selectedImageBitmap != null -> {
                                                            val stream = java.io.ByteArrayOutputStream()
                                                            selectedImageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                                                            stream.toByteArray()
                                                        }
                                                        else -> null
                                                    }
                                                    if (imageBytes != null && currentUserId != null) {
                                                        val imageUrl = VerificationRepository.uploadVerificationImage(currentUserId, imageBytes)
                                                        VerificationRepository.insertVerificationRequest(sub.id, currentUserId, reasonInput, imageUrl)
                                                        verificationStatusBySubmission = verificationStatusBySubmission + (sub.id to "pending")
                                                        selectedSubmissionId = null
                                                        reasonInput = ""
                                                        selectedImageUri = null
                                                        selectedImageBitmap = null
                                                        onVerifyReward()
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                    android.widget.Toast.makeText(context, "Upload failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                                } finally {
                                                    isSubmitting = false
                                                }
                                            }
                                        },
                                        enabled = reasonInput.isNotBlank() && (selectedImageUri != null || selectedImageBitmap != null) && !isSubmitting,
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            disabledContainerColor = mutedBg,
                                            disabledContentColor = textSecondaryColor,
                                        ),
                                    ) {
                                        if (isSubmitting) {
                                            androidx.compose.material3.CircularProgressIndicator(
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        } else {
                                            Text("Verify (+5 GC)", fontSize = 12.sp)
                                        }
                                    }
                                    TextButton(
                                        onClick = {
                                            selectedSubmissionId = null
                                            showError = false
                                            selectedImageUri = null
                                            selectedImageBitmap = null
                                        },
                                    ) {
                                        Text("Cancel", color = textSecondaryColor, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            val currentUserId = AuthRepository.currentUser?.id
                            val status = verificationStatusBySubmission[sub.id]
                            if (currentUserId != null && status != "approved" && status != "pending") {
                                TextButton(
                                    onClick = {
                                        selectedSubmissionId = sub.id
                                        selectedImageUri = null
                                        selectedImageBitmap = null
                                    },
                                    modifier = Modifier.padding(top = 8.dp),
                                ) {
                                    Text("Add verification", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                }
                            } else if (currentUserId != null && status == "approved") {
                                Text(
                                    "✓ Verified by you (+5 GC)",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            } else if (currentUserId != null && status == "pending") {
                                Text(
                                    "Verification submitted — Pending approval",
                                    color = textSecondaryColor,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
