package com.github.argon4w.dissolved.agent;

import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandles;

public class DissolvedUnsafe {
    public static Instrumentation instrumentation;
    public static MethodHandles.Lookup lookup;
}
