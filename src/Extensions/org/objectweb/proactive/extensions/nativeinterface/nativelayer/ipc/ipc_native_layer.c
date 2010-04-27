/*
 * ################################################################
 *
* ProActive Parallel Suite(TM): The Java(TM) library for
*    Parallel, Distributed, Multi-Core Computing for
*    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <unistd.h>
#include <signal.h>
#include <sys/time.h>
#include <errno.h>
#include <sys/sem.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <time.h>
#include "native_layer.h"
#include "ipc_native_layer.h"

//########################################
//####### Data Structure Variables  ######
//########################################

struct msqid_ds bufRecvStat, bufSendStat;

int SEND_Q_ID;
int RECV_Q_ID;
int C2S_Q_ID, S2C_Q_ID;

msg_t_ipc ipc_msg_to_send;
msg_t_ipc ipc_msg_to_recv;

//IDs of the semaphore set.
int sem_set_id_java;
int sem_set_id_native;

//cause the key may change regarding process
int TAG_S_KEY, TAG_R_KEY;

FILE * mslog;
// for the static proActiveSendRequest
//JNIEnv *backup_env;


/*****************************************************************/
/*****************************************************************/
/*           NATIVE LAYER IMPLEMENTATION                         */
/*****************************************************************/
/*****************************************************************/

 int init(int create) {
	int ret = 0;

	init_native_timer(&timer_acc);
	init_native_timer(&recv_acc);
	init_native_timer(&send_acc);

	//TODO automatic detection of side we are
	if (create) {
		if (DEBUG_NATIVE_LAYER_IPC) {
			char * path = (char *) malloc(MAX_NOM);
			path[0]='\0';
			strcpy(path, DEBUG_LOG_OUTPUT_DIR);
			mslog = open_debug_log(path, 69, "m_s_log");

			fprintf(mslog, "INIT invoked %d\n", create);
			fflush(mslog);

			if (mslog == NULL) {
				printf("ERROR WHILE OPENING FILE PATH= %s \n", path);
				perror("[Native_Layer] [IPC]> ERROR !!! Can't initialize logging files");
				return -1;
			}
		}

		if ((ret = init_ipc()) != 0) {
			return ret;
		}

		// we create and start the ipc queue
		if ((ret = closeAllQueues()) != 0) {
			return ret;
		}

		if ((ret = start_ipc_queue_java_side()) != 0) {
			return ret;
		}


	} else {
		// we only get access to the ipc queue
		if ((ret = start_ipc_queue_from_native()) != 0) {
			return ret;
		}
	}

	return ret;
}

 int terminate() {
	int ret = 0;

	if ((ret = closeQueue()) != 0) {
		return ret;
	}

	return ret;
}

 int recv_message_asynch(int * length, void ** data_ptr) {
	int err = 0;
	int ret_code = 0;

	/*C2S_Q_ID*/
	err = recv_ipc_message(RECV_Q_ID, TAG_R_KEY, 1, &ret_code, length, data_ptr);

	/* set length to 0 if no message was available */

	return err;
}

 int recv_message(int * length, void ** data_ptr) {
	int err = 0;
	int ret_code = 0;

	nativetime start = native_timer();

	/*C2S_Q_ID*/
	err = recv_ipc_message(RECV_Q_ID, TAG_R_KEY, 0, &ret_code, length, data_ptr);

	acc_native_time(&recv_acc, diff(start, native_timer()));

//	recv_acc += diff(start, native_timer());
	return err;
}

 int send_message(int length, void * data_ptr) {
	/*S2C_Q_ID*/
	int ret = 0;
	nativetime start = native_timer();

	ret = send_message_to_ipc(SEND_Q_ID, TAG_S_KEY, length, data_ptr);

//	send_acc += diff(start, native_timer());

	acc_native_time(&send_acc, diff(start, native_timer()));

	return ret;
}

/**********************************************/
/*  IPC message utils                         */
/**********************************************/

int get_data_payload() {
	return IPC_MSG_DATA_SIZE;
}

int get_message_payload() {
	return IPC_MSG_PAYLOAD_SIZE;
}

int is_splittable_message(int length) {
	return (get_data_payload() < length);
}

