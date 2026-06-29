package com.example.moodlegovapp.presentation.views.resources

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.presentation.viewmodels.CourseResourcesViewModel
import com.example.moodlegovapp.ui.theme.AppColors

@Composable fun CourseResourcesScreen(courseId:Int, vm: CourseResourcesViewModel, onBackClick:()->Unit){
 val data by vm.data.collectAsState(); val loading by vm.isLoading.collectAsState(); val context= LocalContext.current; LaunchedEffect(courseId){vm.load(courseId)}
 LazyColumn(Modifier.fillMaxSize().background(AppColors.Background), contentPadding=PaddingValues(bottom=24.dp)){
  item{ Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart=28.dp,bottomEnd=28.dp)).background(AppColors.NavyGradient).padding(24.dp)){ IconButton(onClick=onBackClick){Icon(Icons.Default.ArrowBack,null,tint=Color.White)}; Text("Course Resources",color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Bold,modifier=Modifier.align(Alignment.CenterHorizontally)); Spacer(Modifier.height(28.dp)); Text(data?.courseTitle ?: "Course Resources",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold); Text("${data?.instructorName ?: ""}  •  ${data?.totalResourcesCount ?: 0} Resources",color=Color.White.copy(.75f),modifier=Modifier.padding(top=12.dp)) } }
  item{ Row(Modifier.padding(20.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White).padding(14.dp), verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Search,null,tint=Color.Gray);Spacer(Modifier.width(8.dp));Text("Search resources", color=Color.Gray)} }
  data?.resourceGroups?.forEach{ group -> item{ Row(Modifier.fillMaxWidth().padding(horizontal=20.dp, vertical=10.dp), verticalAlignment=Alignment.CenterVertically){ Text(group.groupName ?: "Resources",fontSize=18.sp,fontWeight=FontWeight.Bold); Spacer(Modifier.weight(1f)); Text("${group.filesCount ?: group.files.size} files",fontSize=12.sp,color=Color.Gray,modifier=Modifier.clip(RoundedCornerShape(20.dp)).background(Color.White).padding(horizontal=12.dp, vertical=6.dp)) } }; items(group.files){ file -> Card(Modifier.padding(horizontal=20.dp, vertical=6.dp).fillMaxWidth(), shape=RoundedCornerShape(22.dp), colors=CardDefaults.cardColors(Color.White)){ Row(Modifier.padding(16.dp), verticalAlignment=Alignment.CenterVertically){ Box(Modifier.size(42.dp).clip(CircleShape).background(Color(0xFFFFEEEE)), contentAlignment=Alignment.Center){Icon(Icons.Default.InsertDriveFile,null,tint=Color.Red)}; Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)){ Text(file.name ?: "File",fontWeight=FontWeight.Bold); Text("${file.type ?: "FILE"}  ·  ${file.sizeMb ?: 0.0} MB",color=Color.Gray,fontSize=12.sp) }; IconButton(onClick={ file.url?.let{ context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) } }){ Icon(Icons.Default.Download,null,tint=AppColors.Navy) } } } } }
  if(loading) item{Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment=Alignment.Center){CircularProgressIndicator(color=AppColors.Navy)}}
 }
}
