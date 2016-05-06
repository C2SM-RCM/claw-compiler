! Simple program to test the CLAW compile guard

PROGRAM compile_guard_test
  CALL dummy_subroutine
END PROGRAM

SUBROUTINE dummy_subroutine
  INTEGER :: i
  !$acc claw-guard
  !$claw loop-fusion
  DO i=1,10
    PRINT *, 'First loop body:',i
  END DO

  !$claw loop-fusion
  DO i=1,10
    PRINT *, 'Second loop body:',i
  END DO
END SUBROUTINE
