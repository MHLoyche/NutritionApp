package com.example.nutritionapptwo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
    onMealClick: (String) -> Unit, // Callback when a meal box is clicked
    viewModel: MealViewModel // ViewModel to hold data
) {
    val total = viewModel.getTotalNutrients() // get total nutrients for the day

    Box(modifier = Modifier.fillMaxSize()) {
        WavyBackground()

        // Scaffold to provide basic material design layout structure
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

    val scannedItems = viewModel.getItemsForMeal(mealName)
    val nutrients = viewModel.getNutrientsForMeal(mealName)

    val launcher = if (!isInPreview) {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val barcode = result.data?.getStringExtra("barcode")
                if (!barcode.isNullOrEmpty()) {
                    viewModel.addItemToMeal(
                        mealName,
                        ScannedItem(barcode = barcode)
                    )
                }
            }
        }
    } else null

    LaunchedEffect(mealName) {
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
                protein = nutrients.protein,
                proteinGoal = 140,
                carbs = nutrients.carbs,
                carbsGoal = 100,
                fat = nutrients.fat,
                fatGoal = 80,
                modifier = Modifier.padding(16.dp)
            )

            Button(
                onClick = {
                    val intent = Intent(context, BarcodeScannerActivity::class.java)
                    launcher?.launch(intent)
                },
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = item.name.takeIf { it.isNotBlank() } ?: item.barcode,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            androidx.compose.foundation.layout.Spacer(
                                modifier = Modifier.height(6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Calories: ${item.calories} kcal",
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Protein: ${item.protein} g",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Fat: ${item.fat} g",
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Carbs: ${item.carbohydrates} g",
                                    modifier = Modifier.weight(1f)
                                )
                            }
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
        MealDetailScreen(mealName = "Lunch", onBack = { }, viewModel = previewViewModel)
    }
}