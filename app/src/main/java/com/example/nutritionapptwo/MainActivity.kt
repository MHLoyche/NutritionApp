package com.example.nutritionapptwo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutritionapptwo.ui.theme.NutritionAppTwoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutritionAppTwoTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "main",
                ) {
                    composable("main") {
                        MainScreen { mealName: String ->
                            navController.navigate("meal/$mealName")
                        }
                    }
                    composable("meal/{mealName}") { backStackEntry ->
                        val mealName = backStackEntry.arguments?.getString("mealName") ?: ""
                        MealDetailScreen(mealName = mealName, onBack = { navController.popBackStack()
                        } )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    onMealClick: (String) -> Unit
) {
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
                    kcal = 0,
                    kcalGoal = 2000,
                    protein = 0,
                    proteinGoal = 140,
                    carbs = 0,
                    carbsGoal = 100,
                    fat = 0,
                    fatGoal = 80
                )

                // use named arguments so trailing lambda maps to onClick, not modifier
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
fun MealDetailScreen(mealName: String, onBack: () -> Unit) {

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val barcode = result.data?.getStringExtra("barcode")
            Log.d("TAG", "Scanned barcode: $barcode")
        }
    }

    Scaffold { padding ->
        WavyBackground()
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TitleCard("$mealName Items", modifier = Modifier.weight(1f))

                Button(onClick = onBack, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(text = "Back")
                }
            }

            NutrientBanner(
                kcal = 1337,
                kcalGoal = 2000,
                protein = 60,
                proteinGoal = 140,
                carbs = 80,
                carbsGoal = 100,
                fat = 40,
                fatGoal = 80,
                modifier = Modifier.padding(16.dp)
            )

            Button(
                onClick = {
                    val intent = Intent(context, BarcodeScannerActivity::class.java)
                    launcher.launch(intent)
                },
                modifier = Modifier.align(Alignment.End).padding(16.dp)
            ) {
                Text(text = "Scan Items")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    NutritionAppTwoTheme {
        //MainScreen {  }
        MealDetailScreen(mealName = "Lunch", onBack = { })
    }
}