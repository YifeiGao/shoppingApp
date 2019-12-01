package com.b07.database.helper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.b07.exceptions.InvalidItemException;
import com.b07.exceptions.InvalidRoleException;
import com.b07.exceptions.ItemNotFoundException;
import com.b07.exceptions.UserNotFoundException;
import com.b07.inventory.Inventory;
import com.b07.inventory.Item;
import com.b07.store.Sale;
import com.b07.users.Account;
import com.b07.users.User;
import android.content.Context;


public class DatabaseSerializeHelper {

	/**
	 * 
	 * @param directoryofFile: it is the directory which the admin want to store the output serialized file
	 * @return
	 */
	public static boolean serializeHelper(String directoryofFile, Context context) {
		try {
			String file = directoryofFile + "database_copy.ser";
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			List<User> userList = DatabaseSelectHelper.getUsersDetails(context);
			getUserRoles(userList, context);
			List<Account> accountList = DatabaseSelectHelper.getAllAccounts(context);
			int size = userList.size();
			out.writeInt(size);
			for (User user : userList) {
				out.writeObject(user);
			}
			out.writeInt(accountList.size());
			for (Account account : accountList) {
				out.writeObject(account);
			}
			List<Item> allItem = DatabaseSelectHelper.getAllItems(context);
			out.writeInt(allItem.size());
			for (Item item : allItem) {
				out.writeObject(item);
			}
			HashMap<Integer, String> roleNames = getRoleNames(context);
			out.writeInt(roleNames.size());
			for (Integer roleId : roleNames.keySet()) {
				out.writeObject(roleId);
				out.writeObject(roleNames.get(roleId));
			}
			HashMap<User, String> userPassword = getUserPassword(userList, context);
			out.writeInt(userPassword.size());
			for (User user : userList) {
				out.writeObject(user);
				out.writeObject(userPassword.get(user));
			}
			List<Sale> saleList = getSaleList(userList, context);
			out.writeInt(saleList.size());
			for (Sale sale : saleList) {
				out.writeObject(sale);
			}
			Inventory currentInventory = DatabaseSelectHelper.getInventory(context);
			out.writeObject(currentInventory);
			HashMap<Integer, String> logins  = getAllLogins(userList, context);
			out.writeInt(logins.size());
			for (Integer userId : logins.keySet()) {
				out.writeObject(userId);
				out.writeObject(logins.get(userId));
			}
			out.close();
			fileOut.close();
			System.out.println("Serialized data is saved in " + file);
					return true;
		} catch (IOException | SQLException | InvalidItemException | ItemNotFoundException |InvalidRoleException |UserNotFoundException e) {
			return false;
		}
	}

	/**
	 * This method is to set all user's roleId in the given list of User 
	 * @param userList the list of user needs to be set their roleId 
	 * @throws SQLException if SQL failes to read or write
	 * @throws InvalidIdException if the roleId is not valid(exist in database)
	 */
	private static void getUserRoles(List<User> userList, Context context) throws SQLException, UserNotFoundException {
		// TODO Auto-generated method stub
		for (User user : userList) {
			int roleId = DatabaseSelectHelper.getUserRoleId(user.getId(), context);
			user.setRoleId(roleId);
		}
	}

	/**
	 * This method is to get user's password matches to itself.
	 * @param userList the list of user needs to be matching
	 * @return a hashMap that contains all users with relation User to password
	 */
	private static HashMap<User, String> getUserPassword(List<User> userList, Context context) {
		HashMap<User, String> userPassword = new HashMap<User, String>();
		try {
			for (User currentUser : userList) {
				String password = null;
				password = DatabaseSelectHelper.getPassword(currentUser.getId(), context);
				userPassword.put(currentUser, password);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Some internal error");
		}
		return userPassword;
	}

	/**
	 * This method is to get user's roleId matches to roleName.
	 * @param userList the list of user needs to be matching
	 * @return a hashMap that contains all users with relation roleId to roleName
	 */
	private static HashMap<Integer, String> getRoleNames(Context context) throws InvalidRoleException {
		List<Integer> roleIds;
		HashMap<Integer, String> roleNames = new HashMap<Integer, String>();
		try {
			roleIds = DatabaseSelectHelper.getRoleIds(context);
			for (Integer roleId : roleIds) {
				String roleName = DatabaseSelectHelper.getRoleName(roleId, context);
				roleNames.put(roleId, roleName);
			}
			return roleNames;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Some internal error");
		}
		return roleNames;
	}

	/**
	 * This method is to get a list of sales of each user.
	 * @param userList the list of Users that will get provide info about their sales
	 * @return a list of sale of all given users
	 */
	private static List<Sale> getSaleList(List<User> userList, Context context) {
		List<Sale> saleIds = new ArrayList<Sale>();
		try {
			for (User user : userList) {
				saleIds.addAll(DatabaseSelectHelper.getSalesToUser(user.getId()));
			}
		} catch (SQLException | UserNotFoundException e) {
			// TODO Auto-generated catch block
			return saleIds;
		}
		return saleIds;
	}
	private static HashMap<Integer, String> getAllLogins(List<User> userList, Context context){
		HashMap<Integer, String> logins = new HashMap<Integer, String>();
		for (User user : userList) {
			String userLogin = DatabaseSelectHelper.getUserLogin(user.getId(), context);
			logins.put(user.getId(), userLogin);
		}
		return logins;
	}
}
