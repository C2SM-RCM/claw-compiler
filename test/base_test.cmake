set (ORIGINAL_FILE original_code.f90)
set (XCODEML_FILE original_code.f90.xcodeml)
set (XCODEML_CLAW_FILE original_code.f90.xcodeml.claw)
set (OUTPUT_FILE transformed_code.f90)
set (REFERENCE_FILE reference.f90)

set (EXECUTABLE_ORIGINAL original_code_${TEST_NAME})
set (EXECUTABLE_TRANSFORMED transformed_code_${TEST_NAME})


# Create intermediate representation in XcodeML Fortran format
add_custom_command(
  OUTPUT  ${XCODEML_FILE}
  COMMAND ${OMNI_F_FRONTEND} -o ${XCODEML_FILE} ${ORIGINAL_FILE}
  DEPENDS ${ORIGINAL_FILE}
  COMMENT "Generating XcodeML file")

# Manipulate the intermediate representation and output XcodeML Fortran
add_custom_command(
  OUTPUT  ${XCODEML_CLAW_FILE}
  COMMAND java -cp ${OMNIJAR_INCLUDE} ${OMNI_X2X} -xcodeml ${TRANSLATOR_OPTION} -o ${XCODEML_CLAW_FILE} ${XCODEML_FILE}
  DEPENDS ${XCODEML_FILE}
  COMMENT "Apply XcodeML transformation")

# Decompile XcodeML Fortran to standard Fortran
add_custom_command(
  OUTPUT  ${OUTPUT_FILE}
  COMMAND java -cp ${OMNIJAR_INCLUDE} ${OMNI_F_BACKEND} -l -w 80 -o ${OUTPUT_FILE} ${XCODEML_CLAW_FILE}
  DEPENDS ${XCODEML_CLAW_FILE}
  COMMENT "Decompile XcodeML to Fortran")

add_custom_target(transform-${TEST_NAME} ALL
  DEPENDS ${OUTPUT_FILE})


add_executable (${EXECUTABLE_ORIGINAL} ${ORIGINAL_FILE})
add_executable (${EXECUTABLE_TRANSFORMED} ${OUTPUT_FILE})


add_test(NAME ast-transform-${TEST_NAME} COMMAND diff ${OUTPUT_FILE} ${REFERENCE_FILE})
