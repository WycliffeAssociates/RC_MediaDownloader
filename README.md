# Resource Container (RC) Media Downloader

This tool supports downloading media content to the given resource container and update the urls with respect to the resource container itself.

# Installation
Add the custom maven repository
```
repositories {
    ...
    maven { url "https://nexus-registry.walink.org/repository/maven-public/" }
}
```
Add this to your dependencies (Gradle):
```
implementation 'org.wycliffeassociates:rcmediadownloader:1.0.0'
```

# Usage

Run the .jar executable file with the following arguments:

```-rc <PathToRC>``` Path to your resource container (either .zip file OR directory)

```-pid <ProjectId>``` Project Identifier (usually book slug)

```-md <MediaDivision>``` Media Division ("book" OR "chapter"). e.g. ```-md book```

```-mt <MediaTypes>``` Media Type could be wav/mp3/png... separated by comma if more than one. e.g. ```-mt wav,mp3```

**Example:**

```
  java -jar rcmediadownloader.jar -rc <PathToRC> -pid <ProjectId> -md <MediaDivision> -mt <MediaTypes>
```

