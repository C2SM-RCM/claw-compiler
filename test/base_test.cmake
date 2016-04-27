# Base test definition
# Perform code transformation and compare the output with a reference

# Define input and output file name
set (ORIGINAL_FILE ${CMAKE_CURRENT_SOURCE_DIR}/original_code.f90)
set (OUTPUT_FILE ${CMAKE_CURRENT_SOURCE_DIR}/transformed_code.f90)
set (REFERENCE_FILE ${CMAKE_CURRENT_SOURCE_DIR}/reference.f90)

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
if(${TEST_DEBUG}) # with debug option
  add_custom_command(
    OUTPUT  ${OUTPUT_FILE}
    COMMAND ${CLAWFC} ${OPTIONAL_FLAGS} --debug -J ${XMOD_DIR} --Wx-d -o ${OUTPUT_FILE} ${ORIGINAL_FILE}
    WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
    DEPENDS ${ORIGINAL_FILE}
    COMMENT "Translating CLAW directive with ${CLAWFC}"
  )
else() # without debug option
  add_custom_command(
    OUTPUT  ${OUTPUT_FILE}
    COMMAND ${CLAWFC} ${OPTIONAL_FLAGS} -J ${XMOD_DIR} -o ${OUTPUT_FILE} ${ORIGINAL_FILE}
    WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
    DEPENDS ${ORIGINAL_FILE}
    COMMENT "Translating CLAW directive with ${CLAWFC}"
  )
endif()

add_custom_target(
  transform-${TEST_NAME}
  DEPENDS ${OUTPUT_FILE} ${EXECUTABLE_ORIGINAL} ${EXECUTABLE_TRANSFORMED}
)

add_custom_target(
  clean-${TEST_NAME}
  COMMAND rm -f ${OUTPUT_FILE}
)

add_dependencies(${BUILD_TEST_TARGET} transform-${TEST_NAME})
add_dependencies(${CLEAN_TEST_TARGET} clean-${TEST_NAME})

# Build the original code and the transformed code
add_executable (${EXECUTABLE_ORIGINAL} EXCLUDE_FROM_ALL ${ORIGINAL_FILE})
add_executable (${EXECUTABLE_TRANSFORMED} EXCLUDE_FROM_ALL ${OUTPUT_FILE})

if(NOT IGNORE_TEST)
  # Compare reference transformed code and output of the transformation
  add_test(NAME ast-transform-${TEST_NAME} COMMAND diff ${OUTPUT_FILE} ${REFERENCE_FILE})
  # Compare the output of both executable
  if(OUTPUT_TEST)
    set (TEST_PARAMETERS "<(./${EXECUTABLE_ORIGINAL}) <(./${EXECUTABLE_TRANSFORMED})")
    add_test(NAME compare-output-${TEST_NAME} COMMAND bash -c "diff ${TEST_PARAMETERS}")
  endif()
endif()
# Add build directory to be removed with clean target
set_property(
  DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
  PROPERTY ADDITIONAL_MAKE_CLEAN_FILES ${XMOD_DIR} ${OMNI_TMP_DIR}
)
