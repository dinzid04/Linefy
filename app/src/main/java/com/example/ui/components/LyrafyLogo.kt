package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.testTag
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PlusJakartaSans
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal data class LyrafySvgPathSpec(
    val pathData: String,
    val fillColor: Color,
    val translateX: Float,
    val translateY: Float,
    val pivotX: Float = 627f,
    val pivotY: Float = 627f,
)

internal data class ParsedLyrafyPath(
    val path: Path,
    val fillColor: Color,
    val translateX: Float,
    val translateY: Float,
    val pivotX: Float,
    val pivotY: Float,
)

private const val LYRAFY_VIEWPORT_SIZE = 1254f
private const val LYRAFY_BBOX_LEFT = 240f
private const val LYRAFY_BBOX_TOP = 250f
private const val LYRAFY_BBOX_RIGHT = 1020f
private const val LYRAFY_BBOX_BOTTOM = 960f

// Layer 1: Bodi Harpa & Not Musik (#F8EDDB & #F9EEDC)
private val LAYER_1_SPECS = listOf(
    LyrafySvgPathSpec(
        pathData = "M0 0 C8.63 6.29 13.35 13.62 18 23 C18.42 23.82 18.84 24.64 19.28 25.49 C31.28 51.7 25.04 85.07 16 111 C14.75 114.04 13.39 117.02 12 120 C11.32 121.66 10.65 123.33 10 125 C8.46 128.89 6.83 132.74 5.11 136.55 C4.39 138.14 3.68 139.73 2.97 141.33 C-0.3 148.63 -3.86 155.77 -7.47 162.91 C-13.21 174.26 -18.82 185.66 -24.19 197.19 C-24.7 198.29 -25.22 199.39 -25.75 200.53 C-28.73 206.97 -31.4 213.47 -33.87 220.13 C-34.93 222.82 -36.14 225.33 -37.5 227.88 C-40.2 233.08 -41.94 238.52 -43.69 244.09 C-44.64 247.07 -45.78 249.92 -46.98 252.8 C-49.1 258.01 -50.45 263.27 -51.69 268.75 C-51.9 269.66 -52.11 270.56 -52.33 271.5 C-57.45 293.64 -57.74 317.2 -46.69 337.5 C-36.52 353.42 -23.06 361.68 -5 366 C4.89 368.12 14.93 368.54 25 369 C25 369.66 25 370.32 25 371 C20.69 372.54 16.51 373.53 12 374.31 C4.15 375.71 -3.61 377.46 -11.37 379.29 C-15.27 380.19 -19.12 381.07 -23.09 381.59 C-25.89 381.69 -25.89 381.69 -27 383 C-28.41 383.31 -29.83 383.57 -31.25 383.81 C-35.04 384.47 -38.82 385.16 -42.59 385.91 C-52.89 387.97 -63.16 389.64 -73.58 390.92 C-80.51 391.78 -87.38 392.83 -94.24 394.09 C-98.21 394.79 -101.98 395.46 -106 395 C-119.64 385.04 -125.58 361.37 -128.23 345.8 C-129.44 337.48 -129.56 329.38 -129 321 C-128.96 320.28 -128.92 319.55 -128.88 318.81 C-126.56 280.71 -108.02 244.88 -88.69 212.81 C-87.08 210.13 -85.49 207.45 -83.9 204.75 C-77.92 194.64 -71.65 184.74 -65.21 174.91 C-54.71 158.87 -44.78 142.49 -35 126 C-34.38 124.97 -33.76 123.93 -33.13 122.87 C-29.83 117.3 -26.75 111.78 -24.25 105.81 C-23.96 105.13 -23.67 104.45 -23.37 103.75 C-21.32 98.91 -19.46 94.05 -18 89 C-17.65 87.96 -17.31 86.92 -16.95 85.86 C-13.58 72.21 -15.43 58.81 -22.32 46.68 C-28.44 37.51 -39.12 31.61 -49.77 29.43 C-53.64 28.91 -57.48 28.65 -61.38 28.44 C-62.11 28.39 -62.85 28.35 -63.61 28.31 C-65.4 28.2 -67.2 28.1 -69 28 C-68.96 17.79 -65.14 9.98 -58.12 2.62 C-41.37 -12.84 -18.3 -12.59 0 0 Z ",
        fillColor = Color(0xFFF8EDDB),
        translateX = 533f,
        translateY = 354f,
    ),
    LyrafySvgPathSpec(
        pathData = "M0 0 C0.46 2.37 0.92 4.75 1.38 7.12 C1.5 7.77 1.63 8.42 1.76 9.08 C12.32 64.56 -1.79 120.96 -33 167.64 C-38.02 175.02 -43.22 182.19 -49 189 C-49.85 190.04 -50.7 191.07 -51.55 192.11 C-62.75 205.6 -75.05 218.33 -89 229 C-90.5 230.19 -90.5 230.19 -92.03 231.41 C-127.87 259.74 -170.18 278.32 -215.25 285.44 C-216.36 285.61 -217.48 285.79 -218.62 285.97 C-219.66 286.12 -220.7 286.28 -221.77 286.43 C-222.68 286.57 -223.6 286.7 -224.54 286.84 C-226.97 287 -228.7 286.74 -231 286 C-229.54 285.42 -228.08 284.83 -226.62 284.25 C-225.7 283.88 -224.78 283.5 -223.82 283.12 C-222.13 282.45 -220.42 281.79 -218.71 281.16 C-186.18 269.01 -157.06 244.21 -136 217 C-135.23 216.03 -134.45 215.06 -133.65 214.06 C-121.18 198.25 -111.09 181.26 -101.44 163.61 C-100.24 161.44 -99.01 159.3 -97.75 157.16 C-96.02 154.04 -94.72 150.98 -93.5 147.62 C-92.11 143.89 -90.69 140.23 -89 136.62 C-73.39 101.91 -71.32 64.4 -71 27 C-69.87 26.53 -69.87 26.53 -68.72 26.06 C-61.8 23.2 -54.88 20.33 -47.96 17.47 C-43.7 15.7 -39.44 13.94 -35.19 12.19 C-33.12 11.33 -31.06 10.48 -29 9.62 C-27.53 9.02 -27.53 9.02 -26.02 8.39 C-19.72 5.77 -13.56 2.98 -7.49 -0.14 C-4.57 -1.15 -2.93 -0.87 0 0 Z ",
        fillColor = Color(0xFFF8EDDB),
        translateX = 849f,
        translateY = 647f,
    ),
    LyrafySvgPathSpec(
        pathData = "M0 0 C4.58 4.14 7.11 9.31 9.75 14.81 C10.25 15.8 10.25 15.8 10.75 16.81 C10.82 18.35 10.83 19.9 10.81 21.44 C10.8 22.25 10.79 23.07 10.79 23.91 C10.77 24.54 10.76 25.17 10.75 25.81 C9.33 25.87 9.33 25.87 7.89 25.93 C-7.95 26.74 -22.06 29.68 -33.25 41.81 C-42.23 53.89 -44.06 67.3 -41.94 81.95 C-36.73 109.96 -20.01 133.78 -4.25 156.81 C-3.06 158.56 -1.88 160.31 -0.69 162.06 C3.55 168.31 7.83 174.53 12.2 180.68 C27.2 201.8 40.51 223.75 51.81 247.06 C52.29 248.04 52.77 249.02 53.26 250.02 C55.45 254.61 57.33 258.67 57.75 263.81 C43.05 271.24 28.13 277.88 12.88 284.06 C12.05 284.4 11.22 284.74 10.37 285.09 C8 286.05 5.63 287 3.26 287.95 C2.54 288.24 1.83 288.53 1.09 288.83 C-1.84 290 -4.06 290.81 -7.25 290.81 C-7.39 290.21 -7.53 289.6 -7.67 288.97 C-12.14 269.84 -17.69 251.2 -25.73 233.24 C-27.03 230.31 -28.25 227.36 -29.46 224.39 C-32.59 216.85 -36.16 209.53 -39.75 202.19 C-40.41 200.83 -41.07 199.48 -41.72 198.12 C-44.84 191.71 -47.98 185.31 -51.22 178.96 C-52.75 175.77 -54 172.48 -55.28 169.18 C-56.19 166.96 -57.2 164.87 -58.31 162.75 C-60.07 159.34 -61.26 155.92 -62.41 152.27 C-63.35 149.51 -64.47 146.87 -65.62 144.19 C-70.97 131.47 -74.51 118.44 -77.7 105.06 C-78.2 103.02 -78.73 100.99 -79.27 98.96 C-81.95 87.99 -82.62 77.38 -82.62 66.12 C-82.62 63.58 -82.66 61.04 -82.7 58.5 C-82.84 37.77 -77.69 17.98 -63.12 2.56 C-44.41 -15.08 -19.86 -15.73 0 0 Z ",
        fillColor = Color(0xFFF8EDDB),
        translateX = 781.25f,
        translateY = 356.1875f,
    ),
    LyrafySvgPathSpec(
        pathData = "M0 0 C0.09 17.86 0.16 35.73 0.21 53.59 C0.23 61.89 0.26 70.19 0.3 78.48 C0.34 85.72 0.37 92.96 0.38 100.19 C0.38 104.02 0.39 107.85 0.42 111.67 C0.45 115.29 0.46 118.9 0.45 122.51 C0.45 124.45 0.48 126.38 0.5 128.31 C0.41 147.6 -7.05 164.82 -20 179 C-20.67 179.74 -20.67 179.74 -21.35 180.49 C-41.15 202.16 -71.38 220.41 -101.28 222.2 C-120.23 222.83 -137.36 222.93 -151.95 209.32 C-160.52 199.61 -161.71 189.57 -161.18 177.17 C-159.59 157.97 -144.81 140.36 -131 128 C-108.21 108.78 -80.17 94.88 -50.02 94.7 C-48.64 94.69 -47.27 94.67 -45.9 94.64 C-32.64 94.36 -22.88 97.06 -11 103 C-11 70 -11 37 -11 3 C-1 0 -1 0 0 0 Z ",
        fillColor = Color(0xFFF9EEDC),
        translateX = 676f,
        translateY = 709f,
    ),
)