void print_msg_t_ipc(FILE * f, msg_t_ipc * ipc_msg_to_recv) {
	fprintf(f, "[IPC_MSG_T] msgtype %ld tag %d length %d\n",
			ipc_msg_to_recv->mtype, ipc_msg_to_recv->msg_type, ipc_msg_to_recv->length);
	fflush(f);
}


/*****************************************************************/
/*****************************************************************/
/*           IPC NATIVE LAYER SEND MESSAGES                      */
/*****************************************************************/
/*****************************************************************/


void populate_message(msg_t_ipc * ipc_msg_to_send, long TAG, int msg_type, int length, void * data_ptr) {
	ipc_msg_to_send->mtype = TAG;
	ipc_msg_to_send->msg_type = msg_type;
	ipc_msg_to_send->length = length;

	/* copy data to message data buffer */
	memcpy(ipc_msg_to_send->data, data_ptr, length);
}

int send_raw_msg_to_ipc_queue(int id, void * send_msg_buf) {

	int error = -1;
	while (error < 0) {
		error = msgsnd(id, send_msg_buf, get_message_payload(), 0);

		if (error < 0) {
			if (errno == EINTR) {
				if (DEBUG_NATIVE_LAYER_IPC) {
					fprintf(mslog,
							"[Native_Layer] [IPC]> [send_raw_msg_to_ipc_queue] !!! WARNING: msgsnd EINTR\n");
					fflush(mslog);
				}
			} else {
				if (DEBUG_NATIVE_LAYER_IPC) {
					fprintf(mslog,
							"[Native_Layer] [IPC]> [send_raw_msg_to_ipc_queue] !!! ERROR: msgsnd error\n");
					fflush(mslog);
				}
				perror("[Native_Layer] [IPC]> [send_raw_msg_to_ipc_queue] ERROR ");
				return -1;
			}
		}
	}
	return 0;
}

int send_message_to_ipc(int qid, long tag, int length, void * data_ptr) {

	int remaining_length = -1;
	int length_to_send = -1;
	int msg_type = -1;
	int err = 0;

	void * tmp_data_ptr = data_ptr;

	if (length > get_data_payload()) {
		// splitted message
		msg_type = IPC_FRAGMENTED_MSG;
		length_to_send = get_data_payload();
		populate_message(&ipc_msg_to_send, tag, msg_type, length_to_send, tmp_data_ptr);

		// first fragmented message hold the total length
		ipc_msg_to_send.length = length;
	} else {
		//regular message
		msg_type = IPC_COMPLETE_MSG;
		length_to_send = length;
		populate_message(&ipc_msg_to_send, tag, msg_type, length_to_send, tmp_data_ptr);
	}

	remaining_length = length - get_data_payload();

//	ipc_msg_to_send.timer = native_timer();

	if ((err = send_raw_msg_to_ipc_queue(qid, &ipc_msg_to_send)) < 0) {
		return err;
	}

	while (remaining_length > 0) {
		// update data ptr
		tmp_data_ptr = (((char *) tmp_data_ptr) + length_to_send);
		length_to_send = (remaining_length - get_data_payload());

		if (length_to_send <= 0) {
			msg_type = IPC_FRAGMENTED_END_MSG;
			length_to_send = remaining_length;
		} else {
			/*msg_type == IPC_FRAGMENTED_MSG*/
			length_to_send = get_data_payload();
		}

		populate_message(&ipc_msg_to_send, tag, msg_type, length_to_send, tmp_data_ptr);

		if ((err = send_raw_msg_to_ipc_queue(qid, &ipc_msg_to_send)) < 0) {
			return err;
		}

		remaining_length = (remaining_length - get_data_payload());
	}

	return err;
}

/*****************************************************************/
/*****************************************************************/
/*           IPC NATIVE LAYER RECV MESSAGE			             */
/*****************************************************************/
/*****************************************************************/

