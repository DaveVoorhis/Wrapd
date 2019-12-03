package org.reldb.wrapd.sqldb.postgresql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.reldb.toolbox.configuration.Configuration;
import org.reldb.toolbox.security.PasswordAuthentication;
import org.reldb.toolbox.utilities.ProgressIndicator;
import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.WrapdDatabase;
import org.reldb.wrapd.sqldb.WrapdDatabaseBase;

/*
 * PostgreSQL database definitions specific to the Wrapd framework: user management, etc., and wrapper around the Database database abstraction. 
 */
public class WrapdDB extends WrapdDatabaseBase {
	
	private static Logger log = LogManager.getLogger(WrapdDB.class.toString());
	
	public WrapdDB() throws IOException {
		if (database != null)
			return;
		
		String dbServer = Database.nullTo(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_SERVER), "localhost");
		String dbDatabase = Database.emptyToNull(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_NAME));
		String dbUser = Database.emptyToNull(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_USER));
		String dbPasswd = Database.emptyToNull(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_PASSWORD));
		String dbPort = Database.emptyToNull(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_NONSTANDARD_PORT));
		String dbTablenamePrefix = Database.nullTo(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_TABLENAME_PREFIX), "Wrapd_");
		
		if (dbDatabase == null)
			throw new IOException("Database connection failed. Please specify a database name.");
		
		if (dbPort != null)
			dbServer += ":" + dbPort;
	
		String url = "jdbc:postgresql://" + dbServer + "/" + dbDatabase;
		database = new Database(url, dbUser, dbPasswd, dbTablenamePrefix);		
	}
	
	public DBVersion getDBVersion() throws SQLException {
		return (DBVersion) database.queryAll("SELECT * FROM $$Version", version -> {
			if (!version.next()) {
				log.error("WrapdDB: Error: Version table appears to be empty!");
				return null;
			}
			return new DBVersion(version.getInt("framework_db_version"), version.getInt("user_db_version"));			
		});
	}

	@Override
	public void putFrameworkDBVersion(int version) throws SQLException {
		database.update("UPDATE $$Version SET framework_db_version = ?", version);
	}

	@Override
	public void putUserDBVersion(int version) throws SQLException {
		database.update("UPDATE $$Version SET user_db_version = ?", version);
	}
	
	@Override
	public void initialise(ProgressIndicator progress) throws SQLException {
		progress.initialise(9);
		database.new Transaction(connection -> {
			progress.move(1, "Creating Version table.");
			database.updateAll(connection, "CREATE TABLE $$Version (user_db_version INTEGER, framework_db_version INTEGER);");
			progress.move(2, "Initialising Version.");
			database.updateAll(connection, "INSERT INTO $$Version VALUES (0, 0);");
			progress.move(3, "Creating Groups table.");
			database.updateAll(connection, "CREATE TABLE $$Groups " + 
					"(userGroup TEXT NOT NULL PRIMARY KEY, "
					+ "description TEXT NOT NULL, "
					+ "privilege TEXT NOT NULL);");
			progress.move(4, "Adding Administrator group.");
			addGroup(connection, "Administrator", "Administrator", WrapdDatabase.AdministratorGroupPrivilege);
			progress.move(5, "Adding User group.");
			addGroup(connection, "User", "User", WrapdDatabase.UserGroupPrivilege);
			progress.move(6, "Creating Users table.");
			database.updateAll(connection, "CREATE TABLE $$Users " + 
					"(userID SERIAL NOT NULL PRIMARY KEY, " 
					+ "userName TEXT NOT NULL, " 
					+ "userEmail TEXT NOT NULL UNIQUE, "
					+ "hashedPassword TEXT NOT NULL, "
					+ "passwdChangeToken TEXT NULL, "
					+ "passwdChangeTokenStamp TIMESTAMP NULL, "
					+ "newEmail TEXT NULL, "
					+ "emailChangeToken TEXT NULL, "
					+ "emailChangeTokenStamp TIMESTAMP NULL, "
					+ "registrationToken TEXT NULL, "
					+ "registrationTokenStamp TIMESTAMP NULL, "
					+ "activated BOOLEAN NOT NULL DEFAULT false, "
					+ "changePasswordOnLogin BOOLEAN NOT NULL DEFAULT false, "
					+ "changePasswordOnLoginReason TEXT NULL);");
			progress.move(7, "Creating UserGroups table.");
			database.updateAll(connection, "CREATE TABLE $$UserGroups " + 
					"(userGroup TEXT NOT NULL REFERENCES $$Groups ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "userID INTEGER NOT NULL REFERENCES $$Users ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "CONSTRAINT $$UserGroups_pkey PRIMARY KEY(userGroup, userID));");
			progress.move(8,  "Creating Installation Administrator account in Admininstrator group.");
			addUser(connection, "Installation Administrator", 
					Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.SUPPORT_CONTACT), 
					Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.INSTALLER_ADMIN_PASSWORD), 
					"Administrator");
			progress.move(9, "Done.");
			return true;
		});
	}

	private void version1() throws SQLException {
		database.new Transaction(connection -> {	
			log.info("Wrapd framework database update version 1");
			getDatabase().update("ALTER TABLE $$Users ADD COLUMN lastLogin TIMESTAMP");
			return true;
		});
	}
	
	/** When framework updates are needed, replace this with an array of DatabaseUpdate instances. 
	 * 
	 * 	return new DatabaseUpdate[] {
	 *			database -> {log.info("framework database update 0");},
	 *			database -> {log.info("framework database update 1");}
	 *	};
	 *
	 *  *** DO NOT EDIT OR OVERRIDE THIS METHOD!!! ***
	 */
	@Override	
	public final DatabaseUpdate[] getFrameworkDatabaseUpdates() {
		return new DatabaseUpdate[] {
			// This initial update exists mainly to verify that the update mechanism works.
			database -> version1()
		};
	}
		
	private void addGroup(Connection connection, String groupName, String groupDescription, String privilege) throws SQLException {
		database.update(connection, "INSERT INTO $$Groups VALUES (?, ?, ?);", groupName, groupDescription, privilege);		
	}
	
	@Override
	public void addGroup(String groupName, String groupDescription, String privilege) throws SQLException {
		database.new Transaction(connection -> {
			addGroup(connection, groupName, groupDescription, privilege);
			return true;
		});
	}
	
	private void addUser(Connection connection, String userName, String email, String password, String group) throws SQLException {		
		Long userID = (Long) database.valueOfAll(connection, "SELECT nextval('assign_users_userid_seq') AS nv", "nv");
		database.update(connection, "INSERT INTO $$Users (userID, userName, userEmail, hashedPassword, activated, changePasswordOnLogin, changePasswordOnLoginReason) "
								+ "VALUES (?, ?, ?, ?, true, true, ?);",
				userID,
				userName,
				email,
				PasswordAuthentication.obtainHashedPassword(password),
				"Password change required on first login."
		);
		if (group != null)
			database.update(connection, "INSERT INTO $$UserGroups (userID, userGroup) VALUES (?, ?)", userID, group);
	}
	
	@Override
	public void addUser(String userName, String email, String password, String group) throws SQLException {		
		database.new Transaction(connection -> {
			addUser(connection, userName, email, password, group);
			return true;
		});
	}
	
	private static class User {
		private Integer userID;
		private Boolean changePasswordOnLogin;
		private String changePasswordOnLoginReason;
		public User(Integer userID, Boolean changePasswordOnLogin, String changePasswordOnLoginReason) {
			this.userID = userID;
			this.changePasswordOnLogin = changePasswordOnLogin;
			this.changePasswordOnLoginReason = changePasswordOnLoginReason;
		}
		public Integer getUserID() {
			return userID;
		}
		public Boolean getChangePasswordOnLogin() {
			return changePasswordOnLogin;
		}
		public String getChangePasswordOnLoginReason() {
			return changePasswordOnLoginReason;
		}
	}
	
	// Find any user matching given userID and password. Return null if not found.
	private User findUser(Integer userID, String password) throws SQLException {
		PasswordAuthentication authenticator = new PasswordAuthentication();
		String passwordTrimmed = password.trim();
		return (User) database.query("SELECT hashedPassword, changePasswordOnLogin, changePasswordOnLoginReason FROM $$Users WHERE userID = ?",
				result -> {
					if (result.next()) {
						String token = result.getString("hashedPassword");
						if (authenticator.authenticate(passwordTrimmed.toCharArray(), token))
							return new User(userID, 
									result.getBoolean("changePasswordOnLogin"), 
									result.getString("changePasswordOnLoginReason"));
					}
					return null;	
				},
				userID);
	}

	// Find active user matching given email address and password. Return null if not found.
	private User locateUser(String email, String password) throws SQLException {		
		if (email == null)
			return null;
		PasswordAuthentication authenticator = new PasswordAuthentication();
		String emailTrimmed = email.trim();
		String passwordTrimmed = password.trim();
		return (User) database.query("SELECT userID, hashedPassword, changePasswordOnLogin, changePasswordOnLoginReason FROM $$Users WHERE activated AND userEmail = ?",
			result -> {
				if (result.next()) {
					String token = result.getString("hashedPassword");
					if (authenticator.authenticate(passwordTrimmed.toCharArray(), token))
						return new User(
								result.getInt("userID"), 
								result.getBoolean("changePasswordOnLogin"), 
								result.getString("changePasswordOnLoginReason"));
				}
				return null;	
			},
			emailTrimmed);
	}

	@Override
	public String getLoggedInUserAttribute(String attributeName) {
		try {
			Object attrValue = database.valueOf("SELECT " + attributeName + " FROM $$Users WHERE userID = ?", attributeName, getLoggedInUserID());
			if (attrValue == null)
				return null;
			return attrValue.toString();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public String getLoggedInUserEmail() {
		return getLoggedInUserAttribute("userEmail");
	}
	
	@Override
	public String getLoggedInUserName() {
		return getLoggedInUserAttribute("userName");
	}

	@Override
	public String[] getUserGroups(Integer userID) {
		try {
			return (String[]) database.query("SELECT userGroup FROM $$Users, $$UserGroups WHERE $$Users.userID = $$UserGroups.userID AND $$Users.userID = ?", 
				result -> {
					Vector<String> groups = new Vector<>();
					while (result.next())
						groups.add(result.getString("userGroup"));
					return groups.toArray(new String[0]);						
				},
				userID);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public String[] getLoggedInUserGroups() {
		if (getLoggedInUserID() == null)
			return null;
		return getUserGroups(getLoggedInUserID());
	}
	
	@Override
	public String[] getLoggedInUserPrivileges() {
		if (getLoggedInUserID() == null)
			return null;
		try {
			return (String[]) database.query("SELECT privilege FROM $$Users, $$UserGroups, $$Groups WHERE " 
					+ "$$Users.userID = $$UserGroups.userID AND $$Groups.userGroup = $$UserGroups.userGroup AND $$Users.userID = ?",
				results -> {
					Vector<String> privileges = new Vector<>();
					while (results.next())
						privileges.add(results.getString("privilege"));
					return privileges.toArray(new String[0]);
				},
				getLoggedInUserID());
		} catch (SQLException e) {
			e.printStackTrace();
			return new String[0];
		}
	}

	@Override
	public boolean verifyPassword(String password) {
		try {
			return findUser(getLoggedInUserID(), password) != null;
		} catch (SQLException e) {
			log.error("ERROR: WrapdDB: verifyPassword: " + e);
			return false;
		}
	}

	@Override
	public LoginStatus attemptLogin(String email, String password) {
		try {
			User user = locateUser(email, password);
			if (user != null) {
				boolean needsPasswordChange = user.getChangePasswordOnLogin();
				if (needsPasswordChange) {
					String needsPasswordChangeReason = user.getChangePasswordOnLoginReason();
					if (needsPasswordChangeReason == null || needsPasswordChangeReason.trim().length() == 0)
						needsPasswordChangeReason = "Your password has expired.";
					return new LoginStatus(needsPasswordChangeReason);
				} else {
					database.update("UPDATE $$Users SET lastLogin = NOW() WHERE userID = ?", user.getUserID());
					setLoggedInUserID(user.getUserID());
					return new LoginStatus(true);
				}
			}
		} catch (SQLException e) {
			log.error("ERROR: WrapdDB: attemptLogin: " + e);
			setLoggedInUserID(null);
		}
		return new LoginStatus(false);
	}

	@Override
	public boolean attemptLoginWithPasswordChange(String email, String oldPassword, String newPassword) {
		try {
			User user = locateUser(email, oldPassword);
			if (user != null) {
				boolean needsPasswordChange = user.getChangePasswordOnLogin();
				if (!needsPasswordChange)
					return false;				
				Integer userID = user.getUserID();
				database.update("UPDATE $$Users SET "
						+ "passwdChangeToken = NULL, "
						+ "passwdChangeTokenStamp = NULL, "
						+ "changePasswordOnLogin = false, "
						+ "changePasswordOnLoginReason = NULL, "
						+ "hashedPassword = ?, "
						+ "lastLogin = NOW() "
						+ "WHERE userID = ?", 
							PasswordAuthentication.obtainHashedPassword(newPassword), userID);
				setLoggedInUserID(userID);
				return true;
			}
		} catch (SQLException e) {
			log.error("ERROR: WrapdDB: attemptLoginWithPasswordChange: " + e);
			setLoggedInUserID(null);
		}
		return false;
	}
	
	@Override
	public Long[] getUserIDsForEmail(String email) {
		try {
			return (Long[]) database.query("SELECT userID FROM $$Users WHERE activated AND userEmail = ?",
				result -> {
					Vector<Long> emailAddresses = new Vector<>();
					while (result.next())
						emailAddresses.add(result.getLong("userID"));
					return emailAddresses.toArray(new Long[0]);						
				},
				email);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public boolean enablePasswordChange(Long userID, String authToken) {
		try {
			database.update("UPDATE $$Users SET passwdChangeToken = ?, passwdChangeTokenStamp = NOW() WHERE userID = ?", authToken, userID);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}		
	}
	
	@Override
	public TokenCheckStatus checkPasswordChangeTokenValidity(String token, int tokenExpiryHours) {
		try {
			// expire old tokens
			database.update("UPDATE $$Users SET passwdChangeToken = NULL, passwdChangeTokenStamp = NULL WHERE EXTRACT(EPOCH FROM NOW() - passwdChangeTokenstamp)/3600 > ?;", tokenExpiryHours);
			// is there a user with given token?
			Long userCount = (Long) database.valueOf("SELECT COUNT(userID) AS N FROM $$Users WHERE passwdChangeToken = ?", "N", token.trim());
			return userCount > 0 ? TokenCheckStatus.VALID : TokenCheckStatus.INVALID;
		} catch (SQLException e) {
			e.printStackTrace();
			return TokenCheckStatus.ERROR;
		}
	}

	@Override
	public boolean changePassword(String password, String token) {
		try {
			database.update("UPDATE $$Users SET "
				+ "passwdChangeToken = NULL, "
				+ "passwdChangeTokenStamp = NULL, "
				+ "changePasswordOnLogin = false, "
				+ "changePasswordOnLoginReason = NULL, "
				+ "hashedPassword = ? "
				+ "WHERE passwdChangeToken = ?", 
					PasswordAuthentication.obtainHashedPassword(password), token.trim());
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public boolean changePassword(String newPassword) {
		Integer user = getLoggedInUserID();
		if (user == null)
			return false;
		try {
			database.update("UPDATE $$Users SET "
				+ "passwdChangeToken = NULL, "
				+ "passwdChangeTokenStamp = NULL, "
				+ "changePasswordOnLogin = false, "
				+ "changePasswordOnLoginReason = NULL, "
				+ "hashedPassword = ? "
				+ "WHERE userID = ?",
					PasswordAuthentication.obtainHashedPassword(newPassword), user);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean enableEmailChangeForCurrentUser(String newEmail, String authToken) {
		Integer userID = getLoggedInUserID();
		if (userID == null)
			return false;
		try {
			database.update("UPDATE $$Users SET newEmail = ?, emailChangeToken = ?, emailChangeTokenStamp = NOW() WHERE userID = ?", newEmail, authToken, userID);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public TokenCheckStatus changeEmail(String token, int tokenExpiryHours) {
		try {
			// expire old tokens
			database.update("UPDATE $$Users SET newEmail = NULL, emailChangeToken = NULL, emailChangeTokenStamp = NULL WHERE EXTRACT(EPOCH FROM NOW() - emailChangeTokenstamp)/3600 > ?;", tokenExpiryHours);
			// find user with given token
			Long userCount = (Long) database.valueOf("SELECT COUNT(userID) AS N FROM $$Users WHERE emailChangeToken = ?", "N", token.trim());
			if (userCount == 0)
				return TokenCheckStatus.INVALID;
			database.update("UPDATE $$Users SET emailChangeToken = NULL, emailChangeTokenStamp = NULL, userEmail = newEmail, newEmail = NULL WHERE emailChangeToken = ?", token.trim());
			return TokenCheckStatus.VALID;
		} catch (SQLException e) {
			e.printStackTrace();
			return TokenCheckStatus.ERROR;
		}
	}

	@Override
	public boolean updateUserName(String userName) {
		try {
			database.update("UPDATE $$Users SET userName = ? WHERE userID = ?", userName, getLoggedInUserID());
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public RegisterStatus checkEmailInUse(String email) {
		try {
			// delete any pending requests with this email address
			database.update("DELETE FROM $$Users WHERE NOT activated AND userEmail = ?", email.trim());
			// check if this email address is already active
			Long userCount = (Long) database.valueOf("SELECT COUNT(userID) AS N FROM $$Users WHERE activated AND userEmail = ?", "N", email.trim());
			if (userCount > 0)
				return RegisterStatus.DUPLICATE_EMAIL;
			return RegisterStatus.OK;
		} catch (SQLException e) {
			e.printStackTrace();
			return RegisterStatus.ERROR;
		}
	}

	@Override
	public boolean addUnregisteredUser(String userName, String email, String password, String activationToken) {
		try {
			database.update("INSERT INTO $$Users (userName, userEmail, hashedPassword, activated, registrationToken, registrationTokenStamp) VALUES (?, ?, ?, false, ?, NOW());", 
				userName,
				email,
				PasswordAuthentication.obtainHashedPassword(password),
				activationToken
			);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public String[] getAllEmailAddressesInGroup(String group) {
		try {
			return (String[]) database.query("SELECT userEmail FROM $$Users, $$UserGroups WHERE $$Users.userID = $$UserGroups.userID AND userGroup = ?",
				accountAuthAdmins -> {
					Vector<String> admins = new Vector<>();
					while (accountAuthAdmins.next())
						admins.add(accountAuthAdmins.getString("userEmail"));						
					return admins.toArray(new String[0]);
				},
				group);
		} catch (SQLException e) {
			e.printStackTrace();
			return new String[0];
		}
	}

	@Override
	public ActivationStatus completeUserRegistration(String token, int registrationExpiryHours) {
		try {
			// expire old registrations
			database.update("DELETE FROM $$Users WHERE NOT activated AND EXTRACT(EPOCH FROM NOW() - registrationTokenStamp)/3600 > ?;", registrationExpiryHours);
			// find user with given token
			return (ActivationStatus) database.query("SELECT userEmail FROM $$Users WHERE NOT activated AND registrationToken = ?",
				users -> {
					if (!users.next())
						return ActivationStatus.INVALID;
					String email = users.getString("userEmail");
					// has an account already been activated with this email?
					Long userCount = (Long) database.valueOf("SELECT COUNT(userID) AS N FROM $$Users WHERE activated AND userEmail = ?", "N", email.trim());
					if (userCount > 0)
						return ActivationStatus.DUPLICATE_EMAIL;
					// activate
					database.update("UPDATE $$Users SET registrationToken = NULL, registrationTokenStamp = NULL, activated = true WHERE registrationToken = ?", token.trim());
					return ActivationStatus.VALID;						
				},
				token.trim());
		} catch (SQLException e) {
			e.printStackTrace();
			return ActivationStatus.ERROR;
		}
	}
	
	/** When user database updates are needed, replace this with an array of DatabaseUpdate instances. 
	 * 
	 * 	return new DatabaseUpdate[] {
	 *			database -> {log.info("user database update 0");},
	 *			database -> {log.info("user database update 1");}
	 *	};
	 *
	 * The 0th "update" is normally the initial database definition.
	 *
	 */
	@Override
	public DatabaseUpdate[] getUserDatabaseUpdates() {
		return new DatabaseUpdate[0];
	}

}
