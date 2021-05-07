package st.cs.uni.saarland.de.reachabilityAnalysis;

import java.util.HashSet;
import java.util.Set;

public class CONTENT_RESOLVER_CONSTANTS {
	public final static String QUERY="<android.content.ContentResolver: android.database.Cursor query(android.net.Uri,java.lang.String[],java.lang.String,java.lang.String[],java.lang.String)>";
	public final static String INSERT="<android.content.ContentResolver: android.net.Uri insert(android.net.Uri,android.content.ContentValues)>";
	public final static String BULKINSERT="<android.content.ContentResolver: int bulkInsert(android.net.Uri,android.content.ContentValues[])>";
	public final static String DELETE="<android.content.ContentResolver: int delete(android.net.Uri,java.lang.String,java.lang.String[])>";
	public final static String UPDATE="<android.content.ContentResolver: int update(android.net.Uri,android.content.ContentValues,java.lang.String,java.lang.String[])>";
	public final static String PARSE_URI="<android.net.Uri: android.net.Uri parse(java.lang.String)>";

	private static Set<String> prefixes = new HashSet<>();

	public static Set<String> getPrefixes(){
		if(prefixes.isEmpty()){
			prefixes.add(QUERY.split("\\(")[0]);
			prefixes.add(INSERT.split("\\(")[0]);
			prefixes.add(BULKINSERT.split("\\(")[0]);
			prefixes.add(DELETE.split("\\(")[0]);
			prefixes.add(UPDATE.split("\\(")[0]);
		}
		return prefixes;
	}
}
