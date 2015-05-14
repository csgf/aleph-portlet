/**************************************************************************
Copyright (c) 2011:
Istituto Nazionale di Fisica Nucleare (INFN), Italy
Consorzio COMETA (COMETA), Italy

See http://www.infn.it and and http://www.consorzio-cometa.it for details on
the copyright holders.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author <a href="mailto:riccardo.bruno@ct.infn.it">Riccardo Bruno</a>(INFN)
****************************************************************************/
package it.infn.ct;

// Import generic java libraries
import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.List;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;
 
// Importing portlet libraries
import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;

// Importing liferay libraries
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.Group;
import com.liferay.portal.util.PortletKeys;
import com.liferay.portal.service.RoleServiceUtil;
import com.liferay.portal.service.UserGroupLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;

// Importing GridEngine Job libraries
import it.infn.ct.GridEngine.Job.*;
import it.infn.ct.GridEngine.JobResubmission.GEJobDescription;
import it.infn.ct.GridEngine.Job.MultiInfrastructureJobSubmission;
import it.infn.ct.GridEngine.UsersTracking.UsersTrackingDBInterface;
import java.util.*;

// JSON
import org.json.simple.JSONValue;

// Logging
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 *  This is the class that overrides the GenericPortlet class methods
 *  for the aleph_access project 
 *
 * @author <a href="mailto:riccardo.bruno@ct.infn.it">Riccardo Bruno</a>(COMETA)
 */
public class aleph_portlet extends GenericPortlet {

    // Instantiate the logger object
    AppLogger _log = new AppLogger(aleph_portlet.class);

    // This portlet uses Aciont/Views enumerations in order to
    // manage the different portlet modes and the corresponding
    // view to display
    // You may override the current values with your own business
    // logic best identifiers and manage them through: jsp pages and
    // this java code
    // The jsp parameter PortletStatus will be the responsible of
    // portlet mode switching. This parameter will be read by
    // the processAction method (actionRequest) who will select
    // then the proper view mode. The doView method will read this
    // value (renderResponSe) assigning the correct view mode.
    //
    // At first boot the application will be in ACTIVATE status
    // that means the application still requires to be registered
    // into the GridEngine' UsersTrackingDB' GridOperations table
    // Once registered the defaul view mode will be the VIEW_INPUT

    /**
     * Actions enumeration contains the possible action status mode
     * managed by the application. Action modes are stored into the 
     * 'PortletStatus' parameter inside the actionRequest object
     */
    private enum Actions {
         ACTION_INPUT     // Only one action is possible by this portlet 
        ,ACTION_DOI
    }

    /**
     * Views enumeration contains the possible view mondes managed b
     * the application. View modes are stored into the parameter  'PortletStatus'
     * inside the renderResponse object
     */
    private enum Views {
         VIEW_INPUT   // Only one view is possible by this portlet 
        ,VIEW_DOI
    }

    // Liferay portal data
    // Classes below are used by this portlet code to get information
    // about the current user
    public String       userName   = "unknown";   // Portal username
    public String       firstName  = "unknown";   // Portal user first name
    public String       lastName   = "unknown";   // Portal user last name
    public String       userMail   = "unknown";   // Portal user mail
    public String       portalName = "localhost"; // Name of the hosting portal
    public String       appServerPath;            // This variable stores the absolute path of the Web applications

     
    public String searchText;
    public String checked;
    public String offset;
    public ArrayList globalList;//=new ArrayList();
    public ArrayList listReecords;
    
    // Other misc valuse
    // (!) Pay attention that altough the use of the LS variable
    //     the replaceAll("\n","") has to be used
    public static final String LS = System.getProperty("line.separator");

    // Users must have separated inputSandbox files
    // these file will be generated into /tmp directory
    // and prefixed with the format <timestamp>_<user>_*
    // The timestamp format is:
    public static final String tsFormat = "yyyyMMddHHmmss";

    // Portlet parameters 
    int     alephGridOperation;       // GridEngine Grid operation
    String  portalHost;               // Used to build JSPs AJAX calls
    String  portletPage;              // Used to build JSPs AJAX calls
    String  cloudmgrHost;             // CloudMgr URL
    String  proxyFile;                // Proxy full pathname
    String  portalSSHKey;             // Full path to portal' SSH public key
    String  eTokenHost;               // eTokenServer host
    String  eTokenPort;               // eTokenServer port
    String  eTokenMd5Sum;             // eTokenServer Md5Sum
    String  eTokenVO;                 // eTokenServer VO name
    String  eTokenVOGroup;            // eTokenServer VO Group name
    String  eTokenProxyRenewal;       // eTokenProxyRenewal flag
    String  alephGroupName;           // This is the necessary Liferay' Group to enable ALEPH VM initialization
    String  guacamole_dir;            // Guacamole directory
    String  guacamole_noauthxml;      // Guacamole noauthxml file
    String  guacamole_page;           // Guacamole main page name (version)
    String  service_description;      // iService long description
    boolean isAlephVMEnabled = false; // This flag tells if the user' has the right to instantiate ALEPH VMs or not
    boolean initialized = true;       // The portlet is just initialized?

    // Preferences class
    public class Preferences {
        public String LS = System.getProperty("line.separator");
        HashMap<String,String> prefValues = null; 
        String prefNames[] = {
          "GridOperation"
         ,"cloudMgrHost"      
         ,"proxyFile" 
         ,"portalSSHKey"       
         ,"eTokenHost"        
         ,"eTokenPort"          
         ,"eTokenMd5Sum"        
         ,"eTokenVO"            
         ,"eTokenVOGroup"       
         ,"eTokenProxyRenewal" 
         ,"alephGroupName"     
         ,"guacamole_dir"       
         ,"guacamole_noauthxml"
         ,"guacamole_page"
         ,"iservices_dbname"     
         ,"iservices_dbhost"     
         ,"iservices_dbport"     
         ,"iservices_dbuser"   
         ,"iservices_dbpass"    
         ,"iservices_srvname"
         ,"cloudprovider_dbname"
         ,"cloudprovider_dbhost"
         ,"cloudprovider_dbport"
         ,"cloudprovider_dbuser"
         ,"cloudprovider_dbpass" 
        };
        PortletPreferences pPrefs = null;
        public String[] getPrefNames() { return prefNames; }        

        Preferences() {
            prefValues = new HashMap<String, String>();
            for(int i=0; i<prefNames.length; i++)
              prefValues.put( "", prefNames[i]);
        }

        PortletPreferences getPortletPreferences() { return pPrefs; }

