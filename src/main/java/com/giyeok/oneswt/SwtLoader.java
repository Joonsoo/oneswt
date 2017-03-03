package com.giyeok.oneswt;

import org.eclipse.jdt.internal.jarinjarloader.RsrcURLStreamHandlerFactory;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class SwtLoader {
    // jars from http://download.eclipse.org/eclipse/downloads/drops4/R-4.6.2-201611241400/
    // mirror: http://ftp.kaist.ac.kr/eclipse/eclipse/downloads/drops4/R-4.6.2-201611241400/

    public static final String swtVersion = "4.6.2";

    public void loadSwt() {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        String swtFileNameOsPart =
                osName.contains("win") ? "win32-win32" :
                        osName.contains("mac") ? "cocoa-macosx" :
                                osName.contains("linux") || osName.contains("nix") ? "gtk-linux" :
                                        ""; // throw new RuntimeException("Unknown OS name: "+osName)

        String swtFileNameArchPart = osArch.contains("64") ? "x86_64" : "x86";
        String swtFileName = "swt-" + swtVersion + "-" + swtFileNameOsPart + "-" + swtFileNameArchPart + "-" + swtVersion + ".jar";

        try {
            URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();
            URL.setURLStreamHandlerFactory(new RsrcURLStreamHandlerFactory(classLoader));
            Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addUrlMethod.setAccessible(true);

            // I am using Jar-in-Jar class loader which understands this URL; adjust accordingly if you don't
            URL swtFileUrl = new URL("rsrc:" + swtFileName);
            addUrlMethod.invoke(classLoader, swtFileUrl);
        } catch (Exception e) {
            throw new RuntimeException("Unable to add the SWT jar to the class path: " + swtFileName, e);
        }
    }
}
