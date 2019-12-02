package org.reldb.wrapd.data.bdbje;

import static org.reldb.wrapd.il8n.Strings.*;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.util.logging.Logger;

import org.reldb.toolbox.strings.Str;
import org.reldb.toolbox.utilities.Directory;

import org.reldb.wrapd.compiler.DirClassLoader;
import org.reldb.wrapd.exceptions.ExceptionFatal;
import org.reldb.wrapd.tuples.TupleTypeGenerator;

import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.collections.TransactionRunner;
import com.sleepycat.collections.TransactionWorker;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.JEVersion;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;

public class BDBJEEnvironment implements Closeable {

	private String homeDir;
	private Environment dataEnv;
	private Environment classesEnv;
	private ClassCatalog classes;
	private DirClassLoader classLoader;
	
	private static final Logger log = Logger.getLogger(BDBJEEnvironment.class.toString());

	private static String getDataDir(String homedir) {
		 return homedir + File.separator + "data";
	}

	private static String getClassDir(String homedir) {
		return homedir + File.separator + "classes";
	}
	
	private static String getCodeDir(String homedir) {
		return homedir + File.separator + "code";
	}

	private static String getClickerFileName(String homedir) {
		return homedir + File.separator + "ClickToOpen.rl";
	}
	
	private String getDataDir() {
		return getDataDir(homeDir);
	}
	
	private String getClassDir() {
		return getClassDir(homeDir);
	}
	
	public String getCodeDir() {
		return getCodeDir(homeDir);
	}
	
	private String getClickerFileName() {
		return getClickerFileName(homeDir);
	}
	
	/**
	 * Purge an existing database.
	 * 
	 * @param homedir - directory containing database to purge
	 */
	public static void purge(String homedir) {
		Directory.rmAll(getDataDir(homedir));
		Directory.rmAll(getClassDir(homedir));
		Directory.rmAll(getCodeDir(homedir));
		Directory.rmAll(getClickerFileName(homedir));
	}

	/**
	 * Return true if a database probably exists.
	 * 
	 * @param dbURL - directory of database
	 * @return - true if database appears to exist (this does not guarantee that it can be opened.)
	 */
	public static boolean exists(String dbURL) {
		return (Directory.exists(dbURL) && Directory.exists(getDataDir(dbURL)) && Directory.exists(getClassDir(dbURL)));
	}

	public String getBerkeleyJavaDBVersion() {
		return JEVersion.CURRENT_VERSION.getVersionString();
	}

	private void writeClicker() {
		FileWriter writer = null;
		try {
			writer = new FileWriter(getClickerFileName(), false);
			if (writer != null)
				writer.close();
		} catch (Exception e) {
			log.warning("WARNING: Unable to create " + getClickerFileName());
		}
	}
	
	/**
	 * Create a connection to the Berkeley DB Java Edition.
	 * 
	 * @param dir - directory to hold data
	 * @param create - true if directory can be created if it doesn't exist
	 */
	public BDBJEEnvironment(String dir, boolean create) {
		homeDir = dir;
		
		log.info(Str.ing(NoteOpening, homeDir));
		
		if (!create && !Directory.exists(homeDir))
			throw new ExceptionFatal(Str.ing(ErrNotExists, homeDir));
		
		if (!Directory.chkmkdir(homeDir)) 
			throw new ExceptionFatal(Str.ing(ErrUnableToCreate1, homeDir));	
		
		var dataDir = getDataDir();
		if (!Directory.chkmkdir(dataDir))
			throw new ExceptionFatal(Str.ing(ErrUnableToCreate2, dataDir));
		
		var classDir = getClassDir();
		if (!Directory.chkmkdir(classDir))
			throw new ExceptionFatal(Str.ing(ErrUnableToCreate3, classDir));

		var codeDir = getCodeDir();
		if (!Directory.chkmkdir(codeDir))
			throw new ExceptionFatal(Str.ing(ErrUnableToCreate4, codeDir));
		
		if (create)
			writeClicker();

		classLoader = new DirClassLoader(codeDir, TupleTypeGenerator.getTuplePackage());
		
		var dataEnvConfig = new EnvironmentConfig();
		dataEnvConfig.setTransactional(true);
		dataEnvConfig.setAllowCreate(create);
		dataEnvConfig.setClassLoader(classLoader);
		
		dataEnv = new Environment(new File(dataDir), dataEnvConfig);
		classesEnv = new Environment(new File(classDir), dataEnvConfig);
		
		var classEnvConfig = new DatabaseConfig();
		classEnvConfig.setTransactional(true);
		classEnvConfig.setAllowCreate(create);

		// Needed for serial bindings (i.e., Java serialization)
		var classesDb = classesEnv.openDatabase(null, "classes", classEnvConfig);
		classes = new StoredClassCatalog(classesDb);
		
		log.info(Str.ing(NoteOpened, homeDir));
	}
	