// Layer 2: Tiga Senar Vertikal (#F7EDDA, #EBE3D4, #F7EEDD)
private val LAYER_2_SPECS = listOf(
    LyrafySvgPathSpec(
        pathData = "M0 0 C0.66 0 1.32 0 2 0 C2.36 1.3 2.36 1.3 2.73 2.63 C4.52 8.77 6.12 13.91 10 19 C10.33 19.99 10.66 20.98 11 22 C12.72 23.03 12.72 23.03 15 24 C17.35 25.31 19.67 26.66 22 28 C21.32 28.54 20.64 29.07 19.94 29.62 C18.97 30.41 18 31.19 17 32 C16.42 32.45 15.85 32.89 15.25 33.36 C8.04 39.15 5.73 44.78 4.2 53.74 C4.13 54.49 4.07 55.23 4 56 C4.5 56.5 4.5 56.5 5 57 C5.1 58.91 5.12 60.82 5.12 62.73 C5.13 63.98 5.13 65.23 5.13 66.53 C5.13 67.93 5.12 69.34 5.12 70.75 C5.12 72.23 5.12 73.71 5.12 75.19 C5.13 79.29 5.12 83.38 5.12 87.47 C5.11 91.87 5.11 96.28 5.11 100.68 C5.11 108.32 5.11 115.95 5.11 123.59 C5.1 134.63 5.1 145.68 5.1 156.72 C5.09 174.63 5.08 192.54 5.08 210.46 C5.07 227.86 5.06 245.27 5.05 262.67 C5.05 264.28 5.05 264.28 5.05 265.92 C5.05 271.3 5.05 276.68 5.05 282.06 C5.04 326.7 5.02 371.35 5 416 C2.03 416.33 -0.94 416.66 -4 417 C-4 297.87 -4 178.74 -4 56 C-3.01 56 -2.02 56 -1 56 C-3.54 45.92 -7.04 39.18 -15.13 32.54 C-16.86 31.11 -18.44 29.61 -20 28 C-19.32 27.55 -18.65 27.11 -17.95 26.64 C-7.56 19.6 -2.62 12.22 0 0 Z ",
        fillColor = Color(0xFFF7EDDA),
        translateX = 628f,
        translateY = 275f,
    ),
    LyrafySvgPathSpec(
        pathData = "M0 0 C2.96 1.48 3.35 3.7 4.69 6.69 C7.28 12.03 9.89 15.87 15 19 C15.99 19.33 16.98 19.66 18 20 C18 20.66 18 21.32 18 22 C16.56 22.94 15.11 23.88 13.66 24.8 C9.1 28.09 6.04 32.84 4 38 C3.58 41.17 3.58 41.17 4 44 C4.66 44.66 5.32 45.32 6 46 C6.25 48.47 6.25 48.47 6.25 51.65 C6.25 52.86 6.25 54.07 6.26 55.31 C6.25 56.67 6.25 58.02 6.24 59.38 C6.24 60.8 6.24 62.23 6.25 63.66 C6.25 67.6 6.24 71.54 6.23 75.48 C6.22 79.73 6.23 83.97 6.23 88.22 C6.23 95.57 6.22 102.93 6.21 110.29 C6.2 120.93 6.19 131.57 6.19 142.2 C6.18 159.46 6.17 176.72 6.15 193.98 C6.13 210.75 6.12 227.51 6.11 244.28 C6.11 245.32 6.11 246.35 6.11 247.41 C6.11 252.59 6.1 257.78 6.1 262.96 C6.08 305.97 6.04 348.99 6 392 C2.7 392.33 -0.6 392.66 -4 393 C-4.02 348.81 -4.04 304.62 -4.05 260.43 C-4.05 255.22 -4.05 250.01 -4.05 244.8 C-4.06 243.76 -4.06 242.73 -4.06 241.66 C-4.06 224.83 -4.07 208.01 -4.08 191.19 C-4.09 173.94 -4.09 156.7 -4.09 139.45 C-4.1 128.8 -4.1 118.15 -4.11 107.49 C-4.11 100.2 -4.11 92.91 -4.11 85.61 C-4.11 81.4 -4.11 77.18 -4.12 72.97 C-4.12 69.12 -4.12 65.26 -4.12 61.41 C-4.12 60.01 -4.12 58.61 -4.12 57.21 C-4.13 55.32 -4.13 53.43 -4.12 51.54 C-4.12 50.49 -4.12 49.43 -4.12 48.35 C-4 46 -4 46 -3 45 C-2.08 39.78 -2.71 35.76 -5.5 31.25 C-8.29 27.42 -11.29 25.15 -15.46 22.97 C-16.22 22.49 -16.22 22.49 -17 22 C-17 21.34 -17 20.68 -17 20 C-15.46 18.99 -15.46 18.99 -13.38 17.88 C-6.4 13.68 -2.8 7.47 0 0 Z ",
        fillColor = Color(0xFFEBE3D4),
        translateX = 584f,
        translateY = 311f,
    ),
    LyrafySvgPathSpec(
        pathData = "M0 0 C2.75 1.38 2.76 2.75 3.75 5.62 C6.25 12.1 9.32 16.46 15.38 19.94 C16.67 20.46 16.67 20.46 18 21 C18 21.66 18 22.32 18 23 C16.93 23.56 15.86 24.11 14.75 24.69 C9.1 27.94 5.73 32.72 4 39 C3.57 43.46 3.61 47.89 3.64 52.37 C3.64 53.74 3.64 55.1 3.63 56.47 C3.62 60.22 3.64 63.96 3.65 67.71 C3.66 71.76 3.66 75.8 3.66 79.84 C3.66 86.85 3.67 93.85 3.68 100.85 C3.71 110.98 3.72 121.11 3.72 131.23 C3.73 147.66 3.75 164.09 3.77 180.52 C3.8 196.48 3.82 212.44 3.84 228.39 C3.84 229.38 3.84 230.36 3.84 231.37 C3.84 236.31 3.85 241.25 3.85 246.18 C3.88 287.12 3.93 328.06 4 369 C1.03 369.66 -1.94 370.32 -5 371 C-5 263.75 -5 156.5 -5 46 C-4.01 46 -3.02 46 -2 46 C-3.07 37.04 -6.01 31.54 -12.75 25.56 C-14.9 23.99 -14.9 23.99 -17 23 C-17 22.34 -17 21.68 -17 21 C-15.21 19.74 -15.21 19.74 -12.81 18.31 C-5.53 13.59 -2.7 8.09 0 0 Z ",
        fillColor = Color(0xFFF7EEDD),
        translateX = 672f,
        translateY = 310f,
    ),
)

