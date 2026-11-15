package com.apkkasir.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.apkkasir.data.AppContainer
import com.apkkasir.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val KasirColors = lightColorScheme(
    primary = Color(0xFF17614E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDAF0E6),
    onPrimaryContainer = Color(0xFF103E32),
    background = Color(0xFFF7F8F4),
    surface = Color.White,
    surfaceContainer = Color(0xFFEAF0EA),
    surfaceContainerLow = Color(0xFFF0F4EF),
    surfaceContainerHigh = Color(0xFFE5EDE5),
    surfaceVariant = Color(0xFFE9EFE8),
    secondaryContainer = Color(0xFFDCEDE1),
    onSecondaryContainer = Color(0xFF203E30),
    onSurface = Color(0xFF192621),
    onSurfaceVariant = Color(0xFF46574E),
    outline = Color(0xFF64786C),
    outlineVariant = Color(0xFFCBD8CD),
)

private val KasirDarkColors = darkColorScheme(
    primary = Color(0xFFA0D9C3),
    onPrimary = Color(0xFF10382C),
    primaryContainer = Color(0xFF204B3B),
    onPrimaryContainer = Color(0xFFD8F6E8),
    background = Color(0xFF121C17),
    surface = Color(0xFF1D2A23),
    surfaceContainer = Color(0xFF26362D),
    surfaceContainerLow = Color(0xFF202E26),
    surfaceContainerHigh = Color(0xFF2D3E33),
    surfaceVariant = Color(0xFF293A30),
    secondaryContainer = Color(0xFF314F3E),
    onSecondaryContainer = Color(0xFFE0F2E5),
    onSurface = Color(0xFFE9F4EA),
    onSurfaceVariant = Color(0xFFBFD0C5),
    outline = Color(0xFF8DA397),
    outlineVariant = Color(0xFF41564A),
)

@Composable
fun KasirApp(app: AppContainer) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) KasirDarkColors else KasirColors,
        shapes = Shapes(small = RoundedCornerShape(8.dp), medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp), extraLarge = RoundedCornerShape(20.dp)),
    ) {
        Surface(Modifier.fillMaxSize()) {
            val account by app.session.account.collectAsState()
            val current = account
            if (current == null) {
                AccountScreen(app)
            } else {
                key(current) {
                    CashierNavigation(app)
                }
            }
        }
    }
}

private enum class CashierTab(val route: String, val label: String, val icon: Int) {
    HOME("home", "Beranda", R.drawable.ic_kasir_register),
    HISTORY("history", "Riwayat", R.drawable.ic_kasir_receipt),
    DAILY("daily", "Laporan", R.drawable.ic_kasir_report),
    SETTINGS("settings", "Pengaturan", R.drawable.ic_kasir_user),
}

