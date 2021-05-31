package com.example.myapplication.Model;

/**
 * Author: Me Duc Thinh
 * Modified date: 08/05/2021
 * Description:
 * 1. Create package: Adapter -> UserAdapter.
 * 2. Create package: Model -> User
 * 3. Add action see profile & edit profile to two class: ProfileFragment & EditProfileActivity
 * 4. Add action search user and see friend profile to class: SearchFragment
 * 5. Design the XML: activity_edit_profile, fragment_profile, fragment_search.
 * 6. Add some activity & user_permission to AndroidManifest.xml
 * 7. Add some dependencies to app build.gradle
 */

public class User {
    //Variable name must similar to Database
    private String userFullName;
    private String userEmail;
    private String userBio;
    private String userImageUrl;
    private String userID;

    public User() {
    }

    public User(String userFullName, String userEmail, String userBio, String userImageUrl, String userID) {
        this.userFullName = userFullName;
        this.userEmail = userEmail;
        this.userBio = userBio;
        this.userImageUrl = userImageUrl;
        this.userID = userID;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserBio() {
        return userBio;
    }

    public void setUserBio(String userBio) {
        this.userBio = userBio;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