// Layer 3: Cincin Orbit Emas (#E2B963 & detail aksen)
private val LAYER_3_SPECS = listOf(
    LyrafySvgPathSpec(
        pathData = "M0 0 C8.63 6.29 13.35 13.62 18 23 C18.42 23.82 18.84 24.64 19.28 25.49 C31.28 51.7 25.04 85.07 16 111 C14.75 114.04 13.39 117.02 12 120 C11.32 121.66 10.65 123.33 10 125 C8.46 128.89 6.83 132.74 5.11 136.55 C4.39 138.14 3.68 139.73 2.97 141.33 C-0.3 148.63 -3.86 155.77 -7.47 162.91 C-13.21 174.26 -18.82 185.66 -24.19 197.19 C-24.7 198.29 -25.22 199.39 -25.75 200.53 C-28.73 206.97 -31.4 213.47 -33.87 220.13 C-34.93 222.82 -36.14 225.33 -37.5 227.88 C-40.2 233.08 -41.94 238.52 -43.69 244.09 C-44.64 247.07 -45.78 249.92 -46.98 252.8 C-49.1 258.01 -50.45 263.27 -51.69 268.75 C-51.9 269.66 -52.11 270.56 -52.33 271.5 C-57.45 293.64 -57.74 317.2 -46.69 337.5 C-36.4 353.61 -23.1 361.34 -5 366 C-4.38 366.18 -3.76 366.36 -3.12 366.54 C1.43 367.52 6.18 367.18 10.81 367.19 C11.86 367.2 12.91 367.21 13.99 367.22 C28.64 367.25 42.61 364.42 56.81 361.19 C58 360.92 58 360.92 59.21 360.65 C69.32 358.38 79.38 356 89.33 353.08 C93.06 351.98 96.8 350.98 100.56 350 C108.76 347.83 116.88 345.43 125 343 C126.65 342.51 128.31 342.02 129.96 341.52 C131.66 341.02 133.36 340.51 135.06 340 C136.19 339.66 136.19 339.66 137.35 339.32 C188.26 324.05 238.27 306.23 287 285 C288.06 284.54 289.11 284.08 290.2 283.61 C295.84 281.14 301.43 278.6 307 276 C307.89 275.59 308.77 275.18 309.68 274.76 C346.74 257.56 383.23 237.31 413 209 C414.08 208.02 414.08 208.02 415.18 207.02 C426.38 196.49 436.83 181.82 437.34 166.06 C437.45 158.2 437.25 152.28 432 146 C431.43 145.31 430.86 144.61 430.27 143.9 C421.69 134.47 409.63 130.42 398 126 C398 125.67 398 125.34 398 125 C407.53 125.52 416.68 127.04 426 129 C426.97 129.19 427.94 129.38 428.94 129.58 C440.62 132.28 452.56 138.46 460 148 C460.52 148.64 461.05 149.28 461.59 149.94 C466.8 157.53 465.88 167.04 464.59 175.76 C458.69 199.25 435.66 220.33 416.71 233.79 C414.86 235.1 413.05 236.44 411.23 237.78 C388.03 254.7 362.68 268.24 337.06 281.06 C336.41 281.39 335.76 281.71 335.09 282.05 C333.22 282.99 331.35 283.92 329.47 284.85 C328.39 285.38 327.31 285.92 326.2 286.47 C322.83 288.08 319.44 289.55 316 291 C316.19 292.14 316.39 293.27 316.59 294.45 C316.85 296.01 317.11 297.57 317.38 299.12 C317.56 300.24 317.56 300.24 317.76 301.37 C318.18 303.91 318.59 306.46 319 309 C319.18 310.06 319.35 311.12 319.53 312.21 C328.34 368.27 308.94 424.77 276.42 470.05 C273.42 474.16 270.29 478.12 267 482 C266.15 483.04 265.3 484.07 264.45 485.11 C253.25 498.6 240.95 511.33 227 522 C225.5 523.19 225.5 523.19 223.97 524.41 C188.13 552.74 145.82 571.32 100.75 578.44 C99.64 578.61 98.52 578.79 97.38 578.97 C96.34 579.12 95.3 579.28 94.23 579.43 C93.32 579.57 92.4 579.7 91.46 579.84 C89.03 580 87.3 579.74 85 579 C86.46 578.42 87.92 577.83 89.38 577.25 C90.76 576.69 90.76 576.69 92.18 576.12 C93.87 575.45 95.58 574.79 97.29 574.16 C129.82 562.01 158.94 537.21 180 510 C180.77 509.03 181.55 508.06 182.35 507.06 C194.82 491.25 204.91 474.26 214.56 456.61 C215.76 454.44 216.99 452.3 218.25 450.16 C219.98 447.04 221.28 443.98 222.5 440.62 C223.89 436.89 225.31 433.23 227 429.62 C242.46 395.25 244.68 358.05 245 321 C243.77 321.44 242.54 321.88 241.28 322.34 C216.73 331.15 192.14 339.74 167.3 347.71 C166.44 347.98 165.57 348.26 164.69 348.55 C163.09 349.06 161.49 349.57 159.9 350.07 C156.98 351.01 154.12 351.95 151.3 353.17 C148.72 354.1 146.72 354.06 144 354 C144 354.67 144.01 355.35 144.01 356.04 C144.1 372.52 144.17 389 144.21 405.48 C144.23 413.45 144.26 421.42 144.3 429.39 C144.34 436.35 144.37 443.3 144.38 450.26 C144.38 453.93 144.39 457.61 144.42 461.28 C144.72 499.25 144.72 499.25 136.25 514.44 C135.73 515.42 135.73 515.42 135.2 516.41 C119.47 545.24 89.94 564.82 59 574.26 C41.11 579.2 17.19 580.57 0.45 571.53 C-8.32 566.05 -14.22 559.01 -17.64 549.17 C-20.5 532.07 -15.55 516.77 -5.74 502.78 C13.69 476.33 44.51 456.81 76.83 450.44 C82.19 449.69 87.58 449.74 92.98 449.7 C94.36 449.69 95.73 449.67 97.1 449.64 C109.33 449.38 119.03 451.51 130 457 C130.33 424.66 130.66 392.32 131 359 C123.41 361.31 115.82 363.62 108 366 C95.98 369.17 95.98 369.17 91.07 370.42 C86.89 371.49 82.73 372.62 78.58 373.8 C61.96 378.51 45.25 382.65 28.43 386.59 C26.7 386.99 24.97 387.4 23.24 387.8 C22.42 387.99 21.6 388.19 20.75 388.38 C15.54 389.61 10.38 390.93 5.24 392.42 C1.73 393.33 -1.82 393.84 -5.39 394.4 C-10.16 395.19 -14.89 396.12 -19.62 397.06 C-20.52 397.24 -21.41 397.41 -22.32 397.59 C-23.16 397.76 -23.99 397.93 -24.85 398.1 C-25.6 398.25 -26.35 398.4 -27.13 398.55 C-29.08 398.94 -29.08 398.94 -31 400 C-32.39 400.26 -33.78 400.48 -35.18 400.68 C-36.03 400.81 -36.88 400.93 -37.75 401.06 C-38.64 401.18 -39.52 401.31 -40.44 401.44 C-47.66 402.47 -54.83 403.66 -62 405 C-78.11 407.99 -94.19 410.76 -110.46 412.7 C-112.85 412.98 -115.25 413.28 -117.64 413.59 C-134.46 415.69 -151.13 416.19 -168.06 416.19 C-171.43 416.19 -174.79 416.21 -178.16 416.22 C-200.8 416.29 -227.24 416.05 -244.94 399.62 C-251.14 392.75 -252.74 386.91 -252.45 377.59 C-251.73 371.83 -249.69 367.11 -247 362 C-246.55 361.15 -246.11 360.3 -245.65 359.42 C-232.94 336.88 -211.52 319.11 -191 304 C-190.09 303.32 -190.09 303.32 -189.15 302.63 C-180.36 296.15 -171.27 290.15 -162.05 284.3 C-160.06 283.04 -158.08 281.77 -156.1 280.49 C-154.92 279.74 -153.74 279 -152.56 278.25 C-151.53 277.6 -150.5 276.94 -149.44 276.27 C-148.23 275.64 -148.23 275.64 -147 275 C-146.34 275.33 -145.68 275.66 -145 276 C-145.62 276.5 -146.24 277 -146.88 277.51 C-149.93 279.99 -152.97 282.49 -156 285 C-156.56 285.47 -157.12 285.93 -157.7 286.41 C-180.35 305.31 -208.85 329.92 -212.34 361.06 C-212.76 369.22 -210.87 375.65 -205.39 381.81 C-186.86 398.82 -156.87 396.41 -133.81 396.19 C-131.1 396.17 -128.4 396.16 -125.69 396.15 C-119.13 396.11 -112.56 396.06 -106 396 C-106.59 395.16 -107.19 394.33 -107.8 393.47 C-123.28 371.53 -130.81 348.03 -129 321 C-128.96 320.28 -128.92 319.55 -128.88 318.81 C-126.56 280.71 -108.02 244.88 -88.69 212.81 C-87.08 210.13 -85.49 207.45 -83.9 204.75 C-77.92 194.64 -71.65 184.74 -65.21 174.91 C-54.71 158.87 -44.78 142.49 -35 126 C-34.38 124.97 -33.76 123.93 -33.13 122.87 C-29.83 117.3 -26.75 111.78 -24.25 105.81 C-23.96 105.13 -23.67 104.45 -23.37 103.75 C-21.32 98.91 -19.46 94.05 -18 89 C-17.65 87.96 -17.31 86.92 -16.95 85.86 C-13.58 72.21 -15.43 58.81 -22.32 46.68 C-28.44 37.51 -39.12 31.61 -49.77 29.43 C-53.64 28.91 -57.48 28.65 -61.38 28.44 C-62.11 28.39 -62.85 28.35 -63.61 28.31 C-65.4 28.2 -67.2 28.1 -69 28 C-68.96 17.79 -65.14 9.98 -58.12 2.62 C-41.37 -12.84 -18.3 -12.59 0 0 Z ",
        fillColor = Color(0xFFE2B963),
        translateX = 533f,
        translateY = 354f,
    ),
    LyrafySvgPathSpec("M0 0 C7.79 0.52 15.35 1.45 23 3 C20.36 5.16 18.38 6.47 15 7 C12.32 6.11 9.83 5.18 7.25 4.06 C6.55 3.77 5.86 3.48 5.14 3.18 C3.42 2.46 1.71 1.73 0 1 C0 0.67 0 0.34 0 0 Z ", Color(0xFFD5AF5E), 931f, 479f),
    LyrafySvgPathSpec("M0 0 C0.66 0.33 1.32 0.66 2 1 C1.67 2.65 1.34 4.3 1 6 C-7.91 6.99 -7.91 6.99 -17 8 C-16 6 -16 6 -13.16 5 C-11.41 4.5 -11.41 4.5 -9.62 4 C-5.91 2.95 -3.22 2.15 0 0 Z ", Color(0xFFD7D2C4), 635f, 926f),
    LyrafySvgPathSpec("M0 0 C0.99 0.33 1.98 0.66 3 1 C2.93 1.59 2.86 2.19 2.78 2.8 C2.15 8.47 1.96 13.95 2.16 19.65 C2 22 2 22 0 25 C-1.17 20.84 -1.15 16.8 -1.12 12.5 C-1.13 11.74 -1.13 10.98 -1.14 10.2 C-1.13 6.63 -0.96 3.43 0 0 Z ", Color(0xFFE1DBCB), 516f, 880f),
    LyrafySvgPathSpec("M0 0 C7.26 0 14.52 0 22 0 C15.88 3.06 8.74 5.19 2 3 C1.34 2.01 0.68 1.02 0 0 Z ", Color(0xFFF3E3C5), 536f, 724f),
    LyrafySvgPathSpec("M0 0 C-9.32 6.21 -23.36 3.95 -34 3 C-34 2.67 -34 2.34 -34 2 C-30.21 1.66 -26.42 1.33 -22.62 1 C-21.03 0.86 -21.03 0.86 -19.41 0.71 C-12.92 0.15 -6.51 -0.13 0 0 Z ", Color(0xFFC3BEAE), 575f, 720f),
    LyrafySvgPathSpec("M0 0 C1.14 3.41 0.71 4.56 0 8 C-0.07 10.27 -0.09 12.54 -0.06 14.81 C-0.05 16.55 -0.05 16.55 -0.04 18.33 C-0.02 19.21 -0.01 20.09 0 21 C-0.99 20.34 -1.98 19.68 -3 19 C-3.4 15.92 -3.4 15.92 -3.38 12.19 C-3.38 10.96 -3.39 9.74 -3.4 8.48 C-2.98 4.85 -2.12 2.94 0 0 Z ", Color(0xFFCFAB5B), 284f, 723f),
    LyrafySvgPathSpec("M0 0 C0.66 0 1.32 0 2 0 C1.67 3.96 1.34 7.92 1 12 C0.34 12 -0.32 12 -1 12 C-1 15.96 -1 19.92 -1 24 C-1.33 24 -1.66 24 -2 24 C-2.37 7.11 -2.37 7.11 0 0 Z ", Color(0xFFD4CFC1), 701f, 392f),
    LyrafySvgPathSpec("M0 0 C0.66 0.99 1.32 1.98 2 3 C-1.83 6.92 -5.62 10.68 -10 14 C-10.66 13.67 -11.32 13.34 -12 13 C-11.3 12.31 -10.6 11.63 -9.88 10.92 C-8.97 10.02 -8.06 9.12 -7.12 8.19 C-6.22 7.29 -5.32 6.4 -4.38 5.48 C-1.9 3.04 -1.9 3.04 0 0 Z ", Color(0xFFDED7C7), 706f, 872f),
    LyrafySvgPathSpec("M0 0 C0.93 0.01 1.87 0.02 2.83 0.03 C3.8 0.05 4.77 0.08 5.77 0.11 C7.24 0.13 7.24 0.13 8.75 0.15 C11.17 0.18 13.59 0.23 16.02 0.29 C16.02 0.62 16.02 0.95 16.02 1.29 C13.35 1.63 10.68 1.96 8.02 2.29 C7.27 2.39 6.53 2.48 5.76 2.58 C0.81 3.19 -3.99 3.38 -8.98 3.29 C-5.92 0.6 -4.06 -0.07 0 0 Z ", Color(0xFFA98C4E), 403.98438f, 765.70703f),
    LyrafySvgPathSpec("M0 0 C0.66 0 1.32 0 2 0 C3.59 3.19 3.1 6.63 3.06 10.12 C3.06 10.88 3.05 11.63 3.05 12.41 C3.04 14.27 3.02 16.14 3 18 C2.67 18 2.34 18 2 18 C1.34 12.06 0.68 6.12 0 0 Z ", Color(0xFFCFA85B), 995f, 509f),
    LyrafySvgPathSpec("M0 0 C0.66 0.66 1.32 1.32 2 2 C1.7 6.14 1.32 8.52 -1 12 C-1.66 12 -2.32 12 -3 12 C-2.69 10.56 -2.38 9.12 -2.06 7.69 C-1.89 6.89 -1.71 6.09 -1.54 5.26 C-1 3 -1 3 0 0 Z ", Color(0xFFE1DACB), 769f, 757f),
    LyrafySvgPathSpec("M0 0 C7.42 0.99 7.42 0.99 15 2 C15 2.33 15 2.66 15 3 C13.27 3.2 11.54 3.38 9.81 3.56 C8.85 3.67 7.89 3.77 6.89 3.88 C4.06 4 1.75 3.68 -1 3 C-0.67 2.01 -0.34 1.02 0 0 Z ", Color(0xFFDEB358), 954f, 429f),
    LyrafySvgPathSpec("M0 0 C2.34 3.65 2.46 6.74 2 11 C0 13.44 0 13.44 -2 15 C-1.04 4.44 -1.04 4.44 0 0 Z ", Color(0xFFB2924B), 896f, 357f),
    LyrafySvgPathSpec("M0 0 C0.33 0.99 0.66 1.98 1 3 C1.66 3 2.32 3 3 3 C3.33 2.34 3.66 1.68 4 1 C4.33 2.98 4.66 4.96 5 7 C2.36 7 -0.28 7 -3 7 C-2.01 6.67 -1.02 6.34 0 6 C0 4.02 0 2.04 0 0 Z ", Color(0xFFDABE90), 627f, 325f),
    LyrafySvgPathSpec("M0 0 C0.66 0 1.32 0 2 0 C2.45 0.97 2.91 1.94 3.38 2.94 C4.66 6.07 4.66 6.07 7 7 C7 8.65 7 10.3 7 12 C3.66 9.01 1.78 6.11 0 2 C0 1.34 0 0.68 0 0 Z ", Color(0xFFE9E6DC), 517f, 905f),
    LyrafySvgPathSpec("M0 0 C0.66 0.33 1.32 0.66 2 1 C-2.45 4.47 -2.45 4.47 -7 8 C-7.99 7.34 -8.98 6.68 -10 6 C-8.38 4.97 -6.75 3.95 -5.12 2.94 C-4.22 2.37 -3.32 1.8 -2.38 1.21 C-1.2 0.61 -1.2 0.61 0 0 Z ", Color(0xFFD1B15F), 386f, 629f),
    LyrafySvgPathSpec("M0 0 C0.75 1.56 0.75 1.56 1 4 C-0.13 6.5 -1.52 8.67 -3 11 C-3.66 11 -4.32 11 -5 11 C-3.77 6.95 -2.3 3.56 0 0 Z ", Color(0xFFD1CCBD), 507f, 464f),
    LyrafySvgPathSpec("M0 0 C3.96 0 7.92 0 12 0 C7.6 2.2 4.81 2.52 0 3 C0 2.01 0 1.02 0 0 Z ", Color(0xFFD9B25D), 539f, 742f),
    LyrafySvgPathSpec("M0 0 C-1 2 -1 2 -4.38 3.25 C-5.57 3.5 -6.77 3.75 -8 4 C-8.99 3.34 -9.98 2.68 -11 2 C-9.54 1.47 -8.09 0.95 -6.62 0.44 C-5.81 0.15 -5 -0.14 -4.16 -0.44 C-2 -1 -2 -1 0 0 Z ", Color(0xFFC3A255), 711f, 682f),
    LyrafySvgPathSpec("M0 0 C0.66 0.33 1.32 0.66 2 1 C2.62 3.56 2.62 3.56 3 6 C2.34 6 1.68 6 1 6 C0.67 6.66 0.34 7.32 0 8 C-0.99 7.67 -1.98 7.34 -3 7 C-2.01 4.69 -1.02 2.38 0 0 Z ", Color(0xFFD9B466), 584f, 311f),
    LyrafySvgPathSpec("M0 0 C0.99 0.33 1.98 0.66 3 1 C-0.47 3.97 -0.47 3.97 -4 7 C-4 4 -4 4 -2 1.81 C-1.34 1.21 -0.68 0.62 0 0 Z ", Color(0xFFE8E1D0), 691f, 888f),
    LyrafySvgPathSpec("M0 0 C0.99 0 1.98 0 3 0 C4.35 2.71 4.07 5.01 4 8 C3.01 8.5 3.01 8.5 2 9 C1.34 6.03 0.68 3.06 0 0 Z ", Color(0xFFDCB15D), 386f, 864f),
    LyrafySvgPathSpec("M0 0 C0 4.35 -2.06 5.93 -5 9 C-7.17 9.68 -7.17 9.68 -9 10 C-8.06 8.89 -7.13 7.79 -6.19 6.69 C-5.67 6.07 -5.14 5.46 -4.61 4.82 C-3.13 3.15 -1.6 1.56 0 0 Z ", Color(0xFFD6D0C6), 542f, 840f),
    LyrafySvgPathSpec("M0 0 C-3.48 2.32 -5.86 2.7 -10 3 C-10.66 2.34 -11.32 1.68 -12 1 C-8.02 -0.33 -4.15 -0.07 0 0 Z ", Color(0xFFCBA85B), 630f, 707f),
    LyrafySvgPathSpec("M0 0 C0.66 0 1.32 0 2 0 C1.44 3.27 0.5 4.83 -2 7 C-2.99 6.67 -3.98 6.34 -5 6 C-3.38 3.96 -1.71 1.96 0 0 Z ", Color(0xFFE0DBCD), 712f, 865f),
    LyrafySvgPathSpec("M0 0 C0 0.66 0 1.32 0 2 C-4.46 3.49 -4.46 3.49 -9 5 C-9.66 4.34 -10.32 3.68 -11 3 C-9.73 2.49 -8.46 2 -7.19 1.5 C-6.48 1.22 -5.77 0.94 -5.04 0.66 C-3 0 -3 0 0 0 Z ", Color(0xFFCDA95B), 774f, 659f),
    LyrafySvgPathSpec("M0 0 C0.99 0.33 1.98 0.66 3 1 C3.33 4.3 3.66 7.6 4 11 C1.54 7.31 0.99 4.3 0 0 Z ", Color(0xFFCBC6B8), 708f, 477f),
    LyrafySvgPathSpec("M0 0 C3.96 0.33 7.92 0.66 12 1 C7.64 3.18 6.53 3.18 2 2 C1.34 1.34 0.68 0.68 0 0 Z ", Color(0xFFB08F4B), 931f, 479f),
    LyrafySvgPathSpec("M0 0 C0.66 0 1.32 0 2 0 C5 5.75 5 5.75 5 8 C2 7 2 7 0.81 5.12 C0 3 0 3 0 0 Z ", Color(0xFFD4D0C6), 746f, 465f),
    LyrafySvgPathSpec("M0 0 C0.66 0.33 1.32 0.66 2 1 C2.62 4.06 2.62 4.06 3 7 C1.68 7 0.36 7 -1 7 C-0.67 4.69 -0.34 2.38 0 0 Z ", Color(0xFFCFAB5D), 672f, 310f),
    LyrafySvgPathSpec("M0 0 C1.32 0.33 2.64 0.66 4 1 C4.33 2.32 4.66 3.64 5 5 C3.35 5 1.7 5 0 5 C0 3.35 0 1.7 0 0 Z ", Color(0xFFF9F6EE), 640f, 925f),
    LyrafySvgPathSpec("M0 0 C-3.51 2.41 -5.73 3.37 -10 3 C-10.33 2.34 -10.66 1.68 -11 1 C-3.57 -1.43 -3.57 -1.43 0 0 Z ", Color(0xFFEBC97F), 502f, 738f),
    LyrafySvgPathSpec("M0 0 C0.99 0.33 1.98 0.66 3 1 C1.35 2.65 -0.3 4.3 -2 6 C-2.99 5.67 -3.98 5.34 -5 5 C-3.35 3.35 -1.7 1.7 0 0 Z ", Color(0xFFCFAB59), 309f, 686f),
    LyrafySvgPathSpec("M0 0 C0.33 0 0.66 0 1 0 C1 2.97 1 5.94 1 9 C-0.32 8.67 -1.64 8.34 -3 8 C-2.01 5.36 -1.02 2.72 0 0 Z ", Color(0xFFDED6C4), 791f, 373f),
    LyrafySvgPathSpec("M0 0 C0.99 1.32 1.98 2.64 3 4 C1.02 4.99 1.02 4.99 -1 6 C-1.66 4.68 -2.32 3.36 -3 2 C-2.01 1.34 -1.02 0.68 0 0 Z ", Color(0xFFDAAF5B), 365f, 642f),
    LyrafySvgPathSpec("M0 0 C1.65 0 3.3 0 5 0 C5.33 0.66 5.66 1.32 6 2 C5.34 2 4.68 2 4 2 C4.33 2.99 4.66 3.98 5 5 C3.68 4.67 2.36 4.34 1 4 C1 3.34 1 2.68 1 2 C0.67 1.34 0.34 0.68 0 0 Z ", Color(0xFFD6AC59), 831f, 429f),
    LyrafySvgPathSpec("M0 0 C0.1 0.64 0.21 1.28 0.31 1.94 C0.54 2.62 0.77 3.3 1 4 C1.99 4.33 2.98 4.66 4 5 C2.68 5.99 1.36 6.98 0 8 C-0.66 7.34 -1.32 6.68 -2 6 C-1.34 4.02 -0.68 2.04 0 0 Z ", Color(0xFFEDDAB4), 583f, 350f),
)

