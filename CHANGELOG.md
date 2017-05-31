# Change Log
All notable changes to the CLAW FORTRAN Compiler project will be documented in
this file.

## [0.4 Unreleased]
New features:
* Option `--target=<target>` or `-t=<target>` allows to choose the target for
  code transformation.
* Option `--directive=<directive_language>` or `-d=<directive_language>` allows
  to choose the accelerator directive language used for code generation.
* Transformation order is now configurable with the option `--config=`. A
  default configuration file is available in
  `<INSTALL_DIR>/etc/claw-default.xml`.

New available transformations:
* Low-level:
  * `array-transform`
  * `kcache`
  * `call`
  * `loop-hoist`
  * `if-extract`
  * `verbatim`
  * `ignore`
* High abstraction:
  * `parallelize`

Modification:
* `collapse` clause can be applied to `loop-fusion` transformation.

Architecture:
* All Java libraries now compiled with Ant.
* Execution of JUnit test cases is driven by Ant.
* Program arguments of cx2x.Cx2x is now using Common CLI.
* Preprocessor specific configurations are now stored in
  `compiler/<compiler_id>.cmake` files.
* Some transformations are implemented as driver transformations as they have
  to be performed before the parsing step.

General:
* OMNI Compiler submodule points to the official OMNI Compiler repository. The
  state of the repository is updated only when the latest changes are tested
  and validate.

## [0.1.0] - 2016-02-05
### First release
- **CX2X XcodeML library**: An abstraction of a set of the XcodeML/F
intermediate representation that let the elements easily manipulable from Java
code.
- **CX2X CLAW translator**: An XcodeML to XcodeML translator that implements the
CLAW language directive v0.1 (loop-fusion, loop-interchange, loop-extract,
remove)
- **CLAW Fortran Compiler driver**: the compiler driver that glue together all
pieces together for the full workflow.
