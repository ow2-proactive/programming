#include <stdio.h>
#include "mpi.h"
#include <unistd.h>
#include <string.h>

int main(argc, argv)
int argc;
char **argv;
{
   char hostname[20];
   int rank, size;
   MPI_Init(&argc,&argv);
   MPI_Comm_rank(MPI_COMM_WORLD, &rank);
   MPI_Comm_size(MPI_COMM_WORLD, &size);
   gethostname(hostname,20);
   printf("<%s> Hello world! I am %d of %d\n",hostname,rank,size);
   MPI_Finalize();
   return 0;
}
