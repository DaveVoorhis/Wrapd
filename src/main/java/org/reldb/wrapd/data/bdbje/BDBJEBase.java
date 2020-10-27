package org.reldb.wrapd.data.bdbje;

import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.collections.StoredSortedMap;
import com.sleepycat.collections.TransactionWorker;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DatabaseNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reldb.toolbox.strings.Str;
import org.reldb.wrapd.compiler.DirClassLoader;
import org.reldb.wrapd.data.CatalogEntry;
import org.reldb.wrapd.data.Data;
import org.reldb.wrapd.exceptions.ExceptionFatal;
import org.reldb.wrapd.tuples.Tuple;
import org.reldb.wrapd.tuples.TupleTypeGenerator;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

import static org.reldb.wrapd.il8n.Strings.*;

public class BDBJEBase implements Closeable {

    private static Logger log = LogManager.getLogger(BDBJEBase.class.toString());

    public static final String catalogName = "sys_Catalog";

    private BDBJEEnvironment environment;
    private BDBJEData<String, CatalogEntry> catalog;

    // There can only be one BDBJEData instance per Data source name.
    private Map<String, BDBJEData<?, ?>> dataSources = new HashMap<>();

    /**
     * Open or create a database.
     *
     * @param dir    - String - full specification of the database directory.
     * @param create - boolean - if true, the database will be created if it doesn't exist.
     */
    public BDBJEBase(String dir, boolean create) {
        environment = new BDBJEEnvironment(dir, create);
        environment.open(catalogName, true).close();
        catalog = new BDBJEData<String, CatalogEntry>(this, catalogName) {
            @Override
            public boolean isExtendable() {
                return false;
            }

            @Override
            public boolean isRemovable() {
                return false;
            }

            @Override
            public boolean isRenameable() {
                return false;
            }

            @Override
            public boolean isTypeChangeable() {
                return false;
            }

            @Override
            public boolean isReadonly() {
                return true;
            }
        };
        updateCatalog(catalogName, CatalogEntry.class);
        dataSources.put(catalogName, catalog);
    }

    /**
     * Obtain the subdirectory of the database where Java code is defined.
     *
     * @return - String - full directory of source code.
     */
    public String getCodeDir() {
        return environment.getCodeDir();
    }

    /**
     * Obtain the CatalogEntry (metadata) associated with a given Data (storage) name.
     *
     * @param name
     * @return
     */
    public CatalogEntry getCatalogEntry(String name) {
        return (CatalogEntry) query(catalog, catalog -> catalog.get(name));
    }

    @SuppressWarnings("unchecked")
    void updateCatalog(String name, CatalogEntry entry) {
        query(catalog, catalog -> catalog.put(name, entry));
    }

    @SuppressWarnings("unchecked")
    void updateCatalog(String name, Class<?> tupleType) {
        query(catalog, catalog -> catalog.put(name, new CatalogEntry(name, tupleType.getCanonicalName(), null)));
    }

    void removeCatalogEntry(String name) {
        if (query(catalog, catalog -> catalog.remove(name)) == null)
            log.error(Str.ing(ErrUnableToRemoveCatalogEntry, name));
    }

    /**
     * Return true if a given Data source exists.
     *
     * @param name - name of Data source
     * @return - boolean - true if Data source exists
     */
    public boolean exists(String name) {
        return getCatalogEntry(name) != null;
    }

    /**
     * Get an automatically-generated Data source name that doesn't already exist.
     *
     * @return - name
     */
    public String getNewName() {
        int uniqueifier = 1;
        String name;
        do {
            name = "Data" + uniqueifier++;
        } while (exists(name));
        return name;
    }

    /**
     * Load a class using the classloader.
     *
     * @param name - String - name of class to be loaded.
     * @return - Class<?> - loaded Class.
     * @throws ClassNotFoundException if class is not found.
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Tuple> loadClass(String name) throws ClassNotFoundException {
        return (Class<? extends Tuple>) environment.getClassLoader().forName(name);
    }

    /**
     * Given a Data (storage) name, obtain the tuple type name associated with it.
     *
     * @param name - String - Data (storage) name
     * @return - String - name of tuple type class
     */
    public String getTupleTypeNameOf(String name) {
        var definition = getCatalogEntry(name);
        if (definition == null)
            throw new ExceptionFatal(Str.ing(ErrSourceNotExists, name));
        return definition.typeName;
    }

    /**
     * Given a Data (storage) name, obtain the tuple type associated with it.
     *
     * @param name - String - Data (storage) name
     * @return - Class<?> - class of tuple type
     * @throws ClassNotFoundException - if tuple type class not found.
     */
    public Class<?> getTupleTypeOf(String name) throws ClassNotFoundException {
        return loadClass(getTupleTypeNameOf(name));
    }

