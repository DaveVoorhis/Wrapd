package org.reldb.wrapd.data;

import org.reldb.wrapd.tuples.Tuple;

public class CatalogEntry implements Tuple {

	private static final long serialVersionUID = 1L;
	
	public final String name;
	public final String typeName;
	public final CatalogMetadata metadata;
	
	public CatalogEntry(String name, String typeName, CatalogMetadata metadata) {
		this.name = name;
		this.typeName = typeName;
		this.metadata = metadata;
	}

	public String toString() {
		return String.format("CatalogEntry(\"%s\", %s, %s)", name, typeName, (metadata != null) ? metadata.toString() : "<null>"); 
	}
	
}
