package max.telegram.db;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.telegram.telegrambots.api.objects.User;

public class UserProfileDao {

    private static SessionFactory factory = HibernateUtil.getSessionFactory();

    public void persistUser(User user) {
        Session session = factory.openSession();
        Transaction transaction;
        transaction = session.beginTransaction();
        session.saveOrUpdate(convert(user));
        transaction.commit();
        session.close();
    }

    public void persistLanguagesForUser(User user, String language) {
        Session session = factory.openSession();
        Transaction transaction;
        transaction = session.beginTransaction();
        UserToLanguage userToLanguage = new UserToLanguage(user.getId(), language);
        Criteria criteria = session.createCriteria(UserToLanguage.class);
        criteria.add(Restrictions.eq("userId", user.getId()));
        criteria.add(Restrictions.eq("language", language));
        if (!criteria.list().isEmpty()) {
            UserToLanguage userToLanguageFromDB = (UserToLanguage) criteria.list().iterator().next();
            if (userToLanguageFromDB.getActive() == 1) {
                userToLanguageFromDB.setActive(0);
                session.saveOrUpdate(userToLanguageFromDB);
            } else {
                userToLanguageFromDB.setActive(1);
                session.saveOrUpdate(userToLanguageFromDB);
            }
        } else session.save(userToLanguage);
        transaction.commit();
        session.close();
    }

    public List<UserToLanguage> retrieveLanguagesForUser(User user) {
        Session session = factory.openSession();
        Transaction transaction;
        transaction = session.beginTransaction();
        Criteria criteria = session.createCriteria(UserToLanguage.class);
        criteria.add(Restrictions.eq("userId", user.getId()));
        criteria.add(Restrictions.eq("active", 1));
        List<UserToLanguage> userToLanguageList = criteria.list();
        transaction.commit();
        session.close();
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
