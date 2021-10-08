package org.reldb.wrapd.sqldb;

import org.reldb.toolbox.il8n.Str;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.exceptions.FatalException;
import org.reldb.wrapd.generator.JavaGenerator;

public class ValueOfTypeGenerator {

    public ValueOfTypeGenerator(String codeDirectory, String packageSpec, String sourceQueryName, String valueOfClassName, Class<?> type) {
        this.codeDirectory = codeDirectory;
    }

    public void generate() {
        if (!Directory.chkmkdir(dir))
            throw new FatalException(Str.ing(ErrUnableToCreateOrOpenCodeDirectory, dir));
        return new JavaGenerator(dir).generateJavaCode(queryName, packageSpec, getDefinitionSourceCode());
    }
}
