package org.hihn.entpacker;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class Entpacker {

	private boolean deleteArchive;
	private boolean logging;

	public static void main(String[] args) {
		new Entpacker(args);
	}

	Entpacker(String[] args) {
		loadProperties();

		for (String pPath : args) {
			log("Handling: " + pPath);
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
					log("Unzipping: " + path);
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
			log("Dir: " + destDir + " exists. Skipping");
			return;
		}
		try {
			log("Unzipping: " + fullZipPath);
			ZipFile zipFile = new ZipFile(fullZipPath.toString());
			zipFile.extractAll(destDir.toString());
			if (isDeleteArchive()) {
				log("Deleting archive: " + fullZipPath);
				fullZipPath.toFile().delete();
			}
		} catch (ZipException e) {
			e.printStackTrace();
		}
	}

	private void loadProperties() {
		Path path = Paths.get(getProgrammDir().toString(), "settings.ini");
		try {
			IniFile iniFile = new IniFile(path.toString());
			setDeleteArchive(Boolean.valueOf(iniFile.getString("main", "delete_archive", "false")));
			setLogging(Boolean.valueOf(iniFile.getString("main", "logging", "false")));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Path getProgrammDir() {
		return Paths.get(System.getProperty("user.home"), ".config", "entpacker");
	}

	private void log(String pLogLine) {
		if (isLogging()) {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

			String logLine = sdf.format(new Date()) + " " + pLogLine;
			System.out.println(logLine);

			try {
				Path logFile = Paths.get(getProgrammDir().toString(), "entpacker.log");
				if (!logFile.toFile().exists()) {
					logFile.toFile().createNewFile();
				}
				logLine += System.lineSeparator();
				Files.write(logFile, logLine.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
