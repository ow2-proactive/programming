#include "ProActiveAPI.h"

/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+----- MPI -> PROACTIVE FUNCTIONS  -+-----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

extern msg_t * recv_queue[];
extern int msg_recv_nb;

/*
 * ProActiveSend
 */
int ProActiveSend(void* buf, int count, MPI_Datatype datatype, int dest,
		char* clazz, char* method, int idjob, ...) {
	msg_t send_msg_buf;
	int error;
	//	int pms;
	int length;
	char* next;
	int nb_args = 0;
	char * nb = (char *) malloc(sizeof(char)*2);
	char * parameters = (char *) malloc(50);
	va_list ptr;

	if (DEBUG_STMT) {
		// clear buffers in debug mode to avoid valgrind warnings
		init_msg_t(&send_msg_buf);
	}

	if (DEBUG_NATIVE_SIDE) {
		fprintf(mslog, "Test 0 %s %s\n", clazz, method);
		fflush(mslog);
	}

	strcpy(parameters, "");
	strcpy(send_msg_buf.method, "");

	// ptr initialization
	va_start(ptr, idjob);
	next = va_arg(ptr, char*);
	while (next != NULL) {
		nb_args++;
		strcat(parameters, next);
		strcat(parameters, "\t");
		next = va_arg(ptr, char*);
	}
	sprintf(nb, "%d", nb_args);
	send_msg_buf.msg_type = MSG_SEND_PROACTIVE;
	send_msg_buf.count = count;
	send_msg_buf.src = myRank;
	send_msg_buf.dest = dest;
	send_msg_buf.pa_datatype = type_conversion_MPI_to_proactive(datatype);
	send_msg_buf.TAG = TAG_S_KEY;
	send_msg_buf.idjob = idjob;
	strcpy(send_msg_buf.method, clazz);
	strcat(send_msg_buf.method, "\t");
	strcat(send_msg_buf.method, method);
	strcat(send_msg_buf.method, "\t");
	strcat(send_msg_buf.method, nb);
	strcat(send_msg_buf.method, "\t");
	strcat(send_msg_buf.method, parameters);
	if (DEBUG_NATIVE_SIDE) {
		fprintf(mslog, "Test 4 %s \n", send_msg_buf.method);
		fflush(mslog);
	}

	length = get_mpi_buffer_length(count, datatype, sizeof(char));

	if (length < 0) {
		if (DEBUG_NATIVE_SIDE) {
			fprintf(mslog, "[ProActiveMPI.c] !!! BAD DATATYPE \n");
		}
		return -1;
	}

	memcpy(send_msg_buf.data, buf, length);
	//	pms= sizeof(msg_t) - sizeof(send_msg_buf.TAG) - sizeof(send_msg_buf.data);
	error = msgsnd(C2S_Q_ID, &send_msg_buf, get_payload_size(&send_msg_buf), 0);
	if (error < 0) {
		if (DEBUG_NATIVE_SIDE) {
			fprintf(mslog, "[ProActiveMPI.c] !!! ERROR: msgsnd error\n");
		}
		perror("[ProActiveMPI.c] ERROR");
		return -1;
	}
	if (DEBUG_NATIVE_SIDE) {
		fflush(mslog);
	}

	return 0;
}

/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+----- PROACTIVE -> MPI  FUNCTIONS  -+-----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

/*
 * ProActiveRecv
 */
