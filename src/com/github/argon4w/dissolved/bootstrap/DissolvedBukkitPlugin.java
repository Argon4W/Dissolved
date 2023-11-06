package com.github.argon4w.dissolved.bootstrap;

import com.github.argon4w.dissolved.common.DissolvedArtifacts;
import com.github.argon4w.dissolved.common.TConsumer;
import com.github.argon4w.dissolved.common.TFunction;
import com.github.argon4w.dissolved.common.mixin.DissolvedBootstrapMixinTransformer;
import com.github.argon4w.dissolved.common.mixin.DissolvedMixinService;
import com.github.argon4w.dissolved.agent.DissolvedMixinClasses;
import com.sun.tools.attach.VirtualMachine;
import net.minecraft.server.dedicated.DedicatedServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.aether.repository.RemoteRepository;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.service.MixinService;
import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;
import java.util.jar.Manifest;
import java.util.logging.Level;

public class DissolvedBukkitPlugin extends JavaPlugin implements Listener {
    private static final String DISSOLVED_ACTIVATED_PROPERTY = "dissolved.activated";
    private final boolean DISSOLVED_LOADED = System.getProperties().computeIfAbsent(DISSOLVED_ACTIVATED_PROPERTY, (key) -> "false").equals("true");
    private static DissolvedBukkitPlugin instance;
    private Instrumentation instrumentation;
    private MethodHandles.Lookup lookup;
    private URL[] libraryArtifactURLs;

