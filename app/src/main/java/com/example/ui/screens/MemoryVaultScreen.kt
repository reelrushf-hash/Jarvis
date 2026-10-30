package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entity.ContactMemoryEntity
import com.example.data.db.entity.FactEntity
import com.example.data.db.entity.KnowledgeEntity
import com.example.data.db.entity.TaskEntity
import com.example.data.repository.MemoryStats
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun MemoryVaultScreen(
    stats: MemoryStats,
    facts: List<FactEntity>,
    tasks: List<TaskEntity>,
    contacts: List<ContactMemoryEntity>,
    knowledge: List<KnowledgeEntity>,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onAddTask: (String) -> Unit,
    onDeleteFact: (Long) -> Unit,
    onAddFact: (String, String, String) -> Unit,
    onDeleteContact: (Long) -> Unit,
    onAddContact: (String, String, String) -> Unit,
    onCallContact: (String) -> Unit,
    onDeleteKnowledge: (Long) -> Unit,
    onAddKnowledge: (String, String, String) -> Unit,
    onPruneLogs: () -> Unit,
    onRequestWipeMemory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddFactDialog by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showAddKnowledgeDialog by remember { mutableStateOf(false) }

    val tabTitles = listOf("Facts", "Tasks", "Contacts", "Notes", "Storage")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(16.dp)
    ) {
        // --- 500 MB Budget Meter Card ---
        Card(
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JarvisBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "Memory Engine",
                            tint = JarvisCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "500 MB LOCAL MEMORY VAULT",
                            color = JarvisCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    val percent = if (stats.budgetMb > 0) ((stats.usedMb / stats.budgetMb) * 100).coerceIn(0.0, 100.0) else 0.0
                    Text(
                        text = "${String.format("%.2f", stats.usedMb)} / ${stats.budgetMb} MB (${String.format("%.1f", percent)}%)",
                        color = JarvisCyanLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (stats.usedMb / stats.budgetMb.toDouble()).toFloat().coerceIn(0.01f, 1f) },
                    color = JarvisCyan,
                    trackColor = JarvisSurfaceElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${stats.factCount} Facts • ${stats.taskCount} Tasks • ${stats.contactCount} Contacts",
                        color = JarvisTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "SQLite Room DB",
                        color = JarvisTextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search local memories...", color = JarvisTextMuted, fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = JarvisCyan,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = JarvisCyan,
                unfocusedBorderColor = JarvisBorder,
                focusedTextColor = JarvisTextPrimary,
                unfocusedTextColor = JarvisTextPrimary,
                cursorColor = JarvisCyan,
                focusedContainerColor = JarvisSurfaceElevated,
                unfocusedContainerColor = JarvisSurfaceElevated
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().testTag("memory_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = JarvisSurface,
            contentColor = JarvisCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = JarvisCyan
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                // 1. Facts
                0 -> {
                    val filteredFacts = facts.filter {
                        searchQuery.isBlank() || it.subject.contains(searchQuery, true) || it.factContent.contains(searchQuery, true)
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "SAVED FACTS (${filteredFacts.size})",
                                color = JarvisTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Button(
                                onClick = { showAddFactDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("add_fact_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Fact", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (filteredFacts.isEmpty()) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("No facts stored. Say \"Remember that...\" to Boss vault.", color = JarvisTextMuted, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                                items(filteredFacts, key = { it.id }) { fact ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().border(1.dp, JarvisBorder, RoundedCornerShape(10.dp))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.padding(12.dp).fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = fact.subject,
                                                    color = JarvisCyanLight,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = fact.factContent,
                                                    color = JarvisTextPrimary,
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = "Category: ${fact.category}",
                                                    color = JarvisTextMuted,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                            IconButton(onClick = { onDeleteFact(fact.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Fact", tint = JarvisRedAlert, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Tasks
                1 -> {
                    val filteredTasks = tasks.filter {
                        searchQuery.isBlank() || it.title.contains(searchQuery, true)
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "SCHEDULED TASKS (${filteredTasks.size})",
                                color = JarvisTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Button(
                                onClick = { showAddTaskDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("add_task_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Task", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (filteredTasks.isEmpty()) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("No pending tasks. Say \"Add task...\" to Boss schedule.", color = JarvisTextMuted, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                                items(filteredTasks, key = { it.id }) { task ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().border(1.dp, JarvisBorder, RoundedCornerShape(10.dp))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(10.dp).fillMaxWidth()
                                        ) {
                                            Checkbox(
                                                checked = task.isCompleted,
                                                onCheckedChange = { onToggleTask(task) },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = JarvisCyan,
                                                    uncheckedColor = JarvisCyanDark
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = task.title,
                                                    color = if (task.isCompleted) JarvisTextMuted else JarvisTextPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                if (task.notes.isNotBlank()) {
                                                    Text(text = task.notes, color = JarvisTextSecondary, fontSize = 11.sp)
                                                }
                                            }
                                            IconButton(onClick = { onDeleteTask(task.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = JarvisRedAlert, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Contacts
                2 -> {
                    val filteredContacts = contacts.filter {
                        searchQuery.isBlank() || it.name.contains(searchQuery, true) || it.relationship.contains(searchQuery, true)
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "PINNED CONTACTS (${filteredContacts.size})",
                                color = JarvisTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Button(
                                onClick = { showAddContactDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Contact", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (filteredContacts.isEmpty()) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("No contacts pinned in local vault.", color = JarvisTextMuted, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                                items(filteredContacts, key = { it.id }) { contact ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().border(1.dp, JarvisBorder, RoundedCornerShape(10.dp))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.padding(12.dp).fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = contact.name,
                                                    color = JarvisCyanLight,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${contact.relationship.ifEmpty { "Personal" }} • ${contact.phoneNumber}",
                                                    color = JarvisTextPrimary,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            IconButton(onClick = { onCallContact(contact.phoneNumber) }) {
                                                Icon(Icons.Default.Call, contentDescription = "Call", tint = JarvisCyan, modifier = Modifier.size(20.dp))
                                            }
                                            IconButton(onClick = { onDeleteContact(contact.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = JarvisRedAlert, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Notes & Knowledge
                3 -> {
                    val filteredKnowledge = knowledge.filter {
                        searchQuery.isBlank() || it.title.contains(searchQuery, true) || it.content.contains(searchQuery, true)
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "KNOWLEDGE REPOSITORY (${filteredKnowledge.size})",
                                color = JarvisTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Button(
                                onClick = { showAddKnowledgeDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Note", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (filteredKnowledge.isEmpty()) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("No offline notes saved.", color = JarvisTextMuted, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                                items(filteredKnowledge, key = { it.id }) { item ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().border(1.dp, JarvisBorder, RoundedCornerShape(10.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(text = item.title, color = JarvisCyanLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                IconButton(onClick = { onDeleteKnowledge(item.id) }, modifier = Modifier.size(24.dp)) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = JarvisRedAlert, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Text(text = item.content, color = JarvisTextPrimary, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Storage & Maintenance
                4 -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, JarvisBorder, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "RETENTION & STORAGE MAINTENANCE",
                                    color = JarvisCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "To guarantee responsiveness on 2 GB RAM phones, memory records are indexed and capped at ${stats.budgetMb} MB with automatic pruning.",
                                    color = JarvisTextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = onPruneLogs,
                                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, JarvisCyanDark, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.CleaningServices, contentDescription = "Prune", tint = JarvisCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Prune Old Conversations (Keep Budget)", color = JarvisCyan, fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = onRequestWipeMemory,
                                    colors = ButtonDefaults.buttonColors(containerColor = JarvisRedAlert.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, JarvisRedAlert, RoundedCornerShape(8.dp)).testTag("wipe_memory_button")
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = "Wipe", tint = JarvisRedAlert, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Wipe All Local Memories (PIN Required)", color = JarvisRedAlert, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Add Fact
    if (showAddFactDialog) {
        var subject by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Personal") }

        AlertDialog(
            onDismissRequest = { showAddFactDialog = false },
            title = { Text("Add Fact to Vault", color = JarvisCyan) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject / Topic") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Fact Content") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subject.isNotBlank() && content.isNotBlank()) {
                            onAddFact(subject, content, category)
                            showAddFactDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                ) {
                    Text("Save Fact", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFactDialog = false }) { Text("Cancel") }
            },
            containerColor = JarvisSurfaceElevated
        )
    }

    // Dialog: Add Task
    if (showAddTaskDialog) {
        var taskTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add Task to Schedule", color = JarvisCyan) },
            text = {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Task Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            onAddTask(taskTitle)
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                ) {
                    Text("Add Task", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) { Text("Cancel") }
            },
            containerColor = JarvisSurfaceElevated
        )
    }

    // Dialog: Add Contact
    if (showAddContactDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var relationship by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text("Pin Contact to Vault", color = JarvisCyan) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true)
                    OutlinedTextField(value = relationship, onValueChange = { relationship = it }, label = { Text("Relationship (e.g. Mom, Doctor)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank()) {
                            onAddContact(name, phone, relationship)
                            showAddContactDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                ) {
                    Text("Save", color = Color.Black)
                }
            },
            dismissButton = { TextButton(onClick = { showAddContactDialog = false }) { Text("Cancel") } },
            containerColor = JarvisSurfaceElevated
        )
    }

    // Dialog: Add Knowledge
    if (showAddKnowledgeDialog) {
        var title by remember { mutableStateOf("") }
        var noteContent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddKnowledgeDialog = false },
            title = { Text("Add Knowledge / Note", color = JarvisCyan) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true)
                    OutlinedTextField(value = noteContent, onValueChange = { noteContent = it }, label = { Text("Content") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && noteContent.isNotBlank()) {
                            onAddKnowledge(title, noteContent, "")
                            showAddKnowledgeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                ) {
                    Text("Save Note", color = Color.Black)
                }
            },
            dismissButton = { TextButton(onClick = { showAddKnowledgeDialog = false }) { Text("Cancel") } },
            containerColor = JarvisSurfaceElevated
        )
    }
}
