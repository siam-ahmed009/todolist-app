package bd.edu.seu.todolist.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    private String id; // MongoDB uses String for _id by default

    private String title;
    private String description;
    private boolean completed = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    // Reference to the User who owns this task by their ID
    private String userId;
    private int orderIndex;

    public Task(String task1, String buyGroceries, String s, boolean b, LocalDateTime now, String id) {
    }
}