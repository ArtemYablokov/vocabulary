package com.yablokovs.vocabulary.repo;

import com.yablokovs.vocabulary.model.Part;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SynonymsRepoTest {

    @Autowired
    PartRepository partRepository;

    @Autowired
    SynonymsRepo synonymsRepo;

    @Autowired
    DataSource dataSource;

    @Test
//    @Transactional // not use here !!!
    void findSynonymsByPartId() {
        partRepository.save(new Part());
        partRepository.save(new Part());
        partRepository.save(new Part());
        partRepository.save(new Part());

//        synonymsRepo.createReference(1L, 2L);
//        synonymsRepo.createReference(1L, 4L);
//        synonymsRepo.createReference(3L, 1L);

//        Set<Long> synonymsByPartId1 = synonymsRepo.findSynonymsByPartId(1L);

//        assertThat(synonymsByPartId1).containsOnly(2L, 4L, 3L);
    }
}