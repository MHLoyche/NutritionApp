package com.example.nutritionapptwo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutritionapptwo.model.ScannedItem
import com.example.nutritionapptwo.ui.theme.NutritionAppTwoTheme
import com.example.nutritionapptwo.viewmodel.MealViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutritionAppTwoTheme {
                val navController = rememberNavController() // Navigation controller
                val mealViewModel: MealViewModel = viewModel() // Viewmodel to hold meals + nutrients

                // Navigation host to manage different screens and deciding start screen
                NavHost(
                    navController = navController,
                    startDestination = "main",
                ) {
                    composable("main") {
                        MainScreen(
                            onMealClick = { mealName ->
                                navController.navigate("meal/$mealName") // Navigate to specific meal detail screen
                            },
                            viewModel = mealViewModel
                        )
                    } // composable for meal/{mealName} screen
                    composable("meal/{mealName}") { backStackEntry ->
                        val mealName = backStackEntry.arguments?.getString("mealName") ?: ""
                        MealDetailScreen(
                            mealName = mealName,
                            onBack = { navController.popBackStack() },
                            viewModel = mealViewModel
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun MainScreen(
    onMealClick: (String) -> Unit,
    viewModel: MealViewModel
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    // Collect totals directly from ViewModel's StateFlow so UI updates when items are added
    val total by viewModel.totalForSelectedDate.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        WavyBackground()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                TitleCard("Satura Nutrition")

                NutrientBanner(
                    kcal = total.kcal,
                    kcalGoal = 2000,
                    protein = total.protein,
                    proteinGoal = 140,
                    carbs = total.carbs,
                    carbsGoal = 100,
                    fat = total.fat,
                    fatGoal = 80
                )

                DateSelection(
                    selectedDate = selectedDate,
                    onDateSelected = { date -> viewModel.setSelectedDate(date) }
                )

                MealBox(mealName = "Breakfast", onClick = { onMealClick("Breakfast") })
                MealBox(mealName = "Lunch", onClick = { onMealClick("Lunch") })
                MealBox(mealName = "Dinner", onClick = { onMealClick("Dinner") })
                MealBox(mealName = "Snacks", onClick = { onMealClick("Snacks") })
                MealBox(mealName = "Drinks", onClick = { onMealClick("Drinks") })
            }
        }
    }
}

@Composable
fun TitleCard(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 14.dp)
    )
}

@Composable
fun DateSelection(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = remember { java.util.Calendar.getInstance() }
    val dateFormat = remember {
        java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
    }

    fun shiftDate(days: Int) {
        val current = try {
            dateFormat.parse(selectedDate)
        } catch (_: Exception) {
            java.util.Date()
        }
        val cal = java.util.Calendar.getInstance().apply { time = current }
        cal.add(java.util.Calendar.DAY_OF_YEAR, days)
        onDateSelected(dateFormat.format(cal.time))
    }

    fun openDatePicker() {
        // Initialize calendar to currently selected date
        try {
            val parsed = dateFormat.parse(selectedDate)
            if (parsed != null) {
                calendar.time = parsed
            }
        } catch (_: Exception) {
        }

        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(dateFormat.format(calendar.time))
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEFDD4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left-aligned back arrow
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .clickable { shiftDate(-1) }
                    .height(40.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "<",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Centered date box
            Box(
                modifier = Modifier
                    .weight(2f)
                    .clickable { openDatePicker() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedDate,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            // Right-aligned forward arrow
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
                    .clickable { shiftDate(1) }
                    .height(40.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = ">",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun NutrientBanner(
    kcal: Int,
    kcalGoal: Int,
    carbs: Int,
    carbsGoal: Int,
    protein: Int,
    proteinGoal: Int,
    fat: Int,
    fatGoal: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE9F7D9)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Today’s Macros",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            NutrientProgressRow(
                label = "Calories",
                value = kcal,
                goal = kcalGoal,
                color = Color(0xFFFF8A65)
            )
            NutrientProgressRow(
                label = "Protein",
                value = protein,
                goal = proteinGoal,
                color = Color(0xFF66BB6A)
            )
            NutrientProgressRow(
                label = "Carbs",
                value = carbs,
                goal = carbsGoal,
                color = Color(0xFFA8D554)
            )
            NutrientProgressRow(
                label = "Fat",
                value = fat,
                goal = fatGoal,
                color = Color(0xDC8070F8)
            )
        }
    }
}

@Composable
fun NutrientProgressRow(
    label: String,
    value: Int,
    goal: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Calculate progress as a float between 0 and 1
    val progress = (value.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label: $value / $goal",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            color = color,
            trackColor = Color.LightGray.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun WavyBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF1F8E9),
                    Color(0xFFFFFDE7)
                ),
                startY = 0f,
                endY = height
            )
        )

        // Wavy shape at the top using a Path and Bézier curve
        val wavePath = Path().apply {
            moveTo(0f, height * 0.20f)
            cubicTo(
                width * 0.25f, height * 0.10f,
                width * 0.75f, height * 0.30f,
                width, height * 0.20f
            )
            lineTo(width, 0f)
            lineTo(0f, 0f)
            close()
        }

        drawPath(
            path = wavePath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFAED581),
                    Color(0xFFFFF59D)
                )
            )
        )
    }
}

