package com.yablokovs.vocabulary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yablokovs.vocabulary.mdto.externalApi.Root;
import com.yablokovs.vocabulary.mdto.request.*;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExternalService {

    @SneakyThrows
    public WordFrontEnd findWord(String string) {

        String eng = "https://api.dictionaryapi.dev/api/v2/entries/en/" + string;
        var request = HttpRequest.newBuilder(URI.create(eng))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofMillis(1000))
                .GET()
                .build();
        var client = HttpClient.newHttpClient();
        var result = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

        Root[] response = new ObjectMapper().readValue(result, Root[].class);

        List<PartDto> partDtos = new ArrayList<>();
        response[0].getMeanings().forEach(meaning -> {
            PartDto partDto = new PartDto(meaning.getPartOfSpeech());

            partDto.setSynonyms(meaning.getSynonyms().stream().map(StringHolder::new).toList());
            partDto.setAntonyms(meaning.getAntonyms().stream().map(StringHolder::new).toList());

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

        return wordFrontEnd;
    }
}
