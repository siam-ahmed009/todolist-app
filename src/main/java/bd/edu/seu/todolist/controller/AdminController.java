// ToDoListApp/src/main/java/bd/edu/seu/todolist/controller/AdminController.java
package bd.edu.seu.todolist.controller;

import bd.edu.seu.todolist.dto.RegisterRequest;
import bd.edu.seu.todolist.exception.ResourceNotFoundException;
import bd.edu.seu.todolist.exception.UserAlreadyExistsException;
import bd.edu.seu.todolist.model.User;
import bd.edu.seu.todolist.service.UserService;
import jakarta.validation.Valid; // Import for @Valid annotation
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Import for BindingResult
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute; // Import for @ModelAttribute
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("newUser", new RegisterRequest()); // Add for the new user form
        return "admin/dashboard";
    }

    @PostMapping("/users/add") // New endpoint for adding users
    @PreAuthorize("hasRole('ADMIN')")
    public String addNewUser(@Valid @ModelAttribute("newUser") RegisterRequest registerRequest, // Bind to RegisterRequest DTO
                             BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            // If validation errors, re-fetch existing users and return to dashboard to show errors
            model.addAttribute("users", userService.getAllUsers());
            return "admin/dashboard"; // Stay on the dashboard page to show errors
        }

        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(registerRequest.getPassword()); // Raw password will be encoded in service

        try {
            userService.registerNewUser(newUser); // Use existing service method
            redirectAttributes.addFlashAttribute("message", "User '" + registerRequest.getUsername() + "' added successfully!");
            return "redirect:/admin/dashboard";
        } catch (UserAlreadyExistsException e) {
            result.rejectValue("username", "user.exists", e.getMessage()); // Add error to username field
            model.addAttribute("users", userService.getAllUsers()); // Re-fetch users for the view
            return "admin/dashboard"; // Return to form with error
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding user: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }


    @PostMapping("/users/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable String id, RedirectAttributes redirectAttributes,
                             @AuthenticationPrincipal UserDetails currentUserDetails) {
        try {
            User userToDelete = userService.getUserById(id);

            // Prevent admin from deleting themselves
            if (userToDelete.getUsername().equals(currentUserDetails.getUsername())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You cannot delete your own admin account.");
                return "redirect:/admin/dashboard";
            }
            // Prevent deletion of the default 'admin' user if it's not the currently logged in admin trying to delete another admin
            if (userToDelete.getUsername().equals("admin")) {
                redirectAttributes.addFlashAttribute("errorMessage", "The default 'admin' account cannot be deleted.");
                return "redirect:/admin/dashboard";
            }


            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "User '" + userToDelete.getUsername() + "' deleted successfully!");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}