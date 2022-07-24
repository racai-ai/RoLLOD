package llod.converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

import ro.racai.base.Sentence;
import ro.racai.base.Token;
import ro.racai.brat.Annotation;
import ro.racai.brat.AnnotationReader;
import ro.racai.brat.MalformedAnnotationException;
import ro.racai.conllup.CONLLUPReader;
import ro.racai.util.Counter;

public class ConvertCONLLUPRTASC {
	
	private static HashMap<String,String> sent_to_id=new HashMap<>(5000);
	
	private static void generateConllu(Token tok, String column, PrintWriter out) {
		generateConllu(tok,column,out,null);
	}
	
	private static String escapeString(String s) {
		s=s.replaceAll("\\\\", "\\\\\\\\");
		s=s.replaceAll("\"", "\\\\\"");
		return s;
	}
	
	private static void generateConllu(Token tok, String column, PrintWriter out, String rdfname) {
		String data=tok.getByKey(column);
		if(data==null || data.isEmpty())return ;
		if(!column.equalsIgnoreCase("FORM") && !column.equalsIgnoreCase("LEMMA") && data.equals("_"))return ;
		String rname=column;
		if(rdfname!=null)rname=rdfname;
		data=escapeString(data);
		out.println("    conllu:"+rname+" \""+data+"\" ;");
	}
	
	/*private static void generateConllup(Token tok, String column, PrintWriter out, String rdfname) {
		String data=tok.getByKey(column);
		if(data==null || data.isEmpty())return ;
		if(data.equals("_"))return ;
		String rname=column;
		if(rdfname!=null)rname=rdfname;
		data=escapeString(data);
		out.println("    conllup:"+rname+" \""+data+"\" ;");
	}*/

	public static void generateToken(Token tok, Counter tid, Sentence sent, Counter generatedSid, String sid, Counter docid, PrintWriter out) {
		//System.out.println("generateToken");
		out.println(":"+sid+"_"+tid+" a nif:Word, powla:Node ;");
		if(tid.getC()<sent.getNumTokens())
			out.println("    nif:nextWord :"+sid+"_"+(tid.getC()+1)+" ;");
		if(tid.getC()>1)
			out.println("    nif:previousWord :"+sid+"_"+(tid.getC()-1)+" ;");
		
		out.println("    powla:string \""+escapeString(tok.getByKey("FORM"))+"\" ;");
		out.println("    nif:anchorOf \""+escapeString(tok.getByKey("FORM"))+"\" ;");
		
		generateConllu(tok,"ID",out);
		generateConllu(tok,"FORM",out);
		generateConllu(tok,"LEMMA",out);
		generateConllu(tok,"UPOS",out);
		generateConllu(tok,"XPOS",out);
		generateConllu(tok,"FEATS",out);
		generateConllu(tok,"HEAD",out);
		generateConllu(tok,"DEPREL",out);
		generateConllu(tok,"DEPS",out);
		generateConllu(tok,"MISC",out);
		
		//generateConllup(tok,"RELATE:NE",out,"NE");
		
		/*String gn=tok.getByKey("RELATE:GEONAMES");
		if(gn!=null && !gn.contentEquals("_"))
			out.println("    gn:Feature "+gn+" ;");*/
		
		out.println("    nif:sentence :"+sid+" ;");
		out.println("    powla:hasParent :"+sid+" ;");
		if(tid.getC()<sent.getNumTokens())
			out.println("    powla:next :"+sid+"_"+(tid.getC()+1)+" ;");
		if(tid.getC()>1)
			out.println("    powla:previous :"+sid+"_"+(tid.getC()-1)+" ;");
		out.println("    powla:hasLayer :d"+docid.getC()+"_l_tok .");
		
		out.println();
		
	}
	
	public static String getSentenceId(Sentence sent, Counter generatedSid,Counter docid) {
		HashMap<String,String> smeta=sent.getMetadata();
		String sid="d"+docid.getC()+"_s"+generatedSid;
		if(smeta.containsKey("sent_id")) {
			sid="d"+docid.getC()+"_s"+smeta.get("sent_id");
		}
		return sid;
	}
	
	public static String generateSentence(Sentence sent, Counter generatedSid, Counter docid, PrintWriter out) {
		//System.out.println("generateSentence");
		if(sent.getNumTokens()<1)return null;
		
		String sid=getSentenceId(sent,generatedSid,docid);
		
		out.println(":"+sid+" a nif:Sentence, powla:Node ;");
		out.println("    nif:firstWord :"+sid+"_1 ;");
		out.println("    nif:lastWord :"+sid+"_"+sent.getNumTokens()+" ;");
		int tid=0;
		for(tid=0;tid<sent.getNumTokens();tid++) {
			out.println("    nif:word :"+sid+"_"+tid+" ;");
		}
		out.println("    powla:hasLayer :d"+docid.getC()+"_l_sent .");
		out.println();

		Counter tidc=new Counter();
		for(Token tok:sent.getTokens()) {
			tidc.inc();
			generateToken(tok, tidc, sent, generatedSid, sid, docid, out);
		}
		
		return sid;
	}
	
