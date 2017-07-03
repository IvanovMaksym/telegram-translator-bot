package max.telegram.db;

import java.util.List;
import java.util.stream.Collectors;
import max.telegram.model.UserProfile;
import max.telegram.model.UserToLanguage;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.objects.User;

public class UserProfileDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileDao.class);
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static UserProfileDao userProfileDao = null;

    protected UserProfileDao(){}

    public static UserProfileDao getInstance() {
        if (userProfileDao == null) {
            userProfileDao = new UserProfileDao();
        }
        return userProfileDao;
    }

    private static SessionFactory buildSessionFactory() {
        try {
            LOGGER.info("Creating SessionFactory from hibernate config file");
            return new Configuration().configure().buildSessionFactory();
        }
        catch (Exception ex) {
            LOGGER.error("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public void updateUserNativeLanguage(User user, String language) {
        LOGGER.info("Updating native language {} for userId {}", language, user.getId());
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        UserProfile userProfile = convert(user);
        userProfile.setLanguageCode(language);
        session.saveOrUpdate(userProfile);
        session.getTransaction().commit();
        session.close();
    }

    public void persistUser(User user) {
        LOGGER.info("Persisting userId {}", user.getId());
        Session session = sessionFactory.openSession();
        Transaction transaction;
        transaction = session.beginTransaction();
        session.saveOrUpdate(convert(user));
        transaction.commit();
        session.close();
    }

    public List<String> getUserLanguage(User user) {
        LOGGER.info("Retrieving language for userId {}", user.getId());
        Session session = sessionFactory.openSession();
        Transaction transaction;
        transaction = session.beginTransaction();
        Criteria criteria = session.createCriteria(UserProfile.class);
        criteria.add(Restrictions.eq("id", user.getId()));
        List<UserProfile> userProfiles = criteria.list();
        transaction.commit();
        session.close();
        return userProfiles.stream().map(UserProfile::getLanguageCode).collect(Collectors.toList());
    }

    public void updateLanguagesForUser(User user, String language) {
        LOGGER.info("Updating language {} for a userId {}", language, user.getId());
        Session session = sessionFactory.openSession();
        Transaction transaction;
        transaction = session.beginTransaction();
        UserToLanguage userToLanguage = new UserToLanguage(user.getId(), language);
        Criteria criteria = session.createCriteria(UserToLanguage.class);
        criteria.add(Restrictions.eq("userId", user.getId()));
        criteria.add(Restrictions.eq("language", language));
        if (!criteria.list().isEmpty()) {
            UserToLanguage userToLanguageFromDB = (UserToLanguage) criteria.list().iterator().next();
            if (userToLanguageFromDB.getActive() == 1) {
                LOGGER.info("Disabling language {} for userId {}", language, user.getId());
                userToLanguageFromDB.setActive(0);
                session.saveOrUpdate(userToLanguageFromDB);
            } else {
                LOGGER.info("Activating language {} for userId {}", language, user.getId());
                userToLanguageFromDB.setActive(1);
                session.saveOrUpdate(userToLanguageFromDB);
            }
        } else session.save(userToLanguage);
        transaction.commit();
        session.close();
    }

    public List<UserToLanguage> retrieveLanguagesForUser(User user) {
        LOGGER.info("Retrieving active languages for a userId {}", user.getId());
        Session session = sessionFactory.openSession();
        Transaction transaction;
        transaction = session.beginTransaction();
        Criteria criteria = session.createCriteria(UserToLanguage.class);
        criteria.add(Restrictions.eq("userId", user.getId()));
        criteria.add(Restrictions.eq("active", 1));
        List<UserToLanguage> userToLanguageList = criteria.list();
        transaction.commit();
        session.close();
        LOGGER.info("Returning list of active languages for a userId {}: {}",
            user.getId(),
            userToLanguageList.stream().map(UserToLanguage::getLanguage).toArray());
        return userToLanguageList;
    }

    private UserProfile convert(User user) {
        UserProfile userProfile = new UserProfile();
        userProfile.setId(user.getId());
        userProfile.setFirstName(user.getFirstName());
        userProfile.setLastName(user.getLastName());
        userProfile.setUserName(user.getUserName());
        userProfile.setLanguageCode(user.getLanguageCode());

        return userProfile;
    }
}
