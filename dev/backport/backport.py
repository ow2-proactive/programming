#! /usr/bin/env python
# -*- coding: UTF-8 -*-
#################################################################
#
# ProActive Parallel Suite(TM): The Java(TM) library for
#    Parallel, Distributed, Multi-Core Computing for
#     Enterprise Grids & Clouds
#
# Copyright (C) 1997-2010 INRIA/University of 
#                 Nice-Sophia Antipolis/ActiveEon
# Contact: proactive@ow2.org or contact@activeeon.com
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; version 3 of
# the License.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
# USA
#
# If needed, contact us to obtain a release under GPL Version 2 
# or a different license than the GPL.
#
#  Initial developer(s):               The ProActive Team
#                        http://proactive.inria.fr/team_members.htm
#  Contributor(s):
#
#################################################################
# $$ACTIVEEON_INITIAL_DEV$$
#################################################################

#! /bin/env python

import backendjira
import backendgit

import optparse
import ConfigParser
from git.errors import GitCommandError

'''
This script automatically backports all the unmerged commits into a maintenance branch.

Requirements:
 - A dedicated git repository with one branch for each release branch (master + ?.?.x )
 - An account on the JIRA server
 - All commits must be associated to a JIRA issue key (in the commit message or by using the tagcommit.py script)
 
If something goes wrong, the cherry pick is aborted and the issue must be backported by hand afterward

You should review carefully the patches and check everything is fine before sending the changes to the Subversion repository (git svn dcommit)

To test the script, use the dry-run mode. It disables JIRA update, so you just have to reset your git repository if something goes wrong.
'''

        
def yes_or_no(prompt):
    doit = None
    while doit is None:
        r = raw_input(prompt)
        if r == "y" or r == "Y":
            doit = True
        elif r == "n" or r == "N":
            doit = False   
    return doit
        
        
        
if __name__ == '__main__':
    usage = "usage: %prog [-d | -c config ] --version 4.3.1"
    parser = optparse.OptionParser(usage)
    parser.add_option("-c", "--config",  action="store",      dest="config",   type="string", default="backport.ini", help="Backport script config file",)
    parser.add_option("-d", "--dry-run", action="store_true", dest="dry_run",                 default=False,          help="Dry run mode")
    parser.add_option("-v", "--version", action="store",      dest="version",  type="string", help="The version",)

    (options, args) = parser.parse_args();

    if len(args) != 0 :
        parser.error("incorrect number of  arguments")
    if  options.version is None:
        parser.error("--version must be specified")
        
        
    config = ConfigParser.RawConfigParser()
    config.read(options.config)

    git = backendgit.Git(config.get('git', 'repo_path'))
    try:
        git.repo.git.checkout(backendjira.release_version_to_branch(options.version))
    except GitCommandError:
        print "Failed to checkout branch: %s" % backendgit.release_version_to_branch(options.version)
        os.exit(1)
    
    url   = config.get('jira', 'url')
    user  = config.get('jira', 'user')
    passwd= config.get('jira', 'passwd')
    jira = backendjira.Jira(url, user, passwd, git, options.dry_run)
   
    
    unmerged_issues = jira.get_commit_to_merge(options.version)
    print "Hello ! %s issues need to be backported in %s: %s" % (len(unmerged_issues), options.version, unmerged_issues.keys())
    
    for issue_key in unmerged_issues:
        issue =  jira.get_issue(issue_key)
        print "\n\n\n--> %s" % (issue['key'])
        print "\tSummary:      %s" % (issue['summary'])
        print "\tReporter:     %s" % (issue['reporter'])
        print "\tAssignee:     %s" % (issue['assignee'])
        print "\tFix for :     %s" % (map(lambda x: x['name'], issue['fixVersions']))
        print "\tSVN unmerged_issues: "
        for commit in unmerged_issues[issue_key]:
            print"\t\t #%s:   %s " % (backendgit.get_svn_commit_id(commit), commit.message.split('\n', 1)[0])

        if not yes_or_no("Do you want to cherry pick these commits ? [Y/N]"):
            print "Skipping issue %s" % issue['key']
            continue
        
        print "Merging issue %s" % issue_key
        jira.merge_issue(options.version, issue_key, unmerged_issues[issue_key])
    
    print "Done. Please check your repository before dcommiting"
