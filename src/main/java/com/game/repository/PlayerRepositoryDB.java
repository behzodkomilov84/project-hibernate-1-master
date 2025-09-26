package com.game.repository;

import com.game.entity.Player;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.hibernate.tool.schema.Action.CREATE;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {

    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
//        properties.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
//        properties.put(Environment.URL, "jdbc:mysql://localhost:3306/rpg");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "root");
        properties.put(Environment.HBM2DDL_AUTO, "update");
        properties.put(Environment.SHOW_SQL, "true");
        properties.put(Environment.FORMAT_SQL, "true");

        sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();

    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            String sql = "select * from player";
            NativeQuery<Player> nativeQuery = session.createNativeQuery(sql, Player.class);
            nativeQuery.setFirstResult(pageNumber * pageSize);
            nativeQuery.setMaxResults(pageSize);
            transaction.commit();

            return nativeQuery.list();
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getAllCount() {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            Query<Long> query = session.createNamedQuery("player_getAllCount", Long.class);
            transaction.commit();
            return Math.toIntExact(query.uniqueResult());
        }
    }

    @Override
    public Player save(Player player) {
        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            session.save(player);
//            session.flush();
            transaction.commit();
            return player;
        }
    }

    @Override
    public Player update(Player player) {
        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            session.update(player);
            session.flush();
            transaction.commit();
            return player;
        }
    }

    @Override
    public Optional<Player> findById(long id) {
        try(Session session = sessionFactory.openSession()) {
            Player player = session.find(Player.class, id);
            return Optional.ofNullable(player);
        }
    }

    @Override
    public void delete(Player player) {
        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            session.remove(player);
            transaction.commit();
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}