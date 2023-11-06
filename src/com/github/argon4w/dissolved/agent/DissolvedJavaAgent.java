package com.github.argon4w.dissolved.agent;

import sun.misc.Unsafe;

import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class DissolvedJavaAgent {
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        agentmain(agentArgs, instrumentation);
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("Dissolved JavaAgent Installed!");

        try {
            Field theUnsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) theUnsafeField.get(null);

            Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object implLookupBase = unsafe.staticFieldBase(implLookupField);
            long implLookupOffset = unsafe.staticFieldOffset(implLookupField);

            MethodHandles.Lookup lookup = ((MethodHandles.Lookup) unsafe.getObject(implLookupBase, implLookupOffset));

            loadClass(lookup, DissolvedJavaAgent.class, "com.github.argon4w.dissolved.agent.DissolvedServerBootstrap");
            loadClass(lookup, DissolvedJavaAgent.class, "com.github.argon4w.dissolved.agent.DissolvedMixinClass");
            loadClass(lookup, DissolvedJavaAgent.class, "com.github.argon4w.dissolved.agent.DissolvedMixinClasses");

            Class<?> unsafeClass = loadClass(lookup, DissolvedJavaAgent.class, "com.github.argon4w.dissolved.agent.DissolvedUnsafe");

            lookup.findStaticSetter(unsafeClass, "instrumentation", Instrumentation.class).invoke(instrumentation);
            lookup.findStaticSetter(unsafeClass, "lookup", MethodHandles.Lookup.class).invoke(lookup);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static Class<?> loadClass(MethodHandles.Lookup lookup, Class<?> trustedIn, String className) throws Exception {
        try (InputStream inputStream = DissolvedJavaAgent.class.getClassLoader().getResourceAsStream(className.replace('.', '/').concat(".class"))) {
            if (inputStream == null) {
                throw new ClassNotFoundException(className);
            }

            return lookup.in(trustedIn).defineClass(inputStream.readAllBytes());
        }
    }
}
