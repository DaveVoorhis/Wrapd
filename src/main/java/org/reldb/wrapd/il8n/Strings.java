package org.reldb.wrapd.il8n;

import org.reldb.toolbox.strings.Str;
import org.reldb.wrapd.compiler.ForeignCompilerJava;
import org.reldb.wrapd.data.bdbje.BDBJEBase;
import org.reldb.wrapd.data.bdbje.BDBJEData;
import org.reldb.wrapd.data.bdbje.BDBJEEnvironment;
import org.reldb.wrapd.tuples.TupleTypeGenerator;

/**
 * Internationalisable messages.
 *
 * @author dave
 */
public class Strings {

    public static final int NoteOpening = Str.N("Opening BDBJE at %s.", BDBJEEnvironment.class.toString());
    public static final int NoteOpened = Str.N("Opened BDBJE at %s.", BDBJEEnvironment.class.toString());
    public static final int ErrNotExists = Str.E("BDBJE directory %s does not exist.", BDBJEEnvironment.class.toString());
    public static final int ErrUnableToCreateDir = Str.E("Unable to create directory %s", BDBJEEnvironment.class.toString());
    public static final int NoteClosing = Str.N("Closing BDBJE at %s.", BDBJEEnvironment.class.toString());
    public static final int NoteClosed = Str.N("Closed BDBJE at %s.", BDBJEEnvironment.class.toString());
    public static final int WarnClosingClassRepo = Str.W("Error closing class repository at %s due to %s.", BDBJEEnvironment.class.toString());
    public static final int WarnClosingClassRepoEnv = Str.W("Error closing class repository environment at %s due to %s.", BDBJEEnvironment.class.toString());
    public static final int WarnClosingDataEnv = Str.W("Error closing data storage environment at %s due to %s.", BDBJEEnvironment.class.toString());

    public static final int ErrSourceExists = Str.E("Data source %s already exists.", BDBJEBase.class.toString());
    public static final int ErrUnableToRemoveExistingDb = Str.E("Unable to delete existing data source %s.", BDBJEBase.class.toString());
    public static final int ErrSourceNotExists = Str.E("Data source %s does not exist.", BDBJEBase.class.toString());
    public static final int ErrUnableToGenerateTupleType = Str.E("Unable to create tuple type %s.", BDBJEBase.class.toString());
    public static final int ErrUnableToGenerateTupleType2 = Str.E("Unable to create tuple type %s:\n%s", BDBJEBase.class.toString());
    public static final int ErrUnableToLoadTupleClass = Str.E("Unable to load tuple type class for %s.", BDBJEBase.class.toString());
    public static final int ErrDatabaseNotFound = Str.E("BDBJEBase: Possible damaged database? Berkeley database not found due to: %s", BDBJEBase.class.toString());
    public static final int ErrProblemClosingEnvironment = Str.E("Problem closing environment: %s.", BDBJEBase.class.toString());
    public static final int ErrNameAlreadyInUse = Str.E("Name '%s' is already in use.", BDBJEBase.class.toString());
    public static final int ErrUnableToRemoveCatalogEntry = Str.E("Unable to remove catalog entry '%s'", BDBJEBase.class.toString());

    public static final int ErrUnableToCreateResourceDir = Str.E("Unable to create resource directory: %s.", ForeignCompilerJava.class.toString());
    public static final int ErrUnableToCreatePackageDir = Str.E("Unable to create package directory: %s.", ForeignCompilerJava.class.toString());
    public static final int ErrSavingJavaSource = Str.E("Unable to save Java source: %s.", ForeignCompilerJava.class.toString());

    public static final int ErrUnableToCreateOrOpenCodeDirectory = Str.E("Unable to create or open code directory %s.", TupleTypeGenerator.class.toString());
    public static final int ErrAttemptToAddDuplicateAttributeName = Str.E("Attempt to add duplicate attribute %s.", TupleTypeGenerator.class.toString());
    public static final int ErrAttemptToRemoveNonexistentAttribute = Str.E("Attempt to remove non-existent attribute %s.", TupleTypeGenerator.class.toString());

    public static final int ErrUnableToExtendTupleType = Str.E("Unable to extend tuple type %s:\n%s", BDBJEData.class.toString());
    public static final int ErrUnableToLoadTupleType = Str.E("Unable to load tuple type for %s.", BDBJEData.class.toString());
    public static final int ErrUnableToLoadTupleTypeClass = Str.E("Unable to load tuple type %s.", BDBJEData.class.toString());
    public static final int ErrUnableToLocateCopyFromMethod = Str.E("Unable to load copyFrom method from tuple type %s.", BDBJEData.class.toString());
    public static final int ErrSchemaUpdateFailure = Str.E("Unable to update schema due to: %s", BDBJEData.class.toString());
    public static final int ErrSchemaUpdateCopyFromFailure = Str.E("Unable to update schema due to failure in copyFrom method: %s", BDBJEData.class.toString());
    public static final int ErrUnableToLoadTupleTypeClass2 = Str.E("Unable to load tuple type for %s.", BDBJEData.class.toString());
}
