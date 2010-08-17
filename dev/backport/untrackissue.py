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

import optparse
import ConfigParser
import backendjira

'''
This script allows to disable automatic backport for a given issue key
'''
if __name__ == '__main__':
    usage = "usage: %prog issue_key"
    parser = optparse.OptionParser(usage)
    parser.add_option("-c", "--config",  action="store",      dest="config",   type="string", default="backport.ini", help="Backport script config file",)
    parser.add_option("-p", "--project",  action="store",     dest="project",  type="string",                         help="JIRA project",)

    (options, args) = parser.parse_args();

    if len(args) != 1:
        parser.error("incorrect number of  arguments")
    if options.project is None:
        parser.error("--project is mandatory")
  
    config = ConfigParser.RawConfigParser()
    config.read(options.config)
    
    url   = config.get('jira', 'url')
    user  = config.get('jira', 'user')
    passwd= config.get('jira', 'passwd')
    jira = backendjira.Jira(url, user, passwd, None, options.project)
    
    jira.untrack_issue(args[0])
    