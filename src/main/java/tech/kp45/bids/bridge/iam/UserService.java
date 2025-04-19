package tech.kp45.bids.bridge.iam;

import java.security.KeyPair;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;
import tech.kp45.bids.bridge.iam.entity.Role;
import tech.kp45.bids.bridge.iam.entity.User;
import tech.kp45.bids.bridge.iam.entity.UserRole;
import tech.kp45.bids.bridge.iam.mapper.UserMapper;
import tech.kp45.bids.bridge.iam.mapper.UserRoleMapper;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private KeyPair keyPair;

    public void registerUser(String username, String rawPassword) {
        if (userMapper.findByUsername(username) != null) {
            throw new BasicRuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        userMapper.insert(user);

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(1); // Default role ID
        userRoleMapper.insert(userRole);
    }

    public String login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        List<Role> roles = userRoleMapper.findRolesByUserId(user.getId());
        user.setRoles(roles);

        // Generate JWT token here
        return createJwt(user);
    }

    private String createJwt(User user) {
        JWTSigner signer = JWTSignerUtil.createSigner("RS256", keyPair.getPrivate());
        return JWT.create()
                .setPayload("sub", user.getUsername())
                .setPayload("roles", user.getRoles().stream()
                        .map(Role::getName)
                        .toArray(String[]::new))
                .setPayload("iat", DateUtil.current())
                .setPayload("exp", DateUtil.current() + 3600_000)
                .sign(signer);
    }
}