// Layer 4: Bintang-Bintang Emas (#E3B960, #E3B860, #E3BA66, #E2B963)
private val LAYER_4_SPECS = listOf(
    LyrafySvgPathSpec(
        pathData = "M0 0 C0.33 0 0.66 0 1 0 C1.1 0.98 1.1 0.98 1.2 1.98 C4.19 36.62 4.19 36.62 21 66 C35.57 77.55 54.06 80.58 72 83 C72 83.33 72 83.66 72 84 C71.34 84.1 70.67 84.2 69.99 84.3 C41.43 88.25 41.43 88.25 18 104 C15.7 107.2 13.8 110.5 12 114 C11.63 114.71 11.26 115.43 10.88 116.16 C4.01 131.3 3.03 149.71 1 166 C0.67 166 0.34 166 0 166 C-0.1 165.08 -0.1 165.08 -0.2 164.15 C-3.04 134.16 -3.04 134.16 -15 107 C-15.41 106.37 -15.82 105.74 -16.24 105.1 C-26.34 91.41 -46.41 88.79 -62.02 86.41 C-70.31 85.27 -78.66 84.6 -87 84 C-87 83.67 -87 83.34 -87 83 C-86.29 82.93 -85.57 82.86 -84.84 82.79 C-48.13 80.47 -48.13 80.47 -17.1 62.96 C-8.51 51.76 -4.43 36.74 -3 22.88 C-2.84 21.39 -2.67 19.91 -2.51 18.43 C-2.43 17.7 -2.34 16.96 -2.26 16.2 C-1.62 10.79 -0.8 5.39 0 0 Z ",
        fillColor = Color(0xFFE3B960),
        translateX = 897f,
        translateY = 348f,
        pivotX = 897f,
        pivotY = 431f,
    ),
    LyrafySvgPathSpec(
        pathData = "M0 0 C2.9 2.45 3.76 4.73 4.88 8.31 C7.39 15.44 10.32 19.33 17.12 22.75 C17.97 23.14 18.82 23.52 19.7 23.92 C22 25 22 25 25 27 C24.35 27.23 23.7 27.46 23.02 27.7 C14.14 31.01 9.15 34.66 5.1 43.35 C3.51 47.17 2.18 51.04 1 55 C-0.61 51.99 -1.66 49 -2.69 45.75 C-5.2 38.88 -8.4 33.99 -14.9 30.4 C-17.57 29.17 -20.25 28.04 -23 27 C-20.6 24.6 -18.37 23.83 -15.25 22.5 C-6.89 18.39 -4.06 12.4 -1.12 3.88 C-0.74 2.59 -0.36 1.3 0 0 Z ",
        fillColor = Color(0xFFE3B860),
        translateX = 388f,
        translateY = 818f,
        pivotX = 388f,
        pivotY = 845f,
    ),
    LyrafySvgPathSpec(
        pathData = "M0 0 C0.66 0 1.32 0 2 0 C2.36 1.3 2.36 1.3 2.73 2.63 C4.52 8.77 6.12 13.91 10 19 C10.33 19.99 10.66 20.98 11 22 C12.72 23.03 12.72 23.03 15 24 C17.35 25.31 19.67 26.66 22 28 C21.32 28.54 20.64 29.07 19.94 29.62 C18.97 30.41 18 31.19 17 32 C16.42 32.45 15.85 32.89 15.25 33.36 C7.34 39.72 5.29 46.17 4 56 C4 56.33 4 56.66 4 57 C1.36 57 -1.28 57 -4 57 C-2.51 56.5 -2.51 56.5 -1 56 C-3.54 45.92 -7.04 39.18 -15.13 32.54 C-16.86 31.11 -18.44 29.61 -20 28 C-19.32 27.55 -18.65 27.11 -17.95 26.64 C-7.56 19.6 -2.62 12.22 0 0 Z ",
        fillColor = Color(0xFFE3BA66),
        translateX = 628f,
        translateY = 275f,
        pivotX = 629f,
        pivotY = 303f,
    ),
    LyrafySvgPathSpec(
        pathData = "M0 0 C2.75 1.38 2.76 2.75 3.75 5.62 C6.25 12.1 9.32 16.46 15.38 19.94 C16.67 20.46 16.67 20.46 18 21 C18 21.66 18 22.32 18 23 C16.93 23.56 15.86 24.11 14.75 24.69 C8.78 28.13 6.44 32.79 4 39 C3.67 39 3.34 39 3 39 C2.34 40.98 1.68 42.96 1 45 C0.01 45 -0.98 45 -2 45 C-2.07 44.44 -2.15 43.89 -2.23 43.31 C-3.55 35.61 -6.94 30.71 -12.75 25.56 C-14.9 23.99 -14.9 23.99 -17 23 C-17 22.34 -17 21.68 -17 21 C-15.21 19.74 -15.21 19.74 -12.81 18.31 C-5.53 13.59 -2.7 8.09 0 0 Z ",
        fillColor = Color(0xFFE2B963),
        translateX = 672f,
        translateY = 310f,
        pivotX = 672f,
        pivotY = 332f,
    ),
    LyrafySvgPathSpec(
        pathData = "M0 0 C2.96 1.48 3.35 3.7 4.69 6.69 C7.28 12.03 9.89 15.87 15 19 C15.99 19.33 16.98 19.66 18 20 C18 20.66 18 21.32 18 22 C16.56 22.94 15.11 23.88 13.66 24.8 C8.3 28.67 4.83 34.53 3.31 40.88 C3.21 41.58 3.11 42.28 3 43 C0.52 43.5 0.52 43.5 -2 44 C-2.09 43.36 -2.17 42.72 -2.26 42.06 C-3.24 36.02 -4.54 31.34 -9 27 C-11.09 25.54 -13.19 24.26 -15.43 23.04 C-15.95 22.7 -16.46 22.35 -17 22 C-17 21.34 -17 20.68 -17 20 C-15.46 18.99 -15.46 18.99 -13.38 17.88 C-6.4 13.68 -2.8 7.47 0 0 Z ",
        fillColor = Color(0xFFE2B963),
        translateX = 584f,
        translateY = 311f,
        pivotX = 584f,
        pivotY = 332f,
    ),
)

