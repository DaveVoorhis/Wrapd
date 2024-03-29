package org.reldb.wrapd.sqldb;

import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.compiler.DirClassLoader;
import org.reldb.wrapd.generator.JavaGenerator;
import org.reldb.wrapd.compiler.JavaCompiler;
import org.reldb.wrapd.exceptions.FatalException;
import org.reldb.wrapd.response.Result;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.reldb.wrapd.sqldb.DbHelper.clearDb;

public class QueriesHelper {

	private final static String testSourceName = "Test_Source01";

	private static class Replacement {
		public final String from;
		public final String to;
		public Replacement(String from, String to) {
			this.from = from;
			this.to = to;
		}
	}

	private final String tuplePackage;
	private final String testTargetName;
	private final Replacement[] replacements;
	private final DbHelper dbHelper;
	private final String codeDir;

	public QueriesHelper(String dbpackage, String dbname, String tuplePackage) {
		this.tuplePackage = tuplePackage;
		dbHelper = new DbHelper(dbname);
		this.replacements = new QueriesHelper.Replacement[] {
				new QueriesHelper.Replacement("<dbpackage>", dbpackage),
				new QueriesHelper.Replacement("<db>", dbname),
				new QueriesHelper.Replacement("<tuplepackage>", tuplePackage)
		};
		var testName = "Test" + dbname;
		testTargetName = "Test" + dbname + "_Source01";
		codeDir = dbHelper.getBaseDir() + "/code";
		ensureTestDirectoryExists();
	}

	private String getCodeDir() {
		return codeDir;
	}

	private void ensureTestDirectoryExists() {
		if (!Directory.chkmkdir(dbHelper.getBaseDir()))
			throw new FatalException("Helper: Unable to create directory for test: " + dbHelper.getBaseDir());
	}

	private void destroyDatabase(Database database) {
		clearDb(database, new String[] {
				"$$xyz",
				"$$abc"
		});
		Directory.rmAll(getCodeDir());
	}

	private void createDatabase(Database database) throws SQLException {
		database.transact(xact -> {
			xact.updateAll("CREATE TABLE $$xyz (x INTEGER, y INTEGER, z VARCHAR(20), PRIMARY KEY (x));");
			xact.updateAll("CREATE TABLE $$abc (a INTEGER, b INTEGER, c VARCHAR(40), PRIMARY KEY (a));");
			return Result.OK;
		});
	}

	private void createQueryDefinitions(Database database) throws Throwable {
		(new QueryDefinitions(database, getCodeDir(), tuplePackage)).generate();
	}

	private void setup(Database database) throws Throwable {
		destroyDatabase(database);
		createDatabase(database);
		createQueryDefinitions(database);
	}

	private Class<?> obtainTestCodeClass() throws ClassNotFoundException {
		var dirClassLoader = new DirClassLoader(getCodeDir(), tuplePackage);
		var testClassFullname = tuplePackage + "." + testTargetName;
		return dirClassLoader.forName(testClassFullname);
	}

	private TestExecutionSummary runTestsInClass(Class<?> clazz) {
		var listener = new SummaryGeneratingListener();
		var request = LauncherDiscoveryRequestBuilder.request()
				.selectors(selectClass(clazz))
				.build();
		var launcher = LauncherFactory.create();
		launcher.registerTestExecutionListeners(listener);
		launcher.execute(request);
		return listener.getSummary();
	}

	private JavaCompiler.CompilationResults compileTestCode() throws IOException {
		var source = Files.readString(Path.of("src/test/resources/" + testSourceName + ".java"), StandardCharsets.UTF_8);
		for (var replacement: replacements)
			source = source.replace(replacement.from, replacement.to);
		var generator = new JavaGenerator(getCodeDir());
		var sourcef = generator.generateJavaCode(testTargetName, tuplePackage, source);
		var compiler = new JavaCompiler(getCodeDir());
		var classpath = compiler.getDefaultClassPath();
		return compiler.compileJavaCode(classpath, sourcef);
	}

	private void banner(String prompt, String character) {
		var repetition = 132 - (prompt.length() + 2);
		var barSide = character.repeat(repetition / 2);
		System.out.println(barSide + ' ' + prompt + ' ' + barSide);
	}

	private void banner(String prompt) {
		banner(prompt, "-");
	}

	private void banner() {
		banner("", "=");
	}

	private void run() throws IOException, ClassNotFoundException {
		banner("Compiling Test Code");
		var result = compileTestCode();
		if (!result.compiled) {
			System.out.println(result.compilerMessages);
		}
		assertTrue(result.compiled);

		var clazz = obtainTestCodeClass();
		assertNotNull(clazz);

		banner("Running Tests");
		var testExecutionSummary = runTestsInClass(clazz);
		banner("Test Errors");
		testExecutionSummary.printFailuresTo(new PrintWriter(System.err));
		banner("Test Summary");
		testExecutionSummary.printTo(new PrintWriter(System.out));
		banner("Test Conclusion");
		assertEquals(0, testExecutionSummary.getTotalFailureCount(), "Failures");
	}

	public void test(Database database) throws Throwable {
		banner("Setup", "=");
		setup(database);
		banner("Run", "=");
		run();
		banner("ALL TESTS PASS", "!");
	}

}
