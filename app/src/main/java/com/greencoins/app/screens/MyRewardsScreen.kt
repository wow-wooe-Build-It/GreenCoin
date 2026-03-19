package com.greencoins.app.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.greencoins.app.data.MyRewardsRepository
import com.greencoins.app.data.UserCoupon
import com.greencoins.app.theme.themeCardBgColor
import com.greencoins.app.theme.themeOnSurfaceTextColor
import com.greencoins.app.theme.themeOnSurfaceVariantTextColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MyRewardsScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var coupons by remember { mutableStateOf<List<UserCoupon>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Ready to Scratch", "My Coupons", "Used/Expired")

    var activeScratchCoupon by remember { mutableStateOf<UserCoupon?>(null) }

    fun loadData() {
        scope.launch {
            isLoading = true
            coupons = MyRewardsRepository.getMyCoupons()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    val filteredCoupons = coupons.filter {
        when (selectedTab) {
            0 -> it.status == "locked"
            1 -> it.status == "scratched"
            else -> it.status == "redeemed" || it.status == "expired"
        }
    }

    Scaffold(
        topBar = {
            Column {
                Spacer(modifier = Modifier.height(36.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        "My Rewards",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeOnSurfaceTextColor(),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    title, 
                                    color = if (selectedTab == index) MaterialTheme.colorScheme.primary else themeOnSurfaceVariantTextColor(),
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                ) 
                            }
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (filteredCoupons.isEmpty()) {
                Text(
                    "No rewards found in this section.",
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    color = themeOnSurfaceVariantTextColor()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredCoupons) { coupon ->
                        CouponCard(
                            coupon = coupon,
                            onClick = {
                                if (coupon.status == "locked") {
                                    activeScratchCoupon = coupon
                                }
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = activeScratchCoupon != null,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f),
                modifier = Modifier.fillMaxSize()
            ) {
                if (activeScratchCoupon != null) {
                    ScratchPopup(
                        coupon = activeScratchCoupon!!,
                        onDismiss = { activeScratchCoupon = null },
                        onScratched = { updatedCouponId ->
                            // Update local list
                            coupons = coupons.map { 
                                if (it.id == updatedCouponId) it.copy(status = "scratched") 
                                else it 
                            }
                            activeScratchCoupon = null
                            selectedTab = 1
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CouponCard(coupon: UserCoupon, onClick: () -> Unit) {
    val camp = coupon.campaign
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = themeCardBgColor())
    ) {
        Row(Modifier.fillMaxSize()) {
            if (camp?.imageUrl != null) {
                AsyncImage(
                    model = camp.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(camp?.brandName?.take(2)?.uppercase() ?: "RE", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Black, fontSize = 24.sp)
                }
            }
            Column(
                modifier = Modifier.weight(2f).padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    camp?.brandName ?: "Unknown Brand",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    camp?.title ?: "Mystery Reward",
                    color = themeOnSurfaceTextColor(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (coupon.status == "locked") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Tap to Scratch", color = themeOnSurfaceVariantTextColor(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else if (coupon.status == "scratched" && coupon.inventory != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(coupon.inventory.couponCode, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun ScratchPopup(
    coupon: UserCoupon,
    onDismiss: () -> Unit,
    onScratched: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var isRevealed by remember { mutableStateOf(false) }
    var dissolveAnimation by remember { mutableStateOf(false) }
    
    // Scratch paths
    val paths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var scrubCount by remember { mutableStateOf(0) }

    LaunchedEffect(scrubCount) {
        if (!isRevealed && scrubCount > 40) { // Arbitrary threshold
            isRevealed = true
            dissolveAnimation = true
            // Save to DB
            MyRewardsRepository.scratchCoupon(coupon.id)
            delay(1500) // Let them see the text
            onScratched(coupon.id)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) {
                if (isRevealed) onScratched(coupon.id) else onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(themeCardBgColor()),
            contentAlignment = Alignment.Center
        ) {
            // Actual Coupon Content Underneath
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text("Congratulations!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(coupon.campaign?.title ?: "Mystery Reward", color = themeOnSurfaceTextColor(), fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Text(coupon.inventory?.couponCode ?: "ERROR_MISSING_CODE", color = themeOnSurfaceTextColor(), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Code revealed!", color = themeOnSurfaceVariantTextColor(), fontSize = 14.sp)
            }

            // Scratch Layer overlay
            AnimatedVisibility(
                visible = !dissolveAnimation,
                exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(500)) + scaleOut(targetScale = 1.1f, animationSpec = androidx.compose.animation.core.tween(500)),
                modifier = Modifier.fillMaxSize()
            ) {
                val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val newPath = Path().apply { moveTo(offset.x, offset.y) }
                                    currentPath = newPath
                                    paths.add(newPath)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    currentPath?.let {
                                        it.lineTo(change.position.x, change.position.y)
                                        scrubCount++
                                    }
                                },
                                onDragEnd = { currentPath = null },
                                onDragCancel = { currentPath = null }
                            )
                        }
                ) {
                    // We need to use BlendMode.Clear to erase the foil layer.
                    // This requires a hardware layer (CompositingGroup) usually.
                    with(drawContext.canvas.nativeCanvas) {
                        val checkPoint = saveLayer(null, null)
                        
                        // Draw Foil base
                        drawRect(
                            color = surfaceColor,
                            size = size
                        )
                        
                        // Adding foil text context
                        drawContext.canvas.nativeCanvas.drawText("SCRATCH HERE", size.width / 2f, size.height / 2f, android.graphics.Paint().apply {
                            color = android.graphics.Color.DKGRAY
                            textSize = 60f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        })

                        // Clear paths
                        val eraser = Stroke(width = 120f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        paths.forEach { path ->
                            drawPath(
                                path = path,
                                color = Color.Transparent,
                                style = eraser,
                                blendMode = BlendMode.Clear
                            )
                        }
                        restoreToCount(checkPoint)
                    }
                }
            }
        }
    }
}
