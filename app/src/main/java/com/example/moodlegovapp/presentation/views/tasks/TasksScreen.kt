package com.example.moodlegovapp.presentation.views.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.presentation.viewmodels.TasksViewModel
import com.example.moodlegovapp.ui.theme.AppColors

@Composable
fun TasksScreen(vm: TasksViewModel, onTaskClick: (Int) -> Unit = {}, onBackClick: (() -> Unit)? = null) {
    val tasks by vm.tasks.collectAsState(); val loading by vm.isLoading.collectAsState(); val error by vm.error.collectAsState()
    val active by vm.activeCount.collectAsState(); val certs by vm.certificatesCount.collectAsState(); val pending by vm.pendingCount.collectAsState()
    LaunchedEffect(Unit) { vm.load() }
    LazyColumn(Modifier.fillMaxSize().background(AppColors.Background), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { Header("Taskflow", onBackClick, active, certs, pending) }
        item { SearchBox("Search Tasks") }
        item { SectionTitle("Priority Tasks", "${tasks.count{it.isPriority == true}} urgent") }
        items(tasks.filter { it.isPriority == true }.take(2)) { task -> TaskCard(task.title ?: "Task", task.courseName ?: "", task.status ?: "Due Soon", task.isOverdue == true) { onTaskClick(task.assignId ?: task.id ?: 0) } }
        item { SectionTitle("All Tasks", "${tasks.size} total") }
        items(tasks) { task -> TaskCard(task.title ?: "Task", task.courseName ?: "", task.status ?: "Pending", task.isOverdue == true) { onTaskClick(task.assignId ?: task.id ?: 0) } }
        if (loading) item { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center){ CircularProgressIndicator(color=AppColors.Navy) } }
        if (error != null) item { Text(error ?: "", color = Color.Red, modifier = Modifier.padding(20.dp)) }
    }
}

@Composable private fun Header(title:String, onBackClick:(()->Unit)?, active:Int, certs:Int, pending:Int){
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart=28.dp,bottomEnd=28.dp)).background(AppColors.NavyGradient).padding(20.dp)){
        Row(verticalAlignment=Alignment.CenterVertically){ if(onBackClick!=null) IconButton(onClick=onBackClick){ Icon(Icons.Default.ArrowBack,null,tint=Color.White) }; Text(title,color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold) }
        Spacer(Modifier.height(20.dp)); Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White.copy(.12f)).padding(14.dp), horizontalArrangement=Arrangement.SpaceAround){ Stat(active,"ACTIVE TASKS"); Stat(certs,"CERTIFICATES"); Stat(pending,"PENDING") }
    }
}
@Composable private fun Stat(v:Int,l:String){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Text("$v",color=Color.White,fontWeight=FontWeight.Bold); Text(l,color=Color.White.copy(.7f),fontSize=11.sp) } }
@Composable private fun SearchBox(text:String){ Row(Modifier.padding(20.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White).padding(14.dp), verticalAlignment=Alignment.CenterVertically){ Icon(Icons.Default.Search,null,tint=Color.Gray); Spacer(Modifier.width(8.dp)); Text(text,color=Color.Gray) } }
@Composable private fun SectionTitle(t:String, pill:String){ Row(Modifier.fillMaxWidth().padding(horizontal=20.dp, vertical=8.dp), verticalAlignment=Alignment.CenterVertically){ Text(t,fontSize=18.sp,fontWeight=FontWeight.Bold); Spacer(Modifier.weight(1f)); Text(pill, fontSize=12.sp, color=Color.Gray, modifier=Modifier.clip(RoundedCornerShape(20.dp)).background(Color.White).padding(horizontal=12.dp, vertical=6.dp)) } }
@Composable private fun TaskCard(title:String, course:String, status:String, danger:Boolean, onClick:()->Unit){ Card(onClick=onClick, modifier=Modifier.padding(horizontal=20.dp, vertical=6.dp).fillMaxWidth(), shape=RoundedCornerShape(18.dp), colors=CardDefaults.cardColors(Color.White)){ Row(Modifier.padding(16.dp), verticalAlignment=Alignment.CenterVertically){ Box(Modifier.size(44.dp).clip(CircleShape).background(if(danger) Color(0xFFFFE8E8) else AppColors.Navy.copy(.12f)), contentAlignment=Alignment.Center){ Icon(Icons.Default.Assignment,null,tint= if(danger) Color.Red else AppColors.Navy) }; Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)){ Text(title,fontWeight=FontWeight.Bold); Text(course,color=Color.Gray,fontSize=12.sp) }; Text(status,color=if(danger) Color.Red else Color(0xFF1BBE65),fontSize=12.sp) } } }
