package com.example.trubochki.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.trubochki.models.Employee
import com.example.trubochki.repository.FirebaseRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEmployeeScreen(
    employee: Employee? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val repository = FirebaseRepository()
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf(employee?.name ?: "") }
    var position by remember { mutableStateOf(employee?.position ?: "") }
    var phone by remember { mutableStateOf(employee?.phone ?: "") }
    var email by remember { mutableStateOf(employee?.email ?: "") }
    var experience by remember { mutableStateOf(employee?.experience ?: "") }
    var specialty by remember { mutableStateOf(employee?.specialty ?: "") }
    var photoUrl by remember { mutableStateOf(employee?.photoUrl ?: "") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (employee == null) "Добавить мастера" else "Редактировать мастера",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1565C0))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Аватар
            if (photoUrl.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = photoUrl,
                            error = painterResource(id = android.R.drawable.ic_menu_gallery)
                        ),
                        contentDescription = "Фото мастера",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Поля ввода
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("ФИО *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = position,
                onValueChange = { position = it },
                label = { Text("Должность *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Телефон *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = experience,
                onValueChange = { experience = it },
                label = { Text("Стаж") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = specialty,
                onValueChange = { specialty = it },
                label = { Text("Специализация") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = photoUrl,
                onValueChange = { photoUrl = it },
                label = { Text("URL фото") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://example.com/photo.jpg") },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Введите ФИО", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (position.isBlank()) {
                        Toast.makeText(context, "Введите должность", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (phone.isBlank()) {
                        Toast.makeText(context, "Введите телефон", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        try {
                            val newEmployee = Employee(
                                id = employee?.id ?: "",
                                name = name,
                                position = position,
                                phone = phone,
                                email = email,
                                experience = experience,
                                specialty = specialty,
                                photoUrl = photoUrl
                            )
                            if (employee == null) {
                                repository.addEmployee(newEmployee, null)
                                Toast.makeText(context, "Мастер добавлен!", Toast.LENGTH_SHORT).show()
                            } else {
                                repository.updateEmployee(newEmployee)
                                Toast.makeText(context, "Мастер обновлен!", Toast.LENGTH_SHORT).show()
                            }
                            onSaved()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(if (employee == null) "Добавить" else "Сохранить", fontSize = 16.sp)
                }
            }
        }
    }
}