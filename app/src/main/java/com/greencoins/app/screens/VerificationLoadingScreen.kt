package com.greencoins.app.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.gms.location.LocationServices
import com.greencoins.app.data.AuthRepository
import com.greencoins.app.data.Mission
import com.greencoins.app.data.MissionRepository
import com.greencoins.app.data.Submission
import com.greencoins.app.data.SubmissionVerificationRepository
import com.greencoins.app.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.math.sin

private const val TAG = "VerificationLoading"

private const val POLL_INTERVAL_MS = 10_000L
private const val LONG_WAIT_THRESHOLD_MS = 150_000L
private const val OSCILLATION_PERIOD_MS = 4_000L

private enum class MissionPipelinePhase {
    STARTING,
    GETTING_LOCATION,
    UPLOADING,
    CREATING_SUBMISSION,
    TRIGGERING_VERIFICATION,
    POLLING,
    DONE,
    ERROR,
}

private val stageLabels = listOf(
    "Uploading proof…",
    "Checking image validity…",
    "Checking AI authenticity…",
    "Analyzing before/after impact…",
    "Final verification in progress…",
)

private val rotatingSteps = listOf(
    "Checking your image validity",
    "Checking AI score",
    "Analyzing your images",
    "Finalizing verification",
)

private val tips = listOf(
    "Tune in daily to complete challenges and earn rewards.",
    "Clear before/after photos speed up verification.",
    "Complete challenges regularly to build your streak.",
    "Verified impact helps you earn more GreenCoins.",
)

private fun fakeProgressForElapsed(elapsedMs: Long): Float {
    return when {
        elapsedMs < 8_000L -> 20f * (elapsedMs / 8_000f).coerceIn(0f, 1f)
        elapsedMs < 20_000L -> 20f + 20f * ((elapsedMs - 8_000L) / 12_000f).coerceIn(0f, 1f)
        elapsedMs < 45_000L -> 40f + 20f * ((elapsedMs - 20_000L) / 25_000f).coerceIn(0f, 1f)
        elapsedMs < 75_000L -> 60f + 20f * ((elapsedMs - 45_000L) / 30_000f).coerceIn(0f, 1f)
        elapsedMs < 85_000L -> 80f + 10f * ((elapsedMs - 75_000L) / 10_000f).coerceIn(0f, 1f)
        else -> {
            val t = (elapsedMs - 85_000L) % OSCILLATION_PERIOD_MS
            val wave = (sin(t * 2.0 * Math.PI / OSCILLATION_PERIOD_MS) + 1.0) / 2.0
            (90f + wave * 5f).toFloat()
        }
    }.coerceIn(0f, 94.9f)
}

private fun bandIndexForProgress(p: Float): Int = when {
    p < 20f -> 0
    p < 40f -> 1
    p < 60f -> 2
    p < 80f -> 3
    else -> 4
}

private fun phaseToProgress(phase: MissionPipelinePhase): Float = when (phase) {
    MissionPipelinePhase.STARTING -> 2f
    MissionPipelinePhase.GETTING_LOCATION -> 8f
    MissionPipelinePhase.UPLOADING -> 18f
    MissionPipelinePhase.CREATING_SUBMISSION -> 28f
    MissionPipelinePhase.TRIGGERING_VERIFICATION -> 35f
    MissionPipelinePhase.POLLING -> 40f
    MissionPipelinePhase.DONE -> 100f
    MissionPipelinePhase.ERROR -> 0f
}

/** Pending fake progress caps below 100%; oscillation lives in ~90–95. */
private const val PENDING_PROGRESS_CAP = 94.9f
private const val OSCILLATION_LOW = 89f

/**
 * Keeps progress monotonic except in the pending oscillation band (90–95), where the fake
 * curve may move within the band.
 */
private fun nextMonotonicProgress(current: Float, rawDesired: Float): Float {
    val desired = rawDesired.coerceIn(0f, PENDING_PROGRESS_CAP)
    val inOscillationBand = desired >= OSCILLATION_LOW && current >= OSCILLATION_LOW
    return if (inOscillationBand) {
        desired.coerceIn(OSCILLATION_LOW, PENDING_PROGRESS_CAP)
    } else {
        maxOf(current, desired).coerceIn(0f, PENDING_PROGRESS_CAP)
    }
}

private const val MSG_NO_INTERNET =
    "We couldn't connect to the internet. Please check your connection and try again."
private const val MSG_SERVER =
    "Our servers are taking longer than usual. Please try again in a moment."
