package com.hotel.util;

import com.hotel.model.User;

public class Session {
    private static User currentUser;

    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User user) { currentUser = user; }
    public static void logout() { currentUser = null; }
    public static boolean isAdmin() { return currentUser != null && "ADMIN".equals(currentUser.getRole()); }
    public static boolean isManager() { return currentUser != null && ("MANAGER".equals(currentUser.getRole()) || isAdmin()); }
}
