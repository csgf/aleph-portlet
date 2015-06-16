*********************
Aleph
*********************

============
About
============

.. image:: images/aleph.png
   :align: left
   :target: http://aleph.web.cern.ch/aleph/ 
-------------

ALEPH was a particle physics experiment installed on the Large Electron-Positron collider (LEP) at the CERN laboratory in Geneva/Switzerland. It was designed to explore the physics predicted by the Standard Model and to search for physics beyond it. ALEPH first measured events in LEP in July 1989. LEP operated at around 91 GeV – the predicted optimum energy for the formation of the Z particle. From 1995 to 2000 the accelerator operated at energies up to 200 GeV, above the threshold for producing pairs of W particles. The data taken, consisted of millions of events recorded by the ALEPH detector,allowed precision tests of the electro-weak Standard Model (SM) to be undertaken. The group here concentrated our analysis efforts mainly in Heavy Flavour (beauty and charm) physics, in searches for the the Higgs boson, the particles postulated to generate particle mass, and for physics beyond the SM, e.g. Supersymmetry, and in W physics.
This application perform the search for the production and non-standard decay of a scalar Higgs boson into four tau leptons through the intermediation of the neutral pseudo-scalars Higgs particle. 
The analysis was conducted by the ALEPH collaboration with the data collected at centre-of-mass energies from 183 to 209 GeV.

============
Installation
============
Following instructions are meant for science gateway maintainers while generic users can skip this section.
To install the portlet it is enough to install the war file into the application server and then configure several settings into the portlet preferences pane.
Preferences have the form of a set of couples (key,value). Preferences are also grouped accordingly to the service configured. The meaning of each preference key will be described below, grouping them as well:

.. image:: images/pref_top.png

General settings
****************

:Grid Operation:
 Value used by the GridEngine to register user activity on the DCI
:cloudMgrHost: 
 Unused
:proxyFile:
 Unused

eTokenServer
************
Following settings are related to the eTokenServer service wich is the responsible to deliver proxy certificates from robot Certificates

:eTokenHost:
 Server hostname that issues Robot proxy certificates  
:eTokenPort:
 Server port that issues Robot proxy certificates  
:eTokenMd5Sum:
 The MD5 Sum specifies which specific Robot Certificate will be used to create the proxy certificate  
:eTokenVO:
 VO name for the proxy certificate (VOMS) extension
:eTokenVOGroup:
 VOMS Grou requested
:eTokenProxyRenewal:
 proxy certificate proxy renewal flag
:alephGroupName:  
 unused

Guacamole
*********
Aleph uses Guacamole service to obtain VNC and SSH connections available from the portal

:guacamole_dir:   
 Guacamole service server path
:guacamole_noauthxml: 
 path to the Guacamole noauthxml file
:guacamole_page:
 base page for Guacamole

iServices
*********
iServices is a new GridEngine helper service that manages the interactive services, its allocation status, lifertime, etc.

:iservices_dbname:
 iservices database name
:iservices_dbhost:
 iservices database host
:iservices_dbport:
 iservices database port
:iservices_dbuser:
 iservices database user    
:iservices_dbpass:
 iservices database password
:iservices_srvname:
 iservices interactive service name

cloudProvider
*************
cloudProvider is a new GridEngine helper service that maintains the necessary configuration to allocate new services on the cloud

:cloudprovider_dbname:
 cloud provider database name
:cloudprovider_dbhost:
 cloud provider database host
:cloudprovider_dbport:
 cloud provider database port
:cloudprovider_dbuser:
 cloudprovider database user
:cloudprovider_dbpass:    
 cloudprovider database password

.. image:: images/pref_bottom.png

The buttons represented by the picture above are representing

:Back:
 Return to the portlet
:Set Preferences:
 Apply changes to the preferences
:Reset:
 Reset default portlet settings as configured inside the portlet.xml file

============
Usage
============
The usage of the portlet is simple; the user can select to upload a local R macro file selecting the Browse button in the **Application input file** section, or insert inside the text field the R macro text by pasting a text or editing directly on the larger text box below.
The **job identifier** text is a human readable values that users will use to keep track of any job execution.
Following buttons: Demo, Submit, Reset values and About are respectively:

 *Demo* - Fills the Macro Text box with an R-Macro example

 *Submit* - Executes the given macro on the distributed infrastructure

 *Reset* - Resets the input form

 *About* - Gives an overview of the portlet

.. image:: images/input.png

============
Support
============
To get support such as reporting a bug, a problem or even request new features, please contact

.. _INFN: http://www.ct.infn.it/

:Authors:
 
 `Roberto BARBERA <mailto:roberto.barbera@ct.infn.it>`_ - Italian National Institute of Nuclear Physics (INFN_),
 
 `Riccardo BRUNO <mailto:riccardo.bruno@ct.infn.it>`_ - Italian National Institute of Nuclear Physics (INFN_),

 `Rita RICCERI <mailto:rita.ricceri@ct.infn.it>`_ - Italian National Institute of Nuclear Physics (INFN_),

 `Carla CARRUBBA <mailto:carla.carrubba@ct.infn.it>`_ - Italian National Institute of Nuclear Physics (INFN_),

 `Giuseppina INSERRA <mailto:giuseppina.inserra@ct.infn.it>`_ - Italian National Institute of Nuclear Physics (INFN_),

:Version: v1.0











