package com.github.argon4w.dissolved.common;

import com.github.argon4w.dissolved.agent.DissolvedBootstrapClassLoader;
import com.github.argon4w.dissolved.agent.DissolvedMixinClasses;
import com.github.argon4w.dissolved.agent.DissolvedUnsafe;
import jline.UnsupportedTerminal;
import joptsimple.OptionSet;
import org.bukkit.craftbukkit.Main;
import org.fusesource.jansi.AnsiConsole;

import java.lang.instrument.ClassFileTransformer;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;

public class DissolvedServerMain {
    public static void boot(OptionSet options, DissolvedBootstrapClassLoader classLoader) {
        try {
            Main.useJline = ! UnsupportedTerminal.class.getName().equals(System.getProperty("jline.terminal"));

            if (options.has("nojline")) {
                System.setProperty("user.language", "en");
                Main.useJline = false;
            }

            if (Main.useJline) {
                AnsiConsole.systemInstall();
            } else {
                System.setProperty("jline.terminal", UnsupportedTerminal.class.getName());
            }

            if (options.has("noconsole")) {
                Main.useConsole = false;
            }

            DissolvedArtifacts.downloadDissolvedLibraries(TFunction.of(DissolvedServerMain::addArtifactURL, null));

            classLoader.addNewTransformer("dissolved", new ClassFileTransformer() {
                @Override
                public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                    return DissolvedMixinClasses.getMixinBytes(className).orElse(classfileBuffer);
                }
            });

            System.out.println("Loading libraries, please wait...");
            net.minecraft.server.Main.main(options);
        } catch (Throwable var11) {
            var11.printStackTrace();
        }
    }

    public static URL addArtifactURL(URL url) throws Throwable {
        Class<?> urlClassPathClass = Class.forName("jdk.internal.loader.URLClassPath");
        Object urlClassPath = DissolvedUnsafe.lookup.findGetter(URLClassLoader.class, "ucp", urlClassPathClass).invoke(DissolvedServerMain.class.getClassLoader());
        DissolvedUnsafe.lookup.findVirtual(urlClassPathClass, "addURL", MethodType.methodType(void.class, URL.class)).invoke(urlClassPath, url);

        return url;
    }
}