private fun List<LyrafySvgPathSpec>.parseAll(): List<ParsedLyrafyPath> = map { spec ->
    ParsedLyrafyPath(
        path = PathParser().parsePathString(spec.pathData).toPath(),
        fillColor = spec.fillColor,
        translateX = spec.translateX,
        translateY = spec.translateY,
        pivotX = spec.pivotX,
        pivotY = spec.pivotY,
    )
}

private object LyrafyParsedLayers {
    val layer1: List<ParsedLyrafyPath> by lazy { LAYER_1_SPECS.parseAll() }
    val layer2: List<ParsedLyrafyPath> by lazy { LAYER_2_SPECS.parseAll() }
    val layer3: List<ParsedLyrafyPath> by lazy { LAYER_3_SPECS.parseAll() }
    val layer4: List<ParsedLyrafyPath> by lazy { LAYER_4_SPECS.parseAll() }
}

private fun DrawScope.drawLyrafyPaths(
    paths: List<ParsedLyrafyPath>,
    colorOverride: Color? = null,
    alphaMultiplier: Float = 1f,
) {
    val alpha = alphaMultiplier.coerceIn(0f, 1f)
    if (alpha <= 0.001f) return
    paths.forEach { item ->
        withTransform({
            translate(left = item.translateX, top = item.translateY)
        }) {
            drawPath(
                path = item.path,
                color = (colorOverride ?: item.fillColor).copy(alpha = alpha),
            )
        }
    }
}

