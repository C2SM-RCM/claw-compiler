# Std option for gfortran
set(FPPFLAGS "-E -cpp")         # for preprocessing only
set(FPP_REDIRECT true)          # use redirection > to save file
set(CLAW_TEST_FFP_FLAGS "-cpp") # force preprocessing
set(TEST_BASE_FLAGS "")         # Base flags for test case compilation
set(OPENACC_FLAGS "")           # flags to compile with OpenACC support
set(OPENMP_FLAGS "-fopenmp")    # flags to compile with OpenMP support
set(COMPILER_MACRO "-D_GNU")    # predefined macro by compiler
