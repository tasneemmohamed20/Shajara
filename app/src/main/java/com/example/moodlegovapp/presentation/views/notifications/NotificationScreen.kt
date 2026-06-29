package com.example.moodlegovapp.presentation.views.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.presentation.viewmodels.NotificationsViewModel
import com.example.moodlegovapp.ui.theme.AppColors

@Composable fun NotificationScreen(vm: NotificationsViewModel, onBackClick:()->Unit){
 val notifications by vm.notifications.collectAsState(); val isLoading by vm.isLoading.collectAsState(); val unread = vm.unreadCount; LaunchedEffect(Unit){vm.load()}
 LazyColumn(Modifier.fillMaxSize().background(AppColors.Background), contentPadding=PaddingValues(bottom=24.dp)){
  item{ Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart=28.dp,bottomEnd=28.dp)).background(AppColors.NavyGradient).padding(20.dp)){ Row(verticalAlignment=Alignment.CenterVertically){ IconButton(onClick=onBackClick){Icon(Icons.Default.ArrowBack,null,tint=Color.White)}; Spacer(Modifier.weight(1f)); Text("NotifyHub",color=Color.White,fontWeight=FontWeight.Bold,fontSize=20.sp); Spacer(Modifier.weight(1f)) }; Row(verticalAlignment=Alignment.CenterVertically){ Column{ Text("Notifications",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold); Text("Your training updates", color=Color.White.copy(.75f)) }; Spacer(Modifier.weight(1f)); Text("• $unread Unread", color=AppColors.Gold, modifier=Modifier.clip(RoundedCornerShape(20.dp)).background(Color.White.copy(.15f)).padding(horizontal=12.dp, vertical=8.dp)); Spacer(Modifier.width(8.dp)); Button(onClick={ notifications.forEach { vm.markAsRead(it.id) } }, colors=ButtonDefaults.buttonColors(containerColor=Color.White.copy(.16f))){Text("Mark all read")} } } }
  items(notifications){ n -> Card(Modifier.padding(horizontal=16.dp, vertical=6.dp).fillMaxWidth(), shape=RoundedCornerShape(22.dp), colors=CardDefaults.cardColors(Color.White)){ Row(Modifier.padding(16.dp), verticalAlignment=Alignment.CenterVertically){ Box(Modifier.size(46.dp).clip(CircleShape).background(AppColors.Navy), contentAlignment=Alignment.Center){Icon(Icons.Default.Notifications,null,tint=Color.White)}; Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)){ Text(n.title,fontWeight=FontWeight.Bold); Text(n.body,color=Color.Gray,fontSize=12.sp) }; Text(n.createdAtFormatted,color=Color.Gray,fontSize=11.sp) } } }
  if(isLoading) item{Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment=Alignment.Center){CircularProgressIndicator(color=AppColors.Navy)}}
 }
}
