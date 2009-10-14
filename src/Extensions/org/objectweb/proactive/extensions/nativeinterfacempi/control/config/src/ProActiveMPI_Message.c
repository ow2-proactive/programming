#include "ProActiveMPI_Message.h"

int get_msg_type(void * serialization_ptr) {
	return *(int*)(((char *)serialization_ptr) + MSG_TYPE_OFFSET);
}

int get_count(void * serialization_ptr) {
	return *(int*)(((char *)serialization_ptr) + COUNT_OFFSET);
}

int get_src_rank(void * serialization_ptr) {
	return *(int*)(((char *)serialization_ptr) + SRC_RANK_OFFSET);
}

int get_dest_rank(void * serialization_ptr) {
	return *(int*)(((char *)serialization_ptr) + DEST_RANK_OFFSET);
}

int get_pa_datatype(void * serialization_ptr) {
	return *(int*)(((char *)serialization_ptr) + PA_DATATYPE_OFFSET);
}

int get_msg_tag(void * serialization_ptr) {
	return *(int*)(((char *)serialization_ptr) + MSG_TAG_OFFSET);
}

int get_src_idjob(void * serialization_ptr) {
	return *(int*)(((char *)serialization_ptr) + SRC_IDJOB_OFFSET);
}

int get_dest_idjob(void * serialization_ptr) {
	return *(int*)(((char *)serialization_ptr) + DEST_IDJOB_OFFSET);
}

void * get_data(void * serialization_ptr) {
	return (void *) (((char *)serialization_ptr) + DATA_OFFSET);
}

/*****************************************************************/
/*****************************************************************/
/*           SERIALIZED MESSAGE ACCESSORS                         */
/*****************************************************************/
/*****************************************************************/

void set_msg_type(void * serialization_ptr, int msg_type) {
	memcpy(((char *)serialization_ptr) + MSG_TYPE_OFFSET, &msg_type, sizeof(int));
}

void set_count(void * serialization_ptr, int count) {
	memcpy(((char *)serialization_ptr) + COUNT_OFFSET, &count, sizeof(int));
}

void set_src_rank(void * serialization_ptr, int src_rank) {
	memcpy(((char *)serialization_ptr) + SRC_RANK_OFFSET, &src_rank, sizeof(int));
}

void set_dest_rank(void * serialization_ptr, int dest_rank) {
	memcpy(((char *)serialization_ptr) + DEST_RANK_OFFSET, &dest_rank, sizeof(int));
}

void set_pa_datatype(void * serialization_ptr, int pa_datatype) {
	memcpy(((char *)serialization_ptr) + PA_DATATYPE_OFFSET, &pa_datatype, sizeof(int));
}

void set_msg_tag(void * serialization_ptr, int msg_tag){
	memcpy(((char *)serialization_ptr) + MSG_TAG_OFFSET, &msg_tag, sizeof(int));
}

void set_src_idjob(void * serialization_ptr, int src_idjob) {
	memcpy(((char *)serialization_ptr) + SRC_IDJOB_OFFSET, &src_idjob, sizeof(int));
}

void set_dest_idjob(void * serialization_ptr, int dest_idjob) {
	memcpy(((char *)serialization_ptr) + DEST_IDJOB_OFFSET, &dest_idjob, sizeof(int));
}

int get_pa_mpi_message_size(int payload_size) {
	return DATA_OFFSET + payload_size;
}


int get_pa_mpi_typed_message_size(int count, MPI_Datatype mpi_datatype) {
	return DATA_OFFSET + get_mpi_buffer_length(count, mpi_datatype, sizeof(char));
}


/*****************************************************************/
/*****************************************************************/
/*           MESSAGE SERIALIZATION                               */
/*****************************************************************/
/*****************************************************************/

