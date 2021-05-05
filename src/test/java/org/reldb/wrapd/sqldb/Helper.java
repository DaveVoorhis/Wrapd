package org.reldb.wrapd.sqldb;

import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.compiler.DirClassLoader;
import org.reldb.wrapd.compiler.ForeignCompilerJava;
import org.reldb.wrapd.exceptions.ExceptionFatal;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class Helper {

	private final String baseDir;
	private final String codeDir;
	private final String prompt;
	private final String tupleClassName;
	private final String queryClassName;
	private final String testPackage;
	private final String testSourceName;

	public Helper(String baseDir,
				  String prompt,
				  String testName,
				  String testPackage,
				  String testSourceName) {
		this.baseDir = baseDir;
		this.prompt = prompt;
		this.testPackage = testPackage;
		this.testSourceName = testSourceName;
		tupleClassName = testName + "Tuple";
		queryClassName = testName + "Query";
		codeDir = baseDir + "/code";
		ensureTestDirectoryExists();
	}

	private void ensureTestDirectoryExists() {
		if (!Directory.chkmkdir(baseDir))
			throw new ExceptionFatal("Helper: Unable to create directory for test: " + baseDir);
	}

	private void dropTable(Database database, String tableName) {
		try {
			database.updateAll("DROP TABLE " + tableName);
		} catch (SQLException se) {
			System.out.println(prompt + " ERROR: " + se);
		}
	}

	private void destroyDatabase(Database database) {
		dropTable(database,"$$version");
		dropTable(database,"$$tester");
		Directory.rmAll(getCodeDirectory());
	}

	private void createDatabase(Database database) throws SQLException {
		database.transact(xact -> {
			xact.updateAll("CREATE TABLE $$version (user_db_version INTEGER, framework_db_version INTEGER);");
			xact.updateAll("INSERT INTO $$version VALUES (0, 0);");
			xact.updateAll("CREATE TABLE $$tester (x INTEGER, y INTEGER, PRIMARY KEY (x, y));");
			return true;
		});
	}

	private void destroyTupleClass() {
		ResultSetToTuple.destroyTuple(getCodeDirectory(), tupleClassName);
	}

	private void createTupleClass(Database database) throws SQLException {
		database.createTupleFromQueryAll(getCodeDirectory(), tupleClassName, "SELECT * FROM $$tester");
	}

	private void createQueryDefinitions(Database database) throws QueryDefiner.QueryDefinerException {
		(new QueryDefinitions(database, getCodeDirectory(), queryClassName)).generate();
	}

	private void setup(Database database) throws SQLException, QueryDefiner.QueryDefinerException {
		destroyDatabase(database);
		createDatabase(database);
		destroyTupleClass();
		createTupleClass(database);
		createQueryDefinitions(database);
	}

	private Class<?> obtainTestCodeClass() throws ClassNotFoundException {
		var dirClassLoader = new DirClassLoader(getCodeDirectory(), testPackage);
		var testClassFullname = testPackage + "." + testSourceName;
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

	private ForeignCompilerJava.CompilationResults compileTestCode() throws IOException {
		String source = Files.readString(Path.of("src/test/resources/" + testSourceName + ".java"), StandardCharsets.UTF_8);
		var compiler = new ForeignCompilerJava(getCodeDirectory());
		var classpath = compiler.getDefaultClassPath();
		return compiler.compileForeignCode(classpath, testSourceName, testPackage, source);
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

	public String getCodeDirectory() {
		return codeDir;
	}

	public void test(Database database) throws ClassNotFoundException, IOException, SQLException, QueryDefiner.QueryDefinerException {
		setup(database);
		run();
	}
}
