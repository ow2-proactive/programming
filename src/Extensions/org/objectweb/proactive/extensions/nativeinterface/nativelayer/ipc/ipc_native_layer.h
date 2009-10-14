#ifndef IPC_NATIVE_LAYER_H_
#define IPC_NATIVE_LAYER_H_
#include "NativeTimer.h"

/**********************************************/
/*  Data Structure definition                 */
/**********************************************/

#define DEBUG_NATIVE_LAYER_IPC 1
#define DEBUG_NATIVE_LAYER_IPC_STAT 1

#define DEBUG_PRINT_NATIVE_SIDE(f, statement) if (DEBUG_NATIVE_LAYER_IPC) {statement; fflush(f);}

#define IPC_COMPLETE_MSG 		111
#define IPC_FRAGMENTED_MSG 		222
#define IPC_FRAGMENTED_END_MSG 	333

//TODO 8192 should be MSGMAX
#define IPC_MSG_PAYLOAD_SIZE (8192 - sizeof(long))
//#define IPC_MSG_DATA_SIZE (IPC_MSG_PAYLOAD_SIZE - sizeof(int) - sizeof(int) - sizeof(nativetime))
#define IPC_MSG_DATA_SIZE (IPC_MSG_PAYLOAD_SIZE - sizeof(int) - sizeof(int))

#define ACCESS_PERM 0666

/* used as KEY of message queue */
#define C2S_KEY 6026
#define S2C_KEY	6020

/* used for a potentially second mpi process on same host */
#define C2S02_KEY 6032
#define S2C02_KEY 6038

#define SEM_ID_JAVA 250
#define SEM_ID_MPI  350

/* WARNING, don't forget to update IPC_MSG_DATA_SIZE if you change
 * this data structure */
typedef struct msgbuf_ {
     long mtype;    /* message type, must be > 0 */
     int msg_type;
     int length; /*effective length of data carried by data[] */
     char data[IPC_MSG_DATA_SIZE];  /* message data */
} msg_t_ipc;

nativetime timer_acc;
nativetime recv_acc;
nativetime send_acc;
//nativetimer timer_max = 0;
//nativetimer timer_min = 10000000;
int timer_nb_call = 0;

/**********************************************/
/*  IPC Queue initialization and termination  */
/**********************************************/

union semun {
	int val;
	struct semid_ds *buf;
	unsigned short int *array;
	struct seminfo *__buf;
};

void sem_unlock(int sem_set_id);
void sem_lock(int sem_set_id);

int init_ipc();

int start_ipc_queue_java_side();
int init_queue_java_side(int * queue_id, int queue_key, int queue_key2,  int * out_queue_key);

int start_ipc_queue_from_native();
int start_ipc_queue_native_side(int queue_key, int queue_key2, int * queue_id, int * queue_tag);

int closeQueue();
int closeAllQueues();

void msg_stat(int, struct msqid_ds *);
int msg_get(int);

void exit();
void perror();

/**********************************************/
/*  IPC message utils                         */
/**********************************************/
int get_data_payload();
int get_message_payload();
int is_splittable_message(int length);
void print_msg_t_ipc(FILE * f, msg_t_ipc * ipc_msg_to_recv);
FILE * open_debug_log(char *path, int rank, char * prefix);

void populate_message(msg_t_ipc * ipc_msg_to_send, long TAG, int msg_type, int length, void * data_ptr);

int send_raw_msg_to_ipc_queue(int id, void * send_msg_buf);
int send_message_to_ipc(int qid, long tag, int length, void * data_ptr);

int recv_raw_msg_from_ipc_queue(void * ipc_msg_to_recv, int qid, long tag, int no_wait, int * ret_code);
int recv_ipc_message(int qid, long tag, int no_wait, int * ret_code, int * length, void ** data_ptr);

#endif /*IPC_NATIVE_LAYER_H_*/
