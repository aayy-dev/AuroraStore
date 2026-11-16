/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.extensions.requiresObbDir
import com.aurora.extensions.toast
import com.aurora.store.MainViewModel
import com.aurora.store.R
import com.aurora.store.compose.composable.AccountAvatar
import com.aurora.store.compose.composable.InsufficientStorageDialog
import com.aurora.store.compose.composable.PermissionUpdateWarningDialog
import com.aurora.store.compose.composable.TrackerUpdateWarningDialog
import com.aurora.store.compose.composition.LocalNetworkStatus
import com.aurora.store.compose.composition.LocalUI
import com.aurora.store.compose.composition.UI
import com.aurora.store.compose.navigation.Destination
import com.aurora.store.compose.theme.TvDimens
import com.aurora.store.compose.ui.apps.AppsGamesScreen
import com.aurora.store.compose.ui.commons.MoreSheet
import com.aurora.store.compose.ui.commons.NetworkScreen
import com.aurora.store.compose.ui.sheets.AppUpdateSheet
import com.aurora.store.compose.ui.tv.TvMainShell
import com.aurora.store.compose.ui.tv.TvTopBarItem
import com.aurora.store.compose.ui.updates.UpdatesScreen
import com.aurora.store.data.model.ExodusTracker
import com.aurora.store.data.model.NetworkStatus
import com.aurora.store.data.model.PermissionType
import com.aurora.store.data.model.StorageRequirement
import com.aurora.store.data.providers.PermissionProvider.Companion.isGranted
import com.aurora.store.data.room.update.Update
import com.aurora.store.util.PackageUtil
import com.aurora.store.util.Preferences
import com.aurora.store.util.Preferences.PREFERENCE_UPDATES_WARN_PERMISSIONS
import com.aurora.store.util.Preferences.PREFERENCE_UPDATES_WARN_TRACKERS
import com.aurora.store.util.StorageUtil
import com.aurora.store.viewmodel.all.UpdatesViewModel
import com.aurora.store.viewmodel.commons.MoreViewModel
import com.aurora.store.viewmodel.notifications.NotificationsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Pages hosted by the main pager. The ordinal is the page index used by [MainScreen]'s
 * `initialTab` (and the default-tab preference), so the order here must not change.
 */
private enum class MainTab(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
    @DrawableRes val selectedIconRes: Int = iconRes
) {
    APPS(R.string.title_apps, R.drawable.ic_nav_apps, R.drawable.ic_nav_apps_filled),
    GAMES(R.string.title_games, R.drawable.ic_games),
    UPDATES(R.string.title_updates, R.drawable.ic_updates)
}

/**
 * Bottom navigation entries in display order (Play Store style: Games, Apps, Search, Updates).
 * Search isn't a pager page; it opens the search screen.
 */
private sealed class NavEntry {
    data class Page(val tab: MainTab) : NavEntry()
    data object Search : NavEntry()
}

private val navEntries = listOf(
    NavEntry.Page(MainTab.GAMES),
    NavEntry.Page(MainTab.APPS),
    NavEntry.Search,
    NavEntry.Page(MainTab.UPDATES)
)

