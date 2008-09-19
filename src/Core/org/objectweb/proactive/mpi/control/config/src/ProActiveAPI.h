#ifndef PROACTIVE_API_H_
#define PROACTIVE_API_H_

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

#include "CommonInternalApi.h"

/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+----- PROACTIVE <-> MPI FUNCTIONS  -+-----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

int ProActiveSend(void* buf, int count, MPI_Datatype datatype, int dest, char* clazz, char* method, int idjob, ...);
int ProActiveRecv(void* buf, int count, MPI_Datatype datatype, int src, int tag, int idjob);
int ProActiveIRecv(void* buf, int count, MPI_Datatype datatype, int src, int tag, int idjob, ProActiveMPI_Request * request);
int ProActiveTest(ProActiveMPI_Request *r, int* finished);
int ProActiveWait(ProActiveMPI_Request *r);

#endif /* PROACTIVE_API_H_ */
