package com.example.trubochki.ui.calculator

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.trubochki.models.Employee
import com.example.trubochki.models.Product
import com.example.trubochki.models.Service
import com.example.trubochki.repository.FirebaseRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = FirebaseRepository()
    val coroutineScope = rememberCoroutineScope()

    var employees by remember { mutableStateOf<List<Employee>>(emptyList()) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            employees = repository.getEmployees()
            products = repository.getProducts()
            isLoading = false
        }
    }

    val filteredProducts = if (selectedEmployee != null) {
        products.filter { product ->
            product.masterIds.contains(selectedEmployee?.id)
        }
    } else {
        emptyList()
    }

    val servicePrice = try {
        selectedService?.price?.replace("₽", "")?.replace("р", "")?.trim()?.toDoubleOrNull() ?: 0.0
    } catch (e: Exception) {
        0.0
    }
    val productPrice = selectedProduct?.price ?: 0.0
    val totalPrice = (servicePrice + productPrice) * quantity

    // Используем такую же структуру как в BusinessCardScreen
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // КОНТЕНТ
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Шаг 1: Выбор мастера
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("1️⃣", fontSize = 20.sp)
                                Text(
                                    "Выберите мастера",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (isLoading) {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    employees.forEach { employee ->
                                        EmployeeSelectorItem(
                                            employee = employee,
                                            isSelected = selectedEmployee?.id == employee.id,
                                            onClick = {
                                                selectedEmployee = employee
                                                selectedService = null
                                                selectedProduct = null
                                                quantity = 1
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Шаг 2: Выбор услуги
                if (selectedEmployee != null && selectedEmployee!!.services.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("2️⃣", fontSize = 20.sp)
                                    Text(
                                        "Выберите услугу",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    selectedEmployee!!.services.forEach { service ->
                                        ServiceSelectorItem(
                                            service = service,
                                            isSelected = selectedService?.name == service.name,
                                            onClick = {
                                                if (selectedService?.name == service.name) {
                                                    selectedService = null
                                                } else {
                                                    selectedService = service
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Шаг 3: Выбор товара
                if (selectedEmployee != null && filteredProducts.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("3️⃣", fontSize = 20.sp)
                                    Text(
                                        "Выберите товар",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    filteredProducts.forEach { product ->
                                        ProductSelectorItem(
                                            product = product,
                                            isSelected = selectedProduct?.id == product.id,
                                            onClick = {
                                                if (selectedProduct?.id == product.id) {
                                                    selectedProduct = null
                                                } else {
                                                    selectedProduct = product
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Количество
                if (selectedService != null || selectedProduct != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "4️⃣ Количество",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { if (quantity > 1) quantity-- },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFFE3F2FD), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Уменьшить")
                                    }

                                    Text(
                                        text = "$quantity",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(60.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )

                                    IconButton(
                                        onClick = { quantity++ },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFFE3F2FD), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Увеличить")
                                    }
                                }
                            }
                        }
                    }

                    // Итого
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0)),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Итого к оплате",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    "${totalPrice} ₽",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val phoneNumber = "+79991234567"  // Замени на свой номер
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Phone,  // ← меняем иконку на телефон
                                        contentDescription = null,
                                        tint = Color(0xFF1565C0)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Позвонить мастеру",  // ← меняем текст
                                        color = Color(0xFF1565C0),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // МЕНЮ НЕ ДОБАВЛЯЕМ - ТАК КАК ОНО УЖЕ ЕСТЬ В BusinessCardScreen
    }
}

@Composable
fun EmployeeSelectorItem(
    employee: Employee,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFFE3F2FD) else Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (employee.photoUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(employee.photoUrl),
                    contentDescription = employee.name,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👨‍🔧", fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    employee.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    employee.position,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    "Специализация: ${employee.specialty}",
                    fontSize = 11.sp,
                    color = Color(0xFF1565C0)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Выбрано",
                    tint = Color(0xFF1565C0)
                )
            }
        }
    }
}

@Composable
fun ServiceSelectorItem(
    service: Service,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFFE3F2FD) else Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Construction,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF1565C0) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    service.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    "Цена: ${service.price} ₽",
                    fontSize = 13.sp,
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.Bold
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Выбрано",
                    tint = Color(0xFF1565C0)
                )
            }
        }
    }
}

@Composable
fun ProductSelectorItem(
    product: Product,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFFE3F2FD) else Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (product.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(product.imageUrl),
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔧", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    product.description,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
                Text(
                    "Цена: ${product.price} ₽",
                    fontSize = 13.sp,
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.Bold
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Выбрано",
                    tint = Color(0xFF1565C0)
                )
            }
        }
    }
}