int ProActiveRecv(void* buf, int count, MPI_Datatype datatype, int src,
		int tag, int idjob) {
	msg_t * recv_msg_buf = (msg_t *) malloc(sizeof(msg_t));
	int error = 0, ret_code = 0;
	//	int pms;
	int length;
	// getting datatype
	if (DEBUG_STMT) {
		// clear buffers in debug mode to avoid valgrind warnings
		init_msg_t(recv_msg_buf);
	}
	//TODO refactor not finished see ProActiveMPI_Recv
	ProActive_Datatype pa_datatype = type_conversion_MPI_to_proactive(datatype);

	DEBUG_PRINT_NATIVE_SIDE(mslog, fprintf(mslog, "[ProActiveMPI.c][ProActiveRecv] Entering %d", errno))

	// first we check if we already receive the message
	recv_msg_buf = check_already_received_msg(count, pa_datatype, src, tag, idjob);
	if (recv_msg_buf == NULL) {
		error = recv_ipc_message(S2C_Q_ID, PROACTIVE_KEY, recv_msg_buf, 0, &ret_code);
	}

	if (error < 0) {
		//TODO free
		return error;
	}

	// if an error occured during receive call check if its an interrupted
	// System call and so retry to receive
	/*
	 while (error < 0){
	 //		strerror(errno);
	 DEBUG_PRINT_NATIVE_SIDE(mslog, fprintf(mslog, "[ProActiveRecv] !!! ERROR: msgrcv error ERRNO = %d, \n", errno))
	 
	 if (errno == EINTR){
	 DEBUG_PRINT_NATIVE_SIDE(mslog, fprintf(mslog, "[ProActiveRecv] !!! ERRNO = EINTR, \n"))
	 error = msgrcv(S2C_Q_ID, recv_msg_buf, get_payload_size(recv_msg_buf), PROACTIVE_KEY, 0);
	 DEBUG_PRINT_NATIVE_SIDE(mslog, fprintf(mslog, "[ProActiveRecv] !!! ERROR: msgrcv error ERRNO = %d, \n", errno))
	 } else {
	 DEBUG_PRINT_NATIVE_SIDE(mslog, fprintf(mslog, "[ProActiveRecv] !!! ERROR %d\n", errno))
	 return -1; 
	 }
	 }*/

	DEBUG_PRINT_NATIVE_SIDE(mslog, fprintf(mslog, "[ProActiveRecv] !!! msgrcv succeeds \n"))

	if (recv_msg_buf->idjob != idjob) {
		DEBUG_PRINT_NATIVE_SIDE(mslog, fprintf(mslog, "[ProActiveRecv] !!! BAD PARAMETER idjob, Queuing the message \n"))
		return -1;
	}
	else if (recv_msg_buf->src != src) {
		if (DEBUG_NATIVE_SIDE) {
			fprintf(mslog, "[ProActiveMPI.c] !!! ERROR: BAD PARAMETER src \n");}
		return -1;
	}
	else if ((tag != MPI_ANY_TAG) && (recv_msg_buf->tag != tag)) {
		DEBUG_PRINT_NATIVE_SIDE(mslog,
				fprintf(mslog, "[ProActiveRecv] !!! ERROR: BAD PARAMETER tag \n"))

		int index = get_available_recv_queue_index();

		if (index == -1) {
			DEBUG_PRINT_NATIVE_SIDE(mslog, fprintf(mslog, "[ProActiveRecv] !!! ERROR: RECV MSG QUEUE IS FULL \n"))
		} else {
			// we store the message in the message queue
			recv_queue[index] = recv_msg_buf;
			msg_recv_nb++;
		}
		//		return -1;
	}
	else if (recv_msg_buf->pa_datatype != pa_datatype) {
		DEBUG_PRINT_NATIVE_SIDE(mslog,
				fprintf(mslog, "[ProActiveRecv] !!! ERROR: BAD PARAMETER datatype \n"))
		return -1;
	}
	else {
		length = get_mpi_buffer_length(count, datatype, sizeof(char));

		if (length < 0) {
			if (DEBUG_NATIVE_SIDE) {fprintf(mslog, "[ProActiveRecv] !!! BAD DATATYPE \n");}
			return -1;
		}

		memcpy(buf, recv_msg_buf->data, length);
		// we don't need the message buffer anymore
		free_msg_t(recv_msg_buf);
	}
	DEBUG_PRINT_NATIVE_SIDE(mslog, fprintf(mslog, "[ProActiveRecv]Exiting %d", errno))
	return 0;
}

/*
 * ProActiveWait
 */
