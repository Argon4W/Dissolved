package com.github.argon4w.dissolved.common;

import com.github.argon4w.dissolved.bootstrap.DissolvedBukkitPlugin;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DissolvedArtifacts {
    private static final List<String> DISSOLVED_LIBRARIES = List.of("org.spongepowered:mixin:0.8.5",
            "org.ow2.asm:asm-tree:9.5",
            "org.ow2.asm:asm-util:9.5",
            "org.ow2.asm:asm-analysis:9.5",
            "org.ow2.asm:asm-commons:9.5");

    private final RepositorySystem repository;
    private final DefaultRepositorySystemSession session;
    private List<RemoteRepository> repositories;

    public DissolvedArtifacts() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        repository = locator.getService(RepositorySystem.class);

        session = MavenRepositorySystemUtils.newSession();
        session.setChecksumPolicy("fail");
        session.setLocalRepositoryManager(repository.newLocalRepositoryManager(session, new LocalRepository("libraries")));
        session.setTransferListener(new AbstractTransferListener() {
            public void transferStarted(TransferEvent event) {
                DissolvedBukkitPlugin.getInstance().getLogger().log(Level.INFO, "Downloading libraries: {0}", event.getResource().getRepositoryUrl() + event.getResource().getResourceName());
            }
        });
        session.setReadOnly();

        repositories = new ArrayList<>();
    }

    public void aggregateNewRemoteRepository(List<RemoteRepository> newRepositories) {
        repositories.addAll(newRepositories);
        repositories = repository.newResolutionRepositories(session, repositories);
    }

    public List<ArtifactResult> gatherArtifacts(List<String> artifacts) throws Throwable {
        return repository.resolveDependencies(session, new DependencyRequest(new CollectRequest((Dependency)null, artifacts
                .stream()
                .map(s -> new Dependency(new DefaultArtifact(s), null))
                .collect(Collectors.toList()), repositories), null)).getArtifactResults();
    }

    public static List<URL> downloadDissolvedLibraries(Function<URL, URL> urlOperator) throws Throwable {
        //Initializing the local maven repository to download necessary libraries for Dissolved
        DissolvedArtifacts artifacts = new DissolvedArtifacts();
        artifacts.aggregateNewRemoteRepository(List.of(
                (new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2")).build(),
                (new RemoteRepository.Builder("velocity", "default", "https://nexus.velocitypowered.com/repository/maven-public/")).build()
        ));

        //Downloading the libraries and add them to ClassLoader
        return artifacts.gatherArtifacts(DISSOLVED_LIBRARIES)
                .stream()
                .map(TFunction.of(result -> result.getArtifact().getFile().toURI().toURL(), null))
                .map(urlOperator)
                .filter(Objects::nonNull)
                .toList();
    }
}
