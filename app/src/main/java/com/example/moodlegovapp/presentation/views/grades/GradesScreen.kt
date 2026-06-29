package com.example.moodlegovapp.presentation.views.grades

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.presentation.viewmodels.GradesViewModel
import com.example.moodlegovapp.ui.theme.AppColors

@Composable fun GradesScreen(vm: GradesViewModel){
 val data by vm.data.collectAsState(); val loading by vm.isLoading.collectAsState(); val error by vm.error.collectAsState(); LaunchedEffect(Unit){vm.load()}
 LazyColumn(Modifier.fillMaxSize().background(AppColors.Background), contentPadding=PaddingValues(bottom=24.dp)){
  item{ Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart=28.dp,bottomEnd=28.dp)).background(AppColors.NavyGradient).padding(20.dp)){ Text("SHARJAH POLICE ACADEMY",color=Color.White.copy(.75f),fontSize=12.sp); Text("Grades",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold); Spacer(Modifier.height(22.dp)); Text("OVERALL PERFORMANCE",color=Color.White.copy(.7f)); Text("${data?.overallPerformancePercent ?: 0}%",color=AppColors.Gold,fontSize=34.sp,fontWeight=FontWeight.Bold); LinearProgressIndicator(progress = (data?.overallPerformancePercent ?: 0) / 100f, modifier=Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)), color=AppColors.Gold, trackColor=Color.White.copy(.25f)) } }
  item{ Text("Assessment Results",fontWeight=FontWeight.Bold,fontSize=18.sp,modifier=Modifier.padding(20.dp)) }
  items(data?.assessments ?: emptyList()){ a -> Card(Modifier.padding(horizontal=20.dp, vertical=6.dp).fillMaxWidth(), shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(Color.White)){ Row(Modifier.padding(16.dp), verticalAlignment=Alignment.CenterVertically){ Icon(Icons.Default.BarChart,null,tint=AppColors.Navy); Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)){ Text(a.title ?: "Assessment",fontWeight=FontWeight.SemiBold); Text(a.courseName ?: "",color=Color.Gray,fontSize=12.sp); Text(a.gradeLabel ?: a.status ?: "",color=Color(0xFF1BBE65),fontSize=12.sp) }; Box(Modifier.size(58.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFE9FFF2)), contentAlignment=Alignment.Center){ Text("${a.gradePercent ?: 0}\n/100",color=Color(0xFF1BBE65),fontSize=12.sp) } } } }
  if(loading) item{Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment=Alignment.Center){CircularProgressIndicator(color=AppColors.Navy)}}
  if(error!=null) item{Text(error ?: "", color=Color.Red, modifier=Modifier.padding(20.dp))}
 }
}
