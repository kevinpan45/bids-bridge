package tech.kp45.bids.bridge.iam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;

@CrossOrigin
@RestController
public class IamApi {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public String login(@RequestBody LoginBody body) {
        return userService.login(body.getUsername(), body.getPassword());
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterBody body) {
        userService.registerUser(body.getUsername(), body.getPassword());
    }

}

@Data
class LoginBody {
    private String username;
    private String password;
}

@Data
class RegisterBody {
    private String username;
    private String password;
}