package de.opencourse.subtitle;

import de.opencourse.subtitle.data.SubtitleFromData;
import de.opencourse.subtitle.model.SubtitleInfo;
import de.opencourse.subtitle.utils.DownloadFile;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by djzhang on 11/10/14.
 */
public class ParseSRTHelper {

    private LinkedHashMap<Integer, SubtitleInfo> subtitleInfoLinkedHashMap = new LinkedHashMap<Integer, SubtitleInfo>();
    private SubtitleInfo subtitleInfo;

    public static final String outPath = "/Volumes/Home/djzhang/Desktop";

    public static final int TYPE_FETCH_NEXT = 0;
    public static final int TYPE_FETCH_END = 1;
    public static final int TYPE_FETCH_TIMEOUT = 2;

    public static void main(String[] args) {
        ParseSRTHelper parseSRTHelper = new ParseSRTHelper();

        parseSRTHelper.fetchByUrl(SubtitleFromData.urlTemplate);
        parseSRTHelper.saveAll(new File(outPath, SubtitleFromData.urlTitle));
    }

    private void saveAll(File outPath) {
        System.out.println("");

        Collection<SubtitleInfo> values = subtitleInfoLinkedHashMap.values();
        Iterator<SubtitleInfo> iterator = values.iterator();
        while (iterator.hasNext()) {
            SubtitleInfo subtitleInfo = iterator.next();
            // System.out.println(" = " + subtitleInfo.title);
            File outFile = new File(outPath, subtitleInfo.title + ".srt");
            String absolutePath = outFile.getAbsolutePath();
            System.out.println("  absolutePath = " + absolutePath);
            try {
                DownloadFile.downloadAndSave(outFile, subtitleInfo.downloadUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void fetchByUrl(String url) {
        for (int i = 1; i < 100; i++) {
            subtitleInfo = null;
            int feedType = checkFetchSubtitle(String.format("%s%d", url, i), i);
            if (feedType == TYPE_FETCH_END) {
                break;
            }
        }
    }

    private int checkFetchSubtitle(String current, int pos) {
        boolean isEnd = false;
        while (!isEnd) {
            int feedType = fetchSubtitleValid(current);
            if (feedType != TYPE_FETCH_TIMEOUT) {
                if (feedType == TYPE_FETCH_NEXT) {
                    subtitleInfoLinkedHashMap.put(pos, subtitleInfo);
                }
                return feedType;
            }
        }
        return -1;
    }

    private int fetchSubtitleValid(String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            if (e instanceof HttpStatusException) {
                HttpStatusException exception = (HttpStatusException) e;
                if (exception.getStatusCode() == 404) {
                    System.out.println("StatusCode is 404,not found url:" + url);
                    return TYPE_FETCH_END;
                }
            } else if (e instanceof SocketTimeoutException) {
                // Read timed out
                SocketTimeoutException exception = (SocketTimeoutException) e;
                exception.getMessage();
                System.out.println("Read timed out,current url:" + url);
                return TYPE_FETCH_TIMEOUT;
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

        return TYPE_FETCH_NEXT;
    }

    private void getTitle(Document doc) {
        Element session_body = doc.getElementById("session_body");
        Elements h2s = session_body.getElementsByTag("h2");
        Element first = h2s.first();
        String html = first.html();
        System.out.println("  title is " + html);

        subtitleInfo = new SubtitleInfo();
        subtitleInfo.title = html;

    }

    private void readLines(String html) throws IOException {
        ByteArrayInputStream stringReader = new ByteArrayInputStream(html.getBytes("UTF8"));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stringReader, "UTF-8"));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            parseJavascript(line);
        }
    }

    // file: "http://openmedia.yale.edu/projects/courses/fall08/musi112/embed/musi112_01_090408.srt",
    private void parseJavascript(String html) {
        Pattern p = Pattern.compile("file: \"(.+?).srt\""); // Regex for the
        // value of the
        // html
        Matcher m = p.matcher(html); // you have to use html here and NOT text! Text will drop the 'html' part

        while (m.find()) {
            // System.out.println(" reg-0: " + m.group()); // the whole html text
            String group = m.group(1) + ".srt";
            subtitleInfo.downloadUrl = group;
            System.out.println("  subtitle is " + group); // value only
        }
    }

}
