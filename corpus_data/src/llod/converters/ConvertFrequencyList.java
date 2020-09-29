package llod.converters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import llod.utils.rdf;

public class ConvertFrequencyList {
	public static void main(String[] args) throws FileNotFoundException, IOException {

		if(args.length!=4) {
			System.out.println("Usage:");
			System.out.println("   ConvertFrequencyList INPUT OUTPUT NAME SOURCE DESC");
			System.out.println("      INPUT = input frequency list (comma separated, no header)");
			System.out.println("      OUTPUT = output rdf file (.ttl)");
			System.out.println("      SOURCE = url to include in RDF");
			System.out.println("      LANG = language (en/ro/...)");
			System.out.println("  If INPUT or OUTPUT end with .gz the file will be considered as gzipped");
			System.exit(-1);
		}
		
		String input=args[0];
		String output=args[1];
		String source=args[2];
		String lang=args[3];
		String baseUrl=source;
		
		System.out.println("Processing ["+input+"] -> ["+output+"]");
		
		BufferedReader in=null;
		if(input.endsWith(".gz")) {
			in=new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(input)),Charset.forName("UTF-8")));
		}else {
			in=new BufferedReader(new InputStreamReader(new FileInputStream(input),Charset.forName("UTF-8")));
		}
		
		PrintWriter out=null;
		if(output.endsWith(".gz")) {
			out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(output)),Charset.forName("UTF-8"))));
		}else {
			out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),Charset.forName("UTF-8"))));
		}
		
		//Pattern allowed=Pattern.compile("[-0-9a-zA-ZăîâșțĂÎÂȘȚ+.,?!%$#@^&*()/:;]+");
		Pattern notAllowed=Pattern.compile("[^-0-9a-zA-ZăîâșțĂÎÂȘȚ+.,?!%$#@^&*()/:;]+");
		
		boolean headerWritten=false;
		for(String line=in.readLine();line!=null;line=in.readLine()) {
			line=line.trim();
			if(line.isEmpty())continue;
			
			int pos=line.lastIndexOf(',');
			if(pos<=0)continue;
			String word=line.substring(0,pos);
			long freq=Long.parseLong(line.substring(pos+1));
			
			//if(!allowed.matcher(word).find())continue;
			if(notAllowed.matcher(word).find())continue;
			
			
			if(!headerWritten) {
				headerWritten=true;
				out.println("@prefix :        <"+baseUrl+"> .");
				out.println("@prefix dct:     <http://purl.org/dc/terms/>.");
				out.println("@prefix frac:    <http://www.w3.org/ns/lemon/frac#> .");
				out.println("@prefix ontolex: <http://www.w3.org/ns/lemon/ontolex#> .");
				out.println("@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");
				out.println("@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .");
				out.println("@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .");				
				out.println("");
			}
			
			out.println(":"+rdf.escapeEntity(word)+" a ontolex:LexicalEntry;");
			out.println("  ontolex:canonicalForm \""+word+"\"@"+lang+";");
			out.println("  frac:frequency [");
			out.println("    a frac:CorpusFrequency;");
			out.println("	 rdf:value \""+freq+"\"^^xsd:int;");
			out.println("    dct:source <"+source+"> ].");
			
		}
		
		in.close();
		out.close();
	}
}