private const val MSG_UPLOAD_GENERIC =
    "We couldn't upload your proof right now. Please retry."
private const val MSG_LOCATION =
    "Location access is required to verify this mission. Please enable location and retry."
private const val MSG_FALLBACK =
    "Something went wrong while verifying your mission. Please try again."

/**
 * Maps failures to user-safe copy; logs the raw error for debugging.
 */
private fun toUserFriendlyError(raw: Throwable?): String {
    if (raw != null) Log.e(TAG, "verification pipeline error", raw)
    return toUserFriendlyError(raw?.message, raw)
}

private fun toUserFriendlyError(message: String?, cause: Throwable? = null): String {
    val combined = buildString {
        message?.let { append(it) }
        if (cause != null) {
            if (isNotEmpty()) append(' ')
            append(cause.javaClass.simpleName)
            cause.message?.let { append(' ').append(it) }
            var c = cause.cause
            var depth = 0
            while (c != null && depth++ < 4) {
                append(' ').append(c.message ?: "")
                c = c.cause
            }
        }
    }
    val m = combined.lowercase()

    if (cause is SecurityException) return MSG_LOCATION
    if (cause is UnknownHostException || cause is SocketTimeoutException) return MSG_NO_INTERNET

    if (m.contains("permission") && (m.contains("location") || m.contains("fine_location") || m.contains("coarse"))) {
        return MSG_LOCATION
    }
    if (m.contains("location permission") || m.contains("location access") || m.contains("gps") && m.contains("denied")) {
        return MSG_LOCATION
    }

    if (looksLikeServerError(m)) return MSG_SERVER

    if (looksLikeNetworkError(m)) return MSG_NO_INTERNET

    if (looksLikeUploadError(m)) return MSG_UPLOAD_GENERIC

    return MSG_FALLBACK
}

private fun looksLikeServerError(m: String): Boolean =
    listOf("500", "502", "503", "504", "521", "522", "523", "524", "gateway", "bad gateway", "service unavailable", "server error", "internal server").any { m.contains(it) }

private fun looksLikeNetworkError(m: String): Boolean =
    listOf(
        "unable to resolve",
        "unknown host",
        "no address associated",
        "network is unreachable",
        "connection refused",
        "failed to connect",
        "connection reset",
        "connection timed out",
        "timeout",
        "timed out",
        "etimedout",
        "econnrefused",
        "enotfound",
        "enetunreach",
        "ehostunreach",
        "no route to host",
        "ssl handshake",
        "cleartext not permitted",
        "network error",
        "no connectivity",
    ).any { m.contains(it) }

private fun looksLikeUploadError(m: String): Boolean =
    listOf("upload", "413", "request entity", "stream", "multipart", "entity too large").any { m.contains(it) }

private fun phaseLabel(phase: MissionPipelinePhase): String = when (phase) {
    MissionPipelinePhase.STARTING -> "Preparing…"
    MissionPipelinePhase.GETTING_LOCATION -> "Getting location…"
    MissionPipelinePhase.UPLOADING -> "Uploading proof…"
    MissionPipelinePhase.CREATING_SUBMISSION -> "Creating submission…"
    MissionPipelinePhase.TRIGGERING_VERIFICATION -> "Triggering verification…"
    MissionPipelinePhase.POLLING -> "Verifying your impact…"
    MissionPipelinePhase.DONE -> "Done"
    MissionPipelinePhase.ERROR -> "Something went wrong"
}

/**
 * Runs upload → submit → trigger in the background, then polls until verified/rejected.
 */
