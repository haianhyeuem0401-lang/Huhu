package vn.qbao.tweak

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.StatFs
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

private val Bg = Color(0xFF080B18)
private val Panel = Color(0xFF131A2E)
private val Neon = Color(0xFF63FFB0)
private val Cyan = Color(0xFF65D9FF)
private val Purple = Color(0xFFB68CFF)
private val Muted = Color(0xFFABB5D0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { QBaoApp(this) }
    }
}

private enum class Screen { KEY, HOME, SENSI, OPTIMIZE, GAME, VIP, ADMIN }

@Composable
private fun QBaoApp(context: Context) {
    var screen by remember { mutableStateOf(Screen.KEY) }
    var key by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var selectedGame by remember { mutableStateOf("Free Fire") }
    var vipStarted by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Neon, secondary = Cyan, background = Bg,
            surface = Panel, onBackground = Color.White, onSurface = Color.White
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0xFF1B1640), Bg, Color(0xFF061B24)))
            )
        ) {
            when (screen) {
                Screen.KEY -> KeyScreen(
                    key = key, onKey = { key = it }, busy = busy, message = message,
                    onVerify = {
                        if (key.isBlank()) message = "Vui lòng nhập key."
                    },
                    onVerifyAsync = {
                        busy = true
                        message = ""
                        val result = verifyKey(key.trim())
                        busy = false
                        message = result.second
                        if (result.first) screen = Screen.HOME
                    }
                )
                Screen.HOME -> HomeScreen(
                    onSensi = { screen = Screen.SENSI },
                    onOptimize = { screen = Screen.OPTIMIZE },
                    onGame = { screen = Screen.GAME },
                    onVip = { screen = Screen.VIP },
                    onAdmin = { screen = Screen.ADMIN }
                )
                Screen.SENSI -> DetailScreen("NHẸ TÂM", "HƯỚNG DẪN THIẾT LẬP", Icons.Default.Bolt, onBack = { screen = Screen.HOME }) {
                    InfoLine("1", "Mở Cài đặt trong game và điều chỉnh độ nhạy từng mức.")
                    InfoLine("2", "Thử từng thay đổi trong khu tập luyện; giữ lại mức bạn kiểm soát tốt.")
                    InfoLine("3", "Tùy chỉnh HUD và nút bắn theo thói quen của bạn.")
                    InfoLine("Lưu ý", "Đây là hướng dẫn cài đặt hợp lệ, không tự động ngắm hoặc sửa dữ liệu game.")
                }
                Screen.OPTIMIZE -> DetailScreen("TỐI ƯU MÁY", "KIỂM TRA THIẾT BỊ", Icons.Default.Memory, onBack = { screen = Screen.HOME }) {
                    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    val mi = ActivityManager.MemoryInfo()
                    am.getMemoryInfo(mi)
                    val freeRam = mi.availMem / (1024L * 1024L)
                    val totalRam = mi.totalMem / (1024L * 1024L)
                    val stat = StatFs(Environment.getDataDirectory().path)
                    val freeStorage = stat.availableBytes / (1024L * 1024L * 1024L)
                    InfoLine("RAM còn trống", "$freeRam MB / $totalRam MB")
                    InfoLine("Bộ nhớ còn trống", String.format(Locale.getDefault(), "%.1f GB", freeStorage))
                    InfoLine("Gợi ý", "Đóng ứng dụng bạn không dùng, giải phóng dung lượng khi gần đầy và bật chế độ hiệu năng nếu thiết bị hỗ trợ.")
                    InfoLine("Minh bạch", "Ứng dụng không thể đảm bảo tăng FPS 100% trên mọi máy.")
                }
                Screen.GAME -> DetailScreen("CHỌN GAME", "CHỌN PHIÊN BẢN ĐANG CHƠI", Icons.Default.Gamepad, onBack = { screen = Screen.HOME }) {
                    GameChoice("Free Fire", selectedGame == "Free Fire") { selectedGame = "Free Fire" }
                    GameChoice("Free Fire MAX", selectedGame == "Free Fire MAX") { selectedGame = "Free Fire MAX" }
                    Text("Đã chọn: $selectedGame", color = Neon, fontWeight = FontWeight.Bold)
                    Text("Lựa chọn này dùng để cá nhân hóa hướng dẫn; ứng dụng không tự sửa hoặc khởi chạy game.", color = Muted, fontSize = 13.sp)
                }
                Screen.VIP -> VipScreen(selectedGame, onBack = { screen = Screen.HOME }, started = vipStarted, onStart = { vipStarted = true })
                Screen.ADMIN -> DetailScreen("ADMIN CONTROL", "PHẠM GIA BẢO • QUẢN TRỊ VIÊN", Icons.Default.Security, onBack = { screen = Screen.HOME }) {
                    InfoLine("ADMIN", "Phạm Gia Bảo")
                    InfoLine("Quản lý key", "Giao diện quản trị mẫu. Chức năng tạo, gia hạn, khóa và xóa key cần được kết nối với API admin có xác thực.")
                    InfoLine("Bảo mật", "Không đặt mật khẩu quản trị trực tiếp trong ứng dụng. Hãy xác thực quyền admin ở máy chủ.")
                }
            }
        }
    }

    // Trigger verification from the explicit button without blocking Compose.
    // The event is exposed through a small state flag below in the actual screen implementation.
}

