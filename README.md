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

Settings:

```
; C:\Users\rain0r\.config\entpacker\settings.ini
; /home/rain0r/.config/entpacker/settings.ini

[main]
delete_archive = false
logging = true
```