        public void setPrefValue(String prefName, String prefValue) {
            if(prefValues != null)
               prefValues.put(prefName,prefValue);
        }
        public String getPrefValue(String prefName) {
            if(prefValues != null)
               return prefValues.get(prefName);
            else return "";
        }
        public String getPrefName(int ithName) {
            return prefNames[ithName];
        }
        public String tabify(boolean editableFlag) {
            String prefTable = "";
            String prefValue = "";
            if(prefValues != null)
               for(int i=0; i<prefNames.length; i++) {
                   if(editableFlag) 
                        prefValue = "<input id=\"pref_input\" type=\"text\" name=\""+prefNames[i]+"\" value=\""+prefValues.get(prefNames[i])+"\"/></td";
                   else prefValue = prefValues.get(prefNames[i]);
                   prefTable+="<tr><td>"+prefNames[i]+"</td><td>"+prefValue+"</td></tr>"+LS;
               }
            return prefTable;
        }
        public String dump() {
            String prefDump = "";
            if(prefValues != null)
               for(int i=0; i<prefNames.length; i++)
                   prefDump+=prefNames[i]+" - "+prefValues.get(prefNames[i]) + LS;
            return prefDump;
        }
        public String json() {
            String prefJSON = "";
            String comma    = "";
            if(prefValues != null)
               for(int i=0; i<prefNames.length; i++) {
                   if (i == 0 ) 
                        comma = "";
                   else comma = ",";
                   prefJSON+= comma + " \"" + prefNames[i]+"\" : \""+ prefValues.get(prefNames[i]) + "\" ";
                }
            return "{ "+ prefJSON + " }";
        }
        // Set portlet preferences
        public void setPortletPrefs(ActionRequest request) {
            if(request != null) {
                this.pPrefs = request.getPreferences();
                setPortletPrefs(pPrefs);
            }
        }
        public void setPortletPrefs(RenderRequest request) {
            if(request != null) {
                this.pPrefs = request.getPreferences();
                setPortletPrefs(pPrefs);
            }
        }
        public void setPortletPrefs(ResourceRequest request) {
            if(request != null) {
                this.pPrefs = request.getPreferences();
                setPortletPrefs(pPrefs);
            }
        }
        public void setPortletPrefs(PortletPreferences pPrefs) {
            if(pPrefs != null) {
              this.pPrefs = pPrefs;
              setPortletPrefs();
            } else _log.error("Unable to set portlet preferences from null portlet preference object" + LS);
        }
        public void setPortletPrefs() {
            String report = LS;
            if(pPrefs != null) {
                for(int i=0; i<prefNames.length; i++) 
                    try {
                        report += "====PREF["+prefNames[i]+"]===>"+ prefNames[i] + " = " + prefValues.get(prefNames[i]) + LS;
                        pPrefs.setValue(prefNames[i],prefValues.get(prefNames[i]));
                        pPrefs.store();
                    } catch(Exception e) {
                        _log.error("Unable to set portlet preferences: '"+e.toString()+"'");
                    }
                _log.info(report);
            } else _log.error("Unable to set portlet preferences from null portlet preference object" + LS);
        }
        // Get portlet preferences
        public void getPortletPrefs(ActionRequest request) {
            getPortletPrefs(request.getPreferences());
        }
        public void getPortletPrefs(RenderRequest request) {
            getPortletPrefs(request.getPreferences());
        }
        public void getPortletPrefs(ResourceRequest request) {
            getPortletPrefs(request.getPreferences());
        }
        public void getPortletPrefs(PortletPreferences pPrefs) {
            if(pPrefs != null) {
              for(int i=0; i<prefNames.length; i++) 
                setPrefValue(prefNames[i], pPrefs.getValue(prefNames[i],""));
            } else _log.error("Unable to get portlet preferences from null portlet preference object" + LS);
        } 
    } 
    Preferences prefs = new Preferences();

    // iservices object
    iservices iSrv = null;
    // cloudprovider object
    CloudProvider cloudProvider = null;
    
    //----------------------------
    // Portlet Overriding Methods
    //----------------------------

    /**
     * The init method will be called when installing the portlet for the first time
     * or when restarting the portal server.
     * This is the right time to get default values from WEBINF/portlet.xml file
     * Those values will be assigned into the application preferences as default values
     * If preference values already exists for this application the default settings will
     * be overwritten
     *
     * @see AppInfrastructureInfo
     * @see AppPreferences
     *
     * @throws PortletException
     */
    @Override
    public void init()
    throws PortletException
    {
        _log.info("Calling init()");
        // Load default values from WEBINF/portlet.xml
        //
        //  <init-param>
        //     <name>view-template</name>
        //     <value>/view.jsp</value>
        // </init-param>
        String GridOp      = getInitParameter("GridOperation"      ); prefs.setPrefValue("GridOperation",GridOp);
        alephGridOperation = Integer.parseInt(GridOp               );
        String CAPath      = getInitParameter("CAPath"             ); prefs.setPrefValue("CAPath",CAPath);
        proxyFile          = getInitParameter("ProxyFile"          ); prefs.setPrefValue("ProxyFile"          ,proxyFile          );
        portalSSHKey       = getInitParameter("portalSSHKey"       ); prefs.setPrefValue("portalSSHKey"       ,portalSSHKey       );
        cloudmgrHost       = getInitParameter("CloudMgr"           ); prefs.setPrefValue("CloudMgr"           ,cloudmgrHost       );
        alephGroupName     = getInitParameter("AlephGroupName"     ); prefs.setPrefValue("AlephGroupName"     ,alephGroupName     );
        eTokenHost         = getInitParameter("eTokenHost"         ); prefs.setPrefValue("eTokenHost"         ,eTokenHost         );
        eTokenPort         = getInitParameter("eTokenPort"         ); prefs.setPrefValue("eTokenPort"         ,eTokenPort         );
        eTokenMd5Sum       = getInitParameter("eTokenMd5Sum"       ); prefs.setPrefValue("eTokenMd5Sum"       ,eTokenMd5Sum       );
        eTokenVO           = getInitParameter("eTokenVO"           ); prefs.setPrefValue("eTokenVO"           ,eTokenVO           );
        eTokenVOGroup      = getInitParameter("eTokenVOGroup"      ); prefs.setPrefValue("eTokenVOGroup"      ,eTokenVOGroup      );
        eTokenProxyRenewal = getInitParameter("eTokenProxyRenewal" ); prefs.setPrefValue("eTokenProxyRenewal" ,eTokenProxyRenewal );
        guacamole_dir      = getInitParameter("guacamole_dir"      ); prefs.setPrefValue("guacamole_dir"      ,guacamole_dir      );
        guacamole_noauthxml= getInitParameter("guacamole_noauthxml"); prefs.setPrefValue("guacamole_noauthxml",guacamole_noauthxml);
        guacamole_page     = getInitParameter("guacamole_page"     ); prefs.setPrefValue("guacamole_page"     ,guacamole_page     );
        // iservices params
        String iservices_dbname  = getInitParameter("iservices_dbname" ); prefs.setPrefValue("iservices_dbname" ,iservices_dbname );
        String iservices_dbhost  = getInitParameter("iservices_dbhost" ); prefs.setPrefValue("iservices_dbhost" ,iservices_dbhost );
        String iservices_dbport  = getInitParameter("iservices_dbport" ); prefs.setPrefValue("iservices_dbport" ,iservices_dbport );
        String iservices_dbuser  = getInitParameter("iservices_dbuser" ); prefs.setPrefValue("iservices_dbuser" ,iservices_dbuser );
        String iservices_dbpass  = getInitParameter("iservices_dbpass" ); prefs.setPrefValue("iservices_dbpass" ,iservices_dbpass );
        String iservices_srvname = getInitParameter("iservices_srvname"); prefs.setPrefValue("iservices_srvname",iservices_srvname);
        // cloudprovider params
        String cloudprovider_dbname  = getInitParameter("cloudprovider_dbname" ); prefs.setPrefValue("cloudprovider_dbname" ,cloudprovider_dbname );
        String cloudprovider_dbhost  = getInitParameter("cloudprovider_dbhost" ); prefs.setPrefValue("cloudprovider_dbhost" ,cloudprovider_dbhost );
        String cloudprovider_dbport  = getInitParameter("cloudprovider_dbport" ); prefs.setPrefValue("cloudprovider_dbport" ,cloudprovider_dbport );
        String cloudprovider_dbuser  = getInitParameter("cloudprovider_dbuser" ); prefs.setPrefValue("cloudprovider_dbuser" ,cloudprovider_dbuser );
        String cloudprovider_dbpass  = getInitParameter("cloudprovider_dbpass" ); prefs.setPrefValue("cloudprovider_dbpass" ,cloudprovider_dbpass );

        // Show init-parameters to log files
        _log.info(
             LS + "Init parameters  "
           + LS + "---------------------------------------"
           + LS + "Grid operation: '" + GridOp        + "'"
           + LS + "CloudMgr host : '" + cloudmgrHost  + "'"
           + LS + "AlephGroupName: '" + alephGroupName+ "'"
           + LS + "Aleph proxy   : '" + proxyFile     + "'"
           + LS + "portalSSHKey  : '" + portalSSHKey  + "'"
           + LS + "CA path       : '" + CAPath        + "'"
           + LS + "---------------------------------------"
           + LS + "[eTokenServer ]"
           + LS + "eTokenHost        : '" + eTokenHost         + "'"
           + LS + "eTokenPort        : '" + eTokenPort         + "'"
           + LS + "eTokenMd5Sum      : '" + eTokenMd5Sum       + "'"
           + LS + "eTokenVO          : '" + eTokenVO           + "'"
           + LS + "eTokenVOGroup     : '" + eTokenVOGroup      + "'"
           + LS + "eTokenProxyRenewal: '" + eTokenProxyRenewal + "'"
           + LS + "---------------------------------------"
           + LS + "[iservices]"
           + LS + "iservices_dbname : '" + iservices_dbname  + "'"
           + LS + "iservices_dbhost : '" + iservices_dbhost  + "'"
           + LS + "iservices_dbport : '" + iservices_dbport  + "'"
           + LS + "iservices_dbuser : '" + iservices_dbuser  + "'"
           + LS + "iservices_dbpass : '" + iservices_dbpass  + "'"
           + LS + "iservices_srvname: '" + iservices_srvname + "'"
           + LS + "---------------------------------------" 
           + LS + "[cloudprovider]"
           + LS + "cloudprovider_dbname : '" + cloudprovider_dbname  + "'"
           + LS + "cloudprovider_dbhost : '" + cloudprovider_dbhost  + "'"
           + LS + "cloudprovider_dbport : '" + cloudprovider_dbport  + "'"
           + LS + "cloudprovider_dbuser : '" + cloudprovider_dbuser  + "'"
           + LS + "cloudprovider_dbpass : '" + cloudprovider_dbpass  + "'"
           + LS + "---------------------------------------"
           + LS + "[guacamole]"
           + LS + "guacamole_dir      : '" + guacamole_dir       + "'"
           + LS + "guacamole_noauthxml: '" + guacamole_noauthxml + "'"
           + LS + "guacamole_page     : '" + guacamole_page      + "'"
           + LS + "---------------------------------------"
           + LS + "Preferences:"
           + LS + prefs.dump()
                 );

        // init iservices object
        iSrv = new iservices( iservices_srvname
                             ,iservices_dbname
                             ,iservices_dbhost
                             ,iservices_dbport
                             ,iservices_dbuser
                             ,iservices_dbpass
                            );
        if(iSrv.isEnabled()) {
            _log.info("Id for service '"+iservices_srvname+"' = "+iSrv.getServiceId());
            iSrv.initCloudMgr(proxyFile,CAPath,cloudmgrHost,eTokenHost,eTokenPort,
                              eTokenMd5Sum,eTokenVO,eTokenVOGroup,eTokenProxyRenewal.equals("true"));
            if(iSrv.isCloudMgrEnabled()) {
                   iSrv.initNoAuthConfigXML(guacamole_dir + java.io.File.separator + guacamole_noauthxml);
                   _log.info("CloudMgr is enabled");
            } else _log.info("CloudMgr not enabled");
        }
        // init cloudprovider object
        cloudProvider = new CloudProvider(cloudprovider_dbname
                                         ,cloudprovider_dbhost
                                         ,cloudprovider_dbport
                                         ,cloudprovider_dbuser
                                         ,cloudprovider_dbpass);
        if(cloudProvider.isEnabled())
             _log.info("Cloudprovider is enabled");
        else _log.info("Cloudprovider not enabled");
    } // init