@Composable
private fun KeyScreen(
    key: String,
    onKey: (String) -> Unit,
    busy: Boolean,
    message: String,
    onVerify: () -> Unit,
    onVerifyAsync: suspend () -> Unit
) {
    var requested by remember { mutableIntStateOf(0) }
    val pulse = rememberInfiniteTransition(label = "keyPulse")
    val glow by pulse.animateFloat(initialValue = 0.35f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "glow")
    LaunchedEffect(requested) {
        if (requested > 0) onVerifyAsync()
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(92.dp).border(2.dp, Neon.copy(alpha = glow), RoundedCornerShape(26.dp)).background(Panel, RoundedCornerShape(26.dp)), contentAlignment = Alignment.Center) {
            Text("QB", color = Neon, fontSize = 34.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(18.dp))
        Text("QBAOTWEAK X VFT", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text("DYNAMIC CORE  •  PREMIUM ACCESS", color = Neon.copy(alpha = glow), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(28.dp))
        Surface(color = Panel.copy(alpha = .95f), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Cyan, modifier = Modifier.size(34.dp))
                Spacer(Modifier.height(8.dp))
                Text("✦ KEY ACTIVATION ✦", fontWeight = FontWeight.Black, fontSize = 17.sp, color = Purple)
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = key, onValueChange = onKey, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nhập key của bạn") }, singleLine = true,
                    enabled = !busy, shape = RoundedCornerShape(14.dp)
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { if (key.isNotBlank() && !busy) requested++ else onVerify() },
                    enabled = !busy, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = Bg)
                ) {
                    if (busy) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("KÍCH HOẠT QUYỀN TRUY CẬP", fontWeight = FontWeight.Black)
                }
                AnimatedVisibility(message.isNotBlank()) {
                    Text(message, color = if (message.contains("hợp lệ", true)) Neon else Color(0xFFFF8B9A), modifier = Modifier.padding(top = 12.dp), textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(8.dp))
                Text("Kết nối bảo mật qua máy chủ • Không hiển thị địa chỉ server trên màn hình", color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("AIMLOCK  •  SENSI OVERDRIVE  •  FRAME OPTIMIZER", color = Purple, fontSize = 10.sp, textAlign = TextAlign.Center)
        Text("DEVELOPER: PHẠM GIA BẢO", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun HomeScreen(onSensi: () -> Unit, onOptimize: () -> Unit, onGame: () -> Unit, onVip: () -> Unit, onAdmin: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.Center) {
        Text("QBAOTWEAK X VFT", color = Neon, fontSize = 25.sp, fontWeight = FontWeight.Black)
        Text("DYNAMIC CONTROL PANEL", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(22.dp))
        MenuCard("01", "NEON AIMLOCK", "Tùy chọn hỗ trợ ngắm • thiết lập hợp lệ", Icons.Default.Bolt, onSensi)
        MenuCard("02", "SENSI OVERDRIVE", "Độ nhạy • HUD • thao tác kéo", Icons.Default.Bolt, onSensi)
        MenuCard("03", "FRAME OPTIMIZER", "Kiểm tra RAM • bộ nhớ • hiệu năng", Icons.Default.Memory, onOptimize)
        MenuCard("04", "GAME PROFILE", "Free Fire / Free Fire MAX", Icons.Default.Gamepad, onGame)
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onVip, modifier = Modifier.fillMaxWidth()) {
            Text("▶  CHẠY GIAO DIỆN VIP", color = Neon, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onAdmin, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Security, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("ADMIN CONTROL • PHẠM GIA BẢO", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text("Developer: Phạm Gia Bảo  •  QBaoTweak X Vft", color = Muted, fontSize = 11.sp)
        Text("Các mục là giao diện thiết lập và thông tin thiết bị; không sửa dữ liệu game.", color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun MenuCard(number: String, title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(number, color = Neon, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White)
                Text(subtitle, color = Muted, fontSize = 12.sp)
            }
            Icon(icon, contentDescription = null, tint = Cyan)
        }
    }
}

@Composable
private fun DetailScreen(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)) {
        TextButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null); Text("  QUAY LẠI") }
        Spacer(Modifier.height(12.dp))
        Icon(icon, null, tint = Neon, modifier = Modifier.size(38.dp))
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Neon)
        Text(subtitle, color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Surface(color = Panel, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(label.uppercase(), color = Cyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
private fun GameChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFF173A37) else Panel), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            if (selected) Icon(Icons.Default.CheckCircle, null, tint = Neon)
        }
    }
}

