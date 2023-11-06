package com.github.argon4w.dissolved.agent;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URLClassLoader;

public class DissolvedServerBootstrap {
    public static void boot(Object optionSet, ClassLoader passthroughClassLoader) {
        try {
            DissolvedBootstrapClassLoader mainClassLoader = new DissolvedBootstrapClassLoader(
                    ((URLClassLoader) passthroughClassLoader).getURLs(),
                    passthroughClassLoader
            );
            mainClassLoader.addURL(new File("dissolved-common.jar").toURI().toURL());

            System.out.println("Restarting server");
            Thread runThread = new Thread(() -> {
                try {
                    Class<?> optionSetClass = Class.forName("joptsimple.OptionSet", true, mainClassLoader);
                    Class<?> mainClass = Class.forName("com.github.argon4w.dissolved.common.DissolvedServerMain", true, mainClassLoader);

                    MethodHandle mainHandle = MethodHandles.lookup().findStatic(mainClass, "boot", MethodType.methodType(Void.TYPE, optionSetClass, DissolvedBootstrapClassLoader.class)).asFixedArity();
                    mainHandle.invoke(optionSet, mainClassLoader);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }, "DissolvedServerMain");

            runThread.setContextClassLoader(mainClassLoader);
            runThread.start();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