int recv_raw_msg_from_ipc_queue(void * ipc_msg_to_recv, int qid, long tag, int no_wait, int * ret_code) {
	int error = 0, not_received = 1;
	int flag = no_wait ? IPC_NOWAIT : 0;

	do {
		error = msgrcv(qid, ipc_msg_to_recv, get_message_payload(), tag, flag);
		*ret_code = ((error < 0) ? errno : 0);

		if (DEBUG_NATIVE_LAYER_IPC) {
			fprintf(
					mslog,
					"[Native_Layer] [IPC]> [recv_raw_msg_from_ipc_queue] msgrcv received read %d, errno %d, \n",
					error, *ret_code);
		}

		if ((error < 0) && (*ret_code != ENOMSG) && (*ret_code != EINTR)) {
			if (DEBUG_NATIVE_LAYER_IPC) {
				fprintf(
						mslog,
						"[Native_Layer] [IPC]> [recv_raw_msg_from_ipc_queue] !!! ERROR: msgrcv error ERRNO = %d, \n",
						*ret_code);
				fprintf(
						mslog,
						"[Native_Layer] [IPC]> [recv_raw_msg_from_ipc_queue] ERROR dump: qid %d, msg_type %ld, size %d\n",
						qid, tag, get_message_payload());
				fprintf(mslog,
						"[Native_Layer] [IPC]> [recv_raw_msg_from_ipc_queue] PERROR STR :: %s",
						strerror(*ret_code));
			}
			// we do not display error message if errno == ENOMSG
			// as it means no_wait = true and it's not a runtime error.
			// however we still return error < 0 as it indicates to upper layer
			// no message has been received.
			return -1;
		}

		// else pas d'erreur, erreur = ENOMSG, erreur = EINTR;
		not_received = (*ret_code == EINTR);
	} while (not_received); // iterate if we've been interrupted

	return error;
}

int recv_ipc_message(int qid, long tag, int no_wait, int * ret_code, int * length, void ** data_ptr) {

	int err = recv_raw_msg_from_ipc_queue(&ipc_msg_to_recv, qid, tag, no_wait, ret_code);

	if (err < 0) {
		if (*ret_code == ENOMSG)  {
			*length = 0;
			*data_ptr = NULL;
		}
		return err;
	}

//	nativetime timer_start = ipc_msg_to_recv.timer;

	*length = ipc_msg_to_recv.length;

	// initialize data buffer
	if ((*data_ptr = (char *) calloc(ipc_msg_to_recv.length, sizeof(char))) < 0) {
		*length = -1;
		perror("MALLOC ERROR");
		return -1; //TODO errno ?
	}

	// we have received a message
	if (ipc_msg_to_recv.msg_type == IPC_FRAGMENTED_MSG) {
		// the message is splitted in several part we need to get remaining parts from the queue.
		// WARNING whether we are in no_wait mode or not, we must always finish to read a splitted message !!!
//		int length_to_recv = ipc_msg_to_recv.length;
		int receive = 1;
		char * data_frag_ptr;
		msg_t_ipc recv_splitted_msg;

		if (DEBUG_NATIVE_LAYER_IPC) {
			fprintf(mslog,
					"[Native_Layer] [IPC]> [recv_ipc_message] [BEGIN] Receiving splitted message count:%d\n", ipc_msg_to_recv.length);
		}

		data_frag_ptr = (char *) *data_ptr;

		// copy incoming data
		memcpy(data_frag_ptr, ipc_msg_to_recv.data, get_data_payload());
		data_frag_ptr += get_data_payload();

		while (receive == 1) {
			if ((err = recv_raw_msg_from_ipc_queue(&recv_splitted_msg, qid, tag, 0, ret_code)) < 0) {
				return err;
			}

			if (DEBUG_NATIVE_LAYER_IPC) {
				fprintf(mslog, "[Native_Layer] [IPC]> [recv_ipc_message] Received splitted message \n");
			}

			// memcpy retrieved data message
			memcpy(data_frag_ptr, recv_splitted_msg.data, recv_splitted_msg.length);

			// shifting data pointer
			data_frag_ptr += recv_splitted_msg.length;

			// we receive 'data_length' bytes, we shift the pointer for msg to come.

			if (recv_splitted_msg.msg_type == IPC_FRAGMENTED_END_MSG) {
				// end of splitted message
				receive = 0;
				// We build the merged message
				ipc_msg_to_recv.msg_type = IPC_COMPLETE_MSG;

				if (DEBUG_NATIVE_LAYER_IPC) {
					fprintf(mslog,
							"[Native_Layer] [IPC]> [recv_ipc_message] [END] Receiving splitted message DUMP :\n");
					print_msg_t_ipc(mslog, &ipc_msg_to_recv);
				}
			}
		}
	} else {
		// updating buffer
		memcpy(*data_ptr, ipc_msg_to_recv.data, ipc_msg_to_recv.length);

		if (DEBUG_NATIVE_LAYER_IPC) {
			fprintf(mslog, "[Native_Layer] [IPC]> [recv_ipc_message] Received message \n");
			print_msg_t_ipc(mslog, &ipc_msg_to_recv);
		}
	}



//	long time = diff(timer_start, native_timer());
//	if (time > timer_max) {
//		timer_max = time;
//	}
//
//	if (time < timer_min) {
//		timer_min = time;
//	}

//    timer_acc += time;
//    acc_native_time(&timer_acc, diff(timer_start, native_timer()));
    timer_nb_call++;


	return 0;
}

