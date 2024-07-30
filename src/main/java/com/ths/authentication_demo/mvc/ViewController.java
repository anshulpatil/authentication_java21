package com.ths.authentication_demo.mvc;

import com.ths.authentication_demo.dto.LoginRequest;
import com.ths.authentication_demo.dto.ResetPasswordDto;
import com.ths.authentication_demo.dto.SignupRequest;
import com.ths.authentication_demo.entity.RefreshToken;
import com.ths.authentication_demo.entity.User;
import com.ths.authentication_demo.jwt.JwtHelper;
import com.ths.authentication_demo.mvc.dto.UserSignupDto;
import com.ths.authentication_demo.service.RefreshTokenService;
import com.ths.authentication_demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/view")
public class ViewController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @GetMapping("/index")
    public String home() {
        return "index";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        UserSignupDto user = new UserSignupDto();
        model.addAttribute("user",user);
        return "register";
    }

    @PostMapping("/register/save")
    public String saveUser(@ModelAttribute("user") UserSignupDto user, BindingResult result, Model model) {
        try {
            userService.signup(new SignupRequest(user.getName(), user.getEmail(), user.getPassword()));
        } catch (Exception e) {
            result.rejectValue("email", null, "Email already exists");
        }
        if(result.hasErrors()) {
            model.addAttribute(user);
            return "register";
        }

        return "redirect:/register?success";
    }


    @GetMapping("/login")
    public String getLoginPage(Model model) {
        return "login";
    }

    @PostMapping("/login")
    public String userLogin(@ModelAttribute LoginRequest request, Model model, BindingResult result) {
        try {
            log.info("User logging in {}", request.email());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException e) {

            log.error("Bad Credentials");
        } catch (Exception e) {
            log.error("Error logging in : {}",e.getMessage());
        }

        String token = JwtHelper.generateToken(request.email());
        User user = userService.findByEmail(request.email());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        log.info("Token {}", token);

        model.addAttribute("token", token);
        model.addAttribute("refreshToken", refreshToken);

        return "login";
    }

    @GetMapping("/users")
    public String getAllUsers(Model model) {
        try {
            List<User> users = userService.getAllUsers();
            model.addAttribute("users", users);
            log.info("User size : {}",users.size());
            return "users";
        } catch (Exception e) {
            log.error("Error while fetching user list : {}",e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/forget-password")
    public String showResetPasswordForm(Model model) {
        ResetPasswordDto resetPasswordDto = ResetPasswordDto.builder().build();
        model.addAttribute("resetRequest", resetPasswordDto);
        return "forget-password";
    }
}
