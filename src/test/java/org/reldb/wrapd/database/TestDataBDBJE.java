package org.reldb.wrapd.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.reldb.wrapd.data.bdbje.BDBJEBase;
import org.reldb.wrapd.data.bdbje.BDBJEData;
import org.reldb.wrapd.data.bdbje.BDBJEEnvironment;
import org.reldb.wrapd.data.CatalogEntry;

public class TestDataBDBJE {
	
	private final static boolean verbose = true;
	
	private final static String testDir = DatabaseConfigurationAndSetup.getBaseDirectory();
	
	private static BDBJEBase base;
	
	private final static String storageName1 = "TestData";
	private final static String storageName2 = "AnotherTestData";
	private final static String storageNameRenamed = "TestDataRenamed";
	
	@BeforeAll
	public static void setup() {
		BDBJEEnvironment.purge(testDir);
		base = new BDBJEBase(testDir, true);
	}
	
	private static void showContainer(String prompt, Map<? extends Serializable, ? extends Serializable> container) {
		if (verbose)
			System.out.println("[TEST] " + prompt);
		container.forEach((key, value) -> {
			var str = key + ": " + value.toString();
			if (verbose)
				System.out.println("[TEST] " + str);
		});
		if (verbose)
			System.out.println("[TEST]");
	}
	
	@SuppressWarnings("unchecked")
	@Test 
	public void testData01() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InstantiationException, InvocationTargetException {
		if (base.exists(storageName1)) {
			System.out.println("[TEST] Removing old " + storageName1);
			base.remove(storageName1);
		}
		if (base.exists(storageNameRenamed)) {
			System.out.println("[TEST] Removing old " + storageNameRenamed);
			base.remove(storageNameRenamed);
		}
		
		System.out.println("[TEST] Creating " + storageName1);
		var data = base.create(storageName1);
		
		data.extend("col1", String.class);
		data.extend("col2", Integer.class);

		/*
		 * The complexity below is needed because the new tuple type has been dynamically created in-line here. 
		 * If it has already been created elsewhere, we can simply use it in a conventional Java fashion. The code would then be:
		 * 
		 * var data = base.open("dataname");
		 * var tuple = new testData();
		 * tuple.col1 = "blah";
		 * tuple.col2 = 3;
		 * data.query(container -> container.put(Long.valueOf(1), tuple));
		 * tuple.col1 = "zot";
		 * tuple.col2 = 5;
		 * data.query(container -> container.put(Long.valueOf(2), tuple));
		 * 
		 */
		
		// get tuple type class
		final var tupleType = base.getTupleTypeOf(storageName1);
		// get tuple instance
		final var tuple = tupleType.getConstructor().newInstance();
		// initialise instance
		tupleType.getField("col1").set(tuple, "blah");
		tupleType.getField("col2").set(tuple, 3);		
		// insert instance into database
		data.query(container -> container.put(1L, tuple));
		// initialise instance to something else
		tupleType.getField("col1").set(tuple, "zot");
		tupleType.getField("col2").set(tuple, 5);
		// insert instance
		data.query(container -> container.put(2L, tuple));
		// initialise instance to something else
		tupleType.getField("col1").set(tuple, "zaz");
		tupleType.getField("col2").set(tuple, 66);
		// update instance
		data.query(container -> container.put(2L, tuple));
		
		// Iterate and display container contents
		data.access(container -> showContainer("=== Container Contents Before Schema Change (should have col1 and col2) ===", container));
		
		// change schema
		data.extend("col3", Double.class);
		data.remove("col2");
		
		// get tuple type class and instance
		final var tupleType2 = base.getTupleTypeOf(storageName1);
		final var tuple2 = tupleType2.getConstructor().newInstance();
		// insert instance into database
		tupleType2.getField("col1").set(tuple2, "blat");
		tupleType2.getField("col3").set(tuple2, 2.7);
		data.query(container -> container.put(3L, tuple2));
		// update instance in database
		tupleType2.getField("col1").set(tuple2, "zap");
		tupleType2.getField("col3").set(tuple2, -33.4);
		data.query(container -> container.put(3L, tuple2));
		
		// Iterate and display container contents
		data.access(container -> showContainer("=== Container Contents After Schema Change (should have col1 and col3) ===", container));
		
		// Rename container
		data.renameAllTo(storageNameRenamed);
		
		// Iterate and display container contents
		data.access(container -> showContainer("=== Container Contents After Schema Change (container renamed) ===", container));
		
		// get tuple type class and instance
		final var tupleType3 = base.getTupleTypeOf(storageNameRenamed);
		final var tuple3 = tupleType3.getConstructor().newInstance();
		// insert instance into database
		tupleType3.getField("col1").set(tuple3, "zip");
		tupleType3.getField("col3").set(tuple3, 44.234);
		data.query(container -> container.put(4L, tuple3));
		
		// Iterate and display container contents
		data.access(container -> showContainer("=== Container Contents After Adding a Tuple ===", container));
	}
	
	@Test
	public void testData02() {
		if (base.exists(storageName2)) {
			System.out.println("[TEST] Removing old " + storageName2);
			base.remove(storageName2);
		}
		System.out.println("[TEST] Creating " + storageName2);		
		base.create(storageName2);
	}
	
	@SuppressWarnings("unchecked")
	@AfterAll
	public static void teardown() {
		var catalog = (BDBJEData<String, CatalogEntry>)base.open(BDBJEBase.catalogName);
		catalog.access(container -> {
			assertTrue(container.containsKey(storageNameRenamed));
			assertTrue(container.containsKey(storageName2));
			assertTrue(container.containsKey(BDBJEBase.catalogName));
			showContainer("=== Catalog ===", container);
		});
	}

}
