package com.deguffroy.adrien.go4lunch;

import com.deguffroy.adrien.go4lunch.Models.User;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Adrien Deguffroy on 24/09/2018.
 */
public class UserTest {
    private User usertest;

    @Before
    public void setUp() throws Exception {
        usertest = new User("1234","username",null,12,1000,false);
    }

    @Test
    public void getUserInfo() {
        assertEquals("1234", usertest.getUid());
        assertEquals("username", usertest.getUsername());
        assertEquals(null, usertest.getUrlPicture());
        assertEquals(12, usertest.getSearchRadius());
        assertEquals(1000, usertest.getDefaultZoom());
        assertEquals(false, usertest.isNotificationOn());
    }

    @Test
    public void setUserInfo() {
        usertest.setUid("1111");
        usertest.setUsername("test_username");
        usertest.setUrlPicture("url_picture");
        usertest.setSearchRadius(15);
        usertest.setDefaultZoom(1500);
        usertest.setNotificationOn(true);

        assertEquals("1111", usertest.getUid());
        assertEquals("test_username", usertest.getUsername());
        assertEquals("url_picture", usertest.getUrlPicture());
        assertEquals(15, usertest.getSearchRadius());
        assertEquals(1500, usertest.getDefaultZoom());
        assertEquals(true, usertest.isNotificationOn());
    }
}