/*****************************************************************/
/*****************************************************************/
/*           IPC NATIVE LAYER INIT/TERMINATE                     */
/*****************************************************************/
/*****************************************************************/
int start_ipc_queue_java_side() {
	int ret = -1;

	/*S2C_Q_ID*/
	if ((ret = init_queue_java_side(&SEND_Q_ID, S2C_KEY, S2C02_KEY, &TAG_S_KEY)) != 0) {
		return ret;
	}
	/*C2S_Q_ID*/
	if ((ret = init_queue_java_side(&RECV_Q_ID, C2S_KEY, C2S02_KEY, &TAG_R_KEY)) != 0) {
		return ret;
	}

	return ret;
}

int init_queue_java_side(int * queue_id, int queue_key,
							  int queue_key2,  int * out_queue_key) {
	sem_lock(sem_set_id_java);
	if (DEBUG_NATIVE_LAYER_IPC) {fprintf(mslog,"[Native_Layer] [IPC]> initialize queue! \n"); fflush(mslog);}
	// initialize server->client queue (oa->mpi)
	if ((*queue_id = msg_get(queue_key)) == -1){
		perror("[Native_Layer] [IPC]> !!! one initRecvQueue exists try to initialize second one");
		if ((*queue_id = msg_get(queue_key2)) == -1){
			perror("[Native_Layer] [IPC]> !!! ERROR in second initQueue ");
			return -1;
		}else{
			*out_queue_key=queue_key2;
			if (DEBUG_NATIVE_LAYER_IPC) {fprintf(mslog,"[Native_Layer] [IPC]> Second queue initialized:: QUEUE ID =  %d! \n", *queue_id); fflush(mslog);}
		}
	}
	else{
		*out_queue_key=queue_key;
		if (DEBUG_NATIVE_LAYER_IPC) {fprintf(mslog,"[Native_Layer] [IPC]> First queue initialized ! :: QUEUE ID =  %d! \n", *queue_id); fflush(mslog);}
	}
	if (DEBUG_NATIVE_LAYER_IPC_STAT) {msg_stat(*queue_id, &bufRecvStat);}
	sem_unlock(sem_set_id_java);

	return 0;
}

