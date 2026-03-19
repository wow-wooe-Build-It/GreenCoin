package com.greencoins.app.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.greencoins.app.components.GlassCard
import com.greencoins.app.data.AuthRepository
import com.greencoins.app.data.Mission
import com.greencoins.app.data.MissionRepository
import com.google.android.gms.location.LocationServices
import java.util.Locale
import androidx.compose.material3.MaterialTheme
import com.greencoins.app.theme.AppColors
import com.greencoins.app.ui.toImageVector
import com.greencoins.app.data.Submission
import com.greencoins.app.data.SubmissionVerificationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class PlusStep { object Selection : PlusStep(); object Brief : PlusStep(); object Upload : PlusStep(); object Success : PlusStep() }

@Composable
fun PlusFlow(
    step: PlusStep,
    missionId: String?,
    onSelectMission: (String) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit,
    onMissionSubmitted: () -> Unit = {},
) {
    var missions by remember { mutableStateOf<List<Mission>>(emptyList()) }
    var selectedMission by remember { mutableStateOf<Mission?>(null) }
    var finalSubmission by remember { mutableStateOf<Submission?>(null) }
    
    // Fetch all missions for selection list
    LaunchedEffect(Unit) {
        missions = MissionRepository.getMissions()
    }

    // Fetch specific mission when ID changes
    LaunchedEffect(missionId) {
        if (missionId != null) {
            selectedMission = MissionRepository.getMission(missionId)
        }
    }

    when (step) {
        is PlusStep.Selection -> PlusSelectionStep(missions = missions, onSelectMission = onSelectMission, onCancel = onCancel)
        is PlusStep.Brief -> {
            if (selectedMission != null) {
                PlusBriefStep(mission = selectedMission!!, onNext = onNext, onCancel = onCancel)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        }
        is PlusStep.Upload -> {
            if (selectedMission != null) {
                PlusUploadStep(
                    mission = selectedMission!!, 
                    onNext = { sub -> 
                        finalSubmission = sub
                        onNext() 
                    }, 
                    onCancel = onCancel, 
                    onMissionSubmitted = onMissionSubmitted
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        }
        is PlusStep.Success -> {
            if (selectedMission != null && finalSubmission != null) {
                PlusSuccessStep(mission = selectedMission!!, submission = finalSubmission!!, onCancel = onCancel, onMissionSubmitted = onMissionSubmitted)
            } else {
                 Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        }
    }
}

@Composable
private fun PlusSelectionStep(missions: List<Mission>, onSelectMission: (String) -> Unit, onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 48.dp, bottom = 96.dp, start = 24.dp, end = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Select Mission", color = MaterialTheme.colorScheme.onBackground, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp).graphicsLayer { rotationZ = 45f })
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        
        if (missions.isEmpty()) {
             Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                 CircularProgressIndicator()
             }
        } else {
            missions.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    row.forEach { m ->
                        GlassCard(
                            modifier = Modifier.weight(1f).height(220.dp),
                            onClick = { onSelectMission(m.id) },
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(m.icon.toImageVector(), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(m.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(m.description ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("+${m.gcReward} COINS", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PlusBriefStep(mission: Mission, onNext: () -> Unit, onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp, 48.dp, 24.dp, 40.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp).graphicsLayer { rotationZ = 45f })
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("MISSION BRIEFING", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Mission: ${mission.title}",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 44.sp 
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        mission.instructionSteps.forEachIndexed { index, text ->
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 24.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${index + 1}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.size(24.dp))
                Text(text, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
            shape = RoundedCornerShape(24.dp),
        ) {
            Text("Start Mission", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.size(12.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun MissionReminderCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = missionIconPlaceholder().toImageVector(),
                    contentDescription = "Mission",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column {
                Text(
                    text = "Mission",
                    color = AppColors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Plant a Native Tree",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Reward",
                    color = AppColors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "+250 GreenCoins",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// Simple placeholder icon mapping for the reminder card
private fun missionIconPlaceholder(): com.greencoins.app.data.MissionIcon {
    return com.greencoins.app.data.MissionIcon.TreePine
}

/**
 * Validates EXIF metadata to ensure the image is likely from a real camera.
 * Returns true if valid, false if suspicious (missing required metadata).
 */
private fun hasValidExifMetadata(context: Context, uri: Uri): Boolean {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val exif = ExifInterface(inputStream)
            val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME)
            val cameraMake = exif.getAttribute(ExifInterface.TAG_MAKE)
            val cameraModel = exif.getAttribute(ExifInterface.TAG_MODEL)

            val hasDateTime = !dateTime.isNullOrBlank()
            val hasCameraInfo = !cameraMake.isNullOrBlank() || !cameraModel.isNullOrBlank()

            hasDateTime && hasCameraInfo
        } ?: false
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@Composable
private fun PlusUploadStep(mission: Mission, onNext: (Submission) -> Unit, onCancel: () -> Unit, onMissionSubmitted: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var beforeImageUri by remember { mutableStateOf<Uri?>(null) }
    var afterImageUri by remember { mutableStateOf<Uri?>(null) }
    var description by remember { mutableStateOf("") }

    var isConfirmed by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var pendingImageSlot by remember { mutableStateOf<Boolean?>(null) } // true = before, false = after

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val submitMissionData = { lat: Double?, lon: Double?, locName: String? ->
        scope.launch {
            try {
                val userId = AuthRepository.currentUser?.id
                if (userId != null && beforeImageUri != null && afterImageUri != null) {
                    val beforeInputStream = context.contentResolver.openInputStream(beforeImageUri!!)
                    val afterInputStream = context.contentResolver.openInputStream(afterImageUri!!)
                    if (beforeInputStream != null && afterInputStream != null) {
                        val beforeBytes = beforeInputStream.use { it.readBytes() }
                        val afterBytes = afterInputStream.use { it.readBytes() }
                        try {
                            val beforeUrl = MissionRepository.uploadMissionProof(userId, beforeBytes, "before")
                            val afterUrl = MissionRepository.uploadMissionProof(userId, afterBytes, "after")
                            val insertedSubmission = MissionRepository.submitMission(userId, mission.id, beforeUrl, afterUrl, description, lat, lon, locName)
                            
                            if (insertedSubmission != null) {
                                // 1. AI pipeline
                                SubmissionVerificationRepository.verifySubmission(insertedSubmission.id, beforeUrl, afterUrl, mission.title)
                                // 2. Refetch real state
                                val finalState = SubmissionVerificationRepository.getSubmissionById(insertedSubmission.id) ?: insertedSubmission
                                
                                if (finalState.status == "verified") {
                                    onMissionSubmitted()
                                }
                                onNext(finalState)
                            } else {
                                android.widget.Toast.makeText(context, "Upload failed to insert", android.widget.Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            android.widget.Toast.makeText(context, "Upload failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    } else {
                        android.widget.Toast.makeText(context, "Could not read image(s)", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (userId == null) {
                        android.widget.Toast.makeText(context, "User not logged in", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Please select both Before and After images", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } catch(e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            } finally {
                isSubmitting = false
            }
        }
    }

    fun fetchLocationAndSubmit() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    var locationName: String? = null
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            locationName = listOfNotNull(addr.subLocality, addr.locality, addr.adminArea).filter { it.isNotBlank() }.joinToString(", ")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    submitMissionData(location.latitude, location.longitude, locationName)
                } else {
                    isSubmitting = false
                    android.widget.Toast.makeText(context, "Could not acquire GPS location. Please ensure location services are enabled.", android.widget.Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener {
                isSubmitting = false
                android.widget.Toast.makeText(context, "Failed to get location: ${it.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        } catch (e: SecurityException) {
            isSubmitting = false
            android.widget.Toast.makeText(context, "Location permission is required", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchLocationAndSubmit()
        } else {
            isSubmitting = false
            android.widget.Toast.makeText(context, "Location permission is strictly required to verify missions.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    val onSubmitClicked = {
        if (!isSubmitting) {
            isSubmitting = true
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasFine || hasCoarse) {
                fetchLocationAndSubmit()
            } else {
                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }
    }

    val mediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        when (pendingImageSlot) {
            true -> if (uri != null) {
                if (hasValidExifMetadata(context, uri)) {
                    beforeImageUri = uri
                } else {
                    android.widget.Toast.makeText(context, "Image metadata missing. Please upload a real photo taken from your camera.", android.widget.Toast.LENGTH_LONG).show()
                }
            }
            false -> if (uri != null) {
                if (hasValidExifMetadata(context, uri)) {
                    afterImageUri = uri
                } else {
                    android.widget.Toast.makeText(context, "Image metadata missing. Please upload a real photo taken from your camera.", android.widget.Toast.LENGTH_LONG).show()
                }
            }
            null -> { }
        }
        pendingImageSlot = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(48.dp, 48.dp, 24.dp, 40.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onCancel, enabled = !isSubmitting) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.textSecondary)
            }
            Text("Proof of Impact", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onCancel, enabled = !isSubmitting) {
                Icon(Icons.Default.Add, contentDescription = null, tint = AppColors.textSecondary, modifier = Modifier.size(24.dp).graphicsLayer { rotationZ = 45f })
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        MissionReminderCard()

        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // "Before" box - Clickable for upload
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                    .clickable(enabled = !isSubmitting) {
                        pendingImageSlot = true
                        mediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (beforeImageUri != null) {
                    AsyncImage(
                        model = beforeImageUri,
                        contentDescription = "Before Image",
                        modifier = Modifier.fillMaxSize().graphicsLayer { clip = true; shape = RoundedCornerShape(32.dp) },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Before", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // "After" box - Clickable for upload
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                    .clickable(enabled = !isSubmitting) {
                        pendingImageSlot = false
                        mediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (afterImageUri != null) {
                    AsyncImage(
                        model = afterImageUri,
                        contentDescription = "After Image",
                        modifier = Modifier.fillMaxSize().graphicsLayer { clip = true; shape = RoundedCornerShape(32.dp) },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("After Image", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))



        Spacer(modifier = Modifier.height(24.dp))
        TextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp),
            placeholder = { Text("Describe what you did (optional)", color = AppColors.gray555) },
            colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedIndicatorColor = AppColors.gray333,
            ),
            shape = RoundedCornerShape(24.dp),
            enabled = !isSubmitting
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Confirmation checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.Checkbox(
                checked = isConfirmed,
                onCheckedChange = { isConfirmed = it },
                enabled = !isSubmitting,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "I confirm that this action was completed by me.",
                color = AppColors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onSubmitClicked() },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
            shape = RoundedCornerShape(24.dp),
            enabled = !isSubmitting && beforeImageUri != null && afterImageUri != null
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                 Text("Submit for Verification", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PlusSuccessStep(mission: Mission, submission: Submission, onCancel: () -> Unit, onMissionSubmitted: () -> Unit = {}) {
    // Poll for coin/streak updates while verification is in progress (est. 15 mins)
    LaunchedEffect(Unit) {
        repeat(8) {
            delay(15_000)
            onMissionSubmitted()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.textSecondary)
            }
        }
        Spacer(modifier = Modifier.weight(0.3f))
        val isVerified = submission.status == "verified"
        val isRejected = submission.status == "rejected"
        val isPending = submission.status == "pending"

        val iconData = when {
            isVerified -> Pair(Icons.Default.CheckCircle, MaterialTheme.colorScheme.primary)
            isRejected -> Pair(Icons.Default.Clear, androidx.compose.ui.graphics.Color.Red)
            else -> Pair(Icons.Default.Refresh, androidx.compose.ui.graphics.Color(0xFFECA30B))
        }

        Box(
            modifier = Modifier
                .size(128.dp)
                .background(iconData.second.copy(alpha=0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = iconData.first, contentDescription = null, modifier = Modifier.size(64.dp), tint = iconData.second)
        }
        Spacer(modifier = Modifier.height(40.dp))
        
        val titleText = when {
            isVerified -> "Mission verified"
            isRejected -> "Mission rejected"
            else -> "Needs manual review"
        }
        Text(titleText, color = MaterialTheme.colorScheme.onBackground, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val descText = when {
            isVerified -> "Your proof was authentic. You have earned ${mission.gcReward} GreenCoins!"
            isRejected -> submission.rejectedReason ?: "Your image proof did not match the mission requirements."
            else -> "AI flagged this for review: ${submission.rejectedReason ?: "Analysis uncertain"}.\nModerators will verify it shortly."
        }
        Text(
            descText,
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(48.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(mission.icon.toImageVector(), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Column {
                        Text(mission.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Just now", color = AppColors.textSecondary, fontSize = 10.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .background(iconData.second.copy(alpha=0.15f), RoundedCornerShape(9999.dp))
                        .border(1.dp, iconData.second, RoundedCornerShape(9999.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(submission.status.uppercase(), color = iconData.second, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
            shape = RoundedCornerShape(24.dp),
        ) {
            Text("Back to Dashboard", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.weight(0.5f))
    }
}
