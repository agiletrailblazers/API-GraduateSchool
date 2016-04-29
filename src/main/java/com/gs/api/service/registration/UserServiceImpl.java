package com.gs.api.service.registration;

import com.gs.api.dao.registration.UserDAO;
import com.gs.api.domain.PWChangeCredentials;
import com.gs.api.domain.authentication.AuthCredentials;
import com.gs.api.domain.registration.User;
import com.gs.api.exception.NotFoundException;
import com.gs.api.service.email.EmailService;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;

@Service
public class UserServiceImpl implements UserService {

    final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserDAO userDao;

    @Autowired
    private EmailService emailService;

    @Override
    public void createUser(User user) throws Exception {

        logger.info("Creating new user: {} {} {}", new String[] {user.getPerson().getFirstName(), user.getPerson().getMiddleName(), user.getPerson().getLastName()});

        String userId = userDao.insertNewUser(user);

        // update the user with the generated ID
        user.setId(userId);

        //sending welcome email to new User
        try {
            emailService.sendNewUserEmail(user);
        }
        catch (Exception e) {
            logger.error("User {} created but failed to send new user email", userId, e);
        }
        logger.debug("User creation email for user " + user.getId() + " asynchronously sending, returning to normal flow");
    }

    @Override
    public void deleteUser(String userId) throws Exception {

        logger.info("Deleting user: {}", userId);

        // need to lookup the user by id first because the delete dao requires the timestamp associated with the id
        User user = userDao.getUser(userId);
        userDao.deleteUser(userId, user.getTimestamp());
    }

    @Override
    public User getUser(AuthCredentials authCredentials) throws Exception {

        logger.debug("Get user: {}", authCredentials.getUsername());

        return userDao.getUser(authCredentials.getUsername(), authCredentials.getPassword());
    }

    @Override
    public User getUser(String userId) throws Exception {

        logger.debug("Get user: {}", userId);

        User user = userDao.getUser(userId);

        if (user == null) {
            // user not found
            throw new NotFoundException("User not found by id " + userId);
        }

        return user;
    }

    @Override
    public void forgotPassword(AuthCredentials authCredentials) throws Exception {

        logger.info("Forgot password for user: {}", authCredentials.getUsername());

        // get the user
        User user = userDao.getUserByUsername(authCredentials.getUsername());

        if (user == null) {
            logger.debug("No user found with name {}", authCredentials.getUsername());
            throw new NotFoundException("No user found with name " + authCredentials.getUsername());
        }

        // create and encrypt the new password
        String newPasswordClear = RandomStringUtils.randomAlphanumeric(10);
        final String encryptedPassword = generateHash(newPasswordClear);

        // reset the password
        userDao.resetForgottenPassword(user.getId(), encryptedPassword);

        // send password reset email
        try {
            emailService.sendPasswordResetEmail(user, newPasswordClear);
        }
        catch (Exception e) {
            logger.error("Password reset for user {} but failed to send new user email", user.getUsername(), e);
        }
    }

    @Override
    public void changePassword(final PWChangeCredentials pwChangeCredentials) throws Exception{
        logger.info("Changing password for user: {}", pwChangeCredentials.getUsername());

        // get the user
        User user = userDao.getUser(pwChangeCredentials.getUsername(), pwChangeCredentials.getPassword());

        if (user == null) {
            logger.debug("Username or Password Incorrect for user {}", pwChangeCredentials.getUsername());
            throw new NotFoundException("Username or Password Incorrect for user " + pwChangeCredentials.getUsername());
        }

        userDao.changeUserPassword(user.getId(), pwChangeCredentials.getPassword(), pwChangeCredentials.getNewPassword());
    }

    /**
     * Password encryption/hashing logic taken from existing saba system to ensure compatibility
     * with Saba.
     *
     * @param password clear text password.
     * @return encrypted, hashed password.
     * @throws Exception error during encryption/hashing.
     */
    private String generateHash(String password) throws Exception {

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(password.getBytes("UTF-8"));

        byte[] digest = messageDigest.digest();

        String passwordHash = base16encode(digest);
        return passwordHash;
    }

    /**
     * Password encryption/hashing logic taken from existing saba system to ensure compatibility
     * with Saba.
     *
     * @param ciphertext the encrypted password.
     * @return base-16 encoded encrypted password.
     */
    private String base16encode(byte[] ciphertext) {

        int bytelen = ciphertext.length;
        int bytelenX2 = bytelen * 2;
        StringBuffer b16string = new StringBuffer(bytelenX2);
        int[] bit4 = new int[bytelenX2];
        int i = 0;

        for (int k = 0; k < bytelen; ++k) {
            bit4[i] = (240 & ciphertext[k]) >> 4;
            bit4[i + 1] = 15 & ciphertext[k];
            i += 2;
        }

        for (i = 0; i < bytelenX2; ++i) {
            switch (bit4[i]) {
                case 0:
                    b16string.append("0");
                    break;
                case 1:
                    b16string.append("1");
                    break;
                case 2:
                    b16string.append("2");
                    break;
                case 3:
                    b16string.append("3");
                    break;
                case 4:
                    b16string.append("4");
                    break;
                case 5:
                    b16string.append("5");
                    break;
                case 6:
                    b16string.append("6");
                    break;
                case 7:
                    b16string.append("7");
                    break;
                case 8:
                    b16string.append("8");
                    break;
                case 9:
                    b16string.append("9");
                    break;
                case 10:
                    b16string.append("A");
                    break;
                case 11:
                    b16string.append("B");
                    break;
                case 12:
                    b16string.append("C");
                    break;
                case 13:
                    b16string.append("D");
                    break;
                case 14:
                    b16string.append("E");
                    break;
                case 15:
                    b16string.append("F");
            }
        }

        return b16string.toString();
    }
}
