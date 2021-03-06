package com.techflow.techhubbackend.config;

import com.techflow.techhubbackend.model.UserType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@PropertySource("classpath:UserControllerTest.properties")
public class UserControllerTestDataProperties {

    @Value("${user.email}")
    private String userEmail;

    @Value("${user.password}")
    private String userPassword;

    @Value("${user.username}")
    private String userUsername;

    @Value("${user.type}")
    private UserType userType;

    @Value("${user.profilePicture}")
    private String userProfilePicture;

    @Value("${user.accountStatus}")
    private String userAccountStatus;

    @Value("${user.currentPoints}")
    private Long userCurrentPoints;

    @Value("${user.totalPoints}")
    private Long userTotalPoints;

    @Value("${user.trophies}")
    private Long userTrophies;

    @Value("${user.rafflesWon}")
    private Long userRafflesWon;

    @Value("${user.invalid.email}")
    private String userInvalidEmail;

    @Value("${user.post.email}")
    private String userPostEmail;

    @Value("${user.delete.email}")
    private String userDeleteEmail;

    @Value("${user.put.initialEmail}")
    private String userPutInitialEmail;

    @Value("${user.put.changedEmail}")
    private String userPutChangedEmail;

    @Value("${user.put.changedAccountStatus}")
    private String userPutChangedAccountStatus;

    @Value("${user.put.changedPassword}")
    private String userPutChangedPassword;

    @Value("#{${user.sort.points}}")
    private List<Long> userSortPoints;

    @Value("#{${user.sort.trophies}}")
    private List<Long> userSortTrophies;

    @Value("${user.vip.email}")
    private String userVipEmail;

    @Value("${user.vip.type}")
    private UserType userVipType;

    @Value("${user.vip.vipStatus}")
    private Boolean userVipVipStatus;

    public UserControllerTestDataProperties() {
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserUsername() {
        return userUsername;
    }

    public void setUserUsername(String userUsername) {
        this.userUsername = userUsername;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public void setUserType(String userType) {
        this.userType = UserType.valueOf(userType);
    }

    public String getUserProfilePicture() {
        return userProfilePicture;
    }

    public void setUserProfilePicture(String userProfilePicture) {
        this.userProfilePicture = userProfilePicture;
    }

    public String getUserAccountStatus() {
        return userAccountStatus;
    }

    public void setUserAccountStatus(String userAccountStatus) {
        this.userAccountStatus = userAccountStatus;
    }

    public Long getUserCurrentPoints() {
        return userCurrentPoints;
    }

    public void setUserCurrentPoints(Long userCurrentPoints) {
        this.userCurrentPoints = userCurrentPoints;
    }

    public Long getUserTotalPoints() {
        return userTotalPoints;
    }

    public void setUserTotalPoints(Long userTotalPoints) {
        this.userTotalPoints = userTotalPoints;
    }

    public Long getUserTrophies() {
        return userTrophies;
    }

    public void setUserTrophies(Long userTrophies) {
        this.userTrophies = userTrophies;
    }

    public Long getUserRafflesWon() {
        return userRafflesWon;
    }

    public String getUserInvalidEmail() {
        return userInvalidEmail;
    }

    public void setUserInvalidEmail(String userInvalidEmail) {
        this.userInvalidEmail = userInvalidEmail;
    }

    public String getUserPostEmail() {
        return userPostEmail;
    }

    public void setUserPostEmail(String userPostEmail) {
        this.userPostEmail = userPostEmail;
    }

    public String getUserDeleteEmail() {
        return userDeleteEmail;
    }

    public void setUserDeleteEmail(String userDeleteEmail) {
        this.userDeleteEmail = userDeleteEmail;
    }

    public String getUserPutInitialEmail() {
        return userPutInitialEmail;
    }

    public void setUserPutInitialEmail(String userPutInitialEmail) {
        this.userPutInitialEmail = userPutInitialEmail;
    }

    public String getUserPutChangedEmail() {
        return userPutChangedEmail;
    }

    public void setUserPutChangedEmail(String userPutChangedEmail) {
        this.userPutChangedEmail = userPutChangedEmail;
    }

    public String getUserPutChangedAccountStatus() {
        return userPutChangedAccountStatus;
    }

    public void setUserPutChangedAccountStatus(String userPutChangedAccountStatus) {
        this.userPutChangedAccountStatus = userPutChangedAccountStatus;
    }

    public String getUserPutChangedPassword() {
        return userPutChangedPassword;
    }

    public void setUserPutChangedPassword(String userPutChangedPassword) {
        this.userPutChangedPassword = userPutChangedPassword;
    }

    public List<Long> getUserSortPoints() {
        return userSortPoints;
    }

    public void setUserSortPoints(List<Long> userSortPoints) {
        this.userSortPoints = userSortPoints;
    }

    public List<Long> getUserSortTrophies() {
        return userSortTrophies;
    }

    public void setUserSortTrophies(List<Long> userSortTrophies) {
        this.userSortTrophies = userSortTrophies;
    }

    public String getUserVipEmail() {
        return userVipEmail;
    }

    public void setUserVipEmail(String userVipEmail) {
        this.userVipEmail = userVipEmail;
    }

    public UserType getUserVipType() {
        return userVipType;
    }

    public void setUserVipType(UserType userVipType) {
        this.userVipType = userVipType;
    }

    public Boolean getUserVipVipStatus() {
        return userVipVipStatus;
    }

    public void setUserVipVipStatus(Boolean userVipVipStatus) {
        this.userVipVipStatus = userVipVipStatus;
    }
}
