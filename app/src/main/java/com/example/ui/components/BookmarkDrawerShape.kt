package com.example.ui.components

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class BookmarkDrawerShape(
    private val protrusionPx: Float,
    private val tabTopPx: Float,
    private val tabHeightPx: Float,
    private val filletRadiusPx: Float,
    private val sheetCornerRadiusPx: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()

        val sheetLeft = protrusionPx
        val sheetRight = size.width
        val sheetTop = 0f
        val sheetBottom = size.height

        val tabTop = tabTopPx
        val tabBottom = tabTop + tabHeightPx
        val tabLeft = 0f
        val r = filletRadiusPx.coerceAtMost(protrusionPx / 2f)
        val cr = sheetCornerRadiusPx

        // Start top-right
        path.moveTo(sheetRight, sheetTop)
        path.lineTo(sheetRight, sheetBottom)

        // Bottom edge to bottom-left corner
        path.lineTo(sheetLeft + cr, sheetBottom)
        path.quadraticTo(sheetLeft, sheetBottom, sheetLeft, sheetBottom - cr)

        // Up along sheetLeft towards tab bottom
        path.lineTo(sheetLeft, tabBottom + r)
        // Fillet into notch bottom
        path.quadraticTo(sheetLeft, tabBottom, sheetLeft - r, tabBottom)
        path.lineTo(tabLeft + r, tabBottom)
        path.quadraticTo(tabLeft, tabBottom, tabLeft, tabBottom - r)

        // Up along outer edge of tab notch
        path.lineTo(tabLeft, tabTop + r)
        path.quadraticTo(tabLeft, tabTop, tabLeft + r, tabTop)
        path.lineTo(sheetLeft - r, tabTop)
        // Fillet back into sheet left
        path.quadraticTo(sheetLeft, tabTop, sheetLeft, tabTop - r)

        // Up to top-left corner
        path.lineTo(sheetLeft, sheetTop + cr)
        path.quadraticTo(sheetLeft, sheetTop, sheetLeft + cr, sheetTop)

        // Top edge back to start
        path.lineTo(sheetRight, sheetTop)
        path.close()

        return Outline.Generic(path)
    }
}
