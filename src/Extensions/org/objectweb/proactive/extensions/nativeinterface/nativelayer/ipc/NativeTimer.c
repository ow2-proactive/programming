#include "NativeTimer.h"

/*
long native_timer() {
 struct timespec tp;
  if (clock_gettime (CLOCK_REALTIME, &tp) != 0){
    printf("error\n");
  }

  return tp.tv_nsec;
}


long diff(long start, long end)
{
	long res;

	if ((end-start)<0) {
		res = 1000000000+end-start;
	} else {
		res = end-start;
	}

	return res;
}*/


nativetime diff(nativetime start, nativetime end)
{
	nativetime temp;
	if ((end.tv_nsec-start.tv_nsec)<0) {
		temp.tv_sec = end.tv_sec-start.tv_sec-1;
		temp.tv_nsec = 1000000000+end.tv_nsec-start.tv_nsec;
	} else {
		temp.tv_sec = end.tv_sec-start.tv_sec;
		temp.tv_nsec = end.tv_nsec-start.tv_nsec;
	}
	return temp;
}

nativetime native_timer() {
 nativetime tp;
  if (clock_gettime (CLOCK_REALTIME, &tp) != 0){
    printf("error\n");
  }

  return tp;
}

void init_native_timer(nativetime * send_acc) {
	send_acc->tv_sec = 0;
	send_acc->tv_nsec = 0;

}
void acc_native_time(nativetime * send_acc, nativetime diff) {
	//TODO bug has nano seconds is not converted to seconds
	send_acc->tv_sec = send_acc->tv_sec + diff.tv_sec;
	send_acc->tv_nsec = send_acc->tv_nsec + diff.tv_nsec;
}

void print_timer(nativetime time) {
	printf("%ld:%ld\n", time.tv_sec ,time .tv_nsec);
}