	public static void generateDocument(String input, Counter docid, String dname, PrintWriter out) throws IOException {
		//System.out.println("generateDocument");
		
		sent_to_id.put(dname,":d"+docid.getC());
		
		out.println(":d"+docid.getC()+" a powla:Document, ma:DataTrack ;");
		out.println("    powla:documentID \""+dname+"\" ;");
		out.println("    powla:hasSuperDocument :c1 ;");
		out.println("    ma:hasLanguage [ rdfs:label \"ro\" ] .");		
		out.println();
		out.println(":c1 powla:hasSubDocument :d"+docid.getC()+" .");
		out.println();
		out.println(":d"+docid.getC()+"_l_sent a powla:DocumentLayer ;");
		out.println("    powla:hasDocument :d"+docid.getC()+" .");
		out.println();
		out.println(":d"+docid.getC()+"_l_tok a powla:DocumentLayer ;");
		out.println("    powla:hasDocument :d"+docid.getC()+" .");
		out.println();
		/*out.println(":d"+docid.getC()+"_l_ner a powla:DocumentLayer ;");
		out.println("    powla:hasDocument :d"+docid.getC()+" .");
		out.println();*/
		
		CONLLUPReader in=new CONLLUPReader(Paths.get(input));

		Counter generatedSid=new Counter();
		String prevsid=null;
		for(Sentence sent=in.readSentence();sent!=null;sent=in.readSentence()) {
			generatedSid.inc();
			String csid=generateSentence(sent,generatedSid,docid,out);
			if(csid!=null) {
				if(prevsid!=null) {
					out.println(":"+prevsid+" nif:nextSentence :"+csid+" .");
					out.println(":"+csid+" nif:previousSentence :"+prevsid+" .");
					out.println(":"+prevsid+" powla:next :"+csid+" .");
					out.println(":"+csid+" powla:previous :"+prevsid+" .");
					out.println();
				}
				prevsid=csid;
			}
			
			
		}
		
		in.close();
		
	}

	public static void generateAudio(String input, Counter docid, String dname, PrintWriter out) throws IOException {
		//System.out.println("generateDocument");
		
		String rname=dname.replaceAll("[.]", "_");
		String text_name=dname.substring(0,dname.lastIndexOf('_'));
		String spk=dname.substring(0,dname.lastIndexOf("."));
		spk=spk.substring(spk.lastIndexOf("_")+1);
		
		out.println(":"+rname+" a ma:MediaResource ;");
		out.println("    ma:hasTrack :"+rname+"_audio ;");
		out.println("    ma:hasSubtitling "+sent_to_id.get(text_name)+" .");
		out.println("");
		
		out.println(":"+rname+"_audio a ma:AudioTrack ;");
		out.println("    ma:hasLanguage [ rdfs:label \"ro\" ] ;");
		out.println("    ma:hasFormat \"audio/wav\" ;");
		out.println("    ma:samplingRate \"44.1\" ;");
		out.println("    ma:hasContributor :speaker_"+spk+" ;");
		out.println("    ma:hasFragment <"+dname+"> .");
		out.println("");
		
	}
	
