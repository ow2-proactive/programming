#!/usr/bin/env python

import sys
import os
import string

import numpy as np
import matplotlib.pyplot as plt
import re

def main():
	dir = sys.argv[1]

	if len(sys.argv) == 1:
		dict = create_dict(dir)
		draw_graph(dict)
	else:
		for i in range(2, len(sys.argv)):
			dict = create_dict(dir, sys.argv[i])
			draw_graph(dict, sys.argv[i])

def create_dict(rootdir, match='.*'):
	pattern = re.compile(match)
	dict = {}

	for branch in os.listdir(rootdir):
		branch_dict = {}
		for test in os.listdir(os.path.join(rootdir, branch)):
			if pattern.match(test):
				file = open(os.path.join(rootdir, branch, test))
				str = file.readline()
				str = str.strip()
				start = str.find("=")
				if start != -1:
					branch_dict[test] = round(string.atof(str[start+1:]),2)
				else:
					branch_dict[test] = -1.
		dict[branch] = branch_dict

	return dict


def get_all_test_name(dict):
	for branch in dict:
		return dict[branch].keys()

def get_branches(dict):
	return dict.keys()


def compare_by_branch(dict):
	def local_print(test, d):
		print test
		for t in d:
			print "\t" +  t + "\t" + str(d[t])
		print

	for test in get_all_test_name(dict):
		local_dict = {}
		for branch in dict:
			local_dict[branch] = dict[branch][test]

		local_print(test, local_dict)

### Unused ###

def short_test_name(long_name):
	return long_name[long_name.rfind('.Test')+5:]

def draw_graph(dict, title):

	def autolabel(rects):
		for rect in rects:
			height = rect.get_height()
			ax.text(rect.get_x()+rect.get_width()/2., 1.05*height, '%d'%int(height),
			        ha='center', va='bottom')


	def set_legend(bars, branches):
		bs = ()
		for bar in bars:
			bs = bs + (bar[0],)
		ax.legend( bs, branches)

	colors = ['b', 'g', 'r', 'c', 'm', 'y', 'b']
	branches = get_branches(dict)
	all_tests = get_all_test_name(dict)

	N = len(all_tests)
	ind = np.arange(N)
	width = 0.35


	fig = plt.figure()
	ax = fig.add_subplot(111)

	data_sets = []
	for branch in branches:
		data =()
		for test in all_tests:
			data = data + (dict[branch].get(test, 0),)
		data_sets.append(data)

	bars = []
	counter = 0
	for data in data_sets:
		bar = ax.bar(ind + (counter*width), data, width, color=colors[counter])
		bars.append(bar)
		counter += 1


	# add some
	ax.set_ylabel('Perf')
	ax.set_title('Branch perf comparison for ' + title)
	ax.set_xticks(ind+width)
	ax.set_xticklabels(map(short_test_name, all_tests))

	set_legend(bars, branches)

	for bar in bars:
		autolabel(bar)

	plt.savefig(title + ".png")


if __name__ == "__main__":
    main()
