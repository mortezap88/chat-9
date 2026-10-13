package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomVirtualKeyboard(
    visible: Boolean,
    text: String,
    onTextChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isShifted by remember { mutableStateOf(false) }
    var isNumbersMode by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onClose() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Color(0xFF18181B))
                    .border(1.dp, Color(0xFF27272A), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .navigationBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Input preview & controls bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF090A0C))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (text.isEmpty()) "Type your thought..." else text,
                        color = if (text.isEmpty()) Color(0xFF71717A) else Color.White,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )

                    if (text.isNotEmpty()) {
                        IconButton(
                            onClick = { onTextChange("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFFA1A1AA), modifier = Modifier.size(16.dp))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (text.isNotBlank()) Color.White else Color(0xFF27272A))
                            .clickable(enabled = text.isNotBlank()) { onSend(text.trim()) }
                            .testTag("keyboard_send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (text.isNotBlank()) Color.Black else Color(0xFF71717A),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Keyboard Layout Rows
                if (!isNumbersMode) {
                    val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
                    val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
                    val row3 = listOf("z", "x", "c", "v", "b", "n", "m")

                    // Row 1
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row1.forEach { key ->
                            KeyButton(
                                label = if (isShifted) key.uppercase() else key,
                                modifier = Modifier.weight(1f),
                                onClick = { onTextChange(text + (if (isShifted) key.uppercase() else key)) }
                            )
                        }
                    }

                    // Row 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        row2.forEach { key ->
                            KeyButton(
                                label = if (isShifted) key.uppercase() else key,
                                modifier = Modifier.weight(1f),
                                onClick = { onTextChange(text + (if (isShifted) key.uppercase() else key)) }
                            )
                        }
                    }

                    // Row 3 (Shift, letters, Backspace)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isShifted) Color.White else Color(0xFF27272A))
                                .clickable { isShifted = !isShifted },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = "Shift",
                                tint = if (isShifted) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        row3.forEach { key ->
                            KeyButton(
                                label = if (isShifted) key.uppercase() else key,
                                modifier = Modifier.weight(1f),
                                onClick = { onTextChange(text + (if (isShifted) key.uppercase() else key)) }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF27272A))
                                .clickable {
                                    if (text.isNotEmpty()) onTextChange(text.dropLast(1))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Backspace,
                                contentDescription = "Backspace",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    // Numbers & Symbols Layout
                    val numRow1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
                    val numRow2 = listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/")
                    val numRow3 = listOf("*", "\"", "'", ":", ";", "!", "?")

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        numRow1.forEach { key ->
                            KeyButton(label = key, modifier = Modifier.weight(1f), onClick = { onTextChange(text + key) })
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        numRow2.forEach { key ->
                            KeyButton(label = key, modifier = Modifier.weight(1f), onClick = { onTextChange(text + key) })
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        numRow3.forEach { key ->
                            KeyButton(label = key, modifier = Modifier.weight(1f), onClick = { onTextChange(text + key) })
                        }
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF27272A))
                                .clickable { if (text.isNotEmpty()) onTextChange(text.dropLast(1)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Backspace, contentDescription = "Backspace", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Bottom Row (?123, comma, space, period, close)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1.4f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF27272A))
                            .clickable { isNumbersMode = !isNumbersMode },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isNumbersMode) "ABC" else "?123", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    KeyButton(label = ",", modifier = Modifier.weight(0.9f), onClick = { onTextChange("$text,") })

                    Box(
                        modifier = Modifier
                            .weight(3.5f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF3F3F46))
                            .clickable { onTextChange("$text ") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("space", color = Color(0xFFA1A1AA), fontSize = 12.sp)
                    }

                    KeyButton(label = ".", modifier = Modifier.weight(0.9f), onClick = { onTextChange("$text.") })

                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF27272A))
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Hide", color = Color(0xFFA1A1AA), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF27272A))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
