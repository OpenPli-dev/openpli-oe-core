SUMMARY = "Utilities and libraries for handling compiled object files"
HOMEPAGE = "https://sourceware.org/elfutils"
SECTION = "base"
LICENSE = "(GPLv3 & Elfutils-Exception)"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"
DEPENDS = "libtool bzip2 zlib virtual/libintl"
DEPENDS_append_libc-musl = " argp-standalone fts "
SRC_URI = "https://sourceware.org/elfutils/ftp/${PV}/${BP}.tar.bz2"
SRC_URI[md5sum] = "52adfa40758d0d39e5d5c57689bf38d6"
SRC_URI[sha256sum] = "b88d07893ba1373c7dd69a7855974706d05377766568a7d9002706d5de72c276"

SRC_URI += "\
        file://dso-link-change.patch \
        file://Fix_elf_cvt_gunhash.patch \
        file://fixheadercheck.patch \
        file://0001-elf_getarsym-Silence-Werror-maybe-uninitialized-fals.patch \
        file://0001-remove-the-unneed-checking.patch \
        file://0001-fix-a-stack-usage-warning.patch \
        file://aarch64_uio.patch \
        file://Fix_one_GCC7_warning.patch \
        file://shadow.patch \
"

# pick the patch from debian
# http://ftp.de.debian.org/debian/pool/main/e/elfutils/elfutils_0.168-0.2.debian.tar.xz
SRC_URI += "\
        file://debian/hppa_backend.diff \
        file://debian/arm_backend.diff \
        file://debian/mips_backend.diff \
        file://debian/testsuite-ignore-elflint.diff \
        file://debian/mips_readelf_w.patch \
        file://debian/kfreebsd_path.patch \
        file://debian/0001-Ignore-differences-between-mips-machine-identifiers.patch \
        file://debian/0002-Add-support-for-mips64-abis-in-mips_retval.c.patch \
        file://debian/0003-Add-mips-n64-relocation-format-hack.patch \
        file://debian/hurd_path.patch \
        file://debian/ignore_strmerge.diff \
"
# Fix the patches from Debian with GCC7
SRC_URI += "file://fallthrough.patch"
SRC_URI_append_libc-musl = " file://0001-build-Provide-alternatives-for-glibc-assumptions-hel.patch "

# The buildsystem wants to generate 2 .h files from source using a binary it just built,
# which can not pass the cross compiling, so let's work around it by adding 2 .h files
# along with the do_configure_prepend()

inherit autotools gettext

EXTRA_OECONF = "--program-prefix=eu- --without-lzma"
EXTRA_OECONF_append_class-native = " --without-bzlib"

do_install_append() {
	if [ "${TARGET_ARCH}" != "x86_64" ] && [ -z `echo "${TARGET_ARCH}"|grep 'i.86'` ];then
		rm -f ${D}${bindir}/eu-objdump
	fi
}

EXTRA_OEMAKE_class-native = ""
EXTRA_OEMAKE_class-nativesdk = ""

ALLOW_EMPTY_${PN}_libc-musl = "1"

BBCLASSEXTEND = "native nativesdk"

# Package utilities separately
PACKAGES =+ "${PN}-binutils libelf libasm libdw"
FILES_${PN}-binutils = "\
    ${bindir}/eu-addr2line \
    ${bindir}/eu-ld \
    ${bindir}/eu-nm \
    ${bindir}/eu-readelf \
    ${bindir}/eu-size \
    ${bindir}/eu-strip"

FILES_libelf = "${libdir}/libelf-${PV}.so ${libdir}/libelf.so.*"
FILES_libasm = "${libdir}/libasm-${PV}.so ${libdir}/libasm.so.*"
FILES_libdw  = "${libdir}/libdw-${PV}.so ${libdir}/libdw.so.* ${libdir}/elfutils/lib*"
# Some packages have the version preceeding the .so instead properly
# versioned .so.<version>, so we need to reorder and repackage.
#FILES_${PN} += "${libdir}/*-${PV}.so ${base_libdir}/*-${PV}.so"
#FILES_SOLIBSDEV = "${libdir}/libasm.so ${libdir}/libdw.so ${libdir}/libelf.so"

# The package contains symlinks that trip up insane
INSANE_SKIP_${MLPREFIX}libdw = "dev-so"
