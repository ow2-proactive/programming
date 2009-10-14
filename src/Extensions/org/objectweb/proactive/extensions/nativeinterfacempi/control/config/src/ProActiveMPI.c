/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 *
 * ################################################################
 */

#include "ProActiveMPI.h"
#include "ProActiveMPI_Message.h"
#include "message_queue.h"
#include "native_layer.h"

double timer_recv_msg_start = 0;
double timer_recv_msg_acc = 0;
double timer_handle_msg_start = 0;
double timer_handle_msg_acc = 0;
double timer_queue_msg_start = 0;
double timer_queue_msg_acc = 0;


//int C2S_Q_ID, S2C_Q_ID;

/* Some informations about the mpi job */

int myRank = -1;
int my_job_id=-1;
int nb_job=-1;

//int TAG_S_KEY;
//int TAG_R_KEY;

extern FILE * mslog;

 int internal_ProActiveMPI_Recv(void* buf, int count, MPI_Datatype datatype,
		int src, int tag, int idjob, int no_wait, int * ret_code);

int is_awaited_message(int idjob, int src, int tag, ProActive_Datatype pa_datatype,
					   int count, msg_t_pa_mpi * message);

FILE * open_mpi_debug_log(char *path, int rank, char * prefix) {
	char hostname[MAX_NOM];
	char nombre[2];
	int err = 0;
	sprintf(nombre, "%d", rank);
	gethostname(hostname, MAX_NOM);
	umask(000);
	err = mkdir(path, S_IRWXU | S_IRWXG | S_IRWXO);
	//	if (err < 0) {
	//		perror("MKDIR failed because");
	//	}

	//TODO possible bug as EEXIST indicate that it could be a file
	strcat(path, "/");
	strcat(path, prefix);
	strcat(path, "_");
	strcat(path, hostname);
	strcat(path, "_");
	strcat(path, nombre);
	printf("PATH %s\n", path);

	return fopen(path, "w");
}

/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+-----+- MPI <-> MPI FUNCTIONS -+----+-----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

/*
 * ProActiveMPI_Init
 */
 int ProActiveMPI_Init(int rank) {
	int error;
	msg_t_pa_mpi * static_recv_msg_buf;

	// keep rank of this process
	myRank=rank;

	// init pampi message queue
	init_pa_mpi_msg_queue();

	if (DEBUG_PAMPI_LAYER) {
		char path [256];
		path[0]='\0';
		strcpy(path, DEBUG_LOG_OUTPUT_DIR);
		if ((mslog = (open_mpi_debug_log(path, myRank, "mpi_log"))) == NULL) {
			printf("ERROR WHILE OPENING FILE PATH= %s \n", path);
			perror("[PA/MPI][Init] ERROR !!! Can't initialize logging files");
			exit(1);
		}
		fprintf(mslog, "Initializing queues \n");
		fflush(mslog);
	}

	if ((static_recv_msg_buf = (msg_t_pa_mpi *) malloc(sizeof(msg_t_pa_mpi))) == NULL) {
		if (DEBUG_PAMPI_LAYER) {
			fprintf(mslog, "[PA/MPI][Init] !!! ERROR : MALLOC FAILED\n");
		}
		perror("[PA/MPI][Init] !!! ERROR : MALLOC FAILED");
		return -1;
	}

	// init the native interface
	error = init(0);

	//	error = start_ipc_queue();
	if (error < 0) {
		return error;
	}

	if (DEBUG_PAMPI_LAYER) {
		fprintf(mslog, "[PA/MPI][Init]Start native interface initialization \n ");
	}

	int serialized_init_msg_length;
	void * serialized_init_msg;

	// callback the ProActive runtime with the init message
	serialize_init_message(-1, myRank, &serialized_init_msg_length, &serialized_init_msg);
	if (DEBUG_PAMPI_LAYER) {
		fprintf(mslog, "[PA/MPI][Init] Start native interface INIT message to be sent \n ");
	}

	error = send_message(serialized_init_msg_length, serialized_init_msg);

	if (DEBUG_PAMPI_LAYER) {
		fprintf(mslog, "[PA/MPI][Init] Start native interface INIT message sent \n ");
	}

	free(serialized_init_msg);

	if (error < 0) {
		DEBUG_PRINT_PAMPI_LAYER(mslog, fprintf(mslog, "[PA/MPI][Init] !!! ERROR: msgsnd error\n"))
		perror("ERROR");
		return -1;
	}

	if (DEBUG_PAMPI_LAYER) {
		fprintf(mslog, "Waiting for job number in recv queue \n ");
	}

	// Waiting ack from the ProActive runtime, and retrieve the jobId
	error = recv_message(&serialized_init_msg_length, &serialized_init_msg);

	if (error < 0) {
		return error;
	}

	convert_to_msg_t_pa_mpi(serialized_init_msg_length, serialized_init_msg, static_recv_msg_buf);

	free(serialized_init_msg);
//	error = recv_raw_msg_from_ipc_queue(S2C_Q_ID, TAG_R_KEY, &msg_buf,
//			get_payload_size(&msg_buf), 0, &ret_code);

	// update the job field of this mpi process
	// nb_job is setted in buf.src_rank has a convenience
	my_job_id = static_recv_msg_buf->dest_idjob;
	nb_job = static_recv_msg_buf->src_rank;

	if (DEBUG_PAMPI_LAYER) {
		fprintf(mslog, "[PA/MPI][Init] Process\n");
		fprintf(mslog, "[PA/MPI][Init] myRank == %d \n", myRank);
		fprintf(mslog, "[PA/MPI][Init] nbProActiveJob == %d\n",
				nb_job);
		fflush(mslog);
	}

	// init the native interface reading thread
	start_thread();

	free(static_recv_msg_buf);
	return 0;
}