	public static HashMap<String,String> loadGeoAnn(String input){
		HashMap<String,String> ret=new HashMap<>(100);
		
		try {
			AnnotationReader in=new AnnotationReader(Paths.get(input));
						
			while(true) {
				Annotation ann=null;
				try {
					ann=in.getNextAnnotation();
				}catch(MalformedAnnotationException ex) {continue;}
				
				if(ann==null)break;
				
				String pos=ann.getStart()+"_"+ann.getEnd();
				String id=ann.getType();
				ret.put(pos,id);
			}
			
			in.close();
		}catch(IOException ex) {;}
		
		return ret;
	}
	
	
	public static void generateCorpus(String folder, PrintWriter out,String corpusName,String distName,String version) throws IOException {
		LocalDateTime myDateObj = LocalDateTime.now();
	    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    DateTimeFormatter myFormatYear = DateTimeFormatter.ofPattern("yyyy");
	    String formattedDate = myDateObj.format(myFormatObj);
	    String year=myDateObj.format(myFormatYear);
	    
		out.println(":c1 a powla:Corpus, dcat:Dataset, prov:Entity ;");
		out.println("    dct:title \""+corpusName+"\" ;");
		out.println("    dcat:keyword \"annotated corpus\", \"linguistic linked open data\" ;");
		out.println("    dct:issued \""+formattedDate+"\"^^xsd:date ;");
		out.println("    dcat:contactPoint <http://www.racai.ro> ;");
		out.println("    dct:temporal <http://reference.data.gov.uk/id/year/"+year+"> ;");
		out.println("    dct:spatial <http://sws.geonames.org/798549> ;");
		out.println("    dct:language <http://id.loc.gov/vocabulary/iso639-1/ro> ;");
		out.println("    dct:accrualPeriodicity <http://purl.org/linked-data/sdmx/2009/code#freq-A> ;");
		out.println("    dcat:theme :speech ;");
		out.println("    dcat:distribution :"+distName+" ;");
		out.println("    dct:publisher :racai ;");
		out.println("    dct:creator :vasile, :radu, :verginica, :elena, :maria, :andrei ;");
		out.println("    owl:versionInfo \""+version+"\" ;");
		out.println("    pav:version \""+version+"\" ;");
		out.println("    powla:documentID \""+corpusName+"\" .");
		out.println();
		
		out.println(":speech");
		out.println("    a skos:Concept ;");
		out.println("    skos:inScheme :themes ;");
		out.println("    skos:prefLabel \"Read Speech Corpus\"@en ;");
		out.println("    skos:prefLabel \"Corpus audio citit\"@ro .");
		out.println();
		
		out.println(":"+distName);
		out.println("    a dcat:Distribution ;");
		out.println("    dct:title \"ZIP distribution of the dataset\" ;");
		out.println("    dct:description \"ZIP distribution of the dataset\" ;");
		out.println("    dct:license <https://creativecommons.org/licenses/by-nc-nd/4.0/> ;");
		out.println("    dcat:mediaType \"application/zip\" .");
		out.println();

		out.println(":racai");
		out.println("    a foaf:Organization, prov:Agent ;");
		out.println("    foaf:name \"Research Institute for Artificial Intelligence 'Mihai Drăgănescu', Romanian Academy\" .");
		out.println();

		out.println(":vasile");
		out.println("    a foaf:Person, prov:Agent ;");
		out.println("    foaf:givenName \"Vasile Păiș\" ;");
		out.println("    foaf:mbox <vasile@racai.ro> ;");
		out.println("    prov:actedOnBehalfOf :racai .");
		out.println();
		
		out.println(":radu");
		out.println("    a foaf:Person, prov:Agent ;");
		out.println("    foaf:givenName \"Radu Ion\" ;");
		out.println("    foaf:mbox <radu@racai.ro> ;");
		out.println("    prov:actedOnBehalfOf :racai .");
		out.println();

		out.println(":verginica");
		out.println("    a foaf:Person, prov:Agent ;");
		out.println("    foaf:givenName \"Verginica Barbu Mititelu\" ;");
		out.println("    foaf:mbox <vergi@racai.ro> ;");
		out.println("    prov:actedOnBehalfOf :racai .");
		out.println();

		out.println(":elena");
		out.println("    a foaf:Person, prov:Agent ;");
		out.println("    foaf:givenName \"Elena Irimia\" ;");
		out.println("    foaf:mbox <elena@racai.ro> ;");
		out.println("    prov:actedOnBehalfOf :racai .");
		out.println();
		
		out.println(":maria");
		out.println("    a foaf:Person, prov:Agent ;");
		out.println("    foaf:givenName \"Maria Mitrofan\" ;");
		out.println("    foaf:mbox <maria@racai.ro> ;");
		out.println("    prov:actedOnBehalfOf :racai .");
		out.println();
		
		out.println(":andrei");
		out.println("    a foaf:Person, prov:Agent ;");
		out.println("    foaf:givenName \"Andrei Avram\" ;");
		out.println("    foaf:mbox <andrei.avram@racai.ro> ;");
		out.println("    prov:actedOnBehalfOf :racai .");
		out.println();
		
		out.println(":speaker_1");
		out.println("    a ma:Person, foaf:Person, studio:Device ;");
		out.println("    foaf:gender \"m\" ;");
		out.println("    studio:microphone \"Realtek HD Audio/Speedlink SL-8703-BK\" .");
		out.println("");

		out.println(":speaker_2");
		out.println("    a ma:Person, foaf:Person, studio:Device ;");
		out.println("    foaf:gender \"m\" ;");
		out.println("    studio:microphone \"Samsung phone headphones\" .");
		out.println("");

		out.println(":speaker_3");
		out.println("    a ma:Person, foaf:Person, studio:Device ;");
		out.println("    foaf:gender \"m\" ;");
		out.println("    studio:microphone \"Laptop microphone\" .");
		out.println("");

		out.println(":speaker_4");
		out.println("    a ma:Person, foaf:Person, studio:Device ;");
		out.println("    foaf:gender \"f\" ;");
		out.println("    studio:microphone \"Samsung phone headphones\" .");
		out.println("");

		out.println(":speaker_5");
		out.println("    a ma:Person, foaf:Person, studio:Device ;");
		out.println("    foaf:gender \"f\" ;");
		out.println("    studio:microphone \"LG phone headphones\" .");
		out.println("");

		out.println(":speaker_6");
		out.println("    a ma:Person, foaf:Person, studio:Device ;");
		out.println("    foaf:gender \"f\" ;");
		out.println("    studio:microphone \"Lenovo laptop + headphones\" .");
		out.println("");
		

		File path = Paths.get(folder,"annotated").toFile();
		
		Counter docId=new Counter();
		
	    File [] files = path.listFiles();
	    for (int i = 0; i < files.length; i++){
	        if (files[i].isFile() && files[i].getName().endsWith(".conllup") || files[i].isFile() && files[i].getName().endsWith(".conllu")){
	        	docId.inc();
	    		System.out.println("   Processing document ["+(i+1)+"/"+files.length+"] ["+files[i].getName()+"]");
	            String dname=files[i].getName();
	            dname=dname.substring(0, dname.lastIndexOf('.'));
	    		generateDocument(files[i].getPath(),docId,dname,out);
	        }
	    }		

		path = Paths.get(folder,"audio").toFile();
		docId=new Counter();
	    files = path.listFiles();
	    for (int i = 0; i < files.length; i++){
	        if (files[i].isFile() && files[i].getName().endsWith(".wav")){
	        	docId.inc();
	    		System.out.println("   Processing audio ["+(i+1)+"/"+files.length+"] ["+files[i].getName()+"]");
	            String dname=files[i].getName();
	            //dname=dname.substring(0, dname.lastIndexOf('.'));
	    		generateAudio(files[i].getPath(),docId,dname,out);
	        }
	    }		
	
	
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {

		if(args.length!=6) {
			System.out.println("Usage:");
			System.out.println("   ConvertCONLLUP INPUT OUTPUT URI");
			System.out.println("      INPUT = input Corpus folder containing subfolders \"annotated\" and \"audio\"");
			System.out.println("      OUTPUT = output rdf file (.ttl)");
			System.out.println("      URI = corresponding to the corpus");
			System.out.println("      CORPUSNAME = corpus name");
			System.out.println("      DISTRIBUTION_NAME = distribution name (can be the same as output or a zip name)");
			System.out.println("      VERSION = corpus version number to be included");
			System.out.println("  If OUTPUT ends with .gz the file will be considered as gzipped");
			System.exit(-1);
		}
		
		String input=args[0];
		String output=args[1];
		String uri=args[2];
		String corpusName=args[3];
		String distName=args[4];
		String version=args[5];

		System.out.println("Processing corpus ["+input+"] -> ["+output+"]");
		PrintWriter out=null;
		if(output.endsWith(".gz")) {
			out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(output)),Charset.forName("UTF-8"))));
		}else {
			out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),Charset.forName("UTF-8"))));
		}
		
		out.println("@prefix :        <"+uri+"> .");
		out.println("@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");
		out.println("@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .");
		out.println("@prefix nif:     <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .");
		out.println("@prefix powla:   <http://purl.org/powla/powla.owl#> .");
		out.println("@prefix conllu:  <https://universaldependencies.org/format.html#> .");
		out.println("@prefix dcat:    <http://www.w3.org/ns/dcat#> .");
		out.println("@prefix dct:     <http://purl.org/dc/terms/> .");
		out.println("@prefix skos:    <http://www.w3.org/2004/02/skos/core#> .");
		out.println("@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .");
		out.println("@prefix prov:    <http://www.w3.org/ns/prov#> .");
		out.println("@prefix foaf:    <http://xmlns.com/foaf/0.1/> .");
		out.println("@prefix owl:     <http://www.w3.org/2002/07/owl#> .");
		out.println("@prefix pav:     <http://pav-ontology.github.io/pav/> .");
		out.println("@prefix ma:      <http://www.w3.org/ns/ma-ont#> .");
		out.println("@prefix studio:      <http://isophonics.net/content/studio-ontology> .");
		out.println();
		
		generateCorpus(input,out,corpusName,distName,version);
		
		out.close();
	}
	
}
