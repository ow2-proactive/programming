#! /usr/bin/env python
# -*- coding: UTF-8 -*-
#################################################################
#
# ProActive Parallel Suite(TM): The Java(TM) library for
#    Parallel, Distributed, Multi-Core Computing for
#    Enterprise Grids & Clouds
#
# Copyright (C) 1997-2012 INRIA/University of
#                 Nice-Sophia Antipolis/ActiveEon
# Contact: proactive@ow2.org or contact@activeeon.com
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Affero General Public License
# as published by the Free Software Foundation; version 3 of
# the License.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
# USA
#
# If needed, contact us to obtain a release under GPL Version 2 or 3
# or a different license than the AGPL.
#
#  Initial developer(s):               The ProActive Team
#                        http://proactive.inria.fr/team_members.htm
#  Contributor(s):
#
#################################################################
# $$ACTIVEEON_INITIAL_DEV$$
#################################################################

import git
import re

def get_svn_commit_id(git_commit):
    mo = re.search("git-svn-id: [\w.+:/]+@(\d+) ", git_commit.message, re.MULTILINE)
    if mo is not None:
            return mo.group(1)
    return None

class Git(object):
    '''
    A backend to manipulate a dedicated Git repository
    '''
    def __init__(self, repo_path):
        self.repo = git.Repo(repo_path)
        
    def get_commits_for(self, branch, issueKey):
        '''
        Return a list of git commit related to a given issue Key
        '''
        
        ret = []
        commits =  self.repo.iter_commits(branch)
        pattern = re.compile(issueKey, re.MULTILINE)
        for commit in commits:
            if pattern.search(commit.message) is not None:
                ret.append(commit)
        return ret

    def get_commit_from_svn(self, branch, svn_rev):
        '''
        Return the git commit for a given svn revision
        '''
        commits =  self.repo.iter_commits(branch)
        pattern = re.compile("git-svn-id: [\w.+:/]+@%s" % svn_rev)
        for commit in commits:
            if pattern.search(commit.message) is not None:
                return commit
        
        assert("SVN Commit not found")
