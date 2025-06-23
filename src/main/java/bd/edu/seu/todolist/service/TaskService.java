package bd.edu.seu.todolist.service;

import bd.edu.seu.todolist.exception.ResourceNotFoundException;
import bd.edu.seu.todolist.model.Task;
import bd.edu.seu.todolist.model.User;
import bd.edu.seu.todolist.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasksForUser(User user) {
        return taskRepository.findByUserId(user.getId());
    }

    public List<Task> getAllTasksForUserAndTag(User user, String tag) {
        return taskRepository.findByUserIdAndTagsContaining(user.getId(), tag);
    }

    public Optional<Task> getTaskByIdAndUser(String taskId, User user) {
        return taskRepository.findByIdAndUserId(taskId, user.getId());
    }

    public Task createTask(Task task, User user) {
        task.setUserId(user.getId());
        if (task.getTags() == null) {
            task.setTags(new java.util.HashSet<>());
        }
        return taskRepository.save(task);
    }

    public Task updateTask(String taskId, Task taskDetails, User user) {
        Task task = taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + taskId + " for user " + user.getUsername()));
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setCompleted(taskDetails.isCompleted());
        if (taskDetails.getTags() != null) {
            task.setTags(taskDetails.getTags());
        } else {
            task.setTags(new java.util.HashSet<>()); // Clear tags if null is passed
        }
        return taskRepository.save(task);
    }

    public void deleteTask(String taskId, User user) {
        if (!taskRepository.existsByIdAndUserId(taskId, user.getId())) {
            throw new ResourceNotFoundException("Task not found with id " + taskId + " for user " + user.getUsername());
        }
        taskRepository.deleteById(taskId);
    }
}