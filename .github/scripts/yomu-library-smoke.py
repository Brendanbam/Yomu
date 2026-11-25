"""Exercise a restored 2.0 library on the disposable English-language CI emulator."""

import re
import subprocess
import time
import xml.etree.ElementTree as ET
import zipfile
from contextlib import suppress
from pathlib import Path
from tempfile import TemporaryDirectory


OUT = Path("smoke-results")
FIXTURE = Path("app/src/test/resources/yomu-2.0-library.tachibk")
STORAGE_FOLDER = "00-YomuQA"
ACTIVITY = "app.yomu/eu.kanade.tachiyomi.ui.main.MainActivity"
DIVIDER = "Resize library and manga details panels"


def adb(*args):
    return subprocess.check_output(["adb", *map(str, args)], timeout=25)


def hierarchy():
    adb("shell", "uiautomator", "dump", "/data/local/tmp/yomu-ui.xml")
    xml = adb("exec-out", "cat", "/data/local/tmp/yomu-ui.xml")
    (OUT / "last-hierarchy.xml").write_bytes(xml)
    return ET.fromstring(xml)


def find(label, timeout=25):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        for node in hierarchy().iter("node"):
            labels = (node.get("text", ""), node.get("content-desc", ""))
            if node.get("enabled") == "true" and any(
                value.casefold() == label.casefold() or value.startswith(label + ", ") for value in labels
            ):
                return tuple(map(int, re.findall(r"\d+", node.attrib["bounds"])))
        time.sleep(0.3)
    raise AssertionError(f"{label!r} missing from the screen")


def tap(label, timeout=25):
    x1, y1, x2, y2 = find(label, timeout=timeout)
    adb("shell", "input", "tap", (x1 + x2) // 2, (y1 + y2) // 2)


def capture(name):
    (OUT / f"{name}.png").write_bytes(adb("exec-out", "screencap", "-p"))


def install_fixture():
    with TemporaryDirectory() as temp:
        storage = Path(temp) / STORAGE_FOLDER
        series = storage / "local" / "Paper Lanterns"
        series.mkdir(parents=True)
        (storage / "library.tachibk").write_bytes(FIXTURE.read_bytes())
        for chapter in (1, 2):
            with zipfile.ZipFile(series / f"Chapter {chapter}.cbz", "w") as pages:
                for page in range(1, 7):
                    pages.write(".github/assets/logo.png", f"{page:02}.png")
        adb("push", storage, "/sdcard/")


def open_library():
    adb("shell", "am", "start", "-W", "-n", ACTIVITY, "-a", "eu.kanade.tachiyomi.SHOW_LIBRARY")
    find("Paper Lanterns", timeout=60)


def main():
    adb("shell", "wm", "size", "1080x1920")
    adb("shell", "wm", "density", "420")
    install_fixture()
    tap("Next")
    tap("Select a folder")
    # DocumentsUI normally starts at the internal storage root on a clean AVD.
    try:
        find(STORAGE_FOLDER, timeout=5)
    except AssertionError:
        tap("Show roots")
        tap(adb("shell", "getprop", "ro.product.model").decode().strip())
    tap(STORAGE_FOLDER)
    tap("Use this folder")
    tap("Allow")
    tap("Next")
    tap("Next")
    tap("Get started")
    find("Library")

    path = f"content://com.android.externalstorage.documents/tree/primary%3A{STORAGE_FOLDER}/document/"
    adb("shell", "am", "start", "-W", "-n", ACTIVITY, "-a", "android.intent.action.VIEW",
        "-d", path + f"primary%3A{STORAGE_FOLDER}%2Flibrary.tachibk")
    tap("Restore")
    open_library()
    capture("04-restored-phone-library")
    tap("Paper Lanterns")
    find("Chapter 2")
    capture("05-phone-chapters")
    tap("Chapter 2")
    deadline = time.monotonic() + 30
    while time.monotonic() < deadline:
        activities = adb("shell", "dumpsys", "activity", "activities").decode()
        if re.search(r"(?:topResumedActivity|mResumedActivity)[^\n]*app\.yomu/[^\n]*ReaderActivity", activities):
            break
        time.sleep(0.5)
    else:
        raise AssertionError("Restored local chapter did not open the reader")
    # Fresh Android profiles can show a system tutorial over the full-screen reader.
    with suppress(AssertionError):
        tap("Got it", timeout=6)
    capture("05b-local-reader")
    adb("shell", "input", "keyevent", "BACK")
    find("Chapter 2")
    adb("shell", "input", "keyevent", "BACK")

    adb("shell", "wm", "size", "1600x1000")
    adb("shell", "wm", "density", "160")
    open_library()
    tap("Paper Lanterns")
    before = find(DIVIDER)
    find("Chapter 2")
    capture("06-tablet-inspector")
    x = (before[0] + before[2]) // 2
    y = (before[1] + before[3]) // 2
    adb("shell", "input", "swipe", x, y, x + 80, y, 400)
    after = find(DIVIDER)
    if after[0] <= before[0]:
        raise AssertionError(f"Divider did not move right: {before} -> {after}")
    for _ in range(60):
        adb("shell", "input", "keyevent", "TAB")
        if any(node.get("focused") == "true" and node.get("content-desc") == DIVIDER
               for node in hierarchy().iter("node")):
            break
    else:
        raise AssertionError("Keyboard focus never reached the resize control")
    adb("shell", "input", "keyevent", "DPAD_LEFT")
    if find(DIVIDER)[0] >= after[0]:
        raise AssertionError("D-pad left did not shrink the library pane")
    capture("07-tablet-resized")
    tap("Open full screen")
    find("Chapter 2")
    if any(node.get("content-desc") == DIVIDER for node in hierarchy().iter("node")):
        raise AssertionError("Full-screen details still contains the library divider")
    adb("shell", "input", "keyevent", "BACK")
    find(DIVIDER)
    adb("shell", "input", "keyevent", "BACK")
    find("Paper Lanterns")
    if any(node.get("content-desc") == DIVIDER for node in hierarchy().iter("node")):
        raise AssertionError("Back did not close the inspector")
    adb("shell", "am", "force-stop", "app.yomu")
    open_library()
    capture("08-restored-library-restart")


if __name__ == "__main__":
    try:
        main()
    except Exception:
        with suppress(subprocess.SubprocessError):
            capture("failure")
        raise

