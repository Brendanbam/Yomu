package tachiyomi.domain.release.interactor

import dev.zacsweers.metro.Inject
import tachiyomi.domain.release.model.Release
import tachiyomi.domain.release.service.ReleaseService

@Inject
class GetApplicationRelease(
    private val service: ReleaseService,
) {
    suspend fun await(arguments: Arguments): Result {
        val release = service.latest(arguments) ?: return Result.NoNewUpdate

        // Check if latest version is strictly newer than current version
        val isNewVersion = isNewVersion(
            isNightly = arguments.isNightly,
            commitCount = arguments.commitCount,
            versionName = arguments.versionName,
            versionTag = release.version,
        )
        return when {
            isNewVersion -> Result.NewUpdate(release)
            else -> Result.NoNewUpdate
        }
    }

    companion object {
        fun isNewVersion(
            isNightly: Boolean,
            commitCount: Int,
            versionName: String,
            versionTag: String,
        ): Boolean {
            val cleanedTag = versionTag
                .trim()
                .removePrefix("v")
                .removePrefix("V")
                .removePrefix("r")
                .removePrefix("R")
                .substringBefore("-")
                .substringBefore("+")

            return if (isNightly) {
                val remoteCount = cleanedTag.toIntOrNull() ?: return false
                remoteCount > commitCount
            } else {
                val cleanedCurrent = versionName
                    .trim()
                    .removePrefix("v")
                    .removePrefix("V")
                    .substringBefore("-")
                    .substringBefore("+")

                val newSemVer = cleanedTag.split(".").map { it.toIntOrNull() ?: return false }
                val oldSemVer = cleanedCurrent.split(".").map { it.toIntOrNull() ?: return false }
                if (newSemVer.isEmpty() || oldSemVer.isEmpty()) return false

                val maxLen = maxOf(newSemVer.size, oldSemVer.size)
                for (index in 0 until maxLen) {
                    val newPart = newSemVer.getOrElse(index) { 0 }
                    val oldPart = oldSemVer.getOrElse(index) { 0 }
                    if (newPart != oldPart) {
                        return newPart > oldPart
                    }
                }
                false
            }
        }
    }

    data class Arguments(
        val isFoss: Boolean,
        val isNightly: Boolean,
        val commitCount: Int,
        val versionName: String,
        val repository: String,
        val forceCheck: Boolean = false,
    )

    sealed interface Result {
        data class NewUpdate(val release: Release) : Result
        data object NoNewUpdate : Result
        data object OsTooOld : Result
    }
}