/*
 * ProActiveMPI_Job
 */
 int ProActiveMPI_Job(int * job_, int * nb_job_) {
	*job_ = my_job_id;
	*nb_job_ = nb_job;
	return 0;
}

 int ProActiveMPI_Bcast(void * sendbuf, int count, MPI_Datatype datatype, int tag, int nb_send, int * pa_rank_array) {
	void * serialized_msg;
	int serialized_msg_length;
	int ret = -1;

	serialize_bcast_message(nb_send, sendbuf, count, myRank, my_job_id, pa_rank_array, datatype, tag,
							&serialized_msg_length, &serialized_msg);

	ret = send_message(serialized_msg_length, serialized_msg);

	free(serialized_msg);

	return ret;
}

 int ProActiveMPI_Scatter(int nb_send, void ** buffers, int * count_array, MPI_Datatype datatype, int tag, int * pa_rank_array) {

	void * serialized_msg;
	int serialized_msg_length;
	int ret = -1;

	serialize_scatter_message(nb_send, buffers, myRank, my_job_id, pa_rank_array, count_array, datatype, tag,
								   &serialized_msg_length, &serialized_msg);

//	ret = 0;
//	while(ret < nb_send) {
//		printf("%d" ((char*)serialized_msg) + ret*sizeof(int))
//	}
	ret = send_message(serialized_msg_length, serialized_msg);

	free(serialized_msg);

	return ret;
}

/*
 * ProActiveMPI_Send
 */
 int ProActiveMPI_Send(void * data_buffer, int count, MPI_Datatype mpi_datatype, int dest_rank,
		int msg_tag, int dest_idjob) {

	void * serialized_msg;
	int serialized_msg_length;
	int ret = -1;

	serialize_send_message(data_buffer, count, mpi_datatype, myRank, dest_rank, my_job_id, dest_idjob, msg_tag,
						   &serialized_msg_length, &serialized_msg);

	//TODO check message is sent to C2S_Q_ID
	ret = send_message(serialized_msg_length, serialized_msg);

	free(serialized_msg);

	return ret;
}


 int ProActiveMPI_ISend(void* buf, int count, MPI_Datatype datatype, int dest,
		int tag, int idjob, ProActiveMPI_Request *r) {
	int error = 0;
	error = ProActiveMPI_Send(buf, count, datatype, dest, tag, idjob);
	r->buf = buf; // keep buf address in structure to update it later
	r->count = count;
	r->datatype = datatype;
	r->src = dest;
	r->tag = tag;
	r->idjob = idjob;
	r->op_type = 0; /*send*/;

	if (!error) {
		r->finished = 1;
	}

	return error;
}

 int ProActiveMPI_IRecv(void* buf, int count, MPI_Datatype datatype, int src,
		int tag, int idjob, ProActiveMPI_Request *r) {

	r->buf = buf; // keep buf address in structure to update it later
	r->count = count;
	r->datatype = datatype;
	r->src = src;
	r->tag = tag;
	r->idjob = idjob;
	r->finished = 0;
	r->op_type = 1; /*recv*/;

	return 0;
}

 int ProActiveMPI_Recv(void* buf, int count, MPI_Datatype datatype, int src,
		int tag, int idjob) {
	int nb_read = 0;
	int ret_code = 0;
	// we block until we get the expected message
	nb_read = internal_ProActiveMPI_Recv(buf, count, datatype, src, tag, idjob,
			0/*blocking call*/, &ret_code);
	return nb_read;
}

 int ProActiveMPI_Test(ProActiveMPI_Request *r) {
	int nb_read = 0;
	int ret_code = 0;
	// we try to get the message only one time
	nb_read = internal_ProActiveMPI_Recv(r->buf, r->count, r->datatype, r->src,
			r->tag, r->idjob, 1/*non-blocking call*/, &ret_code);

	if ((nb_read == 0) && (ret_code == ENOMSG)) {
		// we got an error however if error has been triggered because queue is empty
		// we can ignore it as it is the purpose of the ProActiveMPI_Test function to
		// test that finished is not set as we didn't receive the message.
		return 0;
	}

	if (nb_read >= 0) {
		// we received the awaited message
		r->finished = 1;
	}

	return nb_read;
}

 int ProActiveMPI_Wait(ProActiveMPI_Request *r) {
	int nb_read = ProActiveMPI_Recv(r->buf, r->count, r->datatype, r->src,
			r->tag, r->idjob);
	if (nb_read >= 0) {
		r->finished = 1;
	}
	return nb_read;
}


 int internal_ProActiveMPI_Recv(void* buf, int count, MPI_Datatype datatype,
		int src, int tag, int idjob, int no_wait, int * ret_code) {

	msg_t_pa_mpi * recv_msg_buf = NULL;
	ProActive_Datatype pa_datatype = type_conversion_MPI_to_proactive(datatype);
	int awaited = 0;
	int ret = -1;

	DEBUG_PRINT_PAMPI_LAYER(
			mslog,
			fprintf(
					mslog,
					"[PA/MPI][internal] Try to recv from jobid:%d, rank:%d tagged as %d max_count:%d \n",
					idjob, src, tag, count));

	// we wait until we get the awaited message.
	do {
		timer_queue_msg_start = MPI_Wtime();

		recv_msg_buf = get_message(count, pa_datatype, src, tag, idjob);

		timer_queue_msg_acc += (MPI_Wtime() - timer_queue_msg_start);

		// the awaited message is not available
		if (recv_msg_buf == NULL) {
			if (no_wait) {
				return 0;
			}
		} else {
			awaited = 1;
		}
	} while (awaited == 0);

	//START TIMER HANDLE MESSAGE
	timer_handle_msg_start = MPI_Wtime();

	//Received message is the one we're waiting for.
	int length = get_mpi_buffer_length(recv_msg_buf->count, datatype, sizeof(char));

	if (length < 0) {
		if (DEBUG_PAMPI_LAYER) {
			fprintf(mslog, "[PA/MPI][internal] !!! WRONG DATATYPE \n");
		}
		return -3;
	}
	// TODO see how we could integrate this copy into recv_message
	memcpy(buf, recv_msg_buf->data, length);

	if (DEBUG_PAMPI_LAYER) {
		fprintf(mslog, "[PA/MPI][internal] recv_msg_buf->count %d BEGIN\n", recv_msg_buf->count);
		fprintf(mslog, "[PA/MPI][internal] recv_msg_buf->count %d END\n", recv_msg_buf->count);
	}

	ret = recv_msg_buf->count;

	free_msg_t_pa_mpi(recv_msg_buf);


	//STOP TIMER HANDLE MESSAGE
	timer_handle_msg_acc += (MPI_Wtime() - timer_handle_msg_start);
	return ret;
}