@Composable
fun VerificationLoadingScreen(
    mission: Mission,
    pending: MissionSubmissionPending,
    onSubmissionCreated: (Submission) -> Unit,
    onFinished: (Submission) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("animations/verification_loading.json"))
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var submission by remember { mutableStateOf<Submission?>(null) }
    var pipelinePhase by remember { mutableStateOf(MissionPipelinePhase.STARTING) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableIntStateOf(0) }
    val pipelineInFlight = remember { AtomicBoolean(false) }

    var pollFailed by remember { mutableStateOf(false) }
    var showLongWaitMessage by remember { mutableStateOf(false) }
    var pollingStartTime by remember { mutableLongStateOf(0L) }

    val progressAnim = remember { Animatable(0f) }
    var terminalReached by remember { mutableStateOf(false) }
    var rotatingIdx by remember { mutableIntStateOf(0) }
    var tipIdx by remember { mutableIntStateOf(0) }

    val bandLabel = stageLabels[bandIndexForProgress(if (terminalReached) 100f else progressAnim.value)]

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun launchPipelineBody() {
        if (!pipelineInFlight.compareAndSet(false, true)) return
        scope.launch {
            try {
                errorMessage = null
                pipelinePhase = MissionPipelinePhase.STARTING
                delay(32)
                val userId = AuthRepository.currentUser?.id
                if (userId == null) {
                    pipelinePhase = MissionPipelinePhase.ERROR
                    Log.e(TAG, "verification pipeline error: not logged in")
                    errorMessage = MSG_FALLBACK
                    pipelineInFlight.set(false)
                    return@launch
                }
                try {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            scope.launch {
                                try {
                                    if (location == null) {
                                        pipelinePhase = MissionPipelinePhase.ERROR
                                        Log.e(TAG, "verification pipeline error: lastLocation null")
                                        errorMessage = MSG_LOCATION
                                        return@launch
                                    }
                                    var locationName: String? = null
                                    try {
                                        val geocoder = Geocoder(context, Locale.getDefault())
                                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                        if (!addresses.isNullOrEmpty()) {
                                            val addr = addresses[0]
                                            locationName = listOfNotNull(addr.subLocality, addr.locality, addr.adminArea)
                                                .filter { it.isNotBlank() }.joinToString(", ")
                                        }
                                    } catch (_: Exception) { }
                                    pipelinePhase = MissionPipelinePhase.UPLOADING
                                    val beforeStream = context.contentResolver.openInputStream(pending.beforeUri)
                                    val afterStream = context.contentResolver.openInputStream(pending.afterUri)
                                    if (beforeStream == null || afterStream == null) {
                                        pipelinePhase = MissionPipelinePhase.ERROR
                                        Log.e(TAG, "verification pipeline error: could not read image stream(s)")
                                        errorMessage = MSG_UPLOAD_GENERIC
                                        return@launch
                                    }
                                    val beforeBytes = beforeStream.use { it.readBytes() }
                                    val afterBytes = afterStream.use { it.readBytes() }
                                    val beforeImageUrl = MissionRepository.uploadMissionProof(userId, beforeBytes, "before")
                                    val afterImageUrl = MissionRepository.uploadMissionProof(userId, afterBytes, "after")
                                    pipelinePhase = MissionPipelinePhase.CREATING_SUBMISSION
                                    val submissionRow = MissionRepository.submitMission(
                                        userId,
                                        pending.mission.id,
                                        beforeImageUrl,
                                        afterImageUrl,
                                        pending.description,
                                        location.latitude,
                                        location.longitude,
                                        locationName,
                                    )
                                    if (submissionRow == null) {
                                        pipelinePhase = MissionPipelinePhase.ERROR
                                        Log.e(TAG, "verification pipeline error: submitMission returned null")
                                        errorMessage = MSG_SERVER
                                        return@launch
                                    }
                                    pipelinePhase = MissionPipelinePhase.TRIGGERING_VERIFICATION
                                    SubmissionVerificationRepository.triggerVerification(
                                        submissionId = submissionRow.id,
                                        beforeUrl = beforeImageUrl,
                                        afterUrl = afterImageUrl,
                                        mission = pending.mission.title,
                                    )
                                    pollingStartTime = System.currentTimeMillis()
                                    submission = submissionRow
                                    pipelinePhase = MissionPipelinePhase.POLLING
                                    onSubmissionCreated(submissionRow)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    pipelinePhase = MissionPipelinePhase.ERROR
                                    errorMessage = toUserFriendlyError(e)
                                } finally {
                                    pipelineInFlight.set(false)
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            scope.launch {
                                pipelinePhase = MissionPipelinePhase.ERROR
                                errorMessage = toUserFriendlyError(e)
                                pipelineInFlight.set(false)
                            }
                        }
                } catch (e: SecurityException) {
                    Log.e(TAG, "verification pipeline error", e)
                    pipelinePhase = MissionPipelinePhase.ERROR
                    errorMessage = MSG_LOCATION
                    pipelineInFlight.set(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                pipelinePhase = MissionPipelinePhase.ERROR
                errorMessage = toUserFriendlyError(e)
                pipelineInFlight.set(false)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            launchPipelineBody()
        } else {
            pipelinePhase = MissionPipelinePhase.ERROR
            Log.e(TAG, "verification pipeline error: location permission denied")
            errorMessage = MSG_LOCATION
        }
    }

    LaunchedEffect(retryKey) {
        if (submission != null) return@LaunchedEffect
        if (hasLocationPermission()) {
            launchPipelineBody()
        } else {
            pipelinePhase = MissionPipelinePhase.GETTING_LOCATION
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            )
        }
    }

    // Poll for terminal status after submission row exists
    LaunchedEffect(submission?.id) {
        val id = submission?.id ?: return@LaunchedEffect
        var first = true
        while (isActive && !terminalReached) {
            if (first) {
                delay(800)
                first = false
            } else {
                delay(POLL_INTERVAL_MS)
            }
            val refreshed = try {
                SubmissionVerificationRepository.getSubmissionById(id)
            } catch (_: Exception) {
                null
            }
            if (refreshed != null) {
                pollFailed = false
                submission = refreshed
                if (refreshed.status != "pending") {
                    terminalReached = true
                    progressAnim.animateTo(100f, animationSpec = tween(480))
                    onFinished(refreshed)
                    return@LaunchedEffect
                }
            } else {
                pollFailed = true
            }
        }
    }

    LaunchedEffect(submission?.id, submission?.status) {
        if (submission?.status != "pending") return@LaunchedEffect
        delay(LONG_WAIT_THRESHOLD_MS)
        if (!terminalReached) showLongWaitMessage = true
    }

    // Progress: pipeline phases → fake polling while pending (monotonic; oscillation only in ~90–95)
    LaunchedEffect(terminalReached, submission?.status, submission?.id, pipelinePhase, pollingStartTime) {
        while (isActive && !terminalReached) {
            val desired = when {
                submission == null && errorMessage == null -> phaseToProgress(pipelinePhase)
                submission?.status == "pending" -> {
                    val start = pollingStartTime
                    if (start > 0L) {
                        fakeProgressForElapsed(System.currentTimeMillis() - start)
                    } else {
                        phaseToProgress(MissionPipelinePhase.POLLING)
                    }
                }
                else -> progressAnim.value
            }
            val next = nextMonotonicProgress(progressAnim.value, desired)
            progressAnim.snapTo(next)
            delay(400)
        }
    }

    LaunchedEffect(terminalReached, errorMessage) {
        while (isActive && !terminalReached && errorMessage == null) {
            delay(2_500)
            if (!terminalReached) rotatingIdx = (rotatingIdx + 1) % rotatingSteps.size
        }
    }

    LaunchedEffect(terminalReached, errorMessage) {
        while (isActive && !terminalReached && errorMessage == null) {
            delay(3_000)
            if (!terminalReached) tipIdx = (tipIdx + 1) % tips.size
        }
    }

    val primaryLine = when {
        errorMessage != null -> errorMessage!!
        pollFailed -> "Still verifying…"
        submission == null -> phaseLabel(pipelinePhase)
        else -> bandLabel
    }
    val secondaryLine = rotatingSteps[rotatingIdx]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onCancel) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.textSecondary,
                    )
                }
            }

            Text(
                text = "Verifying your impact…",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Text(
                text = mission.title.trim(),
                modifier = Modifier.fillMaxWidth(),
                color = AppColors.textSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(0.12f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .size(280.dp)
                        .semantics { contentDescription = "Verification in progress animation" },
                )
            }

            AnimatedContent(
                targetState = primaryLine to secondaryLine,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(220)) },
                label = "status_text",
            ) { (stage, rotate) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stage,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = rotate,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            if (errorMessage != null && submission == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text("Back")
                    }
                    Button(
                        onClick = {
                            errorMessage = null
                            pipelinePhase = MissionPipelinePhase.STARTING
                            retryKey++
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Retry")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                LinearProgressIndicator(
                    progress = { progressAnim.value.coerceIn(0f, 100f) / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .semantics { contentDescription = "Verification progress ${progressAnim.value.toInt().coerceIn(0, 100)} percent" },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
                Text(
                    text = "${progressAnim.value.toInt().coerceIn(0, 100)}%",
                    modifier = Modifier.padding(top = 8.dp),
                    color = AppColors.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(12.dp))
                AnimatedContent(
                    targetState = tipIdx,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                    label = "tip",
                ) { idx ->
                    Text(
                        text = tips[idx % tips.size],
                        color = AppColors.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showLongWaitMessage && !terminalReached && submission?.status == "pending",
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Text(
                text = "Verification is taking longer than usual. You can continue; status will update shortly.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        RoundedCornerShape(16.dp),
                    )
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
