# CLAW functional tests

This directory contains a set of functional tests for the CLAW Fortran compiler.
All tests have the same structure described below:

* `CMakeLists.txt`: `CMake` file including specififc information for the test
  case for the test (see below the format for this file).
* `original_code.f90`: The original Fortran code with CLAW directives.
* `reference.f90`: A reference Fortran code that will be compared to the
  transformed code.

##### CMakeLists.txt structure
Each test has a `CMakeLists.txt` file that set up few variables to generate
the specific targets for the test case. Here is the format of this file. Note
that only first and last line are mandatory (`TEST_NAME` and `include`)
```cmake
set(TEST_NAME <test_name>)  # test_name must be replaced by a relevant test name
                            # for the test case
set(TEST_DEBUG ON)          # optional, run clawfc with debug flag
set(OUTPUT_TEST ON)         # optional, execute executable output comparison
set(IGNORE_TEST ON)         # optional, does not perform the test but apply
                            # transformations
set(OPTIONAL_FLAGS <flags>) # pass additional flags to clawfc                           
include(${CMAKE_SOURCE_DIR}/test/base_test.cmake) # base cmake file
```

The file `base_test.cmake` is common for all tests and does the following
actions:

1. Run `clawfc` on the file `original_code.f90` and produce a new file
`transformed_code.f90`.
2. Compile `original_code.f90` and `transformed_code.f90` with a standard
Fortran compiler to check their correctness.
3. Execute a `diff` between `transformed_code.f90` and `reference.f90` to check
the correctness of the transformations applied.
4. If the `OUTPUT_TEST` option is set to `ON`, execute a `diff` between the
output of the executable `original_code` and `transformed_code`.

#### Execution
##### All test suite
The test suites can be executed from the root directory (`claw-compiler/`) with
the following command:

```bash
make clean-transformation transformation test
```

The CLAW Fortran compiler must be compiled once before executing the test.

Make sure to use the `clean-transformation` target in order to get rid of
previous code transformations. As the source files do not change, test cases
cannot rely on `make` dependency.

##### Only one test
Each test has its specific set of targets to be run individually. For example,
the test case `fusion1` located in `test/loops/fusion1` can be executed with the
following command:

```bash
cd test/loops/fusion1
make clean-fusion1 transform-fusion1 test
```

More generally, use the following command and modify `test_path`and `test_name`
accordingly.
```bash
cd <test_path>
make clean-<test_name> transform-<test_name> test
```
