from urllib.request import urlretrieve
from zipfile import ZipFile
import os
import shutil

versionNum = "4.7.2"
releaseNum = "R-4.7.2-201711300510"

archs = [
    ["win32-win32", "x86"],
    ["win32-win32", "x86_64"],
    ["cocoa-macosx", "x86_64"],
    ["gtk-linux", "x86"],
    ["gtk-linux", "x86_64"],
    # ["gtk-aix", "ppc"],
    # ["gtk-aix", "ppc64"],
    # ["gtk-hpux", "ia64"],
    # ["gtk-linux", "aarch64"],
    # ["gtk-linux", "arm"],
    # ["gtk-linux", "ppc"],
    # ["gtk-linux", "ppc64"],
    # ["gtk-linux", "ppc64le"],
    # ["gtk-linux", "s390"],
    # ["gtk-linux", "s390x"],
    # ["gtk-solaris", "sparcv9"],
    # ["gtk-solaris", "x86_64"],
]

# url = "ftp://ftp.kaist.ac.kr/eclipse/eclipse/downloads/drops4/R-4.6.2-201611241400/swt-4.6.2-cocoa-macosx-x86_64.zip"

def readable(size):
    units = ['', 'K', 'M', 'G', 'T']
    (num, unit) = (size, 0)
    while num > 1024 and unit < len(units):
        num /= 1024.0
        unit += 1
    x = "%.2f" % num
    while x[-1] == '0':
        x = x[:-1]
    if x[-1] == '.':
        x = x[:-1]
    return x + units[unit]

def download(url, fileName):
    def reporthook(blocknum, blocksize, totalsize):
        readsofar = blocknum * blocksize
        if totalsize > 0:
            percent = min(100, readsofar * 1e2 / totalsize)
            progress = "%10s / %10s (%5.1f%%)" % (readable(readsofar), readable(totalsize), percent)
        else:
            progress = "%10s" % readable(readsofar)
        print("%40s %s" % (fileName, progress), end='\r')
        if readsofar >= totalsize:
            print()

    urlretrieve(url, fileName, reporthook)

files = [
    [
        "ftp://ftp.kaist.ac.kr/eclipse/eclipse/downloads/drops4/%s/swt-%s-%s-%s.zip" % (releaseNum, versionNum, archName, archType),
        "swt-%s-%s-%s" % (versionNum, archName, archType)
    ] for (archName, archType) in archs
]

try:
    os.mkdir("tmp")
except:
    pass

try:
    os.mkdir("swt")
except:
    pass

for f in files:
    (url, fileNameNoZip) = f
    fileName = "tmp/" + fileNameNoZip + ".zip"

    print("Downloading file %s" % fileName)
    download(url, fileName)

    # print("Validating file %s" % fileName)
    # TODO

    print("Extracting file %s" % fileName)
    with ZipFile(fileName) as myzip:
        myzip.extract("swt.jar", "tmp")
        shutil.move("tmp/swt.jar", "swt/" + fileNameNoZip + ".jar")
        myzip.extract("swt-debug.jar", "tmp")
        shutil.move("tmp/swt-debug.jar", "swt/" + fileNameNoZip + "-debug.jar")
        myzip.extract("src.zip", "tmp")
        shutil.move("tmp/src.zip", "swt/" + fileNameNoZip + "-sources.jar")

print("** You may want to delete tmp folder")
