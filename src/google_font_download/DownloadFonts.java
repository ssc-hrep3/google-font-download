package google_font_download;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadFonts {

	public static void main(String[] args) {
		String path = "https://fonts.googleapis.com/css?family=Open+Sans:400,300,300italic,400italic,600,600italic,700,700italic,800,800italic";
		String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36";
		String outputDir = "fonts";
		process(path, userAgent, outputDir);
	}

	public static void process(String path, String userAgent, String outputDir) {
		try {
			Map<String, String> fontUrls = new HashMap<>();

			URL url = new URL(path);
			URLConnection urlConnection = url.openConnection();
			urlConnection.setRequestProperty("user-agent", userAgent);
			InputStream inputStream = urlConnection.getInputStream();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
			InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream);
			BufferedReader in = new BufferedReader(inputStreamReader);

			String inputLine;
			PrintWriter out = new PrintWriter(outputDir + "/" + "fonts.css");
			while ((inputLine = in.readLine()) != null) {
				String patternString = "^(\\s*?src:.*?url\\()(.*?)(\\).*;)$";
				if (inputLine.matches(patternString)) {
					Pattern pattern = Pattern.compile(patternString);
					Matcher matcher = pattern.matcher(inputLine);
					while (matcher.find()) {
						String googleUrl = matcher.group(2);
						String[] splittedGoogleUrl = googleUrl.split("/");
						String filename = splittedGoogleUrl[splittedGoogleUrl.length - 1];
						fontUrls.put(filename, googleUrl);

						out.print(matcher.group(1));
						out.print("'" + filename + "'");
						out.println(matcher.group(3));
					}
				} else {
					out.println(inputLine);
				}
			}
			in.close();
			out.close();

			for (String filename : fontUrls.keySet()) {
				URL website = new URL(fontUrls.get(filename));
				try (InputStream fileInputStream = website.openStream()) {
					Files.copy(fileInputStream, Paths.get(outputDir + "/" + filename), REPLACE_EXISTING);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
