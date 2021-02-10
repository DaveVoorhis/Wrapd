package org.reldb.wrapd.tuples;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.reldb.TestDirectory;
import org.reldb.wrapd.compiler.DirClassLoader;

public class TestTupleTypeGenerator {

	private static final String baseDir = TestDirectory.Is + "Tuples";

	private static String getCodeDirectory() {
		return baseDir + "/code";
	}

	private final static boolean verbose = true;
	
	@Test 
	public void testTupleGenerator01() throws Exception {
		var codeDir = getCodeDirectory();
		var tupleName = "TestTuple";
		var newTupleName = "TestTupleRenamed";
		var copyTupleName = "TestTupleCopied";

		TupleTypeGenerator.destroy(codeDir, tupleName);
		TupleTypeGenerator.destroy(codeDir, newTupleName);
		TupleTypeGenerator.destroy(codeDir, copyTupleName);
		
		var generator = new TupleTypeGenerator(codeDir, tupleName);
		
		generator.addAttribute("Col1", String.class);
		generator.addAttribute("Col2", Integer.class);
		generator.addAttribute("Col3", Boolean.class);
		generator.addAttribute("Col4", Double.class);
		var compilation = generator.compile();
		
		if (verbose) {
			System.out.println("[TEST] === Compilation 1 " + ((compilation.compiled) ? "succeeded" : "failed") + " ===");
			System.out.println("[TEST] " + compilation.compilerMessages);
		}
		
		var loader = new DirClassLoader(codeDir, TupleTypeGenerator.TupleTypePackage);
		var testclass = loader.forName(generator.getTupleClassName());
		
		for (Field field: testclass.getFields())
			if (verbose)
				System.out.println("[TEST] Has field: " + field.getType().toString() + " " + field.getName());
		
		assertEquals(5, testclass.getFields().length);
		
		if (verbose)
			System.out.println("[TEST]");
		
		generator = new TupleTypeGenerator(codeDir, tupleName);
		generator.addAttribute("Col5", Float.class);
		var compilation2 = generator.compile();

		if (verbose) {
			System.out.println("[TEST] === Compilation 2 " + ((compilation2.compiled) ? "succeeded" : "failed") + " ===");
			System.out.println("[TEST] " + compilation2.compilerMessages);
		}
		
		testclass = loader.forName(generator.getTupleClassName());
		for (Field field: testclass.getFields())
			if (verbose)
				System.out.println("[TEST] Has field: " + field.getType().toString() + " " + field.getName());
		
		assertEquals(6, testclass.getFields().length);
		
		if (verbose)
			System.out.println("[TEST]");
		
		generator.removeAttribute("Col4");
		generator.compile();
		
		if (verbose) {
			System.out.println("[TEST] === Compilation 3 " + ((compilation2.compiled) ? "succeeded" : "failed") + " ===");
			System.out.println("[TEST] " + compilation2.compilerMessages);
		}
		
		testclass = loader.forName(generator.getTupleClassName());
		for (Field field: testclass.getFields())
			if (verbose)
				System.out.println("[TEST] Has field: " + field.getType().toString() + " " + field.getName());
		
		assertEquals(5, testclass.getFields().length);
		
		generator.rename(newTupleName);
		generator.compile();
		
		testclass = loader.forName(generator.getTupleClassName());
		for (Field field: testclass.getFields())
			if (verbose)
				System.out.println("[TEST] Has field: " + field.getType().toString() + " " + field.getName());
		
		assertEquals(5, testclass.getFields().length);
		
		var newGenerator = generator.copyTo(copyTupleName);
		
		newGenerator.addAttribute("Col6", Integer.class);
		newGenerator.removeAttribute("Col3");
		newGenerator.compile();
		
		testclass = loader.forName(newGenerator.getTupleClassName());
		for (Field field: testclass.getFields())
			if (verbose)
				System.out.println("[TEST] Has field: " + field.getType().toString() + " " + field.getName());
		
		assertEquals(5, testclass.getFields().length);
	}

}
