package org.reldb.wrapd.sqldb;

import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.TestConfiguration;
import org.reldb.wrapd.compiler.DirClassLoader;
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

	private final String tupleClassName;
	private final String queryClassName;
	private final String testTargetName;
	private final Replacement[] replacements;
	private final DbHelper dbHelper;
	private final String codeDir;

	public QueriesHelper(String dbpackage, String dbname, String tuplePackage) {
		dbHelper = new DbHelper(dbname);
		this.replacements = new QueriesHelper.Replacement[] {
				new QueriesHelper.Replacement("<dbpackage>", dbpackage),
				new QueriesHelper.Replacement("<db>", dbname),
				new QueriesHelper.Replacement("<tuplepackage>", tuplePackage)
		};
		var testName = "Test" + dbname;
		tupleClassName = testName + "Tuple";
		queryClassName = testName + "Query";
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
		clearDb(database, new String[] {"$$tester"});
		Directory.rmAll(getCodeDir());
	}

	private void createDatabase(Database database) throws SQLException {
		database.transact(xact -> {
			xact.updateAll("CREATE TABLE $$tester (x INTEGER, y INTEGER, PRIMARY KEY (x, y));");
			return Result.OK;
		});
	}

	private void destroyTupleClass() {
		ResultSetToTuple.destroyTuple(getCodeDir(), TestConfiguration.Package, tupleClassName);
	}

	private void createTupleClass(Database database) throws SQLException {
		database.createTupleFromQueryAll(getCodeDir(), TestConfiguration.Package, tupleClassName, "SELECT * FROM $$tester");
	}

	private void createQueryDefinitions(Database database) throws QueryDefiner.QueryDefinerException {
		(new QueryDefinitions(database, getCodeDir(), TestConfiguration.Package, queryClassName)).generate();
	}

	private void setup(Database database) throws SQLException, QueryDefiner.QueryDefinerException {
		destroyDatabase(database);
		createDatabase(database);
		destroyTupleClass();
		createTupleClass(database);
		createQueryDefinitions(database);
	}

	private Class<?> obtainTestCodeClass() throws ClassNotFoundException {
		var dirClassLoader = new DirClassLoader(getCodeDir(), TestConfiguration.Package);
		var testClassFullname = TestConfiguration.Package + "." + testTargetName;
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
		String source = Files.readString(Path.of("src/test/resources/" + testSourceName + ".java"), StandardCharsets.UTF_8);
		for (Replacement replacement: replacements)
			source = source.replace(replacement.from, replacement.to);
		var compiler = new JavaCompiler(getCodeDir());
		var classpath = compiler.getDefaultClassPath();
		return compiler.compileJavaCode(classpath, testTargetName, TestConfiguration.Package, source);
	}

	private void run() throws IOException, ClassNotFoundException {
		var result = compileTestCode();
		if (!result.compiled) {
			System.out.println(result.compilerMessages);
		}
		assertTrue(result.compiled);

		var clazz = obtainTestCodeClass();
		assertNotNull(clazz);

		var testExecutionSummary = runTestsInClass(clazz);
		testExecutionSummary.printTo(new PrintWriter(System.out));
		assertEquals(testExecutionSummary.getTotalFailureCount(), 0);
	}

	public void test(Database database) throws ClassNotFoundException, IOException, SQLException, QueryDefiner.QueryDefinerException {
		setup(database);
		run();
	}

}
