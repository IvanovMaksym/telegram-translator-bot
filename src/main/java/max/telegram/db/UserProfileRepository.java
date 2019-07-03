package max.telegram.db;

import max.telegram.model.UserAccount;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserProfileRepository extends CrudRepository<UserAccount, Integer> {

    Optional<UserAccount> findByTelegramId(Integer telegramId);
}
