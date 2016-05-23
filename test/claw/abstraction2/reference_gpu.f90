MODULE mo_column

CONTAINS
 SUBROUTINE compute_column ( nz , q , t , nx , ny )
  INTEGER , INTENT(IN) :: nz
  REAL , INTENT(INOUT) :: t ( : , : , : )
  REAL , INTENT(INOUT) :: q ( : , : , : )
  INTEGER :: k
  REAL :: c
  INTEGER , INTENT(IN) :: nx
  INTEGER :: i
  INTEGER , INTENT(IN) :: ny
  INTEGER :: j

!$acc parallel
!$acc loop collapse(2)
  DO i = 1 , nx , 1
   DO j = 1 , ny , 1
    c = 5.345
    DO k = 2 , nz , 1
     t ( i , j , k ) = c * k
     q ( i , j , k ) = q ( i , j , k - 1 ) + t ( i , j , k ) * c
    END DO
    q ( i , j , nz ) = q ( i , j , nz ) * c
   END DO
  END DO
!$acc end parallel
 END SUBROUTINE compute_column

END MODULE mo_column

