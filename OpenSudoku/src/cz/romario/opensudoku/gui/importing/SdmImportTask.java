package cz.romario.opensudoku.gui.importing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.net.Uri;
import android.widget.ProgressBar;

public class SdmImportTask extends AbstractImportTask {

	public SdmImportTask(Context context, ProgressBar progressBar) {
		super(context, progressBar);
	}
	
	@Override
	protected Boolean processImport() {
		Uri uri = getOptions().getUri();
		getOptions().setFolderName(uri.getLastPathSegment());

		try {
			URL url = new URL(uri.toString());
			InputStreamReader isr = new InputStreamReader(url.openStream());
			BufferedReader br = null;
			try {
				br = new BufferedReader(isr);
				String s;
				while ((s = br.readLine()) != null) {
					if (!s.equals("")) {
						importGame(s);
					}
				}
			} finally {
				if (br != null) br.close();
			}

			return true;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
