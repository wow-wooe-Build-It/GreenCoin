package com.greencoins.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greencoins.app.data.UserProfile
import com.greencoins.app.data.UserRepository
import com.greencoins.app.theme.AppColors
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun PersonalInformationScreen(
    userId: String,
    userProfile: UserProfile?,
    emailFromAuth: String?,
    phoneFromAuth: String?,
    onBack: () -> Unit,
    onProfileUpdated: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(userProfile?.fullName ?: "") }
    var phone by remember { mutableStateOf(userProfile?.phone ?: phoneFromAuth ?: "") }
    var city by remember { mutableStateOf(userProfile?.city ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(userProfile) {
        name = userProfile?.fullName ?: ""
        phone = userProfile?.phone ?: phoneFromAuth ?: ""
        city = userProfile?.city ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.bg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("Personal Information", color = AppColors.white, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                enabled = isEditing,
                readOnly = !isEditing,
                label = { Text("Name", color = AppColors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.white,
                    unfocusedTextColor = AppColors.white,
                    disabledTextColor = AppColors.textSecondary,
                    focusedBorderColor = AppColors.accent,
                    unfocusedBorderColor = AppColors.border,
                    cursorColor = AppColors.accent,
                ),
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = emailFromAuth ?: "",
                onValueChange = { },
                enabled = false,
                readOnly = true,
                label = { Text("Email", color = AppColors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.textSecondary,
                    unfocusedTextColor = AppColors.textSecondary,
                    disabledTextColor = AppColors.textSecondary,
                    focusedBorderColor = AppColors.border,
                    unfocusedBorderColor = AppColors.border,
                ),
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                enabled = isEditing,
                readOnly = !isEditing,
                label = { Text("Phone (optional)", color = AppColors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.white,
                    unfocusedTextColor = AppColors.white,
                    disabledTextColor = AppColors.textSecondary,
                    focusedBorderColor = AppColors.accent,
                    unfocusedBorderColor = AppColors.border,
                    cursorColor = AppColors.accent,
                ),
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                enabled = isEditing,
                readOnly = !isEditing,
                label = { Text("City / Location (optional)", color = AppColors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.white,
                    unfocusedTextColor = AppColors.white,
                    disabledTextColor = AppColors.textSecondary,
                    focusedBorderColor = AppColors.accent,
                    unfocusedBorderColor = AppColors.border,
                    cursorColor = AppColors.accent,
                ),
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(modifier = Modifier.height(32.dp))
            if (isEditing) {
                Button(
                    onClick = {
                        if (isSaving) return@Button
                        scope.launch {
                            isSaving = true
                            UserRepository.updateProfile(userId, fullName = name, phone = phone, city = city)
                            onProfileUpdated()
                            isEditing = false
                            isSaving = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent, contentColor = AppColors.black),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(if (isSaving) "Saving..." else "Save", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent, contentColor = AppColors.black),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Edit", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
