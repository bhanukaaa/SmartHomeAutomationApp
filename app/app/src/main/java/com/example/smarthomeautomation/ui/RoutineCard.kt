//package com.example.smarthomeautomation.ui
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.expandVertically
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.shrinkVertically
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.HorizontalDivider
//import androidx.compose.material3.Icon
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Power
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Switch
//import androidx.compose.material3.SwitchDefaults
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.example.smarthomeautomation.data.Device
//import com.example.smarthomeautomation.data.DeviceState
//import com.example.smarthomeautomation.data.MultiUnit
//import com.example.smarthomeautomation.data.Routine
//import com.example.smarthomeautomation.data.RoutineState
//import com.example.smarthomeautomation.data.SafetyCritical
//import com.example.smarthomeautomation.data.SingleUnit
//
//@Composable
//fun RoutineCard(
//    routine: Routine,
//    onToggle: (Int) -> Unit,
//    modifier: Modifier = Modifier,
//    parentOn: Boolean = true
//) {
//    var isExpanded by remember { mutableStateOf(false) }
//    val isOn = routine.state == RoutineState.ON
//
//    val cardBgColor = when {
//        isOn && parentOn -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
//        else -> MaterialTheme.colorScheme.surface
//    }
//
//    val routineIcon = Icons.Default.Power
//
//    Card(
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 6.dp)
//            .clip(RoundedCornerShape(16.dp)),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(containerColor = cardBgColor),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Surface(
//                    shape = RoundedCornerShape(12.dp),
//                    color = if (isOn && parentOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
//                    modifier = Modifier.size(44.dp)
//                ) {
//                    Box(contentAlignment = Alignment.Center) {
//                        Icon(
//                            imageVector = routineIcon,
//                            contentDescription = null,
//                            tint = if (isOn && parentOn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
//                            modifier = Modifier.size(24.dp)
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.width(12.dp))
//
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = routine.name.ifBlank { "Unnamed Routine" },
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold,
//                        color = MaterialTheme.colorScheme.onSurface
//                    )
//                }
//
//                Switch(
//                    checked = isOn,
//                    onCheckedChange = { onToggle(routine.routineID) },
//                    colors = SwitchDefaults.colors(
//                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
//                        checkedTrackColor = MaterialTheme.colorScheme.primary
//                    )
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun StateBadge(state: RoutineState) {
//    val (badgeColor, textColor) = when (state) {
//        RoutineState.ON -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
//        RoutineState.OFF -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
//    }
//    Surface(
//        shape = RoundedCornerShape(6.dp),
//        color = badgeColor
//    ) {
//        Text(
//            text = state.name,
//            style = MaterialTheme.typography.labelSmall,
//            fontWeight = FontWeight.SemiBold,
//            color = textColor,
//            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
//        )
//    }
//}