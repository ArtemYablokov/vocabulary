package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.config.DataSourceConfig;
import com.yablokovs.vocabulary.model.Word;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

//@SpringBootTest - try to test without a SpringBOOT test context
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DataSourceConfig.class})
//@RunWith(JUnitPlatform.class) // java.lang.NoClassDefFoundError: org/junit/jupiter/api/extension/ScriptEvaluationException
public class WordServiceTest {

    // TODO: 16.10.2022 learn differences
    @PersistenceContext
    EntityManager em;

    // TODO: 16.10.2022 learn
    @Autowired
    ConfigurableApplicationContext configurableApplicationContext;

    @Autowired
    WordService wordService;

    @Test
    public void testSaveWord() {
        Word word = new Word();
        word.setName("from test");
        wordService.saveWord(word);

        //entityManager.persist(word);

        List<Word> allWords = wordService.findAllWords();
        Assert.assertEquals(1, allWords.size());
        Assert.assertEquals("from test", allWords.get(0).getName());
    }

    private void testSaveWordEntityManager() {

//        Transaction transaction = session.beginTransaction();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();

        Word word = new Word();
        word.setName("from test");
        em.persist(word);

        transaction.commit();
    }

    private void testConfigurableContext() {
        configurableApplicationContext.getBeanFactory().registerSingleton("beanId", new Object());

    }
}
