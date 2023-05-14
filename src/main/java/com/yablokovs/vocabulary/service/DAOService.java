package com.yablokovs.vocabulary.service;


import com.yablokovs.vocabulary.repo.SynonymsRequestBuilder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

@Service
public class DAOService {

    private final SynonymsRequestBuilder synonymsRequestBuilder;

    public DAOService(SynonymsRequestBuilder synonymsRequestBuilder) {
        this.synonymsRequestBuilder = synonymsRequestBuilder;
    }

    public Set<Long> findEngAntonymsByPartId(Long id) {
        return synonymsRequestBuilder.findSynonymsOrAntonymsByPartId(id, SynonymsRequestBuilder.DatabaseName.ENG_ANTONYM);
    }

    public Set<Long> findRusAntonymsByRusWordId(Long id) {
        return synonymsRequestBuilder.findSynonymsOrAntonymsByPartId(id, SynonymsRequestBuilder.DatabaseName.RUS_ANTONYM);
    }

    // TODO: 20.11.2022 refactor to INSERT MULTIPLE RAWS in a time
    public void saveEngSynonymIdTuple(Collection<IdTuple> idTuples) {
        idTuples.forEach(idTuple -> synonymsRequestBuilder.createReference(idTuple.getChild(), idTuple.getParent(), SynonymsRequestBuilder.DatabaseName.ENG_SYNONYM));
    }

    public void saveRusSynonymIdTuple(Collection<IdTuple> idTuples) {
        idTuples.forEach(idTuple -> synonymsRequestBuilder.createReference(idTuple.getChild(), idTuple.getParent(), SynonymsRequestBuilder.DatabaseName.RUS_SYNONYM));
    }

    // TODO: 20.11.2022 refactor to INSERT MULTIPLE RAWS in a time
    public void saveEngAntonymIdTuple(Collection<IdTuple> idTuples) {
        idTuples.forEach(idTuple -> synonymsRequestBuilder.createReference(idTuple.getChild(), idTuple.getParent(), SynonymsRequestBuilder.DatabaseName.ENG_ANTONYM));
    }

    public void saveRusAntonymIdTuple(Collection<IdTuple> idTuples) {
        idTuples.forEach(idTuple -> synonymsRequestBuilder.createReference(idTuple.getChild(), idTuple.getParent(), SynonymsRequestBuilder.DatabaseName.RUS_ANTONYM));
    }

    public Set<Long> findEngSynonymsByRusWordId(Long id) {
        return synonymsRequestBuilder.findEngPartSynOrAntIdsByRusWordId(id, SynonymsRequestBuilder.DatabaseName.RUS_ENG_SYNONYM);
    }

    public Set<Long> findEngAntonymsByRusWordId(Long id) {
        return synonymsRequestBuilder.findEngPartSynOrAntIdsByRusWordId(id, SynonymsRequestBuilder.DatabaseName.RUS_ENG_ANTONYM);
    }


    public void saveRusWordToEngPartAsSynonymIdTuple(Collection<IdTuple> idTuples) {
        idTuples.forEach(idTuple -> synonymsRequestBuilder.createReferenceWordRusToEngPartAsSynOrAnt(idTuple.getChild(), idTuple.getParent(), SynonymsRequestBuilder.DatabaseName.RUS_ENG_SYNONYM));
    }

    public void saveRusWordToEngPartAsAntonymIdTuple(Collection<IdTuple> idTuples) {
        idTuples.forEach(idTuple -> synonymsRequestBuilder.createReferenceWordRusToEngPartAsSynOrAnt(idTuple.getChild(), idTuple.getParent(), SynonymsRequestBuilder.DatabaseName.RUS_ENG_ANTONYM));
    }
}
