#!/bin/sh

rsync -Cavz ~/rsyncPAJ/ProActiveJacobi/dist cdalmasso@frontend.bordeaux.grid5000.fr:rsyncPAJ/ProActiveJacobi/

rsync -Cavz ~/rsyncPAJ/ProActiveJacobi/scripts cdalmasso@frontend.bordeaux.grid5000.fr:rsyncPAJ/ProActiveJacobi/

rsync -Cavz ~/rsyncPAJ/ProActiveJacobi/descriptors cdalmasso@frontend.bordeaux.grid5000.fr:rsyncPAJ/ProActiveJacobi/

rsync -Cavz ~/rsyncPAJ/ProActiveJacobi/compile cdalmasso@frontend.bordeaux.grid5000.fr:rsyncPAJ/ProActiveJacobi/

rsync -Cavz ~/.proactive cdalmasso@frontend.bordeaux.grid5000.fr:

rsync -Cavz ~/.bashrc cdalmasso@frontend.bordeaux.grid5000.fr:


#rsync -Cavz --exclude 'doc-src' --exclude 'dist'  --exclude 'classes' --exclude 'ic2d-plugins-src' --exclude 'resource-manager-plugin-src' --exclude 'scheduler-plugins-src'  ~/workspace/ProActiveJacobi cdalmasso@acces.sophia.grid5000.fr:toto

