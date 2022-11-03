package com.yablokovs.vocabulary.service;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class IdTuple {
    private Long child;
    private Long parent;
}
