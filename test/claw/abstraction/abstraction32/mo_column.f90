MODULE mo_column
  IMPLICIT NONE
CONTAINS

  SUBROUTINE compute(nz, q, t, z)
    IMPLICIT NONE

    INTEGER, INTENT(IN)   :: nz   ! Size of the array field
    REAL, INTENT(INOUT)   :: t(:) ! Field declared as one column only
    REAL, INTENT(INOUT)   :: q(:) ! Field declared as one column only
    REAL, INTENT(INOUT)   :: z    ! Field declared as scalar

    !$claw parallelize forward
    CALL compute_column(nz, q, t, z)

  END SUBROUTINE compute


  ! Compute only one column
  SUBROUTINE compute_column(nz, q, t, z)
    IMPLICIT NONE

    INTEGER, INTENT(IN)   :: nz   ! Size of the array field
    REAL, INTENT(INOUT)   :: t(:) ! Field declared as one column only
    REAL, INTENT(INOUT)   :: q(:) ! Field declared as one column only
    REAL, INTENT(INOUT)   :: z    ! Field declared as scalar
    REAL, DIMENSION(:), ALLOCATABLE :: y
    INTEGER :: k                  ! Loop index
    REAL :: c                     ! Coefficient

    ! CLAW definition

    ! Define one dimension that will be added to the variables defined in the
    ! data clause.
    ! Apply the parallelization transformation on this subroutine.

    !$claw define dimension proma(1:nproma) &
    !$claw parallelize data(t,q,z) over(proma,:)

    ALLOCATE(y(nz))

    ! claw parallel region should start from here

    c = 5.345
    DO k = 2, nz
      t(k) = c * k
      q(k) = q(k - 1)  + t(k) * c
    END DO
    q(nz) = q(nz) * c

    ! claw parallel region should end here

    DEALLOCATE(y)
  END SUBROUTINE compute_column
END MODULE mo_column
