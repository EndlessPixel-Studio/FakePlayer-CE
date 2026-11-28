package io.github.endlesspixel.fakeplayer.core.util.library;

import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/** 按需下载的可选依赖组, 坐标为 Maven group:artifact:version */
public enum LibrarySet {
  UNDERTOW(
      Set.of("io.undertow", "org.xnio", "org.jboss", "org.wildfly", "io.smallrye"),
      Set.of("io.github.endlesspixel.fakeplayer.core.http.UndertowExchange"),
      List.of(
          "org.jboss.logging:jboss-logging:3.6.2.Final",
          "org.jboss.xnio:xnio-api:3.8.16.Final",
          "org.jboss.xnio:xnio-nio:3.8.16.Final",
          "org.jboss.threads:jboss-threads:3.9.2",
          "org.wildfly.common:wildfly-common:2.0.1",
          "org.wildfly.client:wildfly-client-config:1.0.1.Final",
          "io.smallrye.common:smallrye-common-annotation:2.14.0",
          "io.smallrye.common:smallrye-common-constraint:2.14.0",
          "io.smallrye.common:smallrye-common-cpu:2.14.0",
          "io.smallrye.common:smallrye-common-expression:2.14.0",
          "io.smallrye.common:smallrye-common-function:2.14.0",
          "io.smallrye.common:smallrye-common-net:2.14.0",
          "io.smallrye.common:smallrye-common-os:2.14.0",
          "io.smallrye.common:smallrye-common-ref:2.14.0",
          "io.undertow:undertow-core:2.4.4.Final")),

  MYSQL(
      Set.of("com.mysql", "com.google.protobuf"),
      Set.of(),
      List.of("com.google.protobuf:protobuf-java:4.36.1", "com.mysql:mysql-connector-j:26.10.0"));

  private final java.util.Set<String> prefixes;
  private final java.util.Set<String> exactClasses;
  private final List<String> coordinates;

  LibrarySet(
      @NotNull java.util.Set<String> prefixes,
      @NotNull java.util.Set<String> exactClasses,
      @NotNull List<String> coordinates) {
    this.prefixes = prefixes;
    this.exactClasses = exactClasses;
    this.coordinates = coordinates;
  }

  public @NotNull java.util.Set<String> prefixes() {
    return this.prefixes;
  }

  public @NotNull java.util.Set<String> exactClasses() {
    return this.exactClasses;
  }

  public @NotNull List<String> coordinates() {
    return this.coordinates;
  }
}
