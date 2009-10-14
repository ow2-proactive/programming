#ifndef NATIVE_LAYER_H_
#define NATIVE_LAYER_H_

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

//TODO change to /tmp/proactive_native_layer
#define DEBUG_LOG_OUTPUT_DIR "/tmp/proactive_native"
#define MAX_NOM 256

extern FILE * mslog;

int init(int creation_flag);

int terminate();

int recv_message(int * length, void ** data_ptr);

int recv_message_asynch(int * length, void ** data_ptr);

int send_message(int length, void * data_ptr);

#endif /*NATIVE_LAYER_H_*/

