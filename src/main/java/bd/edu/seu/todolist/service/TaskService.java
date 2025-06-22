package bd.edu.seu.todolist.service;

import bd.edu.seu.todolist.exception.ResourceNotFoundException;
import bd.edu.seu.todolist.model.Task;
import bd.edu.seu.todolist.model.User;
import bd.edu.seu.todolist.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasksForUser(User user) {
        return taskRepository.findByUserId(user.getId());
    }

    public Optional<Task> getTaskByIdAndUser(String taskId, User user) {
        return taskRepository.findByIdAndUserId(taskId, user.getId());
    }

    public Task createTask(Task task, User user) {
        task.setUserId(user.getId());
        return taskRepository.save(task);
    }

    public Task updateTask(String taskId, Task taskDetails, User user) {
        Task task = taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + taskId + " for user " + user.getUsername()));
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setCompleted(taskDetails.isCompleted());
        return taskRepository.save(task);
    }

    public void deleteTask(String taskId, User user) {
        if (!taskRepository.existsByIdAndUserId(taskId, user.getId())) {
            throw new ResourceNotFoundException("Task not found with id " + taskId + " for user " + user.getUsername());
        }
        taskRepository.deleteById(taskId);
    }
}