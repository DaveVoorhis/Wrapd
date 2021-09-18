package org.reldb.wrapd.il8n;

import org.reldb.toolbox.il8n.Str;
import org.reldb.wrapd.generator.JavaGenerator;
import org.reldb.wrapd.schema.AbstractSchema;
import org.reldb.wrapd.schema.SQLSchema;
import org.reldb.wrapd.tuples.TupleTypeGenerator;

/**
 * Internationalisable messages.
 */
public class Strings {

    /** Unable to create resource directory. */
    public static final int ErrUnableToCreateResourceDir = Str.E("Unable to create resource directory: %s.", JavaGenerator.class.toString());

    /** Unable to create package directory. */
    public static final int ErrUnableToCreatePackageDir = Str.E("Unable to create package directory: %s.", JavaGenerator.class.toString());

    /** Unable to write generated Java code. */
    public static final int ErrSavingJavaSource = Str.E("Unable to save Java source: %s.", JavaGenerator.class.toString());

    /** Unable to create or open directory for generated Java code and classes. */
    public static final int ErrUnableToCreateOrOpenCodeDirectory = Str.E("Unable to create or open code directory %s.", TupleTypeGenerator.class.toString());

    /** Attempt to add a Tuple attribute name that already exists. */
    public static final int ErrAttemptToAddDuplicateAttributeName = Str.E("Attempt to add duplicate attribute %s of type %s to %s.", TupleTypeGenerator.class.toString());

    /** Attempt to remove a Tuple attribute that doesn't exist. */
    public static final int ErrAttemptToRemoveNonexistentAttribute = Str.E("Attempt to remove non-existent attribute %s.", TupleTypeGenerator.class.toString());

    /** Unable to determine version. */
    public static final int ErrUnableToDetermineVersion = Str.E("Unable to determine version due to: %s", AbstractSchema.class.toString());

    /** Unrecognised version type. */
    public static final int ErrUnrecognisedVersionType = Str.E("Unrecognised version type: %s", AbstractSchema.class.toString());

    /** Unable to run an update. */
    public static final int ErrUnableToUpdateToVersion = Str.E("Unable to perform update to version %s", AbstractSchema.class.toString());

    /** Unable to update the version table with the new version. */
    public static final int ErrUnableToSetVersion = Str.E("Unable to set version number to %s", AbstractSchema.class.toString());

    /** The version value is null. */
    public static final int ErrNullVersion = Str.E("Version is null.", AbstractSchema.class.toString());

    /** The version table has now rows. */
    public static final int ErrVersionTableIsEmpty = Str.E("Version table %s is empty. It needs one row.", SQLSchema.class.toString());

    /** Version table contains an invalid value for the version number. */
    public static final int ErrVersionTableValueIsInvalid = Str.E("Version table %s contains an invalid value for %s.", SQLSchema.class.toString());
}
