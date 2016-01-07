# Base test definition
# Perform code transformation and compare the output with a reference

# Define input and output file name
set (ORIGINAL_FILE original_code.f90)
set (OUTPUT_FILE transformed_code.f90)
set (REFERENCE_FILE reference.f90)

# Define executable file name
set (EXECUTABLE_ORIGINAL original_code_${TEST_NAME})
set (EXECUTABLE_TRANSFORMED transformed_code_${TEST_NAME})

# Define directory for build
set (XMOD_DIR __xmod__)
set (OMNI_TMP_DIR __omni_tmp__)

if (NOT EXISTS ${XMOD_DIR})
  file(MAKE_DIRECTORY ${XMOD_DIR})
endif()

# Create intermediate representation in XcodeML Fortran format
add_custom_command(
  OUTPUT  ${OUTPUT_FILE}
  COMMAND ${CLAWFC} -J ${XMOD_DIR} --Wx-d -o ${OUTPUT_FILE} ${ORIGINAL_FILE}
  DEPENDS ${ORIGINAL_FILE}
  COMMENT "Translating CLAW directive with ${CLAWFC}"
)

add_custom_target(transform-${TEST_NAME} ALL
  DEPENDS ${OUTPUT_FILE})

# Clean previous transformed code to trigger the transformation each time as
# original code doesn't change but we want to test the transformation
add_custom_command(
  TARGET transform-${TEST_NAME}
  PRE_BUILD
  COMMAND rm ${OUTPUT_FILE}
  COMMENT "Clean previous transformation"
)


# Build the original code and the transformed code
add_executable (${EXECUTABLE_ORIGINAL} ${ORIGINAL_FILE})
add_executable (${EXECUTABLE_TRANSFORMED} ${OUTPUT_FILE})

# Compare reference transformed code and output of the transformation
add_test(NAME ast-transform-${TEST_NAME} COMMAND diff ${OUTPUT_FILE} ${REFERENCE_FILE})

# Add build directory to be removed with clean target
set_property(
  DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
  PROPERTY ADDITIONAL_MAKE_CLEAN_FILES ${XMOD_DIR} ${OMNI_TMP_DIR}
)
