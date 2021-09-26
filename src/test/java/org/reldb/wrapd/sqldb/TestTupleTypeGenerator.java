package org.reldb.wrapd.sqldb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.reldb.wrapd.TestConfiguration;
import org.reldb.wrapd.compiler.DirClassLoader;
import org.reldb.wrapd.compiler.JavaCompiler;
import org.reldb.wrapd.sqldb.TupleTypeGenerator;

public class TestTupleTypeGenerator {

	private static final String baseDir = TestConfiguration.Directory + "Tuples";

	private static String getCodeDirectory() {
		return baseDir + "/code";
	}

	@Test 
	public void testTupleGenerator01() throws Exception {
		var codeDir = getCodeDirectory();
		var tupleName = "TestTuple";

		var generator = new TupleTypeGenerator(codeDir, TestConfiguration.Package, tupleName);
		generator.destroy();
		generator.addAttribute("Col1", String.class);
		generator.addAttribute("Col2", Integer.class);
		generator.addAttribute("Col3", Boolean.class);
		generator.addAttribute("Col4", Double.class);
		var sourcef = generator.generate();
		var compiler = new JavaCompiler(codeDir);
		var classpath = compiler.getDefaultClassPath();
		var compilation = compiler.compileJavaCode(classpath, sourcef);

		System.out.println("[TEST] === Compilation " + ((compilation.compiled) ? "succeeded" : "failed") + " ===");
		System.out.println("[TEST] " + compilation.compilerMessages);

		var loader = new DirClassLoader(codeDir, TestConfiguration.Package);
		var testClass = loader.forName(generator.getTupleClassName());
		
		for (Field field: testClass.getFields())
			System.out.println("[TEST] Has field: " + field.getType() + " " + field.getName());

		// 4 defined fields plus serial number is 5
		assertEquals(5, testClass.getFields().length);
	}

}
