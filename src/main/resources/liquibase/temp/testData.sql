INSERT INTO wordrus (name, part_of_speech) VALUES ('SynR1', 'noun');
INSERT INTO wordrus (name, part_of_speech) VALUES ('SynR2', 'noun');
INSERT INTO wordrus (name, part_of_speech) VALUES ('AntR3', 'noun');
INSERT INTO wordrus (name, part_of_speech) VALUES ('AntR4', 'noun');
INSERT INTO wordrus (name, part_of_speech) VALUES ('test', 'noun');

INSERT INTO rus_synonym (word_rus_id, synonym_rus_id) VALUES (1, 2);
INSERT INTO rus_synonym (word_rus_id, synonym_rus_id) VALUES (2, 1);

INSERT INTO rus_antonym (word_rus_id, antonym_rus_id) VALUES (3, 4);
INSERT INTO rus_antonym (word_rus_id, antonym_rus_id) VALUES (4, 3);

INSERT INTO word (createdat, name, numberofsearches, updatedat) VALUES (NOW(), 'SynE1', null, NOW());
INSERT INTO word (createdat, name, numberofsearches, updatedat) VALUES (NOW(), 'SynE2', null, NOW());
INSERT INTO word (createdat, name, numberofsearches, updatedat) VALUES (NOW(), 'AntE3', null, NOW());
INSERT INTO word (createdat, name, numberofsearches, updatedat) VALUES (NOW(), 'AntE4', null, NOW());

INSERT INTO part (name, word_id) VALUES ('noun', 1);
INSERT INTO part (name, word_id) VALUES ('noun', 2);
INSERT INTO part (name, word_id) VALUES ('noun', 3);
INSERT INTO part (name, word_id) VALUES ('noun', 4);

INSERT INTO part_synonym (synonym_id, part_id) VALUES (1, 2);
INSERT INTO part_synonym (synonym_id, part_id) VALUES (2, 1);

INSERT INTO part_antonym (antonym_id, part_id) VALUES (3, 4);
INSERT INTO part_antonym (antonym_id, part_id) VALUES (4, 3);

