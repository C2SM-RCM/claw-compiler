PROGRAM loop_interchange

 CALL clawloop ( )
END PROGRAM loop_interchange

SUBROUTINE clawloop ( )
 INTEGER :: i
 INTEGER :: j
 INTEGER :: k
 INTEGER :: a
 INTEGER :: b
 INTEGER :: c
 INTEGER :: x
 INTEGER :: y
 INTEGER :: z

!$claw loop-interchange new-order(k,i,j)
 DO k = 1 , 2 , 1
  DO i = 1 , 4 , 1
   DO j = 1 , 3 , 1
    PRINT * ,"Iteration i=" , i ,", j=" , j ,", k=" , k
   END DO
  END DO
 END DO
!$claw loop-interchange new-order(b,c,a)
 DO b = 1 , 3 , 1
  DO c = 1 , 2 , 1
   DO a = 1 , 4 , 1
    PRINT * ,"Iteration i=" , i ,", j=" , j ,", k=" , k
   END DO
  END DO
 END DO
!$claw loop-interchange new-order(x,z,y)
 DO x = 1 , 4 , 1
  DO z = 1 , 2 , 1
   DO y = 1 , 3 , 1
    PRINT * ,"Iteration i=" , i ,", j=" , j ,", k=" , k
   END DO
  END DO
 END DO
END SUBROUTINE clawloop

