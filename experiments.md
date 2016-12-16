# LIFT Experiments 


In this page, we present LIFT's deduced BGPs as well as performance in recall/precision, using as input:<br><br> 
(1) traces of queries in http://client.linkeddatafragments.org, each executed one by one, and,<br> 
(2) traces of DBpedia LDF server's real log, from [USEWOD](http://usewod.org/data-sets.html) dataset, for the period of 14th October 2014-27th February 2015.

**Summary**


1. [**Deduced BGPs per executed query of TPF servers**](https://github.com/coumbaya/lift/blob/master/experiments.md#deduced-bgps-per-executed-query-of-tpf-servers)

2.  [**Most frequent deduced bgps for usewod 2016**](https://github.com/coumbaya/lift/blob/master/experiments.md#most-frequent-deduced-bgps-for-usewod-2016)

3.  [**Recall and precision plots**](https://github.com/coumbaya/lift/blob/master/experiments.md#recall-and-precision-plots)

4.   [**Appendix information**](https://github.com/coumbaya/lift/blob/master/experiments.md#appendix-information)
   * [Concurently executed query sets](https://github.com/coumbaya/lift/blob/master/experiments.md#concurently-executed-query-sets)
   * [IRI prefixes to authorities](https://github.com/coumbaya/lift/blob/master/experiments.md#iri-prefixes-to-authorities)


## Deduced BGPs per executed query of TPF servers

We extracted 30 queries from the TPF web site 3 concerning DBpedia 2015-04, UGhent, LOV and VIAF. We captured http requests and answers of queries
using the webInspector 1.2 tool, available [here](https://sourceforge.net/p/webinspector/wiki/Home/), which is preinstalled into the webrowser (e.g., Firefox or Google Chrome). We executed each query in isolation one from another, and run LIFT using the maximum possible gap interval, a constraint window between two triple patterns to consider them possibly as part of the same nested loop. For more details on datasets and queries, see [last section](https://github.com/coumbaya/lift/blob/master/experiments.md#appendix-information).

In the next Table, we view deduced BGPs and recall/precision of joins, per query executed in isolation over DBpedia, VIAF, LOV and Ughent.

|ID | Query                                            | Deduced BGPs      |Recall|Precision|
|---|:-------------------------------------------------|:------------------|:---:|:---:|
|Q1 | SELECT ?movie ?title ?name WHERE {<br>(tp1): ?movie dbpedia-owl:starring <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?actor . <br>(tp2): ?actor rdfs:label "Brad Pitt"@en . <br>(tp3): ?movie rdfs:label ?title . <br>(tp4): ?movie dbpedia-owl:director  <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?director . <br>(tp5): ?director rdfs:label ?name <br>   FILTER LANGMATCHES<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(LANG(?title), "EN")<br>   FILTER LANGMATCHES<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(LANG(?name), "EN") <br>}  | BGP_1 { <br> (tp2):    ?s1    rdfs:label    "Brad Pitt"@en .  <br>(tp1):  ?s2   dbpedia:starring   ?s1 . <br>(tp3):    ?s1     rdfs:label  ?o3 . <br>(tp4):  ?s1   dbpedia-owl:director   ?o4 .  <br>(tp5):    ?o4    rdfs:label     ?o5 <br>} |    1      |  1    |
|Q2 | SELECT ?title ?classification WHERE {<br>(tp1): ?author foaf:name <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"Anne De Paepe" . <br>(tp2): ?publication dc:creator ?author .  <br>(tp3): ?publication dc:title ?title . <br>(tp4): ?publication  <br>&nbsp;ugent-biblio:classification ?classification  <br>} | BGP_1{<br>  (tp1):   ?s1     foaf:name     "Anne De Paepe"  . <br>(tp2):   ?s2     dcterms:creator  ?s1 .  <br>(tp4):   ?s2     ugent:classification    ?o3 . <br>(tp3):   ?s2      dctitle:title     ?o4 <br>} | 1   |  1  |
|Q3 | SELECT DISTINCT ?entity WHERE {<br>(tp1): ?entity a dbpedia-owl:Airport . <br>(tp2): ?entity dbpprop:cityServed<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia:Italy <br>}  | BGP_1{<br>  (tp2):   ?s1  dbpprop:cityServed <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; dbpedia:Italy  . <br> (tp1_equiv):  ?s1    rdf:type <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia-owl:Airport<br>}  | 1 | 1 |
|Q4 | SELECT * WHERE {<br> (tp1): ?city  rdf:type<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpclass:AncientCities . <br>(tp2): ?city dbpedia-owl:populationTotal<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?popTotal . <br>OPTIONAL {<br>(tp3): ?city dbpedia-owl:populationMetro<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?popMetro } <br>&nbsp;&nbsp;&nbsp;FILTER (?popTotal > 50000) <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ORDER BY DESC(?popTotal) <br> } | BGP_1{<br>(tp1): ?s1 rdf:type dbpclass:AncientCities  . <br>(tp2):   ?s1 dbpedia-owl:populationTotal<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ?o2 . <br>(tp3):   ?s1     dbpedia-owl:populationMetro<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ?o3 } | 1  | 1 |
|Q5 | SELECT ?name ?deathDate WHERE {<br> (tp1): ?person a dbpedia-owl:Artist . <br> (tp2): ?person  rdfs:label ?name . <br>(tp3): ?person dbpedia-owl:birthPlace <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?place . <br>(tp4): ?place rdfs:label "York"@en .  <br> OPTIONAL {<br>(tp5): ?person dbpprop:dateOfDeath<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?deathDate } <br>&nbsp;&nbsp;&nbsp;FILTER LANGMATCHES<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(LANG(?name),"EN")<br> } | BGP_1{<br> (tp4):  ?s1     rdf:label      "York"@en . <br>(tp3):  ?s2    dbpedia-owl:birthPlace   ?s1 . <br>(tp1):  ?s2   rdf:type  dbpedia-owl:Artist . <br>(tp2):  ?s2  rdf:label     ?o4 . <br>(tp5):  ?s2   dbpprop:dateOfDeath  ?o5<br>} | 1 | 1 |
|Q6 | CONSTRUCT  {<br>?artist a dbpedia-owl:Artist . <br>?artist dbpedia-owl:birthDate ?date <br>}<br> <br> WHERE {<br>(tp1): ?artist dbpedia-owl:<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia:Pablo_Picasso . <br>(tp2): ?artist a dbpedia-owl:Artist . <br>(tp3): ?artist dbpedia-owl:birthDate <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?date <br>} | BGP_1{<br> (tp1):    ?s1  dbpedia-owl:influencedBy <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; dbpedia:Pablo_Picasso . <br>(tp2_equiv):   ?s1    rdf:type <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; dbpedia-owl:Artist  . <br>(tp3):    ?s1    dbpedia-owl:birthDate  ?o3<br>}  | 1 | 1 |
|Q7 | SELECT DISTINCT ?book ?author WHERE {<br>(tp1): ?book rdf:type dbpedia-owl:Book . <br>(tp2): ?book dbpedia-owl:author ?author <br>} LIMIT 100 |  BGP_1{<br>(tp1):   ?s1     rdf:type     dbpedia-owl:Book . <br> (tp2):  ?s1   dbpedia-owl:author     ?o2<br>} <br><br> BGP_2{<br> (tp2'_a):   ?s1   dbpedia-owl:author   ?o1 . <br>(tp2'_b):   ?s1    dbpedia-owl:author     ?o2<br>}  |  1 | 0,5 |
|Q8 | SELECT ?award WHERE {<br>(tp1): ?award a dbpedia-owl:Award . <br>(tp2): ?award dbpprop:country<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?language . <br>(tp3): ?language dbpedia-owl:language <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia:Dutch_language <br>} | BGP_1{ <br>(tp3):   ?s1    dbpedia-owl:language  <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia:Dutch_language . <br>(tp2):   ?s2    dbpprop:country    ?s1 . <br> (tp1'_a):   ?s1     rdf:type      dbpedia-owl:Award .  <br> (tp1_equiv):  ?s2rdf:type<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; dbpedia-owl:Award  <br>}  |  1 |  0,67  |
|Q9 | SELECT DISTINCT ?artist ?band WHERE {<br>{ (tp1): dbpedia:Queen_(band)<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia-owl:bandMember ?artist .}<br>UNION<br>{ (tp2): dbpedia:Queen_(band) <br>dbpedia-owl:formerBandMember ?artist .}<br><br> (tp3): ?artist dbpedia-owl:associatedBand<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?band .<br>} |  BGP_1{<br>(tp1):   dbpedia:Queen_(band)<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia-owl:bandMember  ?o1 . <br>(tp3):   ?o1  dbpedia-owl:associatedBand <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?o2<br>}<br><br>BGP_2{<br>(tp2):  dbpedia:Queen_(band)<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; dbpedia-owl:bandMember  ?o1 .<br>(tp3):  ?o1  <br>&nbsp;&nbsp;&nbsp;dbpedia-owl:formerBandMember   ?o2<br>}  |   1  |  1 |
|Q10| SELECT DISTINCT ?performer ?name WHERE {<br>(tp1): ?work dbpedia-owl:writer <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia:Michael_Jackson . <br> (tp2): ?work dbpedia-owl:musicalArtist<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?performer . <br>  OPTIONAL  {<br> (tp3): ?performer rdfs:label ?name }<br>&nbsp;&nbsp;&nbsp; FILTER LANGMATCHES<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;LANG((?name), "EN")<br> }   |  BGP_1{<br> (tp1):     ?s1   dbpedia-owl:writer<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia:Michael_Jackson . <br>(tp2):     ?s1  dbpedia-owl:musicalArtist     ?o2 . <br>(tp3'_a):    ?s1    rdfs:label    ?o3 . <br>(tp3):     ?o2    rdfs:label       ?o4 <br>}<br><br>BGP_2{<br>(tp2'_a): ?s1 <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia-owl:musicalArtist ?o1 . <br>(tp3'_b):    ?s1   rdfs:label       ?o2  }   |  1 | 1 |
|Q11| SELECT ?software ?company WHERE {<br>(tp1): ?software dbpedia-owl:developer<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?company . <br>(tp2): ?company<br>&nbsp;&nbsp; dbpedia-owl:locationCountry ?country . <br> (tp3): ?country rdfs:label "Belgium"@en <br>}   | BGP_1{<br>(tp3):    ?s1    rdfs:label     "Belgium"@en . <br>(tp2): ?s2    dbpedia-owl:locationCountry <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?s1 . <br>(tp1):  ?s3    dbpedia-owl:developer    ?s2<br>}   |  1 | 1 |
|Q12| SELECT ?person WHERE {<br>(tp1): ?person a yago:Carpenters . <br>(tp2): ?person a <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;yago:PeopleExecutedByCrucifixion <br>} |  BGP_1{<br> (tp2_equiv):    ?s1  rdf:type <br>&nbsp;&nbsp;&nbsp;dbpclass:PeopleExecutedByCrucifixion . <br>(tp1_equiv):   ?s1  rdf:type <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpclass:Carpenters <br>}<br><br>BGP_2{<br>  (tp2'_a):    ?s1   rdf:type<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; dbpclass:Carpenters . <br> (tp1'_a):   ?s1    rdf:type<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpclass:Carpenters<br>}    | 1 | 0,5 |
|Q13| SELECT ?actor ?cause WHERE {<br>(tp1): ?actor dbpedia-owl:deathCause<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  ?cause . <br> (tp2): ?actor dc:subject<br>&nbsp;&nbsp;&nbsp;dbpedia-cat:American_male_film_actors <br>} |  BGP_1{<br>(tp2_equiv):    ?s1 dbpedia-owl:deathCause ?o1 . <br> (tp1_equiv):   ?s1  dcterms:subject<br>&nbsp;&nbsp;&nbsp;&nbsp;
dbpedia-cat:American_male_film_actors <br>}<br>BGP_2{ <br (tp2'_a):  ?s1  dcterms:subject<br>&nbsp;&nbsp;&nbsp;&nbsp;
dbpedia-cat:American_male_film_actors . <br>(tp1'_a): ?s1  dcterms:subject<br>&nbsp;&nbsp;&nbsp;&nbsp;
dbpedia-cat:American_male_film_actors <br><br>}   | 1 | 0,5 |
|Q14| SELECT ?dessert ?fruit WHERE {<br>(tp1): ?dessert dbpedia-owl:type<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; dbpedia:Dessert . <br>(tp2): ?dessert dbpedia-owl:ingredient<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ?fruit . <br>(tp3): ?fruit dbpedia-owl:kingdom<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  dbpedia:Plant <br>} |  BGP_1{<br>(tp1):    ?s1     dbpedia-owl:type<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; dbpedia:Dessert . <br>(tp2):    ?s1     dbpedia-owl:ingredient  ?o2 . <br>(tp3):  ?o2   dbpedia-owl:kingdom     <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia:Plant<br>}<br><br>BGP_2{<br> (tp1'_a):  ?s1     dbpedia-owl:ingredient    ?o1 . <br>(tp2'_a):  ?s1   dbpedia-owl:ingredient    ?o2 . <br> (tp3'_a):  ?s1 dbpedia-owl:kingdom<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia:Plant . <br>(tp3'_b):  ?o2   dbpedia-owl:kingdom     <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia:Plant<br>}   | 1 | 0,4 |
|Q15| SELECT DISTINCT ?device WHERE {<br>(tp1): dbpedia:Raspberry_Pi dbpprop:os <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ?operatingSystem . <br>(tp2): ?device a <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; dbpedia-owl:Device . <br>(tp3): ?device  dbpprop:os <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ?operatingSystem <br>   FILTER<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; (!(?device = dbpedia:Raspberry_Pi)) <br>} |  BGP_1{ <br>  (tp1):   dbpedia:Raspberry_Pi     dbpprop:os <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?o1 . <br> (tp3):   ?s2      dbpprop:os ?o1   .  <br> (tp2_equiv):  ?s2     rdf:type<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia-owl:Device<br>}   | 1 | 1 |
|Q16| SELECT DISTINCT ?entity ?event WHERE {<br>(tp1): ?entity a dbpedia-owl:Event . <br>(tp2): ?entity  rdfs:label ?event . <br>(tp3): ?entity ?predicate dbpedia:Trentino <br>&nbsp;&nbsp;&nbsp;FILTER<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(langMatches(lang(?event), "EN")) <br>} | BGP_1{ <br> (tp3):    ?s1    ?p1     dbpedia:Trentino . <br> (tp1_equiv):   ?s1     rdf:type<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia-owl:Event . <br> (tp2):     ?s1    rdfs:label      ?o3<br>} |  1  |  1  |
|Q17| SELECT ?s WHERE {<br>(tp1): ?s a rdfs:Class . <br>(tp2): ?s rdfs:subClassOf dcat:Dataset <br>}  | BGP_1{<br>  (tp2):    ?s1   rdfs: subClassOf     dcat:Dataset . <br>(tp1_equiv):   ?s1      rdf:type    rdfs:Class<br>}  | 1 | 1 |
|Q18| SELECT distinct ?ontology ?prefix WHERE {<br>(tp1): ?ontology a owl:Ontology . <br>(tp2): ?ontology <br>vann:preferredNamespacePrefix ?prefix . <br>(tp3)?ontology  dc:creator ?creator . <br>(tp4): ?creator foaf:name <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"Pieter Colpaert" <br> } LIMIT 100  |  BGP_1{ <br> (tp4):    ?s1   foaf:name  "Pieter Colpaert" . <br> (tp3):    ?s2   dc:creator     ?s1 . <br> (tp1_equiv):   ?s2  rdf:type     owl:Ontology  . <br> (tp2):   ?s2<br>&nbsp;&nbsp;vann:preferredNamespacePrefix ?o4 <br>}  | 1 | 1 |
|Q19| SELECT ?titleEng ?title WHERE {<br>(tp1): ?movie dbpprop:starring ?actor . <br>(tp2): ?actor rdfs:label<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "Natalie Portman"@en . <br>(tp3): ?movie rdfs:label ?titleEng . <br>(tp4): ?movie rdfs:label ?title <br>   FILTER LANGMATCHES<br>&nbsp;&nbsp;&nbsp;(LANG(?titleEng), "EN")<br> FILTER<br>&nbsp;&nbsp;&nbsp;(!LANGMATCHES(LANG(?title), "EN")) <br>}  | BGP_1{<br> (tp2):    ?s1     rdfs:label<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"Natalie Portman"@en . <br> (tp1):    ?s2    dbpedia:starring  ?s1 . <br> (tp3 MERGED tp4):     ?s2  rdfs:label  ?o3<br>}  | 0,33 | 0,5 |
|Q20| SELECT ?indDish ?belDish ?ingredient  WHERE {<br>(tp1): ?indDish a dbpedia-owl:Food . <br>(tp2): ?indDish dbpedia-owl:origin<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; dbpedia:India . <br>(tp3): ?indDish dbpedia-owl:ingredient <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?ingredient . <br>(tp4): ?belDish a dbpedia-owl:Food . <br>(tp5): ?belDish dbpedia-owl:origin <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; dbpedia:Belgium . <br> (tp6): ?belDish <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia-owl:ingredient ?ingredient <br>}  | BGP_1{ <br>(tp1_or_4): ?s1 rdf:type dbpedia-owl:Food  (tp1_or_4'_a): ?s1 rdf:type <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; dbpedia-owl:Food <br> (tp2): ?s2 dbpedia-owl:origin <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia:India <br>(tp2'_a): ?s4 dbpedia-owl:origin <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia:India <br>(tp1_or_4'_b): ?s4 rdf:type <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia-owl:Food <br>(tp2'_b): ?s5 dbpedia-owl:orgin<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; dbpedia:India <br>(tp3): ?s1 dbpedia-owl:ingredient ?o7 <br>(tp1_or_4'_c): ?s5 rdf:type<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia-owl:Food <br>(tp3_or_6'_a): ?s8 dbpedia-owl:ingredient <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?o7 <br> (tp5'_a): ?o7 dbpedia-owl:origin <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia:Belgium <br>(tp6): ?s1 dbpedia-owl:ingredient <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?o7<br>(tp3_or_6'_b): ?s1 dbpedia-owl:ingredient ?o7<br>(tp5): ?s2 dbpedia-owl:ingredient <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia:Belgium<br>(tp3_or_6_c): ?s6 dbpedia-owl:ingredient <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?o14<br>(tp2_f): ?s8 dbpedia-owl:origin <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia:Belgium<br>(tp3_or_6_c): ?s8 dbpedia-owl:ingredient <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?s2 <br>}<br><br> BGP_2{<br>   (tp3_or_6_d): ?s1  dbpedia-owl:ingredient  <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?o1  <br> (tp1_or_4_d):  ?s1  rdf:type  <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia-owl:Food <br> (tp3_or_6_e): ?s3  dbpedia-owl:ingredient <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?s1 <br> (tp2'_c):  dbpedia-owl:origin     dbpedia:India <br> (tp1_or_4_d): ?s1    rdf:type <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia-owl:Food ?o5 <br>} |  1 | 0,24 |
|Q21| SELECT DISTINCT ?person WHERE {<br>(tp1): dbpedia:Jesus dc:subject <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?common . <br>(tp2): ?person a foaf:Person . <br>(tp3): ?person dc:subject ?common  <br>} LIMIT 1000  | BGP_1{ <br> (tp1):    dbpedia :Jesus    dc:subject   ?o1 . <br> (tp3):    ?s2    dc:subject    ?o1 . <br> (tp2_equiv):   ?s2      rdf:type      foaf:Person  <br>} <br><br> BGP_2{ <br> (tp2'_a):   ?s1  rdf:type      foaf:Person  . <br> (tp2'_b):   ?s1     rdf:type      foaf:Person <br>} | 1 | 0,67 |
|Q22| SELECT ?place ?relation WHERE {<br>(tp1): ?place rdf:type<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; dbpedia-owl:Settlement . <br> (tp2): ?place ?relation <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia:Barack_Obama . <br>} | BGP_1{ <br> (tp2):    ?s1   ?p1  dbpedia:Barack_Obama . <br> (tp1):     ?s1    rdf:type       <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia-owl:Settlement <br>} | 1 | 1 |
|Q23| SELECT ?jansen ?janssen ?title {<br> (tp1): ?publication dc:creator ?creator . <br> (tp2): ?creator foaf:givenname ?jansen . <br>(tp3): ?creator foaf:surname "Jansen" . <br>(tp4): ?publication dc:creator ?name . (tp5): ?name foaf:givenname ?janssen . <br>(tp6): ?name foaf:surname "Janssen" . <br>(tp7): ?publication dc:title ?title  <br>}  |  BGP_1{ <br> (tp3):   ?s1    foaf:surname     "Jansen" . <br> (tp2):    ?s1    foaf:givenname    ?o2 . <br> (tp1):    ?s3   dc:creator  ?s1 . <br>(tp6'_a):   ?s1     foaf:surname  "Janssen" . <br> (tp7):    ?s3   dc:title   ?o5 . <br> (tp4):    ?s3      dc:creator    ?o6 . <br> (tp5):    ?o6     foaf:givenname     ?o7 . <br> (tp4'_a):    ?s3     dc:creator    ?o8 . <br> (tp6):    ?o6    foaf:surname     "Janssen" <br>} | 1 | 0,75 |
|Q24| SELECT ?clubName ?playerName WHERE {<br>(tp1): ?club a dbpedia-owl:SoccerClub . <br>(tp2): ?club  dbpedia-owl:ground ?city . <br> (tp3): ?club  rdfs:label ?clubName . <br>(tp4): ?player dbpedia-owl:team ?club . <br>(tp5): ?player dbpedia-owl:birthPlace ?city.<br>(tp6): ?player rdfs:label ?playerName . <br>(tp7): ?city dbpedia-owl:country <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia:Spain <br>  FILTER LANGMATCHES<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; (LANG(?clubName), "EN")<br>   FILTER LANGMATCHES<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; (LANG(?playerName), "EN") <br>} |  BGP_1{ <br> (tp7): ?s1 dbpedia-owl:country<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia:Spain . <br> (tp2): ?s2     dbpedia-owl:ground     ?s1 . <br> (tp5): ?s3     dbpedia-owl:birthPlace  ?s1 . <br> (tp1): ?s2   rdf:type<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia-owl:SoccerClub . <br> (tp3): ?s2  rdf:label     ?o5  . <br> (tp4'_a):  ?s6     dbpedia-owl:team    ?s2 . <br> (tp6):  ?s3     rdf:label     ?o7 . <br>  (tp4): ?s3   dbpedia-owl:team   ?s2 . <br> (tp4'_b): ?s6     dbpedia-owl:team   ?s2 . <br>  (tp5'_a): ?s3     dbpedia-owl:birthPlace  ?s2  <br>}<br><br>  BGP_2{ <br> (tp5'_b):  ?s1     dbpedia-owl:birthPlace     ?o1 . <br> (tp7'_a):   ?s2    dbpedia-owl:country  ?s1  <br> .  (tp4'_c):  ?s3     dbpedia-owl:team  ?s2 . <br>  (tp6'_a):   ?s3  rdf:label  ?o . <br> (tp2'_a):   ?s     dbpedia-owl:ground  ?s2 . <br> (tp5'_b):  ?s     dbpedia-owl:birthPlace   ?s2 . <br>  (tp5'_c): ?s3  dbpedia-owl:birthPlace ?s2  <br>}  | 1 | 0,32 |
|Q25| SELECT DISTINCT ?coauthorName {<br>(tp1): ?publication1 dc:creator ?coauthor . <br> (tp2): ?coauthor foaf:name <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "Etienne Vermeersch". <br>(tp3): ?coauthor foaf:name <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ?coauthorName  <br><br>XOR {<br>(tp4): ?publication2 dc:creator ?coauthor . <br>(tp5): ?coauthor   foaf:name<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"Freddy Mortier". <br>(tp6): ?coauthor foaf:name <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;?coauthorName <br>}<br> } | BGP_1{ <br> (tp2): ?s1   foaf:name <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "Etienne Vermeersch" . <br> (tp1):  ?s2   dc:creator ?s1 . <br> (tp3): ?s1    foaf:name   ?o3  <br>}<br> <br> BGP_2{  <br> (tp6): ?s1    foaf:name<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"Freddy Mortier"  <br> (tp4): ?s2 dc:creator  ?s1 <br>  (tp4'_a): ?s2  dc:creator  ?o3  <br>  (tp4'_b): ?s2    dc:creator ?s1 . <br> (tp4'_c): ?s3     dc:creator ?o3  . <br> (tp5): ?s2    foaf:name    ?o6 . <br>  (tp4'_d): ?s2   dc:creator  ?o3 <br>}  | 1 | 0,44 |
|Q26| SELECT distinct ?license  WHERE {<br>(tp1): ?ontology a owl:Ontology . <br>(tp2): ?ontology  dc:license ?license <br>} | BGP_1{ <br> (tp2):    ?s1     dc:license     ?o1  . <br> (tp1_equiv):  ?s1     rdf:type    owl: Ontology <br>} <br><br> BGP_2{<br> (tp2):    ?s1      rdf:type     ?o1 . <br> (tp1'_a):  ?s1 rdf:type    owl: Ontology <br>}   | 1 | 0,5 |
|Q27| SELECT ?entity ?label ?comment  WHERE {<br>(tp1): ?entity a <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia-owl:MythologicalFigure . <br>(tp2): ?entity  rdfs:label ?label . <br>(tp3): ?entity dc:subject <br>dbpedia-cat:Women_in_Greek_mythology. <br>(tp4): ?entity rdfs:comment ?comment <br> FILTER<br>&nbsp;&nbsp;&nbsp;&nbsp;(langMatches(lang(?label), "EN"))<br>   FILTER<br>&nbsp;&nbsp;&nbsp;&nbsp;(langMatches(lang(?comment), "EN")) <br>}  |  BGP_1{<br> (tp3): ?s1   dc:subject<br>&nbsp;&nbsp;&nbsp;dbpedia-cat::Women_in_Greek_mythology . <br> (tp1_a): ?s1  rdf:type<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia-owl:MythologicalFigure . <br> (tp4): ?s1  rdfs:comment ?o3 . <br> (tp2): ?s1  rdfs:label ?o4 <br>} <br><br> BGP_2{ <br>  (tp3'_a): ?s1   dc:subject <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia-owl:MythologicalFigure . <br> (tp1'_a):  ?s1    dc:subject <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dbpedia-owl:MythologicalFigure . <br> (tp4'_a): ?s1   rdfs:comment ?o3 . <br> (tp2'_a): ?s1  rdfs:label ?o4<br>}   | 1 | 0,5 |
|Q28| SELECT ?name ?work ?title WHERE {<br>(tp1): ?artist dbpedia-owl:movement<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ?movement . <br>(tp2): ?movement rdfs:label<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"Cubism"@en . <br>(tp3): ?artist foaf:name ?name . <br>(tp4): ?work schema:author ?author . <br>(tp5): ?author schema:sameAs ?artist . <br>(tp6): ?work schema:name ?title <br>   FILTER (!REGEX(?name, ",")) <br>} |  |  |   |
|Q29| SELECT DISTINCT ?subject ?label WHERE {<br>{ (tp1): ?subject dc:title ?label }<br>UNION<br>{ (tp2): ?subject rdfs:label ?label } <br>}LIMIT 100  |  BGP_1{ <br> (tp1):  ?s     dc:itle     ?o <br>}<br> <br> BGP_2{<br>  (tp2):   ?s    rdfs:label        ?o <br>}   | 0/0 | 0/0 |
|Q30| SELECT DISTINCT ?type ?label WHERE {<br>(tp1): ?s a ?type . <br>OPTIONAL {<br>(tp2): ?type rdfs:label ?label }<br> &nbsp;&nbsp;&nbsp;FILTER LANGMATCHES<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(LANG(?label), "EN") <br>}LIMIT 100  |  BGP_1{  <br> (tp1_equiv):  ?s1     rdf:type    ?o1 . <br>(tp2):    ?s1    rdfs:label     ?o2 <br>}  | 1 | 1 |


**Note A**: Q28 needs two datasets to be answered, DBpedia and VIAF.<br>
**Note B**: With (tpi'_j) we annotate the "jth" false postive for the triple pattern "i" of the original query.

## Most frequent deduced bgps for usewod 2016

We analyzed the log of the DBpedia’s LDF server available in the [USEWOD 2016](http://usewod.org/data-sets.html). This log contains http requests from October 2014 to November 2015. We analyzed the first quarter of the log representing 4,720,874 single triple pattern queries (until 27th February 2015). We cleaned 1% of the log with entries that do not corresponds to TPF requests. To obtain corresponding answers, we re-executed the log using the TPF client. Then, we run LIFT with log slices of one hour with a maximum gap (one hour).

Next table shows the most frequent deduced BGPs by LIFT, for the period of 14/10/2014-27/02/2015. 

| Frequency | Deduced BGP                                                |
| ----------|:----------------------------------------------------------|
| 125       | ?s1 rdfs:label "Brad Pitt"@en . <br>?s2 dbpedia-owl:starring ?s1 . <br>?s2 rdfs:label ?o3 . <br>?s2 dbpedia-owl:director ?o3 . <br>?o3 rdfs:label ?o5 |
| 45        | ?s1 rdfs:label "Brad Pitt"@en . <br>?s2 dbppropstarring ?s1 . <br>?s2 rdfs:label ?o3 . <br>?s2 dbpedia-owl:director ?o3 . <br>?o3 rdfs:label ?o5 |
| 43        | ?s1 rdfs:label "York"@en . <br>?s2 dbpedia-owl:birthPlace ?s1 . <br>?s2 rdf:type dbpedia-owl:Artist |
| 33        | ?s1 dbppropcityServed dbpedia:Italy. <br>?s2 rdf:type dbpedia-owl:Airport |
| 31        | ?s1 dbpedia-owl:influencedBy dbpedia:Pablo_Picasso . <br>?s1 rdf:type dbpedia-owl:Artist . <br>?s1 dbpedia-owl:birthDate ?o3 |
| 20        | dbpedia-owl:Agent rdfs:subClassOf ?o1 . <br>?o1 rdfs:subClassOf ?o2 |
| 17        | dbpedia-owl:Activity rdfs:subClassOf ?o1 . <br>?o1 rdfs:subClassOf ?o2 |
| 16        | ?s1 rdfs:label "Trinity College, Dublin"@en . <br>?s2 dbpedia-owl:almaMater ?s1 . <br>?s2 rdf:type dbpedia-owl:Writer |
| 14        |  ?s1 rdf:type dbpedia-owl:Book . <br>?s1 dbpedia-owl:author ?o2 |
| 12        |  ?s1 rdf:type dbpclass:PeopleExecutedByCrucifixion . <br>?s1 rdf:type dbpclass:Carpenters |
| 11        |  ?s1 dbpedia-owl:ingredient ?o1 . <br>?s1 dbpedia-owl:kingdom  dbpedia:Plant |
| 11        |  ?s1 dbpedia-owl:birthPlace dbpedia:Úrbel_del_Castillo . <br>?s1 dbpedia-owl:team ?o2 |
| 10        |  ?s1 rdf:type foaf:Person . <br>?s1 foaf:isPrimaryTopicOf ?o2 |
| 10        |  ?s1 dbpedia-owl:type dbpedia:Dessert . <br>?s1 dbpedia-owl:ingredient ?o1 . <br>?o1 dbpedia-owl:kingdom dbpedia:Plant |
| 8         |  dbpedia:Raspberry_Pi dbpedia-owl:operatingSystem ?o1 .  <br>?s1 dbpedia-owl:operatingSystem ?o1 . <br>?s1 rdf:type dbpedia-owl:Device |

**Note**: When two different triple patterns have the same variable name, this represents a nested loop implementatation, where values from the former are pushed in the latter. 

## Recall and precision plots

In the next Figure, we view recall and precision of joins per query executed in isolation, for BGPs presented in the [previous section](https://github.com/coumbaya/lift/blob/master/experiments.md#deduced-bgps-per-executed-query-of-tpf-servers).

![isolation_values](https://github.com/coumbaya/lift/blob/master/plots/isolationRecallPrecision.PNG?raw=true "isolation_values")


We implemented a tool to shuffle several TPF logs according to different parameters, available [here](https://github.com/coumbaya/traceMixer). Thus, we shufle each collection of query execution traces and evaluate LIFT deduction in terms of recall and precision for different gap values. gap varies from 1% to 100% of the log duration. Each query collection, presented in the [next section](https://github.com/coumbaya/lift/blob/master/experiments.md#concurently-executed-query-sets), was shuffled 4 times and we calculate the average of results by gap. 

In the next Figure, we view precision of joins per collection of queries executed in concurence, concerning DBpedia, VIAF, LOV and Ughent collections.

![dbpedia_precision](https://github.com/coumbaya/lift/blob/master/plots/concurrent_precision_dbpedia_collections.PNG?raw=true "dbpedia_precision")
![other_precision](https://github.com/coumbaya/lift/blob/master/plots/concurrent_precision_viaf_lov_ughent_collections.PNG?raw=true "other_precision")

In the next Figure, we view recall of joins per collection of queries executed in concurence, concerning DBpedia, VIAF, LOV and Ughent collections.

![dbpedia_recall](https://github.com/coumbaya/lift/blob/master/plots/concurrent_recall_dbpedia_collections.PNG?raw=true "dbpedia_recall")
![other_recall](https://github.com/coumbaya/lift/blob/master/plots/concurrent_precision_viaf_lov_ughent_collections.PNG?raw=true "other_recall")

## Appendix information

In this section, we illustrate which queries are executable per dataset, and also present a table matching each IRI's authority to its corresponding prefix.

### Executed queries per dataset

In the table below, we see which queries correspond to every dataset, representing a distinct data provider/LDF server entity.


| Dataset                                         | Queries               |
| ------------------------------------------------|:---------------------:|
| DBpedia 2015-04                                 | Q1, Q3, Q4, Q5, Q6, <br> Q7, Q8, Q9, Q10, Q11, <br> Q12, Q13, Q14, Q15, Q16, <br> Q19, Q20, Q21, Q22, Q24, <br> Q27, Q28, Q29, Q30 |
| Ghent University Academic bibliography (Ughent) | Q2, Q23, Q25, Q29, Q30 |
| Linked Open Vocabulairies (LOV)                 | Q17, Q18, Q26          |
| Virtual International Authority File (VIAF)     | Q28, Q29, Q30          |


### Concurently executed query sets

In the table below, we see the collection of queries, executed concurrently over a single LDF server.


| Collection        | Queries                 |
| ------------------|:-----------------------:|
| DBpedia 1 (DB1)   | Q1, Q8, Q14, Q22        |
| DBpedia 2 (DB2)   | Q3, Q11, Q15, Q20       |
| DBpedia 3 (DB3)   | Q6, Q13, Q19, Q27       |
| DBpedia 4 (DB4)   | Q4, Q12, Q24            |
| DBpedia 5 (DB5)   | Q5, Q7, Q16, Q21        |
| DBpedia 6 (DB6)   | Q9, Q10, Q29, Q30       |
| Ughent 1 (UG1)    | Q17, Q18, Q26, Q29, Q30 |
| LOV 1 (LV1)       | Q2, Q23, Q25, Q29, Q30  |
| VIAF 1 (VF1)      | Q9, Q10, Q29, 

### IRI prefixes to authorities

Finally, we match each authority to a prefix for the corresponding datasets and their queries, as presented above.

| Prefix         | Authority                                                             |
| ---------------|:---------------------------------------------------------------------:|
| geo            | http://www.w3.org/2003/01/geo/wgs84_pos# |
| rdfs           | http://www.w3.org/2000/01/rdf-schema# |
| rdf            | http://www.w3.org/1999/02/22-rdf-syntax-ns# |
| owl            | http://www.w3.org/2002/07/owl# |
| skos           | http://www.w3.org/2004/02/skos/core# |
| xsd            | http://www.w3.org/2001/XMLSchema# |
| hydra          | http://www.w3.org/ns/hydra/core# |
| dc             | http://purl.org/dc/terms/ |
| dc11           | http://purl.org/dc/elements/1.1/ |
| dctitle        | http://purl.org/dc/terms/title/ |
| vann           | http://purl.org/vocab/vann/ |
| foaf           | http://xmlns.com/foaf/0.1/ |
| void           | http://rdfs.org/ns/void# |
| schema         | http://schema.org/sameAs/ |
| dbpedia        | http://dbpedia.org/resource/ |
| dbpedia-owl    | http://dbpedia.org/ontology/ |
| dbpprop        | http://dbpedia.org/property/ |
| dbpclass       | http://dbpedia.org/class/yago/ |
| dbpedia-cat    | http://dbpedia.org/resource/Category/ |
| ugent          | http://lib.ugent.be/classification/classification/ |
| ugent-biblio   | http://data.linkeddatafragments.org/.well-known/genid/ugent-biblio/ |
