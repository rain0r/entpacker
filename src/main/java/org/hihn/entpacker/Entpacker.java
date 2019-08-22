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
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "Entpacker", mixinStandardHelpOptions = true, version = "checksum 1.0", description = "Unzips archives in a directory.")
public class Entpacker implements Runnable {

	@Option(names = "--delete", description = "Delete the archive after successful extraction.")
	private boolean deleteArchive = false;

	@Option(names = "--log", description = "Print what's going on.")
	private boolean logging = false;

	@Option(names = "--end", description = "Process only files with this filename-ending (default: '.zip').")
	private String end = ".zip";

	@Parameters(description = "The directory containing zip archives.")
	private List<File> directories;

	public static void main(String... args) {
		int exitCode = new CommandLine(new Entpacker()).execute(args);
		System.exit(exitCode);
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
			zipFileList = stream.map(String::valueOf).filter(path -> path.endsWith(end)).collect(Collectors.toList());
		} catch (IOException e) {
			log(e.getMessage());
		}
		return zipFileList;
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

	@Override
	public void run() {
		if (directories == null) {
			System.out.println("No directories provided");
			return;
		}

		for (File file : directories) {
			if (file.isDirectory()) {
				List<String> zipFileList = scanDirForZips(file.toPath());
				zipFileList.parallelStream().forEach(entry -> unzip(Paths.get(entry)));
			}
		}
	}
}