void serialize_finalize_message(int src_idjob, int src_rank, int * serialization_length, void ** serialization_ptr) {

	*serialization_length = DATA_OFFSET;

	if ((*serialization_ptr = (void *) calloc(*serialization_length, sizeof(char))) == NULL) {
		perror("MALLOC FAILED");
	}

	set_msg_type(*serialization_ptr, MSG_FINALIZE);
	set_src_idjob(*serialization_ptr, src_idjob);
	set_dest_idjob(*serialization_ptr, -1);
	set_count(*serialization_ptr, 0);
	set_src_rank(*serialization_ptr, src_rank);
	set_dest_rank(*serialization_ptr, -1);
	set_msg_tag(*serialization_ptr, -1);
	set_pa_datatype(*serialization_ptr, -1);
}


void serialize_init_message(int src_idjob, int src_rank, int * serialization_length, void ** serialization_ptr) {

	*serialization_length = DATA_OFFSET;

	if ((*serialization_ptr = (void *) calloc(*serialization_length, sizeof(char))) == NULL) {
		perror("MALLOC FAILED");
	}
	set_msg_type(*serialization_ptr, MSG_INIT);
	set_src_idjob(*serialization_ptr, src_idjob);
	set_dest_idjob(*serialization_ptr, -1);
	set_count(*serialization_ptr, 0);
	set_src_rank(*serialization_ptr, src_rank);
	set_dest_rank(*serialization_ptr, -1);
	set_msg_tag(*serialization_ptr, -1);
	set_pa_datatype(*serialization_ptr, -1);
}

void serialize_send_message_internal(void * data_buffer, int count, MPI_Datatype mpi_datatype,
					   int src_rank, int dest_rank, int src_idjob, int dest_idjob, int msg_tag,
					   void ** serialization_ptr) {

	set_msg_type(*serialization_ptr, MSG_SEND);
	set_src_idjob(*serialization_ptr, src_idjob);
	set_dest_idjob(*serialization_ptr, dest_idjob);
	set_count(*serialization_ptr, count);
	set_src_rank(*serialization_ptr, src_rank);
	set_dest_rank(*serialization_ptr, dest_rank);
	set_msg_tag(*serialization_ptr, msg_tag);
	set_pa_datatype(*serialization_ptr, type_conversion_MPI_to_proactive(mpi_datatype));

	// no set for data * and method

	//appending data buffer at the end of serialization header
	void * data_ptr = ((char *) *serialization_ptr) + DATA_OFFSET;

	memcpy(data_ptr, data_buffer, get_mpi_buffer_length(count, mpi_datatype, sizeof(char)));
}

void serialize_send_message(void * data_buffer, int count, MPI_Datatype mpi_datatype,
					   int src_rank, int dest_rank, int src_idjob, int dest_idjob, int msg_tag,
					   int * serialization_length, void ** serialization_ptr) {

	*serialization_length = get_pa_mpi_typed_message_size(count, mpi_datatype);

	if ((*serialization_ptr = (void *) calloc(*serialization_length, sizeof(char))) == NULL) {
		perror("MALLOC FAILED");
		exit(-1);
	}

	serialize_send_message_internal(data_buffer, count, mpi_datatype, src_rank, dest_rank, src_idjob,
			dest_idjob, msg_tag, serialization_ptr);
}

