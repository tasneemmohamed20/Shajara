package com.example.moodlegovapp.presentation.views.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.presentation.viewmodels.ForgotPasswordViewModel
import com.example.moodlegovapp.ui.theme.AppColors

@Composable fun ForgotPasswordScreen(vm: ForgotPasswordViewModel, onBackClick:()->Unit, onCheckEmail:()->Unit){
 val email by vm.email.collectAsState(); val loading by vm.isLoading.collectAsState(); val error by vm.error.collectAsState(); val show by vm.showCheckEmail.collectAsState()
 LaunchedEffect(show){ if(show){ vm.consumeNavigation(); onCheckEmail() } }
 Column(Modifier.fillMaxSize().background(AppColors.Background)){
  Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart=28.dp,bottomEnd=28.dp)).background(AppColors.NavyGradient).padding(24.dp)){
   IconButton(onClick=onBackClick){Icon(Icons.Default.ArrowBack,null,tint=Color.White)}; Spacer(Modifier.height(18.dp)); Text("ACCOUNT RECOVERY", color=AppColors.Gold); Text("Forgot Password?", color=Color.White, fontSize=28.sp, fontWeight=FontWeight.Bold); Text("Enter your registered email and we'll send you a secure reset link.", color=Color.White.copy(.75f), modifier=Modifier.padding(top=8.dp))
  }
  Card(Modifier.padding(20.dp).fillMaxWidth(), shape=RoundedCornerShape(22.dp), colors=CardDefaults.cardColors(Color.White)){
   Column(Modifier.padding(20.dp), verticalArrangement=Arrangement.spacedBy(14.dp)){
    Row(verticalAlignment=Alignment.CenterVertically){ Box(Modifier.size(42.dp).clip(CircleShape).background(AppColors.Navy), contentAlignment=Alignment.Center){Icon(Icons.Default.Email,null,tint=Color.White)}; Spacer(Modifier.width(12.dp)); Column{Text("STEP 1 OF 1", color=Color.Gray); Text("Enter Your Email", fontWeight=FontWeight.Bold)} }
    OutlinedTextField(value=email, onValueChange=vm::onEmailChange, label={Text("Email Address")}, leadingIcon={Icon(Icons.Default.Email,null)}, modifier=Modifier.fillMaxWidth())
    Text("Check your inbox and spam folder. The reset link expires in 30 minutes.", color=Color(0xFF8A6A20), modifier=Modifier.clip(RoundedCornerShape(14.dp)).background(Color(0xFFFFFAEF)).padding(14.dp))
    if(error!=null) Text(error ?: "", color=Color.Red)
    Button(onClick={vm.requestReset()}, enabled=!loading, modifier=Modifier.fillMaxWidth().height(52.dp), colors=ButtonDefaults.buttonColors(containerColor=AppColors.Gold), shape=RoundedCornerShape(14.dp)){ if(loading) CircularProgressIndicator(color=Color.White, modifier=Modifier.size(20.dp)) else { Icon(Icons.Default.Send,null); Spacer(Modifier.width(8.dp)); Text("Receive a Reset link") } }
   }
  }
  TextButton(onClick=onBackClick, modifier=Modifier.align(Alignment.CenterHorizontally)){ Text("Remember your password? Back to Login") }
 }
}