    /**
     * This method retrieves common portlet information such as:
     *    - The user name
     *    - The portal name
     *    - Application home path
     *    - User's groups
     *    - User's roles
     * @param request ActionRequest object isntance 
     */
    public void getPortletInfo(ActionRequest actionRequest,ResourceRequest resourceRequest,RenderRequest renderRequest) {
        String portletInfo = LS + "------------------------------"
                           + LS + " portletInfo                  "
                           + LS + "------------------------------"
                           + LS;
        ThemeDisplay   themeDisplay   = null;
        User           user           = null;
        PortletSession portletSession = null;
        Company        company        = null;

        try {
            // Get request specific values
            if (null != actionRequest) {
                themeDisplay = (ThemeDisplay)actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
                portletSession = actionRequest.getPortletSession();
                company = PortalUtil.getCompany(actionRequest);
                if(initialized) { prefs.setPortletPrefs(actionRequest); initialized = false; }
                else prefs.getPortletPrefs(actionRequest);
            } else if(null != resourceRequest) {
                themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);
                portletSession = resourceRequest.getPortletSession();
                company = PortalUtil.getCompany(resourceRequest);
                if(initialized) { prefs.setPortletPrefs(resourceRequest); initialized = false; }
                else prefs.getPortletPrefs(resourceRequest);
            } else if(null != renderRequest) {
                themeDisplay = (ThemeDisplay)renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
                portletSession = renderRequest.getPortletSession();
                company = PortalUtil.getCompany(renderRequest);
                portalHost=themeDisplay.getPortalURL();
                if(initialized) { prefs.setPortletPrefs(renderRequest); initialized = false; }
                else prefs.getPortletPrefs(renderRequest);
            } else _log.error("Method getPortletInfo called with no valid requests!");
            
            // Retrieves portalHost, username & mail
            portalHost = themeDisplay.getPortalURL();
            portletPage= themeDisplay.getLayout().getName(themeDisplay.getLocale());
            user       = themeDisplay.getUser();
            userName   = user.getScreenName();
            firstName  = user.getFirstName();
            lastName   = user.getLastName();
            userMail   = user.getEmailAddress();
            portletInfo += ( "portalHost : '" + portalHost  + "'" + LS
                           + "portletPage: '" + portletPage + "'" + LS
                           + "User name  : '" + userName    + "'" + LS
                           + "First name : '" + firstName   + "'" + LS
                           + "Last name  : '" + lastName    + "'" + LS
                           + "User mail  : '" + userMail    + "'" + LS );
            // Retrieve user groups
            portletInfo+= "User's groups" + LS;
            long[] groups = user.getUserGroupIds();
            isAlephVMEnabled = false;
            for(int i=0; i<groups.length; i++) { 
                String groupName = UserGroupLocalServiceUtil.getUserGroup(groups[i]).getName();
                // Enable user to instantiate ALEPH VM 
                if(groupName.equalsIgnoreCase(alephGroupName)) {
                    isAlephVMEnabled = true; // Enable user to instantiate ALEPH VM
                    portletInfo += "* Enabling ALEPH VM flag *";
                    // break # disabled so _log will contain all user groups
                }
                portletInfo += "  group: '" + UserGroupLocalServiceUtil.getUserGroup(groups[i]).getName() + "'" + LS;
            }
            // Retrieve user roles
            List<Role> roles = (List<Role>) RoleServiceUtil.getUserRoles(user.getUserId());
            portletInfo += "User's roles" + LS;
            for (Role role : roles)
                portletInfo += role.getName() + " -> "+ role.getRoleId() + LS;
            // Retrieves the application pathname
            PortletContext portletContext = portletSession.getPortletContext();
            appServerPath                 = portletContext.getRealPath("/");
            portletInfo += "App server path: '" + appServerPath + "'" + LS;
            // Retrieves portal name
            portalName = company.getName();

            // 
            // Update values from preferences (they could be changed)
            //
            alephGridOperation = Integer.parseInt(prefs.getPrefValue("GridOperation"));
            proxyFile          = prefs.getPrefValue("ProxyFile"          );
            portalSSHKey       = prefs.getPrefValue("portalSSHKey"       );
            cloudmgrHost       = prefs.getPrefValue("CloudMgr"           );
            alephGroupName     = prefs.getPrefValue("AlephGroupName"     );
            eTokenHost         = prefs.getPrefValue("eTokenHost"         );
            eTokenPort         = prefs.getPrefValue("eTokenPort"         );
            eTokenMd5Sum       = prefs.getPrefValue("eTokenMd5Sum"       );
            eTokenVO           = prefs.getPrefValue("eTokenVO"           );
            eTokenVOGroup      = prefs.getPrefValue("eTokenVOGroup"      );
            eTokenProxyRenewal = prefs.getPrefValue("eTokenProxyRenewal" );
            guacamole_dir      = prefs.getPrefValue("guacamole_dir"      );
            guacamole_noauthxml= prefs.getPrefValue("guacamole_noauthxml");
            // Show taken info
            portletInfo += "------------------------------" + LS;
            _log.info(portletInfo);
        } catch (PortalException e) {
            _log.error("PortletInfo: got PortalException on actionRequest"+e.toString());
        } catch (SystemException e) {
            _log.error("PortletInfo: got SystemException on actionRequest"+e.toString());
        }
        // User services name
        String serviceInfo = "";
        iSrv.getAllocationInfo(userName);
        service_description = iSrv.getServiceShDesc();
        serviceInfo +=  LS + "--------------------------------------------------------"
                      + LS + "Allocation info for service: '" + iSrv.getServiceName() + "'"
                      + LS + "Service long description   : '" + service_description   + "'";
        if(0 >= iSrv.getNumAllocations())
             serviceInfo += LS + " No available service allocation for user '" + userName + "'";
        else serviceInfo += LS + iSrv.dumpAllocations();
        serviceInfo += LS + "--------------------------------------------------------";
        _log.info(serviceInfo);
    }

    /**
     * This method allows the portlet to process an action request; this method is normally
     * called upon each user interaction (i.e. A submit button inside a jsp' <form statement)
     * This method determines the current application mode through the actionRequest value:
     * 'PortletStatus' and then determines the correct view mode to assign through the
     * ActionResponse 'PortletStatus' variable that will be read by the doView
     * This method will also takes care about the std JSR168/286: EDIT and HELP portlet modes.
     *
     * @param request  ActionRequest object instance
     * @param response ActionResponse object instance
     *
     * @throws PortletException
     * @throws IOException
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response)
        throws PortletException, IOException
    {
        _log.info("Calling processAction()");

        // Get portlet common information
        getPortletInfo(request,null,null);

        // Determine the current portlet mode and forward this state to the response
        // Accordingly to JSRs168/286 the standard portlet modes are:
        // VIEW, EDIT, HELP
        // Supported values are referred in portlet.xml under the '<supports>' tag
        PortletMode mode = request.getPortletMode();
        response.setPortletMode(mode);
        _log.info("portletMode : '"+mode+"'");

        // Switch among different portlet modes: VIEW, EDIT, HELP
        // any custom modes can be managed adding a swicth statement
        // corresponding to the  PortletMode.<CUSTOM_MODE> value

        //----------
        // VIEW Mode
        //
        // The actionStatus value will be taken from the calling jsp file
        // through the 'PortletStatus' parameter; the corresponding
        // VIEW mode will be stored registering the portlet status
        // as render parameter. See the call to setRenderParameter
        // If the actionStatus parameter is null or empty the default
        // action will be the ACTION_MAIN (main interface)
        // This happens the first time the portlet is shown
        // The PortletStatus variable is managed by the jsp page and 
        // this java code
        //----------
        if (mode.equals(PortletMode.VIEW)) {

            // Retrieve the portlet action value, assigning a default value
            // if not specified in the Request (PortletStatus)
            String actionStatus=request.getParameter("PortletStatus");
            // Assigns the default ACTION mode
            if(null==actionStatus || actionStatus.equals(""))
            	actionStatus=""+Actions.ACTION_INPUT;

            // Different actions will be performed accordingly to the
            // different possible statuses
            int jrec = 0;
            int rg = 100;

            // Different actions will be performed accordingly to the
            // different possible statuses
            switch (Actions.valueOf(actionStatus)) {

                case ACTION_INPUT:
                    _log.info("Got action: 'ACTION_INPUT'");
                    // Create the appInput object
                    //App_Input appInput = new App_Input();
                    // Assign the correct view
                    response.setRenderParameter("PortletStatus", "" + Views.VIEW_INPUT);
                    break;

                case ACTION_DOI:
                    _log.info("Got action: 'ACTION_DOI'");
              
                    if ((request.getParameter("filterSearch").toString()).equals("doi")) {
                        String doi = (String) request.getParameter("searchText");
                        System.out.println("DOI----->" + doi);
                        searchText = doi;
                        checked = "doi";
                        getRecordsOAR(doi,jrec, rg);
                        createJsonFromAPI();
                    }

                    if ((request.getParameter("filterSearch").toString()).equals("keyword")) {
                        String keyword = (String) request.getParameter("searchText");
                        System.out.println("KEYWORD----->" + keyword);
                        searchText = keyword;
                        checked = "keyword";

                        int numF = getNumRec(keyword, jrec, rg);
                        for (int i = 0; i < numF; i++) {
                            _log.info("QUERY-->   -- " + jrec + "  -- " + rg);
                            getRecordsOAR(keyword, jrec, rg);
                            jrec = jrec + rg + 1;
                        }
                        createJsonFromAPI();
                    }

                    if ((request.getParameter("filterSearch").toString()).equals("all")) {
                        _log.info("-------ALL-----" + offset);
                        searchText = "";
                        checked = "all";
                        int numF = getNumRec("", jrec, rg);
                        for (int i = 0; i < numF; i++) {
                            _log.info("QUERY-->   -- " + jrec + "  -- " + rg);
                            getRecordsOAR("", jrec, rg);
                            jrec = jrec + rg + 1;
                        }
                        createJsonFromAPI();
                    }
                    // Assign the correct view
                    response.setRenderParameter("PortletStatus", "" + Views.VIEW_DOI);
                    break;
            }
        } // VIEW
        //----------
        // HELP Mode
        //
        // The HELP mode used to give portlet usage HELP to the user
        // This code will be called after the call to doHelp method
        //----------
        else if(mode.equals(PortletMode.HELP)) {
        }
        //----------
        // EDIT Mode
        //
        // The EDIT mode is used to view/setup portlet preferences
        // This code will be called after the user sends the actionURL
        // generated by the doEdit method
        // The code below just stores new preference values or
        // reacts to the preference settings changes
        //----------
        else if(mode.equals(PortletMode.EDIT)) {
          // Get the editAction
          String editAction=request.getParameter("editAction");
          if(editAction==null) editAction="none";
           _log.info("editAction: '"+editAction+"'");
          // Perform the corresponding editAction
          if(editAction.equals("goBack")) {
            response.setPortletMode(PortletMode.VIEW);
            response.setRenderParameter("PortletStatus", ""+Views.VIEW_INPUT);
          } else {
            _log.warn("Unhandled editAction: '"+editAction+"'");
          }
        } // EDIT Mode
        //----------
        // EDIT Mode
        //
        // Any custom portlet mode should be placed here below
        // in a proper 'else if' condition
        //----------
        else {
            // Unsupported portlet modes will come here
            _log.warn("Custom portlet mode: '"+mode.toString()+"'");
        } // CUSTOM Mode
    } // processAction

    /**
     * This method is responsible to assign the correct Application view
     * the view mode is taken from the renderRequest instance by the PortletStatus patameter
     * or automatically assigned accordingly to the Application status/default view mode
     *
     * @param request RenderRequest instance normally sent by the processAction
     * @param response RenderResponse used to send values to the jsp page
     *
     * @throws PortletException
     * @throws IOException
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response)
    throws PortletException, IOException
    {
        _log.info("Calling doView()");
        
        // Get portlet common information
        getPortletInfo(null,null,request);
        
        // Read a param from URL
        String param="";
        HttpServletRequest httpRequest = PortalUtil.getHttpServletRequest(request);
        String aleph_file = PortalUtil.getOriginalServletRequest(httpRequest).getParameter("aleph_file");

        if (aleph_file != null) {
            _log.info("aleph_file=>>" + aleph_file.split("/").length);
            param = aleph_file.split("/")[aleph_file.split("/").length-1];
            _log.info("param=>>" + param);
       }

        // Set the return content type
        response.setContentType("text/html");

        // currentView comes from the processAction; unless such method
        // is not called before (example: page shown with no user action)
        // VIEW_MAIN will be selected as default view
        String currentView=request.getParameter("PortletStatus");
        if(currentView==null) currentView="VIEW_INPUT";

        // Different actions will be performed accordingly to the
        // different possible view modes
        switch(Views.valueOf(currentView)) {
            // The following code is responsible to call the proper jsp file
            // that will provide the correct portlet interface
            case VIEW_INPUT: {
                _log.info("VIEW_MAIN Selected ...");
                request.setAttribute("param"           , param             );
                request.setAttribute("portalHost"      , portalHost        );
                request.setAttribute("portletPage"     , portletPage       );  
                request.setAttribute("isAlephVMEnabled",isAlephVMEnabled   );
                request.setAttribute("iSrv"            ,iSrv               );
                request.setAttribute("serviceDesc"     ,service_description);
                request.setAttribute("guacamole_page"  ,guacamole_page     );
                PortletRequestDispatcher dispatcher=getPortletContext().getRequestDispatcher("/search_view.jsp");
                dispatcher.include(request, response);
            }
            break;
                
           case VIEW_DOI: {
                _log.info("VIEW_SEARCH Selected ...");
                request.setAttribute("listReecords"    , listReecords      );
                request.setAttribute("portalHost"      , portalHost        );
                request.setAttribute("portletPage"     , portletPage       );
                request.setAttribute("globalList"      , globalList        );
                request.setAttribute("searchText"      , searchText        );
                request.setAttribute("checked"         , checked           );
                request.setAttribute("offset"          , offset            );
                request.setAttribute("isAlephVMEnabled",isAlephVMEnabled   );
                request.setAttribute("iSrv"            ,iSrv               );
                request.setAttribute("serviceDesc"     ,service_description);
                request.setAttribute("guacamole_page"  ,guacamole_page     );
                PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/result_search_view.jsp");
                dispatcher.include(request, response);
            }
            break;     
            default:
                _log.warn("Unknown view mode: "+currentView.toString());
        } // switch
    } // doView

    /**
     * This method is responsible to retrieve the current Application preference settings
     * and then show the edit.jsp page where the user can edit the Application preferences
     * This methods prepares an actionURL that will be used by edit.jsp file into a <input ...> form
     * As soon the user press the action button the processAction will be called going in EDIT mode
     * This method is equivalent to the doView method
     *
     * @param request Render request object instance
     * @param response Render response object instance
     *
     * @throws PortletException
     * @throws IOException
     *
     */
    @Override
    public void doEdit(RenderRequest request,RenderResponse response)
    throws PortletException,IOException {
        _log.info("Calling doEdit()");
        
        // Get portlet common information
	getPortletInfo(null,null,request);

        // Set the return content type
        response.setContentType("text/html");

        // ActionURL and the current preference value will be passed to the edit.jsp
        //PortletURL pref_actionURL = response.createActionURL();
        //request.setAttribute("<paramname>","<param_value>");
        request.setAttribute("prefs",prefs);
        request.setAttribute("portalHost" , portalHost );
        request.setAttribute("portletPage", portletPage);
        // The edit.jsp will be the responsible to show/edit the current preference values
        PortletRequestDispatcher dispatcher=getPortletContext().getRequestDispatcher("/edit.jsp");
        dispatcher.include(request, response);
    } // doEdit

    /**
     * This method just calls the jsp responsible to show the portlet information
     * This method is equivalent to the doView method
     *
     * @param request Render request object instance
     * @param response Render response object instance
     *
     * @throws PortletException
     * @throws IOException
     */
    @Override
    public void doHelp(RenderRequest request, RenderResponse response)
    throws PortletException,IOException {
        _log.info("Calling doHelp()");
        response.setContentType("text/html");
        //request.setAttribute("portletVersion",appPreferences.getPortletVersion());
        //PortletRequestDispatcher dispatcher=getPortletContext().getRequestDispatcher("/help.jsp");
        //dispatcher.include(request, response);
    } // doHelp

    /**
     * This enum contains all supported commands managed by the
     * portlet serveResource method 
     */  
    private enum serveCommands {
        none          // Unhandled command
       ,submit        // Submit the job
       ,allocinfo     // Retrieve allocation info
       ,notify        // Notify to portlet
       ,setprefs      // set preference values
       ,getprefs      // get preference values
       ,resetprefs    // back to default preference values
       ,test          // Tester command
    };

    /**
     * This method is used to make ajax calls from the portlet' js page
     *
     * @param request Render request object instance
     * @param response Render response object instance
     * 
     * @throws PortletException
     * @throws IOException
     */
    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) 
    throws PortletException, IOException {
        _log.info("Calling serverResource()");

        // Get portlet common information
        getPortletInfo(null,request,null);

        // Retrieve portletPreferences (not used yet)
        // PortletPreferences portletPreferences = (PortletPreferences) request.getPreferences();

        Map obj=new LinkedHashMap();                                         // Used to prepare JSON output 
        String commandParameters = "";                                       // Used to log commands input parameter
        String commandValue = (String) request.getParameter("command");      // Received command
        if(commandValue == null) commandValue="none";                        // Set unhandled command if no command received

        // Switch among possible commands received from the js
        switch(serveCommands.valueOf(commandValue)) {
            case test:
                // Retrieve command parameters
                String testParamValue1 = request.getParameter("testParam1");
                String testParamValue2 = request.getParameter("testParam2");
                // Prepare log parameter output
                obj.put("testParamValue1",testParamValue1);
                obj.put("testParamValue2",testParamValue2);
                commandParameters = "testParamValue1: '" + testParamValue1 + "'" 
                            + LS  + "testParamValue2: '" + testParamValue2 + "'";
                // Do something with parameters
                // ...
                // Prepare JSON ouptut
                obj.put("commandRes","OK");
                obj.put("testParamValue1",testParamValue1);
                obj.put("testParamValue2",testParamValue2);
            break;

            case setprefs:
                String jsonPrefs = (String)request.getParameter("prefs");
                _log.info( 
                      LS + "Set preferences"
                    + LS + "---------------"
                    + LS + "prefs: '" + jsonPrefs + "'"
                    + LS );
                String params[] = jsonPrefs.split(";");
                for (int i=0; i<params.length; i++) {
                    String param[] = params[i].split("=");
                    prefs.setPrefValue(param[0], param[1]);
                  //_log.info( "Param#"+i+" name: '"+param[0]+"' - Param#"+i+" value: '"+param[1]+"'"+LS);
                }
                prefs.setPortletPrefs();
                //_log.info(prefs.dump());

                // Update changes on Guacamole XML info
                if(iSrv.isCloudMgrEnabled()) {
                   guacamole_dir       = prefs.getPrefValue("guacamole_dir"      );
                   guacamole_noauthxml = prefs.getPrefValue("guacamole_noauthxml");
                   iSrv.initNoAuthConfigXML(guacamole_dir + java.io.File.separator + guacamole_noauthxml);
                }
                obj.put("commandRes","OK");
            break;

            case getprefs:
                _log.info(
                      LS + "Get preferences"
                    + LS + "---------------"
                    + LS );
                obj.put("commandRes","OK");
                String prefNames[] = prefs.getPrefNames();
                for(int i=0; i< prefNames.length; i++) {
                    obj.put(prefs.getPrefName(i),prefs.getPrefValue(prefs.getPrefName(i)));
                }
            break;

            case resetprefs:
                _log.info(
                      LS + "Reset preferences"
                    + LS + "-----------------"
                    + LS );
                obj.put("commandRes","OK");
                initialized = true; // Will force to reset prefs
                init();
            break;

            case submit:
                 // Retrieve command parameters
                 String alephfile = request.getParameter("aleph_file");
                 String alephAlg  = request.getParameter("aleph_alg");
                 _log.info(      "Submit:" 
                          + LS + "=======" 
                          + LS + "Aleph file:      '" + alephfile + "'"
                          + LS + "Aleph algorithm: '" + alephAlg  + "'"
                          );
                 // Services may be installed only having iSrv class enabled
                 if(alephAlg == null && !iSrv.isEnabled()) {
                   obj.put("commandRes" ,"KO");
                   obj.put("commandInfo","Service: '"+iSrv.getServiceName()+"' cannot be instantiated because iservice class is not available. Please contact the administrator.");
                 }
                 // In case of new service check for its maximum allowed allocations number
                 else if(alephAlg == null && iSrv.getNumAllocations() == iSrv.getMaxAllowedAllocations()) {
                   obj.put("commandRes" ,"KO");
                   obj.put("commandInfo","Reached maximum allowed allocations for service: '"+iSrv.getServiceName()+"'");
                 }
                 // Otherwise submit the job
                 else {                       
                   occiSubmit(userName,firstName,lastName,portalName,alephfile,alephAlg); 
                   obj.put("commandRes","OK");
                 }
            break;

            case allocinfo:
                 if(iSrv.isEnabled()) {
                   LinkedList allocList = new LinkedList();
                   for(int i=0; i<iSrv.getNumAllocations(); i++) {
                       // Get values from iservices
                       iservices.AllocInfo alliV[] = iSrv.allocInfo;
                       // Put values into JSON elemen
                       Map allocEntry = new LinkedHashMap();
                       allocEntry.put("allocTs"   ,""+alliV[i].getAllocTs   ());
                       allocEntry.put("allocExpTs",""+alliV[i].getAllocExpTs());
                       allocEntry.put("allocState",   alliV[i].getAllocState());
                       allocEntry.put("allocId"   ,   alliV[i].getAllocId   ());
                       allocEntry.put("srvUUID"   ,   alliV[i].getSrvUUID   ());
                       // accInfo
                       LinkedList accessList = new LinkedList();
                       if(   iSrv.allocInfo[i].accInfo != null 
                          && iSrv.allocInfo[i].accInfo.length > 0) {
                           for(int j=0; j<iSrv.allocInfo[i].accInfo.length; j++) {
                               // Get values
                               iservices.AccessInfo[] acciV = iSrv.allocInfo[i].getAccInfo();
                               // Put values
                               Map accessEntry = new LinkedHashMap();
                               accessEntry.put("ip"       ,acciV[j].getIP       ());
                               accessEntry.put("workgroup",acciV[j].getWorkGroup());
                               accessEntry.put("username" ,acciV[j].getUserName ());
                               accessEntry.put("password" ,acciV[j].getPassword ());
                               accessEntry.put("port"     ,acciV[j].getPort     ());
                               accessEntry.put("proto"    ,acciV[j].getProto    ());
                               accessList.add(accessEntry);
                           }
                       }  
                       allocEntry.put("accInfo", accessList);
                       allocList.add(allocEntry);
                   }
                   obj.put("commandRes","OK");
                   obj.put("allocInfo",allocList);
                 }
                 else {
                   _log.info("Ignoring allocinfo command since iservices class is not enabled");
                   obj.put("commandRes","KO");
                 }
            break;

            case notify:
                 // Something has to be notified to the portlet
                 String sender   = request.getParameter("sender");
                 String keyname  = request.getParameter("keyname");
                 String keyvalue = request.getParameter("keyvalue");
                 if (keyname.equalsIgnoreCase("ipaddr")) {
                   _log.info("Retrieving notification from '"+sender+"': "+keyname+"='"+keyvalue+"'");
                 }
                 else _log.info("Ignored notification message: '"+keyname+"='"+keyvalue+"' from: '"+sender+"'");
            break;

            // default condition does not work since null commands are generating an
            // exception on request.getParameter('command') call
            // the warning below could be replaced by a dedicated catch condition 
            default:
                 _log.warn("Unhandled command: '"+commandValue+"'");
                 return;
        } // swicth

        // Set the content type
        response.setContentType("application/json");

        // Prepare JSON Object
        obj.put("commandValue",commandValue);
        response.getPortletOutputStream().write(JSONValue.toJSONString(obj).getBytes());

        // Show taken parameters
        _log.info(
             LS + "Command: '" + commandValue +"'"
           + LS + "------------------------------"
           + LS + "Parameters:                   "
           + LS + "------------------------------"
           + LS + commandParameters
           + LS + "------------------------------"
           + LS + "JSON output:                  "
           + LS + "------------------------------"
           + LS + JSONValue.toJSONString(obj));
    }

    //----------------------------
    // Portlet Other Methods
    //----------------------------
    public int occiSubmit(String username, String firstName, String lastName, String portalname, String alephfile, String alephAlg) {
        // Retrieve the full  path to the job directory
        String jobPath = appServerPath+"WEB-INF/job/";
        String rOCCIResourcesList[] = null;

        Boolean useCloudProviderInfrastructure = false; 
        InfrastructureInfo infrastructure;  
        MultiInfrastructureJobSubmission mijs = new MultiInfrastructureJobSubmission();
         
        if(    cloudProvider != null
            && cloudProvider.isEnabled() 
            && cloudProvider.getProviderList(alephGridOperation) > 0) {
            if(!useCloudProviderInfrastructure) {
                _log.info("Submission aided by CloudProvider - Using the resourceList method (no Infrastructures)");
                rOCCIResourcesList = cloudProvider.getResourcesList();
                // Retrieve preference settings first
                eTokenHost         = prefs.getPrefValue("eTokenHost"         );
                eTokenPort         = prefs.getPrefValue("eTokenPort"         );
                eTokenMd5Sum       = prefs.getPrefValue("eTokenMd5Sum"       );
                eTokenVO           = prefs.getPrefValue("eTokenVO"           );
                eTokenVOGroup      = prefs.getPrefValue("eTokenVOGroup"      );
                eTokenProxyRenewal = prefs.getPrefValue("eTokenProxyRenewal" );
                // Prepare the GridEngine' InfrastructureInfo object
                infrastructure = new InfrastructureInfo( "GridCT"           // Infrastruture name
                                                        ,"rocci"             // Adaptor
                                                        ,""                  //
                                                        ,rOCCIResourcesList  // Resources list
                                                        ,eTokenHost          // eTokenServer host
                                                        ,eTokenPort          // eTokenServer port
                                                        ,eTokenMd5Sum        // eToken id (md5sum)
                                                        ,eTokenVO            // VO
                                                        ,eTokenVOGroup       // VO.group.role
                                                        ,true                // ProxyRFC
                                                       );
                // Add infrastructure to an array of infrastructures and add them to MultiInfrastructureJobSubmission object
                mijs.addInfrastructure(infrastructure);
            }
            else {
                _log.info("Submission aided by CloudProvider - Using the infrastructure method");
                cloudProvider.AppInfrastructures(alephGridOperation);
                //InfrastructureInfo[] infrastructuresInfo = new InfrastructureInfo[cloudProvider.infrastructuresList.size()];
                for(int i=0; i<cloudProvider.infrastructuresList.size(); i++) {
                    CloudProvider.Infrastructure cpinfra = (CloudProvider.Infrastructure) cloudProvider.infrastructuresList.get(i);
                    _log.info("Infrastructure["+i+"]:"
                          +LS+"     name: '"+cpinfra.name+"'"
                          +LS+"  adaptor: '"+cpinfra.adaptor+"'"
                          +LS+"  enabled: '"+cpinfra.enabled+"'"
                             );
                    infrastructure = new InfrastructureInfo(cpinfra.name
                                                           ,cpinfra.adaptor
                                                           ,""
                                                           ,cpinfra.resourceList()
                                                           ,cpinfra.getParam("etoken_host")
                                                           ,cpinfra.getParam("etoken_port")
                                                           ,cpinfra.getParam("etoken_id")
                                                           ,cpinfra.getParam("VO")
                                                           ,cpinfra.getParam("VO_GroupRole")
                                                           ,cpinfra.getParam("ProxyRFC").equalsIgnoreCase("true")
                                                           );
                    //infrastructuresInfo[i]=infraInfo;
                    // Enabled flag is obtained ANDing Infrastructure main flag with applicationInfrastructure flag
                    if(cpinfra.enabled) {
                       mijs.addInfrastructure(infrastructure);
                       _log.info("Added infrastructure - '"+cpinfra.name+"'");
                    }
                    else _log.info("Skipping infrastructure - '"+cpinfra.name+"' because not enabled");
                }                
            }
        }
        else {
          // Setup CloudProvider object for nebula-server-01
          CloudProvider cp1 = new CloudProvider("nebula-server-01"
                                               ,"nebula-server-01.ct.infn.it"
                                               ,9000
                                               ,"rocci"
                                              );
          // Assign OCCI parameters to the CloudProvider object
          String params1[][] = {
              { "resource"          , "compute"                   }
             ,{ "action"            , "create"                    }
             ,{ "attributes_title"  , "aleph2k"                   }
             ,{ "mixin_os_tpl"      , "uuid_aleph2000_vm_71"      }
             ,{ "mixin_resource_tpl", "small"                     }
             ,{ "auth"              , "x509"                      }
           //,{ "publickey_file"    , jobPath + ".ssh/id_dsa.pub" } // (!) UNUSED; GE uses: $HOME/.ssh/id_rsa.pub
           //,{ "privatekey_file"   , jobPath + ".ssh/id_dsa"     } // (!) UNUSED; GE uses: $HOME/.ssh/id_rsa
          };
          // Add OCCI parameters to the cloud provider object
          cp1.addParams(params1);

          // Setup CloudProvider object for stack-server-01
          CloudProvider cp2 = new CloudProvider("stack-server-01"
                                               ,"stack-server-01.ct.infn.it"
                                               ,8787
                                               ,"rocci"
                                              );
          // Assign OCCI parameters to the CloudProvider object
          String params2[][] = {
              { "resource"          , "compute"                              }
             ,{ "action"            , "create"                               }
             ,{ "attributes_title"  , "aleph2k"                              }
             ,{ "mixin_os_tpl"      , "c3484114-9c67-44ff-a3da-ea9e6058fe3b" }
             ,{ "mixin_resource_tpl", "m1-large"                             }
             ,{ "auth"              , "x509"                                 }
          };

          // Add OCCI parameters to the cloud provider object
          cp2.addParams(params2);


          // Retrieve from cloud provider objects the OCCI endpoints
          String rOCCIURL1 = cp1.endPoint();  _log.info("OCCI Endpoint1: '" + rOCCIURL1 + "'");
          String rOCCIURL2 = cp2.endPoint();  _log.info("OCCI Endpoint2: '" + rOCCIURL2 + "'");

          // Prepare the ROCCI resource list
          String resList[] = { rOCCIURL1
                          //  ,rOCCIURL2
                             };
          rOCCIResourcesList = resList;

          // Prepare the GridEngine' InfrastructureInfo object
          infrastructure = new InfrastructureInfo( "GridCT"                          // Infrastruture name
                                                 ,"rocci"                           // Adaptor
                                                 ,""                                //
                                                 ,rOCCIResourcesList                // Resources list
                                                 ,"etokenserver.ct.infn.it"         // eTokenServer host
                                                 ,"8082"                            // eTokenServer port
                                                 ,"bc779e33367eaad7882b9dfaa83a432c"// eToken id (md5sum)
                                                 ,"fedcloud.egi.eu"                 // VO
                                                 ,"fedcloud.egi.eu"                 // VO.group.role
                                                 ,true                              // ProxyRFC
                                                 );
          // Add infrastructure to an array of infrastructures and add them to MultiInfrastructureJobSubmission object
          mijs.addInfrastructure(infrastructure);
        }

        // Cloud job requires a valid proxy to operate
        // The eTokenserver proxy will be included into the inputSandbox
        //File temp=null;
        //String alephPxyFile = "/tmp/aleph.pxy";
        //temp = new File(alephPxyFile);
        iSrv.getRobotProxyFile("etokenserver.ct.infn.it"
                              ,"8082"
                              ,"bc779e33367eaad7882b9dfaa83a432c"
                              ,"fedcloud.egi.eu"
                              ,"fedcloud.egi.eu"
                              ,"true"
                              ,proxyFile);

        // Build the job identifier
        String[] alephfile_path = { "" };
        String alephfileName = "";
        int vmduration = -1;
        if (null != alephfile) {
          alephfile_path = alephfile.split("/");            
          alephfileName = alephfile_path[alephfile_path.length-1];
        }
        String vmuuid = "";
        String GE_JobId = "aleph: ";
        if(isAlephVMEnabled && alephAlg == null) {
             String moreInfo = "";
             if(!alephfileName.equals(""))
                 moreInfo = "'"+alephfile+"'"; 
             vmuuid     = iSrv.getUUID();
             vmduration = iSrv.getServiceDuration(); 
             GE_JobId = "VM('"+ vmuuid + "') " + moreInfo;
        }
        else GE_JobId = "'" + alephfileName + "'";
        
        // Set job properties
        mijs.setExecutable("aleph.sh");                              // Executable
        mijs.setArguments(                                           // Arguments
                          (null==alephfile?"\"\"":alephfile) + " "   //   aleph file 
                         +proxyFile                          + " "   //   proxy certificate file (having full path)
                         +portalSSHKey                       + " "   //   portal public SSH key (having full path)
                         +portalHost                         + " "   //   portlal host (needed to get notify) 
                         +username                           + " "   //   portal username
                    +"\""+firstName                          + "\" " //   portal user first name
                    +"\""+lastName                           + "\" " //   portal user last name
                         +userMail                           + " "   //   portal user email address
                         +cloudmgrHost                       + " "   //   cloudmgr contacting URL
                    +"\""+vmuuid                             + "\" " //   VM UUID
                         +(null==alephAlg?"\"\"":alephAlg)   + " "   //   ALEPH analisys application (MITQCD,6LEP,...)
                    +"\""+portalname                         + "\" " //   portal name
                         +vmduration                         + " "   //   VM duration in seconds
                    +"\""+GE_JobId                           + "\" " //   the job identifier
                         ); 
        mijs.setJobOutput("stdout.txt");                             // std-output
        mijs.setJobError("stderr.txt");                              // std-error
        mijs.setOutputPath("/tmp/");                                 // Output path
        mijs.setInputFiles(jobPath+"aleph.sh" + "," +                // Aleph pilot script
                           proxyFile          + "," +                // proxy file full path
                           portalSSHKey                              // portal public ssh key
                          );                                         // InputSandbox
        mijs.setOutputFiles("aleph_output.tar");                     // OutputSandbox
          
        // Determine the host IP address
        String   portalIPAddress="";                
        try {
            InetAddress addr = InetAddress.getLocalHost();
            byte[] ipAddr=addr.getAddress();
            portalIPAddress= ""+(short)(ipAddr[0]&0xff)
                           +":"+(short)(ipAddr[1]&0xff)
                           +":"+(short)(ipAddr[2]&0xff)
                           +":"+(short)(ipAddr[3]&0xff);
        }
        catch(Exception e) {
            _log.error("Unable to get the portal IP address");
        } 

        // Submit the job     
        // Submission uses addInfrastructure method; this call is no longer necessary
        // mijs.submitJobAsync(infrastructure, username, portalIPAddress, alephGridOperation, GE_JobId);
        mijs.submitJobAsync(username, portalIPAddress, alephGridOperation, GE_JobId);

        // Remove proxy temporary file
        // temp.delete(); Cannot remove here the file, job submission fails
        
        // Interactive job execution (iservices)
        if(isAlephVMEnabled && alephAlg == null) {
            iSrv.allocService(username,vmuuid);
            iSrv.dumpAllocations();
        }
        return 0;
    }

    public static HttpMethod callAPIOAR(String search, int jrec, int num_rec) {
        HttpMethod method = null;
        HttpClient client = new HttpClient();
        method = new GetMethod("https://www.openaccessrepository.it/search?of=xm&cc=REAL%20DATA&p=" + search + "&jrec=" + jrec + "&rg=" + num_rec);
        System.out.println("QUERY=>>>>>https://www.openaccessrepository.it/search?of=xm&cc=REAL%20DATA&p=" + search + "&jrec=" + jrec + "&rg=" + num_rec);
        return method;
    }

    public static int getNumRec(String date, int jrec, int num_rec) {
        HttpClient client = new HttpClient();
        HttpMethod method = callAPIOAR(date, jrec, num_rec);
        double numRec = 0;
        int numFor = 0;
        String responseXML = null;
        BufferedReader br = null;
        try {
            client.executeMethod(method);
            if (method.getStatusCode() == HttpStatus.SC_OK) {
                method.getResponseBody();
                responseXML = convertStreamToString(method.getResponseBodyAsStream());
                numRec = Double.parseDouble(responseXML.split("Results:")[1].split("-->")[0].replace(" ", ""));
                System.out.println("NUM REC=>" + numRec / 100);
                numFor = (int) Math.ceil(numRec / 100);
                System.out.println("NUM REC=>" + numFor);
                method.releaseConnection();
            }

        } catch (IOException ex) {
            Logger.getLogger(aleph_portlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return numFor;
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        if (is != null) {
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);

                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    public void getRecordsOAR(String search, int jrec, int num_rec) {
        String responseXML = null;
        HttpClient client = new HttpClient();
        HttpMethod method = callAPIOAR(search, jrec, num_rec);
        try {
            client.executeMethod(method);
            if (method.getStatusCode() == HttpStatus.SC_OK) {
                method.getResponseBody();
                responseXML = convertStreamToString(method.getResponseBodyAsStream());
                FileWriter fw = new FileWriter(appServerPath + "datatable/marcXML_OAR_" + jrec + "_" + num_rec + ".xml");
                System.out.println();
                fw.append(responseXML);
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
    }

    public void createJsonFromAPI() {
        try {
            JSONObject obj = new JSONObject();
            JSONArray list = new JSONArray();
            String title = "";
            String url_invenio = "";
            String link_data = "";
            String abs = "";
            int count = 0;
            //creation of a json file output
            FileOutputStream out = null;
            BufferedWriter output = null;
            File file = new File(appServerPath + "datatable/exampleJson.txt");
            output = new BufferedWriter(new FileWriter(file));
            File f = new File(appServerPath + "datatable/");
            for (int i = 0; i < f.listFiles().length; i++) {
                if (f.listFiles()[i].getName().contains("marcXML_OAR")) {
                    SAXBuilder builder = new SAXBuilder();
                    Document document = builder.build(new File(appServerPath + "datatable/" + f.listFiles()[i].getName()));
                    Element root = document.getRootElement();
                    String nomeRadice = root.getName();
                   // System.out.println("TAG ROOT--->" + nomeRadice);
                    //Estraggo i figli dalla radice 
                    List children = root.getChildren();
                    Iterator iterator = children.iterator();

                    //Per ogni figlio record
                    while (iterator.hasNext()) {
                        JSONObject info = new JSONObject();
                        Element itemRecord = (Element) iterator.next();
                        String nomeTag = itemRecord.getName();
                        //System.out.println("Nome Tag=>" + count + ")" + nomeTag);
                        //prendo i figli di record che sono o controlfield o datafield
                        List childrenRecord = itemRecord.getChildren();
                        Iterator iteratorRecord = childrenRecord.iterator();
                        // System.out.println("FIGLI DI RECORD: " + childrenRecord.size());
                        while (iteratorRecord.hasNext()) {
                            Element childRecord = (Element) iteratorRecord.next();
                            String childName = childRecord.getName();
                            //  System.out.println("CHILD NAME" + childName);
                            if (childName.equals("controlfield")) {
                                String attributeTag = childRecord.getAttribute("tag").getValue();
                                // System.out.println("ATTRIBUTE TAG: " + attributeTag);
                                if (attributeTag.equals("001")) {
                                    url_invenio = "https://www.openaccessrepository.it/record/" + childRecord.getValue();
                                //    System.out.println("URL INVENIO " + count + ") " + url_invenio);
                                }
                            } else {
                                String attributeTag = childRecord.getAttribute("tag").getValue();
                                if (attributeTag.equals("856")) {
                                    List childrenDataField = childRecord.getChildren();
                                    Iterator iteratorchildrenDataField = childrenDataField.iterator();
                                    while (iteratorchildrenDataField.hasNext()) {
                                        Element childDataField = (Element) iteratorchildrenDataField.next();
                                        String childDataFieldNome = childDataField.getName();
                                        String attribute_code = childDataField.getAttribute("code").getValue();
                                        if (attribute_code.equals("u")) {
                                            link_data = childDataField.getValue();
                                          //  System.out.println("LINKDATA=>" + count + ") " + link_data);
                                        }
                                    }
                                }

                                if (attributeTag.equals("245")) {
                                    List childrenDataField = childRecord.getChildren();
                                    Iterator iteratorchildrenDataField = childrenDataField.iterator();
                                    while (iteratorchildrenDataField.hasNext()) {
                                        Element childDataField = (Element) iteratorchildrenDataField.next();
                                        String attribute_code = childDataField.getAttribute("code").getValue();
                                        if (attribute_code.equals("a")) {
                                            title = childDataField.getValue();
                                            //System.out.println("TITLE=>" + count + ") " + title);
                                        }
                                    }
                                }
                                if (attributeTag.equals("520")) {
                                    List childrenDataField = childRecord.getChildren();
                                    Iterator iteratorchildrenDataField = childrenDataField.iterator();
                                    while (iteratorchildrenDataField.hasNext()) {
                                        Element childDataField = (Element) iteratorchildrenDataField.next();
                                        String attribute_code = childDataField.getAttribute("code").getValue();
                                        if (attribute_code.equals("a")) {
                                            abs = childDataField.getValue();
                                          //  System.out.println("ABS=>" + count + ") " + abs);
                                        }
                                    }
                                }
                            }
                        }
                        info.put("title", title);
                        info.put("abs", abs);
                        info.put("link_data", link_data);
                        info.put("url", url_invenio);
                        info.put("type_analisys", "<select id='"+count+"sel'> <option  value='6LEP'>6LEP</option> <option value='MITQCD'>MITQCD</option></select>");
                        info.put("action", "<input class='buttonAnalyze' id='" + count + "' type='button' value='Analyze' onclick='submitAction(this.id)'>");
                        list.add(info);
                        count++;
                    }
                }
            }
            obj.put("data", list);
            output.write(obj.toJSONString());
            output.flush();
            output.close();
               for (int j = 0; j < (f.listFiles().length)-1; j++){
                while (f.listFiles()[j].getName().contains("marcXML_OAR")){
                    boolean success = (new File(appServerPath + "datatable/"+f.listFiles()[j].getName())).delete();
                }
            }
        } catch (JDOMException ex) {
            Logger.getLogger(aleph_portlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(aleph_portlet.class.getName()).log(Level.SEVERE, null, ex);
        }

    }    

} // aleph_portlet 

