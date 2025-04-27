@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.spot.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import com.example.spot.navigation.Routes
import com.example.spot.ui.theme.*

@Composable
fun ClassesScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Your Classes", color = Green700, fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )
        
        // Main Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Generate some sample classes
            val classes = listOf(
                ClassItem("CSIT 321 - G1", "Applications Development", "Leah Barbaso", "MW 7:30 - 10:00AM NGE 102"),
                ClassItem("CSIT 340 - G1", "Industry Elective", "Eugene Busico", "TTH 7:30 - 11:00AM NGE 203"),
                ClassItem("IT 344 - G1", "Systems Administration and Maintenance", "Jensar Sayson", "WF 7:30 - 10:30AM NGE 207"),
                ClassItem("IT 342 - G1", "Systems Integration and Architecture", "Frederick Revilleza", "MF 12:00 - 3:00PM NGE 203")
            )
            
            items(classes) { classItem ->
                ClassCard(classItem, navController)
            }
        }
    }
}

data class ClassItem(
    val code: String,
    val name: String,
    val instructor: String,
    val time: String
)

@Composable
fun ClassCard(classItem: ClassItem, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Green100),
        onClick = { navController.navigate("class_view/${classItem.code}") } // Navigate to ClassViewScreen
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Gray)
            ) {
                // Placeholder for instructor image
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = classItem.code,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                )
                Text(
                    text = classItem.name,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextDark)
                )
                Text(
                    text = "Instructor: ${classItem.instructor}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextDark.copy(alpha = 0.7f))
                )
                Text(
                    text = classItem.time,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextDark.copy(alpha = 0.7f))
                )
            }
            Button(
                onClick = { /* Join class logic */ },
                colors = ButtonDefaults.buttonColors(containerColor = Green700)
            ) {
                Text("Join", color = Color.White)
            }
        }
    }
}