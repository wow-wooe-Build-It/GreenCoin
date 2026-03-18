package com.greencoins.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.greencoins.app.components.BottomNav
import com.greencoins.app.components.Header
import com.greencoins.app.data.Screen
import com.greencoins.app.screens.AuthScreen
import com.greencoins.app.screens.CategoryRewardsScreen
import com.greencoins.app.screens.ChallengesScreen
import com.greencoins.app.screens.HelpScreen
import com.greencoins.app.screens.HomeScreen
import com.greencoins.app.screens.PlusFlow
import com.greencoins.app.screens.PlusStep
import com.greencoins.app.screens.ChallengeDetailScreen
import com.greencoins.app.data.ChallengeDetailData
import com.greencoins.app.screens.ProfileScreen
import com.greencoins.app.screens.ShopScreen
import com.greencoins.app.screens.ShopViewModel
import com.greencoins.app.screens.UserViewModel
import com.greencoins.app.theme.AppColors
import androidx.compose.runtime.collectAsState
import com.greencoins.app.data.AuthRepository
import com.greencoins.app.data.UserChallengesRepository
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.launch

@Composable
fun GreenCoinsApp() {
    val isLoggedIn by AuthRepository.isLoggedIn.collectAsState()
    
    // We defer rendering until we know the login state (null means loading)
    if (isLoggedIn == null) {
         Box(modifier = Modifier.fillMaxSize().background(AppColors.bg), contentAlignment = Alignment.Center) {
             CircularProgressIndicator(color = AppColors.accent)
         }
         return
    }

    var screen by remember { 
        mutableStateOf<Screen>(
            if (isLoggedIn == true) Screen.Home else Screen.Auth
        ) 
    }

    // Force redirection when authentication state changes externally (like OAuth deeplink return)
    androidx.compose.runtime.LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == true && screen == Screen.Auth) {
            screen = Screen.Home
        } else if (isLoggedIn == false && screen != Screen.Auth) {
            screen = Screen.Auth
        }
    }

    val userViewModel: UserViewModel = viewModel()
    val headerCoins by userViewModel.headerCoins.collectAsState(initial = 0)
    var plusStep by remember { mutableStateOf<PlusStep>(PlusStep.Selection) }
    var selectedMissionId by remember { mutableStateOf<String?>(null) }
    var selectedShopCategory by remember { mutableStateOf<String?>(null) }
    var selectedChallenge by remember { mutableStateOf<com.greencoins.app.data.ChallengeDetailData?>(null) }
    var joinedChallengeIds by remember { mutableStateOf(setOf<String>()) }
    var showProfilePersonalInfo by remember { mutableStateOf(false) }
    var showProfileImpactStats by remember { mutableStateOf(false) }
    var showProfileRedemptionHistory by remember { mutableStateOf(false) }
    val shopViewModel: ShopViewModel = viewModel()
    val shopCategories by shopViewModel.categories.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    fun handleScreenChange(s: Screen) {
        if (s == Screen.Plus) {
            plusStep = PlusStep.Selection
            screen = Screen.Plus
        } else {
            if (s == Screen.Shop) selectedShopCategory = null
            if (s != Screen.Profile) {
                showProfilePersonalInfo = false
                showProfileImpactStats = false
                showProfileRedemptionHistory = false
            }
            screen = s
        }
    }

    fun handleMissionSelect(id: String) {
        selectedMissionId = id
        plusStep = PlusStep.Brief
        screen = Screen.Plus
    }

    // Load joined challenges for the currently logged-in user (header GC is from UserViewModel)
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == true) {
            val userId = AuthRepository.currentUser?.id
            if (userId != null) {
                joinedChallengeIds = UserChallengesRepository.getJoinedChallengeIds(userId)
            }
        } else {
            joinedChallengeIds = emptySet()
        }
    }

    if (screen == Screen.Auth) {
        AuthScreen(onLogin = { screen = Screen.Home })
        return
    }

    BackHandler(enabled = screen != Screen.Home) {
        when {
            screen == Screen.Help -> screen = Screen.Home
            screen == Screen.Plus -> screen = Screen.Home
            screen == Screen.Shop && selectedShopCategory != null -> selectedShopCategory = null
            screen == Screen.Shop -> screen = Screen.Home
            screen == Screen.Challenges -> screen = Screen.Home
            screen == Screen.ChallengeDetail -> screen = Screen.Challenges
            screen == Screen.Profile && (showProfilePersonalInfo || showProfileImpactStats || showProfileRedemptionHistory) -> {
                showProfilePersonalInfo = false
                showProfileImpactStats = false
                showProfileRedemptionHistory = false
            }
            screen == Screen.Profile -> screen = Screen.Home
            else -> { }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.bg),
    ) {
        if (screen != Screen.Plus && screen != Screen.Help) {
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                Header(coins = headerCoins, onHelp = { screen = Screen.Help })
                val onBack = when {
                    screen == Screen.Home -> null
                    screen == Screen.Shop && selectedShopCategory != null -> { { selectedShopCategory = null } }
                    screen == Screen.Shop -> { { screen = Screen.Home } }
                    screen == Screen.Challenges -> { { screen = Screen.Home } }
                    screen == Screen.ChallengeDetail -> { { screen = Screen.Challenges } }
                    screen == Screen.Profile && (showProfilePersonalInfo || showProfileImpactStats || showProfileRedemptionHistory) -> {
                        { showProfilePersonalInfo = false; showProfileImpactStats = false; showProfileRedemptionHistory = false }
                    }
                    screen == Screen.Profile -> { { screen = Screen.Home } }
                    else -> null
                }
                if (onBack != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.textSecondary)
                        }
                    }
                }
            }
        }
        val showBackRow = screen != Screen.Plus && screen != Screen.Help && screen != Screen.Home
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = when {
                        screen == Screen.Plus || screen == Screen.Help -> 0.dp
                        showBackRow -> 64.dp + 56.dp
                        else -> 64.dp
                    }
                ),
        ) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen",
            ) { current ->
                when (current) {
                    Screen.Auth -> { }
                    Screen.Home -> HomeScreen(
                        onMissionSelect = ::handleMissionSelect,
                        onChallengeClick = { data ->
                            selectedChallenge = data
                            screen = Screen.ChallengeDetail
                        },
                        refreshHeader = { userViewModel.refresh() },
                    )
                    Screen.Shop -> when {
                        selectedShopCategory == null -> ShopScreen(
                            categories = shopCategories,
                            onCategoryClick = { selectedShopCategory = it },
                        )
                        else -> {
                            val refreshScope = rememberCoroutineScope()
                            CategoryRewardsScreen(
                                categories = shopCategories,
                                selectedCategory = selectedShopCategory!!,
                                userBalance = headerCoins,
                                onCategoryChange = { selectedShopCategory = it },
                                onRedeem = { userViewModel.refresh() },
                                onBack = { selectedShopCategory = null },
                            )
                        }
                    }
                    Screen.Help -> HelpScreen(onClose = { screen = Screen.Home })
                    Screen.Plus -> PlusFlow(
                        step = plusStep,
                        missionId = selectedMissionId,
                        onSelectMission = ::handleMissionSelect,
                        onNext = {
                            plusStep = when (plusStep) {
                                is PlusStep.Brief -> PlusStep.Upload
                                is PlusStep.Upload -> PlusStep.Success
                                else -> plusStep
                            }
                        },
                        onCancel = { screen = Screen.Home },
                        onMissionSubmitted = { userViewModel.refresh() },
                    )
                    Screen.Challenges -> ChallengesScreen(
                        onChallengeClick = { data ->
                            selectedChallenge = data
                            screen = Screen.ChallengeDetail
                        }
                    )
                    Screen.ChallengeDetail -> if (selectedChallenge != null) {
                        val refreshScope = rememberCoroutineScope()
                        ChallengeDetailScreen(
                            data = selectedChallenge!!,
                            onBack = { screen = Screen.Challenges },
                            isJoined = selectedChallenge!!.id in joinedChallengeIds,
                            onJoin = {
                                val cid = selectedChallenge!!.id
                                joinedChallengeIds = joinedChallengeIds + cid
                                val userId = AuthRepository.currentUser?.id
                                if (userId != null) {
                                    refreshScope.launch {
                                        UserChallengesRepository.joinChallenge(userId, cid)
                                    }
                                }
                            },
                        )
                    } else {
                        screen = Screen.Challenges // Fallback
                    }
                    Screen.Profile -> ProfileScreen(
                        onLogout = {
                            scope.launch {
                                AuthRepository.logout()
                            }
                        },
                        showPersonalInfo = showProfilePersonalInfo,
                        onShowPersonalInfoChange = { showProfilePersonalInfo = it },
                        showImpactStatistics = showProfileImpactStats,
                        onShowImpactStatisticsChange = { showProfileImpactStats = it },
                        showRedemptionHistory = showProfileRedemptionHistory,
                        onShowRedemptionHistoryChange = { showProfileRedemptionHistory = it },
                    )
                    else -> { }
                }
            }
        }
        if (screen != Screen.Plus) {
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                BottomNav(active = screen, onChange = ::handleScreenChange)
            }
        }
    }
}
