#!/usr/bin/evn python

import sys
import os 
import subprocess 

mapping = {
    '.c':   [['svn:eol-style', 'native']],
    '.cpp': [['svn:eol-style', 'native']],
    '.h':   [['svn:eol-style', 'native']],
    '.sh':  [['svn:eol-style', 'native'],     ['svn:executable', '']],
    '.cmd': [['svn:mime-type', 'text/plain'], ['svn:eol-style', 'CRLF']],
    '.bat': [['svn:mime-type', 'text/plain'], ['svn:eol-style', 'CRLF']],
    '.txt': [['svn:mime-type', 'text/plain'], ['svn:eol-style', 'native']],
    '.xml': [['svn:mime-type', 'text/xml'],   ['svn:eol-style', 'native']],
    '.ent': [['svn:mime-type', 'text/plain'], ['svn:eol-style', 'native']],
    '.dtd': [['svn:mime-type', 'text/plain'], ['svn:eol-style', 'native']],
    '.xsd': [['svn:mime-type', 'text/xml'],   ['svn:eol-style', 'native']],
    '.xsl': [['svn:mime-type', 'text/xml'],   ['svn:eol-style', 'native']],
    '.wsdl':[['svn:mime-type', 'text/xml'],   ['svn:eol-style', 'native']],
    '.htm': [['svn:mime-type', 'text/html'],  ['svn:eol-style', 'native']],
    '.html':[['svn:mime-type', 'text/html'],  ['svn:eol-style', 'native']],
    '.css': [['svn:mime-type', 'text/css'],   ['svn:eol-style', 'native']],
    '.js':  [['svn:mime-type', 'text/plain'], ['svn:eol-style', 'native']],
    '.jsp': [['svn:mime-type', 'text/plain'], ['svn:eol-style', 'native']],
    '.txt': [['svn:mime-type', 'text/plain'], ['svn:eol-style', 'native']],
    '.java':[['svn:mime-type', 'text/plain'], ['svn:eol-style', 'native']],
    '.sql': [['svn:mime-type', 'text/plain'], ['svn:eol-style', 'native']],
    '.doc': [['svn:mime-type', 'application/msword']],
    '.exe': [['svn:mime-type', 'application/octet-stream']],
    '.gif': [['svn:mime-type', 'image/gif']],
    '.gz':  [['svn:mime-type', 'application/x-gzip']],
    '.jar': [['svn:mime-type', 'application/java-archive']],
    '.jpg': [['svn:mime-type', 'image/jpeg']],
    '.jpeg':[['svn:mime-type', 'image/jpeg']],
    '.pdf': [['svn:mime-type', 'application/pdf']],
    '.png': [['svn:mime-type', 'image/png']],
    '.tgz': [['svn:mime-type', 'application/octet-stream']],
    '.zip': [['svn:mime-type', 'application/zip']],
    '.class':[['svn:mime-type', 'application/java']],
    'Makefile':[['svn:eol-style', 'native']],
    '.properties':[['svn:mime-type', 'text/plain'], ['svn:eol-style', 'native']],

}

def fix_svn_prop(file):
    for ext in mapping:
        if file.endswith(ext):
            for prop in mapping[ext]:
                cmd = ['svn', 'propset', prop[0], prop[1], file]
                retcode = subprocess.call(cmd)
                if retcode != 0:
                    print >> sys.stderr, "svn propset failed on %s %s. Do not commit, revert and please contact me" % (file, prop)
                    sys.exit(1)
                
def walk(root):
    print root
    for root, dirs, files in os.walk(root):
        for file in files:
            fix_svn_prop(os.path.join(root, file))
        for dir in ['.git', '.svn']:
            if dir in dirs:
                dirs.remove(dir)


if __name__ == "__main__":
    walk(os.path.abspath(os.getcwd()))