int ProActiveWait(ProActiveMPI_Request *r) {
	msg_t recv_msg_buf;
	int error = -1;
	//	int pms;
	int length;
	if (DEBUG_STMT) {
		// clear buffers in debug mode to avoid valgrind warnings
		init_msg_t(&recv_msg_buf);
	}

	int idjob = (*r).idjob;
	int tag = (*r).tag;
	int count = (*r).count;
	int pa_datatype = type_conversion_MPI_to_proactive(r->datatype);

	//	pms= sizeof(msg_t) - sizeof(recv_msg_buf.TAG) - sizeof(recv_msg_buf.data);

	error = msgrcv(S2C_Q_ID, &recv_msg_buf, get_payload_size(&recv_msg_buf),
			PROACTIVE_KEY, 0);
	while (error < 0) {
		strerror(errno);
		if (DEBUG_NATIVE_SIDE) {
			fprintf(mslog,
					"[ProActiveMPI.c] !!! ERROR: msgrcv error ERRNO = %d, \n",
					errno);
		}

		if (errno == EINTR) {
			if (DEBUG_NATIVE_SIDE) {
				fprintf(mslog, "[ProActiveMPI.c] !!! ERRNO = EINTR, \n");
			}
			error = msgrcv(S2C_Q_ID, &recv_msg_buf,
					get_payload_size(&recv_msg_buf), PROACTIVE_KEY, 0);
		}
		// no message in the queue
		else {
			perror("[ProActiveMPI.c] ERROR");
			return -1;
		}
	}

	if (DEBUG_NATIVE_SIDE) {
		fflush(mslog);
	}
	// filter
	if (recv_msg_buf.idjob != idjob) {
		if (DEBUG_NATIVE_SIDE) {
			fprintf(mslog, "[ProActiveMPI.c] !!! ERROR: BAD PARAMETER idjob \n");
		}
		return -1;
	}
	// else if (recv_msg_buf.src != src){
	// if (DEBUG_NATIVE_SIDE) {
	// fprintf(mslog, "[ProActiveMPI.c] !!! ERROR: BAD PARAMETER src \n");}
	// return -1;
	// }
	else if ((tag != MPI_ANY_TAG) && (recv_msg_buf.tag != tag)) {
		if (DEBUG_NATIVE_SIDE) {
			fprintf(mslog, "[ProActiveMPI.c] !!! ERROR: BAD PARAMETER tag \n");
		}
		return -1;
	} else if (recv_msg_buf.pa_datatype != pa_datatype) {
		if (DEBUG_NATIVE_SIDE) {
			fprintf(mslog,
					"[ProActiveMPI.c] !!! ERROR: BAD PARAMETER datatype \n");
		}
		return -1;
	} else {
		length = get_proactive_buffer_length(count, pa_datatype);

		if (length < 0) {
			if (DEBUG_NATIVE_SIDE) {
				fprintf(mslog, "[ProActiveMPI.c] !!! BAD DATATYPE \n");
			}
			return -1;
		}

		memcpy(r->buf, recv_msg_buf.data, length);
	}
	return 0;
}

/*
 * ProActiveTest
 */
int ProActiveTest(ProActiveMPI_Request *r, int* finished) {
	msg_t recv_msg_buf;
	int error = -1;
	//	int pms;
	int length;
	if (DEBUG_STMT) {
		// clear buffers in debug mode to avoid valgrind warnings
		init_msg_t(&recv_msg_buf);
	}

	int idjob = (*r).idjob;
	int tag = (*r).tag;
	int count = (*r).count;
	int pa_datatype = type_conversion_MPI_to_proactive(r->datatype);

	//	pms= sizeof(msg_t) - sizeof(recv_msg_buf.TAG) - sizeof(recv_msg_buf.data);

	error = msgrcv(S2C_Q_ID, &recv_msg_buf, get_payload_size(&recv_msg_buf),
			PROACTIVE_KEY, IPC_NOWAIT);
	while (error < 0) {
		strerror(errno);
		if (DEBUG_NATIVE_SIDE) {
			fprintf(mslog,
					"[ProActiveMPI.c] !!! ERROR: msgrcv error ERRNO = %d, \n",
					errno);
		}

		if (errno == EINTR) {
			if (DEBUG_NATIVE_SIDE) {
				fprintf(mslog, "[ProActiveMPI.c] !!! ERRNO = EINTR, \n");
			}
			error = msgrcv(S2C_Q_ID, &recv_msg_buf,
					get_payload_size(&recv_msg_buf), PROACTIVE_KEY, IPC_NOWAIT);
		}
		// no message in the queue
		else if (errno == ENOMSG) {
			if (DEBUG_NATIVE_SIDE) {
				fprintf(
						mslog,
						"[ProActiveMPI.c] !!! ERROR: msgrcv error ERRNO = %d, \n",
						errno);
			}
			// mv buffer pointer
			*finished = 0; // not recv yet
			return 0;
		} else {
			perror("[ProActiveMPI.c] ERROR");
			return -1;
		}
	}
	// Msg recved
	*finished = 1;

	if (DEBUG_NATIVE_SIDE) {
		fflush(mslog);
	}
	// filter
	if (recv_msg_buf.idjob != idjob) {
		if (DEBUG_NATIVE_SIDE) {
			fprintf(mslog, "[ProActiveMPI.c] !!! ERROR: BAD PARAMETER idjob \n");
		}
		return -1;
	}
	// else if (recv_msg_buf.src != src){
	// if (DEBUG_NATIVE_SIDE) {
	// fprintf(mslog, "[ProActiveMPI.c] !!! ERROR: BAD PARAMETER src \n");}
	// return -1;
	// }
	else if ((tag != MPI_ANY_TAG) && (recv_msg_buf.tag != tag)) {
		if (DEBUG_NATIVE_SIDE) {
			fprintf(mslog, "[ProActiveMPI.c] !!! ERROR: BAD PARAMETER tag \n");
		}
		return -1;
	} else if (recv_msg_buf.pa_datatype != pa_datatype) {
		if (DEBUG_NATIVE_SIDE) {
			fprintf(mslog,
					"[ProActiveMPI.c] !!! ERROR: BAD PARAMETER datatype \n");
		}
		return -1;
	} else {
		length = get_proactive_buffer_length(count, pa_datatype);

		if (length < 0) {
			if (DEBUG_NATIVE_SIDE) {
				fprintf(mslog, "[ProActiveMPI.c] !!! BAD DATATYPE \n");
			}
			return -1;
		}

		memcpy(r->buf, recv_msg_buf.data, length);
	}
	return 0;
}