/*
 * ProActiveMPI_Finalize
 */
int ProActiveMPI_Finalize() {
	int error;

//	print_timer();
	printf("TIMER recv %15.6f handle %15.6f queue %15.6f\n", timer_recv_msg_acc, timer_handle_msg_acc, timer_queue_msg_acc);

	int serialization_length;
	void * serialized_finalize_msg;

	//check C2S_Q_ID
	serialize_finalize_message(my_job_id, myRank, &serialization_length, &serialized_finalize_msg);

	error = send_message(serialization_length, serialized_finalize_msg);

	free(serialized_finalize_msg);

	if (error < 0) {
		if (DEBUG_PAMPI_LAYER) {
			fprintf(mslog, "[PA/MPI][Finalize] !!! ERROR: msgsnd error\n");
		}
		perror("[PA/MPI][Finalize] ERROR");
		return -1;
	}
	return 0;
}

// /////////////////////////////////////////////////////////
// ///////////////// F77 IMPLEMENTATION /////////////////////
// //////////////////////////////////////////////////////////


void proactivempi_init_(int * rank, int* ierr) {
	*ierr = ProActiveMPI_Init(*rank);
}

void proactivempi_send_(void * buf, int* cnt, MPI_Datatype* datatype,
		int* dest, int* tag, int* idjob, int* ierr) {
	*ierr = ProActiveMPI_Send(buf, *cnt, *datatype, *dest, *tag, *idjob);
}

void proactivempi_recv_(void* buf, int* cnt, MPI_Datatype* datatype, int* src,
		int* tag, int* idjob, int* ierr) {
	*ierr = ProActiveMPI_Recv(buf, *cnt, *datatype, *src, *tag, *idjob);
}

/* NON BLOCKING COMMUNICATION - HOW TO HANDLE REQUEST WITH PROACTIVEMPI_FORTRAN ?
 *
 * Define a structure:
 *
 * struct _request{
 * 		ProActiveMPI_Request  myRequest;
 * 		struct request * next;
 * } request;
 *
 * In fortran the request handler is an INTEGER.
 *
 * For a PROACTIVEMPI_IRECV go through the linked list and create a new ProActiveMPI_Request
 * at the index define in the fortran subroutine that is the INTEGER request.
 *
 *  Unlike in C we cannot use directly some pointers in Fortran, but an INTEGER.
 *  So a linked list has to be defined in order to store a corresponding structure.
 *
 */

void proactivempi_job_(int * job, int * nb_process, int* ierr) {
	*ierr = ProActiveMPI_Job(job, nb_process);
}

void proactivempi_finalize_(int* ierr) {
	*ierr = ProActiveMPI_Finalize();
}
