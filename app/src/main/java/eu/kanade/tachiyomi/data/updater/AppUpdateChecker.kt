package eu.kanade.tachiyomi.data.updater

import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.util.system.isFossBuildType
import eu.kanade.tachiyomi.util.system.isNightlyBuildType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tachiyomi.domain.release.interactor.GetApplicationRelease

@Inject
class AppUpdateChecker(
    private val getApplicationRelease: GetApplicationRelease,
) {

    suspend fun checkForUpdate(forceCheck: Boolean = false): GetApplicationRelease.Result {
        // Yomu currently publishes standard signed releases only; unsupported channels return NoNewUpdate cleanly.
        if (isFossBuildType || isNightlyBuildType) {
            return GetApplicationRelease.Result.NoNewUpdate
        }

        return withContext(Dispatchers.IO) {
            getApplicationRelease.await(
                GetApplicationRelease.Arguments(
                    isFoss = false,
                    isNightly = false,
                    commitCount = BuildConfig.COMMIT_COUNT.toIntOrNull() ?: 0,
                    versionName = BuildConfig.VERSION_NAME,
                    repository = GITHUB_REPO,
                    forceCheck = forceCheck,
                ),
            )
        }
    }
}

val GITHUB_REPO: String by lazy {
    "Brendanbam/Yomu"
}

val RELEASE_TAG: String by lazy {
    if (isNightlyBuildType) {
        "r${BuildConfig.COMMIT_COUNT}"
    } else {
        "v${BuildConfig.VERSION_NAME}"
    }
}

val RELEASE_URL = "https://github.com/$GITHUB_REPO/releases/tag/$RELEASE_TAG"
