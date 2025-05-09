package ui.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(chatUserId: String) {
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""
    val db = FirebaseFirestore.getInstance()

    var messages by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    val scrollState = rememberScrollState()

    val dateFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    // Theme options and selected theme
    val themeOptions = listOf(
        "Default" to Color(0xFFFFFFFF),
        "Light Blue" to Color(0xFFE0F7FA),
        "Pink" to Color(0xFFF8BBD0),
        "Light Green" to Color(0xFFC8E6C9),
        "Gray" to Color(0xFFCFD8DC)
    )
    var selectedTheme by remember { mutableStateOf(themeOptions[0]) }
    var isMenuExpanded by remember { mutableStateOf(false) }

    // Fetch messages from Firestorm
    LaunchedEffect(Unit) {
        db.collection("chats")
            .document(getChatId(currentUserId, chatUserId))
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messages = snapshot.documents.map { it.data ?: mapOf() }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat", fontSize = 24.sp) },
                actions = {
                    IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Theme Menu",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        themeOptions.forEach { theme ->
                            DropdownMenuItem(
                                text = { Text(theme.first) },
                                onClick = {
                                    selectedTheme = theme
                                    isMenuExpanded = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF128C7E))
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(selectedTheme.second)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    messages.forEach { message ->
                        val isSentByCurrentUser = message["senderId"] == currentUserId
                        val alignment = if (isSentByCurrentUser) Alignment.CenterEnd else Alignment.CenterStart

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = alignment
                        ) {
                            Card(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .widthIn(max = 240.dp),
                                colors = if (isSentByCurrentUser) CardDefaults.cardColors(containerColor = Color(0xFFDCF8C6))
                                else CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = message["text"] as String,
                                        color = if (isSentByCurrentUser) Color.Black else Color.Gray,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = dateFormatter.format(Date(message["timestamp"] as Long)),
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BasicTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White, shape = MaterialTheme.shapes.small)
                            .padding(8.dp),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (messageText.text.isNotBlank()) {
                                val message = mapOf(
                                    "text" to messageText.text,
                                    "senderId" to currentUserId,
                                    "receiverId" to chatUserId,
                                    "timestamp" to System.currentTimeMillis()
                                )
                                db.collection("chats")
                                    .document(getChatId(currentUserId, chatUserId))
                                    .collection("messages")
                                    .add(message)
                                messageText = TextFieldValue("")
                            }
                        }
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    )
}

fun getChatId(userId1: String, userId2: String): String {
    return if (userId1 < userId2) "$userId1-$userId2" else "$userId2-$userId1"
}
