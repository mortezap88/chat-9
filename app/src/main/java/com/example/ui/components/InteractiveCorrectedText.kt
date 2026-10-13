package com.example.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.example.data.CorrectionType
import com.example.data.LinguisticCorrection

@Composable
fun InteractiveCorrectedText(
    text: String,
    corrections: List<LinguisticCorrection>,
    fontSizeSp: Int,
    onCorrectionClick: (LinguisticCorrection) -> Unit,
    modifier: Modifier = Modifier
) {
    if (corrections.isEmpty()) {
        Text(
            text = text,
            color = Color(0xFFF4F4F5),
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp * 1.45f).sp,
            fontWeight = FontWeight.Normal,
            modifier = modifier
        )
        return
    }

    val annotatedString = buildAnnotatedString {
        append(text)
        corrections.forEach { corr ->
            val startIdx = text.indexOf(corr.original)
            if (startIdx >= 0) {
                val endIdx = startIdx + corr.original.length
                val highlightColor = if (corr.type == CorrectionType.GRAMMAR) Color(0xFFF87171) else Color(0xFF60A5FA)
                addStyle(
                    style = SpanStyle(
                        color = highlightColor,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.SemiBold
                    ),
                    start = startIdx,
                    end = endIdx
                )
                addStringAnnotation(
                    tag = "CORRECTION",
                    annotation = corr.id,
                    start = startIdx,
                    end = endIdx
                )
            }
        }
    }

    ClickableText(
        text = annotatedString,
        style = TextStyle(
            color = Color(0xFFF4F4F5),
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp * 1.45f).sp,
            fontWeight = FontWeight.Normal
        ),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "CORRECTION", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    val corr = corrections.find { it.id == annotation.item }
                    if (corr != null) {
                        onCorrectionClick(corr)
                    }
                }
        },
        modifier = modifier
    )
}