    /**
     * Create a Data source with a given name. If it exists already, throw ExceptionFatal.
     *
     * @param name - name of Data source.
     * @return - BDBJEData
     */
    public BDBJEData<?, ?> create(String name) {
        if (exists(name))
            throw new ExceptionFatal(Str.ing(ErrSourceExists, name));
        try {
            // if Berkeley Database (somehow) already exists, delete it.
            (environment.open(name, false)).close();
            environment.remove(name);
        } catch (DatabaseException de) {
        }
        // create storage (Berkeley Database)
        environment.open(name, true).close();
        // compile tuple type
        var codeDir = environment.getCodeDir();
        var tupleTypeGenerator = new TupleTypeGenerator(codeDir, name);
        var compileResult = tupleTypeGenerator.compile();
        if (!compileResult.compiled)
            throw new ExceptionFatal(Str.ing(ErrUnableToGenerateTupleType2, name, compileResult));
        Class<?> tupleType;
        try {
            tupleType = environment.getClassLoader().forName(tupleTypeGenerator.getTupleClassName());
        } catch (ClassNotFoundException e) {
            throw new ExceptionFatal(Str.ing(ErrUnableToGenerateTupleType, name));
        }
        // put it in the catalog
        updateCatalog(name, tupleType);
        var dataSource = new BDBJEData<>(this, name);
        dataSources.put(name, dataSource);
        return dataSource;
    }

    /**
     * Open a Data source with a given name. If it doesn't exist, throw ExceptionFatal.
     *
     * @param name - name of Data source.
     * @return - BDBJEData
     */
    public BDBJEData<?, ?> open(String name) {
        var dataSource = dataSources.get(name);
        if (dataSource != null)
            return dataSource;
        // make sure it can be opened
        try {
            getTupleTypeOf(name);
        } catch (ClassNotFoundException e) {
            throw new ExceptionFatal(Str.ing(ErrUnableToLoadTupleClass, name));
        }
        environment.open(name, false).close();
        dataSource = new BDBJEData<>(this, name);
        dataSources.put(name, dataSource);
        return dataSource;
    }

    /**
     * Open a Data source with a given name.
     * <p>
     * If Data source doesn't exist and <code>create</code> is false, throw an ExceptionFatal.
     * If Data source doesn't exist and <code>create</code> is true, create it.
     * If Data source exists, open it regardless of value of <code>create</code>.
     *
     * @param name   - name of Data source.
     * @param create - boolean - if true and Data source doesn't exist, create it.
     * @return
     */
    public BDBJEData<?, ?> open(String name, boolean create) {
        return (create && !exists(name)) ? create(name) : open(name);
    }

    /**
     * Open data storage and run a data access (update or retrieval) operation that returns a value.
     *
     * @param bdbjeData - BDBJEData - the data store
     * @param query     - Data.Query - the operation, typically provided via a lambda expression.
     * @return query result
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    <T> T query(BDBJEData<?, ?> bdbjeData, Data.Query<T> query) {
        var name = bdbjeData.getName();
        // TODO - eliminate the following hack by generalising how BDB keys are specified
        var keyBinding = name.equals(catalogName) ? new StringBinding() : new LongBinding();
        var valueBinding = new SerialBinding(getClassCatalog(), Tuple.class);
        try (var db = environment.open(name, false)) {
            return query.go(new StoredSortedMap<>(db, keyBinding, valueBinding, true));
        }
    }

    /**
     * Rename a Data source.
     *
     * @param oldName - old name
     * @param newName - new name
     */
    public void rename(String oldName, String newName) {
        var catalogEntry = getCatalogEntry(oldName);
        if (catalogEntry == null)
            return;
        if (getCatalogEntry(newName) != null)
            throw new ExceptionFatal(Str.ing(ErrNameAlreadyInUse, newName));
        removeCatalogEntry(oldName);
        environment.rename(oldName, newName);
        updateCatalog(newName, new CatalogEntry(newName, catalogEntry.typeName, catalogEntry.metadata));
        var dataSource = dataSources.get(oldName);
        if (dataSource != null) {
            dataSource.setName(newName);
            dataSources.remove(oldName);
            dataSources.put(newName, dataSource);
        }
    }

    /**
     * Return true if a Data source is removable.
     *
     * @param name - String - name of Data source.
     * @return - boolean - true if Data source is removable via remove(String name).
     */
    public boolean isRemovable(String name) {
        return !name.equals(catalogName);
    }

    /**
     * Remove a Data source.
     *
     * @param name - String - name of Data source.
     */
    public void remove(String name) {
        if (!isRemovable(name))
            return;
        removeCatalogEntry(name);
        try {
            environment.remove(name);
        } catch (DatabaseNotFoundException dnfe) {
            log.warn(Str.ing(ErrDatabaseNotFound, dnfe.getMessage()));
        }
        var codeDir = environment.getCodeDir();
        TupleTypeGenerator.destroy(codeDir, name);
        var dataSource = dataSources.get(name);
        if (dataSource != null)
            dataSources.remove(name);
    }

    public boolean isRenameable(String text) {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * Close the database.
     */
    public void close() {
        try {
            environment.close();
        } catch (Throwable t) {
            log.error(Str.ing(ErrProblemClosingEnvironment, t.getMessage()));
        }
    }

    public ClassCatalog getClassCatalog() {
        return environment.getClassCatalog();
    }

    public DirClassLoader getClassLoader() {
        return environment.getClassLoader();
    }

    public void transaction(TransactionWorker worker) throws DatabaseException, Exception {
        environment.transaction(worker);
    }

}
