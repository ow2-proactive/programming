#ifndef NATIVE_TIMER_H_
#define NATIVE_TIMER_H_

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <time.h>

typedef struct timespec nativetime;

nativetime native_timer();
void init_native_timer(nativetime * send_acc);
nativetime diff(nativetime start, nativetime end);
void acc_native_time(nativetime * send_acc, nativetime diff);
void print_timer(nativetime time);

#endif /*NATIVE_TIMER_H_*/
