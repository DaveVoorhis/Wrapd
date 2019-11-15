package org.reldb.wrapd.tests.database.test;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.Test;
import org.reldb.wrapd.compiler.DirClassLoader;
import org.reldb.wrapd.tuples.TupleTypeGenerator;

public class TestTupleGenerator {
	
	private final static boolean verbose = false;
	
	@Test 
	public void testTupleGenerator01() throws ClassNotFoundException, FileNotFoundException, IOException {
		var codeDir = "./test/code";
		var tupleName = "org.reldb.wrapd.tuples.generated.TestTuple";
		var newTupleName = "org.reldb.wrapd.tuples.generated.TestTupleRenamed";
		var copyTupleName = "org.reldb.wrapd.tuples.generated.TestTupleCopied";
		
		var generator = new TupleTypeGenerator(codeDir, tupleName);
		generator.addAttribute("Col1", String.class);
		generator.addAttribute("Col2", Integer.class);
		generator.addAttribute("Col3", Boolean.class);
		generator.addAttribute("Col4", Double.class);
		var compilation = generator.compile();
		
		if (verbose) {
			System.out.println("=== Compilation " + ((compilation.compiled) ? "succeeded" : "failed") + " ===");
			System.out.println(compilation.compilerMessages);
		}
		
		var loader = new DirClassLoader(codeDir);
		var testclass = loader.forName(tupleName);
		
		for (Field field: testclass.getFields())
			if (verbose)
				System.out.println("Has field: " + field.getType().toString() + " " + field.getName());
		
		assertEquals(5, testclass.getFields().length);
		
		if (verbose)
			System.out.println();
		
		generator = new TupleTypeGenerator(codeDir, tupleName);
		generator.addAttribute("Col5", Float.class);
		var compilation2 = generator.compile();

		if (verbose) {
			System.out.println("=== Compilation " + ((compilation2.compiled) ? "succeeded" : "failed") + " ===");
			System.out.println(compilation2.compilerMessages);
		}
		
		testclass = loader.forName(tupleName);
		for (Field field: testclass.getFields())
			if (verbose)
				System.out.println("Has field: " + field.getType().toString() + " " + field.getName());
		
		assertEquals(6, testclass.getFields().length);
		
		if (verbose)
			System.out.println();
		
		generator.removeAttribute("Col4");
		generator.compile();
		
		if (verbose) {
			System.out.println("=== Compilation " + ((compilation2.compiled) ? "succeeded" : "failed") + " ===");
			System.out.println(compilation2.compilerMessages);
		}
		
		testclass = loader.forName(tupleName);
		for (Field field: testclass.getFields())
			if (verbose)
				System.out.println("Has field: " + field.getType().toString() + " " + field.getName());
		
		assertEquals(5, testclass.getFields().length);
		
		generator.rename(newTupleName);
		generator.compile();
		
		testclass = loader.forName(newTupleName);
		for (Field field: testclass.getFields())
			if (verbose)
				System.out.println("Has field: " + field.getType().toString() + " " + field.getName());
		
		assertEquals(5, testclass.getFields().length);
		
		var newGenerator = generator.copyTo(copyTupleName);
		
		newGenerator.addAttribute("Col6", Integer.class);
		newGenerator.removeAttribute("Col3");
		newGenerator.compile();
		
		testclass = loader.forName(copyTupleName);
		for (Field field: testclass.getFields())
			if (verbose)
				System.out.println("Has field: " + field.getType().toString() + " " + field.getName());
		
		assertEquals(5, testclass.getFields().length);
		
		generator.destroy();
		newGenerator.destroy();
	}

}
