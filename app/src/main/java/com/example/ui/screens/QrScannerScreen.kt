package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.data.engine.QrCodeCodec
import com.example.data.model.ColleagueProfile
import com.example.data.model.MatchType
import com.example.data.model.QrAutoSyncReceipt
import com.example.data.model.QrCameraPermissionState
import com.example.data.model.QrParseResult
import com.example.data.model.UserSettings
import com.example.data.repository.ShiftRepository
import com.example.ui.components.ManualColleagueSheet
import com.example.ui.components.UserAvatarImage
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.i18n.formatMonthTitleLocalized
import com.example.ui.i18n.localizedName
import com.example.ui.i18n.trEn
import com.example.ui.theme.DynamicTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ShiftViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

private const val KEY_CAMERA_PERMISSION_REQUESTED = "qr_camera_permission_requested"
private const val SCAN_DUPLICATE_COOLDOWN_MS = 3500L

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun evaluateCameraPermissionState(
    context: Context,
    wasRequestedBefore: Boolean
): QrCameraPermissionState {
    return try {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return QrCameraPermissionState.GRANTED
        val activity = context.findActivity()
        val showRationale = activity?.let {
            runCatching {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
            }.getOrDefault(false)
        } == true
        when {
            showRationale -> QrCameraPermissionState.RATIONALE_NEEDED
            wasRequestedBefore -> QrCameraPermissionState.PERMANENTLY_DENIED
            else -> QrCameraPermissionState.NOT_REQUESTED
        }
    } catch (_: Throwable) {
        QrCameraPermissionState.NOT_REQUESTED
    }
}

