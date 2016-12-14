# LIFT: Deduced BGPs for DBpedia's real log

In this page, we present LIFT's deduced BGPs using as input traces of the real log of DBpedia, as regenerated with its answers.

| Frequency  | Deduced BGP                                                |
| -----------|:----------------------------------------------------------:|
| 125        | ?s1 http://www.w3.org/2000/01/rdf-schema#label "Brad Pitt"@en. ?s2 http://dbpedia.org/ontology/starring ?s1. ?s2 http://www.w3.org/2000/01/rdf-schema#label ?o3. ?s2 http://dbpedia.org/ontology/director ?o3 . ?o3 http://www.w3.org/2000/01/rdf-schema#label ?o5 |
| 45        | ?s1 http://www.w3.org/2000/01/rdf-schema#label "Brad Pitt"@en. ?s2 http://dbpedia.org/property/starring ?s1. ?s2 http://www.w3.org/2000/01/rdf-schema#label ?o3. ?s2 http://dbpedia.org/ontology/director ?o3 . ?o3 http://www.w3.org/2000/01/rdf-schema#label ?o5 |
| 43        | ?s1 http://www.w3.org/2000/01/rdf-schema#label "York"@en. ?s2 http://dbpedia.org/ontology/birthPlace ?s1. ?s2 http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://dbpedia.org/ontology/Artist |
| 33        | ?s1 http://dbpedia.org/property/cityServed http://dbpedia.org/resource/Italy. ?s2 http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://dbpedia.org/ontology/Airport |