@Composable
fun MainScreen(
    initialTab: Int = 0,
    mainViewModel: MainViewModel = hiltViewModel(),
    moreViewModel: MoreViewModel = hiltViewModel(),
    updatesViewModel: UpdatesViewModel = hiltViewModel(),
    notificationsViewModel: NotificationsViewModel = hiltViewModel(),
    onNavigateTo: (Destination) -> Unit = {}
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val networkStatus = LocalNetworkStatus.current
    val updates by mainViewModel.updateHelper.updates.collectAsStateWithLifecycle(
        initialValue = null
    )
    val updateCount = updates?.size ?: 0
    val notificationCount by notificationsViewModel.unreadCount.collectAsStateWithLifecycle()
    val downloads by updatesViewModel.downloadsList.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = initialTab.coerceIn(
            0,
            MainTab.entries.size - 1
        )
    ) {
        MainTab.entries.size
    }

    var tvTab by rememberSaveable {
        mutableIntStateOf(initialTab.coerceIn(0, MainTab.entries.size - 1))
    }

    var showMoreSheet by remember { mutableStateOf(false) }
    val warnPermissions = remember {
        Preferences.getBoolean(context, PREFERENCE_UPDATES_WARN_PERMISSIONS, false)
    }
    var appUpdateTarget by remember { mutableStateOf<Update?>(null) }
    var trackerWarning by remember {
        mutableStateOf<Pair<Update, List<ExodusTracker>>?>(null)
    }
    var permissionWarning by remember { mutableStateOf<Update?>(null) }
    var storageWarning by remember { mutableStateOf<StorageRequirement?>(null) }
    val checkingJobs = remember { mutableStateMapOf<String, Job>() }

    // A blocked update never produces a download, so the effect below can't clear its marker.
    LaunchedEffect(Unit) {
        updatesViewModel.storageWarning.collect {
            storageWarning = it
            checkingJobs.clear()
        }
    }

    // Once the download a check kicked off actually appears, drop the "checking" marker so the
    // item's in-progress state is driven purely by the download (no flash back to "Update").
    LaunchedEffect(downloads) {
        checkingJobs.keys.toList().forEach { pkg ->
            if (downloads.any { it.packageName == pkg && !it.isFinished }) {
                checkingJobs.remove(pkg)
            }
        }
    }

    fun handleNavigation(destination: Destination) {
        when (destination) {
            is Destination.AppUpdate -> appUpdateTarget = destination.update
            else -> onNavigateTo(destination)
        }
    }

    fun performUpdate(update: Update) {
        if (update.fileList.requiresObbDir() &&
            !isGranted(context, PermissionType.STORAGE_MANAGER)
        ) {
            checkingJobs.remove(update.packageName)
            onNavigateTo(
                Destination.PermissionRationale(setOf(PermissionType.STORAGE_MANAGER))
            )
        } else {
            updatesViewModel.download(update)
        }
    }

    fun checkTrackersThenUpdate(update: Update) {
        if (!Preferences.getBoolean(context, PREFERENCE_UPDATES_WARN_TRACKERS, false)) {
            performUpdate(update)
            return
        }
        checkingJobs[update.packageName] = coroutineScope.launch {
            val installedVc = PackageUtil.getInstalledVersionCode(context, update.packageName)
            val trackers = updatesViewModel.getNewTrackers(update.packageName, installedVc)
            if (trackers.isEmpty()) {
                performUpdate(update)
            } else {
                trackerWarning = update to trackers
            }
        }
    }

    if (networkStatus == NetworkStatus.UNAVAILABLE) {
        NetworkScreen()
        return
    }

    if (showMoreSheet) {
        MoreSheet(
            onDismiss = { showMoreSheet = false },
            onNavigateTo = { destination ->
                showMoreSheet = false
                onNavigateTo(destination)
            }
        )
    }

    appUpdateTarget?.let { app ->
        AppUpdateSheet(
            update = app,
            onDismiss = { appUpdateTarget = null },
            onNavigateTo = { destination ->
                appUpdateTarget = null
                onNavigateTo(destination)
            }
        )
    }

    val pageContent: @Composable (MainTab) -> Unit = { tab ->
        when (tab) {
            MainTab.APPS -> AppsGamesScreen(
                pageType = 0,
                onNavigateTo = onNavigateTo
            )
            MainTab.GAMES -> AppsGamesScreen(
                pageType = 1,
                onNavigateTo = ::handleNavigation
            )
            MainTab.UPDATES -> {
                UpdatesScreen(
                    viewModel = updatesViewModel,
                    onNavigateTo = ::handleNavigation,
                    onRequestUpdate = { update ->
                        if (warnPermissions && update.newPermissions.isNotEmpty()) {
                            permissionWarning = update
                        } else {
                            checkTrackersThenUpdate(update)
                        }
                    },
                    onRequestUpdateAll = { selectedUpdates ->
                        val needsObb = selectedUpdates.any {
                            it.fileList.requiresObbDir()
                        }
                        if (needsObb &&
                            !isGranted(context, PermissionType.STORAGE_MANAGER)
                        ) {
                            onNavigateTo(
                                Destination.PermissionRationale(
                                    setOf(PermissionType.STORAGE_MANAGER)
                                )
                            )
                        } else {
                            val (heldBack, toUpdate) = selectedUpdates.partition {
                                warnPermissions && it.newPermissions.isNotEmpty()
                            }
                            if (heldBack.isNotEmpty()) {
                                context.toast(
                                    resources.getQuantityString(
                                        R.plurals.updates_skipped_new_permissions,
                                        heldBack.size,
                                        heldBack.size
                                    )
                                )
                            }
                            updatesViewModel.downloadAll(toUpdate)
                        }
                    },
                    onCancelUpdate = { packageName ->
                        if (downloads.any {
                                it.packageName == packageName && !it.isFinished
                            }
                        ) {
                            checkingJobs.remove(packageName)
                            updatesViewModel.cancelDownload(packageName)
                        } else {
                            checkingJobs.remove(packageName)?.cancel()
                        }
                    },
                    onCancelAll = { updatesViewModel.cancelAll() },
                    checkingPackages = checkingJobs.keys
                )
            }
        }
    }

    if (LocalUI.current == UI.TV) {
        val saveableStateHolder = rememberSaveableStateHolder()
        TvMainShell(
            items = listOf(
                TvTopBarItem.Action(
                    labelRes = R.string.action_search,
                    iconRes = R.drawable.ic_round_search,
                    showLabel = true,
                    onClick = { onNavigateTo(Destination.Search) }
                ),
                TvTopBarItem.Section(
                    index = MainTab.APPS.ordinal,
                    labelRes = MainTab.APPS.labelRes,
                    iconRes = MainTab.APPS.iconRes
                ),
                TvTopBarItem.Section(
                    index = MainTab.GAMES.ordinal,
                    labelRes = MainTab.GAMES.labelRes,
                    iconRes = MainTab.GAMES.iconRes
                ),
                TvTopBarItem.Section(
                    index = MainTab.UPDATES.ordinal,
                    labelRes = MainTab.UPDATES.labelRes,
                    iconRes = MainTab.UPDATES.iconRes,
                    badgeCount = updateCount
                ),
                TvTopBarItem.Action(
                    labelRes = R.string.title_notifications,
                    iconRes = R.drawable.ic_notifications,
                    badgeCount = notificationCount,
                    onClick = { onNavigateTo(Destination.Notifications) }
                ),
                TvTopBarItem.Action(
                    labelRes = R.string.title_download_manager,
                    iconRes = R.drawable.ic_download_manager,
                    onClick = { onNavigateTo(Destination.Downloads) }
                )
            ),
            selectedIndex = tvTab,
            onSelect = { tvTab = it },
            avatarUrl = moreViewModel.authProvider.authData?.userProfile?.artwork?.url,
            onAvatarClick = { showMoreSheet = true }
        ) {
            val tab = MainTab.entries[tvTab]
            saveableStateHolder.SaveableStateProvider(tab.name) {
                // Updates is still the phone list, so keep it off the screen edges
                if (tab == MainTab.UPDATES) {
                    Box(
                        modifier = Modifier.padding(
                            horizontal = TvDimens.ScreenPaddingHorizontal - 24.dp
                        )
                    ) {
                        pageContent(tab)
                    }
                } else {
                    pageContent(tab)
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                HomeTopBar(
                    avatarUrl = moreViewModel.authProvider.authData?.userProfile?.artwork?.url,
                    notificationCount = notificationCount,
                    onNotificationsClick = { onNavigateTo(Destination.Notifications) },
                    onDownloadsClick = { onNavigateTo(Destination.Downloads) },
                    onAvatarClick = { showMoreSheet = true }
                )
            },
            bottomBar = {
                NavigationBar {
                    navEntries.forEach { entry ->
                        when (entry) {
                            is NavEntry.Page -> {
                                val tab = entry.tab
                                val selected = pagerState.currentPage == tab.ordinal
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(tab.ordinal)
                                        }
                                    },
                                    icon = {
                                        val icon = painterResource(
                                            if (selected) tab.selectedIconRes else tab.iconRes
                                        )
                                        if (tab == MainTab.UPDATES && updateCount > 0) {
                                            BadgedBox(badge = { Badge { Text("$updateCount") } }) {
                                                Icon(painter = icon, contentDescription = null)
                                            }
                                        } else {
                                            Icon(painter = icon, contentDescription = null)
                                        }
                                    },
                                    label = { Text(stringResource(tab.labelRes)) }
                                )
                            }

                            NavEntry.Search -> NavigationBarItem(
                                selected = false,
                                onClick = { onNavigateTo(Destination.Search) },
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_round_search),
                                        contentDescription = null
                                    )
                                },
                                label = { Text(stringResource(R.string.action_search)) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .fillMaxSize()
            ) {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = false,
                    beyondViewportPageCount = MainTab.entries.size - 1,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    pageContent(MainTab.entries[page])
                }
            }
        }
    }

    permissionWarning?.let { update ->
        PermissionUpdateWarningDialog(
            permissions = update.newPermissions,
            onConfirm = {
                permissionWarning = null
                checkTrackersThenUpdate(update)
            },
            onDismiss = { permissionWarning = null }
        )
    }

    trackerWarning?.let { (update, trackers) ->
        TrackerUpdateWarningDialog(
            trackers = trackers,
            onConfirm = {
                val pending = update
                trackerWarning = null
                performUpdate(pending)
            },
            onDismiss = {
                trackerWarning = null
                checkingJobs.remove(update.packageName)
            }
        )
    }

    storageWarning?.let { requirement ->
        InsufficientStorageDialog(
            requirement = requirement,
            onFreeUpSpace = {
                storageWarning = null
                StorageUtil.openFreeUpSpace(context)
            },
            onDismiss = { storageWarning = null }
        )
    }
}

/**
 * Play Store style home app bar: app logo on the start side, then notifications, downloads and
 * the account avatar (which opens the account/more sheet).
 */
@Composable
private fun HomeTopBar(
    avatarUrl: String?,
    notificationCount: Int,
    onNotificationsClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    TopAppBar(
        title = {
            Image(
                modifier = Modifier.size(32.dp),
                painter = painterResource(R.drawable.ic_logo_mark),
                contentDescription = stringResource(R.string.app_name)
            )
        },
        actions = {
            IconButton(onClick = onNotificationsClick) {
                BadgedBox(
                    badge = {
                        if (notificationCount > 0) Badge { Text("$notificationCount") }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_notifications),
                        contentDescription = stringResource(R.string.title_notifications)
                    )
                }
            }
            IconButton(onClick = onDownloadsClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_download_manager),
                    contentDescription = stringResource(R.string.title_download_manager)
                )
            }
            IconButton(onClick = onAvatarClick) {
                AccountAvatar(
                    url = avatarUrl,
                    size = 32.dp,
                    contentDescription = stringResource(R.string.title_more)
                )
            }
        }
    )
}