@Composable
fun QrScannerScreen(
    viewModel: ShiftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val dyn = DynamicTheme.colors
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val settings by viewModel.userSettings.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val localizedMonthTitle = remember(selectedYear, selectedMonth, appLocale) {
        formatMonthTitleLocalized(selectedYear, selectedMonth, appLocale)
    }

    val prefs = remember(context) {
        runCatching {
            context.getSharedPreferences(ShiftRepository.PREFS_NAME, Context.MODE_PRIVATE)
        }.getOrNull()
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Kamera ile Tara, 1: QR Kodumu Göster
    var isFlashOn by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(false) }
    var syncLinkInput by remember { mutableStateOf("") }
    var autoProcessToCalendar by remember { mutableStateOf(true) }

    var resolvedColleague by remember { mutableStateOf<ColleagueProfile?>(null) }
    var resolvedReceipt by remember { mutableStateOf<QrAutoSyncReceipt?>(null) }
    var showManualFriendSheet by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(300) }
    var lastInvalidToastMs by remember { mutableLongStateOf(0L) }
    var lastProcessedRaw by remember { mutableStateOf("") }
    var lastProcessedAtMs by remember { mutableLongStateOf(0L) }

    var cameraSessionKey by remember { mutableIntStateOf(0) }
    var wasRequestedBefore by remember {
        mutableStateOf(
            runCatching { prefs?.getBoolean(KEY_CAMERA_PERMISSION_REQUESTED, false) ?: false }
                .getOrDefault(false)
        )
    }
    var permissionState by remember {
        mutableStateOf(evaluateCameraPermissionState(context, wasRequestedBefore))
    }
    var autoPromptTriggeredThisVisit by remember { mutableStateOf(false) }
    val hasCameraPermission = permissionState == QrCameraPermissionState.GRANTED

    fun showSafeToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        runCatching { Toast.makeText(context, message, duration).show() }
    }

    fun openSystemAppSettings() {
        runCatching {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }.onFailure {
            showSafeToast(
                trEn(
                    isEn,
                    "Uygulama ayarları açılamadı. Lütfen cihaz ayarlarından kamera iznini açın.",
                    "Could not open app settings. Please enable camera permission from device settings."
                ),
                Toast.LENGTH_LONG
            )
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        wasRequestedBefore = true
        runCatching { prefs?.edit()?.putBoolean(KEY_CAMERA_PERMISSION_REQUESTED, true)?.apply() }
        val updatedState = evaluateCameraPermissionState(context, wasRequestedBefore = true)
        val transitionedToGranted = !hasCameraPermission && granted
        permissionState = updatedState

        if (granted) {
            if (transitionedToGranted) {
                cameraSessionKey++
            }
        } else {
            showSafeToast(
                if (updatedState == QrCameraPermissionState.PERMANENTLY_DENIED) {
                    trEn(
                        isEn,
                        "Kamera izni kalıcı reddedilmiş. Ayarlar'dan kamera iznini açabilirsiniz.",
                        "Camera permission is permanently denied. Enable it in App Settings."
                    )
                } else {
                    trEn(
                        isEn,
                        "QR taramak için kamera izni gereklidir. Galeriden seçebilir veya bağlantı yapıştırabilirsiniz.",
                        "Camera permission is required to scan QR codes. You can pick from gallery or paste a sync link."
                    )
                },
                Toast.LENGTH_LONG
            )
        }
    }

    fun triggerCameraPermissionDirectly(openSettingsIfPermanent: Boolean = true) {
        val currentState = evaluateCameraPermissionState(context, wasRequestedBefore)
        permissionState = currentState
        when (currentState) {
            QrCameraPermissionState.GRANTED -> {
                cameraSessionKey++
            }
            QrCameraPermissionState.PERMANENTLY_DENIED -> {
                if (openSettingsIfPermanent) {
                    showSafeToast(trEn(isEn, "Kamera izni ayarlardan açılmalıdır.", "Enable camera permission in settings."))
                    openSystemAppSettings()
                }
            }
            QrCameraPermissionState.NOT_REQUESTED,
            QrCameraPermissionState.RATIONALE_NEEDED -> {
                wasRequestedBefore = true
                runCatching { prefs?.edit()?.putBoolean(KEY_CAMERA_PERMISSION_REQUESTED, true)?.apply() }
                runCatching {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }.onFailure {
                    showSafeToast(
                        trEn(
                            isEn,
                            "Kamera izin penceresi açılamadı. Ayarlar veya Galeri seçeneğini kullanabilirsiniz.",
                            "Could not open permission dialog. Try Settings or Gallery."
                        ),
                        Toast.LENGTH_LONG
                    )
                }
            }
        }
    }

    // Refresh permission state on resume & turn off torch cleanly on pause
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    val previous = permissionState
                    val updated = evaluateCameraPermissionState(context, wasRequestedBefore)
                    permissionState = updated
                    if (previous != updated && updated == QrCameraPermissionState.GRANTED) {
                        cameraSessionKey++
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    isFlashOn = false
                }
                else -> Unit
            }
        }
        runCatching { lifecycleOwner.lifecycle.addObserver(observer) }
        onDispose {
            isFlashOn = false
            runCatching { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }

    // Request camera permission cleanly after initial frame; turn off flash when switching away
    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            val current = evaluateCameraPermissionState(context, wasRequestedBefore)
            permissionState = current
            if (current != QrCameraPermissionState.GRANTED && !autoPromptTriggeredThisVisit) {
                autoPromptTriggeredThisVisit = true
                if (current != QrCameraPermissionState.PERMANENTLY_DENIED) {
                    delay(150L)
                    val refreshed = evaluateCameraPermissionState(context, wasRequestedBefore)
                    permissionState = refreshed
                    if (refreshed == QrCameraPermissionState.NOT_REQUESTED ||
                        refreshed == QrCameraPermissionState.RATIONALE_NEEDED
                    ) {
                        wasRequestedBefore = true
                        runCatching { prefs?.edit()?.putBoolean(KEY_CAMERA_PERMISSION_REQUESTED, true)?.apply() }
                        runCatching {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }
            }
        } else {
            isFlashOn = false
        }
    }

    fun handleDecodedQrRaw(rawValue: String, fromCamera: Boolean) {
        val now = System.currentTimeMillis()
        val parseResult = runCatching {
            viewModel.repository.parseSyncPayloadDetailed(rawValue)
        }.getOrElse {
            QrParseResult.Failure(
                errorCode = "PARSE_ERROR",
                errorMessage = trEn(isEn, "QR verisi çözümlenemedi.", "Could not decode QR data."),
                rawPreview = rawValue.take(60)
            )
        }

        when (parseResult) {
            is QrParseResult.Failure -> {
                if (fromCamera && rawValue == lastProcessedRaw && now - lastInvalidToastMs < 2500L) {
                    return
                }
                lastProcessedRaw = rawValue
                if (!fromCamera || now - lastInvalidToastMs > 2500L) {
                    lastInvalidToastMs = now
                    val localizedErr = if (!isEn) {
                        parseResult.errorMessage
                    } else {
                        when (parseResult.errorCode) {
                            "EMPTY_INPUT" -> "No sync link or QR data entered."
                            "UNSUPPORTED_SCHEME" -> "Unsupported QR or link format. Use a PROSHIFT code."
                            "MISSING_NAME" -> "Colleague name is missing in the QR data."
                            "MISSING_PRESET" -> "Shift cycle pattern is missing in the QR data."
                            else -> "Could not decode QR data."
                        }
                    }
                    showSafeToast(localizedErr)
                }
            }

            is QrParseResult.Success -> {
                if (fromCamera && parseResult.normalizedUri == lastProcessedRaw && now - lastProcessedAtMs < SCAN_DUPLICATE_COOLDOWN_MS) {
                    return
                }
                lastProcessedRaw = parseResult.normalizedUri
                lastProcessedAtMs = now

                val colleague = parseResult.colleague
                if (autoProcessToCalendar) {
                    val receipt = runCatching {
                        viewModel.autoProcessScannedColleague(
                            profile = colleague,
                            sourceLabel = if (fromCamera) trEn(isEn, "Canlı Kamera", "Live Camera") else trEn(isEn, "QR İçe Aktarma", "QR Import"),
                            enableFriendLayer = true
                        )
                    }.getOrNull()
                    resolvedReceipt = receipt
                    resolvedColleague = colleague
                } else {
                    resolvedReceipt = null
                    resolvedColleague = colleague
                }
            }
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) {
            showSafeToast(trEn(isEn, "QR görseli seçilmedi", "No QR image selected"))
            return@rememberLauncherForActivityResult
        }
        coroutineScope.launch {
            val raw = withContext(Dispatchers.IO) {
                QrCodeCodec.decodeQrFromUri(context, uri)
            }
            if (!raw.isNullOrBlank()) {
                handleDecodedQrRaw(raw, fromCamera = false)
            } else {
                showSafeToast(trEn(isEn, "Seçilen görselde QR kod algılanamadı", "No QR code detected in selected image"))
            }
        }
    }

    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
    }

    BackHandler {
        viewModel.navigateBack(fallback = AppScreen.MATCHING_QR)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(dyn.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .imePadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 32.dp)
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(dyn.surfaceElevated, CircleShape)
                            .border(1.dp, dyn.borderSubtle, CircleShape)
                            .clickable { viewModel.navigateBack(fallback = AppScreen.MATCHING_QR) }
                            .testTag("btn_qr_scanner_back"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = trEn("Geri", "Back"),
                            tint = dyn.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = trEn("QR Kod Tarayıcı", "QR Code Scanner"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.textPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(dyn.surfaceElevated, CircleShape)
                            .border(1.dp, dyn.borderSubtle, CircleShape)
                            .clickable { viewModel.navigateBack(fallback = AppScreen.MATCHING_QR) }
                            .testTag("btn_qr_scanner_close"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = trEn("Kapat", "Close"),
                            tint = dyn.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, dyn.primaryNeon, CircleShape)
                            .clickable { viewModel.navigateTo(AppScreen.PROFILE) }
                    ) {
                        UserAvatarImage(
                            avatarPath = settings.avatarUriOrPath,
                            contentDescription = trEn("Profil", "Profile"),
                            displayName = settings.employeeName,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Two-tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .background(dyn.surfaceDim, RoundedCornerShape(14.dp))
                    .border(1.dp, dyn.borderMetal, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val activeTabBg = if (dyn.isDark) dyn.surfaceElevated else dyn.surfaceCard
                val activeTabBorder = dyn.borderSubtle
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 0) activeTabBg else Color.Transparent)
                        .border(1.dp, if (selectedTab == 0) activeTabBorder else Color.Transparent, RoundedCornerShape(10.dp))
                        .clickable { selectedTab = 0 }
                        .testTag("tab_kamera_tara"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null,
                            tint = if (selectedTab == 0) dyn.primaryNeon else dyn.textMuted,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = trEn("Kamera ile Tara", "Scan Camera"),
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) dyn.textPrimary else dyn.textSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 1) activeTabBg else Color.Transparent)
                        .border(1.dp, if (selectedTab == 1) activeTabBorder else Color.Transparent, RoundedCornerShape(10.dp))
                        .clickable { selectedTab = 1 }
                        .testTag("tab_qr_goster"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Filled.QrCode,
                            contentDescription = null,
                            tint = if (selectedTab == 1) dyn.primaryNeon else dyn.textMuted,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = trEn("QR Kodumu Göster", "Show My QR"),
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) dyn.textPrimary else dyn.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TAB 0: KAMERA İLE TARA
            if (selectedTab == 0) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CameraScannerViewfinder(
                        hasPermission = hasCameraPermission,
                        permissionState = permissionState,
                        sessionKey = cameraSessionKey,
                        isFlashOn = isFlashOn,
                        isFrontCamera = isFrontCamera,
                        isScanningPaused = resolvedColleague != null || showManualFriendSheet,
                        onRequestPermission = { triggerCameraPermissionDirectly(openSettingsIfPermanent = true) },
                        onOpenAppSettings = { openSystemAppSettings() },
                        onRetryCamera = { cameraSessionKey++ },
                        onFallbackFrontCamera = { front -> isFrontCamera = front },
                        onQrDetected = { rawValue ->
                            handleDecodedQrRaw(rawValue, fromCamera = true)
                        }
                    )

                    Text(
                        text = when {
                            hasCameraPermission -> trEn("Arkadaşının QR kodunu kamera çerçevesinin içine hizala.", "Align your colleague's QR code inside the camera frame.")
                            permissionState == QrCameraPermissionState.PERMANENTLY_DENIED -> trEn("Kamera izni ayarlardan kapalı. Galeriden görsel seçebilirsin.", "Camera permission is disabled in settings. You can pick an image from Gallery.")
                            else -> trEn("QR okumak için kamera izni ver veya galeriden görsel seç.", "Grant camera permission or pick a QR image from Gallery.")
                        },
                        fontSize = 12.sp,
                        color = dyn.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Auto-Process Scanned QR into Active Calendar Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (autoProcessToCalendar) dyn.activeChipBg else dyn.surfaceCard)
                            .border(
                                1.dp,
                                if (autoProcessToCalendar) dyn.accentCyan.copy(alpha = 0.55f) else dyn.borderSubtle,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(dyn.surfaceCard, CircleShape)
                                    .border(1.dp, dyn.accentCyan.copy(alpha = 0.45f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = dyn.accentCyan,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = trEn("Takvime Otomatik İşle", "Auto-Apply to Calendar"),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.textPrimary
                                )
                                Text(
                                    text = trEn("Okunan QR doğrudan arkadaşına işlenir ve takvimde görünür", "Scanned QR is immediately saved and shown on your calendar"),
                                    fontSize = 10.sp,
                                    color = dyn.textSecondary,
                                    lineHeight = 13.sp
                                )
                            }
                        }

                        Switch(
                            checked = autoProcessToCalendar,
                            onCheckedChange = { enabled -> autoProcessToCalendar = enabled },
                            modifier = Modifier.testTag("switch_auto_apply_calendar"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = dyn.primaryNeon,
                                uncheckedTrackColor = dyn.surfacePanel
                            )
                        )
                    }

                    // 3 Essential Camera Controls
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CameraActionButton(
                            icon = if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            label = if (isFlashOn) trEn("FLAŞ: AÇIK", "FLASH: ON") else trEn("FLAŞ: KAPALI", "FLASH: OFF"),
                            isActive = isFlashOn,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (!hasCameraPermission) {
                                    triggerCameraPermissionDirectly(openSettingsIfPermanent = true)
                                } else if (isFrontCamera) {
                                    Toast.makeText(context, trEn(isEn, "Ön kamerada flaş kullanılamaz", "Flash is not available on front camera"), Toast.LENGTH_SHORT).show()
                                } else {
                                    isFlashOn = !isFlashOn
                                }
                            }
                        )

                        CameraActionButton(
                            icon = Icons.Filled.Image,
                            label = trEn("GALERİDEN SEÇ", "PICK GALLERY"),
                            isActive = false,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                runCatching {
                                    galleryPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }.onFailure {
                                    showSafeToast(trEn(isEn, "Galeri seçici açılamadı", "Could not open gallery picker"))
                                }
                            }
                        )

                        CameraActionButton(
                            icon = Icons.Filled.FlipCameraAndroid,
                            label = if (isFrontCamera) trEn("ÖN KAMERA", "FRONT CAM") else trEn("ARKA KAMERA", "REAR CAM"),
                            isActive = isFrontCamera,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (!hasCameraPermission) {
                                    triggerCameraPermissionDirectly(openSettingsIfPermanent = true)
                                } else {
                                    isFrontCamera = !isFrontCamera
                                    if (isFrontCamera) isFlashOn = false
                                }
                            }
                        )
                    }

                    // Manual Add Friend Card (Works without QR/camera)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(dyn.activeChipBg)
                            .border(1.dp, dyn.accentCyan.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                            .clickable { showManualFriendSheet = true }
                            .padding(14.dp)
                            .testTag("btn_scanner_manual_add"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(dyn.surfaceCard, CircleShape)
                                .border(1.dp, dyn.accentCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Filled.PersonAdd, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(20.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = trEn("Arkadaşı Elle Ekle (Vardiya Seç)", "Add Colleague Manually"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                            Text(text = trEn("2-2-2 (8s), 12/36 veya Özel Döngü ile doğrudan ekle", "Add directly using 2-2-2 (8h), 12/36, or Custom Cycle"), fontSize = 11.sp, color = dyn.accentCyan)
                        }

                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(18.dp))
                    }

                    // Universal Sync Link / JSON Import Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(dyn.surfaceCard)
                            .border(1.dp, dyn.borderMetal, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(dyn.activeChipBg, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Filled.Link, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(16.dp))
                                }
                                Column {
                                    Text(text = trEn("Bağlantı veya Kod ile İçe Aktar", "Import via Link or Code"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                                    Text(text = trEn("PROSHIFT://sync bağlantısını veya JSON verisini yapıştır", "Paste a PROSHIFT://sync link or JSON payload"), fontSize = 11.sp, color = dyn.textSecondary)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = syncLinkInput,
                            onValueChange = { syncLinkInput = it },
                            label = { Text(trEn("Vardiya Bağlantısı veya JSON", "Shift Sync Link or JSON"), fontSize = 11.sp, color = dyn.textSecondary) },
                            placeholder = { Text(trEn("PROSHIFT://sync?... veya {\"p\":\"PROSHIFT\",...}", "PROSHIFT://sync?... or {\"p\":\"PROSHIFT\",...}"), color = dyn.textMuted) },
                            minLines = 2,
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = dyn.accentCyan,
                                unfocusedBorderColor = dyn.borderMetal,
                                focusedTextColor = dyn.accentCyan,
                                unfocusedTextColor = dyn.textPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_sync_link"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val clipText = runCatching {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
                                    }.getOrDefault("")
                                    if (clipText.isBlank()) {
                                        showSafeToast(trEn(isEn, "Panoda kopyalı metin bulunamadı", "No text found on clipboard"))
                                    } else {
                                        syncLinkInput = clipText
                                        handleDecodedQrRaw(clipText, fromCamera = false)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.ContentPaste, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(trEn("Yapıştır", "Paste"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                            }

                            Surface(
                                onClick = {
                                    handleDecodedQrRaw(syncLinkInput, fromCamera = false)
                                },
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(48.dp)
                                    .testTag("btn_verify_sync_link"),
                                shape = RoundedCornerShape(12.dp),
                                color = dyn.primaryDeep
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = trEn("Çözümle ve İşle", "Decode & Apply"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    // Zero-Cloud Guarantee Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(dyn.surfaceDim)
                            .border(1.dp, dyn.borderSubtle, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(14.dp))
                            Text(text = trEn("SIFIR SUNUCU KAYDI (ZERO-CLOUD P2P)", "ZERO-CLOUD P2P PRIVACY"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan, letterSpacing = 0.5.sp)
                        }
                        Text(
                            text = trEn("Kamera ve QR verileri tamamen cihaz içinde işlenir; harici sunuculara aktarılmaz.", "Camera and QR data are processed 100% on-device; nothing is sent to external servers."),
                            fontSize = 11.sp,
                            color = dyn.textMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // TAB 1: QR KODUMU GÖSTER
            if (selectedTab == 1) {
                QrCodeShareTab(
                    viewModel = viewModel,
                    settings = settings,
                    monthTitle = localizedMonthTitle,
                    remainingSeconds = remainingSeconds,
                    onResetTimer = { remainingSeconds = 300 }
                )
            }
        }

        // RESOLVED SHIFT MODAL
        val activeColleague = resolvedColleague
        if (activeColleague != null) {
            ResolvedShiftBottomSheet(
                colleague = activeColleague,
                userSettings = settings,
                repository = viewModel.repository,
                autoAppliedReceipt = resolvedReceipt,
                onDismiss = {
                    resolvedColleague = null
                    resolvedReceipt = null
                },
                onOpenCalendar = {
                    resolvedColleague = null
                    resolvedReceipt = null
                    viewModel.navigateTo(AppScreen.CALENDAR)
                },
                onApplyAsOwnShift = {
                    viewModel.applyColleagueAsOwnShift(activeColleague)
                    resolvedColleague = null
                    resolvedReceipt = null
                    viewModel.navigateTo(AppScreen.CALENDAR)
                },
                onConfirmImport = { showOnCal, alertCommon ->
                    viewModel.autoProcessScannedColleague(
                        profile = activeColleague,
                        sourceLabel = trEn(isEn, "Manuel Onay", "Manual Confirmation"),
                        enableFriendLayer = showOnCal,
                        enableCommonLeaveAlert = alertCommon
                    )
                    resolvedColleague = null
                    resolvedReceipt = null
                    Toast.makeText(
                        context,
                        trEn(
                            isEn,
                            "${activeColleague.name} nöbet çizelgesi takvime ve eşleşmeye eklendi!",
                            "${activeColleague.name}'s shift schedule added to calendar & matching!"
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.navigateTo(AppScreen.MATCHING_QR)
                }
            )
        }

        if (showManualFriendSheet) {
            ManualColleagueSheet(
                onDismiss = { showManualFriendSheet = false },
                onSave = { profile ->
                    if (autoProcessToCalendar) {
                        resolvedReceipt = viewModel.autoProcessScannedColleague(
                            profile = profile,
                            sourceLabel = trEn(isEn, "Elle Ekleme", "Manual Entry"),
                            enableFriendLayer = true
                        )
                    } else {
                        resolvedReceipt = null
                    }
                    resolvedColleague = profile
                }
            )
        }
    }
}

@Composable
private fun CameraScannerViewfinder(
    hasPermission: Boolean,
    permissionState: QrCameraPermissionState,
    sessionKey: Int,
    isFlashOn: Boolean,
    isFrontCamera: Boolean,
    isScanningPaused: Boolean,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onRetryCamera: () -> Unit,
    onFallbackFrontCamera: (Boolean) -> Unit,
    onQrDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val isEn = LocalIsEnglish.current
    val dyn = DynamicTheme.colors
    val lifecycleOwner = LocalLifecycleOwner.current
    val pausedState by rememberUpdatedState(isScanningPaused)
    val onDetectedState by rememberUpdatedState(onQrDetected)
    val onFallbackState by rememberUpdatedState(onFallbackFrontCamera)

    var cameraError by remember(sessionKey, hasPermission) { mutableStateOf<String?>(null) }
    var boundCamera by remember(sessionKey, hasPermission) { mutableStateOf<Camera?>(null) }

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val lastAnalyzeMs = remember { AtomicLong(0L) }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { analysisExecutor.shutdown() }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "scanLaser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserPosition"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (hasPermission && cameraError == null) Color(0xFF040A12) else dyn.surfaceCard)
            .border(1.dp, dyn.borderMetal, RoundedCornerShape(18.dp))
            .testTag("camera_viewfinder_box")
    ) {
        val previewView = remember(sessionKey, hasPermission) {
            if (!hasPermission) {
                null
            } else {
                runCatching {
                    PreviewView(context).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                }.onFailure { err ->
                    cameraError = err.localizedMessage ?: trEn(isEn, "Kamera önizlemesi oluşturulamadı", "Could not create camera preview")
                }.getOrNull()
            }
        }

        if (hasPermission && cameraError == null && previewView != null) {
            LaunchedEffect(boundCamera, isFlashOn, isFrontCamera) {
                runCatching {
                    val cam = boundCamera ?: return@runCatching
                    val hasFlash = cam.cameraInfo.hasFlashUnit()
                    cam.cameraControl.enableTorch(isFlashOn && !isFrontCamera && hasFlash)
                }
            }

            DisposableEffect(lifecycleOwner, previewView, sessionKey, isFrontCamera) {
                val mainExecutor = ContextCompat.getMainExecutor(context)
                var disposed = false
                var cameraProvider: ProcessCameraProvider? = null

                val providerFuture = runCatching {
                    ProcessCameraProvider.getInstance(context)
                }.onFailure { err ->
                    cameraError = err.localizedMessage ?: trEn(isEn, "Kamera servisi başlatılamadı", "Could not start camera service")
                }.getOrNull()

                providerFuture?.addListener(
                    {
                        if (disposed) return@addListener
                        try {
                            val provider = providerFuture.get()
                            cameraProvider = provider

                            val hasBack = runCatching {
                                provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                            }.getOrDefault(false)
                            val hasFront = runCatching {
                                provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                            }.getOrDefault(false)

                            if (!hasBack && !hasFront) {
                                cameraError = trEn(isEn, "Cihazda kullanılabilir kamera sensörü bulunamadı", "No available camera sensor found on this device")
                                return@addListener
                            }

                            val selector = when {
                                isFrontCamera && hasFront -> CameraSelector.DEFAULT_FRONT_CAMERA
                                !isFrontCamera && hasBack -> CameraSelector.DEFAULT_BACK_CAMERA
                                hasFront -> {
                                    onFallbackState(true)
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                }
                                else -> {
                                    onFallbackState(false)
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                }
                            }

                            val preview = Preview.Builder()
                                .build()
                                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also { analysis ->
                                    analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                        try {
                                            if (disposed || pausedState) return@setAnalyzer
                                            val now = System.currentTimeMillis()
                                            val prev = lastAnalyzeMs.get()
                                            if (now - prev < 120L) return@setAnalyzer
                                            if (!lastAnalyzeMs.compareAndSet(prev, now)) return@setAnalyzer

                                            val decoded = QrCodeCodec.decodeQrFromImageProxy(imageProxy)
                                            if (!decoded.isNullOrBlank()) {
                                                mainExecutor.execute {
                                                    if (!disposed && !pausedState) {
                                                        onDetectedState(decoded)
                                                    }
                                                }
                                            }
                                        } catch (_: Throwable) {
                                            // Keep camera stream alive on malformed frames
                                        } finally {
                                            runCatching { imageProxy.close() }
                                        }
                                    }
                                }

                            provider.unbindAll()
                            val camera = provider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                imageAnalysis
                            )
                            boundCamera = camera
                            cameraError = null
                        } catch (e: Throwable) {
                            boundCamera = null
                            cameraError = e.cause?.localizedMessage ?: e.localizedMessage ?: trEn(isEn, "Kamera başlatılamadı", "Camera failed to start")
                        }
                    },
                    mainExecutor
                )

                onDispose {
                    disposed = true
                    runCatching { boundCamera?.cameraControl?.enableTorch(false) }
                    boundCamera = null
                    runCatching { cameraProvider?.unbindAll() }
                }
            }

            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            // Reticle + Laser Line
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height
                val boxSize = 180.dp.toPx()
                val left = (canvasW - boxSize) / 2
                val top = (canvasH - boxSize) / 2
                val right = left + boxSize
                val bottom = top + boxSize
                val cornerLen = 28.dp.toPx()
                val strokeW = 3.dp.toPx()
                val cyanColor = Color(0xFF38BDF8)

                drawLine(cyanColor, Offset(left, top), Offset(left + cornerLen, top), strokeW)
                drawLine(cyanColor, Offset(left, top), Offset(left, top + cornerLen), strokeW)
                drawLine(cyanColor, Offset(right, top), Offset(right - cornerLen, top), strokeW)
                drawLine(cyanColor, Offset(right, top), Offset(right, top + cornerLen), strokeW)
                drawLine(cyanColor, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeW)
                drawLine(cyanColor, Offset(left, bottom), Offset(left, bottom - cornerLen), strokeW)
                drawLine(cyanColor, Offset(right, bottom), Offset(right - cornerLen, bottom), strokeW)
                drawLine(cyanColor, Offset(right, bottom), Offset(right, bottom - cornerLen), strokeW)

                val laserY = top + (boxSize * laserOffset)
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF38BDF8), Color(0xFF67E8F9), Color(0xFF38BDF8), Color.Transparent)
                    ),
                    start = Offset(left + 6.dp.toPx(), laserY),
                    end = Offset(right - 6.dp.toPx(), laserY),
                    strokeWidth = 2.5.dp.toPx()
                )
            }
        } else {
            // Camera permission needed OR hardware error fallback
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(dyn.surfaceCard, dyn.surfaceElevated)
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(dyn.activeChipBg, CircleShape)
                        .border(1.5.dp, if (cameraError != null) dyn.dangerAccent else dyn.accentCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (cameraError != null) Icons.Filled.Refresh else Icons.Filled.CameraAlt,
                        contentDescription = null,
                        tint = if (cameraError != null) dyn.dangerAccent else dyn.accentCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when {
                        cameraError != null -> trEn("Kamera Başlatılamadı", "Camera Failed to Start")
                        permissionState == QrCameraPermissionState.PERMANENTLY_DENIED -> trEn("Kamera İzni Ayarlardan Kapalı", "Camera Disabled in Settings")
                        else -> trEn("Kamera İzni Gerekli", "Camera Permission Required")
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = dyn.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                val activeCameraError = cameraError
                Text(
                    text = when {
                        activeCameraError != null -> activeCameraError
                        permissionState == QrCameraPermissionState.PERMANENTLY_DENIED ->
                            trEn(
                                "Android kamera izni daha önce kalıcı olarak kapatılmış. Uygulama Ayarları > İzinler bölümünden Kamerayı açın.",
                                "Camera permission was permanently denied. Enable Camera in App Settings > Permissions."
                            )
                        else ->
                            trEn(
                                "QR kodu canlı okumak için kameraya doğrudan izin verin veya galeriden görsel seçin.",
                                "Grant camera access to scan live QR codes, or pick a QR image from Gallery."
                            )
                    },
                    fontSize = 11.sp,
                    color = dyn.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (cameraError != null) {
                                cameraError = null
                                onRetryCamera()
                            } else {
                                onRequestPermission()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = dyn.primaryNeon),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(48.dp).testTag("btn_grant_camera_permission")
                    ) {
                        Text(
                            text = when {
                                cameraError != null -> trEn("Kamerayı Yenile", "Retry Camera")
                                permissionState == QrCameraPermissionState.PERMANENTLY_DENIED -> trEn("Ayarlara Git", "Open Settings")
                                else -> trEn("İzni Doğrudan Tetikle", "Grant Permission")
                            },
                            color = dyn.textOnPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    if (cameraError == null && permissionState != QrCameraPermissionState.PERMANENTLY_DENIED) {
                        OutlinedButton(
                            onClick = onOpenAppSettings,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(trEn("Ayarlar", "Settings"), color = dyn.accentCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val dyn = DynamicTheme.colors
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) dyn.activeChipBg else dyn.surfaceCard)
            .border(1.dp, if (isActive) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(imageVector = icon, contentDescription = label, tint = if (isActive) dyn.accentCyan else dyn.textSecondary, modifier = Modifier.size(18.dp))
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isActive) dyn.accentCyan else dyn.textSecondary, maxLines = 1)
        }
    }
}

@Composable
fun QrCodeShareTab(
    viewModel: ShiftViewModel,
    settings: UserSettings,
    monthTitle: String,
    remainingSeconds: Int,
    onResetTimer: () -> Unit
) {
    val context = LocalContext.current
    val isEn = LocalIsEnglish.current
    val dyn = DynamicTheme.colors
    var selectedScope by remember { mutableIntStateOf(0) }
    var hideNotes by remember { mutableStateOf(true) }
    var maskOvertime by remember { mutableStateOf(true) }

    val payload = remember(settings, selectedScope, hideNotes, maskOvertime) {
        "${viewModel.repository.generateSyncPayload(settings)}&scope=$selectedScope&hideNotes=$hideNotes"
    }
    val qrBitmap = remember(payload) {
        QrCodeCodec.generateQrBitmap(payload, 512)
    }

    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header Card (Live settings)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(dyn.surfaceCard)
                .border(1.dp, dyn.borderMetal, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(2.dp, dyn.accentCyan, CircleShape)
            ) {
                UserAvatarImage(
                    avatarPath = settings.avatarUriOrPath,
                    contentDescription = settings.employeeName,
                    displayName = settings.employeeName,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = settings.employeeName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                    Icon(imageVector = Icons.Filled.Verified, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(14.dp))
                }
                Text(
                    text = "${settings.department} • ${settings.cyclePreset.localizedName(isEn)}",
                    fontSize = 11.sp,
                    color = dyn.textSecondary,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(dyn.emeraldBg)
                    .border(1.dp, dyn.emeraldBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = trEn("AKTİF", "ACTIVE"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.emeraldAccent)
            }
        }

        // Real Scannable ZXing QR Code Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(dyn.surfaceCard)
                .border(1.dp, dyn.borderMetal, RoundedCornerShape(18.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, dyn.borderSubtle, RoundedCornerShape(14.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = trEn("PROSHIFT Vardiya QR Kodu", "PROSHIFT Shift QR Code"),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(trEn("QR oluşturulamadı", "Could not generate QR"), color = Color.Black, fontSize = 12.sp)
                }
            }

            Text(
                text = trEn(
                    "Arkadaşın bu QR kodu okuttuğunda senin ${settings.cyclePreset.localizedName(false)} düzenin otomatik takvimine işlenir.",
                    "When your colleague scans this QR code, your ${settings.cyclePreset.localizedName(true)} schedule syncs automatically to their calendar."
                ),
                fontSize = 11.sp,
                color = dyn.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )

            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            val progress = (remainingSeconds / 300f).coerceIn(0f, 1f)

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trEn("Yenilemek için dokun", "Tap to refresh"),
                        fontSize = 11.sp,
                        color = dyn.accentCyan,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onResetTimer() }
                    )
                    Text(
                        text = "%02d:%02d".format(mins, secs),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.accentCyan
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(dyn.surfacePanel)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progress)
                            .height(4.dp)
                            .background(Brush.horizontalGradient(listOf(dyn.primaryDeep, dyn.primaryNeon)))
                    )
                }
            }
        }

        // Direct Sync Link Box (Copyable & Shareable)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(dyn.surfaceCard)
                .border(1.dp, dyn.borderMetal, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = trEn("Doğrudan Senkron Bağlantısı", "Direct Sync Link"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                Text(text = trEn("WhatsApp / Mesaj için", "For WhatsApp / Messages"), fontSize = 10.sp, color = dyn.textMuted)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(dyn.surfaceDim)
                    .border(1.dp, dyn.borderSubtle, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = payload,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = dyn.accentCyan,
                    maxLines = 2
                )
            }
        }

        // Sharing Scope Selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(dyn.surfaceCard)
                .border(1.dp, dyn.borderMetal, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = trEn("Paylaşım Kapsamı", "Sharing Scope"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
            val scopes = listOf(
                trEn("Tüm Ay ($monthTitle)", "Full Month ($monthTitle)"),
                trEn("Yalnızca Ortak İzin Günleri", "Shared Days Off Only"),
                trEn("Önümüzdeki 7 Gün", "Next 7 Days")
            )

            scopes.forEachIndexed { idx, label ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedScope == idx) dyn.activeChipBg else Color.Transparent)
                        .clickable { selectedScope = idx }
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(if (selectedScope == idx) dyn.accentCyan else Color.Transparent, CircleShape)
                            .border(1.5.dp, if (selectedScope == idx) dyn.accentCyan else dyn.textMuted, CircleShape)
                    )
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (selectedScope == idx) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedScope == idx) dyn.textPrimary else dyn.textSecondary
                    )
                }
            }
        }

        // Privacy Switches
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(dyn.surfaceCard)
                .border(1.dp, dyn.borderMetal, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = trEn("Kişisel Notları Gizle", "Hide Personal Notes"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                    Text(text = trEn("Özel nöbet açıklamaları paylaşıma eklenmez", "Private shift notes are excluded from sharing"), fontSize = 10.sp, color = dyn.textMuted)
                }
                Switch(
                    checked = hideNotes,
                    onCheckedChange = { hideNotes = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = dyn.primaryNeon, uncheckedTrackColor = dyn.surfacePanel)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = trEn("Ek Mesai Detaylarını Maskele", "Mask Overtime Details"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                    Text(text = trEn("Sadece ana nöbet blokları iletilir", "Only core shift blocks are shared"), fontSize = 10.sp, color = dyn.textMuted)
                }
                Switch(
                    checked = maskOvertime,
                    onCheckedChange = { maskOvertime = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = dyn.primaryNeon, uncheckedTrackColor = dyn.surfacePanel)
                )
            }
        }

        // Action Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        runCatching {
                            val shareText = if (isEn) {
                                "PROSHIFT Shift Match\n${settings.employeeName} (${settings.cyclePreset.localizedName(true)})\nCopy this link and paste it into PROSHIFT > Import via Link:\n$payload"
                            } else {
                                "PROSHIFT Vardiya Eşleşme\n${settings.employeeName} (${settings.cyclePreset.localizedName(false)})\nBağlantıyı kopyalayıp PROSHIFT > Bağlantı ile İçe Aktar ekranına yapıştır:\n$payload"
                            }
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, trEn(isEn, "Vardiya Bağlantısını Paylaş", "Share Shift Link")))
                        }.onFailure {
                            runCatching {
                                Toast.makeText(context, trEn(isEn, "Paylaşım ekranı açılamadı", "Could not open share sheet"), Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF15803D)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Filled.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text(text = trEn("Bağlantıyı Paylaş", "Share Link"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        val copied = runCatching {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Proshift Sync Payload", payload))
                            clipboard != null
                        }.getOrDefault(false)
                        runCatching {
                            Toast.makeText(
                                context,
                                if (copied) trEn(isEn, "Senkron bağlantısı kopyalandı!", "Sync link copied!") else trEn(isEn, "Panoya kopyalanamadı", "Failed to copy"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                color = dyn.surfaceElevated
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(16.dp))
                        Text(text = trEn("Kopyala", "Copy"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun ResolvedShiftBottomSheet(
    colleague: ColleagueProfile,
    userSettings: UserSettings,
    repository: ShiftRepository,
    autoAppliedReceipt: QrAutoSyncReceipt?,
    onDismiss: () -> Unit,
    onOpenCalendar: () -> Unit,
    onApplyAsOwnShift: () -> Unit,
    onConfirmImport: (showOnCalendar: Boolean, alertCommon: Boolean) -> Unit
) {
    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val dyn = DynamicTheme.colors
    var showSecondaryCalendarStrip by remember { mutableStateOf(true) }
    var notifyCommonLeaveAlert by remember { mutableStateOf(userSettings.commonLeaveAlertEnabled) }

    val isAlreadyApplied = autoAppliedReceipt != null
    val ym = remember { YearMonth.now() }
    val matchResults = remember(colleague, userSettings) {
        repository.calculateShiftMatching(colleague, ym.year, ym.monthValue, userSettings)
    }
    val commonOffCount = matchResults.count { it.matchType == MatchType.COMMON_OFF }
    val commonDutyCount = matchResults.count { it.matchType == MatchType.COMMON_DUTY }
    val swapCount = matchResults.count { it.matchType == MatchType.SWAP_OPPORTUNITY }
    val firstOff = matchResults.firstOrNull { it.matchType == MatchType.COMMON_OFF }
    val firstDuty = matchResults.firstOrNull { it.matchType == MatchType.COMMON_DUTY }
    val dayFmt = remember(appLocale) { DateTimeFormatter.ofPattern("d MMMM EEEE", appLocale) }
    val monthTitle = remember(appLocale) { ym.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy", appLocale)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99000000))
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(dyn.surfaceCard)
                    .border(1.dp, dyn.borderSubtle, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(dyn.borderMetal)
                        .align(Alignment.CenterHorizontally)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    if (isAlreadyApplied) dyn.emeraldBg else dyn.activeChipBg,
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isAlreadyApplied) dyn.emeraldAccent else dyn.accentCyan,
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = if (isAlreadyApplied) dyn.emeraldAccent else dyn.accentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (isAlreadyApplied) trEn("Takvime İşlendi!", "Applied to Calendar!") else trEn("Vardiya Eşleşti!", "Shift Matched!"),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = dyn.textPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isAlreadyApplied) dyn.emeraldBg else dyn.activeChipBg)
                                        .border(
                                            1.dp,
                                            if (isAlreadyApplied) dyn.emeraldBorder else dyn.dayShiftBorder,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isAlreadyApplied) trEn("OTOMATİK SENKRON", "AUTO SYNCED") else trEn("DOĞRULANDI", "VERIFIED"),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAlreadyApplied) dyn.emeraldAccent else dyn.accentCyan
                                    )
                                }
                            }
                            Text(
                                text = trEn(
                                    "${colleague.name} • $monthTitle çizelgesi aktif takvim katmanına eklendi.",
                                    "${colleague.name} • $monthTitle schedule added to active calendar overlay."
                                ),
                                fontSize = 11.sp,
                                color = dyn.textSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(dyn.surfaceElevated, CircleShape)
                            .border(1.dp, dyn.borderSubtle, CircleShape)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = trEn("Kapat", "Close"), tint = dyn.textSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                // Colleague Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(dyn.surfaceElevated)
                        .border(1.dp, dyn.borderMetal, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, dyn.nightShiftAccent, CircleShape)
                    ) {
                        UserAvatarImage(
                            avatarPath = "",
                            contentDescription = colleague.name,
                            displayName = colleague.name,
                            accentColor = dyn.nightShiftAccent,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = colleague.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                        Text(text = colleague.department, fontSize = 11.sp, color = dyn.textSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(dyn.activeChipBg)
                            .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = colleague.cyclePreset.localizedName(isEn), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                    }
                }

                // Live Match Analysis Tiles
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(dyn.dayShiftBg)
                            .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(text = trEn("$commonOffCount Gün", "$commonOffCount Days"), fontSize = 16.sp, fontWeight = FontWeight.Black, color = dyn.dayShiftAccent)
                            Text(text = trEn("Ortak İzin", "Shared Off"), fontSize = 9.sp, color = dyn.textSecondary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(dyn.nightShiftBg)
                            .border(1.dp, dyn.nightShiftBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(text = trEn("$commonDutyCount Gün", "$commonDutyCount Days"), fontSize = 16.sp, fontWeight = FontWeight.Black, color = dyn.nightShiftAccent)
                            Text(text = trEn("Birlikte Nöbet", "Same Shift"), fontSize = 9.sp, color = dyn.textSecondary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(dyn.eveningShiftBg)
                            .border(1.dp, dyn.eveningShiftBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(text = trEn("$swapCount Gün", "$swapCount Days"), fontSize = 16.sp, fontWeight = FontWeight.Black, color = dyn.eveningShiftAccent)
                            Text(text = trEn("Takas Fırsatı", "Swap Option"), fontSize = 9.sp, color = dyn.textSecondary)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(dyn.surfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(6.dp).background(dyn.accentCyan, CircleShape))
                        Text(
                            text = trEn(
                                "İlk Ortak İzin: ${firstOff?.date?.format(dayFmt) ?: "Bu ay yok"}",
                                "Next Shared Off: ${firstOff?.date?.format(dayFmt) ?: "None this month"}"
                            ),
                            fontSize = 11.sp,
                            color = dyn.textPrimary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(6.dp).background(dyn.nightShiftAccent, CircleShape))
                        Text(
                            text = trEn(
                                "İlk Ortak Nöbet: ${firstDuty?.date?.format(dayFmt) ?: "Bu ay yok"}",
                                "Next Shared Shift: ${firstDuty?.date?.format(dayFmt) ?: "None this month"}"
                            ),
                            fontSize = 11.sp,
                            color = dyn.textPrimary
                        )
                    }
                }

                if (!isAlreadyApplied) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(dyn.surfaceElevated)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = trEn("Takvimimde İkincil Şerit Olarak Göster", "Show as Secondary Bar on Calendar"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary, modifier = Modifier.weight(1f))
                        Switch(
                            checked = showSecondaryCalendarStrip,
                            onCheckedChange = { showSecondaryCalendarStrip = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = dyn.primaryNeon, uncheckedTrackColor = dyn.surfacePanel)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(dyn.surfaceElevated)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = trEn("Ortak İzinlerde Ön Bildirim Gönder", "Alert Before Shared Days Off"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary, modifier = Modifier.weight(1f))
                        Switch(
                            checked = notifyCommonLeaveAlert,
                            onCheckedChange = { notifyCommonLeaveAlert = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = dyn.primaryNeon, uncheckedTrackColor = dyn.surfacePanel)
                        )
                    }

                    Button(
                        onClick = { onConfirmImport(showSecondaryCalendarStrip, notifyCommonLeaveAlert) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_confirm_qr_import"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = dyn.primaryNeon)
                    ) {
                        Text(trEn("Takvime İşle ve Eşleştir", "Apply to Calendar & Match"), color = dyn.textOnPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                } else {
                    Button(
                        onClick = onOpenCalendar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_open_calendar_after_qr"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = dyn.primaryNeon)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = dyn.textOnPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(trEn("Aktif Takvimde Gör", "View on Active Calendar"), color = dyn.textOnPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    OutlinedButton(
                        onClick = onApplyAsOwnShift,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_apply_qr_to_own_schedule"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = trEn("Bu Vardiyayı Kendi Takvimim Olarak Uygula", "Apply This Schedule as My Own"),
                            color = dyn.accentCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { onConfirmImport(true, notifyCommonLeaveAlert) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_open_matching_after_qr"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = trEn("Eşleşme ve Ortak İzin Analizini Aç", "Open Match & Shared Off Analysis"),
                            color = dyn.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
