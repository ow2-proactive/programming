#include <stdio.h>
#include <stdlib.h>
#include "mpi.h"
/*#include "ProActiveMPI.h"*/


int main (int argc, char **argv)
{
   int myproc, size, other_proc, nprocs, i, last;
   double t0, t1, time;
   double *a, *b;
   double max_rate = 0.0, min_latency = 10e6;
   MPI_Request request, request_a, request_b;
   MPI_Status status;

   a = (double *) malloc (1048576 * sizeof (double));
   b = (double *) malloc (1048576 * sizeof (double));


   for (i = 0; i < 1048576; i++) {
      a[i] = (double) i;
      b[i] = 0.0;
   }

   MPI_Init(&argc, &argv);
   MPI_Comm_size(MPI_COMM_WORLD, &nprocs);
   MPI_Comm_rank(MPI_COMM_WORLD, &myproc);


   other_proc = (myproc + 1) % 2;


/* Communications between nodes
 *   - Blocking sends and recvs
 *   - No guarantee of prepost, so might pass through comm buffer
 */

   if (myproc == 0) printf("\n  Synchronous ping-pong\n\n");

   for (size = 8; size <= 8388608; size *= 2) {

	  t0 = MPI_Wtime();
      if (myproc == 0) {
         MPI_Send(a, size/8, MPI_DOUBLE, other_proc, 0, MPI_COMM_WORLD);
         MPI_Recv(b, size/8, MPI_DOUBLE, other_proc, 0, MPI_COMM_WORLD, &status);
      } else {
         MPI_Recv(b, size/8, MPI_DOUBLE, other_proc, 0, MPI_COMM_WORLD, &status);
         MPI_Send(b, size/8, MPI_DOUBLE, other_proc, 0, MPI_COMM_WORLD);
      }
      t1 = MPI_Wtime();
      time = 1.e6 * (t1 - t0);

	  if (myproc == 0 && time > 0.000001) {
	  printf(" %7d bytes took %9.0f usec (%8.3f MB/sec)\n", size, time, 2.0 * size / time);

	  } else if (myproc == 0) {
			  printf(" %7d bytes took less than the timer accuracy\n", size);
	  }

   }


   if (myproc == 0) printf("\n  Asynchronous ping-pong\n\n");

    for (size = 8; size <= 8388608; size *= 2) {


      MPI_Irecv(b, size/8, MPI_DOUBLE, other_proc, 0, MPI_COMM_WORLD, &request);

	  t0 = MPI_Wtime();
       if (myproc == 0) {
	   MPI_Send(a, size/8, MPI_DOUBLE, other_proc, 0, MPI_COMM_WORLD);
	   MPI_Wait(&request, &status);
       } else {
	   MPI_Wait(&request, &status);
	   MPI_Send(b, size/8, MPI_DOUBLE, other_proc, 0, MPI_COMM_WORLD);
       }
       t1 = MPI_Wtime();
       time = 1.e6 * (t1 - t0);

	  if (myproc == 0 && time > 0.000001) {
	  printf(" %7d bytes took %9.0f usec (%8.3f MB/sec)\n", size, time, 2.0 * size / time);

	  } else if (myproc == 0) {
			  printf(" %7d bytes took less than the timer accuracy\n", size);
	  }

    }


   MPI_Finalize();
}



