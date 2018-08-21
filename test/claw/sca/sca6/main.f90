!
! This file is released under terms of BSD license
! See LICENSE file for more information
!
! Test the CLAW abstraction model with one additional dimension.
!

PROGRAM test_abstraction6
  USE mo_column_extra, ONLY: compute_one
  REAL, DIMENSION(20,60) :: q, t  ! Fields as declared in the whole model
  INTEGER :: nproma, nz           ! Size of array fields
  INTEGER :: p                    ! Loop index

  nproma = 20
  nz = 60

  DO p = 1, nproma
    q(p,1) = 0.0
    t(p,1) = 0.0
  END DO

  !$claw parallelize forward create update
  DO p = 1, nproma
    CALL compute_one(nz, q(p,:), t(p,:))
  END DO

  PRINT*,SUM(q)
  PRINT*,SUM(t)
END PROGRAM test_abstraction6