    public void onLoad() {
        DissolvedBukkitPlugin.instance = this;

        try {
            if (!DISSOLVED_LOADED) {
                System.setProperty(DISSOLVED_ACTIVATED_PROPERTY, "true");

                //Creating files for Dissolved JavaAgent
                File agentJarFile = ensureFile(new File("dissolved-java-agent.jar"));
                File commonJarFile = ensureFile(new File("dissolved-common.jar"));

                //Setting up DissolvedJarRedistributor for saving files to Dissolved JavaAgent
                DissolvedJarRedistributor agentRedistributor = new DissolvedJarRedistributor(agentJarFile, createAgentJarManifest());
                agentRedistributor.gatherPackage("com.github.argon4w.dissolved.agent");
                agentRedistributor.redistribute();

                DissolvedJarRedistributor commonRedistributor = new DissolvedJarRedistributor(commonJarFile, new Manifest());
                commonRedistributor.gatherPackage("com.github.argon4w.dissolved.common");
                commonRedistributor.redistribute();

                attachAgent();
                initialize();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        getLogger().log(Level.INFO, "Current CraftServer.getName(): {0}", getServer().getName());
        if (!DISSOLVED_LOADED) {
            getServer().getPluginManager().registerEvents(this, this);
        }
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) throws Exception {
        startMixin();
        Set<Config> mixinConfigs = new HashSet<>(Mixins.getConfigs());

        getDissolvedMixinService().attach(getInstrumentation(), new DissolvedBootstrapMixinTransformer(getDissolvedMixinService().getTransformer(), this));
        bootMixin(DedicatedServer.class);
        getDissolvedMixinService().detach(getInstrumentation());

        registerMixins(mixinConfigs);

        getServer().shutdown();
    }

    public File ensureFile(File file) throws IOException {
        if ((!file.exists() || file.isDirectory()) && !file.createNewFile()) {
            throw new FileAlreadyExistsException("Cannot create file: \"dissolved-java-agent.jar\"");
        }

        return file;
    }

    private Manifest createAgentJarManifest() {
        Manifest manifest = new Manifest();
        String agentClassName = "com.github.argon4w.dissolved.agent.DissolvedJavaAgent";

        manifest.getMainAttributes().putValue("Manifest-Version", "1");
        manifest.getMainAttributes().putValue("Premain-Class", agentClassName);
        manifest.getMainAttributes().putValue("Agent-Class", agentClassName);
        manifest.getMainAttributes().putValue("Launcher-Agent-Class", agentClassName);
        manifest.getMainAttributes().putValue("Can-Redefine-Classes", "true");
        manifest.getMainAttributes().putValue("Can-Retransform-Classes", "true");
        manifest.getMainAttributes().putValue("Can-Set-Native-Method-Prefix", "true");

        return manifest;
    }

    private void attachAgent() throws Throwable {
        Field theUnsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
        theUnsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafeField.get(null);

        //Enabling the ALLOW_ATTACH_SELF for attaching the current VM to load Dissolved JavaAgent at the runtime
        Field allowAttachSelfField = Class.forName("sun.tools.attach.HotSpotVirtualMachine").getDeclaredField("ALLOW_ATTACH_SELF");

        Object allowAttachSelfBase = unsafe.staticFieldBase(allowAttachSelfField);
        long allowAttachSelfOffset = unsafe.staticFieldOffset(allowAttachSelfField);
        unsafe.putBoolean(allowAttachSelfBase, allowAttachSelfOffset, true);

        VirtualMachine virtualMachine = VirtualMachine.attach(Long.toString(ProcessHandle.current().pid()));
        virtualMachine.loadAgent("dissolved-java-agent.jar");
        virtualMachine.detach();
    }

    public void gatherUnsafeValues() throws Throwable {
        Class<?> dissolvedUnsafe = Class.forName("com.github.argon4w.dissolved.agent.DissolvedUnsafe");
        instrumentation = (Instrumentation) dissolvedUnsafe.getDeclaredField("instrumentation").get(null);
        lookup = (MethodHandles.Lookup) dissolvedUnsafe.getDeclaredField("lookup").get(null);
    }

    public URL addArtifactURL(URL url) throws Throwable {
        Class<?> urlClassPathClass = Class.forName("jdk.internal.loader.URLClassPath");
        Object urlClassPath = lookup.findGetter(URLClassLoader.class, "ucp", urlClassPathClass).invoke(getClass().getClassLoader());
        lookup.findVirtual(urlClassPathClass, "addURL", MethodType.methodType(void.class, URL.class)).invoke(urlClassPath, url);

        return url;
    }

    public void downloadArtifacts() throws Throwable {
        //Initializing the local maven repository to download necessary libraries for Dissolved
        DissolvedArtifacts artifacts = new DissolvedArtifacts();
        artifacts.aggregateNewRemoteRepository(List.of(
                (new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2")).build(),
                (new RemoteRepository.Builder("velocity", "default", "https://nexus.velocitypowered.com/repository/maven-public/")).build()
        ));

        //Downloading the libraries and add them to ClassLoader
        libraryArtifactURLs = DissolvedArtifacts.downloadDissolvedLibraries(TFunction.of(this::addArtifactURL, null)).toArray(new URL[0]);
    }

    public void initialize() throws Throwable {
        gatherUnsafeValues();
        downloadArtifacts();
    }

    public void registerMixins(Set<Config> mixinConfigs) {
        for (Config config : mixinConfigs) {
            config.getConfig().getTargets().forEach(TConsumer.of(string -> {
                Class<?> clazz = Class.forName(string);
                byte[] bytes = getClassBytes(clazz);

                DissolvedMixinClasses.addNewMixin(clazz, bytes, getMixin(clazz, bytes));
            }));
        }
    }

    public void startMixin() {
        MixinBootstrap.init();
        getDissolvedMixinService().startup();
    }

    public void bootMixin(Class<?> clazz) throws Exception {
        instrumentation.retransformClasses(clazz);
    }

    public byte[] getClassBytes(Class<?> clazz) throws Exception {
        try (InputStream inputStream = ensureStream(clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/').concat(".class")))) {
            return inputStream.readAllBytes();
        }
    }

    public byte[] getMixin(Class<?> clazz, byte[] originalBytes) {
        return getDissolvedMixinService().getTransformer().transformClassBytes(null, clazz.getName(), originalBytes);
    }

    public InputStream ensureStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IOException();
        }

        return inputStream;
    }

    public URL[] getLibraryArtifactURLs() {
        return libraryArtifactURLs;
    }

    public DissolvedMixinService getDissolvedMixinService() {
        return ((DissolvedMixinService) MixinService.getService());
    }

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public MethodHandles.Lookup getLookup() {
        return lookup;
    }

    public static DissolvedBukkitPlugin getInstance() {
        return instance;
    }
}