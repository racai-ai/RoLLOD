package llod.utils;

public class rdf {

	public static String escapeEntity(String data) {
		String ret=data;

		ret=ret.replaceAll(" ", "_");
		ret=ret.replaceAll("[\\\\]", "\\\\\\\\");
		ret=ret.replaceAll("[$]", "\\\\\\$");
		//ret=ret.replaceAll("[-]", "\\\\-");
		
		String toEscape="~.-!&'()*+,;=/?#@%_";
		for(int i=0;i<toEscape.length();i++) {
			ret=ret.replaceAll("["+toEscape.charAt(i)+"]", "\\\\"+toEscape.charAt(i));
		}
		
		ret=ret.replaceAll("[<]", "%3C");
		ret=ret.replaceAll("[>]", "%3E");
		ret=ret.replaceAll("[”]", "#e2809d");
		ret=ret.replaceAll("[„]", "#e2809e");
		ret=ret.replaceAll("[“]", "#e2809c");
		ret=ret.replaceAll("[—]", "#e28094");
		ret=ret.replaceAll("[’]", "#e28099");
		ret=ret.replaceAll("[─]", "#e29480");
		ret=ret.replaceAll("[\\^]", "%5E");
		ret=ret.replaceAll("[\\[]", "%5B");
		ret=ret.replaceAll("[\\]]", "%5D");
		
		ret=ret.replaceAll("[»]","#c2bb");		
		ret=ret.replaceAll("[«]","#c2ab");
		ret=ret.replaceAll("[°]","#c2b0");
		ret=ret.replaceAll("[²]","#c2b2");
		
		return ret;
	}
	
}
