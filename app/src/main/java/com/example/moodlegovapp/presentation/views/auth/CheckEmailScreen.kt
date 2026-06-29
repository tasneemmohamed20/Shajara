package com.example.moodlegovapp.presentation.views.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.ui.theme.AppColors

@Composable fun CheckEmailScreen(email:String, onBackToLogin:()->Unit, onBackClick:()->Unit){
 Column(Modifier.fillMaxSize().background(AppColors.Background)){
  Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart=28.dp,bottomEnd=28.dp)).background(AppColors.NavyGradient).padding(24.dp)){
   IconButton(onClick=onBackClick){Icon(Icons.Default.ArrowBack,null,tint=Color.White)}; Text("EMAIL SENT", color=Color(0xFF2ECC71), modifier=Modifier.padding(top=24.dp)); Text("Check Your Email", color=Color.White, fontSize=28.sp, fontWeight=FontWeight.Bold); Text("We've sent password reset instructions to your email address.", color=Color.White.copy(.75f), modifier=Modifier.padding(top=8.dp))
  }
  Card(Modifier.padding(20.dp).fillMaxWidth(), shape=RoundedCornerShape(22.dp), colors=CardDefaults.cardColors(Color.White)){
   Column(Modifier.padding(24.dp), horizontalAlignment=Alignment.CenterHorizontally){ Box(Modifier.size(110.dp).clip(CircleShape).background(Color(0xFFE2FAEA)), contentAlignment=Alignment.Center){ Icon(Icons.Default.Email,null,tint=Color(0xFF21B45B), modifier=Modifier.size(54.dp)) }; Text("RESET LINK SENT SUCCESSFULLY", color=Color(0xFF21B45B), modifier=Modifier.padding(20.dp)); Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(AppColors.Background).padding(16.dp), verticalAlignment=Alignment.CenterVertically){ Icon(Icons.Default.Email,null,tint=AppColors.Navy); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)){Text("SENT TO", color=Color.Gray); Text(email, fontWeight=FontWeight.Bold)}; Icon(Icons.Default.CheckCircle,null,tint=Color(0xFF21B45B)) }; Spacer(Modifier.height(24.dp)); Text("NEXT STEPS", color=Color.Gray, modifier=Modifier.fillMaxWidth()); Text("1  Check your email inbox\n2  Click the reset link\n3  Create a new password", modifier=Modifier.fillMaxWidth().padding(top=12.dp)); Text("Didn't receive it? Check your spam folder or tap resend below.", color=Color(0xFF8A6A20), modifier=Modifier.padding(top=20.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFFFFAEF)).padding(14.dp)) }
  }
  Button(onClick=onBackToLogin, modifier=Modifier.padding(horizontal=20.dp).fillMaxWidth().height(52.dp), colors=ButtonDefaults.buttonColors(containerColor=AppColors.Gold), shape=RoundedCornerShape(14.dp)){ Text("Back to Login") }
 }
}
