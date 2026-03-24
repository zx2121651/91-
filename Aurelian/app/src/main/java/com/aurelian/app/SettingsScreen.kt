package com.aurelian.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var stealthMode by remember { mutableStateOf(false) }
    var netWorthVerified by remember { mutableStateOf(true) }
    var globalRoam by remember { mutableStateOf(false) }
    var ageRange by remember { mutableStateOf(25f..35f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("偏好设置", color = Gold, style = Typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Gold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF131313),
                titleContentColor = Gold
            )
        )

        HorizontalDivider(color = Color(0xFF303030), thickness = 1.dp)

        // Section: Discover Preferences
        SettingsSectionTitle("匹配门槛")
        SettingsSliderItem("年龄范围", "${ageRange.start.toInt()} - ${ageRange.endInclusive.toInt()} 岁", ageRange) { ageRange = it }
        SettingsDropdownItem("最低学历要求", "硕士及以上")
        SettingsDropdownItem("行业/领域偏好", "金融 / 艺术 / 科技")

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Privacy & Verification
        SettingsSectionTitle("隐私与资产")
        SettingsSwitchItem("无痕隐身模式 (The Royal 会员专享)", stealthMode, "浏览他人时不留下访客记录") { stealthMode = it }
        SettingsSwitchItem("全球漫游模式", globalRoam, "跨国社交，随时切换城市") { globalRoam = it }

        Spacer(modifier = Modifier.height(16.dp))

        // Verification Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .background(Color(0xFF1B1B1B), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("净资产验证状态", color = Silver, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(if (netWorthVerified) "已认证" else "未认证", color = if (netWorthVerified) Gold else Color.Gray, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("您的流动资产评估报告已通过由瑞银集团 (UBS) 提供的联合审核。", color = Color.Gray, fontSize = 12.sp, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A), contentColor = Gold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("更新资产证明")
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = Gold,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 24.dp, top = 32.dp, bottom = 16.dp)
    )
}

@Composable
fun SettingsSwitchItem(title: String, checked: Boolean, subtitle: String? = null, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Silver, fontSize = 16.sp)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Black,
                checkedTrackColor = Gold,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF303030)
            )
        )
    }
}

@Composable
fun SettingsDropdownItem(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = Silver, fontSize = 16.sp)
        Text(value, color = Gold, fontSize = 16.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSliderItem(title: String, value: String, range: ClosedFloatingPointRange<Float>, onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Silver, fontSize = 16.sp)
            Text(value, color = Gold, fontSize = 16.sp)
        }
        RangeSlider(
            value = range,
            onValueChange = onRangeChange,
            valueRange = 18f..60f,
            colors = SliderDefaults.colors(
                thumbColor = Gold,
                activeTrackColor = Gold,
                inactiveTrackColor = Color(0xFF303030)
            ),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
