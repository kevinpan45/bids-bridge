package tech.kp45.bids.bridge.iam;

import org.springframework.security.oauth2.jwt.Jwt;

import tech.kp45.bids.bridge.iam.entity.User;

public class UserUtils {
    public static User toUser(Jwt jwt) {
        if (jwt == null) {
            User guest = new User();
            guest.setUsername("guest");
            guest.setEmail("guest@example.com");
            guest.setEmailVerified(true);
            return guest;
        }
        User user = new User();
        user.setUsername(jwt.getClaimAsString("nickname"));
        user.setEmail(jwt.getClaimAsString("email"));
        user.setEmailVerified(jwt.getClaimAsBoolean("email_verified") != null && jwt.getClaimAsBoolean("email_verified"));
        user.setUid(jwt.getClaimAsString("sub"));
        user.setAvatarLink(jwt.getClaimAsString("picture"));
        return user;
    }
}
