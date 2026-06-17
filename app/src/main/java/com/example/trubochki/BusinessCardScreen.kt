package com.example.trubochki.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.trubochki.ui.calculator.CalculatorScreen
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data Models
data class PlumbingProduct(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "",
    val inStock: Boolean = true
)

data class PlumbingEmployee(
    val id: String = "",
    val name: String = "",
    val position: String = "",
    val phone: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val experience: String = "",
    val specialty: String = "",
    val services: List<PlumbingService> = emptyList()
)

data class PlumbingService(
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val masterId: String = "",
    val masterName: String = ""
)

@Composable
fun BusinessCardScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedProduct by remember { mutableStateOf<PlumbingProduct?>(null) }
    var selectedEmployee by remember { mutableStateOf<PlumbingEmployee?>(null) }
    var showAdminLogin by remember { mutableStateOf(false) }
    var showAdminPanel by remember { mutableStateOf(false) }  // ← добавляем состояние для админ-панели
    var clickCount by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    fun onLogoClick() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > 1000) {
            clickCount = 1
        } else {
            clickCount++
        }
        lastClickTime = currentTime

        if (clickCount >= 5) {
            showAdminLogin = true
            clickCount = 0
            Toast.makeText(context, "Вход в админ-панель", Toast.LENGTH_SHORT).show()
        }
    }

    val tabs = listOf("Главная", "Товары", "Услуги", "Мастера", "Расчет")

    if (selectedProduct != null) {
        ProductDetailScreen(
            product = selectedProduct!!,
            onBack = { selectedProduct = null }
        )
    } else if (selectedEmployee != null) {
        EmployeeDetailScreen(
            employee = selectedEmployee!!,
            onBack = { selectedEmployee = null }
        )
    } else if (showAdminLogin) {
        // Экран входа
        AdminLoginScreen(
            onLoginSuccess = {
                showAdminLogin = false
                showAdminPanel = true  // ← после успешного входа открываем админ-панель
            },
            onBack = {
                showAdminLogin = false
            }
        )
    } else if (showAdminPanel) {
        // Админ-панель
        AdminPanelScreen(onLogout = {
            showAdminPanel = false
        })
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> MainTab(Modifier.fillMaxSize(), context, ::onLogoClick)
                    1 -> ProductsTab(Modifier.fillMaxSize(), onProductClick = { product -> selectedProduct = product })
                    2 -> ServicesScreen()
                    3 -> EmployeesTab(Modifier.fillMaxSize(), onEmployeeClick = { employee -> selectedEmployee = employee })
                    4 -> CalculatorScreen()
                }
            }

            NavigationBar(
                containerColor = Color(0xFF1565C0),
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                when (index) {
                                    0 -> Icons.Default.Home
                                    1 -> Icons.Default.ShoppingCart
                                    2 -> Icons.Default.Build
                                    3 -> Icons.Default.People
                                    else -> Icons.Default.Calculate
                                },
                                contentDescription = title,
                                tint = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.6f)
                            )
                        },
                        label = {
                            Text(
                                title,
                                color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.6f)
                            )
                        },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AdminSection(modifier: Modifier) {
    var isAdminLoggedIn by remember { mutableStateOf(false) }

    if (!isAdminLoggedIn) {
        AdminLoginScreen(onLoginSuccess = { isAdminLoggedIn = true })
    } else {
        AdminPanelScreen(onLogout = { isAdminLoggedIn = false })
    }
}

@Composable
fun MainTab(
    modifier: Modifier,
    context: Context,
    onLogoClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier.padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { HeaderCard(onLogoClick = onLogoClick) }
        item { ContactsCard(context) }
        item { DetailsCard(context) }
        item { QRCard(context) }
    }
}

@Composable
fun HeaderCard(onLogoClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .clickable(onClick = onLogoClick),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🔧", fontSize = 40.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Сантехника-Профи",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Профессиональный ремонт и установка",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}
@Composable
fun ContactsCard(context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Контакты",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(12.dp))

            ContactItem(
                emoji = "📞",
                title = "Телефон",
                value = "+7 (999) 123-45-67"
            ) {
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+79991234567")))
            }

            ContactItem(
                emoji = "📧",
                title = "Email",
                value = "info@santehnika.ru"
            ) {
                context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:info@santehnika.ru")))
            }

            ContactItem(
                emoji = "📍",
                title = "Адрес",
                value = "г. Москва, ул. Строителей, д. 15"
            ) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=Москва Строителей 15")))
            }

            ContactItem(
                emoji = "🕐",
                title = "Режим работы",
                value = "Пн-Вс: 8:00 - 20:00"
            ) { }

            ContactItem(
                emoji = "🚨",
                title = "Аварийная служба",
                value = "Круглосуточно"
            ) { }
        }
    }
}

