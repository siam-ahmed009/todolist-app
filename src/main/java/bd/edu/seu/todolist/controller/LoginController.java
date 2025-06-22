package bd.edu.seu.todolist.controller;

import bd.edu.seu.todolist.dto.RegisterRequest;
import bd.edu.seu.todolist.model.User;
import bd.edu.seu.todolist.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", true);
        }
        if (logout != null) {
            model.addAttribute("logout", true);
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegisterRequest()); // Bind to RegisterRequest DTO
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") RegisterRequest registerRequest,
                               BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register"; // Stay on registration page if errors
        }

        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(registerRequest.getPassword()); // Raw password

        try {
            userService.registerNewUser(newUser);
            redirectAttributes.addFlashAttribute("registrationSuccess", true);
            return "redirect:/login";
        } catch (bd.edu.seu.todolist.exception.UserAlreadyExistsException e) {
            // Add error to model for showing on register page
            result.rejectValue("username", "user.exists", e.getMessage()); // Generic field error
            return "register"; // Return to form with error
        }
    }
}