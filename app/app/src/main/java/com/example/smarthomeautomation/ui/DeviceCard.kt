package com.example.smarthomeautomation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.smarthomeautomation.data.Device
import com.example.smarthomeautomation.data.DeviceState
import com.example.smarthomeautomation.data.MultiUnit
import com.example.smarthomeautomation.data.SafetyCritical
import com.example.smarthomeautomation.data.SingleUnit
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceCard(
    device: Device,
    onToggle: (Int) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    parentOn: Boolean = true,
    onBodyClick: (Int) -> Unit = {},
    onDelete: (Int) -> Unit = {},
    small: Boolean = false,
    deleteable: Boolean = false,
    enabled: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val rawIsOn = device.state == DeviceState.ON
    val effectiveIsOn = rawIsOn && parentOn
    val isInteractive =
        enabled && device.state != DeviceState.ERROR && device.state != DeviceState.DISCONNECTED

    val onGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF818CF8).copy(alpha = 0.25f),
            Color(0xFF304FFE).copy(alpha = 0.25f)
        )
    )

    val cardBgColor = when {
        effectiveIsOn -> Color.Transparent
        device.state == DeviceState.ERROR -> Color.Red.copy(alpha = 0.15f)
        device.state == DeviceState.DISCONNECTED -> Color.Yellow.copy(alpha = 0.1f)
        else -> Color.Transparent
    }

    val deviceIcon = when (device) {
        is SafetyCritical -> Icons.Default.Shield
        is MultiUnit -> Icons.Default.Devices
        is SingleUnit -> Icons.Default.Power
        else -> Icons.Default.Power
    }

    val shapeRadius = if (small) 16.dp else 24.dp
    val contentPadding = if (small) 10.dp else 16.dp

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xEE1E1E2E),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Delete Device",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${device.name}?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(device.deviceID)
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    onBodyClick(device.deviceID)
                },
                onLongClick = if (!small && deleteable) {
                    { showDeleteDialog = true }
                } else null
            ),
        shape = RoundedCornerShape(shapeRadius),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(shapeRadius))
                .hazeChild(
                    state = hazeState,
                    shape = RoundedCornerShape(shapeRadius),
                    style = HazeDefaults.style(
                        blurRadius = 24.dp,
                        backgroundColor = Color.Transparent,
                        tint = Color.White.copy(alpha = 0.05f)
                    )
                )
                .background(if (effectiveIsOn) onGradient else SolidColor(cardBgColor))
                .border(
                    width = 0.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(0.2f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(shapeRadius)
                )
                .padding(contentPadding)
        ) {
            if (small) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = device.name.ifBlank { "Unnamed" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (device is MultiUnit) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                                contentDescription = "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(16.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { isExpanded = !isExpanded }
                            )
                        }
                    }

                    Switch(
                        checked = rawIsOn,
                        onCheckedChange = { onToggle(device.deviceID) },
                        enabled = isInteractive,
                        modifier = Modifier.scale(0.7f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981),
                            uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (effectiveIsOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = deviceIcon,
                                contentDescription = null,
                                tint = if (effectiveIsOn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Switch(
                        checked = rawIsOn,
                        onCheckedChange = { onToggle(device.deviceID) },
                        enabled = isInteractive,
                        modifier = Modifier.scale(0.8f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981),
                            uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = device.name.ifBlank { "Unnamed" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (device is MultiUnit) {
                                    Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { isExpanded = !isExpanded }
                                } else {
                                    Modifier
                                }
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StateBadge(state = if (parentOn) device.state else DeviceState.OFF)

                        if (device is MultiUnit) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                                contentDescription = "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (device is SafetyCritical) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${device.maxOnDuration}s limit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (device is MultiUnit) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (small) 8.dp else 12.dp)
                    ) {
                        HorizontalDivider(
                            color = if (effectiveIsOn) Color.Black.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val chunkedSubUnits = device.subUnits.chunked(2)
                        chunkedSubUnits.forEach { pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Max)
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DeviceCard(
                                    device = pair[0],
                                    onToggle = onToggle,
                                    hazeState = hazeState,
                                    parentOn = effectiveIsOn,
                                    onBodyClick = onBodyClick,
                                    small = small,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                                if (pair.size > 1) {
                                    DeviceCard(
                                        device = pair[1],
                                        onToggle = onToggle,
                                        hazeState = hazeState,
                                        parentOn = effectiveIsOn,
                                        onBodyClick = onBodyClick,
                                        small = small,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun StateBadge(state: DeviceState) {
    val (badgeColor, textColor) = when (state) {
        DeviceState.ON -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        DeviceState.OFF -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        DeviceState.ERROR -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        DeviceState.DISCONNECTED -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = badgeColor
    ) {
        Text(
            text = state.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}
