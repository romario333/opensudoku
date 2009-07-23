package cz.romario.opensudoku.utils;

import java.util.Collection;
import java.util.Iterator;


public class StringUtils {
	public static String join(Collection<?> s, String delimiter) {
	      StringBuilder builder = new StringBuilder();
	      Iterator<?> iter = s.iterator();
	      while (iter.hasNext()) {
	         builder.append(iter.next());
	          if (iter.hasNext()) {
	              builder.append(delimiter);
	          }
	      }
	      return builder.toString();
	 }

}
