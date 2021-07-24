package org.reldb.wrapd.il8n;

import org.reldb.toolbox.il8n.Str;
import org.reldb.wrapd.compiler.JavaCompiler;
import org.reldb.wrapd.schema.AbstractSchema;
import org.reldb.wrapd.schema.SQLSchema;
import org.reldb.wrapd.tuples.TupleTypeGenerator;

/**
 * Internationalisable messages.
 */
public class Strings {

    public static final int ErrUnableToCreateResourceDir = Str.E("Unable to create resource directory: %s.", JavaCompiler.class.toString());
    public static final int ErrUnableToCreatePackageDir = Str.E("Unable to create package directory: %s.", JavaCompiler.class.toString());
    public static final int ErrSavingJavaSource = Str.E("Unable to save Java source: %s.", JavaCompiler.class.toString());

    public static final int ErrUnableToCreateOrOpenCodeDirectory = Str.E("Unable to create or open code directory %s.", TupleTypeGenerator.class.toString());
    public static final int ErrAttemptToAddDuplicateAttributeName = Str.E("Attempt to add duplicate attribute %s of type %s to %s.", TupleTypeGenerator.class.toString());
    public static final int ErrAttemptToRemoveNonexistentAttribute = Str.E("Attempt to remove non-existent attribute %s.", TupleTypeGenerator.class.toString());

    public static final int ErrUnableToDetermineVersion = Str.E("Unable to determine version due to: %s", AbstractSchema.class.toString());
    public static final int ErrUnrecognisedVersionType = Str.E("Unrecognised version type: %s", AbstractSchema.class.toString());
    public static final int ErrUnableToUpdateToVersion = Str.E("Unable to perform update to version %s", AbstractSchema.class.toString());
    public static final int ErrUnableToSetVersion = Str.E("Unable to set version number to %s", AbstractSchema.class.toString());
    public static final int ErrNullVersion = Str.E("Version is null.", AbstractSchema.class.toString());

    public static final int ErrVersionTableIsEmpty = Str.E("Version table %s is empty. It needs one row.", SQLSchema.class.toString());
    public static final int ErrVersionTableValueIsInvalid = Str.E("Version table %s contains an invalid value for %s.", SQLSchema.class.toString());
}
