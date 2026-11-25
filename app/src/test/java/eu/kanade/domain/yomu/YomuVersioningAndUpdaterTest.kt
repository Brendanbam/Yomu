package eu.kanade.domain.yomu

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.data.release.GitHubAsset
import tachiyomi.data.release.ReleaseServiceImpl
import tachiyomi.domain.release.interactor.GetApplicationRelease

class YomuVersioningAndUpdaterTest {

    @Test
    fun `semver comparison never treats lower major version as newer`() {
        // Regression test for upstream bug where 1.9.9 > 2.0.0 returned true
        GetApplicationRelease.isNewVersion(
            isNightly = false,
            commitCount = 0,
            versionName = "2.0.0",
            versionTag = "v1.9.9",
        ).shouldBeFalse()

        GetApplicationRelease.isNewVersion(
            isNightly = false,
            commitCount = 0,
            versionName = "2.1.0",
            versionTag = "v2.0.9",
        ).shouldBeFalse()
    }

    @Test
    fun `semver comparison identifies genuine updates and handles equal or malformed tags`() {
        GetApplicationRelease.isNewVersion(false, 0, "1.0.0", "v2.0.0").shouldBeTrue()
        GetApplicationRelease.isNewVersion(false, 0, "2.0.0", "v2.0.1").shouldBeTrue()
        GetApplicationRelease.isNewVersion(false, 0, "2.0.0", "v2.1.0").shouldBeTrue()
        GetApplicationRelease.isNewVersion(false, 0, "2.0.0", "v2.0.0").shouldBeFalse()
        GetApplicationRelease.isNewVersion(false, 0, "2.0.0", "not-a-version").shouldBeFalse()
        GetApplicationRelease.isNewVersion(false, 0, "2.0.0", "").shouldBeFalse()
    }

    @Test
    fun `nightly comparison uses commit count safely`() {
        GetApplicationRelease.isNewVersion(true, 100, "2.0.0", "r105").shouldBeTrue()
        GetApplicationRelease.isNewVersion(true, 100, "2.0.0", "r100").shouldBeFalse()
        GetApplicationRelease.isNewVersion(true, 100, "2.0.0", "r95").shouldBeFalse()
        GetApplicationRelease.isNewVersion(true, 100, "2.0.0", "invalid").shouldBeFalse()
    }

    @Test
    fun `selectDownloadLink picks exact ABI, walks fallbacks, and ignores non-APK checksum assets`() {
        val assets = listOf(
            GitHubAsset("yomu-v2.0.0-checksums.sha256", "https://example/checksums.sha256"),
            GitHubAsset("yomu-v2.0.0-universal.apk", "https://example/universal.apk"),
            GitHubAsset("yomu-v2.0.0-arm64-v8a.apk", "https://example/arm64.apk"),
            GitHubAsset("yomu-v2.0.0-armeabi-v7a.apk", "https://example/armv7.apk"),
            GitHubAsset("yomu-v2.0.0-x86_64.apk", "https://example/x86_64.apk"),
        )

        ReleaseServiceImpl.selectDownloadLink(assets, listOf("arm64-v8a", "armeabi-v7a"), isFoss = false) shouldBe
            "https://example/arm64.apk"

        ReleaseServiceImpl.selectDownloadLink(assets, listOf("x86_64", "x86"), isFoss = false) shouldBe
            "https://example/x86_64.apk"

        // Unsupported ABI falls back cleanly to universal APK
        ReleaseServiceImpl.selectDownloadLink(assets, listOf("riscv64"), isFoss = false) shouldBe
            "https://example/universal.apk"

        // FOSS build never downloads a standard APK when no -foss APK is published
        ReleaseServiceImpl.selectDownloadLink(assets, listOf("arm64-v8a"), isFoss = true).shouldBeNull()
    }

    @Test
    fun `selectDownloadLink returns null when release contains only non-APK files`() {
        val nonApkAssets = listOf(
            GitHubAsset("yomu-v2.0.0-checksums.sha256", "https://example/checksums.sha256"),
            GitHubAsset("release-notes.txt", "https://example/notes.txt"),
        )

        ReleaseServiceImpl.selectDownloadLink(nonApkAssets, listOf("arm64-v8a"), isFoss = false).shouldBeNull()
    }
}
