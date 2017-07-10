package io.schrepfer.googlefontdownload;

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
		String path = "https://fonts.googleapis.com/css?family=Roboto+Mono:300,400|Roboto:300,400,400i,500";
		String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36";
		String outputDir = "fonts";
		process(path, userAgent, outputDir);
	}

	public static void process(String path, String userAgent, String outputDir) {
		try {
			Map<String, String> fontUrls = new HashMap<>();

			System.out.println("Downloading all font from "+ path + "\n");
			URL url = new URL(path);
			URLConnection urlConnection = url.openConnection();
			urlConnection.setRequestProperty("user-agent", userAgent);
			InputStream inputStream = urlConnection.getInputStream();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
			InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream);
			BufferedReader in = new BufferedReader(inputStreamReader);

			PrintWriter out = new PrintWriter(outputDir + "/" + "fonts.css");
			String patternString = "^(\\s*?src:.*?url\\()(.*?)(\\).*;)$";
			Pattern pattern = Pattern.compile(patternString);

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.matches(patternString)) {
					Matcher matcher = pattern.matcher(inputLine);
					while (matcher.find()) {
						String fontUrl = matcher.group(2);
						String[] splittedFontUrl = fontUrl.split("/");
						String filename = splittedFontUrl[splittedFontUrl.length - 1];
						fontUrls.put(filename, fontUrl);

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

			for (Map.Entry<String, String> font : fontUrls.entrySet()) {
				String filename = font.getKey();
				URL fontUrl = new URL(font.getValue());

				System.out.println("Downloading Font URL: " + fontUrl);
				try (InputStream fileInputStream = fontUrl.openStream()) {
					Files.copy(fileInputStream, Paths.get(outputDir + "/" + filename), REPLACE_EXISTING);
				}
			}

		} catch (IOException e) {
			System.err.println("An error occurred.");
			e.printStackTrace();
		}
	}

}
