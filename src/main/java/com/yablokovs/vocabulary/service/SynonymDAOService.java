package com.yablokovs.vocabulary.service;


import com.yablokovs.vocabulary.repo.SynonymsRequestBuileder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

@Service
public class SynonymDAOService {

    private final SynonymsRequestBuileder synonymsRequestBuileder;

    public SynonymDAOService(SynonymsRequestBuileder synonymsRequestBuileder) {
        this.synonymsRequestBuileder = synonymsRequestBuileder;
    }

    public Set<Long> findSynonymsByPartId(Long id) {
        return synonymsRequestBuileder.findSynonymsOrAntonymsByPartId(id, SynonymsRequestBuileder.DatabaseName.ANTONYM);

    }

    public Set<Long> findAntonymsByPartId(Long id) {
        return synonymsRequestBuileder.findSynonymsOrAntonymsByPartId(id, SynonymsRequestBuileder.DatabaseName.ANTONYM);
    }

    // TODO: 20.11.2022 refactor to INSERT MULTIPLE RAWS in a time
    public void saveSynIdTuple(Collection<IdTuple> idTuples) {
        idTuples.forEach(idTuple -> synonymsRequestBuileder.createReference(idTuple.getChild(), idTuple.getParent(), SynonymsRequestBuileder.DatabaseName.SYNONYM));
    }

    // TODO: 20.11.2022 refactor to INSERT MULTIPLE RAWS in a time
    public void saveAntIdTuple(Collection<IdTuple> idTuples) {
        idTuples.forEach(idTuple -> synonymsRequestBuileder.createReference(idTuple.getChild(), idTuple.getParent(), SynonymsRequestBuileder.DatabaseName.ANTONYM));
    }


    // TODO: 02/03/23 may be simplified to one call to DB (LIMIT 1)
    public Long getAnyAntonymForSynSet(Set<Long> synSet) {
        Long firstWordFormSynonyms = synSet.iterator().next();
        Set<Long> foundAntonymsBySynonym = findAntonymsByPartId(firstWordFormSynonyms); // bc // skip // yz
        Long firstWordFormFoundAntonyms = foundAntonymsBySynonym.iterator().next();
        return firstWordFormFoundAntonyms;
    }
}
