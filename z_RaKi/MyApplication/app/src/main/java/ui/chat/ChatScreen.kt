package ui.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.myapplication.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""
    val db = FirebaseFirestore.getInstance()

    var userList by remember { mutableStateOf(listOf<Map<String, String>>()) }
    var filteredUserList by remember { mutableStateOf(listOf<Map<String, String>>()) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var isDarkMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showAnimation by remember { mutableStateOf(false) }

    // Fetch users from Firestorm
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val snapshot = db.collection("users").get().await()
            val users = snapshot.documents.mapNotNull { document ->
                val id = document.id
                val name = document.getString("name")
                if (id != currentUserId && name != null) {
                    mapOf("id" to id, "name" to name)
                } else null
            }
            userList = users
            filteredUserList = users
        } catch (e: Exception) {
            // Handle errors
        } finally {
            isLoading = false
        }
    }

    // Filter users based on the search query
    LaunchedEffect(searchQuery) {
        filteredUserList = if (searchQuery.text.isEmpty()) {
            userList
        } else {
            userList.filter {
                it["name"]?.contains(searchQuery.text, ignoreCase = true) == true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        showAnimation = true
                        isDarkMode = !isDarkMode
                    }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (isDarkMode) Color.Black else Color(0xFF128C7E)
                )
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(if (isDarkMode) Color.Black else Color.White)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isDarkMode) Color.Gray else Color.LightGray,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .padding(8.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = if (isDarkMode) Color.White else Color.Black)
                            )
                        }

                        // User List
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredUserList) { user ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                navController.navigate("chat_detail/${user["id"]}")
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDarkMode) Color.DarkGray else Color(0xFF25D366)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = user["name"] ?: "Unknown",
                                                style = MaterialTheme.typography.bodyLarge,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = if (isDarkMode) Color.White else Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Dark/Light Mode Animation
                AnimatedVisibility(
                    visible = showAnimation,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isDarkMode) R.drawable.bat_logo else R.drawable.sun_logo
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = Color.Unspecified
                    )
                }
            }
        }
    )
}
