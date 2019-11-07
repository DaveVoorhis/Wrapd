package org.reldb.wrapd.db;

import java.io.IOException;
import java.sql.SQLException;

import org.reldb.wrapd.utilities.ProgressIndicator;

public interface WrapdDatabase {

	public final String AdministratorGroupPrivilege = "999999999";
	public final String UserGroupPrivilege = "100000000";
	
	public enum CheckDatabaseStatus {
		ERROR, OK, CREATE_DATABASE_NEEDED, UPGRADE_DATABASE_NEEDED
	}

	public enum TokenCheckStatus {
		ERROR, VALID, INVALID
	}
	
	public enum ActivationStatus {
		ERROR, VALID, INVALID, DUPLICATE_EMAIL
	}	
	
	public enum RegisterStatus {
		ERROR, OK, DUPLICATE_EMAIL
	}
	
	public static class LoginStatus {
		private boolean loggedIn;
		private String requiresNewPasswordReason;
		public LoginStatus(boolean loggedIn) {
			this.loggedIn = loggedIn;
			requiresNewPasswordReason = null;
		}
		public LoginStatus(String requiresNewPasswordReason) {
			loggedIn = false;
			this.requiresNewPasswordReason = requiresNewPasswordReason;
		}
		public boolean isLoggedIn() {
			return loggedIn;
		}
		public String getRequiresNewPasswordReason() {
			return requiresNewPasswordReason;
		}
		public boolean isNewPasswordRequired() {
			return requiresNewPasswordReason != null;
		}
	}

	public static class DBVersion {
		private int frameworkDBVersion;
		private int userDBVersion;
		public DBVersion(int frameworkDBVersion, int userDBVersion) {
			this.frameworkDBVersion = frameworkDBVersion;
			this.userDBVersion = userDBVersion;
		}
		public int getFrameworkDBVersion() {
			return frameworkDBVersion;
		}
		public int getUserDBVersion() {
			return userDBVersion;
		}
	}
	
	@FunctionalInterface
	public interface DatabaseUpdate {
		public void go(WrapdDatabase db) throws SQLException;
	}
	
	/** When framework updates are needed, replace this with an array of DatabaseUpdate instances. 
	 * 
	 * 	return new DatabaseUpdate[] {
	 *			database -> {System.out.println("framework database update 0");},
	 *			database -> {System.out.println("framework database update 1");}
	 *	};
	 *
	 *  *** DO NOT EDIT OR OVERRIDE THIS METHOD!!! ***
	 */
	DatabaseUpdate[] getFrameworkDatabaseUpdates();
		
	/** When user database updates are needed, replace this with an array of DatabaseUpdate instances. 
	 * 
	 * 	return new DatabaseUpdate[] {
	 *			database -> {System.out.println("user database update 0");},
	 *			database -> {System.out.println("user database update 1");}
	 *	};
	 *
	 * The 0th "update" is normally the initial database definition.
	 *
	 */
	DatabaseUpdate[] getUserDatabaseUpdates();
	
	/** Get the database version. */
	DBVersion getDBVersion() throws SQLException;
	
	/** Put the framework database version. Normally invoked only by WrapdDB. */
	void putFrameworkDBVersion(int version) throws SQLException;
	
	/** Put the user's database version. Normally invoked only by WrapdDB. */
	void putUserDBVersion(int version) throws SQLException;
	
	/** Check the database status. Normally invoked only by WrapdDB. */
	CheckDatabaseStatus checkDatabase() throws IOException;

	/** Attempt to initialise the database. Normally invoked only by WrapdDB. */
	void initialise(ProgressIndicator progress) throws SQLException;

	/** Attempt to upgrade the database. Normally invoked only by WrapdDB. */
	void upgrade(ProgressIndicator progress) throws SQLException;

	/** Add a new user group. */
	void addGroup(String groupName, String groupDescription, String privilege) throws SQLException;

	/** Add a new user. The user must change password on first login. This ensures that the administrator can't know the user's password. */
	void addUser(String userName, String email, String password, String group) throws SQLException;

	/** Get the current logged-in user's ID. Return null if no user is logged in. */
	Integer getLoggedInUserID();

	/** Log the user out. */
	void logout();

	/** For the current logged-in user, get the value of a given user attribute. */
	String getLoggedInUserAttribute(String attributeName);

	/** Get the current logged-in user's email address. */
	String getLoggedInUserEmail();

	/** Get the current logged-in user's user name. */
	String getLoggedInUserName();

	/** Get the names of the groups that the given user belongs to. */
	String[] getUserGroups(Integer userID);
	
	/** Get the names of the groups that the current logged-in user belongs to. */
	String[] getLoggedInUserGroups();

	/** Get the current logged-in user's privileges, based on the groups to which the user belongs. */
	String[] getLoggedInUserPrivileges();

	/** Verify that the specified password is the correct one for the logged-in user. */
	boolean verifyPassword(String password);

	/** Attempt to login given email and password. */
	LoginStatus attemptLogin(String email, String password);

	/** Attempt to login given email, current password, and a new password. The User's 'changePasswordOnLogin' attribute must be true. If successful, set password to newPassword. */
	boolean attemptLoginWithPasswordChange(String email, String oldPassword, String newPassword);

	/** Get user IDs associated with a given email address. Return null if error; empty array if none found. */
	Long[] getUserIDsForEmail(String email);

	/** Update the user's record identified by userID to allow a password change using changePassword(String password, String token). */
	boolean enablePasswordChange(Long userID, String authToken);

	/** Check to see whether a given password change token is valid. */
	TokenCheckStatus checkPasswordChangeTokenValidity(String token, int tokenExpiryHours);

	/** Change password given password change token. */
	boolean changePassword(String password, String token);

	/** Change password for current logged-in user. */
	boolean changePassword(String newPassword);

	/** Update current logged-in user's record to indicate that an email change will be allowed by changeEmail (below) if authToken is supplied. */
	boolean enableEmailChangeForCurrentUser(String newEmail, String authToken);

	/** Update the user's record identified by token with the newEmail address specified in enableEmailChangeForCurrentUser(). */
	TokenCheckStatus changeEmail(String token, int tokenExpiryHours);

	/** Change the current logged-in user's userName field. */
	boolean updateUserName(String userName);

	/** Check to see if the given email address is already in use. */
	RegisterStatus checkEmailInUse(String email);

	/** Add an unregistered, un-activated user. The activationToken must be passed to completeUserRegistration() to make the registration active. */
	boolean addUnregisteredUser(String userName, String email, String password, String activationToken);

	/** Given a group, return all the email addresses of users in that group. */
	String[] getAllEmailAddressesInGroup(String group);

	/** Activate an unactivated registration identified by token, which was supplied to addUnregisteredUser(). */
	ActivationStatus completeUserRegistration(String token, int registrationExpiryHours);

	/** Get a reference to the underlying database. */
	Database getDatabase();
	
}