/*
 * ProActiveIRecv
 */
int ProActiveIRecv(void* buf, int count, MPI_Datatype datatype, int src,
		int tag, int idjob, ProActiveMPI_Request *r) {
	msg_t recv_msg_buf;
	int error = -1;
	//	int pms;
	int length;
	if (DEBUG_STMT) {
		// clear buffers in debug mode to avoid valgrind warnings
		init_msg_t(&recv_msg_buf);
	}

	//	pms= sizeof(msg_t) - sizeof(recv_msg_buf.TAG) - sizeof(recv_msg_buf.data);

	error = msgrcv(S2C_Q_ID, &recv_msg_buf, get_payload_size(&recv_msg_buf),
			PROACTIVE_KEY, IPC_NOWAIT);

	while (error < 0) {
		strerror(errno);
		if (DEBUG_NATIVE_SIDE) {
			fprintf(mslog,
					"[ProActiveMPI.c] !!! ERROR: msgrcv error ERRNO = %d, \n",
					errno);
		}

		if (errno == EINTR) {
			if (DEBUG_NATIVE_SIDE) {
				fprintf(mslog, "[ProActiveMPI.c] !!! ERRNO = EINTR, \n");
			}
			error = msgrcv(S2C_Q_ID, &recv_msg_buf,
					get_payload_size(&recv_msg_buf), PROACTIVE_KEY, IPC_NOWAIT);
		}
		// no message in the queue
		else if (errno == ENOMSG) {
			if (DEBUG_NATIVE_SIDE) {
				fprintf(
						mslog,
						"[ProActiveMPI.c] !!! ERROR: msgrcv error ERRNO = %d, \n",
						errno);
			}
			r->buf = buf; // keep buf address in structure to update it later
			(*r).finished = 0; // nothing recv yet
			// keep parameters for further recv
			(*r).count = count;
			(*r).datatype = datatype;
			(*r).src = src;
			(*r).tag = tag;
			(*r).idjob = idjob;
			return 0;
		} else {
			perror("[ProActiveIRecv]!!! ERROR");
			return -1;
		}
	}

	if (DEBUG_NATIVE_SIDE) {
		fflush(mslog);
	}
	// filter
	if (recv_msg_buf.idjob != idjob) {
		if (DEBUG_NATIVE_SIDE) {
			fprintf(mslog, "[ProActiveMPI.c] !!! ERROR: BAD PARAMETER idjob \n");
		}
		return -1;
	}
	// else if (recv_msg_buf.src != src){
	// if (DEBUG_NATIVE_SIDE) {
	// fprintf(mslog, "[ProActiveMPI.c] !!! ERROR: BAD PARAMETER src \n");}
	// return -1;
	// }
	else if (recv_msg_buf.tag != tag) {
		if (DEBUG_NATIVE_SIDE) {
			fprintf(mslog, "[ProActiveMPI.c] !!! ERROR: BAD PARAMETER tag \n");
		}
		return -1;
	} else if (recv_msg_buf.pa_datatype
			!= type_conversion_MPI_to_proactive(datatype)) {
		if (DEBUG_NATIVE_SIDE) {
			fprintf(mslog,
					"[ProActiveMPI.c] !!! ERROR: BAD PARAMETER datatype \n");
		}
		return -1;
	} else {
		length = get_mpi_buffer_length(count, datatype, sizeof(char));

		if (length < 0) {
			if (DEBUG_NATIVE_SIDE) {
				fprintf(mslog, "[ProActiveMPI.c] !!! BAD DATATYPE \n");
			}
			return -1;
		}

		memcpy(buf, recv_msg_buf.data, length);
	}

	return 0;

}
