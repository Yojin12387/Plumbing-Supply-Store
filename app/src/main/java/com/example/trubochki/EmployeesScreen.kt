package com.example.trubochki.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Employee(
    val id: Int,
    val name: String,
    val position: String,
    val phone: String,
    val email: String,
    val avatar: String
)

@Composable
fun EmployeesScreen() {
    val context = LocalContext.current
    val employees = listOf(
        Employee(1, "Анна Петрова", "Генеральный директор", "+7 (999) 111-22-33", "anna@trubochki.ru", "👩‍💼"),
        Employee(2, "Иван Смирнов", "Менеджер по продажам", "+7 (999) 222-33-44", "ivan@trubochki.ru", "👨‍💼"),
        Employee(3, "Елена Козлова", "Специалист по работе с клиентами", "+7 (999) 333-44-55", "elena@trubochki.ru", "👩‍💻"),
        Employee(4, "Михаил Соколов", "Логист", "+7 (999) 444-55-66", "mikhail@trubochki.ru", "📦"),
        Employee(5, "Ольга Новикова", "Бухгалтер", "+7 (999) 555-66-77", "olga@trubochki.ru", "💰")
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Шапка
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "👥",
                    fontSize = 48.sp
                )
                Text(
                    text = "Наша команда",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Всегда готовы помочь вам",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Список сотрудников
        LazyColumn(
            modifier = Modifier.padding(2.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(employees) { employee ->
                EmployeeCard(employee, context)
            }
        }
    }
}

@Composable
fun EmployeeCard(employee: Employee, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Аватар
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF3E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = employee.avatar,
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Информация о сотруднике
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = employee.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                    Text(
                        text = employee.position,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Контакты
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { dialEmployee(context, employee.phone) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Звонок", fontSize = 12.sp)
                }

                Button(
                    onClick = { emailEmployee(context, employee.email) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Email", fontSize = 12.sp)
                }
            }
        }
    }
}

fun dialEmployee(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${number.replace(" ", "").replace("(", "").replace(")", "").replace("-", "")}"))
    context.startActivity(intent)
}

fun emailEmployee(context: Context, email: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
    context.startActivity(intent)
}