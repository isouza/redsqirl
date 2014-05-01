package idiro.workflow.server;

import idiro.BlockManager;
import idiro.Log;
import idiro.hadoop.NameNodeVar;
import idiro.tm.task.in.Preference;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

/**
 * Software preference manager.
 * 
 * The class contains different way of accessing properties.
 * In order to look properties of the current user, you don't need
 * to specify a user in the function.
 * 
 * @author etienne
 *
 */
public class WorkflowPrefManager extends BlockManager {

	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(WorkflowPrefManager.class);

	/**
	 * The single instance of process runner.
	 */
	private static WorkflowPrefManager runner = new WorkflowPrefManager();

	/**
	 * System Preferences
	 */
	private final static Preferences systemPrefs = Preferences
			.systemNodeForPackage(WorkflowPrefManager.class);

	public final static Preference<String>

	/**
	 * RedSqirl home directory path
	 */
	pathSysHome = new Preference<String>(systemPrefs, "Path Home",
			"/usr/share/redsqirl");

	public static Preference<String>
	/**
	 * Root of the system specific preferences
	 */
	pathSystemPref = new Preference<String>(systemPrefs,
			"Path to store/retrieve system preferences", pathSysHome.get()
					+ "/conf"),

			/**
			 * Path of the packages
			 */
			pathSysPackagePref = new Preference<String>(systemPrefs,
					"Path to retrieve system packages", pathSysHome.get()
							+ "/packages"),
			/**
			 * System preference file
			 */
			pathSysCfgPref = new Preference<String>(systemPrefs,
					"Path to retrieve general system configuration",
					pathSystemPref.get() + "/idm_sys.properties"),
			/**
			 * System lang preference file.These properties are optional and are
			 * used by the front-end to give a bit more details about user
			 * settings. For each user property, you can create a #{key}_label
			 * and a #{key}_desc property.
			 */
			pathSysLangCfgPref = new Preference<String>(systemPrefs,
					"Path to retrieve labels of sys parameters",
					pathSystemPref.get() + "/idm_sys_lang.properties"),

			/**
			 * Path users folder
			 */
			pathUsersFolder = new Preference<String>(systemPrefs,
					"Path to store/retrieve system preferences",
					pathSysHome.get() + "/users");

	private static String
	/**
	 * Root of the user specific preferences. Accessible from idm-workflow side.
	 */
	pathUserPref = pathUsersFolder.get() + "/"
			+ System.getProperty("user.name"),
	/**
	 * Where to find the icons menu. Accessible from idm-workflow side.
	 */
	pathIconMenu = pathUserPref + "/icon_menu",
	/**
	 * User Tmp folder. Accessible from idm-workflow side.
	 */
	pathTmpFolder = pathUserPref + "/tmp",
	/**
	 * The local directory to store oozie specific data. Accessible from
	 * idm-workflow side.
	 */
	pathOozieJob = pathUserPref + "/jobs",
	/**
	 * The local directory to store temporarily workflow. Accessible from
	 * idm-workflow side.
	 */
	pathWorkflow = pathUserPref + "/workflows",
	/**
	 * Colour pref file. Accessible from idm-workflow side.
	 */
	pathUserDFEOutputColour = pathUserPref + "/output_colours.properties",
	/**
	 * Path of the user packages. Accessible from idm-workflow side.
	 */
	pathUserPackagePref = pathUserPref + "/packages",
	/**
	 * Lib Path for system package
	 */
	sysPackageLibPath = pathSysHome.get() + "/lib/packages",
	/**
	 * Lib Path for user package. Accessible from idm-workflow side.
	 */
	userPackageLibPath = pathUserPref + "/lib/packages",
	/**
	 * Help directory path from package install directory.
	 */
	pathSysHelpPref = "/packages/help",

	/**
	 * Icon Image directory path from package install directory.
	 */
	pathSysImagePref = "/packages/images",