void serialize_bcast_message(int nb_send, void * sendbuf, int count, int src_rank, int src_idjob,
							 int * pa_rank_array, MPI_Datatype mpi_datatype, int msg_tag,
							 int * serialization_length, void ** serialization_ptr) {

	char * data_ptr = NULL;
	int i = 0;
	int message_length = 0;
	*serialization_length = 0;

	message_length = get_pa_mpi_typed_message_size(count, mpi_datatype);

	/* MESSAGE_HEADER + msg_lengths + (nb_send * (encapsulated message)) */
	*serialization_length = get_pa_mpi_message_size(sizeof(int) + (nb_send * message_length));

	if ((*serialization_ptr = (void *) calloc(*serialization_length, sizeof(char))) == NULL) {
		perror("MALLOC FAILED");
		exit(-1);
	}

	set_msg_type(*serialization_ptr, MSG_BCAST);
	set_src_idjob(*serialization_ptr, src_idjob);
	set_dest_idjob(*serialization_ptr, -1);
	/*count is set to nb of individual send to perform in the bcast*/
	set_count(*serialization_ptr, nb_send);
	set_src_rank(*serialization_ptr, src_rank);
	set_dest_rank(*serialization_ptr, -1);
	set_msg_tag(*serialization_ptr, msg_tag);
	set_pa_datatype(*serialization_ptr, type_conversion_MPI_to_proactive(mpi_datatype));

	data_ptr = ((char *) *serialization_ptr) + DATA_OFFSET;
	memcpy(data_ptr, &message_length, sizeof(int));
	data_ptr += sizeof(int);

	// append messages to the end of the buffer
	while (i < nb_send) {
		serialize_send_message_internal(sendbuf, count, mpi_datatype,
							   src_rank, pa_rank_array[(2*i)+1], src_idjob, pa_rank_array[2*i], msg_tag,
							   (void **) &data_ptr);
		data_ptr += message_length;
        i++;
	}
}


void serialize_scatter_message(int nb_send, void ** data_buffer,
							   int src_rank, int src_idjob, int * pa_rank_array,
							   int * count_array, MPI_Datatype mpi_datatype, int msg_tag,
					           int * serialization_length, void ** serialization_ptr) {
	int i = 0;
	char * data_ptr = NULL;
	int * msg_length_array = NULL;
	int msg_length_array_size = (nb_send * sizeof(int));
	*serialization_length = 0;

	//TODO why don't we use a local array here ?
	if ((msg_length_array = (int *) calloc(nb_send, sizeof(int))) == NULL) {
			perror("MALLOC FAILED");
			exit(-1);
	}

	while(i < nb_send) {
		msg_length_array[i] = get_pa_mpi_typed_message_size(count_array[i], mpi_datatype);
		*serialization_length += msg_length_array[i];
		i++;
	}

	/* MESSAGE_HEADER + serialized_message_size_array +  (nb_send * (encapsulated message)) */
	*serialization_length = get_pa_mpi_message_size(msg_length_array_size + *serialization_length);

	if ((*serialization_ptr = (void *) calloc(*serialization_length, sizeof(char))) == NULL) {
		perror("MALLOC FAILED");
		exit(-1);
	}

	/*By convention all src/dest rank/jobid are set to -1 when message is a scatter one*/
	set_msg_type(*serialization_ptr, MSG_SCATTER);
	set_src_idjob(*serialization_ptr, -1);
	set_dest_idjob(*serialization_ptr, -1);
	/*count is set to nb of individual send to perform in the scatter*/
	set_count(*serialization_ptr, nb_send);
	set_src_rank(*serialization_ptr, -1);
	set_dest_rank(*serialization_ptr, -1);
	set_msg_tag(*serialization_ptr, msg_tag);
	set_pa_datatype(*serialization_ptr, type_conversion_MPI_to_proactive(mpi_datatype));

	//appending data buffer at the end of serialization header
	data_ptr = ((char *) *serialization_ptr) + DATA_OFFSET;
	memcpy(data_ptr, msg_length_array, msg_length_array_size);
	data_ptr += msg_length_array_size;

	// append messages to the end of the buffer
	i = 0;
	while (i < nb_send) {
		serialize_send_message_internal(data_buffer[i], count_array[i], mpi_datatype,
							   src_rank, pa_rank_array[(2*i)+1], src_idjob, pa_rank_array[2*i], msg_tag,
							   (void **) &data_ptr);
		data_ptr += msg_length_array[i];
        i++;
	}
	free(msg_length_array);
}


/*****************************************************************/
/*****************************************************************/
/*           MESSAGE UTILS			                             */
/*****************************************************************/
/*****************************************************************/

