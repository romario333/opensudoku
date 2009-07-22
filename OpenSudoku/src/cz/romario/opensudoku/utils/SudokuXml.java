package cz.romario.opensudoku.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import cz.romario.opensudoku.db.SudokuDatabase;
import cz.romario.opensudoku.game.SudokuGame;

import android.util.Log;

public class SudokuXml {
	private static final String TAG = "SudokuXml";
	
	public static long importUri(android.net.Uri auri,SudokuDatabase sudokuDB) {
		Log.i(TAG,auri.toString());
		try {
			java.net.URI juri;
			juri = new java.net.URI(auri.getScheme(),
			        auri.getSchemeSpecificPart(),
			        auri.getFragment());
			InputStreamReader isr=new InputStreamReader(juri.toURL().openStream());
			long folderID;
			try{
				folderID=importXml(isr,sudokuDB);
			}finally{
				isr.close();
			}
			return folderID;
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
	
	public static long importXml(Reader in,SudokuDatabase sudokuDB) {
		String name="import";
		ArrayList<String> games=new ArrayList<String>();
	  
		BufferedReader inBR = new BufferedReader(in);
    	/*while((s=in.readLine())!=null){
    		Log.i(tag, "radek: "+s);
    	}*/
		
		//parse xml
    	XmlPullParserFactory factory;
    	XmlPullParser xpp;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			xpp = factory.newPullParser();
	        xpp.setInput( inBR );
	        int eventType = xpp.getEventType();
	        String lastTag="";
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	            if(eventType == XmlPullParser.START_TAG) {
	            	lastTag=xpp.getName();
	            } else if(eventType == XmlPullParser.END_TAG) {
	            	lastTag="";
	            } else if(eventType == XmlPullParser.TEXT) {
	            	if(lastTag.equals("name")){
	           	 		name=xpp.getText();
	           	 	}else if(lastTag.equals("game")){
	           	 		games.add(xpp.getText());
	           	 	} 

	            }
	            eventType = xpp.next();
	           }
		} catch (XmlPullParserException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		//store to db
		long folderID=sudokuDB.insertFolder(name);
		for(int i=0;i<games.size();i++){
			SudokuGame sudoku = SudokuGame.parseString(games.get(i));
			sudokuDB.insertSudoku(folderID, sudoku);	
		}
		
		return folderID;
	}
}
