package ai.vannmrtnz.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VannMrtnzApp() }
    }
}

private val Bg = Color(0xFF090A12)
private val Panel = Color(0xFF141625)
private val Purple = Color(0xFF9B7BFF)
private val Cyan = Color(0xFF63E6E2)
private val TextMain = Color(0xFFF3F3FA)
private val TextMuted = Color(0xFFA5A8BD)

data class ChatMessage(val role: String, val content: String)

@Composable
fun VannMrtnzApp() {
    val messages = remember {
        mutableStateListOf(
            ChatMessage("assistant", "Halo! Gue VannMrtnz.AI 🤖✨\nAda yang bisa gue bantu hari ini?")
        )
    }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Purple,
            secondary = Cyan,
            background = Bg,
            surface = Panel
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(Bg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Panel).padding(horizontal = 18.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(Purple.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = Cyan, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("VannMrtnz.AI", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                    Text("AI ASSISTANT  •  ONLINE UI", color = Cyan, fontSize = 10.sp, letterSpacing = 1.2.sp)
                }
                IconButton(onClick = {
                    messages.clear()
                    messages.add(ChatMessage("assistant", "Chat baru dimulai. Gas, mau bahas apa? 🚀"))
                    error = null
                }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus chat", tint = TextMuted)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    MessageBubble(msg)
                }
                if (loading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Surface(color = Panel, shape = RoundedCornerShape(16.dp)) {
                                Text("VannMrtnz.AI sedang mikir…", color = Cyan, modifier = Modifier.padding(14.dp), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            error?.let {
                Text(it, color = Color(0xFFFF8A8A), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth().background(Panel).padding(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Tanya apa aja…", color = TextMuted) },
                    shape = RoundedCornerShape(22.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextMain,
                        unfocusedTextColor = TextMain,
                        focusedBorderColor = Purple,
                        unfocusedBorderColor = Color(0xFF33364B),
                        cursorColor = Cyan,
                        focusedContainerColor = Bg,
                        unfocusedContainerColor = Bg
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (!loading && input.isNotBlank()) {
                            val text = input.trim()
                            input = ""
                            sendMessage(text, messages, { loading = it }, { error = it }, scope) {
                                scope.launch {
                                    listState.animateScrollToItem(messages.lastIndex.coerceAtLeast(0))
                                }
                            }
                        }
                    })
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        val text = input.trim()
                        if (text.isNotEmpty() && !loading) {
                            input = ""
                            sendMessage(text, messages, { loading = it }, { error = it }, scope) {
                                scope.launch {
                                    listState.animateScrollToItem(messages.lastIndex.coerceAtLeast(0))
                                }
                            }
                        }
                    },
                    enabled = input.isNotBlank() && !loading,
                    modifier = Modifier.size(52.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Purple, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Kirim")
                }
            }
            Text(
                "Jawaban AI bisa keliru. Periksa info penting.",
                color = TextMuted,
                fontSize = 10.sp,
                modifier = Modifier.fillMaxWidth().background(Panel).padding(bottom = 9.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 320.dp)
                .background(if (isUser) Color(0xFF34275A) else Panel, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            Text(
                if (isUser) "LO" else "VANNMRTNZZ.AI",
                color = if (isUser) Cyan else Purple,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(5.dp))
            Text(message.content, color = TextMain, fontSize = 14.sp, lineHeight = 21.sp)
        }
    }
}

private fun sendMessage(
    text: String,
    messages: MutableList<ChatMessage>,
    setLoading: (Boolean) -> Unit,
    setError: (String?) -> Unit,
    scope: kotlinx.coroutines.CoroutineScope,
    onFinished: () -> Unit
) {
    messages.add(ChatMessage("user", text))
    setLoading(true)
    setError(null)

    scope.launch {
        try {
            val reply = withContext(Dispatchers.IO) {
                requestAi(messages.toList())
            }
            messages.add(ChatMessage("assistant", reply))
        } catch (e: Exception) {
            setError(e.message ?: "Terjadi kesalahan saat menghubungi server.")
        } finally {
            setLoading(false)
            onFinished()
        }
    }
}

private fun requestAi(messages: List<ChatMessage>): String {
    val baseUrl = BuildConfig.API_BASE_URL.trimEnd('/') + "/"
    if (baseUrl.contains("YOUR-BACKEND-URL")) {
        throw IOException("Backend belum diatur. Edit API_BASE_URL di app/build.gradle.kts lalu build ulang.")
    }

    val array = JSONArray()
    messages.takeLast(24).forEach { msg ->
        array.put(JSONObject().put("role", msg.role).put("content", msg.content))
    }
    val json = JSONObject().put("messages", array).toString()
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(100, TimeUnit.SECONDS)
        .callTimeout(110, TimeUnit.SECONDS)
        .build()

    val builder = Request.Builder()
        .url(baseUrl + "chat")
        .post(json.toRequestBody(mediaType))
        .header("Accept", "application/json")
    if (BuildConfig.API_TOKEN.isNotBlank()) {
        builder.header("X-App-Token", BuildConfig.API_TOKEN)
    }

    client.newCall(builder.build()).execute().use { response ->
        val body = response.body?.string().orEmpty()
        val data = try { JSONObject(body) } catch (_: Exception) { JSONObject() }
        if (!response.isSuccessful) {
            throw IOException(data.optString("error", "Server error (${response.code})"))
        }
        return data.optString("reply").ifBlank { throw IOException("Server tidak mengirim balasan.") }
    }
}
