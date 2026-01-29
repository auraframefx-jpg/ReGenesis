package dev.aurakai.auraframefx.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavController
import dev.aurakai.auraframefx.ui.theme.SovereignTeal
import kotlin.math.absoluteValue

/**
 * 🛰️ EXODUS HUD
 * The Split-Screen Anti-Gravity HUD (15/85 Ratio).
 * Replaces the Sovereign Procession Screen.
 */
/**
 * Composes the Exodus HUD screen: a horizontally swipeable row of monolith cards with
 * 3D-like scale and alpha effects, and a bottom Prometheus globe that pulses in response to global touch.
 *
 * The top region occupies ~85% of the height and displays a pager of monoliths whose scale,
 * alpha, and vertical offset vary with distance from the center to produce a parallax/orbit feel.
 * Double-tapping a monolith navigates to its detail route; press-and-hold anywhere triggers the globe pulse.
 *
 * @param navController NavController used to navigate to a monolith's detail screen on card double-tap
 */
@Composable
fun ExodusHUD(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { SovereignRouter.getCount() })

    // Pulse State driven by touch
    var isPressed by remember { mutableStateOf(false) }

    val pulseIntensity by animateFloatAsState(
        targetValue = if (isPressed) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 200),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // Capture touches globally for the "12th Sense" pulse
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onDoubleTap = { /* Consumed here to prevent conflicts, handled in cards */ },
                    onTap = { /* Consumed here to prevent conflicts */ }
                )
            }
    ) {
        // TOP 85%: The 11 Sovereign Monoliths (8K High-Fi)
        Box(
            modifier = Modifier
                .weight(0.85f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
             HorizontalPager(
                 state = pagerState,
                 modifier = Modifier.fillMaxSize(),
                 contentPadding = PaddingValues(horizontal = 64.dp), // Increased padding for 3D effect space
                 pageSpacing = 16.dp
             ) { page ->
                val route = SovereignRouter.fromPage(page)

                // Prometheus Orbit Logic: Calculate scale and alpha based on distance from center
                val pageOffset = (
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                ).absoluteValue

                // We want the center item to be full size, and items to the side to "curve away" (scale down)
                val scale = lerp(
                    start = 0.85f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                )

                // Optional: Alpha fade for distant items
                val alpha = lerp(
                    start = 0.5f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                )

                MonolithCard(
                    assetPath = route.highFiPath,
                    onDoubleTap = { navController.navigate("pixel_domain/${route.id}") },
                    onPress = {
                        isPressed = true
                    },
                    onRelease = {
                        isPressed = false
                    },
                    modifier = Modifier
                        .fillMaxHeight(0.9f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                            // Simple Y translation to simulate arc if needed, but scale often suffices for "orbit" feel in flat 2D
                            translationY = pageOffset * 20.dp.toPx() // Sinks down slightly as it moves away
                        }
                )
            }
        }

        // BOTTOM 15%: The Prometheus Globe (Celestial Navigation)
        Box(
            modifier = Modifier
                .weight(0.15f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            PrometheusGlobe(
                color = SovereignTeal,
                pulseIntensity = pulseIntensity
            )
        }
    }
}

/**
 * Renders a monolith visual that handles double-tap navigation and press/release touch callbacks.
 *
 * @param assetPath Path or identifier for the image displayed by the monolith.
 * @param onDoubleTap Called when the card is double-tapped (typically used to navigate to details).
 * @param onPress Called when a press gesture begins on the card.
 * @param onRelease Called when the press gesture ends or is released.
 * @param modifier Modifier applied to the monolith; gesture handling for taps and presses is added to this modifier. 
 */
@Composable
fun MonolithCard(
    assetPath: String,
    onDoubleTap: () -> Unit,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    SovereignMonolith(
        imagePath = assetPath,
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDoubleTap() },
                    onPress = {
                        onPress()
                        tryAwaitRelease()
                        onRelease()
                    }
                )
            }
    )
}