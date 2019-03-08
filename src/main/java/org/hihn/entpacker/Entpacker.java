package org.hihn.entpacker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

		for (String directory : args) {
			Path dirPath = new File(directory).toPath();
			if (Files.isDirectory(dirPath)) {
				List<String> zipFileList = scanDirForZips(dirPath);
				zipFileList.parallelStream().forEach(entry -> unzip(Paths.get(entry)));
			}
		}
	}

	private void unzip(Path fullZipPath) {
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
			log(e.getMessage());
		}
	}

	private List<String> scanDirForZips(Path dirPath) {
		List<String> zipFileList = null;
		try (Stream<Path> stream = Files.walk(dirPath, 1)) {
			zipFileList = stream.map(String::valueOf).filter(path -> path.endsWith(".zip")).collect(Collectors.toList());
		} catch (IOException e) {
			log(e.getMessage());
		}
		return zipFileList;
	}

	private void loadProperties() {
		Path path = Paths.get(getProgrammDir().toString(), "settings.ini");
		try {
			IniFile iniFile = new IniFile(path.toString());
			setDeleteArchive(Boolean.valueOf(iniFile.getString("main", "delete_archive", "false")));
			setLogging(Boolean.valueOf(iniFile.getString("main", "logging", "false")));
		} catch (IOException e) {
			log("Could not read settings: " + e.getMessage());
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
