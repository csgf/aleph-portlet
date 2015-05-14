#
# aleph-portlet
#
#
Aleph-portlet gives two main functionalities

1) Provides an input interface to query the openaccessrepository (www.openaccessrepository.ct.infn.it) on ALEPH documents.

2) Provides a RESTFull API interface to submit cloud-based jobs able to analyze ALEPH data

Aleph portlet belongs to a new generation of portlets comparing with the 'mi-hostname-portlet'. Aleph portlet makes use of jQuery and its Ajax calls. The portlet code handles ajax calls and it answers to RESTful-like URLs like: http://<portal_address>:<portal_port>/web/guest/<portlet_page>/-/aleph/json?command=test&testParam1=paramval1&testParam2=paramval2
Please notice that <portlet_page> is configured inside the portlet configuration file: docroot/WEB-INF/liferay-portlet.xml at:
  <friendly-url-mapping>
  <friendly-url-routes> 
and then ./docroot/WEB-INF/src/it/infn/ct/aleph-friendly-url-routes.xml

NOTES:

Aleph portlet requires OCCI client installation on Liferay server; these installation steps have been performed as 'root' user.

wget -O /etc/yum.repos.d/rocci-cli.repo http://repository.egi.eu/community/software/rocci.cli/4.2.x/releases/repofiles/sl-6-x86_64.repo
yum install -y occi-cli
ln -s /opt/occi-cli/bin/occi /usr/bin/occi
