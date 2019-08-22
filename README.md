# entpacker
Extracts all zip-files in a directory. 


## Runing
### As a single `jar`-file

```
mvn clean compile assembly:single
java -jar entpacker-0.0.1-SNAPSHOT.jar /tmp/
```

### With `mvn`

From the reposiroty base directory
```
mvn exec:java -q -Dexec.mainClass="org.hihn.entpacker.Entpacker" -Dexec.args="/foo/bar"
```

With `--end` parameter and from another directory
```
mvn exec:java -q -Dexec.mainClass="org.hihn.entpacker.Entpacker" -f /home/foo/pom.xml  -Dexec.args="'--end=_error.zip' '/foo/bar'"
```


## Arguments:

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