/**
 * Full-white (or custom-tinted) Lyrafy Harp Logo Mark for the top-left Home header.
 * Crops tightly to the artwork bounding box so the harp mark looks crisp at 38-44.dp.
 */
@Composable
fun LyrafyLogoMark(
    tint: Color = Color.White,
    modifier: Modifier = Modifier,
) {
    val l1 = remember { LyrafyParsedLayers.layer1 }
    val l2 = remember { LyrafyParsedLayers.layer2 }
    val l3 = remember { LyrafyParsedLayers.layer3 }
    val l4 = remember { LyrafyParsedLayers.layer4 }

    Canvas(modifier = modifier) {
        val bboxW = LYRAFY_BBOX_RIGHT - LYRAFY_BBOX_LEFT
        val bboxH = LYRAFY_BBOX_BOTTOM - LYRAFY_BBOX_TOP
        val scale = minOf(size.width / bboxW, size.height / bboxH)
        val offsetX = (size.width - bboxW * scale) * 0.5f - LYRAFY_BBOX_LEFT * scale
        val offsetY = (size.height - bboxH * scale) * 0.5f - LYRAFY_BBOX_TOP * scale

        withTransform({
            translate(left = offsetX, top = offsetY)
            scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
        }) {
            drawLyrafyPaths(l3, colorOverride = tint)
            drawLyrafyPaths(l1, colorOverride = tint)
            drawLyrafyPaths(l2, colorOverride = tint)
            drawLyrafyPaths(l4, colorOverride = tint)
        }
    }
}

