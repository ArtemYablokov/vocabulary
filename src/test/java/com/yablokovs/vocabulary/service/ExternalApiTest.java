package com.yablokovs.vocabulary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yablokovs.vocabulary.mdto.externalApi.localMapper.DefinitionToExampleTuple;
import com.yablokovs.vocabulary.mdto.externalApi.Root;
import com.yablokovs.vocabulary.mdto.externalApi.localMapper.Meaning;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

public class ExternalApiTest {


    @Test
    @SneakyThrows
    public void testApi() {

        String epam = "https://epam.com";
        String eng = "https://api.dictionaryapi.dev/api/v2/entries/en/sell";
        var request = HttpRequest.newBuilder(URI.create(eng))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofMillis(1000))
                .GET()
                .build();
        var client = HttpClient.newHttpClient();
        var result = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

        Root[] response = new ObjectMapper().readValue(result, Root[].class);

        Map<String, Meaning> partOfSpeechToDefinition = new HashMap<>();

//        Assert.assertEquals(response.length, 0); - not always come only one word !!!

        response[0].getMeanings().forEach(meaning -> {
            List<DefinitionToExampleTuple> definitionToExampleTuples = new ArrayList<>();

            meaning.getDefinitions().forEach(
                    definition -> definitionToExampleTuples.add(
                            new DefinitionToExampleTuple(definition.getDefinition(), definition.getExample())));

            partOfSpeechToDefinition.put(meaning.getPartOfSpeech(),
                    new Meaning(meaning.getSynonyms(), meaning.getAntonyms(), definitionToExampleTuples));
        });

        System.out.println("\nGOOGLE SASAI \n" + result);

        Assert.assertEquals("", "");
    }
}
