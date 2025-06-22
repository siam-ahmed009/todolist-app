package bd.edu.seu.todolist.service;

import bd.edu.seu.todolist.exception.ResourceNotFoundException;
import bd.edu.seu.todolist.model.Task;
import bd.edu.seu.todolist.model.User;
import bd.edu.seu.todolist.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        // User ID in MongoDB is typically a String
        testUser = new User("user123", "testuser", "encodedpassword", "test@example.com", Collections.singleton("ROLE_USER"));
        task1 = new Task("task1", "Buy groceries", "Milk, Eggs, Bread", false, LocalDateTime.now(), testUser.getId());
        task2 = new Task("task2", "Walk the dog", "Morning walk", true, LocalDateTime.now(), testUser.getId());
    }

    @Test
    void getAllTasksForUser_ShouldReturnTasks() {
        when(taskRepository.findByUserId(testUser.getId())).thenReturn(Arrays.asList(task1, task2));

        List<Task> tasks = taskService.getAllTasksForUser(testUser);

        assertNotNull(tasks);
        assertEquals(2, tasks.size());
        assertEquals("Buy groceries", tasks.get(0).getTitle());
        verify(taskRepository, times(1)).findByUserId(testUser.getId());
    }

    @Test
    void getTaskByIdAndUser_ShouldReturnTask_WhenFound() {
        when(taskRepository.findByIdAndUserId("task1", testUser.getId())).thenReturn(Optional.of(task1));

        Optional<Task> foundTask = taskService.getTaskByIdAndUser("task1", testUser);

        assertTrue(foundTask.isPresent());
        assertEquals("Buy groceries", foundTask.get().getTitle());
        verify(taskRepository, times(1)).findByIdAndUserId("task1", testUser.getId());
    }

    @Test
    void getTaskByIdAndUser_ShouldReturnEmpty_WhenNotFound() {
        when(taskRepository.findByIdAndUserId("nonexistent", testUser.getId())).thenReturn(Optional.empty());

        Optional<Task> foundTask = taskService.getTaskByIdAndUser("nonexistent", testUser);

        assertFalse(foundTask.isPresent());
        verify(taskRepository, times(1)).findByIdAndUserId("nonexistent", testUser.getId());
    }

    @Test
    void createTask_ShouldSaveTask() {
        Task newTask = new Task(null, "New Task", "Description", false, null, null);
        when(taskRepository.save(any(Task.class))).thenReturn(task1); // Simulate saving and returning the saved task

        Task createdTask = taskService.createTask(newTask, testUser);

        assertNotNull(createdTask);
        assertEquals(testUser.getId(), createdTask.getUserId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void updateTask_ShouldUpdateAndReturnTask_WhenFound() {
        Task updatedDetails = new Task();
        updatedDetails.setTitle("Updated Title");
        updatedDetails.setDescription("Updated Description");
        updatedDetails.setCompleted(true);

        when(taskRepository.findByIdAndUserId("task1", testUser.getId())).thenReturn(Optional.of(task1));
        when(taskRepository.save(any(Task.class))).thenReturn(task1); // Return the same mock task after saving

        Task updatedTask = taskService.updateTask("task1", updatedDetails, testUser);

        assertNotNull(updatedTask);
        assertEquals("Updated Title", updatedTask.getTitle());
        assertTrue(updatedTask.isCompleted());
        verify(taskRepository, times(1)).findByIdAndUserId("task1", testUser.getId());
        verify(taskRepository, times(1)).save(task1);
    }

    @Test
    void updateTask_ShouldThrowException_WhenNotFound() {
        when(taskRepository.findByIdAndUserId(anyString(), anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                taskService.updateTask("nonexistent", new Task(), testUser));
        verify(taskRepository, times(1)).findByIdAndUserId(anyString(), anyString());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void deleteTask_ShouldDeleteTask_WhenExists() {
        when(taskRepository.existsByIdAndUserId("task1", testUser.getId())).thenReturn(true);
        doNothing().when(taskRepository).deleteById("task1");

        assertDoesNotThrow(() -> taskService.deleteTask("task1", testUser));
        verify(taskRepository, times(1)).existsByIdAndUserId("task1", testUser.getId());
        verify(taskRepository, times(1)).deleteById("task1");
    }

    @Test
    void deleteTask_ShouldThrowException_WhenNotFound() {
        when(taskRepository.existsByIdAndUserId(anyString(), anyString())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                taskService.deleteTask("nonexistent", testUser));
        verify(taskRepository, times(1)).existsByIdAndUserId(anyString(), anyString());
        verify(taskRepository, never()).deleteById(anyString());
    }
}