/**
 * 4-Stage Choreographed Lyrafy Intro Splash Screen (Cold-Start Animation):
 * - Stage 1 (0ms - 350ms): Pitch-black (#0A0A0A) screen; thin golden horizontal line (#E2B963)
 *   expands from center to left & right.
 * - Stage 2 (300ms - 950ms): Vertical clipRect opens up & down (FastOutSlowInEasing) from the
 *   center line, revealing Layer 1 (Harp Body & Note) and Layer 2 (Three Vertical Strings),
 *   followed by Layer 3 (Golden Orbit Ring) sweeping around the harp.
 * - Stage 3 (750ms - 1400ms): Layer 4 (Golden Stars) scales in with bouncy spring while "Lyrafy"
 *   (28.sp, Bold, #F8EDDB, letterSpacing = 1.2.sp) fades in and slides up below the logo.
 * - Stage 4 (1650ms - 2050ms): Entire splash screen crossfades (alpha 1f -> 0f, scale 1f -> 1.05f)
 *   smoothly into the Home screen.
 */
@Composable
fun LyrafyIntroSplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val l1 = remember { LyrafyParsedLayers.layer1 }
    val l2 = remember { LyrafyParsedLayers.layer2 }
    val l3 = remember { LyrafyParsedLayers.layer3 }
    val l4 = remember { LyrafyParsedLayers.layer4 }

    val lineExpandProgress = remember { Animatable(0f) }
    val lineFadeProgress = remember { Animatable(1f) }
    val harpRevealProgress = remember { Animatable(0f) }
    val stringsRevealProgress = remember { Animatable(0f) }
    val orbitSweepProgress = remember { Animatable(0f) }
    val starsScaleProgress = remember { Animatable(0f) }
    val titleAlphaProgress = remember { Animatable(0f) }
    val titleSlideDp = remember { Animatable(18f) }
    val screenAlphaProgress = remember { Animatable(1f) }
    val screenScaleProgress = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Stage 1 (0ms - 350ms): Horizontal golden line expands from center
        launch {
            lineExpandProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            )
        }

        // Stage 2 (300ms - 950ms): Harp body & strings reveal vertically from center line + Orbit ring sweep
        launch {
            delay(280L)
            harpRevealProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 560, easing = FastOutSlowInEasing),
            )
        }
        launch {
            delay(340L)
            stringsRevealProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 560, easing = FastOutSlowInEasing),
            )
        }
        launch {
            delay(430L)
            orbitSweepProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
            )
        }
        launch {
            delay(520L)
            lineFadeProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 360, easing = LinearOutSlowInEasing),
            )
        }

        // Stage 3 (750ms - 1400ms): Golden stars pop with bouncy spring + "Lyrafy" title fades & slides up
        launch {
            delay(750L)
            starsScaleProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
        launch {
            delay(760L)
            titleAlphaProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
            )
        }
        launch {
            delay(760L)
            titleSlideDp.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
            )
        }

        // Stage 4 (1650ms - 2050ms): Crossfade out (alpha 1f -> 0f, scale 1f -> 1.05f)
        delay(1650L)
        launch {
            screenScaleProgress.animateTo(
                targetValue = 1.05f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            )
        }
        screenAlphaProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        )
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = screenAlphaProgress.value
                scaleX = screenScaleProgress.value
                scaleY = screenScaleProgress.value
            }
            .background(Color(0xFF0A0A0A))
            .testTag("lyrafy_intro_splash_screen"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .size(230.dp)
                    .testTag("lyrafy_intro_canvas"),
            ) {
                val scale = minOf(size.width / LYRAFY_VIEWPORT_SIZE, size.height / LYRAFY_VIEWPORT_SIZE) * 1.48f
                val offsetX = (size.width - LYRAFY_VIEWPORT_SIZE * scale) * 0.5f
                val offsetY = (size.height - LYRAFY_VIEWPORT_SIZE * scale) * 0.5f
                val centerY = 610f

                withTransform({
                    translate(left = offsetX, top = offsetY)
                    scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
                }) {
                    // Layer 3: Golden Orbit Ring sweeping horizontally and vertically
                    val orbitFrac = orbitSweepProgress.value.coerceIn(0f, 1f)
                    if (orbitFrac > 0.001f) {
                        val rightClip = LYRAFY_BBOX_LEFT + (LYRAFY_BBOX_RIGHT - LYRAFY_BBOX_LEFT) * orbitFrac
                        val halfH = (LYRAFY_BBOX_BOTTOM - LYRAFY_BBOX_TOP) * 0.58f * orbitFrac
                        clipRect(
                            left = LYRAFY_BBOX_LEFT - 40f,
                            top = centerY - halfH,
                            right = rightClip + 40f,
                            bottom = centerY + halfH,
                        ) {
                            drawLyrafyPaths(
                                paths = l3,
                                alphaMultiplier = orbitFrac,
                            )
                        }
                    }

                    // Layer 1: Harp Body & Musical Note opening vertically from the center line
                    val harpFrac = harpRevealProgress.value.coerceIn(0f, 1f)
                    if (harpFrac > 0.001f) {
                        val halfSpan = 390f * harpFrac
                        clipRect(
                            left = 0f,
                            top = centerY - halfSpan,
                            right = LYRAFY_VIEWPORT_SIZE,
                            bottom = centerY + halfSpan,
                        ) {
                            drawLyrafyPaths(
                                paths = l1,
                                alphaMultiplier = (harpFrac * 1.25f).coerceIn(0f, 1f),
                            )
                        }
                    }

                    // Layer 2: Three Vertical Strings growing from the center line
                    val stringsFrac = stringsRevealProgress.value.coerceIn(0f, 1f)
                    if (stringsFrac > 0.001f) {
                        val halfSpan = 370f * stringsFrac
                        clipRect(
                            left = 0f,
                            top = centerY - halfSpan,
                            right = LYRAFY_VIEWPORT_SIZE,
                            bottom = centerY + halfSpan,
                        ) {
                            drawLyrafyPaths(
                                paths = l2,
                                alphaMultiplier = (stringsFrac * 1.25f).coerceIn(0f, 1f),
                            )
                        }
                    }

                    // Layer 4: Golden Stars popping with bouncy spring around each star's own pivot
                    val starScale = starsScaleProgress.value
                    if (starScale > 0.01f) {
                        val starAlpha = starScale.coerceIn(0f, 1f)
                        l4.forEach { starItem ->
                            withTransform({
                                scale(
                                    scaleX = starScale,
                                    scaleY = starScale,
                                    pivot = Offset(starItem.pivotX, starItem.pivotY),
                                )
                                translate(left = starItem.translateX, top = starItem.translateY)
                            }) {
                                drawPath(
                                    path = starItem.path,
                                    color = starItem.fillColor.copy(alpha = starAlpha),
                                )
                            }
                        }
                    }

                    // Stage 1 Horizontal Golden Line (#E2B963) at the center
                    val lineExpand = lineExpandProgress.value.coerceIn(0f, 1f)
                    val lineAlpha = lineFadeProgress.value.coerceIn(0f, 1f)
                    if (lineExpand > 0.001f && lineAlpha > 0.001f) {
                        val cx = 627f
                        val halfLineLen = 285f * lineExpand
                        drawLine(
                            color = Color(0xFFE2B963).copy(alpha = lineAlpha),
                            start = Offset(cx - halfLineLen, centerY),
                            end = Offset(cx + halfLineLen, centerY),
                            strokeWidth = 5.5f,
                            cap = StrokeCap.Round,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Lyrafy",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color(0xFFF8EDDB),
                letterSpacing = 1.2.sp,
                modifier = Modifier
                    .offset { IntOffset(0, titleSlideDp.value.dp.roundToPx()) }
                    .graphicsLayer {
                        alpha = titleAlphaProgress.value
                    }
                    .testTag("lyrafy_intro_title"),
            )
        }
    }
}

