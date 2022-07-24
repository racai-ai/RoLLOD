package ro.racai.util;

public class TypedEntityImpl<TYPE,ENTITY> implements TypedEntity<TYPE, ENTITY> {

	private TYPE type;
	private ENTITY entity;
	int count;
	
	public TypedEntityImpl() {
		this.type=null;
		this.entity=null;
		this.count=0;
	}
	
	public TypedEntityImpl(TYPE t,ENTITY e) {
		this.type=t;
		this.entity=e;
		this.count=1;
	}
	
	public TYPE getType() {
		return type;
	}
	
	public void setType(TYPE t) {
		this.type=t;
	}
	
	public ENTITY getEntity() {
		return entity;
	}
	
	public void setEntity(ENTITY e) {
		this.entity=e;
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypedEntityImpl<?, ?> other = (TypedEntityImpl<?, ?>) obj;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}
	
	public void incCount() {
		this.count++;
	}
	
	public void incCount(int n) {
		this.count+=n;
	}
}
