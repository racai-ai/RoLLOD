package llod.converters;

import llod.utils.rdf;

public class test {

	public static void main(String[] args) {

		System.out.println(rdf.escapeEntity("-"));
		System.out.println(rdf.escapeEntity("\\"));
		System.out.println(rdf.escapeEntity("</s>"));
		
	}

}