int init_ipc () {
	int     rc;				// return value of system call
	union semun semap;       /* semaphore value, for semctl().     */
	//TODO r some random stuff

	// REMOVE SEMAPHORES if exist
	if (semctl(sem_set_id_java, 0, IPC_RMID, semap) == -1) {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "can not remove semphore SEM_ID_JAVA!");}
	}else {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "semaphore removed SEM_ID_JAVA\n");}
	}

	if (semctl(sem_set_id_native, 0, IPC_RMID, semap) == -1) {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "can not remove semphore SEM_ID_MPI!");}
	}else {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "semaphore removed SEM_ID_MPI\n");}
	}

	// create a semaphore set with ID 250, with one semaphore
    // in it, with access only to the owner.
    sem_set_id_java = semget(SEM_ID_JAVA, 1, IPC_CREAT | IPC_EXCL | S_IRUSR |
                 S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH);
    // if second process
    if (sem_set_id_java == -1) {
	sem_set_id_java = semget(SEM_ID_JAVA, 1, IPC_CREAT | S_IRUSR |
                 S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH);
	if  (sem_set_id_java == -1){
		perror("[Native_Layer] [IPC]> init: semget sem_set_id_java");
		exit(1);
	}
    }// first process
    else{
	/* initialize the first (and single) semaphore in our set to '1'. */
        semap.val = 1;
        rc = semctl(sem_set_id_java, 0, SETVAL, semap);
        if (rc == -1) {
		perror("[Native_Layer] [IPC]> init: semctl sem_set_id_java");
		exit(1);
        }
    }

    //  create a semaphore set with ID 350, with one semaphore
    // in it, with access only to the owner.
    sem_set_id_native = semget(SEM_ID_MPI, 1, IPC_CREAT | S_IRUSR |
                 S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH);
    if  (sem_set_id_native == -1){
	perror("[Native_Layer] [IPC]> init: semget");
	exit(1);
	}
    else{
		/* initialize the first (and single) semaphore in our set to '1'. */
        semap.val = 1;
        rc = semctl(sem_set_id_native, 0, SETVAL, semap);
	}

	return 0;
}

int start_ipc_queue_native_side(int queue_key, int queue_key2, int * queue_id, int * queue_tag) {
	struct msqid_ds bufstat;
	// accessing exclusively the queue
	if ((*queue_id = msgget(queue_key, ACCESS_PERM)) == -1) {
		perror("[Native_Layer] [IPC]> mssget");
		DEBUG_PRINT_NATIVE_SIDE(mslog, fprintf(mslog, "Cannot open receiving queue: %d   \n",*queue_id))
		return -1;
	} else {
		*queue_tag=queue_key;
		// check the pid of the last process which have accessed to the queue
		// if (pid <> 0) open this process is the second one
		msg_stat(*queue_id, &bufstat);
		if (bufstat.msg_lspid != 0) {
			if ((*queue_id = msgget(queue_key2, ACCESS_PERM)) == -1) {
				perror("[Native_Layer] [IPC]> msgget");
				DEBUG_PRINT_NATIVE_SIDE(mslog, fprintf(mslog, "Cannot open the second receiving queue: %d   \n",*queue_id))
				return -1;
			} else {
				*queue_tag=queue_key2;
				DEBUG_PRINT_NATIVE_SIDE(mslog, fprintf(mslog, "Second Receiving Queue %d successfully opened \n ",*queue_id))
			}
		}
		DEBUG_PRINT_NATIVE_SIDE(mslog, fprintf(mslog, "Receiving Queue %d successfully opened \n ",*queue_id))
	}
	return 0;
}


int start_ipc_queue_from_native() {
	int error;
	// semaphore set ID.
	int sem_set_id_native;
	struct semid_ds test;

	// get the mpi semaphore
	sem_set_id_native = semget(SEM_ID_MPI, 1, IPC_CREAT | S_IRUSR | S_IWUSR
			| S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH);
	if (sem_set_id_native == -1) {
		perror("[Native_Layer] [IPC]> semget");
		exit(1);
	}

	semctl(sem_set_id_native, 0, IPC_STAT, &test);

	DEBUG_PRINT_NATIVE_SIDE(mslog, fprintf(mslog, "Block Semaphore  \n"));

	// first process lock the semaphore
	sem_lock(sem_set_id_native);

	/*C2S_Q_ID*/
	error = start_ipc_queue_native_side(C2S_KEY, C2S02_KEY, &SEND_Q_ID, &TAG_S_KEY);
	C2S_Q_ID = SEND_Q_ID;
	if (error < 0) {
		return error;
	}

	/*S2C_Q_ID*/
	error = start_ipc_queue_native_side(S2C_KEY, S2C02_KEY, &RECV_Q_ID, &TAG_R_KEY);
	S2C_Q_ID = RECV_Q_ID;
	if (error < 0) {
		return error;
	}

	// unlock the semaphore
	sem_unlock(sem_set_id_native);
	DEBUG_PRINT_NATIVE_SIDE(mslog, fprintf(mslog, "UnBlock Semaphore  \n"))

	return 0;
}

