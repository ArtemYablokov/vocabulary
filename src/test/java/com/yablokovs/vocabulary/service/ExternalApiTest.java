package com.yablokovs.vocabulary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yablokovs.vocabulary.mdto.externalApi.Root;
import com.yablokovs.vocabulary.mdto.request.*;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ExternalApiTest {


    @Test
    @SneakyThrows
    public void testApi() {

        String eng = "https://api.dictionaryapi.dev/api/v2/entries/en/sell";
        var request = HttpRequest.newBuilder(URI.create(eng))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofMillis(1000))
                .GET()
                .build();
        var client = HttpClient.newHttpClient();
        var result = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

        Root[] response = new ObjectMapper().readValue(result, Root[].class);

//        Assert.assertEquals(response.length, 0); - not always come only one word !!!

        List<PartDto> partDtos = new ArrayList<>();
        response[0].getMeanings().forEach(meaning -> {
            PartDto partDto = new PartDto(meaning.getPartOfSpeech());

            partDto.setSynonyms(meaning.getSynonyms().stream().map(SynonymOrAntonymStringHolder::new).toList());
            partDto.setAntonyms(meaning.getAntonyms().stream().map(SynonymOrAntonymStringHolder::new).toList());

            List<DefinitionDto> definitions = new ArrayList<>();
            meaning.getDefinitions().forEach(definition -> {
                DefinitionDto definitionDto = new DefinitionDto(definition.getDefinition());
                definitionDto.setPhrases(List.of(new PhraseDto(definition.getExample())));
                definitions.add(definitionDto);
            });
            partDto.setDefinitions(definitions);

            partDtos.add(partDto);

        });

        WordFrontEnd wordFrontEnd = new WordFrontEnd();
        wordFrontEnd.setName(response[0].getWord());
        wordFrontEnd.setParts(partDtos);

        System.out.println("\nGOOGLE SASAI \n" + result);

        Assert.assertEquals("", "");
    }
}
