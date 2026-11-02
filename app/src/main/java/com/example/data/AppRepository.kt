package com.example.data

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.model.AppInfoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRepository(
    context: Context,
    private val scope: CoroutineScope
) {
    private val appContext = context.applicationContext
    private val packageManager: PackageManager = appContext.packageManager

    // Cache decoded icons so focus movement never triggers icon decoding
    private val iconCache = LruCache<String, ImageBitmap>(120)

    private val _installedApps = MutableStateFlow<List<AppInfoItem>>(emptyList())
    val installedApps: StateFlow<List<AppInfoItem>> = _installedApps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var receiverRegistered = false
    private var lastIncludeSelf = false
    private var lastLowEndMode = false

    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val pkg = intent?.data?.schemeSpecificPart
            if (pkg != null) {
                iconCache.remove("$pkg:false")
                iconCache.remove("$pkg:true")
            }
            refreshInstalledApps(
                includeSelf = lastIncludeSelf,
                lowEndMode = lastLowEndMode,
                forceReload = true
            )
        }
    }

    fun registerPackageReceiver() {
        if (receiverRegistered) return
        runCatching {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addAction(Intent.ACTION_PACKAGE_CHANGED)
                addDataScheme("package")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(
                    packageChangeReceiver,
                    filter,
                    Context.RECEIVER_NOT_EXPORTED
                )
            } else {
                appContext.registerReceiver(packageChangeReceiver, filter)
            }
            receiverRegistered = true
        }
    }

    fun unregisterPackageReceiver() {
        if (!receiverRegistered) return
        runCatching {
            appContext.unregisterReceiver(packageChangeReceiver)
        }
        receiverRegistered = false
    }

    fun refreshInstalledApps(
        includeSelf: Boolean = lastIncludeSelf,
        lowEndMode: Boolean = lastLowEndMode,
        forceReload: Boolean = false
    ) {
        if (!forceReload &&
            _installedApps.value.isNotEmpty() &&
            includeSelf == lastIncludeSelf &&
            lowEndMode == lastLowEndMode
        ) {
            return
        }
        lastIncludeSelf = includeSelf
        lastLowEndMode = lowEndMode

        scope.launch {
            _isLoading.value = true
            val discovered = withContext(Dispatchers.IO) {
                queryLaunchableAppsInternal(includeSelf = includeSelf, lowEndMode = lowEndMode)
            }
            _installedApps.value = discovered
            _isLoading.value = false
        }
    }

    fun queryLaunchableAppsBlocking(
        includeSelf: Boolean = false,
        lowEndMode: Boolean = false
    ): List<AppInfoItem> {
        val list = queryLaunchableAppsInternal(includeSelf, lowEndMode)
        _installedApps.value = list
        return list
    }

    private fun queryLaunchableAppsInternal(
        includeSelf: Boolean,
        lowEndMode: Boolean
    ): List<AppInfoItem> {
        val selfPkg = appContext.packageName
        val excludedPackages = if (includeSelf) {
            emptySet()
        } else {
            setOf(selfPkg, "com.ahmedsalam.nexaviewtv")
        }

        val leanbackIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val standardIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val leanbackActivities = runCatching {
            packageManager.queryIntentActivities(leanbackIntent, 0)
        }.getOrDefault(emptyList())

        val standardActivities = runCatching {
            packageManager.queryIntentActivities(standardIntent, 0)
        }.getOrDefault(emptyList())

        val prefsIntent = Intent(Intent.ACTION_APPLICATION_PREFERENCES)
        val packagesWithPrefs = runCatching {
            packageManager.queryIntentActivities(prefsIntent, 0)
                .mapNotNull { it.activityInfo?.packageName }
                .toSet()
        }.getOrDefault(emptySet())

        val resolvedByPackage = LinkedHashMap<String, Pair<ResolveInfo, Boolean>>()

        for (info in leanbackActivities) {
            val pkg = info.activityInfo?.packageName ?: continue
            if (pkg in excludedPackages) continue
            resolvedByPackage[pkg] = info to true
        }
        for (info in standardActivities) {
            val pkg = info.activityInfo?.packageName ?: continue
            if (pkg in excludedPackages) continue
            if (!resolvedByPackage.containsKey(pkg)) {
                resolvedByPackage[pkg] = info to false
            }
        }

        val targetIconPx = if (lowEndMode) 64 else 96
        val items = ArrayList<AppInfoItem>(resolvedByPackage.size)

        for ((pkg, pair) in resolvedByPackage) {
            val (resolveInfo, isLeanback) = pair
            val activityInfo = resolveInfo.activityInfo ?: continue
            val rawLabel = runCatching {
                resolveInfo.loadLabel(packageManager)?.toString()?.trim()
            }.getOrNull()
            val label = if (!rawLabel.isNullOrBlank()) {
                rawLabel
            } else {
                runCatching {
                    packageManager.getApplicationLabel(activityInfo.applicationInfo).toString()
                }.getOrDefault(pkg)
            }

            val cacheKey = "$pkg:$lowEndMode"
            val iconBitmap = iconCache.get(cacheKey) ?: runCatching {
                val drawable = resolveInfo.loadIcon(packageManager)
                    ?: activityInfo.loadIcon(packageManager)
                drawableToImageBitmap(drawable, targetIconPx, lowEndMode)?.also {
                    iconCache.put(cacheKey, it)
                }
            }.getOrNull()

            val (primaryColor, secondaryColor) = resolveTileColorsForPackage(pkg, label)

            items.add(
                AppInfoItem(
                    packageName = pkg,
                    activityName = activityInfo.name,
                    label = label,
                    iconBitmap = iconBitmap,
                    isAllAppsShortcut = false,
                    isLeanback = isLeanback,
                    hasAppPreferencesActivity = pkg in packagesWithPrefs,
                    tileColorHex = primaryColor,
                    tileSecondaryColorHex = secondaryColor
                )
            )
        }

        // Sort with priority TV apps first (YouTube first if installed), then alphabetical
        return items.sortedWith(
            compareByDescending<AppInfoItem> { it.isYouTube }
                .thenBy { priorityRank(it.packageName, it.label) }
                .thenBy { it.label.lowercase() }
        )
    }

    fun launchApp(app: AppInfoItem): Result<Unit> {
        return runCatching {
            val pkg = app.packageName
            val leanbackIntent = packageManager.getLeanbackLaunchIntentForPackage(pkg)
            val standardIntent = packageManager.getLaunchIntentForPackage(pkg)
            val explicitIntent = if (app.activityName != null) {
                Intent(Intent.ACTION_MAIN).apply {
                    component = ComponentName(pkg, app.activityName)
                    addCategory(if (app.isLeanback) Intent.CATEGORY_LEANBACK_LAUNCHER else Intent.CATEGORY_LAUNCHER)
                }
            } else {
                null
            }

            val launchIntent = leanbackIntent ?: standardIntent ?: explicitIntent
                ?: throw IllegalStateException("التطبيق غير متاح للتشغيل حالياً")

            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            appContext.startActivity(launchIntent)
        }
    }

    fun openSystemAppInfo(packageName: String): Boolean {
        return runCatching {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun openAppSpecificSettings(packageName: String): Boolean {
        return runCatching {
            val intent = Intent(Intent.ACTION_APPLICATION_PREFERENCES).apply {
                setPackage(packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val resolved = packageManager.resolveActivity(intent, 0)
            val activityInfo = resolved?.activityInfo
            if (activityInfo != null && activityInfo.exported) {
                intent.component = ComponentName(activityInfo.packageName, activityInfo.name)
                appContext.startActivity(intent)
                true
            } else {
                false
            }
        }.getOrDefault(false)
    }

    private fun drawableToImageBitmap(
        drawable: Drawable,
        maxSizePx: Int,
        lowEndMode: Boolean
    ): ImageBitmap? {
        return runCatching {
            if (drawable is BitmapDrawable && drawable.bitmap != null) {
                val src = drawable.bitmap
                if (src.width <= maxSizePx && src.height <= maxSizePx) {
                    return src.asImageBitmap()
                }
            }
            val intrinsicW = drawable.intrinsicWidth.takeIf { it > 0 } ?: maxSizePx
            val intrinsicH = drawable.intrinsicHeight.takeIf { it > 0 } ?: maxSizePx
            val ratio = intrinsicW.toFloat() / intrinsicH.toFloat()
            val width: Int
            val height: Int
            if (ratio >= 1f) {
                width = maxSizePx
                height = (maxSizePx / ratio).toInt().coerceAtLeast(1)
            } else {
                height = maxSizePx
                width = (maxSizePx * ratio).toInt().coerceAtLeast(1)
            }

            val config = Bitmap.Config.ARGB_8888
            val bitmap = Bitmap.createBitmap(width, height, config)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        }.getOrNull()
    }

    private fun priorityRank(pkg: String, label: String): Int {
        val lowerPkg = pkg.lowercase()
        val lowerLabel = label.lowercase()
        return when {
            lowerPkg.contains("youtube") || lowerLabel.contains("youtube") -> 0
            lowerPkg.contains("netflix") || lowerLabel.contains("netflix") -> 1
            lowerPkg.contains("amazon.avod") || lowerPkg.contains("primevideo") || lowerLabel.contains("prime video") -> 2
            lowerPkg.contains("disney") || lowerLabel.contains("disney") -> 3
            lowerPkg.contains("spotify") || lowerLabel.contains("spotify") -> 4
            lowerPkg.contains("chrome") || lowerLabel.contains("chrome") -> 5
            lowerPkg.contains("videolan") || lowerPkg.contains("vlc") || lowerLabel.contains("vlc") -> 6
            lowerPkg.contains("xbmc") || lowerPkg.contains("kodi") || lowerLabel.contains("kodi") -> 7
            lowerPkg.contains("stremio") || lowerLabel.contains("stremio") -> 8
            else -> 50
        }
    }

    private fun resolveTileColorsForPackage(pkg: String, label: String): Pair<Long, Long> {
        val p = pkg.lowercase()
        val l = label.lowercase()
        return when {
            p.contains("youtube") || l.contains("youtube") -> 0xFFDC2626L to 0xFF991B1BL
            p.contains("netflix") || l.contains("netflix") -> 0xFF18181BL to 0xFF09090BL
            p.contains("primevideo") || p.contains("amazon.avod") || l.contains("prime") -> 0xFF0284C7L to 0xFF0369A1L
            p.contains("disney") || l.contains("disney") -> 0xFF1E3A8AL to 0xFF172554L
            p.contains("spotify") || l.contains("spotify") -> 0xFF064E3BL to 0xFF022C22L
            p.contains("stremio") || l.contains("stremio") -> 0xFF312E81L to 0xFF1E1B4BL
            p.contains("kodi") || p.contains("xbmc") || l.contains("kodi") -> 0xFF1E293BL to 0xFF0F172AL
            p.contains("vlc") || p.contains("videolan") || l.contains("vlc") -> 0xFF1E293BL to 0xFF0F172AL
            p.contains("chrome") || l.contains("chrome") -> 0xFF1E293BL to 0xFF0F172AL
            else -> 0xFF172036L to 0xFF0F172AL
        }
    }

    companion object {
        const val ALL_APPS_SHORTCUT_PACKAGE = "nexaview.internal.all_apps"

        fun createAllAppsShortcutTile(): AppInfoItem = AppInfoItem(
            packageName = ALL_APPS_SHORTCUT_PACKAGE,
            activityName = null,
            label = "كل التطبيقات",
            iconBitmap = null,
            isAllAppsShortcut = true,
            isLeanback = true,
            hasAppPreferencesActivity = false,
            tileColorHex = 0xFF1E293BL,
            tileSecondaryColorHex = 0xFF0F172AL
        )
    }
}