void print_msg_t_pa_mpi(msg_t_pa_mpi * recv_msg_buf) {
	printf("[MSG_T_PA_MPI] msg_type %d src_jobid %d dest_jobid %d count %d src_rank %d dest_rank %d msg_tag %d pa_datatype %d\n",
			recv_msg_buf->msg_type,
			recv_msg_buf->src_idjob,
			recv_msg_buf->dest_idjob,
			recv_msg_buf->count,
			recv_msg_buf->src_rank,
			recv_msg_buf->dest_rank,
			recv_msg_buf->msg_tag,
			recv_msg_buf->pa_datatype);
}

void convert_to_msg_t_pa_mpi(int length, void * data_ptr, msg_t_pa_mpi * recv_msg_buf) {

	recv_msg_buf->msg_type = get_msg_type(data_ptr);
	recv_msg_buf->src_idjob = get_src_idjob(data_ptr);
	recv_msg_buf->dest_idjob = get_dest_idjob(data_ptr);
	recv_msg_buf->count = get_count(data_ptr);
	recv_msg_buf->src_rank = get_src_rank(data_ptr);
	recv_msg_buf->dest_rank = get_dest_rank(data_ptr);
	recv_msg_buf->msg_tag = get_msg_tag(data_ptr);
	recv_msg_buf->pa_datatype = get_pa_datatype(data_ptr);

	void * msg_data_ptr = get_data(data_ptr);

	int data_length = get_proactive_buffer_length(recv_msg_buf->count, recv_msg_buf->pa_datatype);

	if (data_length == 0) {
		recv_msg_buf->data = NULL;
	} else {
		if ((recv_msg_buf->data = (char *) calloc(data_length, sizeof(char))) == NULL) {
			printf("MALLOC FAILED count %d datatype %d\n", recv_msg_buf->count, recv_msg_buf->pa_datatype);
			perror("MALLOC FAILED");
		}

		// we must make a copy of data
		memcpy(recv_msg_buf->data, msg_data_ptr, data_length);
	}
}

msg_t_pa_mpi * copy_message(msg_t_pa_mpi * message) {
	msg_t_pa_mpi * copy;
	if ((copy = (msg_t_pa_mpi *) malloc(sizeof(msg_t_pa_mpi))) == NULL) {
		perror("[ProActiveMPI_Recv] !!! ERROR : MALLOC FAILED");
		return NULL;
	}

	memcpy(copy, message, sizeof(msg_t_pa_mpi));

	// now we must also copy data
	int data_length = get_proactive_buffer_length(copy->count, copy->pa_datatype);

	if ((copy->data = (char *) calloc(data_length, sizeof(char))) == NULL) {
		perror("MALLOC FAILED");
		abort();
	}

	// we must make a copy of data
	memcpy(copy->data, message->data, data_length);

	return copy;
}

void free_msg_t_pa_mpi(msg_t_pa_mpi * recv_msg_buf) {
	// We no more need the message
	free(recv_msg_buf->data);
	free(recv_msg_buf);
}

int get_proactive_buffer_length(int count, ProActive_Datatype datatype) {
	MPI_Datatype mpi_datatype = type_conversion_proactive_to_MPI(datatype);
	return get_mpi_buffer_length(count, mpi_datatype, sizeof(char));
}

int is_awaited_message(int idjob, int src, int tag, ProActive_Datatype pa_datatype,
					   int count, msg_t_pa_mpi * message) {
	int awaited = 1;
	if ((idjob != MPI_ANY_SOURCE) && (message->src_idjob != idjob)) {
		awaited = 0;
	} else if ((src != MPI_ANY_SOURCE) && (message->src_rank != src)) {
		awaited = 0;
	} else if ((tag != MPI_ANY_TAG) && (message->msg_tag != tag)) {
		awaited = 0;
	} else if (message->pa_datatype != pa_datatype) {
		awaited = 0;
	} else if (message->count > count) {
		/* MPI_recv does not necessarily match count but should be greater or equal */
		awaited = 0;
	}

	return awaited;
}

