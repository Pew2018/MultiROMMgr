# OnePlus 3T A3003: source audit and personal rebuild

Audit date: 2026-10-20. This is a rebuilt manager, not a byte-identical recovery of
the missing historical BitO-MultiROMMgr-001.apk or its signing certificate.

## Baseline and upstream comparison

Pew2018/master initially equals BitOBSessiOn/master at
20f745d16490758e267f804c6c738fea83f0928c (2016-12-19), with zero differing commits.
No AGENTS.md or Actions workflow exists in that baseline.
Original package com.tassadar.multirommgr, versionCode 188, versionName 1.188,
minSdk 16, targetSdk 23 and support libraries 23.1.1 are retained.
The APK filename does not change the application version to "001".

Baseline: AGP 1.5.0, Gradle 2.2.1, compile SDK 23, build tools 23.0.2.
Rebuild: AGP 4.2.2, Gradle 6.7.1, Java 11, compile SDK 23, build tools 30.0.3.
The unrecognized historical wrapper JAR is replaced by the official Gradle v6.7.1
wrapper from gradle/gradle (blob e708b1c023ec8b20f512888fe07c5bd3ff77bb8f);
CI wrapper validation remains enabled. Google Maven supplies the original Android support libraries. No AndroidX migration,
target SDK increase, recovery image/kernel change, or embedded executable replacement.

## Evidence and compatibility assessment

- [Original device database](https://github.com/BitOBSessiOn/MultiROMMgr/blob/20f745d16490758e267f804c6c738fea83f0928c/MultiROMMgr/src/main/assets/devices.json)
  already recognizes oneplus3t (plus a historical OpenPlus3T typo). Detection uses
  Build.DEVICE, not the retail model A3003.
- [BitO's 3T device tree](https://github.com/BitOBSessiOn/android_device_oneplus_oneplus3t/tree/507ef946edc22a89b2ac3e43fecf989c3b83051f)
  has ARM64 + 32-bit ARM support, bootdevice 624000.ufshc, boot/recovery 64 MiB,
  and MultiROM flags MR_NO_KEXEC=2 / MR_ALLOW_NKK71_NOKEXEC_WORKAROUND=true.
  Its recovery.fstab uses /dev/block/bootdevice/by-name; the manager's original
  /dev/block/platform/soc/624000.ufshc/by-name paths are preserved. Their existence
  and mapping on the user's current ROM still require on-device inspection.
- [Author's 2017 v33 3T package listing](https://androidfilehost.com/?fid=529152257862699016)
  names multirom-20170128-v33-UNOFFICIAL-oneplus3t.zip for A3003.
  This identifies the historical setup; it does not verify a download or its contents.
- MultiROM.java looks in /data/media/0/multirom and /data/media/multirom, executes
  multirom -v, lists roms, and boots through --boot-rom. These interfaces are
  plausible for v33 but are not a device test.
- The baseline manager's kernel check treats /proc/device-tree as evidence of
  kexec. This is only a legacy heuristic, not proof. Its boot dialog can reject
  a secondary ROM without kexec even though a port has a no-kexec workaround.
  These checks are retained rather than bypassed speculatively. Use the matched
  MultiROM recovery's own boot menu if the manager cannot perform the operation.
- Historical separate 3T packages and later unified OP3/3T packages must not be
  interchanged solely because the manager recognizes both names. Match recovery,
  v33 ZIP, firmware, primary/secondary ROM and encryption state to the same port.

## Safe changes

1. Split the 3T database entry from OP3. Preserve all original block paths and
   every other device entry. Preserve the typo alias and add correct OnePlus3T.
   3T now has an empty default manifest URL and official=false. Previously it
   inherited Tasssadar's generic manifest and could fall back to oneplus3.
   There is no independently verified current 3T update manifest; automated
   downloads/installation are unavailable by default on 3T. Existing installed
   MultiROM detection and management remain available. Developer manifest override
   remains an expert-only existing feature, not a supported update source.
2. Locale-independent device matching and null handling.
3. Request the already-declared storage permission on Android 6+. Grant Files/
   Storage permission before using the existing Download directory.
4. Bound network connect/read waits; reject resumed responses that ignore the
   requested byte range, preventing append corruption. Existing checksums/GPG
   behavior is retained.
5. Parse legacy versions and v33 port suffixes safely; null/empty/malformed
   versions return 0 rather than crashing or being treated as current.
6. Actions builds a release variant with a CI debug certificate, checks package,
   SDK levels, APK signatures, exact embedded-asset bytes and runs version tests.
   Lint reports are published with the original non-blocking lint policy.

## What this build does not establish

The APK needs root for device operations and a compatible preinstalled MultiROM
v33 + MultiROM-capable TWRP + compatible ROM/firmware. It does not contain these
components or install them offline. Install matching historical components through
the appropriate recovery after a full off-device backup, including stock boot,
recovery and essential device partitions. Do not format or decrypt data merely
because the manager cannot detect it.

No A3003 was connected; installation, root acquisition, encryption access, native
asset execution, ROM discovery, boot, rename, erase and flashing remain untested.
Native assets are old ARM executables; retaining them does not verify them on
modern ROMs. targetSdk 23 intentionally preserves legacy behavior, and the app is
not presented as a modern Android app or as supporting Android 16.

The release variant is signed for installation, but with a fresh debug certificate
on each fresh CI runner. It cannot upgrade the historical APK (different signer)
and later builds may need uninstall/reinstall, which removes app settings. No
private signing key is committed or uploaded. Keep the downloaded APK if you
need to reinstall the exact same build.

## Remaining audit findings

The original app includes privileged shell operations and legacy quote handling,
an exported ROM-list provider and boot dialog, old networking/GPG/MD5 mechanisms,
and non-blocking lint. This focused recovery build is not a full security
modernization. Do not feed untrusted ROM names, developer manifests or files to
the root manager. No runtime protections have been bypassed to claim compatibility.

## Build and verification

Open Actions > Build legacy MultiROM Manager APK > latest successful master run.
Download BitO-MultiROMMgr-001 artifact and extract BitO-MultiROMMgr-001.apk.
The same artifact includes SHA256SUMS, signature.txt, badging.txt and BUILD-INFO.txt.
build-reports contains JUnit and lint output. Signature, contents and compilation
checks establish a structurally installable APK, not successful installation or
operation on a physical OnePlus 3T.
