#include <pthread.h>
#include <stdio.h>
#include "native_layer.h"
#include "ProActiveMPI_Message.h"
#include "message_queue.h"

#define NUM_THREADS     1

void * read_from_native_layer(void *threadid)
{
	int ret = 1;
	msg_t_pa_mpi message;

	while (ret >= 0) {
		void * data_ptr = NULL;
		int length = -1;
		// wait for a message from the native interface
		if ((ret = recv_message(&length, &data_ptr)) < 0) {
			printf("ERROR recv_message from native interface failed\n");
		} else {
			convert_to_msg_t_pa_mpi(length, data_ptr, &message);
			// when got one store it in the message_queue
			store_message(&message);
		}
	}

	pthread_exit(NULL);
}

void start_thread()
{
   pthread_t threads[NUM_THREADS];
   int rc, t;
   for(t=0; t<NUM_THREADS; t++){
      printf("In main: creating thread %d\n", t);
      rc = pthread_create(&threads[t], NULL, read_from_native_layer, (void *)t);
      if (rc){
         printf("ERROR; return code from pthread_create() is %d\n", rc);
         exit(-1);
      }
   }
}
