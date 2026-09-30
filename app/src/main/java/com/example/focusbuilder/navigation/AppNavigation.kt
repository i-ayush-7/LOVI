package com.example.focusbuilder.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.focusbuilder.ui.GameCompleteScreen
import com.example.focusbuilder.ui.GameIntroScreen
import com.example.focusbuilder.ui.HomeScreen
import com.example.focusbuilder.ui.ParentDashboardScreen
import com.example.focusbuilder.ui.WaitForItScreen
import com.example.focusbuilder.ui.WelcomeScreen

import androidx.compose.ui.platform.LocalContext

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val startDest = androidx.compose.runtime.remember {
        val prefs = context.getSharedPreferences("LoviPrefs", android.content.Context.MODE_PRIVATE)
        if (prefs.getString("child_name", null).isNullOrBlank()) "welcome" else "home"
    }
    val navController = rememberNavController()
    val isProUnlocked by com.example.focusbuilder.billing.RevenueCatManager.isLoviProUnlocked.collectAsState()

    NavHost(navController = navController, startDestination = startDest) {
        composable("welcome") {
            WelcomeScreen(onGetStarted = {
                navController.navigate("home") {
                    popUpTo("welcome") { inclusive = true }
                }
            })
        }
        composable("home") {
            HomeScreen(
                onNavigateToWaitForIt = {
                    navController.navigate("game_intro")
                },
                onNavigateToSequenceRepeat = {
                    navController.navigate("game_intro_sequence")
                },
                onNavigateToWhatsMissing = {
                    navController.navigate("game_intro_whats_missing")
                },
                onNavigateToRuleSwitchSort = {
                    if (isProUnlocked) navController.navigate("game_intro_rule_switch") else navController.navigate("paywall")
                },
                onNavigateToFindSignal = {
                    if (isProUnlocked) navController.navigate("game_intro_find_signal") else navController.navigate("paywall")
                },
                onNavigateToWatchFirefly = {
                    if (isProUnlocked) navController.navigate("game_intro_watch_firefly") else navController.navigate("paywall")
                },
                onNavigateToParentDashboard = {
                    navController.navigate("parent_dashboard")
                }
            )
        }
        composable("game_intro_whats_missing") {
            GameIntroScreen(
                instructionText = "Memorize what you see!",
                onReady = {
                    navController.navigate("whats_missing") {
                        popUpTo("game_intro_whats_missing") { inclusive = true }
                    }
                }
            )
        }
        composable("whats_missing") {
            com.example.focusbuilder.ui.WhatsMissingScreen(
                onNavigateHome = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
        composable("game_intro") {
            GameIntroScreen(
                instructionText = "Watch carefully!",
                onReady = {
                    navController.navigate("wait_for_it") {
                        popUpTo("game_intro") { inclusive = true }
                    }
                }
            )
        }
        composable("game_intro_rule_switch") {
            GameIntroScreen(
                instructionText = "Pay attention to the rule!",
                onReady = {
                    navController.navigate("rule_switch_sort") {
                        popUpTo("game_intro_rule_switch") { inclusive = true }
                    }
                }
            )
        }
        composable("rule_switch_sort") {
            com.example.focusbuilder.ui.RuleSwitchSortScreen(
                onNavigateHome = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
        composable("game_intro_find_signal") {
            GameIntroScreen(
                instructionText = "Find all the targets!",
                onReady = {
                    navController.navigate("find_signal") {
                        popUpTo("game_intro_find_signal") { inclusive = true }
                    }
                }
            )
        }
        composable("find_signal") {
            com.example.focusbuilder.ui.FindSignalScreen(
                onNavigateHome = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
        composable("game_intro_watch_firefly") {
            GameIntroScreen(
                instructionText = "Catch the firefly when it lands on the target flower!",
                onReady = {
                    navController.navigate("watch_firefly") {
                        popUpTo("game_intro_watch_firefly") { inclusive = true }
                    }
                }
            )
        }
        composable("watch_firefly") {
            com.example.focusbuilder.ui.WatchFireflyScreen(
                onNavigateHome = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
        composable("game_intro_sequence") {
            GameIntroScreen(
                instructionText = "Watch the pattern and repeat!",
                onReady = {
                    navController.navigate("sequence_repeat") {
                        popUpTo("game_intro_sequence") { inclusive = true }
                    }
                }
            )
        }
        composable("sequence_repeat") {
            com.example.focusbuilder.ui.SequenceRepeatScreen(
                onNavigateHome = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
        composable("wait_for_it") {
            WaitForItScreen(
                onNavigateHome = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
        composable("game_complete") {
            GameCompleteScreen(
                statText = "Correct taps: 8",
                onNavigateHome = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
        composable("parent_dashboard") {
            ParentDashboardScreen(onNavigateBack = {
                navController.popBackStack()
            })
        }
        composable("paywall") {
            com.example.focusbuilder.ui.PaywallScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onUnlockSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}
