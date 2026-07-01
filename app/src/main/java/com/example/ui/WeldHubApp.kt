package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.database.BookingEntity
import com.example.data.database.DesignEntity
import com.example.data.database.MessageEntity
import com.example.data.database.UserEntity
import com.example.ui.theme.*
import com.example.utils.WeldCalculators
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeldHubApp(
    viewModel: WeldHubViewModel = viewModel()
) {
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsState()
    
    // Manage Bottom Navigation selection locally
    var currentBottomTab by remember { mutableStateOf("home") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Logo",
                            tint = OrangeAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WeldHub",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Platform",
                            tint = SteelBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                actions = {
                    var showRoleMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        TextButton(
                            onClick = { showRoleMenu = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = SteelBlue)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Switch Role")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(currentUser?.role ?: "Guest", fontWeight = FontWeight.SemiBold)
                        }
                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Customer Rajesh") },
                                onClick = {
                                    viewModel.loginAsCustomer()
                                    showRoleMenu = false
                                    navController.navigate("customer_main") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Welder Ramesh") },
                                onClick = {
                                    viewModel.loginAsWelder()
                                    showRoleMenu = false
                                    navController.navigate("welder_dashboard") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("WeldHub Admin") },
                                onClick = {
                                    viewModel.loginAsAdmin()
                                    showRoleMenu = false
                                    navController.navigate("admin_panel") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.border(0.5.dp, SlateBorder)
            )
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            val shouldShowBottomBar = currentRoute == "customer_main" || currentRoute?.startsWith("customer_") == true
            
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(1.dp, SlateBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    NavigationBarItem(
                        selected = currentBottomTab == "home",
                        onClick = { currentBottomTab = "home" },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SteelBlue,
                            selectedTextColor = SteelBlue,
                            unselectedIconColor = IndustrialGray,
                            unselectedTextColor = IndustrialGray,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = currentBottomTab == "gallery",
                        onClick = { currentBottomTab = "gallery" },
                        icon = { Icon(Icons.Default.List, contentDescription = "Designs") },
                        label = { Text("Designs", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SteelBlue,
                            selectedTextColor = SteelBlue,
                            unselectedIconColor = IndustrialGray,
                            unselectedTextColor = IndustrialGray,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = currentBottomTab == "estimator",
                        onClick = { currentBottomTab = "estimator" },
                        icon = { Icon(Icons.Default.Build, contentDescription = "AI Estimator") },
                        label = { Text("AI Estimate", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SteelBlue,
                            selectedTextColor = SteelBlue,
                            unselectedIconColor = IndustrialGray,
                            unselectedTextColor = IndustrialGray,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = currentBottomTab == "welders",
                        onClick = { currentBottomTab = "welders" },
                        icon = { Icon(Icons.Default.LocationOn, contentDescription = "Welders") },
                        label = { Text("Welders", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SteelBlue,
                            selectedTextColor = SteelBlue,
                            unselectedIconColor = IndustrialGray,
                            unselectedTextColor = IndustrialGray,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = currentBottomTab == "projects",
                        onClick = { currentBottomTab = "projects" },
                        icon = { Icon(Icons.Default.Done, contentDescription = "Projects") },
                        label = { Text("Projects", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SteelBlue,
                            selectedTextColor = SteelBlue,
                            unselectedIconColor = IndustrialGray,
                            unselectedTextColor = IndustrialGray,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "customer_main",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("customer_main") {
                when (currentBottomTab) {
                    "home" -> CustomerHomeScreen(viewModel, navController)
                    "gallery" -> DesignGalleryScreen(viewModel, navController)
                    "estimator" -> AIEstimatorScreen(viewModel, navController)
                    "welders" -> NearbyWeldersScreen(viewModel, navController)
                    "projects" -> CustomerProjectsScreen(viewModel, navController)
                }
            }

            composable(
                route = "design_detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                DesignDetailScreen(id, viewModel, navController)
            }

            composable(
                route = "welder_profile/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                WelderProfileScreen(id, viewModel, navController)
            }

            composable(
                route = "chat_screen/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("userId") ?: 0
                ChatScreen(id, viewModel, navController)
            }

            composable(
                route = "booking_create/{designId}/{welderId}",
                arguments = listOf(
                    navArgument("designId") { type = NavType.IntType },
                    navArgument("welderId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val designId = backStackEntry.arguments?.getInt("designId") ?: 0
                val welderId = backStackEntry.arguments?.getInt("welderId") ?: 0
                BookingCreateScreen(designId, welderId, viewModel, navController)
            }

            composable("welder_dashboard") {
                WelderDashboardScreen(viewModel, navController)
            }

            composable("admin_panel") {
                AdminPanelScreen(viewModel, navController)
            }
        }
    }
}

// ==========================================
// 1. CUSTOMER HOME SCREEN
// ==========================================
@Composable
fun CustomerHomeScreen(
    viewModel: WeldHubViewModel,
    navController: NavHostController
) {
    val designs by viewModel.designsList.collectAsState()
    val welders by viewModel.weldersList.collectAsState()
    var searchPrompt by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("All") }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Location Selector & Avatar (Matches Professional Polish Header)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .border(0.5.dp, SlateBorder, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CURRENT LOCATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = IndustrialGray,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sector 18, Noida, UP",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Change Location",
                            tint = SteelBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // User Avatar "SK"
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, SlateBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Search Bar (Matches rounded-2xl Slate Style)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchPrompt,
                    onValueChange = { searchPrompt = it },
                    placeholder = { Text("Search gates, staircases, grills...", color = IndustrialGray, fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = SteelBlue,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = IndustrialGray) },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (searchPrompt.isNotEmpty()) {
                                    viewModel.searchDesignByAI(searchPrompt)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Build, contentDescription = "AI Search", tint = OrangeAccent)
                        }
                    }
                )
                
                if (searchPrompt.isNotEmpty()) {
                    Text(
                        text = "✨ Press the orange gear icon to search with WeldHub Design AI!",
                        fontSize = 11.sp,
                        color = OrangeAccent,
                        modifier = Modifier.padding(top = 4.dp, start = 8.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Premium Hero Banner Card (Polished & Balanced spacing)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SteelBlueDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = OrangeAccent.copy(alpha = 0.12f),
                            radius = size.maxDimension / 1.5f,
                            center = center
                        )
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "BUILD YOUR LEGACY IN STEEL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OrangeAccent,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "India's Elite Heavy Steel & Gate Marketplace",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 26.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Hire verified structural welders, book site fabricators, and get instant AI-powered cost estimations.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Popular Categories Section (Matches HTML selection & unselected borders)
        item {
            val categories = listOf("All", "Main Gate", "SS Gate", "Sliding Gate", "Laser Cut Gate", "Balcony", "Staircase", "Grill")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Popular Categories",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "View All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SteelBlue,
                        modifier = Modifier.clickable {
                            // Switch or highlight gallery
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCat == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) SteelBlue else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isSelected) SteelBlue else SlateBorder, RoundedCornerShape(20.dp))
                                .clickable { selectedCat = cat }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Monsoon Rust Protection Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, SteelBlueLight.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SteelBlueDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MONSOON RUST PROTECTION",
                            color = OrangeAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Get 15% off on Epoxy Base Primer coatings for all mild steel gate fabrications booked this week.",
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { /* Apply offer */ },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Claim", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Trending Gate Designs (Matches Design Card Horizontal layout with CAD blueprints)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trending Gate Designs",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "View All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SteelBlue,
                        modifier = Modifier.clickable {
                            // Highlight or navigate
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    designs.take(5).forEach { design ->
                        DesignCardHorizontal(design = design) {
                            navController.navigate("design_detail/${design.id}")
                        }
                    }
                }
            }
        }

        // Elite Welders Nearby (Matches Verified Experts Section)
        item {
            Text(
                text = "Elite Welders Nearby",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        items(welders.take(3)) { welder ->
            WelderItemRow(welder = welder, onChatClick = {
                navController.navigate("chat_screen/${welder.id}")
            }) {
                navController.navigate("welder_profile/${welder.id}")
            }
        }
    }
}

// ==========================================
// 2. DESIGN GALLERY SCREEN
// ==========================================
@Composable
fun DesignGalleryScreen(
    viewModel: WeldHubViewModel,
    navController: NavHostController
) {
    val designs by viewModel.designsList.collectAsState()
    
    val categories = listOf(
        "All", "Main Gate", "Sliding Gate", "Folding Gate", "SS Gate", 
        "MS Gate", "Balcony", "Staircase", "Grill", "Window Grill", 
        "Railing", "CNC Gate", "Laser Cut Gate", "Industrial Shed"
    )
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredDesigns = remember(designs, selectedCategory) {
        if (selectedCategory == "All") designs else designs.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SteelBlue,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }

        if (filteredDesigns.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = "Empty", tint = IndustrialGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No designs found in this category.", color = IndustrialGray)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredDesigns) { design ->
                    DesignCardVertical(design = design, onFavToggle = {
                        viewModel.toggleFavorite(design.id)
                    }) {
                        navController.navigate("design_detail/${design.id}")
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. DESIGN DETAIL SCREEN (WITH PORTFOLIO / DETAILS)
// ==========================================
@Composable
fun DesignDetailScreen(
    id: Int,
    viewModel: WeldHubViewModel,
    navController: NavHostController
) {
    val designs by viewModel.designsList.collectAsState()
    val design = designs.find { it.id == id }
    val welders by viewModel.weldersList.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    if (design == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Design not found.")
        }
        return
    }

    var scale by remember { mutableStateOf(1f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(design.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { viewModel.toggleFavorite(design.id) }) {
                Icon(
                    imageVector = if (favorites.contains(design.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (favorites.contains(design.id)) OrangeAccent else IndustrialGray
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(CharcoalDark),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Design Image Placeholder",
                    tint = SteelBlue,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(design.title, color = Color.White, fontWeight = FontWeight.Bold)
                Text("[ Interactive Zoom Mode Enabled ]", color = OrangeAccent, fontSize = 11.sp)
            }

            Slider(
                value = scale,
                onValueChange = { scale = it },
                valueRange = 1f..3f,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp, vertical = 8.dp),
                colors = SliderDefaults.colors(
                    activeTrackColor = OrangeAccent,
                    thumbColor = OrangeAccent
                )
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Estimated Cost: ₹${String.format("%,.0f", design.estimatedCost)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OrangeAccent
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (design.difficultyLevel) {
                            "Easy" -> Color(0xFF2E7D32)
                            "Medium" -> Color(0xFFE65F2B)
                            else -> Color(0xFFC62828)
                        }
                    )
                ) {
                    Text(
                        text = "${design.difficultyLevel} Level",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Structural Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpecChip(title = "Material Used", value = design.materialUsed, modifier = Modifier.weight(1f))
                SpecChip(title = "Pipe Framing", value = design.pipeSize, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpecChip(title = "Sheet Thickness", value = design.sheetThickness, modifier = Modifier.weight(1f))
                SpecChip(title = "Est. Time", value = "${design.estimatedTimeDays} Days", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Finishing Color Options", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(design.colorOptions, fontSize = 13.sp, color = IndustrialGray)

            Spacer(modifier = Modifier.height(24.dp))

            Text("Recommended Fabricators", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            welders.take(2).forEach { welder ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { navController.navigate("welder_profile/${welder.id}") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = welder.name,
                            tint = SteelBlue,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(welder.name, fontWeight = FontWeight.Bold)
                            Text("Exp: ${welder.experienceYears} Years • Rating: ${welder.rating}★", fontSize = 12.sp, color = IndustrialGray)
                        }
                        Button(
                            onClick = { navController.navigate("booking_create/${design.id}/${welder.id}") },
                            colors = ButtonDefaults.buttonColors(containerColor = SteelBlue),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Book Now", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. AI ESTIMATOR SCREEN (GEMINI / LOCAL)
// ==========================================
@Composable
fun AIEstimatorScreen(
    viewModel: WeldHubViewModel,
    navController: NavHostController
) {
    var heightInput by remember { mutableStateOf("10") }
    var widthInput by remember { mutableStateOf("6") }
    var selectedMaterial by remember { mutableStateOf("Mild Steel (MS)") }
    var selectedType by remember { mutableStateOf("Main Gate") }
    var notesInput by remember { mutableStateOf("") }

    val isEstimating by viewModel.isEstimating.collectAsState()
    val aiEstimate by viewModel.aiEstimateState.collectAsState()

    var localResult by remember { mutableStateOf<WeldCalculators.CostEstimationResult?>(null) }

    val materials = listOf("Mild Steel (MS)", "Stainless Steel (SS 304)", "Wrought Iron")
    val types = listOf("Main Gate", "Sliding Gate", "SS Gate", "Window Grill", "Balcony Railing")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SteelBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, contentDescription = "AI", tint = OrangeAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WeldHub AI Estimator", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Enter the dimensions of your steel gate or railing, and WeldHub AI will calculate structural weight, pipe specifications, labor schedules, and cost estimates.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = heightInput,
                    onValueChange = { heightInput = it },
                    label = { Text("Height (ft)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = widthInput,
                    onValueChange = { widthInput = it },
                    label = { Text("Width (ft)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            Text("Select Steel Material Type", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                materials.forEach { mat ->
                    FilterChip(
                        selected = selectedMaterial == mat,
                        onClick = { selectedMaterial = mat },
                        label = { Text(mat) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            Text("Select Design Category", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                types.forEach { t ->
                    FilterChip(
                        selected = selectedType == t,
                        onClick = { selectedType = t },
                        label = { Text(t) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("Custom specifications / site issues") },
                placeholder = { Text("E.g., require heavy 14 gauge tubes, mitered corners...") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val h = heightInput.toDoubleOrNull() ?: 10.0
                        val w = widthInput.toDoubleOrNull() ?: 6.0
                        localResult = WeldCalculators.calculateCost(h, w, selectedMaterial, selectedType)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Standard Cost")
                }

                Button(
                    onClick = {
                        val h = heightInput.toDoubleOrNull() ?: 10.0
                        val w = widthInput.toDoubleOrNull() ?: 6.0
                        viewModel.getAICostEstimate(h, w, selectedMaterial, selectedType, notesInput)
                    },
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isEstimating
                ) {
                    if (isEstimating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text("WeldHub AI ✨")
                    }
                }
            }
        }

        if (isEstimating) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(color = OrangeAccent, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("WeldHub AI is formulating bill of materials & market rates...", fontSize = 12.sp, color = OrangeAccent, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        aiEstimate?.let { estimate ->
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalMedium),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = "Done", tint = OrangeAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("LIVE AI MARKET QUOTATION", fontWeight = FontWeight.Bold, color = OrangeAccent)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        estimate.split("\n").forEach { line ->
                            when {
                                line.startsWith("###") -> {
                                    Text(
                                        text = line.replace("###", "").trim(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = SteelBlueLight,
                                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                    )
                                }
                                line.startsWith("-") || line.startsWith("*") -> {
                                    Row(modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp)) {
                                        Text("• ", color = OrangeAccent, fontWeight = FontWeight.Bold)
                                        Text(line.substring(1).trim(), color = Color.White, fontSize = 13.sp)
                                    }
                                }
                                else -> {
                                    if (line.isNotEmpty()) {
                                        Text(line, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        localResult?.let { result ->
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Standard Steel Formula Report", fontWeight = FontWeight.Bold, color = SteelBlue, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LocalResultRow(label = "Steel Quantity Required", value = "${String.format("%.1f", result.steelQuantityKg)} kg")
                        LocalResultRow(label = "Estimated Frame Pipe Length", value = "${String.format("%.1f", result.totalPipeLengthFt)} ft")
                        LocalResultRow(label = "Total Raw Weight", value = "${String.format("%.1f", result.weightKg)} kg")
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        LocalResultRow(label = "Estimated Fabrication Labor", value = "₹${String.format("%,.0f", result.laborCostRs)}")
                        LocalResultRow(label = "Rust Priming & Paint", value = "₹${String.format("%,.0f", result.paintCostRs)}")
                        LocalResultRow(label = "Standard Transport & Setup", value = "₹${String.format("%,.0f", result.transportCostRs)}")
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Standard Budget", fontWeight = FontWeight.Bold, color = OrangeAccent)
                            Text("₹${String.format("%,.0f", result.totalEstimatedCostRs)}", fontWeight = FontWeight.Bold, color = OrangeAccent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocalResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = CharcoalLight)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CharcoalDark)
    }
}

// ==========================================
// 5. NEARBY WELDERS / MAP SCREEN
// ==========================================
@Composable
fun NearbyWeldersScreen(
    viewModel: WeldHubViewModel,
    navController: NavHostController
) {
    val welders by viewModel.weldersList.collectAsState()
    val fabricators by viewModel.fabricatorsList.collectAsState()
    val contractors by viewModel.contractorsList.collectAsState()

    var activeRoleFilter by remember { mutableStateOf("All") }

    val allWeldersList = remember(welders, fabricators, contractors, activeRoleFilter) {
        when (activeRoleFilter) {
            "Welders" -> welders
            "Fabricators" -> fabricators
            "Contractors" -> contractors
            else -> welders + fabricators + contractors
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(SteelBlueDark)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = Color.White.copy(alpha = 0.08f), radius = size.height * 0.8f, center = center)
                drawCircle(color = Color.White.copy(alpha = 0.05f), radius = size.height * 0.5f, center = center)
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text("LIVE GEOLOCATION RADAR", color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Displaying active welders & structural shops within 15 km of your location.", color = Color.White, fontSize = 13.sp)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Welders", "Fabricators", "Contractors").forEach { f ->
                FilterChip(
                    selected = activeRoleFilter == f,
                    onClick = { activeRoleFilter = f },
                    label = { Text(f) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(allWeldersList) { welder ->
                WelderItemRow(welder = welder, onChatClick = {
                    navController.navigate("chat_screen/${welder.id}")
                }) {
                    navController.navigate("welder_profile/${welder.id}")
                }
            }
        }
    }
}

// ==========================================
// 6. WELDER PROFILE & PORTFOLIO SCREEN
// ==========================================
@Composable
fun WelderProfileScreen(
    id: Int,
    viewModel: WeldHubViewModel,
    navController: NavHostController
) {
    val welders by viewModel.weldersList.collectAsState()
    val fabs by viewModel.fabricatorsList.collectAsState()
    val conts by viewModel.contractorsList.collectAsState()
    val allWelders = welders + fabs + conts
    
    val welder = allWelders.find { it.id == id }
    val portfolio by viewModel.getWelderPortfolio(id).collectAsState(initial = emptyList())
    val reviews by viewModel.getWelderReviews(id).collectAsState(initial = emptyList())

    if (welder == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Welder profile not found.")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(SteelBlue, SteelBlueDark)
                    )
                )
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-40).dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SteelBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = welder.name.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(welder.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            if (welder.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = OrangeAccent, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text("${welder.role} • ${welder.experienceYears} Years Exp", fontSize = 13.sp, color = IndustrialGray)
                        Text(welder.locationName, fontSize = 11.sp, color = IndustrialGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (welder.availableToday) {
                        BadgeLabel(text = "Available Today", color = Color(0xFF2E7D32))
                    } else {
                        BadgeLabel(text = "Busy/Booked", color = Color(0xFFC62828))
                    }
                    if (welder.emergencyService) {
                        BadgeLabel(text = "Emergency Welding", color = OrangeAccent)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("About", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(welder.about, fontSize = 13.sp, color = CharcoalLight, lineHeight = 18.sp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-30).dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { /* Simulated Call */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SteelBlue)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call Now")
                }
                Button(
                    onClick = { navController.navigate("chat_screen/${welder.id}") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Chat")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Weld Chat")
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-10).dp)
        ) {
            Text("Work Portfolio (${portfolio.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (portfolio.isEmpty()) {
                Text("No portfolio items uploaded yet.", fontSize = 12.sp, color = IndustrialGray)
            } else {
                portfolio.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Material: ${item.materialUsed}", fontSize = 12.sp, color = CharcoalLight)
                            Text("Location: ${item.location}", fontSize = 11.sp, color = IndustrialGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PortfolioProgressBox(label = "BEFORE", modifier = Modifier.weight(1f))
                                PortfolioProgressBox(label = "FABRICATION", modifier = Modifier.weight(1f))
                                PortfolioProgressBox(label = "COMPLETED", modifier = Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "\"${item.customerReview}\"",
                                fontSize = 12.sp,
                                color = SteelBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Customer Reviews", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (reviews.isEmpty()) {
                Text("No reviews written yet. Be the first to review!", fontSize = 12.sp, color = IndustrialGray)
            } else {
                reviews.forEach { r ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(r.customerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Row {
                                    repeat(r.rating) {
                                        Icon(Icons.Default.Star, contentDescription = "*", tint = OrangeAccent, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(r.reviewText, fontSize = 12.sp, color = CharcoalLight)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PortfolioProgressBox(label: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .height(60.dp)
            .background(CharcoalMedium, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Build, contentDescription = "Sparks", tint = OrangeAccent.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 7. BOOKING CREATE SCREEN
// ==========================================
@Composable
fun BookingCreateScreen(
    designId: Int,
    welderId: Int,
    viewModel: WeldHubViewModel,
    navController: NavHostController
) {
    val designs by viewModel.designsList.collectAsState()
    val welders by viewModel.weldersList.collectAsState()
    val fabs by viewModel.fabricatorsList.collectAsState()
    val welder = (welders + fabs).find { it.id == welderId }
    val design = designs.find { it.id == designId }

    var addressInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var scheduleDate by remember { mutableStateOf("") }

    if (welder == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Invalid booking parameters.")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Formal Fabrication Booking", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        design?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.Build, contentDescription = "Design", tint = SteelBlue, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(it.title, fontWeight = FontWeight.Bold)
                        Text("Category: ${it.category}", fontSize = 12.sp, color = IndustrialGray)
                        Text("Steel Cost: ₹${String.format("%,.0f", it.estimatedCost)}", fontSize = 13.sp, color = OrangeAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp)) {
                Icon(Icons.Default.Person, contentDescription = "Welder", tint = OrangeAccent, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Fabricator: ${welder.name}", fontWeight = FontWeight.Bold)
                    Text("Rating: ${welder.rating}★ (${welder.ratingCount} reviews)", fontSize = 12.sp, color = IndustrialGray)
                    Text("Loc: ${welder.locationName}", fontSize = 11.sp, color = IndustrialGray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = scheduleDate,
            onValueChange = { scheduleDate = it },
            label = { Text("Desired Commencement Date (e.g. 10th July)") },
            placeholder = { Text("DD/MM/YYYY") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = addressInput,
            onValueChange = { addressInput = it },
            label = { Text("Site Installation Address") },
            placeholder = { Text("Enter full address across India...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = notesInput,
            onValueChange = { notesInput = it },
            label = { Text("Notes / Special Instructions") },
            placeholder = { Text("E.g., require custom height, locks, wheels...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        val totalCost = design?.estimatedCost ?: 45000.0
        val advanceRequired = totalCost * 0.20

        Card(
            colors = CardDefaults.cardColors(containerColor = SteelBlueDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Escrow Advance Protection", color = OrangeAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Fabrication Estimate", color = Color.White, fontSize = 13.sp)
                    Text("₹${String.format("%,.0f", totalCost)}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("20% Escrow Advance Required", color = Color.White, fontSize = 13.sp)
                    Text("₹${String.format("%,.0f", advanceRequired)}", color = OrangeAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Your advance payment will be held securely in WeldHub Escrow and only released to the welder as milestones are completed.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.bookProject(
                    welderId = welder.id,
                    welderName = welder.name,
                    designId = design?.id,
                    designTitle = design?.title,
                    date = scheduleDate.ifEmpty { "10/07/2026" },
                    siteAddress = addressInput.ifEmpty { "Gachibowli Main Rd, Hyderabad" },
                    notes = notesInput,
                    totalCost = totalCost,
                    advancePaid = advanceRequired
                )
                navController.navigate("customer_main") {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Pay Advance & Create Project", fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 8. CHAT SCREEN
// ==========================================
@Composable
fun ChatScreen(
    userId: Int,
    viewModel: WeldHubViewModel,
    navController: NavHostController
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val welders by viewModel.weldersList.collectAsState()
    val fabs by viewModel.fabricatorsList.collectAsState()
    val otherUser = (welders + fabs + listOf(
        UserEntity(id = 999, name = "Rajesh Patel", phone = "", role = "Customer")
    )).find { it.id == userId }

    val messages by viewModel.chatMessages.collectAsState()
    var textInput by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        viewModel.loadConversation(userId)
    }

    if (otherUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Conversation not found.")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(otherUser.name, fontWeight = FontWeight.Bold)
                Text(otherUser.role, fontSize = 11.sp, color = IndustrialGray)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                val isMyMessage = msg.senderId == currentUser?.id
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isMyMessage) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMyMessage) SteelBlue else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (isMyMessage) 12.dp else 0.dp,
                            bottomEnd = if (isMyMessage) 0.dp else 12.dp
                        ),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.content,
                                color = if (isMyMessage) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        val isWelderUser = currentUser?.role == "Welder" || currentUser?.role == "Fabricator"
        if (isWelderUser) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SteelBlueDark)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("QUICK WORK QUOTATION", color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                Button(
                    onClick = {
                        viewModel.sendChatMessage(
                            receiverId = userId,
                            text = "✨ STEEL FABRICATION QUOTE: SS 304 High-Gloss Gate (10x6 ft). Total Cost: ₹1,25,000. Advance Required: ₹25,000. Schedule: 10 Days.",
                            type = "quotation"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Send Quote", fontSize = 11.sp)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Write message...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input"),
                shape = RoundedCornerShape(20.dp),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (textInput.isNotEmpty()) {
                        viewModel.sendChatMessage(userId, textInput)
                        textInput = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = OrangeAccent)
            }
        }
    }
}

// ==========================================
// 9. CUSTOMER PROJECTS / TRACKING SCREEN
// ==========================================
@Composable
fun CustomerProjectsScreen(
    viewModel: WeldHubViewModel,
    navController: NavHostController
) {
    val bookings by viewModel.customerBookings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("Your Fabrication Projects", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (bookings.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = "Empty", tint = IndustrialGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No active bookings found.", color = IndustrialGray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(bookings) { booking ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(booking.designTitle ?: "Custom Fabrication Work", fontWeight = FontWeight.Bold)
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = when (booking.status) {
                                            "Completed" -> Color(0xFF2E7D32)
                                            "In Progress" -> Color(0xFF326284)
                                            else -> Color(0xFFE65F2B)
                                        }
                                    )
                                ) {
                                    Text(
                                        text = booking.status,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Welder: ${booking.welderName}", fontSize = 13.sp, color = CharcoalLight)
                            Text("Site: ${booking.siteAddress}", fontSize = 12.sp, color = IndustrialGray)

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Progress:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                LinearProgressIndicator(
                                    progress = booking.progressPercentage / 100f,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = OrangeAccent
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${booking.progressPercentage}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Escrow Paid", fontSize = 11.sp, color = IndustrialGray)
                                    Text("₹${String.format("%,.0f", booking.advancePaid)}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { /* Simulated Invoice */ },
                                    colors = ButtonDefaults.buttonColors(containerColor = SteelBlue)
                                ) {
                                    Icon(Icons.Default.List, contentDescription = "Invoice", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("GST Invoice", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 10. WELDER DASHBOARD SCREEN
// ==========================================
@Composable
fun WelderDashboardScreen(
    viewModel: WeldHubViewModel,
    navController: NavHostController
) {
    val bookings by viewModel.welderBookings.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        item {
            Text("Welder Workstation", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Swamy Steel Fabrication Works, Okhla", fontSize = 13.sp, color = IndustrialGray)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(title = "Total Jobs", value = "${bookings.size}", color = SteelBlue, modifier = Modifier.weight(1f))
                StatCard(title = "Escrow Earnings", value = "₹1,45,000", color = OrangeAccent, modifier = Modifier.weight(1.2f))
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Text("Active Project Orders", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (bookings.isEmpty()) {
            item {
                Text("No active customer fabrications currently assigned.", fontSize = 12.sp, color = IndustrialGray)
            }
        } else {
            items(bookings) { job ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(job.designTitle ?: "Custom Fabrication Work", fontWeight = FontWeight.Bold)
                            Text("₹${String.format("%,.0f", job.totalCost)}", color = OrangeAccent, fontWeight = FontWeight.Bold)
                        }
                        Text("Customer: ${job.customerName}", fontSize = 12.sp, color = CharcoalLight)
                        Text("Address: ${job.siteAddress}", fontSize = 11.sp, color = IndustrialGray)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Progress: ${job.progressPercentage}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Slider(
                                value = job.progressPercentage.toFloat(),
                                onValueChange = {
                                    viewModel.updateProjectProgress(
                                        bookingId = job.id,
                                        status = if (it >= 100f) "Completed" else "In Progress",
                                        progress = it.toInt()
                                    )
                                },
                                valueRange = 0f..100f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    activeTrackColor = OrangeAccent,
                                    thumbColor = OrangeAccent
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { navController.navigate("chat_screen/${job.customerId}") }) {
                                Icon(Icons.Default.Send, contentDescription = "Chat", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Contact Client")
                            }

                            Button(
                                onClick = {
                                    viewModel.updateProjectProgress(
                                        bookingId = job.id,
                                        status = "Completed",
                                        progress = 100
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(6.dp),
                                enabled = job.progressPercentage < 100
                            ) {
                                Text("Complete Job", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 11. ADMIN PANEL SCREEN
// ==========================================
@Composable
fun AdminPanelScreen(
    viewModel: WeldHubViewModel,
    navController: NavHostController
) {
    val designs by viewModel.designsList.collectAsState()
    val welders by viewModel.weldersList.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        item {
            Text("WeldHub Control Room", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("National Portal Management Dashboard", fontSize = 12.sp, color = IndustrialGray)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(title = "Total Designs", value = "${designs.size}", color = SteelBlue, modifier = Modifier.weight(1f))
                StatCard(title = "Welders Registered", value = "${welders.size}", color = OrangeAccent, modifier = Modifier.weight(1f))
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Text("Pending Welder Approvals", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(welders.filter { !it.isVerified }) { welder ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(welder.name, fontWeight = FontWeight.Bold)
                        Text("ITI Certificate Approved • ${welder.experienceYears} Yrs Exp", fontSize = 11.sp, color = IndustrialGray)
                    }
                    Button(
                        onClick = {
                            viewModel.verifyWelder(welder)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Verify Welder", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// SHARED CUSTOM COMPOSABLES
// ==========================================
@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun GateWireframeMockup(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(CharcoalDark)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            // Draw outer gate frame
            drawRect(
                color = SteelBlue.copy(alpha = 0.4f),
                topLeft = androidx.compose.ui.geometry.Offset(width * 0.12f, height * 0.15f),
                size = androidx.compose.ui.geometry.Size(width * 0.76f, height * 0.7f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )
            // Draw vertical bars
            val steps = 8
            val startX = width * 0.12f
            val endX = width * 0.88f
            val stepSize = (endX - startX) / steps
            for (i in 1 until steps) {
                val x = startX + i * stepSize
                drawLine(
                    color = SteelBlue.copy(alpha = 0.3f),
                    start = androidx.compose.ui.geometry.Offset(x, height * 0.15f),
                    end = androidx.compose.ui.geometry.Offset(x, height * 0.85f),
                    strokeWidth = 2.dp.toPx()
                )
            }
            // Draw diagonal draft indicators
            drawLine(
                color = OrangeAccent.copy(alpha = 0.4f),
                start = androidx.compose.ui.geometry.Offset(startX, height * 0.15f),
                end = androidx.compose.ui.geometry.Offset(endX, height * 0.85f),
                strokeWidth = 3.dp.toPx()
            )
            drawLine(
                color = OrangeAccent.copy(alpha = 0.4f),
                start = androidx.compose.ui.geometry.Offset(startX, height * 0.85f),
                end = androidx.compose.ui.geometry.Offset(endX, height * 0.15f),
                strokeWidth = 3.dp.toPx()
            )
        }
        
        // Custom schematic draft status indicators at bottom right
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(OrangeAccent)
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
fun SpecChip(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.border(1.dp, SlateBorder, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 11.sp, color = IndustrialGray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun BadgeLabel(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DesignCardHorizontal(design: DesignEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() }
            .border(1.dp, SlateBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            GateWireframeMockup(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = design.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Material: ${design.materialUsed} • ${design.difficultyLevel}",
                    fontSize = 11.sp,
                    color = IndustrialGray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${String.format("%,.0f", design.estimatedCost)}",
                        color = OrangeAccent,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Est. Budget",
                        color = IndustrialGray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DesignCardVertical(
    design: DesignEntity,
    onFavToggle: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, SlateBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                GateWireframeMockup(modifier = Modifier.fillMaxSize())
                IconButton(
                    onClick = { onFavToggle() },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (design.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Save",
                        tint = if (design.isSaved) OrangeAccent else Color.White
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = design.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = design.category,
                    fontSize = 11.sp,
                    color = IndustrialGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${String.format("%,.0f", design.estimatedCost)}",
                        color = OrangeAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Est. Price",
                        color = IndustrialGray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WelderItemRow(
    welder: UserEntity,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onProfileClick() }
            .border(1.dp, SlateBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SteelBlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = welder.name.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = welder.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (welder.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = SteelBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = OrangeAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${welder.rating}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeAccent,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Text(
                        text = " • ${welder.experienceYears} Years Exp • ${welder.locationName}",
                        fontSize = 11.sp,
                        color = IndustrialGray
                    )
                }
            }
            
            Button(
                onClick = { onProfileClick() },
                colors = ButtonDefaults.buttonColors(containerColor = SteelBlue),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Hire", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