/**
 * Full-color Gold & Cream Lyrafy Harp Logo (without black background)
 * rendered from the parsed SVG paths of `svg_3.txt` / `svg_2.txt`.
 */
@Composable
fun LyrafyGoldHarpIcon(
    modifier: Modifier = Modifier,
) {
    val l1 = remember { LyrafyParsedLayers.layer1 }
    val l2 = remember { LyrafyParsedLayers.layer2 }
    val l3 = remember { LyrafyParsedLayers.layer3 }
    val l4 = remember { LyrafyParsedLayers.layer4 }

    Canvas(modifier = modifier) {
        val bboxWidth = LYRAFY_BBOX_RIGHT - LYRAFY_BBOX_LEFT
        val bboxHeight = LYRAFY_BBOX_BOTTOM - LYRAFY_BBOX_TOP
        val scale = minOf(size.width / bboxWidth, size.height / bboxHeight) * 0.92f
        val centerSourceX = (LYRAFY_BBOX_LEFT + LYRAFY_BBOX_RIGHT) * 0.5f
        val centerSourceY = (LYRAFY_BBOX_TOP + LYRAFY_BBOX_BOTTOM) * 0.5f
        val targetCenterX = size.width * 0.5f
        val targetCenterY = size.height * 0.5f

        withTransform({
            translate(
                left = targetCenterX - centerSourceX * scale,
                top = targetCenterY - centerSourceY * scale,
            )
            scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
        }) {
            drawLyrafyPaths(paths = l3)
            drawLyrafyPaths(paths = l1)
            drawLyrafyPaths(paths = l2)
            drawLyrafyPaths(paths = l4)
        }
    }
}

/**
 * Custom animated SVG loading indicator (`LyrafyLogoLoadingIndicator`) that replaces generic
 * `CircularProgressIndicator` spinners across the app:
 * 1. Golden Orbit Ring (`l3`): Continuous sweep / shimmer pulse (`0f..360f` over `2000ms`, `LinearEasing`).
 * 2. Three Harp Strings (`l2`): Alternating opacity pulse (`0.4f <-> 1.0f`) giving a gentle vibrating string effect.
 * 3. Golden Stars (`l4`): Twinkling scale & opacity (`0.7f <-> 1.15f`, `Reverse`) around each star's pivot.
 */
@Composable
fun LyrafyLogoLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
) {
    val l1 = remember { LyrafyParsedLayers.layer1 }
    val l2 = remember { LyrafyParsedLayers.layer2 }
    val l3 = remember { LyrafyParsedLayers.layer3 }
    val l4 = remember { LyrafyParsedLayers.layer4 }

    val infiniteTransition = rememberInfiniteTransition(label = "lyrafy_logo_loading")

    val orbitPhaseDeg = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbit_phase_deg",
    )

    val stringWavePhase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "string_wave_phase",
    )

    val starTwinkleScale = infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "star_twinkle_scale",
    )

    Canvas(
        modifier = modifier
            .size(size)
            .testTag("lyrafy_logo_loading_indicator"),
    ) {
        val bboxWidth = LYRAFY_BBOX_RIGHT - LYRAFY_BBOX_LEFT
        val bboxHeight = LYRAFY_BBOX_BOTTOM - LYRAFY_BBOX_TOP
        val scale = minOf(this.size.width / bboxWidth, this.size.height / bboxHeight) * 0.94f
        val centerSourceX = (LYRAFY_BBOX_LEFT + LYRAFY_BBOX_RIGHT) * 0.5f
        val centerSourceY = (LYRAFY_BBOX_TOP + LYRAFY_BBOX_BOTTOM) * 0.5f
        val targetCenterX = this.size.width * 0.5f
        val targetCenterY = this.size.height * 0.5f

        val phaseRad = orbitPhaseDeg.value * (PI.toFloat() / 180f)
        val orbitPulseAlpha = (0.72f + 0.28f * sin(phaseRad)).coerceIn(0.45f, 1f)

        withTransform({
            translate(
                left = targetCenterX - centerSourceX * scale,
                top = targetCenterY - centerSourceY * scale,
            )
            scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
        }) {
            // 1. Golden Orbit Ring with sweeping shimmer & subtle breathing rotation
            l3.forEachIndexed { idx, item ->
                val subAlpha = (orbitPulseAlpha * (0.82f + 0.18f * cos(phaseRad + idx * 0.45f))).coerceIn(0.35f, 1f)
                withTransform({
                    translate(left = item.translateX, top = item.translateY)
                }) {
                    drawPath(
                        path = item.path,
                        color = item.fillColor.copy(alpha = subAlpha),
                    )
                }
            }

            // Sweep highlight arc along the golden orbit path
            val sweepNorm = (orbitPhaseDeg.value / 360f).coerceIn(0f, 1f)
            val sweepWindowHalf = (LYRAFY_BBOX_RIGHT - LYRAFY_BBOX_LEFT) * 0.28f
            val sweepCenter = LYRAFY_BBOX_LEFT + (LYRAFY_BBOX_RIGHT - LYRAFY_BBOX_LEFT) * sweepNorm
            clipRect(
                left = (sweepCenter - sweepWindowHalf).coerceAtLeast(LYRAFY_BBOX_LEFT - 30f),
                top = LYRAFY_BBOX_TOP - 30f,
                right = (sweepCenter + sweepWindowHalf).coerceAtMost(LYRAFY_BBOX_RIGHT + 30f),
                bottom = LYRAFY_BBOX_BOTTOM + 30f,
            ) {
                l3.forEach { item ->
                    withTransform({
                        translate(left = item.translateX, top = item.translateY)
                    }) {
                        drawPath(
                            path = item.path,
                            color = Color(0xFFFFE599).copy(alpha = 0.55f),
                        )
                    }
                }
            }

            // Harp body & musical note
            drawLyrafyPaths(paths = l1, alphaMultiplier = 0.96f)

            // 2. Three Vertical Strings pulsing alternately (0.4f <-> 1.0f)
            l2.forEachIndexed { index, stringItem ->
                val staggerOffset = index * (2f * PI.toFloat() / 3f)
                val wave = (sin(stringWavePhase.value + staggerOffset) + 1f) * 0.5f
                val stringAlpha = (0.4f + 0.6f * wave).coerceIn(0.4f, 1f)
                withTransform({
                    translate(left = stringItem.translateX, top = stringItem.translateY)
                }) {
                    drawPath(
                        path = stringItem.path,
                        color = stringItem.fillColor.copy(alpha = stringAlpha),
                    )
                }
            }

            // 3. Golden Stars twinkling (scale 0.7f <-> 1.15f & alpha pulse)
            l4.forEachIndexed { index, starItem ->
                val isEven = index % 2 == 0
                val rawScale = starTwinkleScale.value
                val itemScale = if (isEven) rawScale else (1.85f - rawScale).coerceIn(0.7f, 1.15f)
                val itemAlpha = ((itemScale - 0.55f) / 0.6f).coerceIn(0.5f, 1f)
                withTransform({
                    scale(
                        scaleX = itemScale,
                        scaleY = itemScale,
                        pivot = Offset(starItem.pivotX, starItem.pivotY),
                    )
                    translate(left = starItem.translateX, top = starItem.translateY)
                }) {
                    drawPath(
                        path = starItem.path,
                        color = starItem.fillColor.copy(alpha = itemAlpha),
                    )
                }
            }
        }
    }
}

