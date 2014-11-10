package de.lynda.subtitle.match.utils;

public class CheckSameName {

	public static boolean isMatchWithoutNumber(String name, String match) {
		if (name.equals("Keynote '09 Essential Training")) {
			int x = 0;
		}
		name = removePostType(name.toLowerCase());
		match = removePostType(match.toLowerCase());

		String[] split = match.split(" ");
		if (split.length > 1) {
			if (isNumeric(split[0])) {
				match = match.replace(split[0] + " ", "");
				split = match.split(" ");
			}
		}

		int size = split.length;
		int step = 0;
		for (String s : split) {
			if (name.contains(s)) {
				step++;
			}
		}

		return step == size;
	}

	private static String removePostType(String name) {
		int index = name.lastIndexOf(".");
		if (index != -1) {
			return name.substring(0, index);
		}
		return name;
	}

	public static boolean isMatch(String name, String match) {
		if (name.equals("Keynote '09 Essential Training")) {
			int x = 0;
		}
		name = name.toLowerCase();
		match = match.toLowerCase();

		String[] split = match.split(" ");

		int size = split.length;
		int step = 0;
		for (String s : split) {
			if (name.contains(s)) {
				step++;
			}
		}

		return step == size;
	}

	public static boolean isNumeric(String str) {
		for (int i = str.length(); --i >= 0;) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

}
