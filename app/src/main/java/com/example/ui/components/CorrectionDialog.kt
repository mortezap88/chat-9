package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CorrectionType
import com.example.data.LinguisticCorrection

@Composable
fun CorrectionDialog(
    correction: LinguisticCorrection,
    onApply: (LinguisticCorrection) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (correction.type == CorrectionType.GRAMMAR) Color(0xFFEF4444).copy(alpha = 0.15f)
                            else Color(0xFF3B82F6).copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (correction.type == CorrectionType.GRAMMAR) "Grammar Suggestion" else "Word Choice",
                        color = if (correction.type == CorrectionType.GRAMMAR) Color(0xFFDC2626) else Color(0xFF2563EB),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column {
                    Text("Original:", fontSize = 11.sp, color = Color(0xFF71717A), fontWeight = FontWeight.SemiBold)
                    Text(
                        text = correction.original,
                        fontSize = 14.sp,
                        color = Color(0xFFEF4444),
                        textDecoration = TextDecoration.LineThrough,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column {
                    Text("Suggested:", fontSize = 11.sp, color = Color(0xFF71717A), fontWeight = FontWeight.SemiBold)
                    Text(
                        text = correction.suggested,
                        fontSize = 15.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text("Reason:", fontSize = 11.sp, color = Color(0xFF71717A), fontWeight = FontWeight.SemiBold)
                    Text(
                        text = correction.reason,
                        fontSize = 12.5.sp,
                        color = Color(0xFF3F3F46),
                        lineHeight = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(correction) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18181B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.padding(end = 4.dp))
                Text("Apply Fix", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ignore", color = Color(0xFF71717A))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
