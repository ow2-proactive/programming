#! /usr/bin/perl
#
#
# Display all failed tests for all ProActive Projects

use warnings;
use strict;

use XML::XPath;
use LWP::Simple;


my $jobs_xml = XML::XPath->new(filename => get_and_save("http://galpage-exp.inria.fr:8080/hudson/api/xml"));
my $nodeset = $jobs_xml->find('//*/job[contains(name, \'ProActive\')]/url/text()'); 
foreach my $node ($nodeset->get_nodelist) {
	my $project_url = XML::XPath::XMLParser::as_string($node);
	my $file = get_and_save("$project_url/lastSuccessfulBuild/testReport/api/xml");
	next if (! defined $file);

	print "\n\n$project_url\n\n";

	my $job_xml = XML::XPath->new(filename => get_and_save("$project_url/lastSuccessfulBuild/testReport/api/xml"));

	my $nodeset = $job_xml->find('//*/child/child[statu=\'REGRESSION\']/className/text()'); 
	foreach my $node ($nodeset->get_nodelist) {
		print "\t", "NEW ", XML::XPath::XMLParser::as_string($node), "\n";
	}

	$nodeset = $job_xml->find('//*/child/child[statu=\'FAILED\']/className/text()');
	foreach my $node ($nodeset->get_nodelist) {
		print "\t", "    ", XML::XPath::XMLParser::as_string($node), "\n";
	}
}

sub get_and_save {
	my ($url) = @_;
	my $file = "/tmp/zorglup.xml";

	my $blob = get($url);
	if (! defined $blob ) {
		return undef;
	} 

	open (FILE, ">$file") or die ("Zut");
	print(FILE $blob);
	close(FILE);

	return $file
}
