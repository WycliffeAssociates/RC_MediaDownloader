# Resource Container (RC) Media Downloader

This tool supports downloading media content to the given resource container and update the urls with respect to the resource container itself.

# Library Module Usage
Add this custom maven repository
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

Finally, in your code:
```
val urlParameter = MediaUrlParameter(projectId, mediaDivision, mediaTypes)
// this will return a new RC file
val file = RCMediaDownloader.download(rcFile, urlParameter, DownloadClient()) 

// or you want to overwrite the original RC file
val file = RCMediaDownloader.download(rcFile, urlParameter, DownloadClient(), overwrite = true)
```
# CLI/Terminal Usage (Java 11+ required)

Run the .jar executable file with the following arguments:

```-rc <PathToRC>``` Path to your resource container (either .zip file OR directory)

```-pid <ProjectId>``` Project Identifier (usually book slug)

```-md <MediaDivision>``` Media Division ("book" OR "chapter"). e.g. ```-md book```

```-mt <MediaTypes>``` Media Type could be wav/mp3/png... separated by comma if more than one. e.g. ```-mt wav,mp3```

**Example:**

```
  java -jar rcmediadownloader.jar -rc <PathToRC> -pid <ProjectId> -md <MediaDivision> -mt <MediaTypes>
```
