package org.reldb.wrapd.schema;

import org.junit.jupiter.api.Test;
import org.reldb.toolbox.progress.ConsoleProgressIndicator;
import org.reldb.wrapd.response.Result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestAbstractSchema {

    @Test
    public void testAbstractSchemaNullVersion() {
        var schema = new AbstractSchema() {
            @Override
            public Version getVersion() {
                return null;
            }

            @Override
            protected Result setVersion(VersionNumber number) {
                return null;
            }

            @Override
            protected Result create() {
                return null;
            }

            @Override
            protected Update[] getUpdates() {
                return new Update[0];
            }
        };
        var result = schema.setup(new ConsoleProgressIndicator("testAbstractSchemaNullVersion: "));
        assertTrue(result.error.toString().endsWith("Version is null."));
    }

    @Test
    public void testAbstractSchemaNewDatabase() {
        var schema = new AbstractSchema() {
            private Version version = new VersionNewDatabase();

            @Override
            public Version getVersion() {
                return version;
            }

            @Override
            protected Result setVersion(VersionNumber number) {
                version = number;
                return Result.OK;
            }

            @Override
            protected Result create() {
                return Result.OK;
            }

            @Override
            protected Update[] getUpdates() {
                return new Update[0];
            }
        };
        var result = schema.setup(new ConsoleProgressIndicator("testAbstractSchemaNewDatabase: "));
        assertTrue(result.isOk());
        assertEquals(0, ((VersionNumber)schema.getVersion()).value);
    }

    @Test
    public void testAbstractSchemaNewDatabaseAnd1Update() {
        var schema = new AbstractSchema() {
            private Version version = new VersionNewDatabase();

            @Override
            public Version getVersion() {
                return version;
            }

            @Override
            protected Result setVersion(VersionNumber number) {
                version = number;
                return Result.OK;
            }

            @Override
            protected Result create() {
                return Result.OK;
            }

            int updateCounter = 0;

            public int getUpdateCounter() {
                return updateCounter;
            }

            @Override
            protected Update[] getUpdates() {
                return new Update[] {
                        // v1
                        new Update() {
                            @Override
                            public Result apply(AbstractSchema schema) {
                                updateCounter = updateCounter + 1;
                                return Result.OK;
                            }
                        },
                };
            }
        };
        var result = schema.setup(new ConsoleProgressIndicator("testAbstractSchemaNewDatabaseAnd1Update: "));
        assertTrue(result.isOk());
        assertEquals(1, schema.getUpdateCounter());
        assertEquals(1, ((VersionNumber)schema.getVersion()).value);
    }

    public static class TestSchema01 extends AbstractSchema {
        private Version version = new VersionNewDatabase();
        private int updateCounter = 0;

        public TestSchema01(Version initialVersion) {
            version = initialVersion;
        }

        @Override
        public Version getVersion() {
            return version;
        }

        @Override
        protected Result setVersion(VersionNumber number) {
            version = number;
            return Result.OK;
        }

        @Override
        protected Result create() {
            return Result.OK;
        }

        public int getUpdateCounter() {
            return updateCounter;
        }

        @Override
        protected AbstractSchema.Update[] getUpdates() {
            return new AbstractSchema.Update[] {
                    // v1
                    new AbstractSchema.Update() {
                        @Override
                        public Result apply(AbstractSchema schema) {
                            updateCounter = updateCounter + 1;
                            return Result.OK;
                        }
                    },
                    // v2
                    new AbstractSchema.Update() {
                        @Override
                        public Result apply(AbstractSchema schema) {
                            updateCounter = updateCounter + 10;
                            return Result.OK;
                        }
                    },
                    // v3
                    new AbstractSchema.Update() {
                        @Override
                        public Result apply(AbstractSchema schema) {
                            updateCounter = updateCounter + 100;
                            return Result.OK;
                        }
                    },
                    // v4
                    new AbstractSchema.Update() {
                        @Override
                        public Result apply(AbstractSchema schema) {
                            updateCounter = updateCounter + 1000;
                            return Result.OK;
                        }
                    }
            };
        }
    }

    @Test
    public void testAbstractSchemaNewDatabaseAnd4Updates() {
        TestSchema01 schema = new TestSchema01(new VersionNewDatabase());
        var result = schema.setup(new ConsoleProgressIndicator("testAbstractSchemaNewDatabaseAnd4Updates: "));
        assertTrue(result.isOk());
        assertEquals(1111, schema.getUpdateCounter());
        assertEquals(4, ((VersionNumber)schema.getVersion()).value);
    }

    @Test
    public void testAbstractSchemaExistingDatabaseAnd4UpdatesFrom1() {
        var schema = new TestSchema01(new VersionNumber(1));
        var result = schema.setup(new ConsoleProgressIndicator("testAbstractSchemaExistingDatabaseAnd4UpdatesFrom1: "));
        assertTrue(result.isOk());
        assertEquals(1110, schema.getUpdateCounter());
        assertEquals(4, ((VersionNumber)schema.getVersion()).value);
    }

    @Test
    public void testAbstractSchemaExistingDatabaseAnd4UpdatesFrom2() {
        var schema = new TestSchema01(new VersionNumber(2));
        var result = schema.setup(new ConsoleProgressIndicator("testAbstractSchemaExistingDatabaseAnd4UpdatesFrom2: "));
        assertTrue(result.isOk());
        assertEquals(1100, schema.getUpdateCounter());
        assertEquals(4, ((VersionNumber)schema.getVersion()).value);
    }

    @Test
    public void testAbstractSchemaExistingDatabaseAnd4UpdatesFrom3() {
        var schema = new TestSchema01(new VersionNumber(3));
        var result = schema.setup(new ConsoleProgressIndicator("testAbstractSchemaExistingDatabaseAnd4UpdatesFrom3: "));
        assertTrue(result.isOk());
        assertEquals(1000, schema.getUpdateCounter());
        assertEquals(4, ((VersionNumber)schema.getVersion()).value);
    }

    @Test
    public void testAbstractSchemaExistingDatabaseNoUpdateNeeded() {
        var schema = new TestSchema01(new VersionNumber(4));
        var result = schema.setup(new ConsoleProgressIndicator("testAbstractSchemaExistingDatabaseNoUpdateNeeded: "));
        assertTrue(result.isOk());
        assertEquals(0, schema.getUpdateCounter());
        assertEquals(4, ((VersionNumber)schema.getVersion()).value);
    }

}
