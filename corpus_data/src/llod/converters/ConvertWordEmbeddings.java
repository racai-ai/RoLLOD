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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import llod.utils.rdf;

public class ConvertWordEmbeddings {

	public static void main(String[] args) throws FileNotFoundException, IOException {

		if(args.length!=6) {
			System.out.println("Usage:");
			System.out.println("   ConvertWordEmbeddings INPUT OUTPUT NAME SOURCE DESC");
			System.out.println("      INPUT = input word embeddings file");
			System.out.println("      OUTPUT = output rdf file (.ttl)");
			System.out.println("      NAME = embeddings name to be used inside RDF (no spaces)");
			System.out.println("      SOURCE = url to include in RDF");
			System.out.println("      DESC = description to be included in RDF (English)");
			System.out.println("      LANG = language of embeddings (en/ro/...)");
			System.out.println("  If INPUT or OUTPUT end with .gz the file will be considered as gzipped");
			System.exit(-1);
		}
		
		String input=args[0];
		String output=args[1];
		String name=args[2];
		String source=args[3];
		String desc=args[4];
		String lang=args[5];
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
		
		boolean first=true;
		int sz=-1;
		boolean headerWritten=false;
		for(String line=in.readLine();line!=null;line=in.readLine()) {
			line=line.trim();
			if(line.isEmpty())continue;
			
			String[] data=line.split("[ ]");
			
			if(first) {
				first=false;
				if(data.length==2) {
					System.out.println("Reading "+data[0]+" entries of size "+data[1]);
					sz=Integer.parseInt(data[1]);
					continue;
				}
			}
			
			if(sz==-1) {
				sz=data.length-1;
				System.out.println("Reading entries of size "+sz);
			}
			
			if(headerWritten==false) {
				headerWritten=true;
				out.println("@prefix :        <"+baseUrl+"> .");
				out.println("@prefix dct:     <http://purl.org/dc/terms/>.");
				out.println("@prefix frac:    <http://www.w3.org/ns/lemon/frac#> .");
				out.println("@prefix ontolex: <http://www.w3.org/ns/lemon/ontolex#> .");
				out.println("@prefix owl:     <http://www.w3.org/2002/07/owl#> .");
				out.println("@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");
				out.println("@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .");
				out.println("@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .");
				out.println("");
				out.println(":"+name+" rdfs:subClassOf frac:Embedding;");
				out.println("  rdfs:subClassOf"); 
				out.println("    [ a owl:Restriction;");
				out.println("	   owl:onProperty dct:source;");
				out.println("	   owl:hasValue");
			    out.println("		  <"+source+"> ],");
			    out.println("    [ a owl:Restriction;");
			    out.println("	   owl:onProperty dct:extent;");
			    out.println("	   owl:hasValue \""+sz+"\"^^xsd:int ],");
			    out.println("	 [ a owl:Restriction;");
			    out.println("	   owl:onProperty dct:description;");
			    out.println("	   owl:hasValue \""+desc+"\"@en ].");
				out.println("");
			}
			

			int pos=line.indexOf(' ');
			String emb=line.substring(pos+1);
			
			out.println(":"+rdf.escapeEntity(data[0])+" a ontolex:LexicalEntry;");
			out.println("  ontolex:canonicalForm \""+data[0]+"\"@"+lang+";");
			out.println("  frac:embedding ["); 
			out.println("    a :"+name+";");
			out.println("	 rdf:value \""+emb+"\" ].");
			
		}
		
		in.close();
		out.close();
	}

}