@Composable
fun MealBox(
    mealName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF)
        )
    ) {
        Box(
            modifier = Modifier
                .height(70.dp)
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = mealName,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MealDetailScreen(
    mealName: String,
    onBack: () -> Unit,
    viewModel: MealViewModel
) {
    val context = LocalContext.current
    val isInPreview = androidx.compose.ui.platform.LocalInspectionMode.current

    // Observe the items and nutrients via StateFlows so the UI updates immediately on changes
    val itemsByMeal by viewModel.itemsByMeal.collectAsState()
    val mealNutrientsMap by viewModel.mealNutrientsByMeal.collectAsState()

    val scannedItems = itemsByMeal[mealName] ?: emptyList()
    val nutrients = mealNutrientsMap[mealName] ?: com.example.nutritionapptwo.model.MealDetails(0,0,0,0)

    val launcher = if (!isInPreview) {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val barcode = result.data?.getStringExtra("barcode")
                if (!barcode.isNullOrEmpty()) {
                    viewModel.addItemFromBarcode(mealName, barcode)
                }
            }
        }
    } else null

    val selectedDate by viewModel.selectedDate.collectAsState()
    LaunchedEffect(mealName, selectedDate) {
        val scannedItems = viewModel.getItemsForMeal(mealName)
        if (mealName == "Lunch" && scannedItems.isEmpty()) {
            viewModel.addItemToMeal(
                mealName,
                ScannedItem(
                    barcode = "1234567890123",
                    name = "Sample Item",
                    calories = 250,
                    protein = 10,
                    fat = 5,
                    carbohydrates = 30
                )
            )
            viewModel.addItemToMeal(
                mealName,
                ScannedItem(
                    barcode = "9876543210987",
                    name = "Another Item",
                    calories = 150,
                    protein = 5,
                    fat = 2,
                    carbohydrates = 20
                )
            )
        }
        if (mealName == "Dinner" && scannedItems.isEmpty()) {
            viewModel.addItemToMeal(
                mealName,
                ScannedItem(
                    barcode = "123632890123",
                    name = "Sample Item 3",
                    calories = 250,
                    protein = 10,
                    fat = 5,
                    carbohydrates = 30
                )
            )
        }
    }

    Scaffold { padding ->
        WavyBackground()
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleCard("$mealName Items", modifier = Modifier.weight(1f))

                Button(onClick = onBack, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(text = "Back")
                }
            }

            NutrientBanner(
                kcal = nutrients.kcal,
                kcalGoal = 2000,
                carbs = nutrients.carbs,
                carbsGoal = 100,
                protein = nutrients.protein,
                proteinGoal = 140,
                fat = nutrients.fat,
                fatGoal = 80,
                modifier = Modifier.padding(16.dp)
            )

            Button(
                onClick = {
                    if (!isInPreview && launcher != null) {
                        try {
                            val intent = Intent(context, BarcodeScannerActivity::class.java)
                            launcher.launch(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Scanner not available", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isInPreview,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp)
            ) {
                Text(text = "Scan Items")
            }

            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item {
                    Text(
                        text = "Scanned Items ${scannedItems.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(scannedItems) { item ->
                    val displayName = item.name.ifBlank { item.barcode }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "$displayName - ${item.calories} kcal, ${item.protein} g protein, ${item.fat} g fat, ${item.carbohydrates} g carbs",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                item {
                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier.height(16.dp)
                    )
                }
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    NutritionAppTwoTheme {
        // Preview with an empty MealViewModel
        val previewViewModel = MealViewModel()
        MainScreen(onMealClick = { }, viewModel = previewViewModel)
        //MealDetailScreen(mealName = "Lunch", onBack = { }, viewModel = previewViewModel)
    }
}