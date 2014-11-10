package de.lynda.subtitle.match.utils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class SearchSubtitleAndCopyTask {

	public static final String SRT_TAG = ".srt";

	public static void execute(File movFile, List<File> subtitleFileList, String[] sourceMediaType) {
		List<File> matchList = new LinkedList<File>();

		for (File subtitleFile : subtitleFileList) {
			String movFileName = movFile.getName();
			String subtitleFileName = subtitleFile.getName();

			if (CheckSameName.isMatchWithoutNumber(movFileName, subtitleFileName)) {
				matchList.add(subtitleFile);
			}
		}

		if (matchList.size() == 0) {
		} else if (matchList.size() == 1) {
			File fromFile = matchList.get(0);
			copyFile(fromFile.getAbsolutePath(), getOutputFilePath(movFile.getAbsolutePath(), sourceMediaType));
		} else if (matchList.size() > 1) {
		}
	}

	private static String getOutputFilePath(String path, String[] sourceMediaType) {
		for (String type : sourceMediaType) {
			path = path.replace("." + type, SRT_TAG);
		}
		return path;
	}

	public static void copyFile(String from, String to) {
		if (valieFilePostName(to) == false) {
			System.out.println("##### not valid path :" + to);
			return;
		}
		try {
			// Copy from file
			DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(from)));
			// Copy to: path
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(to)));

			byte[] buf = new byte[1024]; // size: 1024 byte
			int len;

			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			in.close();
			out.close();

		} catch (FileNotFoundException e) {
			System.out.println("Файл не найден: " + e.getMessage());
		} catch (EOFException e1) {
			System.out.println("Достигнут конец файла: " + e1.getMessage());
		} catch (IOException e2) {
			System.out.println("Проблема при чтении файла: " + e2.getMessage());
		} finally {
			System.out.println(to);
		}
	}

	private static boolean valieFilePostName(String to) {
		String post = to.substring(to.length() - SRT_TAG.length());
		if (post.equals(SRT_TAG)) {
			return true;
		}
		return false;
	}
}
