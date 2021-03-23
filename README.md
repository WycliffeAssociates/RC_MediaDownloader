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
implementation 'org.wycliffeassociates:rcmediadownloader:1.1.3'
```

Finally, in your code:
```
val urlParameter = MediaUrlParameter(projectId, mediaDivision, mediaTypes, chapter)
// this will return a new RC file
val file = RCMediaDownloader.download(rcFile, urlParameter, DownloadClient()) 

// or you want to overwrite the original RC file
val file = RCMediaDownloader.download(rcFile, urlParameter, DownloadClient(), overwrite = true)

// Download project cumulatively (keep other projects in resource container)
val file = RCMediaDownloader.download(rcFile, urlParameter, DownloadClient(), singleProject = false, overwrite = true)
```
# CLI/Terminal Usage (Java 11+ required)

Run the .jar executable file with the following arguments:

```-rc <PathToRC>``` Path to your resource container (either .zip file OR directory)

```-pid <ProjectId>``` Project Identifier (usually book slug)

```-md <MediaDivision>``` Media Division ("book" OR "chapter"). e.g. ```-md book```

```-mt <MediaTypes>``` Media Type could be wav/mp3/png... separated by comma if more than one. e.g. ```-mt wav,mp3```

```-ch <ChapterNumber>``` Specify chapter number or default to all chapters

```-sp <SingleProject>``` Limits media manifest to contain at most one project

```-o <Overwrite>``` Option to overwrite the original resource container

**Example:**

```
  java -jar rcmediadownloader.jar -rc /path/to/rc -pid gen -md chapter -ch 10 -mt wav -o
```
