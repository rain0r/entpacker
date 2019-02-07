package org.hihn.entpacker;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

public class Entpacker {

    private boolean deleteArchive;
    private boolean logging;

    public static void main(String[] args) {
        new Entpacker(args);
    }

    Entpacker(String[] args) {
        for (String pPath : args) {
            Path targetDir = new File(pPath).toPath();
            if (Files.isDirectory(targetDir)) {
                try {
                    scanDirForZips(targetDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void scanDirForZips(Path foo) throws IOException {
        String glob = "glob:**/*.zip";
        String path = foo.toString();
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);
        Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(path)) {
                    unzip(path);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void unzip(Path fullZipPath) throws IOException {
        Path prefixDir = fullZipPath.getParent();
        String basename = fullZipPath.toFile().getName();
        String[] tokens = basename.split("\\.(?=[^\\.]+$)");
        if (tokens.length != 2) {
            throw new RuntimeException();
        }
        String dirname = tokens[0];
        File destDir = prefixDir.resolve(Paths.get(dirname)).toFile();
        try {
            ZipFile zipFile = new ZipFile(fullZipPath.toString());
            zipFile.extractAll(destDir.toString());
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    private void loadProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream is = getClass().getResourceAsStream("application.properties")) {
            properties.load(is);
            String foo = properties.getProperty("delete.archive", "false");
            properties.getProperty("logging", "false");
        }
    }
}
