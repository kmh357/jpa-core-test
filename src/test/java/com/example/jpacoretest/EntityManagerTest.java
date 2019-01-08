package com.example.jpacoretest;

import junit.framework.TestCase;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class EntityManagerTest {
    @PersistenceContext(type = PersistenceContextType.TRANSACTION)
    private EntityManager em;

    @Autowired
    AccountService accountService;

    @Before
    public void setup(){
        Account account = new Account();
        account.setId(1);
        account.setName("test");
        em.persist(account);
        em.flush();
        em.clear();
    }

    @Test
    @Transactional
    public void checkPersitenceContextEntitySaved(){
        Account account = em.find(Account.class, 1);
        assertNotNull(account);

        PersistenceUnitUtil persistenceUnitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
        boolean hasEntity = persistenceUnitUtil.isLoaded(account);
        assertTrue(hasEntity);
    }
    @Test
    @Transactional
    public void checkReuseSavedEntity(){
        Account account = em.find(Account.class,1);
        assertNotNull(account);

        Account account2 = em.find(Account.class, 1);
        assertNotNull(account2);

        assertThat(account ,is(sameInstance(account2)) );
    }

    @Test
    @Transactional
    public void whenFlushPersistenceContext(){
        accountService.testData();

        accountService.updateAccount(2);

        Account account2 = em.find(Account.class, 2);
        assertThat("changedName", is(account2.getName()));
    }
}

@Service(value = "entityManagerAccountService")
class AccountService{
    @Getter
    @PersistenceContext
    private EntityManager em;


    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public void updateAccount(int id){
        Account account = em.find(Account.class, id);
        account.setName("changedName");
    }
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public void testData(){
        Account account = new Account();
        account.setId(2);
        account.setName("kmh");
        em.persist(account);
    }

}

@Data
@Entity
class Account {
    @Id
    private int id;

    private String name;
}