	/**
	 * Icon Image directory path for user packages. Accessible from idm-workflow
	 * side.
	 */
	pathUserImagePref = "/packages/" + System.getProperty("user.name")
			+ "/images",

			/**
			 * Help directory path for user packages. Accessible from
			 * idm-workflow side.
			 */
			pathUserHelpPref = "/packages/" + System.getProperty("user.name")
					+ "/help";

	// User preferences
	/**
	 * User Preferences
	 */
	private final static Preferences userPrefs = Preferences
			.userNodeForPackage(WorkflowPrefManager.class);

	/**
	 * User properties. These properties cannot be changed in a production
	 * environment. However they can be changed for back-end unit-testing.
	 */
	public static Preference<String>
	/**
	 * User properties with specific user settings.
	 */
	pathUserCfgPref = new Preference<String>(userPrefs,
			"Path to retrieve general user configuration", pathUserPref
					+ "/idm_user.properties"),
	/**
	 * User lang properties. These properties are optional and are used by the
	 * front-end to give a bit more details about user settings. For each user
	 * property, you can create a #{key}_label and a #{key}_desc property.
	 */
	pathUserLangCfgPref = new Preference<String>(userPrefs,
			"Path to retrieve labels of sys parameters", pathUserPref
					+ "/idm_user_lang.properties");

	/**
	 * True if the instance is initialised.
	 */
	private boolean init = false;

	/** Namenode url */
	public static final String sys_namenode = "namenode",
	/** idiro engine path */
	sys_idiroEngine_path = "idiroengine_path",
	/** Max number of workers for Giraph */
	sys_max_workers = "max_workers",
	/** Job Tracker URL for hadoop */
	sys_jobtracker = "jobtracker",
	/** Default queue for hadoop */
	sys_oozie_queue = "queue",
	/** Oozie URL */
	sys_oozie = "oozie_url",
	/** Oozie xml schema location */
	sys_oozie_xmlns = "oozie_xmlns",
	// sys_oozie_build_mode = "oozie_build_mode",
			/** Default Hive XML */
			sys_hive_default_xml = "hive_default_xml",
			/** Hive XML */
			sys_hive_xml = "hive_xml",
			/** Hive Extra Lib */
			sys_hive_extralib = "hive_extra_lib",
			/** Allow a user to install */
			sys_allow_user_install = "allow_user_install",
			/** Path for tomcat */
			sys_tomcat_path = "tomcat_path",
			/** Path for installed packages */
			sys_install_package = "package_dir",
			/** URL for Package Manager */
			sys_pack_manager_url = "pack_manager_url",
			/** Trusted host to packages */
			sys_pack_download_trust = "trusted_pack_hosts",
			/** The admin user */
			sys_admin_user = "admin_user";
	/** Hive JDBC Url */
	public static final String user_hive = "hive_jdbc_url",
	/** Path to Private Key */
	user_rsa_private = "private_rsa_key",
	/** Backup Path of workflow on HFDS */
	user_backup = "backup_path",
	/** Maximum Number of Paths */
	user_nb_backup = "number_backup",
	/** Number of oozie job directories to keep */
	user_nb_oozie_dir_tokeep = "number_oozie_job_directory_tokeep",
	/** Path on HDFS to store Oozie Jobs */
	user_hdfspath_oozie_job = "hdfspath_oozie_job";

	/**
	 * Constructor.
	 * 
	 */
	private WorkflowPrefManager() {

	}

	/**
	 * 
	 * @return Returns the single allowed instance of ProcessRunner
	 */
	public static WorkflowPrefManager getInstance() {
		if (!runner.init) {
			runner.init = true;
			// Loads in the log settings.
			Log.init();
			NameNodeVar.set(getUserProperty(sys_namenode));
		}
		return runner;
	}

