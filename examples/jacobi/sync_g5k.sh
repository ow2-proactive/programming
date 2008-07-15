#!/bin/sh


rsync --exclude 'dist'  --exclude 'classes' -avz ~/workspace/ProActiveJacobi cdalmasso@acces.sophia.grid5000.fr:rsyncPAJ

#rsync -Cavz --exclude 'doc-src' --exclude 'dist'  --exclude 'classes' --exclude 'ic2d-plugins-src' --exclude 'resource-manager-plugin-src' --exclude 'scheduler-plugins-src'  ~/workspace/ProActiveJacobi cdalmasso@acces.sophia.grid5000.fr:toto

