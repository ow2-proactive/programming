#! /usr/bin/env python
# -*- coding: UTF-8 -*-
#################################################################
#
# ProActive Parallel Suite(TM): The Java(TM) library for
#    Parallel, Distributed, Multi-Core Computing for
#    Enterprise Grids & Clouds
#
# Copyright (C) 1997-2011 INRIA/University of
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


import ConfigParser
import SOAPpy
import git
import os
import re
import tempfile
import time
import calendar

BOT_PREFIX="%%&%%"

   

def release_version_to_branch(version):
    return version.rstrip("0123456789") + "x"

def _jira_merge_message(jirakey, revisions, version):
    msg = "%s MERGED key=%s !! branch=%s !! version=%s !! revisions=" % (BOT_PREFIX, jirakey, release_version_to_branch(version), version) 
    for revision in revisions:
        msg += "#" + str(revision) + " "
    return msg
    

class Jira(object):
    '''
    A backend to manipulate and query a remote JIRA server
    '''
    def __init__(self, baseurl, username, password, gitb, project, dryrun=False):
        self.baseurl = baseurl
        self.username = username
        self.password = password
        self.gitb = gitb
        self.soap = SOAPpy.WSDL.Proxy(baseurl + "/rpc/soap/jirasoapservice-v2?wsdl")
        self.token= self.soap.login(self.username, self.password)
        self.dryrun = dryrun
        self.project = project

        self.statuses_by_id = {} 
        self.statuses_by_name = {} 

        statuses = self.soap.getStatuses(self.token)
        for status in statuses:
            self.statuses_by_name[status['name']] = status['id']
            self.statuses_by_id[status['id']] = status['name']
      
        
    def get_issue(self, issue_key):
        '''
        Return the summary of an issue key (string) 
        '''
        return self.soap.getIssue(self.token, issue_key)
    
    def get_commit_to_merge(self, version):
        '''
        Return a dictionary with all the issues to be backported
        
        Keys are the issue keys. Values are a list of git commits, linked to this issue key
        '''
        ret = {}
        
        issues = self.soap.getIssuesFromJqlSearch(self.token, "project = %s AND (status = Resolved OR status = Closed) AND fixVersion='%s'" % (self.project, version), 1000)
        for issue in issues:
            commits = self.get_unmerged_commits_for_issue(issue.key, version)
            if len(commits) != 0:
                ret[issue.key] = commits
                
        return ret
                
    def get_unmerged_commits_for_issue(self, issue_key, version):
        '''
        Return a list of git commits.
        
        Each commit in this list is linked to the given issue key and has not yet been backported 
        '''
        ret = []

        comments = self.soap.getComments(self.token, issue_key)

        # If the issue is untracked, return an empty list
        patern = re.compile("%s UNTRACK_ISSUE key=%s" % (BOT_PREFIX, issue_key))
        for comment in comments:
            if patern.match(comment.body) is not None:
                return []

        # Get the timestamp of the last comment made by the merge bot
        patern = re.compile("%s MERGED key=%s !! branch=[\w\-.]+ !! version=%s !! revisions=(#[\d]+ )+" % (BOT_PREFIX, issue_key, version))
        last_merged = None
        for comment in comments:
            if patern.match(comment.body) is not None:
                # Use local time not UTC
                t = calendar.timegm((comment.created[0], comment.created[1], comment.created[2], comment.created[3], comment.created[4], int(comment.created[5]), -1, -1, -1))

                last_merged = t

        # Get the list of commits with a given issue key
        commits = self.gitb.get_commits_for("master", issue_key)
        for commit in commits:
            if commit.committed_date > last_merged:
                ret.append(commit)
                

        # Get the list of tagged commits
        patern = re.compile("%s ADD_COMMIT key=%s !! revision=(#[\d]+ )+" % (BOT_PREFIX, issue_key))
        for comment in comments:
            mo = patern.match(comment.body) 
            if mo is not None:
                commit = self.gitb.get_commit_from_svn("master", mo.group(1))
                if commit.committed_date > last_merged:
                    ret.append(commit)
                    
        # Sort commits by date
        ret.sort(key=lambda x: x.committed_date)
        return ret
    
    
    def merge_issue(self, version, issue_key, commits):
        '''
        Cherry pick all the commit related to the issue key into the release branch
        
        If something goes wrong, all the cherry pick are rollbacked 
        '''
        revs = []

        for commit in commits:
            mo = re.search("git-svn-id: [\w.+:/]+@(\d+) ", commit.message, re.MULTILINE)
            assert mo is not None, "No SVN commit found"
            revs.append(mo.group(1))
            print "\tCherry picking: %s" % mo.group(1)
            try :
                output = self.gitb.repo.git.cherry_pick(commit.sha, n=True)
                print output
            except git.GitCommandError:  
                self.gitb.repo.git.checkout(f=True)
                self.gitb.repo.git.reset("HEAD", hard=True)
                print "\t\t********************************************************************************************"
                print "\t\t --> CHERRY PICK OF #%s FAILED (conflicts). PLEASE BACKPORT %s MANUALLY <-- " % (mo.group(1), issue_key)
                print "\t\t********************************************************************************************"

                return None

        fname = tempfile.mktemp()
        f = open(fname, 'w')
        f.write(_jira_merge_message(issue_key, revs, version))
        f.write("\n\n\n")
        for commit in commits:
            f.write("--\n")
            f.write(commit.message)
            f.write("\n")
        f.close()
        
        self.gitb.repo.git.commit(a=True, s=True, F=fname)
        if not self.dryrun:
            self.soap.addComment(self.token, issue_key, {'body' : _jira_merge_message(issue_key, revs, version)})

        return True
    
    def add_missing_svn_commit(self, issue_key, svn_rev):
        if not self.dryrun:
            self.soap.addComment(self.token, issue_key, {'body' : "%s ADD_COMMIT key=%s !! revision=%s" % (BOT_PREFIX, issue_key, svn_rev)})

    def untrack_issue(self, issue_key):
        if not self.dryrun:
            self.soap.addComment(self.token, issue_key, {'body' : "%s UNTRACK_ISSUE key=%s" % (BOT_PREFIX, issue_key)})

    def get_untracked_issue(self, version):
        ret = []

        issues = self.soap.getIssuesFromJqlSearch(self.token, "project = %s AND (status = Resolved OR status = Closed) AND fixVersion='%s'" % (self.project, version), 1000)
        patern = re.compile("%s UNTRACK_ISSUE key=([A-Z]+-[0-9]+)" % (BOT_PREFIX))
        for issue in issues:
            comments = self.soap.getComments(self.token, issue['key'])
            for comment in comments:
                if patern.match(comment.body) is not None:
                    ret.append(issue)

        return ret