@Composable
fun ContactItem(emoji: String, title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 28.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DetailsCard(context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Реквизиты",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(12.dp))

            DetailRow("ИНН", "1234567890", context)
            DetailRow("КПП", "123456789", context)
            DetailRow("ОГРН", "1234567890123", context)
            DetailRow("Р/С", "40702810123456789012", context)
            DetailRow("БИК", "044525225", context)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText(label, value))
                Toast.makeText(context, "$label скопирован", Toast.LENGTH_SHORT).show()
            }
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
        Text(value, fontSize = 14.sp, color = Color.Black)
    }
}

@Composable
fun QRCard(context: Context) {
    val appLink = "https://play.google.com/store/apps/details?id=${context.packageName}"
    val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${Uri.encode(appLink)}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "QR-код приложения",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Наведите камеру для установки",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .size(180.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(qrUrl),
                    contentDescription = "QR-код для установки приложения",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, "Установите приложение \"Сантехника-Профи\" по ссылке: $appLink")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Поделиться"))
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Поделиться ссылкой")
            }
        }
    }
}

// Products Tab with Firebase
@Composable
fun ProductsTab(
    modifier: Modifier,
    onProductClick: (PlumbingProduct) -> Unit
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    var products by remember { mutableStateOf<List<PlumbingProduct>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val snapshot = firestore.collection("products").get().await()
                products = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(PlumbingProduct::class.java)?.copy(id = doc.id)
                }
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(context, "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Шапка с градиентом - ТЕПЕРЬ ВСЕ ПО ЦЕНТРУ
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1565C0)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🛠️",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Сантехника и оборудование",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Качественные товары для вашего дома",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        items(products) { product ->
            ProductCardModern(
                product = product,
                onClick = { onProductClick(product) }
            )
        }
    }
}

@Composable
fun ProductCardModern(
    product: PlumbingProduct,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Фото товара
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
                        )
                    )
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = product.imageUrl,
                            error = painterResource(id = android.R.drawable.ic_menu_gallery)
                        ),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔧", fontSize = 64.sp)
                    }
                }
            }

            // Информация о товаре
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = product.description,
                    fontSize = 13.sp,
                    color = Color(0xFF757575),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = "${product.price} ₽",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Подробнее",
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun PlumbingProductCard(
    product: PlumbingProduct,
    context: Context,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
                        )
                    )
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = product.imageUrl,
                            error = painterResource(id = android.R.drawable.ic_menu_gallery)
                        ),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔧", fontSize = 40.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = product.description,
                    fontSize = 13.sp,
                    color = Color(0xFF757575),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE3F2FD)
                ) {
                    Text(
                        text = "${product.price} ₽",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (!product.inStock) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFF9800)
                        )
                        Text(
                            text = "Нет в наличии",
                            fontSize = 11.sp,
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Подробнее",
                tint = Color(0xFF1565C0),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun EmployeesTab(
    modifier: Modifier,
    onEmployeeClick: (PlumbingEmployee) -> Unit
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    var employees by remember { mutableStateOf<List<PlumbingEmployee>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val snapshot = firestore.collection("employees").get().await()
                employees = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(PlumbingEmployee::class.java)?.copy(id = doc.id)
                }
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(context, "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Шапка - ТЕПЕРЬ ВСЕ ПО ЦЕНТРУ
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally  // ← ЦЕНТР
                ) {
                    Text(
                        text = "👨‍🔧",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Наши мастера",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Профессионалы с опытом работы",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center  // ← ТЕКСТ ПО ЦЕНТРУ
                    )
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(employees) { employee ->
                EmployeeCardModern(
                    employee = employee,
                    onClick = { onEmployeeClick(employee) }
                )
            }
        }
    }
}

@Composable
fun EmployeeCardModern(
    employee: PlumbingEmployee,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
                        )
                    )
            ) {
                if (employee.photoUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = employee.photoUrl,
                            error = painterResource(id = android.R.drawable.ic_menu_gallery)
                        ),
                        contentDescription = employee.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👨‍🔧", fontSize = 32.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = employee.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = employee.position,
                    fontSize = 13.sp,
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFFFFC107)
                    )
                    Text(
                        text = employee.experience,
                        fontSize = 11.sp,
                        color = Color(0xFF757575)
                    )
                }
                Text(
                    text = employee.specialty,
                    fontSize = 11.sp,
                    color = Color(0xFF757575),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Подробнее",
                tint = Color(0xFF1565C0),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun PlumbingEmployeeCard(
    employee: PlumbingEmployee,
    context: Context,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
                        )
                    )
            ) {
                if (employee.photoUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = employee.photoUrl,
                            error = painterResource(id = android.R.drawable.ic_menu_gallery)
                        ),
                        contentDescription = employee.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👨‍🔧", fontSize = 32.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    employee.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    employee.position,
                    fontSize = 14.sp,
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFFFC107)
                    )
                    Text(
                        "Стаж: ${employee.experience}",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }
                Text(
                    "Специализация: ${employee.specialty}",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Подробнее",
                tint = Color(0xFF1565C0),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}