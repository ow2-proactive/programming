#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <util.h>
#include <utmp.h>
#include <ctype.h>
#include <sys/wait.h>
#include <pthread.h>

int err_pipe[2];
int stayAlive = 1;

#define PASSWD_SIZE 512
/**
  This thread will read the error pipe and write it to
  standard error.
  The hack with stayAlive is necessary, because the process
  that writes to the pipe is changing into su (exec) and
  it is not able to close the pipe normally - so we will get
  0s for ever.
*/
void *errThread(){
  int amount;
  char ebuf[BUFSIZ];
  do {
    amount = read( err_pipe[0], &ebuf, BUFSIZ );
    if (amount > 0 ) {
      ebuf[amount] = '\0';
      fprintf( stderr, "%s", ebuf );
    }
  } while ( amount > 0 || ( amount == 0 && stayAlive ) );
}

/**
    MAC Specifics:
        su is located in /usr/bin/su
        su prints "Password:" on stdout (on Linux it is stderr)
        su does not acknowledge when the password is typed
        su does read stdin for commands, -c has to be used
  About:
    This program is a replacement for expect for the 'su' Mac command.
    It's underlying principles are the same, i.e. it will:
      1) fork a process with a pseudo terminal as standard input/output (as 'su' accepts the password only
    if it is input from a terminal device)
      2) the child process will redirect its error output to a pipe, and start 'su' (if the std.err. is not redirected
      it gets mixed together with the std.out - this is how forkpty works)
      3) the main process will than write the password to the std.in. of the 'su' process and wait for aknowledgement
      (if the password was wrong, the 'su' will exit with exit status 1 and so will this program terminate with e.s. 1)
      4) then it will write the command (may be made up of several arguments) passed to this program; since the terminal
      started inside the 'su' has to exit once the command has executes, this is wrapped into the form:
	"( _command_ _args..._ ); exit 0;"
      Thus it is crucial for the text inside the parantheses not to contain syntax errors, or illegal characters.
      Otherwise, the executbale may seem to hang, as the command line will never exit.
      5) after starting the user-specified command, the executable will forward the 'su' process' std.out to the output
      channel, and its std.err to the error channel.
      6) in the end, it will wait for the 'su' process to exit, and will return with it's exit value

  Compiling the code:
    Since this code has no special dependencies apart from libutil and libpthread, it can easily be built
    on any machine (no need for makefiles, etc.)

  Utilization:
   Since 'su' needs a username and password to change the user, the first two parameters
   will be these. The rest of the paramters are written as they are to the shell opened by
   the login process.
   These arguments are supposed *not* to contain any syntax errors, or invalid characters

  Arguments:
    1 - username
    2 - password
    3... - command to execute

  Unicode support:
    This application is not guaranteed to work on UTF16/UTF32 shells

*/
int main(int argc, char** argv) {

  int master_tty;
  int std_out;
  int std_err;
  pid_t pid;

  if ( pipe( err_pipe ) == -1 ) {
    printf("error setting pipe\n");
    return 1;
  }

  if (argc < 3 ) {
    fprintf( stderr, "usage: <username> <command> [<arg> ...]\n" );
    return 1;
  }

  // Read password from stdin
  char passwd[PASSWD_SIZE];
  int index = 0;
  while (index < PASSWD_SIZE) {
    passwd[index] = fgetc(stdin);
    if (passwd[index] == '\n') {
        passwd[index] = '\0';
        break;
    }
    if (passwd[index] == EOF) {
        passwd[index] = '\0';
        break;
    }
    index++;
  }
  passwd[PASSWD_SIZE-1] = '\0';

  //get a process with terminal
  pid = forkpty( &master_tty, NULL, NULL, NULL);

  if ( pid == -1 ) {
    return 1;
  }

  if ( pid != 0 ) {
    // -- PARENT

    close( err_pipe[1] );

    int amount;
    char buf[BUFSIZ];

//    wait until password is prompted
    while(1) {
      amount = read( master_tty, &buf, BUFSIZ );
      // TODO real check - but be careful with UNICODE!
      if ( amount > 0 ) break;
    }
    //write password to the terminal
    write( master_tty, passwd, strlen(passwd) );
    write( master_tty, "\n", sizeof "\n" );

    // ack after typing password
    read(master_tty, &buf, BUFSIZ);

    //wait until command executes
    //in the meantime forward error (in an other thread) & output (in this thread)
    pthread_t errT;
    pthread_create( &errT, NULL, errThread, NULL );

    do {
      amount = read( master_tty, &buf, BUFSIZ );
      if (amount > 0 ) {
        buf[amount] = '\0';
	    fprintf( stdout, "%s", buf );
	    fflush( stdout );
      }
    } while ( amount > 0 );

    //kill other thread - ugly hack
    stayAlive = 0;

    pthread_join( errT, NULL );

    int status;
    pid_t w = waitpid( pid, &status, WUNTRACED | WCONTINUED);

    exit ( WEXITSTATUS( status ) );

    //TODO exit value?
  } else {

    // -- CHILD

    //set up new stderr
    close( err_pipe[0] );
    dup2( err_pipe[1], STDERR_FILENO );

    /* Ensure that terminal echo is switched off so that we
       do not get back from the spawned process the same
       messages that we have sent it. */
    struct termios orig_termios;
    if (tcgetattr (STDIN_FILENO, &orig_termios) < 0) {
      perror ("ERROR getting current terminal's attributes");
      return -1;
    }

    orig_termios.c_lflag &= ~(ECHO | ECHOE | ECHOK | ECHONL);
    orig_termios.c_oflag &= ~(ONLCR );

    if (tcsetattr (STDIN_FILENO, TCSANOW, &orig_termios) < 0) {
      perror ("ERROR setting current terminal's attributes");
      return -1;
    }

    // concatenate argv to command to run
    int args_size = argc - 3; // number of spaces that will be added
    for ( int i = 2; i < argc; i++ ) {
        args_size += strlen(argv[i]);
    }

    char *command = malloc(args_size * sizeof(char));

    strcat(command, argv[2]);
    for(int i=3; i<argc; i++){
        strcat(command, " ");
        strcat(command, argv[i]);
    }

    //start su with a given username and a given command
    execl("/usr/bin/su", "/usr/bin/su", argv[1], "-c", command, (char *) 0);

    //unreachable code?
    close( err_pipe[1] );

    exit( EXIT_SUCCESS );
  }
}


