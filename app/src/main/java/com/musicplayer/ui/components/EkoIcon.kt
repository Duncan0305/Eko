package com.musicplayer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.musicplayer.R
import com.musicplayer.ui.theme.*

enum class Symbol {
    Search, Folder, Refresh, Sort, More, Close, Arrow, Back, Info, Check, Settings, Music,
    Play, Pause, Next, Previous, Shuffle, Repeat, RepeatOne, Queue
}

@Composable
fun EkoIcon(symbol: Symbol, modifier: Modifier = Modifier, description: String? = null,
            tint: Color = LocalContentColor.current) {
    Canvas(modifier.size(24.dp).semantics { if (description != null) contentDescription = description }) {
        scale(size.width / 24f, size.height / 24f, pivot = Offset.Zero) {
            fun line(x: Float, y: Float, xx: Float, yy: Float) = drawLine(tint, Offset(x,y), Offset(xx,yy), 1.7f, StrokeCap.Round)
            when (symbol) {
                Symbol.Search -> { drawCircle(tint, 6.5f, Offset(10.5f,10.5f), style = Stroke(1.7f)); line(15.5f,15.5f,21f,21f) }
                Symbol.Folder -> {
                    val path = Path().apply { moveTo(3f,6f); lineTo(9f,6f); lineTo(11f,8f); lineTo(21f,8f); lineTo(21f,19f); lineTo(3f,19f); close() }
                    drawPath(path, tint, style = Stroke(1.7f, join = StrokeJoin.Round))
                }
                Symbol.Refresh -> { drawArc(tint, 35f, 285f, false, Offset(4f,4f), Size(16f,16f), style=Stroke(1.7f, cap=StrokeCap.Round)); line(20f,3f,20f,9f); line(15f,9f,20f,9f) }
                Symbol.Sort -> { line(4f,6f,20f,6f); line(4f,12f,15f,12f); line(4f,18f,10f,18f) }
                Symbol.More -> { listOf(5f,12f,19f).forEach { drawCircle(tint,1.6f,Offset(12f,it)) } }
                Symbol.Close -> { line(6f,6f,18f,18f); line(6f,18f,18f,6f) }
                Symbol.Arrow -> { line(4f,12f,20f,12f); line(14f,6f,20f,12f); line(14f,18f,20f,12f) }
                Symbol.Back -> { line(20f,12f,4f,12f); line(10f,6f,4f,12f); line(10f,18f,4f,12f) }
                Symbol.Info -> { drawCircle(tint,9f,Offset(12f,12f),style=Stroke(1.7f)); line(12f,11f,12f,17f); drawCircle(tint,1f,Offset(12f,7f)) }
                Symbol.Check -> { line(5f,12f,10f,17f); line(10f,17f,20f,7f) }
                Symbol.Settings -> {
                    listOf(6f,12f,18f).forEach { line(4f,it,20f,it) }
                    listOf(Offset(9f,6f),Offset(16f,12f),Offset(8f,18f)).forEach {
                        drawCircle(EkoBackground,2.4f,it); drawCircle(tint,2.4f,it,style=Stroke(1.7f))
                    }
                }
                Symbol.Music -> { line(10f,6f,10f,17f); line(10f,6f,19f,4f); line(19f,4f,19f,15f); drawOval(tint,Offset(4f,15f),Size(6f,5f)); drawOval(tint,Offset(13f,13f),Size(6f,5f)) }
                Symbol.Play -> {
                    val path = Path().apply { moveTo(8f,5f); lineTo(19f,12f); lineTo(8f,19f); close() }
                    drawPath(path, tint)
                }
                Symbol.Pause -> { drawRoundRect(tint, Offset(7f,5f), Size(3.5f,14f), CornerRadius(1.2f)); drawRoundRect(tint, Offset(13.5f,5f), Size(3.5f,14f), CornerRadius(1.2f)) }
                Symbol.Next -> {
                    val path = Path().apply { moveTo(5f,5f); lineTo(15f,12f); lineTo(5f,19f); close() }
                    drawPath(path, tint); line(18f,5f,18f,19f)
                }
                Symbol.Previous -> {
                    val path = Path().apply { moveTo(19f,5f); lineTo(9f,12f); lineTo(19f,19f); close() }
                    drawPath(path, tint); line(6f,5f,6f,19f)
                }
                Symbol.Shuffle -> {
                    line(4f,7f,8f,7f); line(8f,7f,16f,17f); line(16f,17f,20f,17f)
                    line(16f,14f,20f,17f); line(16f,20f,20f,17f)
                    line(4f,17f,8f,17f); line(8f,17f,16f,7f); line(16f,7f,20f,7f)
                    line(16f,4f,20f,7f); line(16f,10f,20f,7f)
                }
                Symbol.Repeat, Symbol.RepeatOne -> {
                    line(6f,7f,18f,7f); line(15f,4f,18f,7f); line(15f,10f,18f,7f)
                    line(18f,17f,6f,17f); line(9f,14f,6f,17f); line(9f,20f,6f,17f)
                    line(5f,8f,4f,10f); line(4f,10f,4f,12f); line(19f,16f,20f,14f); line(20f,14f,20f,12f)
                    if (symbol == Symbol.RepeatOne) drawContext.canvas.nativeCanvas.drawText("1", 10.3f, 14.8f,
                        android.graphics.Paint().apply { color = tint.toArgb(); textSize = 8f; isFakeBoldText = true })
                }
                Symbol.Queue -> { line(4f,6f,16f,6f); line(4f,12f,16f,12f); line(4f,18f,12f,18f); drawCircle(tint, 2f, Offset(19f,18f)); line(19f,13f,19f,18f) }
            }
        }
    }
}

@Composable
fun EkoMark(modifier: Modifier = Modifier.size(36.dp)) {
    Image(
        painter = painterResource(R.mipmap.ic_launcher),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(CircleShape)
    )
}
