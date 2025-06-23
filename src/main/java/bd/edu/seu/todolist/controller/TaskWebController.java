package bd.edu.seu.todolist.controller;

import bd.edu.seu.todolist.model.Task;
import bd.edu.seu.todolist.model.User;
import bd.edu.seu.todolist.service.TaskService;
import bd.edu.seu.todolist.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Ensure Model is imported
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tasks")
public class TaskWebController {

    private final TaskService taskService;
    private final UserService userService;

    public TaskWebController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    private User getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database."));
    }

    @GetMapping
    public String listTasks(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        model.addAttribute("tasks", taskService.getAllTasksForUser(currentUser));
        model.addAttribute("newTask", new Task()); // For the form to add new tasks
        return "tasks"; // points to tasks.html
    }

    @PostMapping
    public String addTask(@Valid @ModelAttribute("newTask") Task task, BindingResult result,
                          RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        if (result.hasErrors()) {
            User currentUser = getCurrentUser(userDetails);
            model.addAttribute("tasks", taskService.getAllTasksForUser(currentUser)); // Re-populate existing tasks
            model.addAttribute("newTask", task); // The 'task' object now contains the submitted data with errors
            return "tasks"; // Stay on the tasks.html page to show errors
        }
        User currentUser = getCurrentUser(userDetails);
        // The 'task' object from @ModelAttribute should already have tags bound from the form
        taskService.createTask(task, currentUser);
        redirectAttributes.addFlashAttribute("message", "Task added successfully!");
        return "redirect:/tasks"; // Redirect on success
    }

    @PostMapping("/{id}/toggle")
    public String toggleTaskComplete(@PathVariable String id, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        Task task = taskService.getTaskByIdAndUser(id, currentUser)
                .orElseThrow(() -> new bd.edu.seu.todolist.exception.ResourceNotFoundException("Task not found"));
        task.setCompleted(!task.isCompleted());
        taskService.updateTask(id, task, currentUser);
        return "redirect:/tasks";
    }

    @PostMapping("/{id}/delete")
    public String deleteTask(@PathVariable String id, RedirectAttributes redirectAttributes,
                             @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        taskService.deleteTask(id, currentUser);
        redirectAttributes.addFlashAttribute("message", "Task deleted successfully!");
        return "redirect:/tasks";
    }
}