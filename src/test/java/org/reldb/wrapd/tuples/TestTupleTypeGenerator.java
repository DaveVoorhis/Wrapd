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

	@Test 
	public void testTupleGenerator01() throws Exception {
		var codeDir = getCodeDirectory();
		var tupleName = "TestTuple";

		TupleTypeGenerator.destroy(codeDir, tupleName);

		var generator = new TupleTypeGenerator(codeDir, tupleName);
		
		generator.addAttribute("Col1", String.class);
		generator.addAttribute("Col2", Integer.class);
		generator.addAttribute("Col3", Boolean.class);
		generator.addAttribute("Col4", Double.class);
		var compilation = generator.compile();
		
		System.out.println("[TEST] === Compilation " + ((compilation.compiled) ? "succeeded" : "failed") + " ===");
		System.out.println("[TEST] " + compilation.compilerMessages);

		var loader = new DirClassLoader(codeDir, TupleTypeGenerator.TupleTypePackage);
		var testclass = loader.forName(generator.getTupleClassName());
		
		for (Field field: testclass.getFields())
			System.out.println("[TEST] Has field: " + field.getType().toString() + " " + field.getName());

		// 4 defined fields plus serial number is 5
		assertEquals(5, testclass.getFields().length);
	}

}
