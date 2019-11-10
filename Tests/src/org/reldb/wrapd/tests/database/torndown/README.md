Run this last to perform any clean-up after running tests requiring a separate setup phase.

The teardown phase could be included in the test phase, but it's often helpful to see
temporary results, generated code, logs, etc. that are part of the testing process. Teardown should remove them.