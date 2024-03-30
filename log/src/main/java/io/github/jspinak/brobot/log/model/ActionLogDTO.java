package io.github.jspinak.brobot.log.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.HashSet;
import java.util.Set;

@Document(indexName = "actionlog")
public class ActionLogDTO {
    @Id
    private String id;
    private String action;
    private boolean success;
    private Set<String> images = new HashSet<>();
    private Set<String> ownerStates = new HashSet<>();
}
