package com.example.focus_planner.ui.screens.onborading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.focus_planner.data.model.OnboardingPage
import com.example.focus_planner.data.model.onboardingPages
import com.example.focus_planner.utils.PreferencesManager
import com.example.focus_planner.utils.SharedPreferencesManager
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    val preferencesManager = PreferencesManager(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            count = onboardingPages.size,
        ) { page ->
            OnboardingPageContent(onboardingPages[page])
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(onboardingPages.size) { index ->
                val color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(8.dp)
                        .background(color, shape = MaterialTheme.shapes.small)
                )
            }
        }

        Button(
            onClick = {
                if (pagerState.currentPage == onboardingPages.size - 1) {
                    preferencesManager.setOnboardingCompleted(context, true)
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                } else {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = if (pagerState.currentPage == onboardingPages.size - 1) "Empezar" else "Siguiente"
            )
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(page.animationRes))
    val progress by animateLottieCompositionAsState(composition)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .height(250.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(page.title, style = MaterialTheme.typography.headlineSmall)
        Text(page.description, style = MaterialTheme.typography.bodyMedium)
    }
}