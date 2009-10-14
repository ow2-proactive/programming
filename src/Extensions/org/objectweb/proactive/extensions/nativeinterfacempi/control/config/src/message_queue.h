#ifndef MESSAGE_QUEUE_H_
#define MESSAGE_QUEUE_H_

#define RECV_QUEUE_SIZE 80

#include <stdio.h>
#include <list>
#include "common_definition.h"
#include "ProActiveMPI_Message.h"

/* thread methods */
void start_thread();

/* message queue methods */

void init_pa_mpi_msg_queue();

msg_t_pa_mpi * get_message(int count, ProActive_Datatype pa_datatype,
		int src, int tag, int idjob);

void store_message(msg_t_pa_mpi * recv_msg_buf);


#endif /*MESSAGE_QUEUE_H_*/
