#ifndef COMMON_DEFINITION_H_
#define COMMON_DEFINITION_H_

#define DEBUG_PAMPI_LAYER 1

#define DEBUG_PRINT_PAMPI_LAYER(f, statement) if (DEBUG_PAMPI_LAYER) {if (f != NULL) {statement; fflush(f);}}

#define DEBUG_LOG_OUTPUT_DIR "/tmp/proactive_native"

FILE * open_debug_log(char *path, int rank, char * prefix);

#endif /*COMMON_DEFINITION_H_*/
