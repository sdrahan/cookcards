package app.cookcards.webapp.recipe;

import app.cookcards.webapp.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByUserOrderByUpdatedAtDesc(User user);
    Optional<Recipe> findByIdAndUser(Long id, User user);
}
