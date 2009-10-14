#ifndef PROACTIVE_MPI_H_
#define PROACTIVE_MPI_H_

#include <mpi.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <sys/sem.h>
#include <unistd.h>
#include <stdio.h>
#include <signal.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <sys/wait.h>
#include <pthread.h>
#include <sched.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <errno.h>
#include <stdarg.h>
#include <sys/stat.h>
#include "common_definition.h"

typedef struct _proactiveRequest {
	  void *buf;
	  int count;
	  MPI_Datatype datatype;
	  int src;
	  int tag;
	  int idjob;
	  int finished;
	  int op_type; /* 0 is send, 1 is recv*/
} ProActiveMPI_Request;

/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+---- MPI/C <-> MPI/CFUNCTIONS -+-----+----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

int ProActiveMPI_Init(int rank);
int ProActiveMPI_Send(void* buf, int count, MPI_Datatype datatype, int dest, int tag, int idjob );
int ProActiveMPI_ISend(void* buf, int count, MPI_Datatype datatype, int dest, int tag, int idjob, ProActiveMPI_Request *r);
int ProActiveMPI_Recv(void* buf, int count, MPI_Datatype datatype, int src, int tag, int idjob);
int ProActiveMPI_IRecv(void* buf, int count, MPI_Datatype datatype, int src, int tag, int idjob, ProActiveMPI_Request * request);
int ProActiveMPI_Test(ProActiveMPI_Request *r);
int ProActiveMPI_Wait(ProActiveMPI_Request *r);

int ProActiveMPI_Bcast(void * sendbuf, int count, MPI_Datatype datatype, int tag, int nb_send, int * pa_rank_array);

int ProActiveMPI_Scatter(int nb_send, void ** buffers, int * count_array, MPI_Datatype datatype, int tag, int * pa_rank_array);

//int ProActiveMPI_AllSend(void * buf, int count, MPI_Datatype datatype, int tag, int idjob);
int ProActiveMPI_Job(int * job, int * nb_process);
//int ProActiveMPI_Barrier(int job);
int ProActiveMPI_Finalize();

/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+- MPI/F77 <-> MPI/F77 FUNCTIONS -+---+----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

void proactivempi_init_(int* rank,  int* ierr);
void proactivempi_job_(int * job, int * nb_process, int* ierr);
void proactivempi_send_(void * buf, int* cnt, MPI_Datatype* datatype, int* dest, int* tag, int* idjob, int* ierr);
void proactivempi_recv_(void* buf, int* cnt, MPI_Datatype* datatype, int* src, int* tag, int* idjob, int* ierr);
/*
void proActivempi_irecv_(void* buf, int* cnt, MPI_Datatype* datatype, int* src, int* tag, int* idjob, int * request, int* ierr);
void proactivempi_test_(int *r, int* flag, int* ierr);
void proactivempi_wait_(int *r, int* ierr);
*/
void proactivempi_finalize_(int* ierr);
//void proactivempi_barrier_(int* job, int* ierr);
//void proactivempi_allsend_(void * buf, int* cnt, MPI_Datatype* datatype, int* tag, int* idjob, int*ierr);


#endif /* PROACTIVE_MPI_H_ */