	/**
	 * Create the given user redsqirl home directory if it does not exist.
	 * 
	 * @param userName
	 */
	public static void createUserHome(String userName) {
		File home = new File(getPathUserPref(userName));
		logger.debug(home.getAbsolutePath());
		if (!home.exists()) {
			home.mkdirs();

			File packageF = new File(getPathUserPackagePref(userName));
			logger.debug(packageF.getAbsolutePath());
			packageF.mkdir();

			File libPackage = new File(getUserPackageLibPath(userName));
			logger.debug(libPackage.getAbsolutePath());
			libPackage.mkdirs();

			// Everybody is able to write in this home folder
			logger.debug("set permissions...");
			home.setWritable(true, false);
			home.setReadable(true, false);
		}
	}

	/**
	 * Setup a the redsqirl home directory from the back-end.
	 */
	public static void setupHome() {
		File iconMenu = new File(pathIconMenu);
		if (!iconMenu.exists()) {
			iconMenu.mkdir();
		}
		File userProp = new File(pathUserCfgPref.get());
		File userPropLang = new File(pathUserLangCfgPref.get());
		if (!userProp.exists()) {
			Properties prop = new Properties();
			prop.setProperty(user_hive, "");

			Properties propLang = new Properties();
			propLang.setProperty(user_hive + "_label", "JDBC URL");
			propLang.setProperty(user_hive + "_desc", "JDBC URL");
			try {
				prop.store(new FileWriter(userProp), "");
				propLang.store(new FileWriter(userPropLang), "");
			} catch (IOException e) {
				logger.warn("Fail to write default properties");
			}
		}
	}

	/**
	 * Is WorkflowPrefManager initialized
	 * 
	 * @return <code>true</code> if initialize else <code>false</code>
	 */
	public boolean isInit() {
		return init;
	}

	/**
	 * Change the sys home property, and update dependency properties.
	 * 
	 * If the sys home property changed, most of other class value that depends
	 * of it have to change. This function update the syshome and all other
	 * properties.
	 * 
	 * @param newValueSysHome
	 *            new value of @see pathSysHome . If null it removes the
	 *            property and use the default.
	 */
	public static void changeSysHome(String newValueSysHome) {

		if (newValueSysHome == null || newValueSysHome.isEmpty()) {
			pathSysHome.remove();
		} else {
			pathSysHome.put(newValueSysHome);
		}

		pathSysPackagePref = new Preference<String>(systemPrefs,
				"Path to retrieve system packages", pathSysHome.get()
						+ "/packages");
		pathSysCfgPref = new Preference<String>(systemPrefs,
				"Path to retrieve general system configuration",
				pathSystemPref.get() + "/idm_sys.properties");
		pathUsersFolder = new Preference<String>(systemPrefs,
				"Path to store/retrieve system preferences", pathSysHome.get()
						+ "/users");

		pathUserPref = pathUsersFolder.get() + "/"
				+ System.getProperty("user.name");
		pathIconMenu = pathUserPref + "/icon_menu";
		pathTmpFolder = pathUserPref + "/tmp";
		pathOozieJob = pathUserPref + "/jobs";
		pathWorkflow = pathUserPref + "/workflows";
		pathUserDFEOutputColour = pathUserPref + "/output_colours.properties";
		pathUserPackagePref = pathUserPref + "/packages";
		sysPackageLibPath = pathSysHome.get() + "/lib/packages";
		userPackageLibPath = pathUserPref + "/lib/packages";
		pathSysHelpPref = "/packages/help";
		pathSysImagePref = "/packages/images";

		pathUserImagePref = "/packages/" + System.getProperty("user.name")
				+ "/images";

		pathUserHelpPref = "/packages/" + System.getProperty("user.name")
				+ "/help";

		pathUserCfgPref = new Preference<String>(userPrefs,
				"Path to retrieve general user configuration", pathUserPref
						+ "/idm_user.properties");
	}

	/**
	 * Reset the System preferences
	 */
	public static void resetSys() {
		pathSystemPref.remove();
		pathSysHome.remove();
		pathSysPackagePref.remove();
		pathSysCfgPref.remove();
	}

