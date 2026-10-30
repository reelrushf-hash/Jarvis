package com.example

import android.Manifest
import android.content.ComponentCallbacks2
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.system.FloatingJarvisService
import com.example.ui.JarvisViewModel
import com.example.ui.screens.MainHudScreen
import com.example.ui.screens.MemoryVaultScreen
import com.example.ui.screens.SettingsAndSecurityScreen
import com.example.ui.screens.SystemActionsScreen
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntentAction(intent)

        setContent {
            MyApplicationTheme {
                JarvisApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntentAction(intent)
    }

    private fun handleIntentAction(intent: Intent?) {
        if (intent?.getBooleanExtra("EXTRA_AUTO_START_LISTENING", false) == true) {
            viewModel.startListening()
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            // Low memory condition on 2 GB device: prune logs & clear non-essential caches
            viewModel.pruneOldLogs()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisApp(viewModel: JarvisViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val facts by viewModel.facts.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val knowledge by viewModel.knowledge.collectAsStateWithLifecycle()
    val auditLogs by viewModel.repository.auditLogs.collectAsStateWithLifecycle(initialValue = emptyList())

    var showSettingsSheet by remember { mutableStateOf(false) }
    var selectedDrawerTab by remember { mutableIntStateOf(0) }

    // Sync floating button service
    LaunchedEffect(uiState.ownerProfile.floatingButtonEnabled) {
        val serviceIntent = Intent(context, FloatingJarvisService::class.java)
        if (uiState.ownerProfile.floatingButtonEnabled) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)) {
                try {
                    ContextCompat.startForegroundService(context, serviceIntent)
                } catch (e: Exception) {
                    // Ignore service start exception
                }
            }
        } else {
            context.stopService(serviceIntent)
        }
    }

    // Runtime Permission Launcher (Microphone, Phone Calls, Contacts)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        if (!audioGranted) {
            Toast.makeText(context, "Microphone permission required for voice commands, Boss.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val needed = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.CALL_PHONE)
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.READ_CONTACTS)
        }
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = JarvisBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Minimal Screen (V2 Interface)
            MainHudScreen(
                state = uiState,
                onMicToggle = { viewModel.toggleListening() },
                onSendCommand = { viewModel.executeCommand(it) },
                onLanguageSelect = { viewModel.setLanguage(it) },
                onEmergencyStop = { viewModel.emergencyStop() },
                onReplayAudio = { viewModel.replayLastResponse() },
                onStopAudio = { viewModel.stopSpeaking() },
                onOpenSettings = {
                    viewModel.refreshMemoryStats()
                    showSettingsSheet = true
                }
            )
        }
    }

    // --- Settings & Vault Modal Sheet ---
    if (showSettingsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = sheetState,
            containerColor = JarvisSurface,
            contentColor = JarvisCyan,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp)
                    .background(JarvisBackground)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "CARO LINE VAULT & SETTINGS",
                        color = JarvisCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(
                        onClick = { showSettingsSheet = false },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = JarvisCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Drawer Tabs
                val drawerTabs = listOf("500MB VAULT", "TELEMETRY", "SECURITY")
                TabRow(
                    selectedTabIndex = selectedDrawerTab,
                    containerColor = JarvisSurface,
                    contentColor = JarvisCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedDrawerTab]),
                            color = JarvisCyan
                        )
                    }
                ) {
                    drawerTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedDrawerTab == index,
                            onClick = { selectedDrawerTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedDrawerTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        )
                    }
                }

                // Drawer Tab Content
                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedDrawerTab) {
                        0 -> MemoryVaultScreen(
                            stats = uiState.memoryStats,
                            facts = facts,
                            tasks = tasks,
                            contacts = contacts,
                            knowledge = knowledge,
                            onToggleTask = { viewModel.toggleTask(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                            onAddTask = { viewModel.addTaskManual(it) },
                            onDeleteFact = { viewModel.deleteFact(it) },
                            onAddFact = { sub, cont, cat -> viewModel.addFactManual(sub, cont, cat) },
                            onDeleteContact = { viewModel.deleteContact(it) },
                            onAddContact = { name, phone, rel -> viewModel.addContactManual(name, phone, rel) },
                            onCallContact = {
                                showSettingsSheet = false
                                viewModel.executeCommand("call $it")
                            },
                            onDeleteKnowledge = { viewModel.deleteKnowledge(it) },
                            onAddKnowledge = { title, cont, tags -> viewModel.addKnowledgeManual(title, cont, tags) },
                            onPruneLogs = { viewModel.pruneOldLogs() },
                            onRequestWipeMemory = {
                                viewModel.requestSensitiveActionWithPin {
                                    viewModel.wipeAllMemoriesConfirmed()
                                }
                            }
                        )
                        1 -> SystemActionsScreen(
                            diagnostics = uiState.diagnostics,
                            onLaunchApp = {
                                showSettingsSheet = false
                                viewModel.executeCommand("open $it")
                            },
                            onSearchYouTube = {
                                showSettingsSheet = false
                                viewModel.executeCommand("search youtube for $it")
                            },
                            onInitiateCall = {
                                showSettingsSheet = false
                                viewModel.executeCommand("call $it")
                            },
                            onSendWhatsApp = { phone, msg ->
                                showSettingsSheet = false
                                viewModel.executeCommand("send whatsapp to $phone saying $msg")
                            }
                        )
                        2 -> SettingsAndSecurityScreen(
                            profile = uiState.ownerProfile,
                            auditLogs = auditLogs,
                            onUpdateProfile = { viewModel.updateProfile(it) }
                        )
                    }
                }
            }
        }
    }

    // --- Action Confirmation Dialog (e.g. Call Confirmation for Safety) ---
    uiState.pendingConfirmation?.let { confirmation ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissPendingAction() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Call, contentDescription = "Confirm", tint = JarvisCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Action, Boss", color = JarvisCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(confirmation.prompt, color = JarvisTextPrimary, fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmPendingAction() },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                ) {
                    Text("Confirm & Execute", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissPendingAction() }) {
                    Text("Cancel", color = JarvisTextSecondary)
                }
            },
            containerColor = JarvisSurfaceElevated,
            modifier = Modifier.testTag("action_confirmation_dialog")
        )
    }

    // --- Security PIN Verification Dialog for Privileged Operations ---
    if (uiState.showPinVerification) {
        var enteredPin by remember { mutableStateOf("") }
        var isPinError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { viewModel.dismissPinDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = "Security PIN", tint = JarvisAmber, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Boss Authorization Required", color = JarvisAmber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Privileged operation requested. Please enter your secret PIN to authenticate as Boss.",
                        color = JarvisTextPrimary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            isPinError = false
                        },
                        label = { Text("Security PIN") },
                        singleLine = true,
                        isError = isPinError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("pin_verify_input")
                    )
                    if (isPinError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Incorrect PIN. Access Denied, Boss.", color = JarvisRedAlert, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val verified = viewModel.verifyPinAndExecute(enteredPin)
                        if (!verified) {
                            isPinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                ) {
                    Text("Verify", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissPinDialog() }) {
                    Text("Cancel", color = JarvisTextSecondary)
                }
            },
            containerColor = JarvisSurfaceElevated,
            modifier = Modifier.testTag("security_pin_dialog")
        )
    }
}
