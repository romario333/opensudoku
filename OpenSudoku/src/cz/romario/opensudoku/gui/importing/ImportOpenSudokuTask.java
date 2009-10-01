package cz.romario.opensudoku.gui.importing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.net.Uri;
import android.widget.ProgressBar;

public class ImportOpenSudokuTask extends AbstractImportTask {

	// TODO: coze? tohle bych mel provadet v AbstractImportTask
	private static final Pattern SUDOKU_PATT = Pattern.compile(".*\\D([\\d]{81})\\D.*");
	
	public ImportOpenSudokuTask(Context context, ProgressBar progressBar) {
		super(context, progressBar);
	}
	
	@Override
	protected Boolean processImport() {
		Uri uri = getOptions().getUri();
		try {
			java.net.URI juri;
			juri = new java.net.URI(uri.getScheme(), uri
					.getSchemeSpecificPart(), uri.getFragment());
			InputStreamReader isr = new InputStreamReader(juri.toURL()
					.openStream());
			try {
				return importXml(isr);
			} finally {
				isr.close();
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean importXml(Reader in) {
		String folderName = "import";

		BufferedReader inBR = new BufferedReader(in);
		/*
		 * while((s=in.readLine())!=null){ Log.i(tag, "radek: "+s); }
		 */

		// parse xml
		XmlPullParserFactory factory;
		XmlPullParser xpp;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(false);
			xpp = factory.newPullParser();
			xpp.setInput(inBR);
			int eventType = xpp.getEventType();
			String lastTag = "";
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					lastTag = xpp.getName();
					if (lastTag.equals("game")) {
						importGame(xpp.getAttributeValue(null, "data"));
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					lastTag = "";
				} else if (eventType == XmlPullParser.TEXT) {
					if (lastTag.equals("name")) {
						folderName = xpp.getText();
					} else if (lastTag.equals("parse-page")) {
						// download page and find sudoku strings
						URL url = new URL(xpp.getText());
						InputStreamReader isr = new InputStreamReader(url
								.openStream());
						BufferedReader br = null;
						try {
							br = new BufferedReader(isr);
							
							String s;
							while ((s = br.readLine()) != null) {
								Matcher m = SUDOKU_PATT.matcher(s);
								if (m.find()) {
									importGame(m.group(1));
								}
						}
						} finally {
							if (br != null) br.close();
						}
					}

				}
				eventType = xpp.next();
			}

			getOptions().setFolderName(folderName);

			return true;
		} catch (XmlPullParserException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
