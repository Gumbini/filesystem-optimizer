package main;

import filesystem.FileManager;
import util.StringUtil;

import java.io.File;
import java.util.Scanner;

public class Main {
	public static void die(String message) {
		System.err.println("[FATAL] " + message);
		System.exit(1);
	}

	public static void hint(String message) {
		System.err.println("[INFO/WARN] " + message);
	}

	public static void main(String[] args) throws Exception {
		System.err.println("Filesystem Optimizer v1.0");

		ArgParser argParser = new ArgParser()
			.onError(Main::die)
			.onWarning(Main::hint)
			.parseArgs(args)
			.setDefaultIfAbsent(ArgParser.ValueKey.HASH_ALGORITHM, "SHA-256")
			.setDefaultIfAbsent(ArgParser.BivalentKey.INTERACTIVE_CONSOLE, true);

		FileManager fileManager = new FileManager(argParser);

		File dirFile = (File) argParser.getValue(ArgParser.ValueKey.DIR_FILE);
		if (dirFile != null) {
			Scanner dirFileScanner = new Scanner(dirFile);
			while (dirFileScanner.hasNextLine()) {
				fileManager.crawlFilesystem(dirFileScanner.nextLine());
			}
			dirFileScanner.close();
		}

		if (!argParser.isSet(ArgParser.BivalentKey.INTERACTIVE_CONSOLE))
			return;

		argParser.onError(Main::hint);

		// Interactive console

		Scanner stdinScanner = new Scanner(System.in);
		while (true) {
			System.err.printf("(%d)$> ", fileManager.elements());

			if (!stdinScanner.hasNextLine())
				break;

			String[] tokens = StringUtil.tokenize(stdinScanner.nextLine());
			if (tokens.length == 0)
				continue;

			switch (tokens[0]) {
				case "exit":
				case "e":
					stdinScanner.close();
					System.err.println("Bye");
					return; // exit
				case "crawl":
				case "c":
					if (tokens.length == 1) {
						hint("No arguments given! Action aborted.");
						break;
					}
					for (int i = 1; i < tokens.length; i++) {
						fileManager.crawlFilesystem(tokens[i]);
					}
					break;
				case "drop-wrappers":
				case "rw":
					fileManager.dropWrappers();
					break;
				case "dump-wrappers":
				case "dw":
					if (tokens.length == 1) {
						hint("No argument given! Action aborted.");
						break;
					}
					if (tokens.length > 2) {
						hint("Multiple arguments given! Using the first...");
					}
					fileManager.dumpWrappers(new File(tokens[1]));
					break;
				case "load-wrappers":
				case "lw":
					if (tokens.length == 1) {
						hint("No argument given. Do nothing...");
						break;
					}
					fileManager.loadWrappers(new File(tokens[1]));
					break;
				case "drop-options":
				case "do":
					argParser.clear();
					break;
				case "add-options":
				case "ao":
					String[] newArgs = new String[tokens.length - 1];
					System.arraycopy(tokens, 1, newArgs, 0, newArgs.length);
					argParser.parseArgs(newArgs);
					break;
				default:
					hint("Unknown command: \"" + tokens[0] + "\"");
			}
		}
		stdinScanner.close();
		System.err.println();
		System.err.println("Interrupted");
	}
}
