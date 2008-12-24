#!/usr/bin/env python

from hudson import *
import pprint

BASE_URL = "http://hudson.activeeon.com/"


def get_most_failing_tests(job):
    first = job.get_first_build_number()
    last  = job.get_last_build_number() 
    for i in range(first, last):
        build = job.get_build(i)
        test_report = build.get_test_report()
        print build.get_result()
        if test_report != None:
            print test_report.get_failed()
        

hudson = Hudson(BASE_URL)
proactive = hudson.get_job("ProActive")
get_most_failing_tests(proactive)