int closeQueue() {

	// print timers
	printf("nbcall: %d, timer_acc %ld:%ld send_acc %ld:%ld recv_acc %ld:%ld\n", timer_nb_call,
			timer_acc.tv_sec ,timer_acc.tv_nsec,
			send_acc.tv_sec, send_acc.tv_nsec,
			recv_acc.tv_sec, recv_acc.tv_nsec);
//	printf("nbcall: %d acc %ld min %ld max %ld send_acc %ld recv_acc %ld\n", timer_nb_call, timer_acc, timer_min, timer_max, send_acc, recv_acc);

	// REMOVE MESSAGE QUEUES
	if (msgctl(SEND_Q_ID, IPC_RMID, NULL) == -1) {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "can not remove queue SEND_Q_ID!");}
	}else {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "message queues removed SEND_Q_ID\n");}
	}

	if (msgctl(RECV_Q_ID, IPC_RMID, NULL) == -1) {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "can not remove queue RECV_Q_ID!");}
	}else {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "message queues removed RECV_Q_ID\n");}
	}

	if (msgctl(C2S_Q_ID, IPC_RMID, NULL) == -1) {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "can not remove queue C2S_KEY!");}
	}else {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "message queues removed C2S_KEY\n");}
	}

	if (msgctl(S2C_Q_ID, IPC_RMID, NULL) == -1) {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "can not remove queue S2C_KEY!");}
	}else {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "message queues removed S2C_KEY\n");}
	}
	// REMOVE SEMAPHORES
	if (semctl(sem_set_id_java, 0, IPC_RMID, NULL) == -1) {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "can not remove semphore SEM_ID_JAVA!");}
	}else{
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "semaphore removed SEM_ID_JAVA\n");}
	}

	if (semctl(sem_set_id_native, 0, IPC_RMID, NULL) == -1) {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "can not remove semphore SEM_ID_MPI!");}
	}else {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "semaphore removed SEM_ID_MPI\n");}
	}
	if (DEBUG_NATIVE_LAYER_IPC){
		fflush(mslog);
	}
	return 0;
}

int closeAllQueues() {
	int msqid1, msqid2;

	if (DEBUG_NATIVE_LAYER_IPC) {
		fprintf(mslog, "[BEGIN] Trying to close previously opened communication structures\n");
	}

	// REMOVE MESSAGE QUEUES
	if ((msqid1 = msgget(C2S_KEY, ACCESS_PERM )) == -1) {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "Cannot open queue C2S_KEY!\n");}
	}else {
		if (msgctl(msqid1, IPC_RMID, NULL) == -1) {
			if (DEBUG_NATIVE_LAYER_IPC){
				fprintf(mslog, "can not remove queue C2S_KEY!");}
		}else {
			if (DEBUG_NATIVE_LAYER_IPC){
				fprintf(mslog, "message queues removed C2S_KEY\n");}
		}
	}
	if ((msqid1 = msgget(C2S02_KEY, ACCESS_PERM )) == -1) {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "Cannot open queue C2S02_KEY!\n");}
	}else {
		if (msgctl(msqid1, IPC_RMID, NULL) == -1) {
			if (DEBUG_NATIVE_LAYER_IPC){
				fprintf(mslog, "can not remove queue C2S02_KEY!");}
		}else {
			if (DEBUG_NATIVE_LAYER_IPC){
				fprintf(mslog, "message queues removed C2S02_KEY\n");}
		}
	}
	if ((msqid2 = msgget(S2C_KEY, ACCESS_PERM )) == -1) {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "Cannot open queue S2C_KEY!\n");}
	}else {
		if (msgctl(msqid2, IPC_RMID, NULL) == -1) {
			if (DEBUG_NATIVE_LAYER_IPC){
				fprintf(mslog, "can not remove queue S2C_KEY!");}
		}else {
			if (DEBUG_NATIVE_LAYER_IPC){
				fprintf(mslog, "message queues removed S2C_KEY\n");}
		}
	}
	if ((msqid2 = msgget(S2C02_KEY, ACCESS_PERM )) == -1) {
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "Cannot open queue S2C02_KEY!\n");}
	}else {
		if (msgctl(msqid2, IPC_RMID, NULL) == -1) {
			if (DEBUG_NATIVE_LAYER_IPC){
				fprintf(mslog, "can not remove queue S2C02_KEY!");}
		}else {
			if (DEBUG_NATIVE_LAYER_IPC){
				fprintf(mslog, "message queues removed S2C02_KEY\n");}
		}
	}


	if (DEBUG_NATIVE_LAYER_IPC) {
		fprintf(mslog, "[END]   Trying to close previously opened communication structures\n");
		fflush(mslog);
	}
	return 0;
}


