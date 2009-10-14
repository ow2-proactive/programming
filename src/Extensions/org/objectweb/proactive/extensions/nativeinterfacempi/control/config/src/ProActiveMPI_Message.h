#ifndef PROACTIVE_MPI_MESSAGE_H_
#define PROACTIVE_MPI_MESSAGE_H_


#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <mpi.h>

//#define MSG_DATA_SIZE 8020
#define MET_SIZE 128

//#define ANY_SRC -2
//#define ANY_TAG -1

/**********************************************/
/*  Message Types                             */
/**********************************************/

#define MSG_SEND			2
#define MSG_RECV			4
#define MSG_INIT			6
#define MSG_ALLSEND       	8
#define MSG_FINALIZE		10
#define MSG_SEND_PROACTIVE	12
#define MSG_SCATTER			14
#define MSG_BCAST			16

/**********************************************/
/*  Message serialization offsets             */
/**********************************************/

#define MSG_TYPE_OFFSET 0
#define SRC_IDJOB_OFFSET (MSG_TYPE_OFFSET + sizeof(int))
#define DEST_IDJOB_OFFSET (SRC_IDJOB_OFFSET + sizeof(int))
#define COUNT_OFFSET (DEST_IDJOB_OFFSET + sizeof(int))
#define SRC_RANK_OFFSET (COUNT_OFFSET + sizeof(int))
#define DEST_RANK_OFFSET (SRC_RANK_OFFSET + sizeof(int))
#define MSG_TAG_OFFSET (DEST_RANK_OFFSET + sizeof(int))
#define PA_DATATYPE_OFFSET (MSG_TAG_OFFSET + sizeof(int))
#define DATA_PTR_OFFSET (PA_DATATYPE_OFFSET + sizeof(int))
#define METHOD_OFFSET (DATA_PTR_OFFSET + sizeof(char*))
#define DATA_OFFSET (METHOD_OFFSET + MET_SIZE)

/**********************************************/
/*  Datatype definition                       */
/**********************************************/

#define CONV_MPI_PROACTIVE_NULL ((ProActive_Datatype) 0)
#define CONV_MPI_PROACTIVE_CHAR ((ProActive_Datatype) 1)
#define CONV_MPI_PROACTIVE_UNSIGNED_CHAR ((ProActive_Datatype) 2)
#define CONV_MPI_PROACTIVE_BYTE ((ProActive_Datatype) 3)
#define CONV_MPI_PROACTIVE_SHORT ((ProActive_Datatype) 4)
#define CONV_MPI_PROACTIVE_UNSIGNED_SHORT ((ProActive_Datatype) 5)
#define CONV_MPI_PROACTIVE_INT ((ProActive_Datatype) 6)
#define CONV_MPI_PROACTIVE_UNSIGNED ((ProActive_Datatype) 7)
#define CONV_MPI_PROACTIVE_LONG ((ProActive_Datatype) 8)
#define CONV_MPI_PROACTIVE_UNSIGNED_LONG ((ProActive_Datatype) 9)
#define CONV_MPI_PROACTIVE_FLOAT ((ProActive_Datatype) 10)
#define CONV_MPI_PROACTIVE_DOUBLE ((ProActive_Datatype) 11)
#define CONV_MPI_PROACTIVE_LONG_DOUBLE ((ProActive_Datatype) 12)
#define CONV_MPI_PROACTIVE_LONG_LONG_INT ((ProActive_Datatype) 13)
#define CONV_MPI_PROACTIVE_COMPLEX ((ProActive_Datatype) 14)
#define CONV_MPI_PROACTIVE_DOUBLE_COMPLEX ((ProActive_Datatype) 15)

#define GET_MPI_BUFFER_LG(pa_datatype, type, native_type, count) if (pa_datatype == type) { return sizeof(native_type) * count;}

/**********************************************/
/*  Datatype definition                       */
/**********************************************/

typedef int ProActive_Datatype;

typedef struct _msg {	// to be put into common header file "javampi.h"
  int msg_type, src_idjob, dest_idjob, count, src_rank, dest_rank, msg_tag;
  ProActive_Datatype pa_datatype;
  char * data; /* 8 */
  char method[MET_SIZE]; /* 128 */
} msg_t_pa_mpi ;

/**********************************************/
/*  Message Utils		                      */
/**********************************************/

msg_t_pa_mpi * copy_message(msg_t_pa_mpi * message);

void free_msg_t_pa_mpi(msg_t_pa_mpi * recv_msg_buf);

int get_mpi_buffer_length(int count, MPI_Datatype datatype, int byte_size);

int get_pa_mpi_message_size(int payload_size);

int get_pa_mpi_typed_message_size(int count, MPI_Datatype mpi_datatype);

int same_MPI_Datatype(MPI_Datatype datatype1, MPI_Datatype datatype2);

int get_proactive_buffer_length(int count, ProActive_Datatype datatype);

MPI_Datatype type_conversion_proactive_to_MPI(ProActive_Datatype datatype);

ProActive_Datatype type_conversion_MPI_to_proactive(MPI_Datatype datatype);

void print_msg_t_pa_mpi(msg_t_pa_mpi * msg);


int is_awaited_message(int idjob, int src, int tag, ProActive_Datatype pa_datatype,
					   int count, msg_t_pa_mpi * message);

/**********************************************/
/*  Message Serialization                     */
/**********************************************/

int get_msg_type(void * serialization_ptr);

int get_count(void * serialization_ptr);

int get_src_rank(void * serialization_ptr);

int get_src_idjob(void * serialization_ptr);

int get_dest_rank(void * serialization_ptr);

int get_pa_datatype(void * serialization_ptr);

int get_msg_tag(void * serialization_ptr);

int get_dest_idjob(void * serialization_ptr);

void * get_data(void * serialization_ptr);

void set_msg_type(void * serialization_ptr, int msg_type);

void set_count(void * serialization_ptr, int count);

void set_src_rank(void * serialization_ptr, int src_rank);

void set_dest_rank(void * serialization_ptr, int dest_rank);

void set_pa_datatype(void * serialization_ptr, int pa_datatype);

void set_msg_tag(void * serialization_ptr, int msg_tag);

void set_src_idjob(void * serialization_ptr, int src_idjob);

void set_dest_idjob(void * serialization_ptr, int dest_idjob);

void convert_to_msg_t_pa_mpi(int length, void * data_ptr, msg_t_pa_mpi * recv_msg_buf);

void serialize_send_message(void * data_buffer, int count, MPI_Datatype mpi_datatype,
					   int src_rank, int dest_rank, int src_idjob, int dest_idjob, int msg_tag,
					   int * serialization_length, void ** serialization_ptr);

void serialize_bcast_message(int nb_send, void * sendbuf, int count, int myRank, int my_job_id,
							 int * pa_rank_array, MPI_Datatype datatype, int msg_tag,
							 int * serialized_msg_length, void ** serialized_ptr);

void serialize_scatter_message(int nb_send, void ** data_buffer, int src_rank, int src_idjob, int * pa_rank_array,
							   int * count_array, MPI_Datatype mpi_datatype, int msg_tag,
					           int * serialization_length, void ** serialization_ptr);

void serialize_init_message(int src_idjob, int src_rank, int * serialization_length, void ** serialization_ptr);

void serialize_finalize_message(int src_idjob, int src_rank, int * serialization_length, void ** serialization_ptr);

#endif /*PROACTIVE_MPI_MESSAGE_H_*/