@Composable
private fun VipScreen(game: String, onBack: () -> Unit, started: Boolean, onStart: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "greenCode")
    val alpha by transition.animateFloat(initialValue = .35f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "pulse")
    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.Center) {
        TextButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null); Text("  QUAY LẠI") }
        Spacer(Modifier.height(14.dp))
        Text("QBao // VIP CONSOLE", color = Neon, fontSize = 23.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(16.dp))
        Surface(color = Color(0xFF020906), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                val lines = listOf("> QBao Tweak console initialized", "> Profile: VIP UI", "> Target: $game", "> Checking local device status...", "> No game files modified")
                lines.forEach { Text(it, color = Neon.copy(alpha = alpha), fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                if (started) {
                    Spacer(Modifier.height(12.dp))
                    Text("✓ HOÀN TẤT", color = Neon, fontWeight = FontWeight.Black)
                    Text("Bạn có thể mở $game thủ công.", color = Color.White)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = Bg)) {
            Text(if (started) "ĐÃ HOÀN TẤT" else "CHẠY KIỂM TRA", fontWeight = FontWeight.Black)
        }
    }
}

private suspend fun verifyKey(key: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    try {
        val base = BuildConfig.API_BASE_URL.trimEnd('/') + "/"
        require(!base.contains("YOUR-BACKEND-DOMAIN")) { "Chưa cấu hình API_BASE_URL trong app/build.gradle.kts." }
        val conn = (URL(base + "api/check-key").openConnection() as HttpURLConnection)
        conn.requestMethod = "POST"
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        conn.setRequestProperty("Accept", "application/json")
        conn.outputStream.use { it.write(JSONObject().put("key", key).toString().toByteArray(Charsets.UTF_8)) }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        conn.disconnect()
        val json = JSONObject(body)
        val valid = json.optBoolean("valid", false)
        val msg = json.optString("message", if (valid) "Key hợp lệ" else "Key không hợp lệ")
        Pair(valid, msg)
    } catch (e: Exception) {
        Pair(false, when {
            e.message?.contains("Chưa cấu hình") == true -> e.message!!
            else -> "Không kết nối được máy chủ. Kiểm tra mạng hoặc cấu hình API."
        })
    }
}