/*****************************************************************/
/*****************************************************************/
/*           IPC NATIVE LAYER QUEUE UTILS                        */
/*****************************************************************/
/*****************************************************************/

int msg_get(int KEY){
	int id;
	if ((id = msgget(KEY, IPC_CREAT| IPC_EXCL | ACCESS_PERM )) == -1){
		if (DEBUG_NATIVE_LAYER_IPC){
			fprintf(mslog, "!!!ERROR Init: Cannot open queue! \n");
			fflush(mslog);
		}
	}
	return id;
}

void msg_stat(int msgid, struct msqid_ds * msg_info)
{
	int reval;
	reval=msgctl(msgid,IPC_STAT,msg_info);
	if(reval==-1)
	{
		if (DEBUG_NATIVE_LAYER_IPC){
		fprintf(mslog, "get msg info error\n");}
		return;
	}
	if (DEBUG_NATIVE_LAYER_IPC){
		fprintf(mslog, "\n");
		fprintf(mslog, "ID of queue is %d \n",msgid);
		fprintf(mslog, "current number of bytes on queue is %ld\n",msg_info->msg_cbytes);
		fprintf(mslog, "number of messages in queue is %d\n", (int) msg_info->msg_qnum);
		fprintf(mslog, "max number of bytes on queue is %d\n",(int) msg_info->msg_qbytes);
		fprintf(mslog, "pid of last msgsnd is %d\n",msg_info->msg_lspid);
		fprintf(mslog, "pid of last msgrcv is %d\n",msg_info->msg_lrpid);
		fprintf(mslog, "last msgsnd time is %s", ctime(&(msg_info->msg_stime)));
		fprintf(mslog, "last msgrcv time is %s", ctime(&(msg_info->msg_rtime)));
		fprintf(mslog, "last change time is %s", ctime(&(msg_info->msg_ctime)));
		fprintf(mslog, "msg uid is %d \n",msg_info->msg_perm.uid);
		fprintf(mslog, "msg gid is %d \n",msg_info->msg_perm.gid);
//		msg_info->msg_qbytes = MSQ_SIZE;
		reval=msgctl(msgid,IPC_SET,msg_info);
		if(reval==-1)
		{
			fprintf(mslog, "set msg info error\n");
			return;
		}
		fflush(mslog);
	}
}

FILE * open_debug_log(char *path, int rank, char * prefix) {
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

/////////////////////////////////////////////
////////////SEMAPHORE FUNCTIONS ////////////
/////////////////////////////////////////////

/*
 * function: sem_lock. locks the semaphore, for exclusive access to a resource.
 * input:    semaphore set ID.
 * output:   none.
 */

void sem_lock(int sem_set_id) {
	/* structure for semaphore operations.   */
	struct sembuf sem_op;
	int ret = -1;

	/* wait on the semaphore, unless it's value is non-negative. */
	sem_op.sem_num = 0;
	sem_op.sem_op = -1;
	sem_op.sem_flg = 0;

	if ((ret = semop(sem_set_id, &sem_op, 1)) < 0) {
		perror("sem_lock error ");
	}
}

/*
 * function: sem_unlock. un-locks the semaphore.
 * input:    semaphore set ID.
 * output:   none.
 */
void sem_unlock(int sem_set_id) {
	/* structure for semaphore operations.   */
	struct sembuf sem_op;
	int ret = -1;

	/* signal the semaphore - increase its value by one. */
	sem_op.sem_num = 0;
	sem_op.sem_op = 1; /* <-- Comment 3 */
	sem_op.sem_flg = 0;

	if ((ret = semop(sem_set_id, &sem_op, 1)) < 0) {
		perror("sem_unlock error ");
	}
}