	/** 
	 * Begin a transaction.
	 * 
	 * @return <link>Transaction</link>
	 */
	public Transaction beginTransaction() {
		var config = new TransactionConfig();
	    config.setSerializableIsolation(true);		
		return dataEnv.beginTransaction(null, config);
	}

	/**
	 * Run a TransactionWorker in a transaction.
	 * 
	 * @param worker - TransactionWorker instance, which can be a lambda expression.
	 * @throws DatabaseException
	 * @throws Exception
	 */
	public void transaction(TransactionWorker worker) throws DatabaseException, Exception {
		var runner = new TransactionRunner(dataEnv);
		runner.run(worker);
	}
	
	/** 
	 * Delete all records from a named Berkeley DB "Database".
	 * 
	 * @param name - Database name.
	 */
	public void truncate(String name) {
		dataEnv.truncateDatabase(null, name, false);
	}

	/**
	 * Remove a specified Berkeley DB "Database".
	 * 
	 * @param name - Database name.
	 */
	public void remove(String name) {
		dataEnv.removeDatabase(null, name);
	}

	/**
	 * Rename a specified Berkeley DB "Database".
	 * 
	 * @param oldName - old name
	 * @param newName - new name
	 */
	public void rename(String oldName, String newName) {
		dataEnv.renameDatabase(null, oldName, newName);
	}
	
	/** 
	 * Open a Berkeley DB "Database", i.e., a persistent key/value store.
	 * 
	 * @param name - name of store
	 * @param create - true to create this Database if it doesn't exist
	 * @return - a new Database
	 */
	Database open(String name, boolean create) {
		var dbConfig = new DatabaseConfig();
		dbConfig.setTransactional(true);
		dbConfig.setAllowCreate(create);
		return dataEnv.openDatabase(null, name, dbConfig);
	}

	/**
	 * Close an open Berkeley DB "Database" returned by <link>open</link>.
	 * 
	 * @param database - <link>Database</link> to close.
	 */
	void close(Database database) {
		database.close();
	}

	/**
	 * Obtain the Java class catalog.
	 * 
	 * @return - <link>ClassCatalog</link>
	 */
	ClassCatalog getClassCatalog() {
		return classes;
	}

	/**
	 * Obtain the class loader.
	 * 
	 * @return the instance of DirClassLoader used by this BDBJEEnvironment.
	 */
	public DirClassLoader getClassLoader() {
		return classLoader;
	}
	
	/** Closes the database. */
	public void close() {
		if (classes != null) {
			log.info(Str.ing(NoteClosing, homeDir));
			try {
				classes.close();
			} catch (Throwable t) {
				log.warning(Str.ing(WarnClosingClassRepo, homeDir, t.toString()));
			} finally {
				classes = null;
			}
		}
		if (classesEnv != null) {
			try {
				classesEnv.close();
			} catch (Throwable t) {
				log.warning(Str.ing(WarnClosingClassRepoEnv, homeDir, t.toString()));
			} finally {
				classesEnv = null;
			}
		}
		if (dataEnv != null) {
			try {
				dataEnv.close();
			} catch (Throwable t) {
				log.warning(Str.ing(WarnClosingDataEnv, homeDir, t.toString()));
			} finally {
				dataEnv = null;
			}
			log.info(Str.ing(NoteClosed, homeDir));
		}
	}

}
