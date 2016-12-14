# LIFT: Deduced BGPs of queries executed in isolation

In this page, we present LIFT's deduced BGPs using as input traces of queries in http://client.linkeddatafragments.org, each executed one by one.

**Summary**


1. [**Deduced BGPs per query**](https://github.com/coumbaya/lift/blob/master/experiments_with_client.linkeddatafragments.org#deduced-bgps-per-query)

2.  [**Recall and precision plots**](https://github.com/coumbaya/lift/blob/master/experiments_with_client.linkeddatafragments.org#recall-and-precision-plots)

3.   [**Appendix: general information**](https://github.com/coumbaya/lift/blob/master/experiments_with_client.linkeddatafragments.org#general-dataset-information)
   * [Executed queries per dataset](https://github.com/coumbaya/lift/blob/master/experiments_with_client.linkeddatafragments.org#executed-queries-per-dataset)
   * [IRI prefixes to authorities](https://github.com/coumbaya/lift/blob/master/experiments_with_client.linkeddatafragments.org#iri-prefixes-to-authorities)


## Deduced BGPs per query

Next, we present deduced BGPs of LIFT as well as precision recall of joins per query, as presented in http://client.linkeddatafragments.org/, each executed in isolation.

| ID  | Query             | Deduced BGPs                       | Recall of joins | Precision of joins |
| ----|:-----------------:|:----------------------------------:| ------------------ |:---------------:|
| Q1           | SELECT ?movie ?title ?name WHERE { ?movie dbpedia-owl:starring ?actor . (tp1) ?actor rdfs:label "Brad Pitt"@en . (tp2) ?movie rdfs:label ?title . (tp3) ?movie dbpedia-owl:director  director . (tp4) ?director rdfs:label ?name . (tp5) FILTER LANGMATCHES(LANG(?title), "EN") FILTER LANGMATCHES(LANG(?name), "EN") }  |    |          |       |
| Q2           | SELECT ?title ?classification WHERE { ?author foaf:name "Anne De Paepe" .   (tp1) ?publication dc:creator ?author .    (tp2) ?publication dc:title ?title .   (tp3) ?publication <http://lib.ugent.be/classification/classification>  ?classification .   (tp4) } |     |      |         |
| Q3           | SELECT DISTINCT ?entity WHERE {   ?entity a dbpedia-owl:Airport .   (tp1)   ?entity dbpprop:cityServed dbpedia:Italy  .   (tp2)  }  |     |          |       |
| Q4           |  ?city  rdf:type dbpclass:AncientCities.   (tp1) ?city dbpedia-owl:populationTotal ?popTotal.  (tp2) OPTIONAL {   ?city dbpedia-owl:populationMetro ?popMetro .   (tp3) } FILTER (?popTotal > 50000) } ORDER BY DESC(?popTotal)          |    |           |         |
| Q5           | SELECT ?name ?deathDate WHERE { ?person a dbpedia-owl:Artist . (tp1)  ?person   rdfs:label ?name . (tp2)  ?person dbpedia-owl:birthPlace  ?place . (tp3) ?place rdfs:label "York"@en . (tp4)   FILTER LANGMATCHES(LANG(?name),  "EN")  OPTIONAL {  ?person dbpprop:dateOfDeath ?deathDate.  (tp5)  }  }         |    |        |         |
| Q6           | SELECT {  ?artist a dbpedia-owl:Artist.  ?artist dbpedia-owl:birthDate ?date.  }    WHERE {    ?artist dbpedia-owl:influencedBy dbpedia:Pablo_Picasso .    (tp1)    ?artist a dbpedia-owl:Artist .  (tp2)   ?artist dbpedia-owl:birthDate ?date .    (tp3)   }         |     |           |        |
| Q7           | SELECT DISTINCT ?book ?author WHERE {  ?book rdf:type dbpedia-owl:Book .   (tp1)  ?book dbpedia-owl:author ?author.    (tp2)  }  LIMIT 100 |      |          |       |
| Q8           | SELECT ?award WHERE {  ?award a dbpedia-owl:Award .   (tp1)  ?award dbpprop:country ?language .   (tp2)  ?language dbpedia-owl:language dbpedia:Dutch_language .   (tp3)  } |  | |      |
| Q9           | SELECT DISTINCT ?artist ?band WHERE { {  <http://dbpedia.org/resource/Queen_(band)>  dbpedia-owl:bandMember ?artist .   (tp1)  }  UNION {  <http://dbpedia.org/resource/Queen_(band)>   dbpedia-owl:formerBandMember ?artist .   (tp2)  } ?artist dbpedia-owl:associatedBand ?band.   (tp3) } |     |          |      |
| Q10          | SELECT DISTINCT ?performer ?name WHERE {  ?work dbpedia-owl:writer dbpedia:Michael_Jackson .  (tp1)  ?work dbpedia-owl:musicalArtist ?performer .  (tp2)  OPTIONAL   {  ?performer rdfs:label ?name.  (tp3)  FILTER LANGMATCHES(LANG(?name), "EN")  }     }       |     |          |      |
| Q11          | SELECT ?software ?company WHERE { ?software dbpedia-owl:developer ?company .  (tp1)  ?company dbpedia-owl:locationCountry ?country .   (tp2)  ?country rdfs:label "Belgium"@en .  (tp3) }          |   |          |     |
| Q12          | SELECT ?person WHERE {  ?person a yago:Carpenters .  (tp1)  ?person a yago:PeopleExecutedByCrucifixion .  (tp2)  }         |     |        |     |
| Q13          | SELECT ?actor ?cause WHERE { ?actor dbpedia-owl:deathCause ?cause .  (tp1) ?actor dc:subject dbpedia-cat:American_male_film_actors  (tp2) } |     |           |      |
| Q14          | SELECT ?dessert ?fruit WHERE { ?dessert dbpedia-owl:type dbpedia:Dessert. (tp1) ?dessert dbpedia-owl:ingredient ?fruit .  (tp2) ?fruit dbpedia-owl:kingdom dbpedia:Plant . (tp3) }         |   |          |       |
| Q15          | SELECT DISTINCT ?device WHERE { dbpedia:Raspberry_Pi dbpprop:os ?operatingSystem .   (tp1)   ?device a dbpedia-owl:Device .   (tp2)   ?device  dbpprop:os ?operatingSystem .  (tp3)       FILTER (!(?device = dbpedia:Raspberry_Pi))  } |0s     | LS7           |1m38s       |
| Q16          | SELECT DISTINCT ?entity ?event WHERE { ?entity a dbpedia-owl:Event .   (tp1)  ?entity  rdfs:label ?event .   (tp2)  ?entity ?predicate dbpedia.org:Trentino .  (tp3)  FILTER(langMatches(lang(?event), "EN"))  } |     |          |       |
| Q17          | SELECT ?s WHERE { ?s a rdfs:Class .   (tp1)  ?s rdfs:subClassOf dcat:Dataset .   (tp2)  }          |   |          |      |
| Q18         | SELECT distinct ?ontology ?prefix WHERE { ?ontology a owl:Ontology . (tp1)  ?ontology vann:preferredNamespacePrefix ?prefix . (tp2)  ?ontology  dc:creator ?creator .  (tp3)  ?creator foaf:name "Pieter Colpaert" . (tp4)  } LIMIT 100       |     |           |       |
| Q19          | SELECT ?titleEng ?title WHERE { ?movie dbpprop:starring ?actor .  (tp1)  ?actor rdfs:label "Natalie Portman"@en  .  (tp2)  ?movie rdfs:label ?titleEng .  (tp3)  ?movie rdfs:label ?title .  (tp4)  FILTER LANGMATCHES(LANG(?titleEng), "EN") FILTER (!LANGMATCHES(LANG(?title), "EN")) }          |    |           |       |
| Q20          | SELECT ?indDish ?belDish ?ingredient  WHERE { ?indDish a dbpedia-owl:Food . (tp1)  ?indDish dbpedia-owl:origin dbpedia:India . (tp2)  ?indDish dbpedia-owl:ingredient ?ingredient . (tp3)  ?belDish a dbpedia-owl:Food . (tp4)  ?belDish dbpedia-owl:origin dbpedia:Belgium . (tp5) ?belDish dbpedia-owl:ingredient ?ingredient . (tp6) 
}         |    |          |       |
| Q21          | SELECT DISTINCT ?person WHERE { dbpedia:Jesus dc:subject ?common . (tp1)  ?person a foaf:Person . (tp2)  ?person dc:subject ?common. (tp3)  } LIMIT 1000         |    |           |      |
| Q22          | SELECT ?place ?relation WHERE { ?place rdf:type dbpedia-owl:Settlement . (tp1) ?place ?relation dbpedia:Barack_Obama . (tp2)  } |     |           |      |
| Q23          | SELECT ?jansen ?janssen ?title {  ?publication dc:creator ?creator .  (tp1) ?creator foaf:givenname ?jansen .  (tp2) ?creator foaf:surname "Jansen" .  (tp3) ?publication dc:creator ?name .  (tp4) ?name foaf:givenname ?janssen .  (tp5) ?name foaf:surname "Janssen" .  (tp6) ?publication dc:title ?title .  (tp7) }         |    |         |      |
| Q24          | SELECT ?clubName ?playerName WHERE { ?club a dbpedia-owl:SoccerClub .  (tp1) ?club  dbpedia-owl:ground ?city .  (tp2) ?club  rdfs:label ?clubName .  (tp3) ?player dbpedia-owl:team ?club .  (tp4) ?player  dbpedia-owl:birthPlace ?city .  (tp5) ?player rdfs:label ?playerName .  (tp6) ?city dbpedia-owl:country dbpedia:Spain.  (tp7) FILTER LANGMATCHES(LANG(?clubName), "EN") FILTER LANGMATCHES(LANG(?playerName), "EN") }         |    |           |   |
| Q25          | SELECT DISTINCT ?coauthorName { ?publication1 dc:creator ?coauthor . (tp1) ?coauthor   foaf:name "Etienne Vermeersch". (tp2) ?coauthor   foaf:name ?coauthorName.. (tp3)  XOR {  ?publication2 dc:creator ?coauthor .  (tp4) ?coauthor   foaf:name "Freddy Mortier". (tp5) ?coauthor foaf:name ?coauthorName. (tp6) } }       |   |          |       |
| Q26          | SELECT distinct ?license  WHERE { ?ontology a owl:Ontology . (tp1) ?ontology  dc:license ?license. (tp2) } |    |          |      |
| Q27          | SELECT ?entity ?label ?comment  WHERE { ?entity a dbpedia-owl:MythologicalFigure .  (tp1) ?entity  rdfs:label ?label .  (tp2) ?entity dc:subject  <http://dbpedia.org/resource/Category:Women_in_Greek_mythology>.  (tp3) ?entity rdfs:comment ?comment .  (tp4) FILTER(langMatches(lang(?label), "EN")) FILTER(langMatches(lang(?comment), "EN")) }         |    |      |      |
| Q28          | SELECT ?name ?work ?title WHERE{  ?artist dbpedia-owl:movement ?movement .    (tp1) ?movement rdfs:label "Cubism"@en .   (tp2) ?artist foaf:name ?name .   (tp3) ?work schema:author ?author .  (tp4) ?author schema:sameAs ?artist .  (tp5) ?work schema:name ?title .  (tp6) FILTER (!REGEX(?name, ",")) }        |     |           |       |
| Q29          | SELECT DISTINCT ?subject ?label WHERE { { ?subject dc:title ?label  (tp1) } UNION { ?subject rdfs:label ?label  (tp2) } }LIMIT 100         |    |         |     |
| Q30          |SELECT DISTINCT ?type ?label WHERE {  ?s a ?type.  OPTIONAL {  ?type rdfs:label ?label.  }   FILTER LANGMATCHES(LANG(?label), "EN")  } LIMIT 100        |     |   |     |


## Recall and precision plots

In the next two Figures, we view recall and precision of joins, per query executed in isolation.

![GitHub Logo](https://github.com/coumbaya/lift/blob/master/plots/precision_joins_client_ldf_per_query.PNG)
![GitHub Logo](https://github.com/coumbaya/lift/blob/master/plots/precision_joins_client_ldf_per_query.PNG)


## Appendix: general information

In this section, we illustrate which queries are executable per dataset, and also present a table matching each IRI's authority to its corresponding prefix.

### Executed queries per dataset

In the table below, we see which queries correspond to every dataset, representing a distinct data provider/LDF server entity.


| Dataset                                         | Queries               |
| ------------------------------------------------|:---------------------:|
| DBpedia 2015-04                                 | Q1, Q3, Q4, Q5, Q6, Q7, Q8, Q9, Q10, Q11, Q12, Q13, Q14, Q15, Q16, Q19, Q20, Q21, Q22, Q24, Q27, Q28, Q29, Q30 |
| Ghent University Academic bibliography (Ughent) | Q2, Q23, Q25, Q29, Q30 |
| Linked Open Vocabulairies (LOV)                 | Q17, Q18, Q26          |
| Virtual International Authority File (VIAF)     | Q29, Q29, Q30          |


**Note**: Q28 needs two datasets to be answered, DBpedia and VIAF.

### IRI prefixes to authorities

Finally, we match each authority to a prefix for the corresponding datasets, as presented above.

| Prefix         | Authority                                                             |
| ---------------|:---------------------------------------------------------------------:|
| dbpedia-cat    | http://www.w3.org/ns/dcat#> |
| geo            | http://www.w3.org/2003/01/geo/wgs84_pos# |
| rdfs           | http://www.w3.org/2000/01/rdf-schema# |
| rdf            | http://www.w3.org/1999/02/22-rdf-syntax-ns# |
| owl            | http://www.w3.org/2002/07/owl# |
| skos           | http://www.w3.org/2004/02/skos/core# |
| xsd            | http://www.w3.org/2001/XMLSchema# |
| hydra          | http://www.w3.org/ns/hydra/core# |
| dc             | http://purl.org/dc/terms/ |
| dc11           | http://purl.org/dc/elements/1.1/ |
| dctitle        | http://purl.org/dc/terms/title |
| vann           | http://purl.org/vocab/vann/ |
| foaf           | http://xmlns.com/foaf/0.1/ |
| void           | http://rdfs.org/ns/void# |
| ugent          | http://lib.ugent.be/classification/classification |
| schema         | http://schema.org/sameAs |
| dbpedia        | http://dbpedia.org/resource/ |
| dbpedia-owl    | http://dbpedia.org/ontology/> |
| dbpprop        | http://dbpedia.org/property/ |
| dbpclass       | http://dbpedia.org/class/yago/ |
| dbpedia-cat    | http://dbpedia.org/resource/Category |
| ugent-biblio   | http://data.linkeddatafragments.org/.well-known/genid/ugent-biblio/ |

