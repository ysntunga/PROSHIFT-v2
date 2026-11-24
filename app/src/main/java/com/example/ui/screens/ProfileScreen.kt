package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CyclePreset
import com.example.data.model.ProfessionGroup
import com.example.ui.components.EditProfileSheet
import com.example.ui.components.PinSecurityDialog
import com.example.ui.components.UserAvatarImage
import com.example.ui.components.savePickedAvatarToInternalStorage
import com.example.ui.components.titaniumBrushedSurface
import com.example.ui.theme.DynamicTheme
import com.example.ui.theme.ProshiftMonoFont
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.i18n.formatCurrencyUnit
import com.example.ui.i18n.localizedName
import com.example.ui.i18n.localizedTitles
import com.example.ui.i18n.trEn
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ShiftViewModel

@Composable
fun ProfileScreen(
    viewModel: ShiftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.userSettings.collectAsState()
    val stats by viewModel.timesheetStats.collectAsState()
    val telemetry by viewModel.dutyTelemetry.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val activeProfileId by viewModel.activeProfileId.collectAsState()

    var showEditProfileSheet by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showAvatarSheet by remember { mutableStateOf(false) }
    var showNewProfileDialog by remember { mutableStateOf(false) }

    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val activeGroup = remember(settings.professionGroup) {
        ProfessionGroup.fromId(settings.professionGroup)
    }
    val displayedTitle = remember(settings.title, isEn) {
        if (!isEn) settings.title else {
            ProfessionGroup.entries.firstNotNullOfOrNull { g ->
                val idx = g.titles.indexOfFirst { it.equals(settings.title, ignoreCase = true) }
                if (idx >= 0) g.localizedTitles(true).getOrNull(idx) else null
            } ?: settings.title
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = savePickedAvatarToInternalStorage(context, uri)
            if (savedPath != null) {
                viewModel.updateSettings(settings.copy(avatarUriOrPath = savedPath))
                Toast.makeText(context, trEn(isEn, "Profil fotoğrafı güncellendi", "Profile photo updated"), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, trEn(isEn, "Fotoğraf yüklenemedi", "Could not load photo"), Toast.LENGTH_SHORT).show()
            }
        }
        showAvatarSheet = false
    }

    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    val dyn = DynamicTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(dyn.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .imePadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 96.dp)
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
                        .size(44.dp)
                        .background(dyn.surfaceElevated, CircleShape)
                        .border(1.dp, dyn.borderSubtle, CircleShape)
                        .clickable { viewModel.navigateTo(AppScreen.HOME) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = trEn(isEn, "Geri", "Back"),
                        tint = dyn.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = trEn(isEn, "Profil", "Profile"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.textPrimary
                    )
                    Text(
                        text = "${activeGroup.emoji} ${activeGroup.localizedName(isEn).uppercase(appLocale)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.accentCyan,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(dyn.surfaceElevated, CircleShape)
                    .border(1.dp, dyn.borderSubtle, CircleShape)
                    .clickable { viewModel.navigateTo(AppScreen.SETTINGS) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = trEn(isEn, "Ayarlar", "Settings"),
                    tint = dyn.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Hero Identity Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .titaniumBrushedSurface(RoundedCornerShape(20.dp), dyn.borderSubtle),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .background(dyn.brushedSurfaceBrush)
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Clickable Avatar + Floating Camera Badge
                Box(
                    modifier = Modifier.size(104.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(2.5.dp, dyn.accentCyan, CircleShape)
                            .clickable { showAvatarSheet = true }
                            .testTag("btn_profile_avatar")
                    ) {
                        UserAvatarImage(
                            avatarPath = settings.avatarUriOrPath,
                            contentDescription = settings.employeeName,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Live Duty Dot (Top-Right)
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .background(if (telemetry.isOnDuty) dyn.accentCyan else dyn.emeraldAccent, CircleShape)
                            .border(2.dp, dyn.surfaceCard, CircleShape)
                    )

                    // Floating Camera Button (Bottom-End, 48dp accessible hit area)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.BottomEnd)
                            .clickable { showAvatarSheet = true }
                            .testTag("btn_change_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(dyn.primaryNeon, CircleShape)
                                .border(2.dp, dyn.surfaceCard, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhotoCamera,
                                contentDescription = trEn(isEn, "Profil Fotoğrafını Değiştir", "Change Profile Photo"),
                                tint = dyn.textOnPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Status & Profession Group Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(dyn.activeChipBg, RoundedCornerShape(20.dp))
                            .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(if (telemetry.isOnDuty) dyn.accentCyan else dyn.emeraldAccent, CircleShape)
                            )
                            Text(
                                text = if (telemetry.isOnDuty) trEn(isEn, "NÖBETTE", "ON DUTY") else trEn(isEn, "DİNLENMEDE", "OFF DUTY"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.accentCyan,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(dyn.surfaceElevated, RoundedCornerShape(20.dp))
                            .border(1.dp, dyn.borderMetal, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${activeGroup.emoji} ${activeGroup.localizedName(isEn)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = settings.employeeName,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.02).sp,
                    color = dyn.textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Title & Department Highlight
                Text(
                    text = displayedTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = dyn.accentCyan
                )
                Text(
                    text = trEn(
                        isEn,
                        "${settings.department} • Kıdem: ${stats.seniorityYears} Yıl",
                        "${settings.department} • Seniority: ${stats.seniorityYears} Yr"
                    ),
                    fontSize = 12.sp,
                    color = dyn.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Single clean primary Edit Profile button (avatar tap already edits photo)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showEditProfileSheet = true }
                        .testTag("btn_edit_profile")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.horizontalGradient(listOf(dyn.primaryNeon, dyn.primaryDeep))),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text(
                                text = trEn(isEn, "Profili Düzenle", "Edit Profile"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card: Çoklu Profil Yönetimi
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .titaniumBrushedSurface(RoundedCornerShape(20.dp), dyn.borderSubtle),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(trEn(isEn, "Çoklu Profil Yönetimi", "Multi-Profile Management"), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                        Text(trEn(isEn, "Farklı görev/vardiya profilleri arasında geçiş yapın", "Switch between different role & shift profiles"), fontSize = 11.sp, color = dyn.textSecondary)
                    }
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showNewProfileDialog = true }
                            .border(1.dp, dyn.accentCyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .testTag("btn_add_profile"),
                        shape = RoundedCornerShape(10.dp),
                        color = dyn.surfaceElevated
                    ) {
                        Text(
                            text = trEn(isEn, "+ Yeni Profil", "+ New Profile"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.accentCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(profiles) { profile ->
                        val isSel = profile.id == activeProfileId
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.switchProfile(profile.id) }
                                .border(1.dp, if (isSel) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(12.dp))
                                .testTag("profile_chip_${profile.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) dyn.activeChipBg else dyn.surfaceElevated
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = profile.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) dyn.accentCyan else dyn.textPrimary
                                    )
                                    Text(
                                        text = profile.department,
                                        fontSize = 10.sp,
                                        color = dyn.textSecondary
                                    )
                                }
                                if (profiles.size > 1 && !isSel) {
                                    Text(
                                        text = "✕",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = dyn.dangerAccent,
                                        modifier = Modifier
                                            .clickable { viewModel.deleteProfile(profile.id) }
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card: Meslek Grubu & Ünvan Kimliği
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .titaniumBrushedSurface(RoundedCornerShape(20.dp), dyn.borderSubtle),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WorkOutline,
                            contentDescription = null,
                            tint = dyn.accentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = trEn(isEn, "Meslek Grubu & Ünvan", "Profession & Role"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(dyn.activeChipBg, RoundedCornerShape(6.dp))
                            .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${settings.hourlyWage.toInt()}${formatCurrencyUnit(isEn)}/${trEn(isEn, "sa", "h")}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.accentCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = trEn(isEn, "SEKTÖR / MESLEK GRUBU SEÇİN", "SELECT SECTOR / PROFESSION"),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = dyn.textMuted,
                    letterSpacing = 0.6.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ProfessionGroup.entries) { group ->
                        val isSelected = activeGroup == group
                        Surface(
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    val isPresetFromAny = ProfessionGroup.entries.any { g ->
                                        g.titles.contains(settings.title) || g.localizedTitles(true).contains(settings.title)
                                    }
                                    val nextTitle = if (settings.title.isBlank() || isPresetFromAny) {
                                        group.localizedTitles(isEn).firstOrNull() ?: settings.title
                                    } else {
                                        settings.title
                                    }
                                    viewModel.updateSettings(
                                        settings.copy(
                                            professionGroup = group.id,
                                            title = nextTitle
                                        )
                                    )
                                }
                                .border(
                                    1.dp,
                                    if (isSelected) dyn.accentCyan else dyn.borderSubtle,
                                    RoundedCornerShape(12.dp)
                                )
                                .testTag("prof_group_${group.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) dyn.activeChipBg else dyn.surfaceElevated
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = group.emoji, fontSize = 15.sp)
                                Column {
                                    Text(
                                        text = group.localizedName(isEn),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) dyn.accentCyan else dyn.textPrimary
                                    )
                                    Text(
                                        text = trEn(isEn, "${group.titles.size} Ünvan", "${group.titles.size} Roles"),
                                        fontSize = 9.sp,
                                        color = dyn.textMuted
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trEn(
                            isEn,
                            "AKTİF ÜNVAN (${activeGroup.localizedName(false).uppercase(appLocale)})",
                            "ACTIVE ROLE (${activeGroup.localizedName(true).uppercase(appLocale)})"
                        ),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.textMuted,
                        letterSpacing = 0.6.sp
                    )
                    Text(
                        text = trEn(isEn, "Özel Yaz →", "Custom →"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.accentCyan,
                        modifier = Modifier
                            .clickable { showEditProfileSheet = true }
                            .padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val localizedRoleTitles = remember(activeGroup, isEn) { activeGroup.localizedTitles(isEn) }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(localizedRoleTitles) { index, roleTitle ->
                        val rawTitle = activeGroup.titles.getOrNull(index)
                        val isSelected = settings.title.equals(roleTitle, ignoreCase = true) ||
                            (rawTitle != null && settings.title.equals(rawTitle, ignoreCase = true))
                        Surface(
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.updateSettings(settings.copy(title = roleTitle))
                                }
                                .border(
                                    1.dp,
                                    if (isSelected) dyn.primaryNeon else dyn.borderSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                                .testTag("prof_title_$index"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) dyn.activeChipBg else dyn.surfacePanel
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Filled.Check else Icons.Filled.Badge,
                                    contentDescription = null,
                                    tint = if (isSelected) dyn.accentCyan else dyn.textMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = roleTitle,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) dyn.accentCyan else dyn.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card: Aktif Vardiya Düzeni & Hızlı Geçiş
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .titaniumBrushedSurface(RoundedCornerShape(20.dp), dyn.borderSubtle),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trEn(isEn, "Aktif Vardiya Düzeni", "Active Shift Pattern"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.textPrimary
                    )

                    Box(
                        modifier = Modifier
                            .background(dyn.activeChipBg, RoundedCornerShape(6.dp))
                            .border(1.dp, dyn.primaryNeon.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = settings.cyclePreset.localizedName(isEn),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.primaryNeon
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hızlı Vardiya Döngü Seçicisi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        CyclePreset.CYCLE_12_24_12_48 to "12/24",
                        CyclePreset.CYCLE_12_36 to "12/36",
                        CyclePreset.CYCLE_24_48 to "24/48",
                        CyclePreset.CYCLE_8_2_2_2 to "2-2-2"
                    ).forEach { (preset, shortTitle) ->
                        val isChosen = settings.cyclePreset == preset
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .titaniumBrushedSurface(
                                    shape = RoundedCornerShape(10.dp),
                                    borderColor = if (isChosen) dyn.primaryNeon else dyn.borderSubtle,
                                    elevated = isChosen
                                )
                                .clickable {
                                    viewModel.updateSettings(
                                        settings.copy(
                                            cyclePreset = preset,
                                            isAnchorConfigured = true
                                        )
                                    )
                                    Toast.makeText(
                                        context,
                                        trEn(isEn, "${preset.localizedName(false)} aktif edildi", "${preset.localizedName(true)} activated"),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .testTag("profile_quick_cycle_${preset.id}"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isChosen) dyn.activeChipBg else Color.Transparent
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = shortTitle,
                                    fontSize = 11.sp,
                                    fontFamily = ProshiftMonoFont,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChosen) dyn.primaryNeon else dyn.textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .titaniumBrushedSurface(RoundedCornerShape(12.dp), dyn.borderSubtle, elevated = true)
                        .clickable { viewModel.navigateTo(AppScreen.WIZARD) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Repeat,
                                contentDescription = null,
                                tint = dyn.accentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = trEn(isEn, "Vardiya Şablonunu Değiştir", "Change Shift Template"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.textPrimary
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = trEn(isEn, "Git", "Go"),
                            tint = dyn.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section: KISAYOLLAR
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = trEn(isEn, "KISAYOLLAR", "SHORTCUTS"),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = dyn.textMuted,
                letterSpacing = 0.6.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .titaniumBrushedSurface(RoundedCornerShape(16.dp), dyn.borderSubtle),
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    // Arkadaş Eşleşme & QR Paylaş
                    ProfileActionRow(
                        icon = Icons.Filled.QrCode2,
                        title = trEn(isEn, "Arkadaş Eşleşme & QR Paylaş", "Shift Match & QR Share"),
                        subtitle = trEn(isEn, "Ortak izinleri gör veya QR kodunu okut", "See common days off or scan a QR code"),
                        onClick = { viewModel.navigateTo(AppScreen.MATCHING_QR) }
                    )

                    ProfileSectionDivider()

                    // Bildirim & Nöbet Alarmı
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(AppScreen.SETTINGS) }
                            .padding(vertical = 8.dp),
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
                                    .size(36.dp)
                                    .background(dyn.surfaceElevated, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = null,
                                    tint = dyn.accentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = trEn(isEn, "Bildirim & Nöbet Alarmı", "Notifications & Shift Alarm"),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = dyn.textPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(dyn.activeChipBg, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (settings.shiftReminderEnabled) trEn(isEn, "• Aktif", "• Active") else trEn(isEn, "Kapalı", "Off"),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (settings.shiftReminderEnabled) dyn.accentCyan else dyn.textMuted
                                        )
                                    }
                                }
                                val leadTimeLabel = if (settings.preShiftAlertMinutes % 60 == 0) {
                                    trEn(isEn, "${settings.preShiftAlertMinutes / 60} sa", "${settings.preShiftAlertMinutes / 60}h")
                                } else {
                                    trEn(isEn, "${settings.preShiftAlertMinutes} dk", "${settings.preShiftAlertMinutes}m")
                                }
                                Text(
                                    text = trEn(
                                        isEn,
                                        "Vardiya öncesi $leadTimeLabel • Günlük ${settings.shiftReminderTime}",
                                        "$leadTimeLabel before shift • Daily ${settings.shiftReminderTime}"
                                    ),
                                    fontSize = 11.sp,
                                    color = dyn.textSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = dyn.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    ProfileSectionDivider()

                    // Puantaj & Bordro Parametreleri
                    ProfileActionRow(
                        icon = Icons.Filled.Calculate,
                        title = trEn(isEn, "Puantaj & Bordro Parametreleri", "Timesheet & Payroll Settings"),
                        subtitle = trEn(
                            isEn,
                            "Gündüz ${settings.gunduzStartTime}-${settings.gunduzEndTime} • Gece ${settings.geceStartTime}-${settings.geceEndTime}",
                            "Day ${settings.gunduzStartTime}-${settings.gunduzEndTime} • Night ${settings.geceStartTime}-${settings.geceEndTime}"
                        ),
                        onClick = { viewModel.navigateTo(AppScreen.TIMESHEET) }
                    )

                    ProfileSectionDivider()

                    // Takvim Entegrasyonu (Persisted)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
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
                                    .size(36.dp)
                                    .background(dyn.surfaceElevated, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Sync,
                                    contentDescription = null,
                                    tint = dyn.accentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = trEn(isEn, "Takvim Entegrasyonu", "Calendar Integration"),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.textPrimary
                                )
                                Text(
                                    text = trEn(isEn, "Resmi tatil & yerel takvim eşitlemesi", "Public holidays & local calendar sync"),
                                    fontSize = 11.sp,
                                    color = dyn.textSecondary
                                )
                            }
                        }

                        Switch(
                            checked = settings.calendarSyncEnabled,
                            onCheckedChange = {
                                viewModel.updateSettings(settings.copy(calendarSyncEnabled = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = dyn.textOnPrimary,
                                checkedTrackColor = dyn.primaryNeon,
                                uncheckedThumbColor = dyn.textMuted,
                                uncheckedTrackColor = dyn.surfacePanel
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section: GÜVENLİK & VERİ
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = trEn(isEn, "GÜVENLİK & VERİ", "SECURITY & DATA"),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = dyn.textMuted,
                letterSpacing = 0.6.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .titaniumBrushedSurface(RoundedCornerShape(16.dp), dyn.borderSubtle),
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    // Yerel JSON Yedekleme
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
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
                                    .size(36.dp)
                                    .background(dyn.surfaceElevated, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Storage,
                                    contentDescription = null,
                                    tint = dyn.accentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = trEn(isEn, "Yerel Veri Yedekleme (JSON)", "Local Data Backup (JSON)"),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.textPrimary
                                )
                                Text(
                                    text = trEn(isEn, "Tüm nöbet ve ayarları yedek olarak dışa aktar", "Export all shifts and settings as a backup"),
                                    fontSize = 11.sp,
                                    color = dyn.textSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = dyn.activeChipBg,
                            modifier = Modifier
                                .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    val backupJson = viewModel.repository.exportFullBackupJson()
                                    viewModel.sharePlainText(
                                        trEn(isEn, "PROSHIFT Yerel Yedek (.json)", "PROSHIFT Local Backup (.json)"),
                                        backupJson
                                    )
                                }
                                .testTag("btn_backup_json")
                        ) {
                            Text(
                                text = trEn(isEn, "Yedek Al", "Backup"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.accentCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                            )
                        }
                    }

                    ProfileSectionDivider()

                    // Biyometrik / PIN Kilit
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPinDialog = true }
                            .padding(vertical = 8.dp)
                            .testTag("row_pin_security"),
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
                                    .size(36.dp)
                                    .background(dyn.surfaceElevated, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Fingerprint,
                                    contentDescription = null,
                                    tint = dyn.accentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = trEn(isEn, "Biyometrik / PIN Kilidi", "Biometric / PIN Lock"),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.textPrimary
                                )
                                Text(
                                    text = if (settings.isPinSecurityEnabled) {
                                        trEn(isEn, "4 haneli güvenlik kilidi etkin", "4-digit security lock active")
                                    } else {
                                        trEn(isEn, "Vardiya defteri erişim koruması ekle", "Protect access to your shift log")
                                    },
                                    fontSize = 11.sp,
                                    color = dyn.textSecondary
                                )
                            }
                        }

                        Text(
                            text = if (settings.isPinSecurityEnabled) trEn(isEn, "Aktif", "Active") else trEn(isEn, "Kapalı", "Off"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (settings.isPinSecurityEnabled) dyn.accentCyan else dyn.textMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Başlangıç Ekranına Dön
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    Toast.makeText(context, trEn(isEn, "Karşılama ekranına dönüldü", "Returned to welcome screen"), Toast.LENGTH_SHORT).show()
                    viewModel.navigateTo(AppScreen.WELCOME)
                }
                .border(1.dp, dyn.borderMetal, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = dyn.surfaceCard
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = trEn(isEn, "Karşılamaya Dön", "Return to Welcome"),
                    tint = dyn.dangerAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = trEn(isEn, "Başlangıç Ekranına Dön", "Return to Welcome Screen"),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = dyn.textPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = trEn(
                    isEn,
                    "PROSHIFT • Verileriniz yalnızca cihazınızda saklanır.",
                    "PROSHIFT • Your data stays exclusively on your device."
                ),
                fontSize = 10.sp,
                color = dyn.textMuted
            )
        }
    }

    if (showAvatarSheet) {
        AlertDialog(
            onDismissRequest = { showAvatarSheet = false },
            containerColor = dyn.surfaceCard,
            title = {
                Text(
                    trEn(isEn, "Profil Fotoğrafı Seç", "Select Profile Photo"),
                    color = dyn.textPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .border(1.dp, dyn.accentCyan, RoundedCornerShape(12.dp))
                            .testTag("dialog_pick_gallery_avatar"),
                        shape = RoundedCornerShape(12.dp),
                        color = dyn.activeChipBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(20.dp))
                            Text(trEn(isEn, "Galeriden Fotoğraf Seç", "Choose Photo from Gallery"), color = dyn.accentCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Text(trEn(isEn, "HAZIR AVATARLAR", "PRESET AVATARS"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            "" to trEn(isEn, "Operatör", "Operator"),
                            "preset:kaan" to trEn(isEn, "Klinik Uzmanı", "Clinician")
                        ).forEach { (presetKey, label) ->
                            val isSel = settings.avatarUriOrPath == presetKey
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.updateSettings(settings.copy(avatarUriOrPath = presetKey))
                                        showAvatarSheet = false
                                    }
                                    .border(1.dp, if (isSel) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) dyn.activeChipBg else dyn.surfaceElevated
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, if (isSel) dyn.accentCyan else dyn.borderMetal, CircleShape)
                                    ) {
                                        UserAvatarImage(
                                            avatarPath = presetKey,
                                            contentDescription = label,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) dyn.accentCyan else dyn.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    if (settings.avatarUriOrPath.isNotBlank()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.updateSettings(settings.copy(avatarUriOrPath = ""))
                                    showAvatarSheet = false
                                }
                                .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            color = dyn.surfaceElevated
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = dyn.textSecondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(trEn(isEn, "Varsayılan Avatara Sıfırla", "Reset to Default Avatar"), color = dyn.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarSheet = false }) {
                    Text(trEn(isEn, "Kapat", "Close"), color = dyn.textSecondary)
                }
            }
        )
    }

    if (showEditProfileSheet) {
        EditProfileSheet(
            currentSettings = settings,
            onDismiss = { showEditProfileSheet = false },
            onSave = { updated -> viewModel.updateSettings(updated) }
        )
    }

    if (showPinDialog) {
        PinSecurityDialog(
            enabled = settings.isPinSecurityEnabled,
            currentPin = settings.pinCode,
            onDismiss = { showPinDialog = false },
            onSave = { enabled, pin ->
                viewModel.updateSettings(settings.copy(isPinSecurityEnabled = enabled, pinCode = pin))
            }
        )
    }

    if (showNewProfileDialog) {
        var newName by remember { mutableStateOf("") }
        var newDept by remember { mutableStateOf("") }
        var newTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewProfileDialog = false },
            containerColor = dyn.surfaceCard,
            title = { Text(trEn(isEn, "Yeni Vardiya Profili", "New Shift Profile"), color = dyn.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(trEn(isEn, "Ad Soyad *", "Full Name *"), fontSize = 11.sp, color = dyn.textSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = dyn.accentCyan,
                            unfocusedBorderColor = dyn.borderMetal,
                            focusedTextColor = dyn.textPrimary,
                            unfocusedTextColor = dyn.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDept,
                        onValueChange = { newDept = it },
                        label = { Text(trEn(isEn, "Birim / Bölüm", "Unit / Department"), fontSize = 11.sp, color = dyn.textSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = dyn.accentCyan,
                            unfocusedBorderColor = dyn.borderMetal,
                            focusedTextColor = dyn.textPrimary,
                            unfocusedTextColor = dyn.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text(trEn(isEn, "Ünvan / Görev", "Title / Role"), fontSize = 11.sp, color = dyn.textSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = dyn.accentCyan,
                            unfocusedBorderColor = dyn.borderMetal,
                            focusedTextColor = dyn.textPrimary,
                            unfocusedTextColor = dyn.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createProfile(
                            name = newName.ifBlank { trEn(isEn, "Ek Profil ${profiles.size + 1}", "Extra Profile ${profiles.size + 1}") },
                            department = newDept.ifBlank { settings.department },
                            title = newTitle.ifBlank { settings.title },
                            cyclePreset = settings.cyclePreset
                        )
                        showNewProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dyn.primaryNeon)
                ) {
                    Text(trEn(isEn, "Oluştur", "Create"), color = dyn.textOnPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProfileDialog = false }) {
                    Text(trEn(isEn, "Vazgeç", "Cancel"), color = dyn.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun ProfileActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val dyn = DynamicTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
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
                    .size(36.dp)
                    .background(dyn.surfaceElevated, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = dyn.accentCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = dyn.textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = dyn.textSecondary
                )
            }
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = dyn.textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ProfileSectionDivider() {
    val dyn = DynamicTheme.colors
    Spacer(modifier = Modifier.height(6.dp))
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dyn.borderSubtle))
    Spacer(modifier = Modifier.height(6.dp))
}
