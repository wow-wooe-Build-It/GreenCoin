package com.greencoins.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.CircleShape
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.greencoins.app.data.AuthRepository
import com.greencoins.app.theme.AppColors
import com.greencoins.app.theme.GreenCoinsTheme
import kotlinx.coroutines.launch
import android.util.Patterns

/**
 * Preserved exactly: background glow, logo, title, subtitle, Google + Email buttons, terms text.
 */
@Composable
fun AuthScreen(onLogin: () -> Unit) {
    var isEmailMode by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.bg),
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "glow")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(7000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(7000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
                .size(560.dp)
                .scale(scale)
                .alpha(alpha)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(AppColors.accent.copy(alpha = 0.5f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        ) {}
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(AppColors.accent, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = AppColors.black,
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "GreenCoins",
                color = AppColors.white,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Rewarding your environmental impact",
                color = AppColors.textSecondary,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(48.dp))

            if (isLoading) {
                CircularProgressIndicator(color = AppColors.accent)
            } else if (isEmailMode) {
                Text(
                    text = if (isSignUp) "Create Account" else "Welcome Back",
                    color = AppColors.white,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isSignUp) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name", color = AppColors.textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppColors.white,
                            unfocusedTextColor = AppColors.white,
                            focusedBorderColor = AppColors.accent,
                            unfocusedBorderColor = AppColors.border,
                            cursorColor = AppColors.accent,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number", color = AppColors.textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppColors.white,
                            unfocusedTextColor = AppColors.white,
                            focusedBorderColor = AppColors.accent,
                            unfocusedBorderColor = AppColors.border,
                            cursorColor = AppColors.accent,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = AppColors.textSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppColors.white,
                        unfocusedTextColor = AppColors.white,
                        focusedBorderColor = AppColors.accent,
                        unfocusedBorderColor = AppColors.border,
                        cursorColor = AppColors.accent,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = AppColors.textSecondary) },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppColors.white,
                        unfocusedTextColor = AppColors.white,
                        focusedBorderColor = AppColors.accent,
                        unfocusedBorderColor = AppColors.border,
                        cursorColor = AppColors.accent,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                error?.let {
                    Text(it, color = Color.Red, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                isLoading = true
                                error = null
                                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    error = "Enter a valid email address"
                                    isLoading = false
                                    return@launch
                                }
                                if (isSignUp) {
                                    AuthRepository.signUpWithEmail(email, password, name, phone)
                                    if (AuthRepository.isUserLoggedIn()) {
                                        onLogin()
                                    } else {
                                        error = "Please check your email for confirmation link."
                                        isLoading = false
                                    }
                                } else {
                                    AuthRepository.signInWithEmail(email, password)
                                    onLogin()
                                }
                            } catch (e: Exception) {
                                error = e.message ?: "Authentication failed"
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent, contentColor = AppColors.black),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text(if (isSignUp) "Sign Up" else "Sign In", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Create one",
                    color = AppColors.white,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        isSignUp = !isSignUp
                        error = null
                    }
                )
                if (!isSignUp) { // Only show back button if not switching modes or handle logic better?
                     // Actually let's keep "Back" to go to landing page
                     Spacer(modifier = Modifier.height(16.dp))
                     Text(
                        "Back",
                        color = AppColors.textSecondary,
                        modifier = Modifier.clickable { isEmailMode = false }
                     )
                }
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                isLoading = true
                                error = null
                                // Note: This will open the browser. When the user returns via deep link,
                                // the session effectively resumes and GreenCoinsApp will observe the state change.
                                AuthRepository.signInWithGoogle()
                                // We purposefully do NOT call onLogin() here because signInWithGoogle opens
                                // the browser async. The app will automatically switch to HomeScreen once the 
                                // deep link returns and AuthRepository.isLoggedIn emits true in GreenCoinsApp!
                            } catch (e: Exception) {
                                error = e.message ?: "Google Sign-In failed"
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.white, contentColor = AppColors.black),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data("https://www.google.com/favicon.ico").build(),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text("Continue with Google", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { isEmailMode = true; isSignUp = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.accent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.accent),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text("Login with Email", fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(
            text = "By continuing you agree to our Terms of Service",
            color = AppColors.gray555,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
        )
    }
}

@Preview(showBackground = true, name = "Auth Screen")
@Composable
private fun AuthScreenPreview() {
    GreenCoinsTheme {
        AuthScreen(onLogin = {})
    }
}
