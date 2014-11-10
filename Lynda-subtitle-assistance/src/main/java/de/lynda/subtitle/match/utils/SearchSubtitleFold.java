package de.lynda.subtitle.match.utils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class SearchSubtitleFold {

	public List<File> searchPath = new LinkedList<File>();

	public void findInDirectory(File source, String name) {
		File[] files = source.listFiles();

		for (File f : files) {

			String fName = f.getName();

			if (f.isDirectory()) {

				if (CheckSameName.isMatch(fName, name)) {
					searchPath.add(f);
				}
				findInDirectory(f, name);

			}

		}
	}
}
