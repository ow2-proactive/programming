//@snippet-start MPI_Coupling_Example
#include <stdio.h>
#include <stdlib.h>
#include "mpi.h"
/*Include ProActiveMPI header*/
#include "ProActiveMPI.h"


int main (int argc, char **argv)
{
   int myjobid, otherjobid, i, last, nprocs, allprocs, error, rank, size;
   double t0, t1, time;
   double *a, *b;
   double max_rate = 0.0, min_latency = 10e6;
   ProActiveMPI_Request request;

   a = (double *) malloc (1048576* sizeof (double));
   b = (double *) malloc (1048576* sizeof (double));


   for (i = 0; i < 1048576; i++) {
      a[i] = (double) i;
      b[i] = 0.0;
   }

   MPI_Init(&argc, &argv);
   MPI_Comm_rank(MPI_COMM_WORLD, &rank);

   /*Init ProActiveMPI passing MPI rank as parameter*/
   error = ProActiveMPI_Init(rank);
   if (error < 0){
           printf("[MPI] !!! Error ProActiveMPI init \n");
           MPI_Abort( MPI_COMM_WORLD, 1 );
   }

   /*Obtain Application (or Job) ID*/
   ProActiveMPI_Job (&myjobid, &allprocs);
   otherjobid = (myjobid + 1) % 2;


   /*Synchronous ProActiveMPI ping-pong*/
   if (myjobid == 0) printf("\n  Synchronous ping-pong\n\n");
 
   for (size = 8; size <= 8388608; size *= 2) {

	  t0 = MPI_Wtime();
      if (myjobid == 0) {
         ProActiveMPI_Send(a, size/8, MPI_DOUBLE, 0, 0, otherjobid);
         ProActiveMPI_Recv(b, size/8, MPI_DOUBLE, 0, 0, otherjobid);
      } else {
         ProActiveMPI_Recv(b, size/8, MPI_DOUBLE, 0, 0, otherjobid);
         ProActiveMPI_Send(b, size/8, MPI_DOUBLE, 0, 0, otherjobid);
      }
      t1 = MPI_Wtime();
      time = 1.e6 * (t1 - t0);

   	  if (myjobid == 0 && time > 0.000001) {
    	  printf(" %7d bytes took %9.0f usec (%8.3f MB/sec)\n", size, time, 2.0 * size / time);

	  } else if (myjobid == 0) {
			  printf(" %7d bytes took less than the timer accuracy\n", size);
	  }

   }


/*asynchronous ProActiveMPI ping-pong*/
if (myjobid == 0) printf("\n Asynchronous ping-pong\n\n");

    for (size = 8; size <= 8388608; size *= 2) {


      ProActiveMPI_IRecv(b, size/8, MPI_DOUBLE, 0, 0, otherjobid, &request);

 	  t0 = MPI_Wtime();
       if (myjobid == 0) {
    	   ProActiveMPI_Send(a, size/8, MPI_DOUBLE, 0, 0, otherjobid);
    	   ProActiveMPI_Wait(&request);
       } else {
    	   ProActiveMPI_Wait(&request);
    	   ProActiveMPI_Send(b, size/8, MPI_DOUBLE, 0, 0,  otherjobid);
       }
       t1 = MPI_Wtime();
       time = 1.e6 * (t1 - t0);

    	  if (myjobid == 0 && time > 0.000001) {
     	  printf(" %7d bytes took %9.0f usec (%8.3f MB/sec)\n", size, time, 2.0 * size / time);

 	  } else if (myjobid == 0) {
 			  printf(" %7d bytes took less than the timer accuracy\n", size);
 	  }

    }

   /*Finalize ProActiveMPI application (before finalizing MPI application)*/
   ProActiveMPI_Finalize();
   MPI_Finalize();
}

//@snippet-end MPI_Coupling_Example