/* If return type is MPI_DATATYPE_NULL then it is an error */
MPI_Datatype type_conversion_proactive_to_MPI(ProActive_Datatype datatype) {

	switch (datatype) {
	case CONV_MPI_PROACTIVE_CHAR:
		return MPI_CHAR;

	case CONV_MPI_PROACTIVE_UNSIGNED_CHAR:
		return MPI_UNSIGNED_CHAR;

	case CONV_MPI_PROACTIVE_BYTE:
		return MPI_BYTE;

	case CONV_MPI_PROACTIVE_SHORT:
		return MPI_SHORT;

	case CONV_MPI_PROACTIVE_UNSIGNED_SHORT:
		return MPI_UNSIGNED_SHORT;

	case CONV_MPI_PROACTIVE_INT:
		return MPI_INT;

	case CONV_MPI_PROACTIVE_UNSIGNED:
		return MPI_UNSIGNED;

	case CONV_MPI_PROACTIVE_LONG:
		return MPI_LONG;

	case CONV_MPI_PROACTIVE_UNSIGNED_LONG:
		return MPI_UNSIGNED_LONG;

	case CONV_MPI_PROACTIVE_FLOAT:
		return MPI_FLOAT;

	case CONV_MPI_PROACTIVE_DOUBLE:
		return MPI_DOUBLE;

	case CONV_MPI_PROACTIVE_LONG_DOUBLE:
		return MPI_LONG_DOUBLE;

	case CONV_MPI_PROACTIVE_LONG_LONG_INT:
		return MPI_LONG_LONG;

	case CONV_MPI_PROACTIVE_COMPLEX:
		return MPI_COMPLEX;

	case CONV_MPI_PROACTIVE_DOUBLE_COMPLEX:
		return MPI_DOUBLE_COMPLEX;

	case CONV_MPI_PROACTIVE_NULL:
		return MPI_DATATYPE_NULL;

	default:
		// Unknown data type
		return MPI_DATATYPE_NULL;
	}
}

int get_mpi_buffer_length(int count, MPI_Datatype datatype, int byte_size) {
	ProActive_Datatype pa_datatype = type_conversion_MPI_to_proactive(datatype);

	GET_MPI_BUFFER_LG(pa_datatype, CONV_MPI_PROACTIVE_INT, int, count)
	GET_MPI_BUFFER_LG(pa_datatype, CONV_MPI_PROACTIVE_UNSIGNED, unsigned int, count)
	GET_MPI_BUFFER_LG(pa_datatype, CONV_MPI_PROACTIVE_DOUBLE, double, count)
	GET_MPI_BUFFER_LG(pa_datatype, CONV_MPI_PROACTIVE_DOUBLE_COMPLEX, double, count)
	GET_MPI_BUFFER_LG(pa_datatype, CONV_MPI_PROACTIVE_SHORT, short, count)
	GET_MPI_BUFFER_LG(pa_datatype, CONV_MPI_PROACTIVE_UNSIGNED_SHORT, unsigned short, count)
	GET_MPI_BUFFER_LG(pa_datatype, CONV_MPI_PROACTIVE_CHAR, char, count)
	GET_MPI_BUFFER_LG(pa_datatype, CONV_MPI_PROACTIVE_UNSIGNED_CHAR, unsigned char, count)
	GET_MPI_BUFFER_LG(pa_datatype, CONV_MPI_PROACTIVE_BYTE, char, count)
	GET_MPI_BUFFER_LG(pa_datatype, CONV_MPI_PROACTIVE_LONG, long, count)
	GET_MPI_BUFFER_LG(pa_datatype, CONV_MPI_PROACTIVE_UNSIGNED_LONG, unsigned long, count)
	GET_MPI_BUFFER_LG(pa_datatype, CONV_MPI_PROACTIVE_FLOAT, float, count)
	GET_MPI_BUFFER_LG(pa_datatype, CONV_MPI_PROACTIVE_LONG_DOUBLE, long double, count)
	GET_MPI_BUFFER_LG(pa_datatype, CONV_MPI_PROACTIVE_LONG_LONG_INT, long int, count)

	if (pa_datatype == CONV_MPI_PROACTIVE_COMPLEX) {
		return sizeof(float) * 2;
	}

	if (pa_datatype == CONV_MPI_PROACTIVE_DOUBLE_COMPLEX) {
		return sizeof(double) * 2;
	}

    if (pa_datatype == CONV_MPI_PROACTIVE_NULL) {
	return 0;
	} else {
		printf("ERROR UNKNOW PROACTIVE_DATATYPE %d \n", pa_datatype);
		exit(-4);
	}

	return -1;
}

