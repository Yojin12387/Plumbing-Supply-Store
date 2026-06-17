package com.example.trubochki.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.trubochki.models.Product
import com.example.trubochki.models.Service
import com.example.trubochki.repository.FirebaseRepository
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var currentScreen by remember { mutableStateOf<AdminSubScreen>(AdminSubScreen.List) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var editingEmployee by remember { mutableStateOf<Employee?>(null) }

    when (currentScreen) {
        AdminSubScreen.List -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // ШАПКА С КНОПКОЙ ВЫХОДА
                TopAppBar(
                    title = { Text("Панель администратора", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1565C0)),
                    actions = {
                        // КНОПКА ВЫХОДА
                        IconButton(onClick = onLogout) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = "Выйти",
                                tint = Color.White
                            )
                        }
                    }
                )

                // ТАБЫ
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF1565C0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Товары", modifier = Modifier.padding(16.dp), color = Color.White)
                    }
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Мастера", modifier = Modifier.padding(16.dp), color = Color.White)
                    }
                }

                // КОНТЕНТ
                when (selectedTab) {
                    0 -> ManageProductsScreen(
                        onAddClick = {
                            editingProduct = null
                            currentScreen = AdminSubScreen.AddEditProduct
                        },
                        onEditClick = { product ->
                            editingProduct = product
                            currentScreen = AdminSubScreen.AddEditProduct
                        }
                    )
                    1 -> ManageEmployeesScreen(
                        onAddClick = {
                            editingEmployee = null
                            currentScreen = AdminSubScreen.AddEditEmployee
                        },
                        onEditClick = { employee ->
                            editingEmployee = employee
                            currentScreen = AdminSubScreen.AddEditEmployee
                        }
                    )
                }
            }
        }

        AdminSubScreen.AddEditProduct -> {
            AddEditProductScreen(
                product = editingProduct,
                onBack = { currentScreen = AdminSubScreen.List },
                onSaved = { currentScreen = AdminSubScreen.List }
            )
        }

        AdminSubScreen.AddEditEmployee -> {
            AddEditEmployeeScreen(
                employee = editingEmployee,
                onBack = { currentScreen = AdminSubScreen.List },
                onSaved = { currentScreen = AdminSubScreen.List }
            )
        }
    }
}

sealed class AdminSubScreen {
    object List : AdminSubScreen()
    object AddEditProduct : AdminSubScreen()
    object AddEditEmployee : AdminSubScreen()
}

