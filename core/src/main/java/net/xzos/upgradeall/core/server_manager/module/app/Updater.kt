package net.xzos.upgradeall.core.server_manager.module.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.xzos.upgradeall.core.data_manager.utils.VersioningUtils
import net.xzos.upgradeall.core.network_api.GrpcApi
import net.xzos.upgradeall.core.route.ReleaseInfoItem
import net.xzos.upgradeall.core.system_api.api.IoApi


class Updater(private val app: App) : UpdaterApi {
    private val dataMutex = Mutex()

    override suspend fun getUpdateStatus(): Int {
        val releaseInfoList = getReleaseInfo() ?: return NETWORK_404
        return if (releaseInfoList.isEmpty())
            NETWORK_ERROR
        else {
            //检查是否取得云端版本号
            if (app.installedVersionNumber != null
                    || app.markProcessedVersionNumber != null
            ) {
                // 检查是否获取本地版本号
                if (isLatest()) APP_LATEST
                else APP_OUTDATED
            } else APP_NO_LOCAL
        }
    }

    private suspend fun isLatest(): Boolean {
        val latestVersion = getLatestVersioning()
        return VersioningUtils.compareVersionNumber(
                app.markProcessedVersionNumber ?: app.installedVersionNumber, latestVersion
        )
    }

    override suspend fun getReleaseInfo(): List<ReleaseInfoItem>? {
        val appInfo = app.appInfo
        if (appInfo != null) {
            dataMutex.withLock {
                val hubUuid = app.hubDatabase?.hubConfig?.uuid ?: return null
                return GrpcApi.getReleaseInfo(hubUuid, appInfo)
            }
        }
        return null
    }

    // 获取最新版本号
    override suspend fun getLatestVersioning(): String? {
        val appInfo = app.appInfo
        val releaseInfoList = getReleaseInfo() ?: return null
        return if (appInfo != null) {
            if (releaseInfoList.isNotEmpty())
                releaseInfoList[0].versionNumber
            else null
        } else null
    }

    // 使用内置下载器下载文件
    suspend fun downloadReleaseFile(fileIndex: Pair<Int, Int>, externalDownloader: Boolean = false) {
        withContext(Dispatchers.IO) {
            getReleaseInfo()?.let { releaseInfoList ->
                val asset = releaseInfoList[fileIndex.first].getAssets(fileIndex.second)
                IoApi.downloadFile(
                        asset.fileName, asset.downloadUrl,
                        externalDownloader = externalDownloader
                )
            }
        }
    }

    companion object {
        const val NETWORK_404 = -1
        const val NETWORK_ERROR = 0
        const val APP_LATEST = 1
        const val APP_OUTDATED = 2
        const val APP_NO_LOCAL = 3

    }
}

private interface UpdaterApi {
    suspend fun getReleaseInfo(): List<ReleaseInfoItem>?
    suspend fun getUpdateStatus(): Int
    suspend fun getLatestVersioning(): String?
}