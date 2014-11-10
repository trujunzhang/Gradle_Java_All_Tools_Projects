package de.opencourse.subtitle;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by djzhang on 11/10/14.
 */
public class ParseSRTHelper {

	public static void main(String[] args) {
		String url = "http://oyc.yale.edu/music/musi-112/lecture-";
		for (int i = 1; i < 100; i++) {
			boolean hasEnd = fetchSubtitleByUrl(String.format("%s%d", url, i));
			if (hasEnd)
				break;
		}
	}

	private static boolean fetchSubtitleByUrl(String url) {
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			if (e instanceof HttpStatusException) {
				HttpStatusException exception = (HttpStatusException) e;
				if (exception.getStatusCode() == 404) {
					System.out.println("StatusCode is 404,not found url:" + url);
					return true;
				}
			}
		}

		getTitle(doc);

		Element mediaplayer = doc.getElementById("mediaplayer");
		Elements scripts = mediaplayer.parent().getElementsByTag("script");
		for (Element script : scripts) {
			String html = script.html();
			try {
				readLines(html);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return false;
	}

	private static void getTitle(Document doc) {
		Element session_body = doc.getElementById("session_body");
		Elements h2s = session_body.getElementsByTag("h2");
		Element first = h2s.first();
		String html = first.html();
		System.out.println("title is " + html);

	}

	private static void readLines(String html) throws IOException {
		ByteArrayInputStream stringReader = new ByteArrayInputStream(html.getBytes("UTF8"));
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stringReader, "UTF-8"));

		String line;
		while ((line = bufferedReader.readLine()) != null) {
			parseJavascript(line);
		}
	}

	// file: "http://openmedia.yale.edu/projects/courses/fall08/musi112/embed/musi112_01_090408.srt",
	private static void parseJavascript(String html) {
		Pattern p = Pattern.compile("file: \"(.+?).srt\""); // Regex for the
															// value of the
															// html
		Matcher m = p.matcher(html); // you have to use html here and NOT text! Text will drop the 'html' part

		while (m.find()) {
			// System.out.println(" reg-0: " + m.group()); // the whole html text
			String group = m.group(1) + ".srt";
			System.out.println(" subtitle is " + group); // value only
		}
	}

}
