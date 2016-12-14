# LIFT: Deduced BGPs for DBpedia's real log

In this page, we present LIFT's deduced BGPs using as input traces of the real log of DBpedia, as regenerated with its answers.

| Frequency  | Deduced BGP                                                |
| -----------|:----------------------------------------------------------:|
| Q1           | SELECT ?movie ?title ?name WHERE { ?movie dbpedia-owl:starring ?actor . (tp1) ?actor rdfs:label "Brad Pitt"@en . (tp2) ?movie rdfs:label ?title . (tp3) ?movie dbpedia-owl:director  director . (tp4) ?director rdfs:label ?name . (tp5) FILTER LANGMATCHES(LANG(?title), "EN") FILTER LANGMATCHES(LANG(?name), "EN") }  |