int same_MPI_Datatype(MPI_Datatype datatype1, MPI_Datatype datatype2) {
	return datatype1 == datatype2;
}

/* if method return MPI_DATATYPE_NULL then we are not able to convert the mpi datatype to a proactive one */
ProActive_Datatype type_conversion_MPI_to_proactive(MPI_Datatype datatype) {

	if (same_MPI_Datatype(datatype, MPI_CHAR) > 0) {
		return CONV_MPI_PROACTIVE_CHAR;
	}

	if (same_MPI_Datatype(datatype, MPI_UNSIGNED_CHAR) > 0) {
		return CONV_MPI_PROACTIVE_UNSIGNED_CHAR;
	}

	if (same_MPI_Datatype(datatype, MPI_BYTE) > 0) {
		return CONV_MPI_PROACTIVE_BYTE;
	}

	if (same_MPI_Datatype(datatype, MPI_SHORT) > 0) {
		return CONV_MPI_PROACTIVE_SHORT;
	}

	if (same_MPI_Datatype(datatype, MPI_UNSIGNED_SHORT) > 0) {
		return CONV_MPI_PROACTIVE_UNSIGNED_SHORT;
	}

	if (same_MPI_Datatype(datatype, MPI_INT) > 0) {
		return CONV_MPI_PROACTIVE_INT;
	}

	if (same_MPI_Datatype(datatype, MPI_UNSIGNED) > 0) {
		return CONV_MPI_PROACTIVE_UNSIGNED;
	}

	if (same_MPI_Datatype(datatype, MPI_LONG) > 0) {
		return CONV_MPI_PROACTIVE_LONG;
	}

	if (same_MPI_Datatype(datatype, MPI_UNSIGNED_LONG) > 0) {
		return CONV_MPI_PROACTIVE_UNSIGNED_LONG;
	}

	if (same_MPI_Datatype(datatype, MPI_FLOAT) > 0) {
		return CONV_MPI_PROACTIVE_FLOAT;
	}

	if (same_MPI_Datatype(datatype, MPI_DOUBLE) > 0) {
		return CONV_MPI_PROACTIVE_DOUBLE;
	}

	if (same_MPI_Datatype(datatype, MPI_LONG_DOUBLE) > 0) {
		return CONV_MPI_PROACTIVE_LONG_DOUBLE;
	}

	if (same_MPI_Datatype(datatype, MPI_LONG_LONG) > 0) {
		return CONV_MPI_PROACTIVE_LONG_LONG_INT;
	}

	if (same_MPI_Datatype(datatype, MPI_COMPLEX) > 0) {
		return CONV_MPI_PROACTIVE_COMPLEX;
	}

	if (same_MPI_Datatype(datatype, MPI_DOUBLE_COMPLEX) > 0) {
		return CONV_MPI_PROACTIVE_DOUBLE_COMPLEX;
	}

	return CONV_MPI_PROACTIVE_NULL;
}