@Composable
fun ManageProductsScreen(
    onAddClick: () -> Unit,
    onEditClick: (Product) -> Unit
) {
    val context = LocalContext.current
    val repository = FirebaseRepository()
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun loadProducts() {
        coroutineScope.launch {
            products = repository.getProducts()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadProducts()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Управление товарами",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp),
            color = Color(0xFF1565C0)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            items(products) { product ->
                ProductAdminCard(
                    product = product,
                    onEdit = {
                        editingProduct = product
                        showDialog = true
                    },
                    onDelete = {
                        coroutineScope.launch {
                            repository.deleteProduct(product.id)
                            loadProducts()
                            Toast.makeText(context, "Удалено", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        Button(
            onClick = {
                editingProduct = null
                showDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
            shape = RoundedCornerShape(0.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Добавить товар", fontSize = 16.sp, color = Color.White)
            }
        }
    }

    if (showDialog) {
        ProductDialog(
            product = editingProduct,
            onDismiss = { showDialog = false },
            onSave = { name, description, price, category, inStock, imageUrl, masterIds ->
                coroutineScope.launch {
                    try {
                        if (editingProduct == null) {
                            val newProduct = Product(
                                name = name,
                                description = description,
                                price = price.toDoubleOrNull() ?: 0.0,
                                category = category,
                                inStock = inStock,
                                imageUrl = imageUrl,
                                masterIds = masterIds
                            )
                            repository.addProduct(newProduct, null)
                            Toast.makeText(context, "Товар добавлен!", Toast.LENGTH_SHORT).show()
                        } else {
                            val updatedProduct = editingProduct!!.copy(
                                name = name,
                                description = description,
                                price = price.toDoubleOrNull() ?: 0.0,
                                category = category,
                                inStock = inStock,
                                imageUrl = imageUrl,
                                masterIds = masterIds
                            )
                            repository.updateProduct(updatedProduct)
                            Toast.makeText(context, "Товар обновлен!", Toast.LENGTH_SHORT).show()
                        }
                        loadProducts()
                        showDialog = false
                    } catch (e: Exception) {
                        Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}

@Composable
fun ManageEmployeesScreen(
    onAddClick: () -> Unit,
    onEditClick: (Employee) -> Unit
) {
    val context = LocalContext.current
    val repository = FirebaseRepository()
    var employees by remember { mutableStateOf<List<Employee>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var editingEmployee by remember { mutableStateOf<Employee?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun loadEmployees() {
        coroutineScope.launch {
            employees = repository.getEmployees()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadEmployees()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Управление мастерами",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp),
            color = Color(0xFF1565C0)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            items(employees) { employee ->
                EmployeeAdminCard(
                    employee = employee,
                    onEdit = {
                        editingEmployee = employee
                        showDialog = true
                    },
                    onDelete = {
                        coroutineScope.launch {
                            repository.deleteEmployee(employee.id)
                            loadEmployees()
                            Toast.makeText(context, "Удалено", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        Button(
            onClick = {
                editingEmployee = null
                showDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
            shape = RoundedCornerShape(0.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Добавить мастера", fontSize = 16.sp, color = Color.White)
            }
        }
    }

    if (showDialog) {
        EmployeeDialog(
            employee = editingEmployee,
            onDismiss = { showDialog = false },
            onSave = { name, position, phone, email, experience, specialty, photoUrl, services ->
                coroutineScope.launch {
                    try {
                        if (editingEmployee == null) {
                            val newEmployee = Employee(
                                name = name,
                                position = position,
                                phone = phone,
                                email = email,
                                experience = experience,
                                specialty = specialty,
                                photoUrl = photoUrl,
                                services = services
                            )
                            repository.addEmployee(newEmployee, null)
                            Toast.makeText(context, "Мастер добавлен!", Toast.LENGTH_SHORT).show()
                        } else {
                            val updatedEmployee = editingEmployee!!.copy(
                                name = name,
                                position = position,
                                phone = phone,
                                email = email,
                                experience = experience,
                                specialty = specialty,
                                photoUrl = photoUrl,
                                services = services
                            )
                            repository.updateEmployee(updatedEmployee)
                            Toast.makeText(context, "Мастер обновлен!", Toast.LENGTH_SHORT).show()
                        }
                        loadEmployees()
                        showDialog = false
                    } catch (e: Exception) {
                        Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}

@Composable
fun ProductAdminCard(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (product.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(product.imageUrl),
                    contentDescription = product.name,
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF3E0)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🔧", fontSize = 32.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text(product.description, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                Text("${product.price} ₽", fontSize = 12.sp, color = Color(0xFF1565C0))
                if (!product.inStock) {
                    Text("Нет в наличии", fontSize = 10.sp, color = Color.Red)
                }
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF1565C0))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}

@Composable
fun ProductDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, price: String, category: String, inStock: Boolean, imageUrl: String, masterIds: List<String>) -> Unit
) {
    val context = LocalContext.current
    val repository = FirebaseRepository()
    var name by remember { mutableStateOf(product?.name ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var inStock by remember { mutableStateOf(product?.inStock ?: true) }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "") }
    var selectedMasterIds by remember { mutableStateOf<List<String>>(product?.masterIds ?: emptyList()) }
    var masters by remember { mutableStateOf<List<Employee>>(emptyList()) }
    var isLoadingMasters by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            masters = repository.getEmployees()
            isLoadingMasters = false
        }
    }

    fun toggleMaster(masterId: String) {
        selectedMasterIds = if (selectedMasterIds.contains(masterId)) {
            selectedMasterIds.filter { it != masterId }
        } else {
            selectedMasterIds + masterId
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Добавить товар" else "Редактировать товар") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Цена *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Категория") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL фото (необязательно)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://example.com/photo.jpg", fontSize = 12.sp) }
                )
                if (imageUrl.isNotEmpty()) {
                    ProductImagePreview(imageUrl = imageUrl)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("В наличии:")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = inStock, onCheckedChange = { inStock = it })
                }

                Text(
                    text = "Связанные мастера:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (isLoadingMasters) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else if (masters.isEmpty()) {
                    Text(
                        text = "Нет доступных мастеров. Сначала добавьте мастеров.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                } else {
                    masters.forEach { master ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = selectedMasterIds.contains(master.id),
                                onCheckedChange = { toggleMaster(master.id) }
                            )
                            Column(modifier = Modifier.padding(start = 4.dp)) {
                                Text(master.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(master.position, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                Text(
                    text = "💡 Совет: выберите мастеров, которые могут выполнять работы с этим товаром",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Введите название", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    if (price.isBlank()) {
                        Toast.makeText(context, "Введите цену", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    onSave(name, description, price, category, inStock, imageUrl, selectedMasterIds)
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun ProductImagePreview(imageUrl: String) {
    Card(
        modifier = Modifier.fillMaxWidth().height(150.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = imageUrl,
                error = painterResource(id = android.R.drawable.ic_menu_gallery)
            ),
            contentDescription = "Preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun EmployeeAdminCard(employee: Employee, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (employee.photoUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = employee.photoUrl,
                        error = painterResource(id = android.R.drawable.ic_menu_gallery)
                    ),
                    contentDescription = employee.name,
                    modifier = Modifier.size(50.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    color = Color(0xFFFFF3E0)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("👨‍🔧", fontSize = 28.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(employee.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(employee.position, fontSize = 12.sp, color = Color.Gray)
                Text(employee.phone, fontSize = 11.sp, color = Color(0xFF1565C0))
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Редактировать", tint = Color(0xFF1565C0))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.Red)
            }
        }
    }
}

@Composable
fun EmployeeDialog(
    employee: Employee?,
    onDismiss: () -> Unit,
    onSave: (name: String, position: String, phone: String, email: String, experience: String, specialty: String, photoUrl: String, services: List<Service>) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(employee?.name ?: "") }
    var position by remember { mutableStateOf(employee?.position ?: "") }
    var phone by remember { mutableStateOf(employee?.phone ?: "") }
    var email by remember { mutableStateOf(employee?.email ?: "") }
    var experience by remember { mutableStateOf(employee?.experience ?: "") }
    var specialty by remember { mutableStateOf(employee?.specialty ?: "") }
    var photoUrl by remember { mutableStateOf(employee?.photoUrl ?: "") }

    var servicesText by remember {
        mutableStateOf(
            employee?.services?.joinToString("\n") { "${it.name} - ${it.price} ₽" } ?: ""
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (employee == null) "Добавить мастера" else "Редактировать мастера") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ФИО *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it },
                    label = { Text("Должность *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Телефон *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = experience,
                    onValueChange = { experience = it },
                    label = { Text("Стаж") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Специализация") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = servicesText,
                    onValueChange = { servicesText = it },
                    label = { Text("Услуги и цены") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    placeholder = {
                        Text(
                            "Пример:\nУстановка смесителя - 1500 ₽\nЗамена труб - 3000 ₽",
                            fontSize = 12.sp
                        )
                    },
                    minLines = 5,
                    maxLines = 10
                )

                OutlinedTextField(
                    value = photoUrl,
                    onValueChange = { photoUrl = it },
                    label = { Text("URL фото (необязательно)") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (photoUrl.isNotEmpty()) {
                    EmployeePhotoPreview(photoUrl = photoUrl)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📝 Формат ввода услуг:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                        Text(
                            text = "Название услуги - цена ₽ (каждая услуга с новой строки)",
                            fontSize = 11.sp,
                            color = Color(0xFF1565C0)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Введите ФИО", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    if (position.isBlank()) {
                        Toast.makeText(context, "Введите должность", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    if (phone.isBlank()) {
                        Toast.makeText(context, "Введите телефон", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }

                    val services = servicesText
                        .split("\n")
                        .mapNotNull { line ->
                            val trimmed = line.trim()
                            if (trimmed.isNotEmpty()) {
                                val separator = if (trimmed.contains(" - ")) " - " else if (trimmed.contains(" – ")) " – " else null
                                if (separator != null) {
                                    val parts = trimmed.split(separator)
                                    if (parts.size >= 2) {
                                        val serviceName = parts[0].trim()
                                        val price = parts[1].trim().replace("₽", "").trim()
                                        Service(name = serviceName, price = price)
                                    } else null
                                } else null
                            } else null
                        }

                    onSave(name, position, phone, email, experience, specialty, photoUrl, services)
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun EmployeePhotoPreview(photoUrl: String) {
    Card(
        modifier = Modifier.fillMaxWidth().height(150.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = photoUrl,
                error = painterResource(id = android.R.drawable.ic_menu_gallery)
            ),
            contentDescription = "Preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}