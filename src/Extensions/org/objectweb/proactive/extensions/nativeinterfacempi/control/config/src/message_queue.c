#include "message_queue.h"
#include <pthread.h>

using namespace std;

list<msg_t_pa_mpi *> message_list;

pthread_mutex_t queue_mutex;
pthread_cond_t count_threshold_cv;

void init_pa_mpi_msg_queue() {
	pthread_mutex_init(&queue_mutex, NULL);
}

msg_t_pa_mpi * get_message(int count, ProActive_Datatype pa_datatype, int src, int tag, int idjob) {

	pthread_mutex_lock(&queue_mutex);
	//iterate over the list
	list<msg_t_pa_mpi *> ::iterator it;

	for(it=message_list.begin(); it!=message_list.end(); it++) {
      // the first message matching awaited caracteristics
	  // is the right one as queue is ordered
      if (is_awaited_message(idjob, src, tag, pa_datatype, count, *it)) {
	  msg_t_pa_mpi * match = *it;
	  message_list.remove(match);
	  pthread_mutex_unlock (&queue_mutex);
	  return match;
      }
	}

	pthread_cond_wait(&count_threshold_cv, &queue_mutex);
    pthread_mutex_unlock(&queue_mutex);
	return NULL;
}

void store_message(msg_t_pa_mpi * recv_msg_buf) {
	pthread_mutex_lock(&queue_mutex);
	message_list.push_back(copy_message(recv_msg_buf));
	pthread_cond_signal(&count_threshold_cv);
	pthread_mutex_unlock(&queue_mutex);
}