	/**
	 * Reset the User preferences
	 */
	public static void resetUser() {
		pathUserCfgPref.remove();
	}

	/**
	 * Get the properties for System
	 * 
	 * @return system properties
	 */
	public static Properties getSysProperties() {
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(pathSysCfgPref.get())));
		} catch (Exception e) {
			logger.error("Error when loading " + pathSysCfgPref.get() + " "
					+ e.getMessage());
		}
		return prop;
	}

	/**
	 * Get the lang properties for the System
	 * 
	 * @return
	 */
	public static Properties getSysLangProperties() {
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(pathSysLangCfgPref.get())));
		} catch (Exception e) {
			logger.error("Error when loading " + pathSysLangCfgPref.get() + " "
					+ e.getMessage());
		}
		return prop;
	}

	/**
	 * Get the properties for System
	 * 
	 * @return system properties
	 */
	public static Properties getUserProperties() {
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(pathUserCfgPref.get())));
		} catch (Exception e) {
			logger.error("Error when loading " + pathUserCfgPref + " "
					+ e.getMessage());
		}
		return prop;
	}

	/**
	 * Get the user properties a given user.
	 * 
	 * @param user
	 *            The user name
	 * @return The user properties.
	 */
	public static Properties getUserProperties(String user) {
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(getPathUserPref(user)
					+ "/idm_user.properties")));
		} catch (Exception e) {
			logger.error("Error when loading " + getPathUserPref(user)
					+ "/idm_user.properties" + e.getMessage());
		}
		return prop;
	}

	/**
	 * Get the lang properties of the given user.
	 * 
	 * @param user
	 *            The user name
	 * @return The lang user properties.
	 */
	public static Properties getUserLangProperties(String user) {
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(getPathUserPref(user)
					+ "/idm_user_lang.properties")));
		} catch (Exception e) {
			logger.error("Error when loading " + getPathUserPref(user)
					+ "/idm_user_lang.properties" + e.getMessage());
		}
		return prop;
	}

	/**
	 * Get a System property
	 * 
	 * @param key
	 *            property to receive
	 * @return property from system properties
	 */
	public static String getSysProperty(String key) {
		return getSysProperties().getProperty(key);
	}

	/**
	 * 
	 * Get a System property
	 * 
	 * @param key
	 *            property requested
	 * @param defaultValue
	 *            if requested value is null
	 * @return property from system properties
	 */
	public static String getSysProperty(String key, String defaultValue) {
		return getSysProperties().getProperty(key, defaultValue);
	}

	/**
	 * Get a User property
	 * 
	 * @param key
	 *            property to receive
	 * @return property from user properties
	 */
	public static String getUserProperty(String key) {
		return getUserProperties().getProperty(key);
	}

	/**
	 * 
	 * Get a User property
	 * 
	 * @param key
	 *            property requested
	 * @param defaultValue
	 *            if requested value is null
	 * @return property from User properties
	 */
	public static String getUserProperty(String key, String defaultValue) {
		return getUserProperties().getProperty(key, defaultValue);
	}

	/**
	 * Get the path of the jobs on HDFS
	 * 
	 * @return path of the jobs on HDFS
	 */
	public static String getHDFSPathJobs() {
		String path = getUserProperty(WorkflowPrefManager.user_hdfspath_oozie_job);
		if (path == null || path.isEmpty()) {
			path = "/user/" + System.getProperty("user.name") + "/.idm/jobs";
		}
		return path;
	}

	/**
	 * Get the path of the backups on HDFS
	 * 
	 * @return path of the backups on HDFS
	 */
	public static String getBackupPath() {
		String path = getUserProperty(WorkflowPrefManager.user_backup);
		if (path == null || path.isEmpty()) {
			path = "/user/" + System.getProperty("user.name") + "/idm-backup";
		}
		return path;
	}

	/**
	 * Get the path to the private key
	 * 
	 * @return path to private key
	 */
	public static String getRsaPrivate() {
		String path = getUserProperty(WorkflowPrefManager.user_rsa_private);
		if (path == null || path.isEmpty()) {
			path = System.getProperty("user.home") + "/.ssh/id_rsa";
		}
		return path;
	}

	/**
	 * Get the maximum number of backups for the user allowed
	 * 
	 * @return the maximum number of backups allowed
	 */
	public static int getNbBackup() {
		String numberBackup = getUserProperty(WorkflowPrefManager.user_nb_backup);
		int nbBackup = 25;
		if (numberBackup != null) {
			try {
				nbBackup = Integer.valueOf(numberBackup);
				if (nbBackup < 0) {
					nbBackup = 25;
				}
			} catch (Exception e) {
			}
		}
		return nbBackup;
	}

	/**
	 * Get the maximum number of Oozie Directories to keep
	 * 
	 * @return get the maximum number of Oozie Directories to keep
	 */
	public static int getNbOozieDirToKeep() {
		String numberBackup = getUserProperty(WorkflowPrefManager.user_nb_backup);
		int nbOozieDir = 25;
		if (numberBackup != null) {
			try {
				nbOozieDir = Integer.valueOf(numberBackup);
				if (nbOozieDir < 0) {
					nbOozieDir = 25;
				} else if (nbOozieDir == 0) {
					nbOozieDir = 1;
				}
			} catch (Exception e) {
			}
		}
		return nbOozieDir;

	}

	/**
	 * Get the package manager trusted host list to download from
	 * 
	 * @return package manager trusted host list to download from
	 */
	public static String[] getPackTrustedHost() {
		String[] trustedURL = new String[0];
		String pack = getSysProperty(WorkflowPrefManager.sys_pack_download_trust);
		if (pack != null && !pack.isEmpty()) {
			trustedURL = pack.split(";");
		}
		return trustedURL;
	}

	/**
	 * Get the Package Manager URI
	 * 
	 * @return URI for the Package Manager
	 */
	public static String getPckManagerUri() {
		String uri = getSysProperty(WorkflowPrefManager.sys_pack_manager_url);
		if (uri == null || uri.isEmpty()) {
			uri = "http://localhost:9090/idm-repo";
		}
		return uri;
	}

	/**
	 * Get the Systen Administrator Username
	 * 
	 * @return User name for the System Administrator
	 */
	public static String[] getSysAdminUser() {
		String[] sysUsers = null;
		String pack = getSysProperty(WorkflowPrefManager.sys_admin_user);
		if (pack != null && !pack.isEmpty()) {
			sysUsers = pack.split(":");
		}
		return sysUsers;
	}

	/**
	 * Check if User is allowed to install Packages
	 * 
	 * @return <code>true</code> if User is allowed to install else
	 *         <code>false</code>
	 */
	public static boolean isUserPckInstallAllowed() {
		return getSysProperty(WorkflowPrefManager.sys_allow_user_install,
				"FALSE").equalsIgnoreCase("true");
	}

	public static void main(String[] args) {
		for (int i = 0; i < args.length; ++i) {
			if (args[i].contains("=")
					&& args[i].indexOf('=') == args[i].lastIndexOf('=')) {
				String[] pref = args[i].split("=");

				if (pref[0].equalsIgnoreCase("pathSystemPref")) {
					pathSystemPref.put(pref[1]);
				} else if (pref[0].equalsIgnoreCase("pathSysHome")) {
					pathSysHome.put(pref[1]);
				} else if (pref[0].equalsIgnoreCase("pathSysPackagePref")) {
					pathSysPackagePref.put(pref[1]);
				} else if (pref[0].equalsIgnoreCase("pathSysCfgPref")) {
					pathSysCfgPref.put(pref[1]);
				}
			}
		}
	}

	/**
	 * User home directory.
	 * @param user
	 * @return
	 */
	public static String getPathUserPref(String user) {
		return pathUsersFolder.get() + "/" + user;
	}

	/**
	 * User icon menu directory.
	 * @param user
	 * @return
	 */
	public static String getPathIconMenu(String user) {
		return getPathUserPref(user) + "/icon_menu";
	}

	/**
	 * User temporary folder.
	 * @param user
	 * @return
	 */
	public static String getPathTmpFolder(String user) {
		return getPathUserPref(user) + "/tmp";
	}

	/**
	 * User oozie job folder
	 * @param user
	 * @return
	 */
	public static String getPathOozieJob(String user) {
		return getPathUserPref(user) + "/jobs";
	}

	/**
	 * User temporary workflow folder.
	 * @param user
	 * @return
	 */
	public static String getPathWorkflow(String user) {
		return getPathUserPref(user) + "/workflows";
	}

	/**
	 * User property file.
	 * @param user
	 * @return
	 */
	public static String getPathUserCfgPref(String user) {
		return getPathUserPref(user) + "/idm_user.properties";
	}

	/**
	 * User link colour property file.
	 * @param user
	 * @return
	 */
	public static String getPathUserDFEOutputColour(String user) {
		return getPathUserPref(user) + "/output_colours.properties";
	}

	/**
	 * User package folder.
	 * @param user
	 * @return
	 */
	public static String getPathUserPackagePref(String user) {
		return getPathUserPref(user) + "/packages";
	}

	/**
	 * Lib path folder.
	 * @param user
	 * @return
	 */
	public static String getUserPackageLibPath(String user) {
		return getPathUserPref(user) + "/lib/packages";
	}

	/**
	 * User Image folder from install directory.
	 * @param user
	 * @return
	 */
	public static String getPathUserImagePref(String user) {
		return "/packages/" + user + "/images";
	}

	/**
	 * Help folder from install directory.
	 * @param user
	 * @return
	 */
	public static String getPathUserHelpPref(String user) {
		return "/packages/" + user + "/help";
	}

	/**
	 * @return the pathsyspackagepref
	 */
	public static final Preference<String> getPathsyspackagepref() {
		return pathSysPackagePref;
	}

	/**
	 * @return the pathuserpref
	 */
	public static final String getPathuserpref() {
		return pathUserPref;
	}

	/**
	 * @return the pathiconmenu
	 */
	public static final String getPathiconmenu() {
		return pathIconMenu;
	}

	/**
	 * @return the pathtmpfolder
	 */
	public static final String getPathtmpfolder() {
		return pathTmpFolder;
	}

	/**
	 * @return the pathooziejob
	 */
	public static final String getPathooziejob() {
		return pathOozieJob;
	}

	/**
	 * @return the pathworkflow
	 */
	public static final String getPathworkflow() {
		return pathWorkflow;
	}

	/**
	 * @return the pathuserdfeoutputcolour
	 */
	public static final String getPathuserdfeoutputcolour() {
		return pathUserDFEOutputColour;
	}

	/**
	 * @return the pathuserpackagepref
	 */
	public static final String getPathuserpackagepref() {
		return pathUserPackagePref;
	}

	/**
	 * @return the userpackagelibpath
	 */
	public static final String getUserpackagelibpath() {
		return userPackageLibPath;
	}

	/**
	 * @return the pathsysimagepref
	 */
	public static final String getPathsysimagepref() {
		return pathSysImagePref;
	}

	/**
	 * @return the pathuserimagepref
	 */
	public static final String getPathuserimagepref() {
		return pathUserImagePref;
	}

	/**
	 * @return the pathuserhelppref
	 */
	public static final String getPathuserhelppref() {
		return pathUserHelpPref;
	}

	/**
	 * @return the sysPackageLibPath
	 */
	public static String getSysPackageLibPath() {
		return sysPackageLibPath;
	}

	/**
	 * @return the pathSysHelpPref
	 */
	public static String getPathSysHelpPref() {
		return pathSysHelpPref;
	}

}
