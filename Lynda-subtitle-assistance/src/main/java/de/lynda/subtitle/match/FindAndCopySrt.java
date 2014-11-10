package de.lynda.subtitle.match;

import de.lynda.subtitle.match.utils.SearchSubtitleAndCopyTask;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by djzhang on 10/13/14.
 */
public class FindAndCopySrt {
	private List<File> subtitleFileList = new LinkedList<File>();
	private List<File> destFileList = new LinkedList<File>();

	public FindAndCopySrt(File subtitleFold, File dest) {
		collectListInPath(subtitleFold, subtitleFileList, "srt");
		collectListInPath(dest, destFileList, null);
	}

	public void collectListInPath(File fold, List<File> list, String postType) {
		File[] files = fold.listFiles();

		for (File file : files) {

			String name = file.getName().toLowerCase();
			if (file.isDirectory()) {

				if (this.checkFoldType(name)) {
					collectListInPath(file, list, postType);
				}

			} else {

				if (this.checkFileType(name, postType)) {
					list.add(file);
				}

			}

		}
	}

	private boolean checkFoldType(String name) {
		// Exercise Files
		if (name.equals("Exercise Files".toLowerCase())) {
			return false;
		}
		return true;
	}

	private boolean checkFileType(String name, String postType) {
		// /Volumes/macshare/MacPE/Lynda/keynote 09/
		if (name.equals(".DS_Store".toLowerCase())) {
			return false;
		}
		if (postType != null) {
			if (!name.contains(postType)) {
				return false;
			}
		}
		return true;
	}

	public void startFindAndCopyTask(String[] sourceMediaType) {
		for (File movFile : destFileList) {
			SearchSubtitleAndCopyTask.execute(movFile, subtitleFileList, sourceMediaType);// used
		}
		// SearchSubtitleAndCopyTask.execute(destFileList.get(0), subtitleFileList);// Test
	}
}
