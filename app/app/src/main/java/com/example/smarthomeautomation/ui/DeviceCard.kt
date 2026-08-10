package com.example.smarthomeautomation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.smarthomeautomation.data.Device
import com.example.smarthomeautomation.data.DeviceState
import com.example.smarthomeautomation.data.MultiUnit
import com.example.smarthomeautomation.data.SafetyCritical
import com.example.smarthomeautomation.data.SingleUnit

@Composable
fun DeviceCard(
    device: Device,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    parentOn: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rawIsOn = device.state == DeviceState.ON
    val effectiveIsOn = rawIsOn && parentOn
    val isInteractive =
        device.state != DeviceState.ERROR && device.state != DeviceState.DISCONNECTED

    val onGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF00FF87), Color(0xFF60EFFF))
    )
    val cardBgColor = when {
        effectiveIsOn -> Color.Transparent
        device.state == DeviceState.ERROR -> Color.Red
        device.state == DeviceState.DISCONNECTED -> Color.Yellow
        else -> Color.Transparent
    }

    val deviceIcon = when (device) {
        is SafetyCritical -> Icons.Default.Shield
        is MultiUnit -> Icons.Default.Devices
        is SingleUnit -> Icons.Default.Power
        else -> Icons.Default.Power
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (device is MultiUnit) Modifier.clickable {
                    isExpanded = !isExpanded
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (!effectiveIsOn) BorderStroke(1.dp, Color.Gray) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .then(
                    if (effectiveIsOn) Modifier.background(onGradient) else Modifier
                )
                .padding(12.dp)
        ) {
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
                        checkedThumbColor = Color.Green,
                        checkedTrackColor = Color.DarkGray,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.LightGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = device.name.ifBlank { "Unnamed" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (effectiveIsOn) Color.Black else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
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

            if (device is MultiUnit) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
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
                                    parentOn = effectiveIsOn,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                                if (pair.size > 1) {
                                    DeviceCard(
                                        device = pair[1],
                                        onToggle = onToggle,
                                        parentOn = effectiveIsOn,
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
