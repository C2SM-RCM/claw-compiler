# CLAW Fortran Compiler development

Useful information about the development of the CLAW Fortran Compiler are given
on this page.


### Testing

#### Unit test with JUnit
Unit tests are set up using JUnit. Unit tests are built together with the
compiler if the corresponding JARs Dependencies are available.

JARs dependencies:

*  `junit-4.2.jar`
*  `hamcrest-core-1.3.jar`

#### Transformation tests
Another category of test is focusing on the the correct application of each
transformation by the translator.

Those tests can be found under `/test/`

To build all the tests, use the following command. All the tests are transformed
with the CLAW Fortran Compiler and then the original code as well as the
transformed are compiled with a standard Fortran compiler.

```bash
make test-suite
```

This target is a combination of three independent targets:

```bash
make clean-transformation transformation test
```


### Driver

The driver `clawfc` is driving the transformation process. This process is
defined as follows:

*Fortran Code* -> **Preprocessor(1)** -> *Fortran Code* -> **F_Front (2)** ->
*XcodeML Code* -> **Cx2x(3)** -> *XcodeML Code* -> **omx2f(4)** -> *Fortran Code*

###### Transformation process
1. The Fortran code is passed into the preprocessor with the corresponding
flags.
2. The fortran without preprocessing macros is passed into the OMNI Compiler
front-end and produce an intermediate file containing the XcodeML intermediate
representation of the Fortran code.
3. The XcodeML intermediate representation containing the dedicated language
directive is translated. The output is a translated XcodeML intermediate
representation.
4. The XcodeML intermediate representation is passed through the OMNI compiler
back-end to produce standard Fortran code.  

###### Executables involved in the transformation process
* **Preprocessor**: preprocessor from the standard compiler available.
* **F_Front**: OMNI Compiler front-end. It converts Fortran code in XcodeML
intermediate representation.
* **Cx2x**: Dedicated claw directives translator. It translates XcodeML with
directives into a transformed XcodeML intermediate representation.
* **omx2f**: OMNI Compiler back-end. It converts XcodeML intermediate
representation into Fortran code.
