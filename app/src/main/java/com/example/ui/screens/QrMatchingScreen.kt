package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MatchType
import com.example.data.model.ShiftMatchDay
import com.example.ui.components.ManualColleagueSheet
import com.example.ui.components.UserAvatarImage
import com.example.ui.theme.DynamicTheme
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.i18n.formatMonthTitleLocalized
import com.example.ui.i18n.localizedName
import com.example.ui.i18n.localizedNote
import com.example.ui.i18n.localizedTitle
import com.example.ui.i18n.trEn
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ShiftViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun QrMatchingScreen(
    viewModel: ShiftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val settings by viewModel.userSettings.collectAsState()
    val colleagues by viewModel.colleagues.collectAsState()
    val selectedColleague by viewModel.selectedColleague.collectAsState()
    val matchResults by viewModel.matchResults.collectAsState()
    val stats by viewModel.timesheetStats.collectAsState()
    val localizedMonthTitle = remember(matchResults, appLocale, stats.monthTitle) {
        val refDate = matchResults.firstOrNull()?.date ?: LocalDate.now()
        formatMonthTitleLocalized(refDate.year, refDate.monthValue, appLocale)
    }

    var selectedMatrixFilter by remember { mutableIntStateOf(0) } // 0: Ortak İzin, 1: Birlikte Nöbet, 2: Tümü
    var showColleaguePicker by remember { mutableStateOf(false) }
    var showManualAddSheet by remember { mutableStateOf(false) }

    val commonOffList = remember(matchResults) { matchResults.filter { it.matchType == MatchType.COMMON_OFF } }
    val commonDutyList = remember(matchResults) { matchResults.filter { it.matchType == MatchType.COMMON_DUTY } }
    val swapOppList = remember(matchResults) { matchResults.filter { it.matchType == MatchType.SWAP_OPPORTUNITY } }

    val displayedMatches = remember(matchResults, selectedMatrixFilter) {
        when (selectedMatrixFilter) {
            0 -> commonOffList
            1 -> commonDutyList
            else -> matchResults
        }
    }

    BackHandler {
        viewModel.navigateBack(fallback = AppScreen.HOME)
    }

    val activeColleague = selectedColleague
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
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
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

                Text(
                    text = trEn(isEn, "Arkadaş Eşleşme &\nOrtak İzinler", "Shift Matching &\nCommon Days Off"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = dyn.textPrimary,
                    lineHeight = 19.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(dyn.surfaceElevated, RoundedCornerShape(10.dp))
                        .border(1.dp, dyn.borderSubtle, RoundedCornerShape(10.dp))
                        .clickable {
                            selectedMatrixFilter = (selectedMatrixFilter + 1) % 3
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = trEn(isEn, "Filtreyi Değiştir", "Cycle Filter"),
                        tint = dyn.accentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Add Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(dyn.surfaceElevated, RoundedCornerShape(10.dp))
                        .border(1.dp, dyn.borderSubtle, RoundedCornerShape(10.dp))
                        .clickable {
                            viewModel.navigateTo(AppScreen.QR_SCANNER)
                        }
                        .testTag("btn_add_colleague_qr"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = trEn(isEn, "QR ile Arkadaş Ekle", "Add Friend via QR"),
                        tint = dyn.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Profile Avatar Circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, dyn.primaryNeon, CircleShape)
                        .clickable { viewModel.navigateTo(AppScreen.PROFILE) }
                ) {
                    UserAvatarImage(
                        avatarPath = settings.avatarUriOrPath,
                        contentDescription = trEn(isEn, "Profil", "Profile"),
                        displayName = settings.employeeName,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ARKADAŞ EŞLEŞME & ORTAK İZİNLER
        run {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(dyn.accentCyan, CircleShape))
                        Text(
                            text = trEn(isEn, "ÇEVRİMDIŞI QR EŞLEŞME", "OFFLINE QR MATCH"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.accentCyan,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        text = "v${com.example.BuildConfig.VERSION_NAME}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.textMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = trEn(isEn, "Arkadaş Vardiya Eşleştirme &\nOrtak İzin Dedektörü", "Colleague Shift Matching &\nCommon Off-Day Finder"),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.02).sp,
                    color = dyn.textPrimary,
                    lineHeight = 28.sp
                )

                Text(
                    text = trEn(
                        isEn,
                        "Mesai arkadaşınla vardiyanı karşılaştır, ortak boş günlerinizi keşfet veya QR ile yeni arkadaş ekle.",
                        "Compare shifts with a colleague, discover common days off, or add someone via QR."
                    ),
                    fontSize = 12.sp,
                    color = dyn.textSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Connection Block (Live User ⇄ Clickable Colleague Switcher)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = dyn.surfaceCard
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, dyn.borderSubtle, RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left User
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
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
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = trEn(isEn, "SEN", "YOU"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                                Text(
                                    text = settings.employeeName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = dyn.textPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = settings.cyclePreset.localizedName(isEn),
                                    fontSize = 10.sp,
                                    color = dyn.textSecondary,
                                    maxLines = 1
                                )
                            }

                            // Middle
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(dyn.activeChipBg, CircleShape)
                                        .border(1.5.dp, dyn.dayShiftBorder, CircleShape)
                                        .clickable { showColleaguePicker = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.SwapHoriz,
                                        contentDescription = trEn(isEn, "Arkadaş Değiştir", "Switch Colleague"),
                                        tint = dyn.accentCyan,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = trEn(isEn, "KİŞİ SEÇ", "SELECT"),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.accentCyan,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Right User: Selected Colleague or Empty Add Placeholder
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showColleaguePicker = true }
                                    .padding(4.dp)
                                    .testTag("btn_select_colleague")
                            ) {
                                if (activeColleague != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, dyn.nightShiftAccent, CircleShape)
                                    ) {
                                        UserAvatarImage(
                                            avatarPath = "",
                                            contentDescription = activeColleague.name,
                                            displayName = activeColleague.name,
                                            accentColor = dyn.nightShiftAccent,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = trEn(isEn, "EŞLEŞEN", "MATCHED"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.nightShiftAccent)
                                    Text(
                                        text = activeColleague.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = dyn.textPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = activeColleague.cyclePreset.localizedName(isEn),
                                        fontSize = 10.sp,
                                        color = dyn.textSecondary,
                                        maxLines = 1
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(dyn.activeChipBg)
                                            .border(2.dp, dyn.accentCyan.copy(alpha = 0.65f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PersonAdd,
                                            contentDescription = trEn(isEn, "Arkadaş Ekle", "Add Friend"),
                                            tint = dyn.accentCyan,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = trEn(isEn, "ARKADAŞ EKLE", "ADD FRIEND"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                                    Text(
                                        text = trEn(isEn, "Henüz kişi yok", "No one yet"),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = dyn.textPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = trEn(isEn, "QR veya Elle", "QR or Manual"),
                                        fontSize = 10.sp,
                                        color = dyn.textSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Button 1: QR Kod ile Arkadaş Cihazından İçe Aktar
                        Surface(
                            onClick = { viewModel.navigateTo(AppScreen.QR_SCANNER) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_open_qr_scanner"),
                            shape = RoundedCornerShape(12.dp),
                            color = dyn.primaryDeep
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = trEn(isEn, "QR Kod ile Arkadaş Cihazından İçe Aktar", "Import from Friend's Device via QR"),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Button 2 & 3: Eşleşen Arkadaşı Değiştir + Elle Arkadaş Ekle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showColleaguePicker = true }
                                    .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                color = dyn.surfaceElevated
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = Icons.Filled.Link, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = trEn(isEn, "Kişi Seç (${colleagues.size})", "People (${colleagues.size})"),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = dyn.textPrimary,
                                        maxLines = 1
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showManualAddSheet = true }
                                    .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(12.dp))
                                    .testTag("btn_manual_add_from_matching"),
                                shape = RoundedCornerShape(12.dp),
                                color = dyn.activeChipBg
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = Icons.Filled.PersonAdd, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = trEn(isEn, "Elle Ekle", "Add Manual"),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = dyn.accentCyan,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (activeColleague == null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, dyn.borderSubtle, RoundedCornerShape(18.dp))
                            .testTag("empty_colleagues_card"),
                        shape = RoundedCornerShape(18.dp),
                        color = dyn.surfaceCard
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(dyn.activeChipBg, CircleShape)
                                    .border(1.5.dp, dyn.dayShiftBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PersonAdd,
                                    contentDescription = null,
                                    tint = dyn.accentCyan,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                text = trEn(isEn, "Henüz Arkadaş Eklenmedi", "No Friends Added Yet"),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = dyn.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = trEn(
                                    isEn,
                                    "Ortak izin günlerini ve birlikte nöbet analizini görmek için arkadaşının QR kodunu tara veya elle yeni kişi ekle.",
                                    "Scan a friend's QR code or add someone manually to view common days off and shared shifts."
                                ),
                                fontSize = 12.sp,
                                color = dyn.textSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp
                            )
                        }
                    }
                } else {
                    // 3 Live Stat Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            color = dyn.surfaceCard
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.EventAvailable,
                                        contentDescription = trEn(isEn, "Ortak İzin", "Common Off"),
                                        tint = dyn.dayShiftAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(dyn.dayShiftBg, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = trEn(isEn, "Ortak", "Shared"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.dayShiftAccent)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "${commonOffList.size}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = dyn.textPrimary)
                                Text(text = trEn(isEn, "Ortak İzin", "Common Off"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                                Text(text = localizedMonthTitle, fontSize = 10.sp, color = dyn.accentCyan, maxLines = 1)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            color = dyn.surfaceCard
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.People,
                                        contentDescription = trEn(isEn, "Birlikte Nöbet", "Same Shift"),
                                        tint = dyn.nightShiftAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(dyn.nightShiftBg, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = trEn(isEn, "Nöbet", "Shift"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.nightShiftAccent)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "${commonDutyList.size}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = dyn.textPrimary)
                                Text(text = trEn(isEn, "Birlikte Nöbet", "Same Shift"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                                Text(text = trEn(isEn, "Aynı Vardiya", "On Duty Together"), fontSize = 10.sp, color = dyn.textSecondary)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            color = dyn.surfaceCard
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.SwapHoriz,
                                        contentDescription = trEn(isEn, "Zıt / Takas", "Opposite / Swap"),
                                        tint = dyn.eveningShiftAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(dyn.eveningShiftBg, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = trEn(isEn, "Farklı", "Swap"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.eveningShiftAccent)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "${swapOppList.size}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = dyn.textPrimary)
                                Text(text = trEn(isEn, "Zıt / Takas", "Opposite / Swap"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                                Text(text = trEn(isEn, "Fırsat Günü", "Swap Option"), fontSize = 10.sp, color = dyn.textSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = trEn(isEn, "TAKVİM ÇAKIŞMASI", "SCHEDULE OVERLAP"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.textMuted,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = trEn(isEn, "$localizedMonthTitle — Ortak Gün Özeti", "$localizedMonthTitle — Shared Schedule"),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.textPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(dyn.surfaceElevated)
                                .border(1.dp, dyn.borderSubtle, RoundedCornerShape(10.dp))
                                .clickable { viewModel.navigateTo(AppScreen.CALENDAR) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Filled.DateRange, contentDescription = trEn(isEn, "Takvimi Aç", "Open Calendar"), tint = dyn.accentCyan, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3 Filter Pills with live counts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1.3f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedMatrixFilter = 0 }
                                .border(1.dp, if (selectedMatrixFilter == 0) dyn.dayShiftAccent else dyn.borderSubtle, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedMatrixFilter == 0) dyn.dayShiftBg else dyn.surfaceElevated
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = trEn(isEn, "Ortak (${commonOffList.size})", "Off (${commonOffList.size})"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedMatrixFilter == 0) dyn.dayShiftAccent else dyn.textSecondary)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1.3f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedMatrixFilter = 1 }
                                .border(1.dp, if (selectedMatrixFilter == 1) dyn.nightShiftAccent else dyn.borderSubtle, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedMatrixFilter == 1) dyn.nightShiftBg else dyn.surfaceElevated
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = trEn(isEn, "Nöbet (${commonDutyList.size})", "Shift (${commonDutyList.size})"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedMatrixFilter == 1) dyn.nightShiftAccent else dyn.textSecondary)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedMatrixFilter = 2 }
                                .border(1.dp, if (selectedMatrixFilter == 2) dyn.textPrimary else dyn.borderSubtle, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedMatrixFilter == 2) dyn.surfacePanel else dyn.surfaceElevated
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = trEn(isEn, "Tümü (${matchResults.size})", "All (${matchResults.size})"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedMatrixFilter == 2) dyn.textPrimary else dyn.textSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Timeline Strip Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = trEn(isEn, "${localizedMonthTitle.uppercase(appLocale)} ŞERİDİ", "${localizedMonthTitle.uppercase(appLocale)} STRIP"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(modifier = Modifier.size(5.dp).background(dyn.dayShiftAccent, CircleShape))
                                Text(text = trEn(isEn, "Ortak İzin", "Common Off"), fontSize = 10.sp, color = dyn.textSecondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(modifier = Modifier.size(5.dp).background(dyn.nightShiftAccent, CircleShape))
                                Text(text = trEn(isEn, "Nöbet", "Shift"), fontSize = 10.sp, color = dyn.textSecondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(modifier = Modifier.size(5.dp).background(dyn.eveningShiftAccent, CircleShape))
                                Text(text = trEn(isEn, "Zıt", "Swap"), fontSize = 10.sp, color = dyn.textSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dynamic Horizontal Match Strip
                    val dayAbbrFmt = remember(appLocale) { DateTimeFormatter.ofPattern("EEE", appLocale) }
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(matchResults) { match ->
                            val isCommonOff = match.matchType == MatchType.COMMON_OFF
                            val isCommonDuty = match.matchType == MatchType.COMMON_DUTY
                            val badgeColor = when (match.matchType) {
                                MatchType.COMMON_OFF -> dyn.dayShiftAccent
                                MatchType.COMMON_DUTY -> dyn.nightShiftAccent
                                MatchType.SWAP_OPPORTUNITY -> dyn.eveningShiftAccent
                            }
                            val stripBg = when (match.matchType) {
                                MatchType.COMMON_OFF -> dyn.dayShiftBg
                                MatchType.COMMON_DUTY -> dyn.nightShiftBg
                                MatchType.SWAP_OPPORTUNITY -> dyn.surfaceCard
                            }
                            val stripBorder = when (match.matchType) {
                                MatchType.COMMON_OFF -> dyn.dayShiftBorder
                                MatchType.COMMON_DUTY -> dyn.nightShiftBorder
                                MatchType.SWAP_OPPORTUNITY -> dyn.borderSubtle
                            }
                            val lbl = when (match.matchType) {
                                MatchType.COMMON_OFF -> trEn(isEn, "İzin", "Off")
                                MatchType.COMMON_DUTY -> trEn(isEn, "Nöbet", "Shift")
                                MatchType.SWAP_OPPORTUNITY -> trEn(isEn, "Zıt", "Swap")
                            }
                            Surface(
                                modifier = Modifier
                                    .width(48.dp)
                                    .border(1.dp, stripBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        Toast.makeText(
                                            context,
                                            "${match.date.dayOfMonth} $localizedMonthTitle: ${match.localizedNote(activeColleague.name, isEn)}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = stripBg
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "%02d".format(match.date.dayOfMonth),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isCommonOff) dyn.dayShiftText else if (isCommonDuty) dyn.nightShiftText else dyn.textPrimary
                                    )
                                    Text(
                                        text = match.date.format(dayAbbrFmt).take(3),
                                        fontSize = 10.sp,
                                        color = dyn.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = lbl, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dynamic Matched Day Cards (first 10 filtered items)
                    if (displayedMatches.isEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, dyn.borderSubtle, RoundedCornerShape(14.dp)),
                            shape = RoundedCornerShape(14.dp),
                            color = dyn.surfaceCard
                        ) {
                            Box(modifier = Modifier.padding(18.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = trEn(isEn, "Bu filtre için eşleşen gün bulunamadı.", "No matching days found for this filter."),
                                    fontSize = 12.sp,
                                    color = dyn.textSecondary
                                )
                            }
                        }
                    } else {
                        displayedMatches.take(10).forEach { match ->
                            MatchDayDetailCard(
                                match = match,
                                userName = settings.employeeName.substringBeforeLast(" "),
                                colleagueName = activeColleague.name.substringBeforeLast(" "),
                                onReminderClick = {
                                    com.example.data.notification.ShiftReminderScheduler.scheduleCommonOffReminder(
                                        context = context,
                                        targetDate = match.date,
                                        colleagueName = activeColleague.name
                                    )
                                    val fmtDate = match.date.format(DateTimeFormatter.ofPattern("d MMMM", appLocale))
                                    Toast.makeText(
                                        context,
                                        trEn(isEn, "$fmtDate için ortak plan hatırlatıcısı kuruldu", "Shared plan reminder set for $fmtDate"),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // SECTION: Takvim Katman & Aktarım Tercihleri (Persisted via UserSettings)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = dyn.surfaceCard
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, dyn.borderSubtle, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Layers, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(18.dp))
                            Column {
                                Text(
                                    text = trEn(isEn, "Takvim Katman & Aktarım Tercihleri", "Calendar Layer & Sync Preferences"),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.textPrimary
                                )
                                Text(
                                    text = trEn(isEn, "Kişisel takvim görünümünde arkadaş senkronu", "Friend overlay in your personal calendar view"),
                                    fontSize = 10.sp,
                                    color = dyn.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeColleague?.let {
                                        trEn(
                                            isEn,
                                            "${it.name.substringBefore(" ")} Vardiyasını Takvimimde Göster",
                                            "Show ${it.name.substringBefore(" ")}'s Shifts on Calendar"
                                        )
                                    } ?: trEn(isEn, "Arkadaş Vardiyasını Takvimimde Göster", "Show Friend's Shifts on My Calendar"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.textPrimary
                                )
                                Text(
                                    text = trEn(
                                        isEn,
                                        "Kişisel vardiya kartlarının arkasında ikincil şerit olarak akar.",
                                        "Appears as a secondary strip alongside your personal shift cards."
                                    ),
                                    fontSize = 10.sp,
                                    color = dyn.textSecondary
                                )
                            }

                            Switch(
                                checked = settings.showFriendOnCalendar,
                                onCheckedChange = {
                                    viewModel.updateSettings(settings.copy(showFriendOnCalendar = it))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = dyn.textOnPrimary,
                                    checkedTrackColor = dyn.primaryNeon,
                                    uncheckedThumbColor = dyn.textMuted,
                                    uncheckedTrackColor = dyn.surfacePanel
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = trEn(isEn, "Renk Katmanı Stili", "Color Layer Style"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                            Box(
                                modifier = Modifier
                                    .background(dyn.activeChipBg, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                val styleBadge = when (settings.colorLayerStyle) {
                                    0 -> trEn(isEn, "Neon Cyan Şerit", "Neon Cyan Strip")
                                    1 -> trEn(isEn, "Kehribar Kesikli", "Amber Dashed")
                                    else -> trEn(isEn, "İndigo Matris", "Indigo Matrix")
                                }
                                Text(text = styleBadge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                trEn(isEn, "Neon Cyan", "Neon Cyan"),
                                trEn(isEn, "Kehribar Kesik", "Amber Dash"),
                                trEn(isEn, "İndigo Matris", "Indigo Matrix")
                            ).forEachIndexed { idx, styleName ->
                                val isSelected = settings.colorLayerStyle == idx
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.updateSettings(settings.copy(colorLayerStyle = idx))
                                        }
                                        .border(1.dp, if (isSelected) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(8.dp)),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) dyn.activeChipBg else dyn.surfaceElevated
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    when (idx) {
                                                        0 -> dyn.dayShiftAccent
                                                        1 -> dyn.eveningShiftAccent
                                                        else -> dyn.nightShiftAccent
                                                    },
                                                    CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = styleName,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) dyn.accentCyan else dyn.textSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = trEn(isEn, "Ortak İzin Bildirimi", "Common Off-Day Alert"),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = dyn.textPrimary
                                    )
                                    Box(modifier = Modifier.size(5.dp).background(dyn.accentCyan, CircleShape))
                                }
                                Text(
                                    text = trEn(
                                        isEn,
                                        "İkinizin de aynı anda boş olduğu günlerden 24 saat önce hatırlatma gönder.",
                                        "Send a reminder 24 hours before days when you are both off duty."
                                    ),
                                    fontSize = 10.sp,
                                    color = dyn.textSecondary,
                                    lineHeight = 13.sp
                                )
                            }

                            Switch(
                                checked = settings.commonLeaveAlertEnabled,
                                onCheckedChange = {
                                    viewModel.updateSettings(settings.copy(commonLeaveAlertEnabled = it))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = dyn.textOnPrimary,
                                    checkedTrackColor = dyn.primaryNeon,
                                    uncheckedThumbColor = dyn.textMuted,
                                    uncheckedTrackColor = dyn.surfacePanel
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, dyn.borderSubtle, RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp),
                            color = dyn.surfaceElevated
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(16.dp))
                                Text(
                                    text = trEn(
                                        isEn,
                                        "Aktarılan vardiya verileri cihazlar arası yerel depolamada saklanır, harici sunucuya gönderilmez.",
                                        "Imported shift data is stored locally on your device and never sent to external servers."
                                    ),
                                    fontSize = 10.sp,
                                    color = dyn.textSecondary,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showColleaguePicker) {
        AlertDialog(
            onDismissRequest = { showColleaguePicker = false },
            containerColor = dyn.surfaceCard,
            title = { Text(trEn(isEn, "Mesai Arkadaşı Seç", "Select Colleague"), color = dyn.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                if (colleagues.isEmpty()) {
                    Text(
                        text = trEn(
                            isEn,
                            "Kayıtlı arkadaş bulunmuyor. QR kod tarayarak veya elle yeni kişi ekleyebilirsiniz.",
                            "No saved colleagues yet. Add someone by scanning a QR code or entering manually."
                        ),
                        color = dyn.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.testTag("colleague_picker_empty_text")
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colleagues.forEach { col ->
                            val isSel = activeColleague != null &&
                                col.name == activeColleague.name &&
                                col.qrCodeId == activeColleague.qrCodeId
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.selectColleague(col)
                                        showColleaguePicker = false
                                    }
                                    .border(1.dp, if (isSel) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(10.dp)),
                                color = if (isSel) dyn.activeChipBg else dyn.surfaceElevated
                            ) {
                                Row(
                                    modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(col.name, color = dyn.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${col.department} • ${col.cyclePreset.localizedName(isEn)}", color = dyn.textSecondary, fontSize = 11.sp)
                                    }
                                    if (isSel) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = trEn(isEn, "Seçili", "Selected"),
                                            tint = dyn.accentCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeColleague(col) },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("btn_delete_colleague_${col.qrCodeId}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteOutline,
                                            contentDescription = trEn(isEn, "${col.name} sil", "Delete ${col.name}"),
                                            tint = dyn.dangerAccent,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showColleaguePicker = false
                        viewModel.navigateTo(AppScreen.QR_SCANNER)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dyn.primaryNeon)
                ) {
                    Text(trEn(isEn, "QR ile Yeni Kişi Ekle", "Add via QR"), color = dyn.textOnPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showColleaguePicker = false
                        showManualAddSheet = true
                    }
                ) {
                    Text(trEn(isEn, "Elle Ekle", "Add Manual"), color = dyn.accentCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showManualAddSheet) {
        ManualColleagueSheet(
            onDismiss = { showManualAddSheet = false },
            onSave = { profile ->
                viewModel.addAndSelectColleague(profile)
                Toast.makeText(
                    context,
                    trEn(isEn, "${profile.name} arkadaş listene eklendi", "${profile.name} added to your friends list"),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

@Composable
private fun MatchDayDetailCard(
    match: ShiftMatchDay,
    userName: String,
    colleagueName: String,
    onReminderClick: () -> Unit
) {
    val dyn = DynamicTheme.colors
    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val dayAbbr = match.date.format(DateTimeFormatter.ofPattern("EEE", appLocale)).uppercase(appLocale)
    val dayNum = "%02d".format(match.date.dayOfMonth)
    val isWeekend = match.date.dayOfWeek.value >= 6

    val (badgeText, badgeColor, badgeBg, borderCol) = when (match.matchType) {
        MatchType.COMMON_OFF -> if (isWeekend) {
            MatchBadgeStyle(
                trEn(isEn, "HAFTA SONU ORTAK İZİN", "WEEKEND COMMON OFF"),
                dyn.nightShiftAccent,
                dyn.nightShiftBg,
                dyn.nightShiftBorder
            )
        } else {
            MatchBadgeStyle(
                trEn(isEn, "TAM GÜN ORTAK İZİN", "FULL DAY COMMON OFF"),
                dyn.dayShiftAccent,
                dyn.dayShiftBg,
                dyn.dayShiftBorder
            )
        }
        MatchType.COMMON_DUTY -> MatchBadgeStyle(
            trEn(isEn, "AYNI VARDİYA • BİRLİKTE NÖBET", "SAME SHIFT • ON DUTY TOGETHER"),
            dyn.nightShiftAccent,
            dyn.nightShiftBg,
            dyn.borderSubtle
        )
        MatchType.SWAP_OPPORTUNITY -> MatchBadgeStyle(
            trEn(isEn, "ZIT VARDİYA • TAKAS FIRSATI", "OPPOSITE SHIFT • SWAP OPTION"),
            dyn.eveningShiftAccent,
            dyn.eveningShiftBg,
            dyn.borderSubtle
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(16.dp),
        color = dyn.surfaceCard
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, borderCol, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = dayAbbr, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                        Text(text = dayNum, fontSize = 20.sp, fontWeight = FontWeight.Black, color = dyn.textPrimary)
                    }
                    Column {
                        Box(
                            modifier = Modifier
                                .background(badgeBg, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = badgeText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = match.localizedNote(colleagueName, isEn), fontSize = 11.sp, color = dyn.textSecondary, maxLines = 1)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(dyn.surfaceElevated, CircleShape)
                        .border(1.dp, dyn.borderSubtle, CircleShape)
                        .clickable { onReminderClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.NotificationsActive, contentDescription = trEn(isEn, "Hatırlat", "Remind"), tint = dyn.accentCyan, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, dyn.borderSubtle, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    color = dyn.surfaceElevated
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (match.userShift.isWork) Icons.Filled.WbSunny else Icons.Filled.LocalCafe,
                            contentDescription = null,
                            tint = if (match.userShift.isWork) dyn.dayShiftAccent else dyn.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(text = userName, fontSize = 9.sp, color = dyn.textMuted, maxLines = 1)
                            Text(text = match.userShift.localizedTitle(isEn), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, dyn.borderSubtle, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    color = dyn.surfaceElevated
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (match.colleagueShift.isWork) Icons.Filled.WbSunny else Icons.Filled.LocalCafe,
                            contentDescription = null,
                            tint = if (match.colleagueShift.isWork) dyn.nightShiftAccent else dyn.accentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(text = colleagueName, fontSize = 9.sp, color = dyn.textMuted, maxLines = 1)
                            Text(text = match.colleagueShift.localizedTitle(isEn), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                        }
                    }
                }
            }
        }
    }
}

private data class MatchBadgeStyle(
    val text: String,
    val accent: Color,
    val background: Color,
    val border: Color
)
