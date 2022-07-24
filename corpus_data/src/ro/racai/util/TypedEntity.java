package ro.racai.util;

public interface TypedEntity<TYPE,ENTITY> {
	public TYPE getType();
	public ENTITY getEntity();
	public int getCount();
	public void incCount();
	public void incCount(int n);
}
