package org.hihn.entpacker;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

public class Entpacker {


    private boolean deleteArchive = false;

    private boolean logging = false;

    public static void main(String[] args) {
        new Entpacker(args);
    }

    Entpacker(String[] args) {
        loadProperties();

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
        if (destDir.exists()) {
            if (isLogging()) {
                System.out.println("Dir: " + destDir + " exists. Skipping");
            }
            return;
        }
        try {
            if (isLogging()) {
                System.out.println("Unzipping: " + fullZipPath);
            }
            ZipFile zipFile = new ZipFile(fullZipPath.toString());
            zipFile.extractAll(destDir.toString());
            if (isDeleteArchive()) {
                if (isLogging()) {
                    System.out.println("Deleting archive: " + fullZipPath);
                }
                fullZipPath.toFile().delete();
            }
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    private void loadProperties() {
        Properties properties = new Properties();

        try {
            properties.load(Entpacker.class.getResourceAsStream("/application.properties"));

            boolean deleteArchive = Boolean.valueOf(properties.getProperty("delete.archive"));
            boolean logging = Boolean.valueOf(properties.getProperty("logging"));

            setDeleteArchive(deleteArchive);
            setLogging(logging);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isDeleteArchive() {
        return deleteArchive;
    }

    public void setDeleteArchive(boolean deleteArchive) {
        this.deleteArchive = deleteArchive;
    }

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }
}
