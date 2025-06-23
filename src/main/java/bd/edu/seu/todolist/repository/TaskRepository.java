package bd.edu.seu.todolist.repository;

import bd.edu.seu.todolist.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByUserId(String userId);
    Optional<Task> findByIdAndUserId(String id, String userId);
    Boolean existsByIdAndUserId(String id, String userId);

    List<Task> findByUserIdAndTagsContaining(String userId, String tag);
}