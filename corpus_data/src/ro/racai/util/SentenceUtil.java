package ro.racai.util;

import java.util.HashMap;
import java.util.Set;

import ro.racai.base.Sentence;
import ro.racai.base.Token;

public class SentenceUtil {

	public static Set<TypedEntity<String,String>> getEntitiesBIO(Sentence s, String wordColumnId, String entityColumnId){
		HashMap<TypedEntity<String,String>,TypedEntity<String,String>> ret=new HashMap<>(10);
		
		String currentType="";
		String currentEnt="";
		for(Token t:s.getTokens()) {
			String ann=t.getByKey(entityColumnId);
			if(ann.startsWith("B-")) {
				if(currentType.length()>0) {
					TypedEntity<String,String> e=new TypedEntityImpl<>(currentType,currentEnt);
					if(!ret.containsKey(e))ret.put(e, e);
					else ret.get(e).incCount();
				}
				
				currentType=ann.substring(2);
				currentEnt=t.getByKey(wordColumnId);
			}else if(ann.startsWith("I-") && ann.substring(2).equals(currentType)) {
				currentEnt+=" "+t.getByKey(wordColumnId);
			}else if((ann.equals("_") || ann.equals("-") || ann.equals("O")) && currentType.length()>0) {
				TypedEntity<String,String> e=new TypedEntityImpl<>(currentType,currentEnt);
				if(!ret.containsKey(e))ret.put(e, e);
				else ret.get(e).incCount();
				
				currentType="";
				currentEnt="";
			}
		}
		
		if(currentType.length()>0) {
			TypedEntity<String,String> e=new TypedEntityImpl<>(currentType,currentEnt);
			if(!ret.containsKey(e))ret.put(e, e);
			else ret.get(e).incCount();
			
		}

		return ret.keySet();
	}
	
}
