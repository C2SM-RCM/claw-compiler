MODULE mo_column

CONTAINS
 FUNCTION compute_point ( t , nproma ) RESULT(q)
  INTEGER , INTENT(IN) :: nproma
  REAL , INTENT(IN) :: t ( 1 : nproma )
  REAL :: q ( 1 : nproma )
  REAL :: c
  INTEGER :: proma

!$acc data present(t)
!$acc parallel
!$acc loop gang vector
  DO proma = 1 , nproma , 1
   c = 5.345
   q ( proma ) = q ( proma ) + t ( proma ) * c
  END DO
!$acc end parallel
!$acc end data
 END FUNCTION compute_point

END MODULE mo_column