@Composable
private fun CashierNavigation(app: AppContainer) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val selectedTab = if (route == "history/{filter}") CashierTab.HISTORY else
        CashierTab.entries.firstOrNull { it.route == route }
    val dailyState = currentDailyReport(app.reports, route)
    val back = { nav.popBackStack(); Unit }
    val open: (String) -> Unit = { target -> nav.navigate(target) { launchSingleTop = true } }
    val select: (CashierTab) -> Unit = { tab ->
        val activeRoute = nav.currentDestination?.route
        if (tab == CashierTab.HOME) {
            nav.popBackStack("home", inclusive = false, saveState = activeRoute != "history/{filter}")
        } else if (activeRoute != tab.route) {
            nav.navigate(tab.route) {
                popUpTo("home") { saveState = activeRoute != "history/{filter}" }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    val latestDailyState by rememberUpdatedState(dailyState)
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { if (selectedTab != null) CashierNavigationBar(selectedTab, select) },
    ) { padding ->
        NavHost(nav, startDestination = "home",
            modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
            composable("home") {
                HomeScreen(latestDailyState) { if (it == "daily") select(CashierTab.DAILY) else open(it) }
            }
            composable("settings") {
                Page("Pengaturan") {
                    MenuItem("Barang", "Tambah barang dan ubah harga", R.drawable.ic_kasir_box) { open("catalog") }
                    MenuItem("QRIS toko", "Pasang gambar pembayaran", R.drawable.ic_kasir_qr) { open("qris") }
                    MenuItem("Akun dan pemulihan", "Ganti kata sandi atau kode", R.drawable.ic_kasir_user) { open("access") }
                    OutlinedButton(onClick = app.session::exit, modifier = Modifier.heightIn(min = 48.dp)) { Text("Keluar") }
                }
            }
            composable("catalog") { CatalogScreen(app, back) }
            composable("qris") { QrisSettingsScreen(app, back) }
            composable("access") { AccessSettingsScreen(app, back) }
            composable("daily") { DailyReportScreen(latestDailyState, { open("documents") }) { open("sale/$it") } }
            composable("correction") { SaleSearchScreen(app, back) { open("sale/$it") } }
            composable("sale/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                SaleCorrectionScreen(app, checkNotNull(it.arguments).getLong("id"), back)
            }
            composable("documents") { ReportDocumentsScreen(app, back) }
            composable("checkout") {
                CheckoutScreen(app, null, back) { id ->
                    nav.navigate("receipt/$id") {
                        popUpTo("checkout") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            composable("checkout/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                CheckoutScreen(app, checkNotNull(it.arguments).getLong("id"), back) { id ->
                    nav.navigate("receipt/$id") {
                        popUpTo("checkout/{id}") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            composable("history") {
                CashierHistoryScreen(app, HistoryFilter.ALL, null, { open("correction") },
                    { open("checkout/$it") }, { open("receipt/$it") })
            }
            composable("history/{filter}") {
                val filter = if (it.arguments?.getString("filter") == "pending") HistoryFilter.PENDING else HistoryFilter.RECEIPTS
                CashierHistoryScreen(app, filter, back, { open("correction") },
                    { open("checkout/$it") }, { open("receipt/$it") })
            }
            composable("receipt/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                ReceiptScreen(app, checkNotNull(it.arguments).getLong("id"), back)
            }
        }
    }
}

@Composable
private fun CashierNavigationBar(selected: CashierTab, select: (CashierTab) -> Unit) {
    NavigationBar {
        CashierTab.entries.forEach { tab ->
            val interactions = remember { MutableInteractionSource() }
            val hovered by interactions.collectIsHoveredAsState()
            val focused by interactions.collectIsFocusedAsState()
            val highlight by animateColorAsState(
                if (hovered || focused) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                label = "Sorotan tab",
            )
            NavigationBarItem(
                selected = selected == tab,
                onClick = { select(tab) },
                modifier = Modifier.hoverable(interactions).heightIn(min = 56.dp),
                interactionSource = interactions,
                icon = {
                    Surface(shape = MaterialTheme.shapes.small,
                        color = highlight,
                        border = if (focused) BorderStroke(2.dp, MaterialTheme.colorScheme.outline) else null) {
                        Icon(painterResource(tab.icon), null, Modifier.padding(3.dp).size(22.dp))
                    }
                },
                label = { Text(tab.label, textAlign = TextAlign.Center) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Page(
    title: String,
    back: (() -> Unit)? = null,
    action: @Composable RowScope.() -> Unit = {},
    form: Boolean = false,
    body: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { KasirToolbar(title, back, action) }) { insets ->
        Box(Modifier.fillMaxSize().padding(insets).imePadding(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = if (form) 460.dp else 640.dp).fillMaxSize()
                    .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (form) ContentCard(body) else body()
            }
        }
    }
}

@Composable
internal fun ContentCard(content: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KasirToolbar(title: String, back: (() -> Unit)?, action: @Composable RowScope.() -> Unit = {}) {
    Column {
        TopAppBar(
            title = { Text(title, Modifier.semantics { heading() }, fontWeight = FontWeight.SemiBold, maxLines = 2) },
            navigationIcon = { if (back != null) TextButton(onClick = back) { Text("Kembali") } },
            actions = action,
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
internal fun SectionTitle(title: String) {
    Text(title, Modifier.padding(top = 8.dp).semantics { heading() },
        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun MenuItem(title: String, subtitle: String, icon: Int, open: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(onClick = open, modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = MaterialTheme.shapes.medium, color = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer) {
                Icon(painterResource(icon), contentDescription = null, modifier = Modifier.padding(10.dp).size(24.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
internal fun SummaryCard(label: String, value: String, supporting: String? = null) {
    Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            if (supporting != null) Text(supporting, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun ErrorText(message: String?) {
    if (message != null) Text(message, color = MaterialTheme.colorScheme.error)
}

internal class UiAction(private val scope: CoroutineScope) {
    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)

    fun run(block: suspend () -> Unit) {
        if (busy) return
        busy = true
        error = null
        scope.launch {
            try {
                block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                error = failure.message ?: "Operasi gagal. Coba lagi."
            } finally {
                busy = false
            }
        }
    }
}

@Composable
internal fun rememberAction(): UiAction {
    val scope = rememberCoroutineScope()
    return remember(scope) { UiAction(scope) }
}
