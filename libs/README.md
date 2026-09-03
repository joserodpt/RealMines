# Vendored dependencies

This directory is a **file-based Maven repository**, not a loose pile of jars. It
holds dependencies that are not published to any public Maven repository and so
cannot be resolved over the network.

Right now that is exactly one artifact:

| Artifact | Version | Why it lives here |
|---|---|---|
| `de.c4t4lysm:catamines` | 2.5.11 | CataMines ships only as a SpigotMC download - no public source repo, no upstream Maven repo. Needed to compile `CataMinesConverter`. |

It is consumed through the `realmines-local-libs` repository declared in
`realmines-api/pom.xml`, with `provided` + `optional` scope, so it is never
bundled into the plugin jar and never chased by anyone depending on
`RealMinesAPI`.

## Adding or updating an artifact

Keep the standard Maven layout - `<groupId as dirs>/<artifactId>/<version>/` -
and ship a matching `.pom` next to the jar:

```
libs/de/c4t4lysm/catamines/2.5.11/catamines-2.5.11.jar
libs/de/c4t4lysm/catamines/2.5.11/catamines-2.5.11.pom
```

Or let Maven write the layout for you:

```bash
mvn deploy:deploy-file \
  -Durl=file://$(pwd)/libs \
  -DgroupId=de.c4t4lysm -DartifactId=catamines -Dversion=2.5.11 \
  -Dpackaging=jar -Dfile=CataMines-2.5.11.jar
```

Do **not** reintroduce `<scope>system</scope>`: it is deprecated in Maven 3.9,
removed in Maven 4, and it renders the published `RealMinesAPI` pom invalid,
which silently strips the API's own transitive dependencies from consumers.
