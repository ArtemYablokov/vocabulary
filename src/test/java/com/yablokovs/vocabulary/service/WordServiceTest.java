package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.config.DataSourceConfig;
import com.yablokovs.vocabulary.model.Definition;
import com.yablokovs.vocabulary.model.Part;
import com.yablokovs.vocabulary.model.Phrase;
import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.repo.DefinitionRepository;
import com.yablokovs.vocabulary.repo.PartRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;

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
        wordService.saveNewWordWithPartsAndDefinitions(word);

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


    @Autowired
    DefinitionRepository definitionRepository;

    // todo Test is not valid - Hibernate use CONTEX - no, each time create SQL-query
    @Test
    public void testSaveDefinitionWithPhrase() {

        Phrase phrase = new Phrase();
        phrase.setName("Phrase");

        Definition entity = new Definition();
        entity.setName("Definition");
        entity.setPhrases(Collections.singletonList(phrase));
        Definition save = definitionRepository.save(entity);

        List<Definition> all = definitionRepository.findAll();
        List<Definition> other = definitionRepository.findAll();
        Assert.assertEquals(1, all.size());
        Definition definition = all.get(0);

        Assert.assertEquals("Definition", definition.getName());
        List<Phrase> phrases = definition.getPhrases();

        Assert.assertEquals(1, phrases.size());
        Assert.assertEquals("Phrase", phrases.get(0).getName());
    }

    @Autowired
    PartRepository partRepository;

    @Test
    public void testSavePartWithDefinition() {

        Definition definition = new Definition();
        definition.setName("Definition");

        Part part = new Part();
        part.setName("Part");
        part.setDefinitions(Collections.singletonList(definition));
        definition.setPart(part);

        partRepository.save(part);
        List<Part> all = partRepository.findAll();
        Assert.assertEquals(1, all.size());
        Part part1 = all.get(0);
        Assert.assertEquals("Part", part1.getName());

        Definition definition1 = part1.getDefinitions().get(0);
        Assert.assertEquals("Definition", definition1.getName());

    }

    private void testConfigurableContext() {
        configurableApplicationContext.getBeanFactory().registerSingleton("beanId", new Object());

    }
}
