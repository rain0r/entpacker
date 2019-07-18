# entpacker
Extract all zip-files in a directory. 

Build a single, executable jar:

```
mvn clean compile assembly:single
```

Using:

```
java -jar entpacker-0.0.1-SNAPSHOT.jar /tmp/
```

Arguments:

```
Usage: Entpacker [-hV] [--delete] [--log] [--end=<end>] [<directories>...]
Unzips archives in a directory.
      [<directories>...]   The directory containing zip archives.
      --delete             Delete the archive after successful extraction.
      --end=<end>          Process only files with this filename-ending
                             (default: '.zip').
  -h, --help               Show this help message and exit.
      --log                Print what's going on.
  -V, --version            Print version information and exit.
```