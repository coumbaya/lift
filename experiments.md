# LIFT: Experiments with menu queries & real log traces

In this page, we present LIFT's deduced BGPs using as input (1) traces of queries in http://client.linkeddatafragments.org, each executed one by one, and, (2) traces of DBpedia LDF server's real log, from Usewod logs in [here](http://usewod.org/data-sets.html), during the period of 14th October 2014-27th February 2015.

**Summary**


1. [**Deduced BGPs per query**](https://github.com/coumbaya/lift/blob/master/experiments.md#deduced-bgps-per-query)

2.  [**Top 15 most frequent deduced bgps**](https://github.com/coumbaya/lift/blob/master/experiments.md#top-15-most-frequent-deduced-bgps)

3.  [**Recall and precision plots**](https://github.com/coumbaya/lift/blob/master/experiments.md#recall-and-precision-plots)

4.   [**Appendix information**](https://github.com/coumbaya/lift/blob/master/experiments.md#appendix-information)
   * [Executed queries per dataset](https://github.com/coumbaya/lift/blob/master/experiments.md#executed-queries-per-dataset)
   * [IRI prefixes to authorities](https://github.com/coumbaya/lift/blob/master/experiments.md#iri-prefixes-to-authorities)


## Deduced bgps per query

Next, we present deduced BGPs of LIFT as well as precision/recall of joins per query, for queries presented in http://client.linkeddatafragments.org/, each executed in isolation.

|ID | Query                        | Deduced BGPs                       |Recall| Precision |
|---|:----------------------------:|:----------------------------------:|------|:---------:|
|Q1 | SELECT ?movie ?title ?name WHERE { <br>   ?movie dbpedia-owl:starring ?actor . (tp1)<br> ?actor rdfs:label "Brad Pitt"@en . (tp2)<br>?movie rdfs:label ?title . (tp3)<br>?movie dbpedia-owl:director  director . (tp4)<br>?director rdfs:label ?name . (tp5)<br>   FILTER LANGMATCHES(LANG(?title), "EN")<br>   FILTER LANGMATCHES(LANG(?name), "EN") }  | BGP_1 { <br> (tp2):    ?s1    rdfs:label    "Brad Pitt"@en  <br>(tp1):  ?s2   dbpedia:starring   ?s1 <br>(tp3):    ?s1     rdfs:label  ?o3 <br>(tp4):  ?s1   dbpedia-owl:director   ?o4 <br>(tp5):    ?o4    rdfs:label     ?o5 } |    1      |  1    |
|Q2 | SELECT ?title ?classification WHERE { ?author foaf:name "Anne De Paepe" . (tp1) <br>publication dc:creator ?author . (tp2) <br>?publication dc:title ?title . (tp3)<br>?publication ugent-biblio:classification  ?classification .   (tp4) } | BGP_1{<br>  (tp1):   ?s1     foaf:name     "Anne De Paepe"   <br>(tp2):   ?s2     dcterms:creator  ?s1  <br>(tp4):   ?s2     ugent:classification    ?o3 <br>(tp3):   ?s2      dctitle:title     ?o4  } | 1   |  1  |
|Q3 | SELECT DISTINCT ?entity WHERE {<br>?entity a dbpedia-owl:Airport . (tp1)<br>?entity dbpprop:cityServed dbpedia:Italy  . (tp2)  }  | BGP_1{<br>  (tp2):   ?s1     dbpprop:cityServed      dbpedia:Italy  <br> (tp1_equiv):  ?s1    rdf:type   dbpedia-owl:Airport }  | 1 | 1 |
|Q4 | ?city  rdf:type dbpclass:AncientCities . (tp1)<br>  ?city dbpedia-owl:populationTotal ?popTotal . (tp2)<br>   OPTIONAL {  ?city dbpedia-owl:populationMetro ?popMetro . (tp3) } <br>FILTER (?popTotal > 50000) } <br>  ORDER BY DESC(?popTotal)   | BGP_1{<br>(tp1):   ?s1      rdf:type      dbpclass:AncientCities  <br>(tp2):   ?s1     dbpedia-owl:populationTotal      ?o2 <br>(tp3):   ?s1     dbpedia-owl:populationMetro     ?o3 } | 1  | 1 |
|Q5 | SELECT ?name ?deathDate WHERE {<br>?person a dbpedia-owl:Artist . (tp1)  ?person   rdfs:label ?name . (tp2)<br>?person dbpedia-owl:birthPlace  ?place . (tp3)<br>?place rdfs:label "York"@en . (tp4)<br>   FILTER LANGMATCHES(LANG(?name),  "EN")<br>   OPTIONAL {<br>?person dbpprop:dateOfDeath ?deathDate.  (tp5)  }  } | BGP_1{<br> (tp4):  ?s1     rdf:label      "York"@en  <br>(tp3):  ?s2    dbpedia-owl:birthPlace   ?s1  <br>(tp1):  ?s2   rdf:type  dbpedia-owl:Artist  <br>(tp2):  ?s2  rdf:label     ?o4  <br>(tp5):  ?s2   dbpprop:dateOfDeath      ?o5  } | 1 | 1 |
|Q6 | CONSTRUCT  {<br>?artist a dbpedia-owl:Artist.  ?artist dbpedia-owl:birthDate ?date.  }    WHERE {<br>?artist dbpedia-owl:influencedBy dbpedia:Pablo_Picasso . (tp1)<br>?artist a dbpedia-owl:Artist . (tp2)<br>?artist dbpedia-owl:birthDate ?date . (tp3)   } | BGP_1{<br> (tp1):    ?s1  dbpedia-owl:influencedBy      dbpedia:Pablo_Picasso  <br>(tp2_equiv):   ?s1    rdf:type      dbpedia-owl:Artist  <br>(tp3):    ?s1    dbpedia-owl:birthDate       ?o3  }  | 1 | 1 |
|Q7 | SELECT DISTINCT ?book ?author WHERE {<br>?book rdf:type dbpedia-owl:Book . (tp1)<br>?book dbpedia-owl:author ?author. (tp2)  }<br> LIMIT 100 |  BGP_1{  (tp1):   ?s1     rdf:type     dbpedia-owl:Book   (tp2):   ?s1   dbpedia-owl:author     ?o2   } <br> BGP_2{<br> (tp2'_a):   ?s1   dbpedia-owl:author   ?o1  <br>(tp2'_b):   ?s1    dbpedia-owl:author     ?o2  }  |  1 | 0,5 |
|Q8 | SELECT ?award WHERE {<br>?award a dbpedia-owl:Award . (tp1)<br>?award dbpprop:country ?language . (tp2)<br>?language dbpedia-owl:language dbpedia:Dutch_language . (tp3)  } | BGP_1{ <br>(tp3):   ?s1    dbpedia-owl:language     dbpedia:Dutch_language  <br>(tp2):   ?s2    dbpprop:country    ?injectsub(tp3) <br> (tp1'_a):   ?injectsub(tp3)     rdf:type      dbpedia-owl:Award <br> (tp1_equiv):  ?s2       rdf:type     dbpedia-owl:Award  }  |  1 |  0,67  |
|Q9 | SELECT DISTINCT ?artist ?band WHERE {<br>{dbpedia:Queen_(band) dbpedia-owl:bandMember ?artist . (tp1) }<br>UNION<br>{  dbpedia:Queen_(band)   dbpedia-owl:formerBandMember ?artist . (tp2)  }<br>?artist dbpedia-owl:associatedBand ?band . (tp3) } |  BGP_1{<br>(tp1):   dbpedia:Queen_(band)    dbpedia-owl:bandMember  ?o1  <br>(tp3):   ?o1       dbpedia-owl:associatedBand        ?o2   }   BGP_2{<br>(tp2):  dbpedia:Queen_(band)   dbpedia-owl:bandMember  ?o1 <br>(tp3):  ?o1     dbpedia-owl:formerBandMember   ?o2  }  |   1  |  1 |
|Q10| SELECT DISTINCT ?performer ?name WHERE {<br>?work dbpedia-owl:writer dbpedia:Michael_Jackson . (tp1)<br>?work dbpedia-owl:musicalArtist ?performer . (tp2)<br>  OPTIONAL  {  ?performer rdfs:label ?name.  (tp3)<br>   FILTER LANGMATCHES(LANG(?name), "EN")  }  }   |  BGP_1{<br> (tp1):     ?s1   dbpedia-owl:writer    dbpedia:Michael_Jackson <br>(tp2):     ?s1  dbpedia-owl:musicalArtist     ?o2  <br>(tp3'_a):    ?s1    rdfs:label       ?o3  <br>(tp3):     ?o2    rdfs:label       ?o4  }  BGP_2{<br>(tp2'_a):     ?s1 dbpedia-owl:musicalArtist     ?o1 <br>(tp3'_b):    ?s1   rdfs:label       ?o2  }   |  1 | 1 |
|Q11| SELECT ?software ?company WHERE {<br>?software dbpedia-owl:developer ?company . (tp1) ?company dbpedia-owl:locationCountry ?country . (tp2)<br>?country rdfs:label "Belgium"@en .  (tp3) }   | BGP_1{<br>(tp3):    ?s1    rdfs:label     "Belgium"@en <br>(tp2):    ?s2    dbpedia-owl:locationCountry    ?s1 <br>(tp1):    ?s3    dbpedia-owl:developer    ?s2 }   |  1 | 1 |
|Q12| SELECT ?person WHERE {<br>?person a yago:Carpenters . (tp1)<br>?person a yago:PeopleExecutedByCrucifixion . (tp2)  } |  BGP_1{<br> (tp2_equiv):    ?s1  rdf:type     dbpclass:PeopleExecutedByCrucifixion  <br>(tp1_equiv):   ?s1  rdf:type   dbpclass:Carpenters  }<br>   BGP_2{<br>  (tp2'_a):    ?s1   rdf:type     dbpclass:Carpenters <br> (tp1'_a):   ?s1    rdf:type   dbpclass:Carpenters }    | 1 | 0,5 |
|Q13| SELECT ?actor ?cause WHERE {<br>?actor dbpedia-owl:deathCause ?cause . (tp1)<br>?actor dc:subject dbpedia-cat:American_male_film_actors  (tp2) } |  BGP_1{  (tp2_equiv):    ?s1  rdf:type     dbpclass:PeopleExecutedByCrucifixion  (tp1_equiv):   ?s1  rdf:type   dbpclass:Carpenters     }    BGP_2{ <br (tp2'_a):    ?s1   rdf:type     dbpclass:Carpenters <br>(tp1'_a):   ?s1    rdf:type   dbpclass:Carpenters    }   | 1 | 0,5 |
|Q14| SELECT ?dessert ?fruit WHERE {<br>?dessert dbpedia-owl:type dbpedia:Dessert. (tp1)<br>?dessert dbpedia-owl:ingredient ?fruit . (tp2)<br>?fruit dbpedia-owl:kingdom dbpedia:Plant . (tp3) } |  BGP_1{<br>(tp1):    ?s1     dbpedia-owl:type      dbpedia:Dessert <br>(tp2):    ?s1     dbpedia-owl:ingredient    ?o2  <br>(tp3):  ?o2   dbpedia-owl:kingdom     dbpedia:Plant  }  BGP_2{<br> (tp1'_a):  ?s1     dbpedia-owl:ingredient    ?o1 <br>(tp2'_a):  ?s1   dbpedia-owl:ingredient    ?o2 <br> (tp3'_a):  ?s1 dbpedia-owl:kingdom     dbpedia:Plant <br>(tp3'_b):  ?o2   dbpedia-owl:kingdom     dbpedia:Plant }   | 1 | 0,4 |
|Q15| SELECT DISTINCT ?device WHERE {<br>dbpedia:Raspberry_Pi dbpprop:os ?operatingSystem . (tp1)<br>?device a dbpedia-owl:Device . (tp2)<br>?device  dbpprop:os ?operatingSystem .  (tp3)<br>   FILTER (!(?device = dbpedia:Raspberry_Pi))  } |  BGP_1{ <br>  (tp1):   dbpedia:Raspberry_Pi     dbpprop:os      ?o1 <br> (tp3):   ?s2      dbpprop:os      ?o1 <br> (tp2_equiv):  ?s2     rdf:type      dbpedia-owl:Device  }   | 1 | 1 |
|Q16| SELECT DISTINCT ?entity ?event WHERE {<br>?entity a dbpedia-owl:Event . (tp1)<br>?entity  rdfs:label ?event . (tp2)<br>?entity ?predicate dbpedia.org:Trentino . (tp3)<br>FILTER(langMatches(lang(?event), "EN"))  } | BGP_1{ <br> (tp3):    ?s1    ?p1     dbpedia:Trentino  <br> (tp1_equiv):   ?s1     rdf:type         dbpedia-owl:Event  <br> (tp2):     ?s1    rdfs:label      ?o3  } |  1  |  1  |
|Q17| SELECT ?s WHERE {<br>?s a rdfs:Class . (tp1)<br>?s rdfs:subClassOf dcat:Dataset . (tp2)  }  | BGP_1{<br>  (tp2):    ?s1   rdfs: subClassOf     dcat:Dataset <br>(tp1_equiv):   ?s1      rdf:type    rdfs:Class  }  | 1 | 1 |
|Q18| SELECT distinct ?ontology ?prefix WHERE {<br>?ontology a owl:Ontology . (tp1)<br>?ontology vann:preferredNamespacePrefix ?prefix . (tp2)<br>?ontology  dc:creator ?creator . (tp3)<br>?creator foaf:name "Pieter Colpaert" . (tp4)  }<br>LIMIT 100  |  BGP_1{ <br> (tp4):    ?s1   foaf:name     "Pieter Colpaert" <br> (tp3):    ?s2   dc:creator     ?s1  <br> (tp1_equiv):   ?s2  rdf:type     owl:Ontology  <br> (tp2):   ?s2    vann:preferredNamespacePrefix     ?o4  }  | 1 | 1 |
|Q19| SELECT ?titleEng ?title WHERE {<br>?movie dbpprop:starring ?actor . (tp1)<br>?actor rdfs:label "Natalie Portman"@en  . (tp2)<br>?movie rdfs:label ?titleEng . (tp3)<br>?movie rdfs:label ?title . (tp4)<br>   FILTER LANGMATCHES(LANG(?titleEng), "EN")<br>   FILTER (!LANGMATCHES(LANG(?title), "EN")) }  | BGP_1{<br> (tp2):    ?s1     rdfs:label   "Natalie Portman"@en <br> (tp1):    ?s2    dbpedia:starring  ?s1 <br> (tp3 MERGED tp4):     ?s2  rdfs:label  ?o3   }  | 0,33 | 0,5 |
|Q20| SELECT ?indDish ?belDish ?ingredient  WHERE {<br>?indDish a dbpedia-owl:Food . (tp1)  ?indDish dbpedia-owl:origin dbpedia:India . (tp2)<br>?indDish dbpedia-owl:ingredient ?ingredient . (tp3)<br>?belDish a dbpedia-owl:Food . (tp4)<br>?belDish dbpedia-owl:origin dbpedia:Belgium . (tp5)<br>?belDish dbpedia-owl:ingredient ?ingredient . (tp6)  }  | BGP_1{<br>  (tp1_or_4):  ?s1     rdf:type  dbpedia-owl:Food  (tp1_or_4'_a): ?s1   rdf:type  dbpedia-owl:Food   (tp2):   ?s1   dbpedia-owl:origin   dbpedia:India  (tp2'_a):   ?s4  dbpedia-owl:origin     dbpedia:India  (tp1_or_4'_b):   ?s4    rdf:type  dbpedia-owl:Food <br> (tp2'_b):  ?s5   dbpedia-owl:origin dbpedia:India <br> (tp3):  ?s1  dbpedia-owl:ingredient  ?o <br> (tp1_or_4'_c):  ?s5   rdf:type  dbpedia-owl:Food <br> (tp3_or_6'_a): ?s6    dbpedia-owl:ingredient    INJECTEDobj(tp3) <br> (tp5'_a):  INJECTED(tp3)   dbpedia-owl:origin  dbpedia:Belgium <br> (tp6):  INJECTEDsubj(tp1_or_4) dbpedia-owl:ingredient  INJECTEDobj(tp3) <br>  (tp3_or_6'_b): INJECTEDsubj(tp2'_a)  dbpedia-owl:ingredient INJECTEDobj(tp3) <br> (tp5):  INJECTEDsubj (tp2)  dbpedia-owl:ingredient  dbpedia:Belgium <br> (tp3_or_6_c): INJECTEDsubj (tp3_or_6'_a)  dbpedia-owl:ingredient  ?o <br> (tp2_f):  INJECTEDsubj(tp3_or_6'_a)  dbpedia-owl:origin  dbpedia:Belgium <br> (tp3_or_6_c):  INJECTEDsubj(tp3_or_6'_a):  dbpedia-owl:ingredient   INJECTEDobj(tp5):  } BGP_2{<br>   (tp3_or_6_d): ?s  dbpedia-owl:ingredient     ?o  <br> (tp1_or_4_d):  INJECTEDsubj(tp3_or_6'_a)  rdf:type  dbpedia-owl:Food <br> (tp3_or_6_e): ?s  dbpedia-owl:ingredient INJECTED(tp3_or_6'_a) <br> (tp2'_c): INJECTEDsubj(tp1_or_4_d)  dbpedia-owl:origin     dbpedia:India <br> (tp1_or_4_d): INJECTEDsubj (tp3_or_6_d)    rdf:type dbpedia-owl:Food ?o  } |  1 | 0,24 |
|Q21| SELECT DISTINCT ?person WHERE {<br>dbpedia:Jesus dc:subject ?common . (tp1)<br>?person a foaf:Person . (tp2)<br>?person dc:subject ?common. (tp3)  }<br>   LIMIT 1000  | BGP_1{ <br> (tp1):    dbpedia :Jesus    dc:subject     ?o1 <br> (tp3):    ?s2    dc:subject     ?injectobj(tp1) <br> (tp2_equiv):   ?s2      rdf:type      foaf:Person   }   BGP_2{ <br> (tp2'_a):   ?s1  rdf:type      foaf:Person  <br> (tp2'_b):   ?s1     rdf:type      foaf:Person   } | 1 | 0,67 |
|Q22| SELECT ?place ?relation WHERE {<br>?place rdf:type dbpedia-owl:Settlement . (tp1)<br>?place ?relation dbpedia:Barack_Obama . (tp2)  } | BGP_1{ <br> (tp2):    ?s1   ?p1  dbpedia :Barack_Obam  <br> (tp1):     ?s1    rdf:type       dbpedia-owl:Settleme   } | 1 | 1 |
|Q23| SELECT ?jansen ?janssen ?title {<br>?publication dc:creator ?creator . (tp1)<br>?creator foaf:givenname ?jansen . (tp2)<br>?creator foaf:surname "Jansen" . (tp3)<br>?publication dc:creator ?name . (tp4) ?name foaf:givenname ?janssen . (tp5)<br>?name foaf:surname "Janssen" . (tp6)<br>?publication dc:title ?title . (tp7) }  |  BGP_1{ <br> (tp3):   ?s1    foaf:surname     Jansen <br> (tp2):    ?s1    foaf:givenname    ?o2 <br> (tp1):    ?s3   dc:creator  <br>  ?s1 (tp6'_a):   ?s1     foaf:surname      "Janssen" <br> (tp7):    ?s3   dc:title      ?o5 <br> (tp4):    ?s3      dc:creator    ?o6 <br> (tp5):    ?o6     foaf:givenname     ?o7 <br> (tp4'_a):    ?s3     dc:creator    ?o8 <br> (tp6):    ?o6    foaf:surname     "Janssen" } | 1 | 0,75 |
|Q24| SELECT ?clubName ?playerName WHERE {<br>?club a dbpedia-owl:SoccerClub . (tp1)<br>?club  dbpedia-owl:ground ?city . (tp2)<br>?club  rdfs:label ?clubName . (tp3)<br>?player dbpedia-owl:team ?club . (tp4)<br>?player dbpedia-owl:birthPlace ?city . (tp5)<br>?player rdfs:label ?playerName . (tp6)<br>?city dbpedia-owl:country dbpedia:Spain. (tp7)<br>   FILTER LANGMATCHES(LANG(?clubName), "EN")<br>   FILTER LANGMATCHES(LANG(?playerName), "EN") } |  BGP_1{ <br> (tp7): ?s1     dbpedia-owl:country     dbpedia:Spain. <br> (tp2): ?s2     dbpedia-owl:ground     ?s1  <br> (tp5): ?s3     dbpedia-owl:birthPlace  ?s1 <br> (tp1): ?s2   rdf:type   dbpedia-owl:SoccerClub <br> (tp3): ?s2  rdf:label     ?o5   <br> (tp4'_a):  ?s6     dbpedia-owl:team    ?s2 <br> (tp6):  ?s3     rdf:label     ?o7 <br>  (tp4): ?s3   dbpedia-owl:team   ?s2  <br> (tp4'_b): ?s6     dbpedia-owl:team   ?s2 <br>  (tp5'_a): ?s3     dbpedia-owl:birthPlace  ?s2  }<br>  BGP_2{ <br> (tp5'_b):  ?s1     dbpedia-owl:birthPlace     ?o1 <br> (tp7'_a):   ?s2    dbpedia-owl:country     INJECTEDsubj(tp5'_b) <br>  (tp4'_c):  ?s     dbpedia-owl:team     INJECTEDsubj(tp7'_a) <br>  (tp6'_a):   INJECTEDsubj(tp4'_c)    rdf:label     ?o <br> (tp2'_a):   ?s     dbpedia-owl:ground     INJECTEDsubj(tp7'_a)  <br> (tp5'_b):  ?s     dbpedia-owl:birthPlace      INJECTEDsubj(tp7'_a) <br>  (tp5'_c): INJECTEDsubj(tp4'_c) dbpedia-owl:birthPlace INJECTEDsubj(tp7'_a)  }  | 1 | 0,32 |
|Q25| SELECT DISTINCT ?coauthorName {<br>?publication1 dc:creator ?coauthor . (tp1)<br>?coauthor foaf:name "Etienne Vermeersch". (tp2)<br>?coauthor foaf:name ?coauthorName . (tp3)<br>XOR {<br>?publication2 dc:creator ?coauthor .  (tp4)<br>?coauthor   foaf:name "Freddy Mortier". (tp5)<br>?coauthor foaf:name ?coauthorName. (tp6) } } | BGP_1{ <br> (tp2): Deduced LDF_2: ?s     foaf:name     "Etienne Vermeersch"  <br> (tp1):   LDF_3: ?s     dc:creator    INJECTEDsubj(tp2) <br> (tp3):  LDF_4: INJECTEDsubj(tp2)     foaf:name     ?o   } <br> BGP_2{  <br> (tp6): ?s     foaf:name   "Freddy Mortier"  <br> (tp4):  LDF_7: ?s     dc:creator     INJECTEDsubj(tp6) <br>  (tp4'_a): LDF_8: INJECTEDsubj(tp4)    dc:creator     ?o  <br>  (tp4'_b): INJECTEDsubj(tp4)    dc:creator  INJECTEDsubj(LDF_6)  <br> (tp4'_c): LDF_10: ?s     dc:creator     INJECTEDobj(tp4'_a)  <br> (tp5): LDF_11: INJECTEDobj(tp4)    foaf:name    ?o <br>  (tp4'_d): LDF_12: INJECTEDsubj (tp4)    dc:creator     INJECTEDobj(tp4'_a)   }  | 1 | 0,44 |
|Q26| SELECT distinct ?license  WHERE {<br>?ontology a owl:Ontology . (tp1)<br>?ontology  dc:license ?license. (tp2) } | BGP_1{ <br> (tp2):    ?s     dc:license     ?o  <br> (tp1_equiv):  ?injectsubj(tp2)     rdf:type    owl: Ontology  } <br>  BGP_2{<br> (tp2):    ?s      rdf:type     ?o <br> (tp1'_a):    ?injectsubj(tp2)     rdf:type    owl: Ontology  }   | 1 | 0,5 |
|Q27| SELECT ?entity ?label ?comment  WHERE {<br>?entity a dbpedia-owl:MythologicalFigure . (tp1)<br>?entity  rdfs:label ?label . (tp2)<br>?entity dc:subject  dbpedia-cat:Women_in_Greek_mythology . (tp3)<br>?entity rdfs:comment ?comment . (tp4)<br>   FILTER(langMatches(lang(?label), "EN"))<br>   FILTER(langMatches(lang(?comment), "EN")) }  |  BGP_1{<br> (tp3):    ?s   dc:subject    dbpedia-cat::Women_in_Greek_mythology <br> (tp1_a):    ?injectsubj(tp3)  rdf:type   dbpedia-owl:MythologicalFigure <br> (tp4):     ?injectsubj(tp3)      rdfs:comment     ?o <br> (tp2):     ?injectsubj(tp3)       rdfs:label      ?o } <br> BGP_2{ <br>  (tp3'_a):    ?s   dc:subject    dbpedia-owl:MythologicalFigure  <br> (tp1'_a):    ?injectsubj(tp3'_a)    dc:subject  dbpedia-owl:MythologicalFigure  <br> (tp4'_a):     ?injectsubj(tp3'_a)      rdfs:comment     ?o <br> (tp2'_a):     ?injectsubj(tp3'_a)       rdfs:label      ?o }   | 1 | 0,5 |
|Q28| SELECT ?name ?work ?title WHERE {<br>?artist dbpedia-owl:movement ?movement . (tp1)<br>?movement rdfs:label "Cubism"@en . (tp2)<br>?artist foaf:name ?name . (tp3)<br>?work schema:author ?author . (tp4)<br>?author schema:sameAs ?artist . (tp5)<br>?work schema:name ?title . (tp6)<br>   FILTER (!REGEX(?name, ",")) } |  |  |   |
|Q29| SELECT DISTINCT ?subject ?label WHERE {<br>{ ?subject dc:title ?label  (tp1) }<br>UNION<br>{ ?subject rdfs:label ?label  (tp2) } }<br>LIMIT 100  |  BGP_1{ <br> (tp1):  ?s     dc:itle     ?o }<br>  BGP_2{<br>  (tp2):   ?s    rdfs:label        ?o  }   | 0/0 | 0/0 |
|Q30| SELECT DISTINCT ?type ?label WHERE {<br>?s a ?type . (tp1)<br>OPTIONAL { ?type rdfs:label ?label . (tp2)}<br>   FILTER LANGMATCHES(LANG(?label), "EN")  }<br>LIMIT 100  |  BGP_1{  <br> (tp1_equiv):  ?s1     rdf:type    ?o1 <br>(tp2):    ?s1    rdfs:label     ?o2 }  | 1 | 1 |


## Top 15 most frequent deduced bgps

In this page, we present LIFT's deduced BGPs using as input traces of the real log of DBpedia, as regenerated with its answers.

| Frequency | Deduced BGP                                                |
| ----------|:----------------------------------------------------------:|
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
| 11        |  ?s1 dbpedia-owl:ingredient ?o1 . <br>?s1 dbpedia-owl:kingdom  http://dbpedia.org/ressource/Plant |
| 11        |  ?s1 dbpedia-owl:birthPlace dbpedia:Úrbel_del_Castillo . <br>?s1 dbpedia-owl:team ?o2 |
| 10        |  ?s1 rdf:type foaf:Person . <br>?s1 foaf:isPrimaryTopicOf ?o2 |
| 10        |  ?s1 dbpedia-owl:type dbpedia:Dessert . <br>?s1 dbpedia-owl:ingredient ?o1 . <br>?o1 dbpedia-owl:kingdom dbpedia:dbpePlant |
| 8         |  dbpedia:Raspberry_Pi dbpedia-owl:operatingSystem ?o1 .  <br>?s1 dbpedia-owl:operatingSystem ?o1 . <br>?s1 rdf:type dbpedia-owl:Device |


## Recall and precision plots

In the next Figure, we view recall and precision of joins, per query executed in isolation.

![GitHub Logo](https://github.com/coumbaya/lift/blob/master/plots/isolationRecallPrecision.PNG)

In the next two Figures, we view precision and recall of joins per collection of queries executed in concurence, concerning DBpedia, VIAF, LOV and Ughent collections.

![GitHub Logo](https://github.com/coumbaya/lift/tree/master/plots/concurrent_precision_dbpedia_collections.PNG)
![GitHub Logo](https://github.com/coumbaya/lift/tree/master/plots/concurrent_precision_viaf_lov_ughent_collections.PNG)
![GitHub Logo](https://github.com/coumbaya/lift/tree/master/plots/concurrent_recall_dbpedia_collections.PNG)
![GitHub Logo](https://github.com/coumbaya/lift/tree/master/plots/concurrent_precision_viaf_lov_ughent_collections.PNG)


## Appendix information

In this section, we illustrate which queries are executable per dataset, and also present a table matching each IRI's authority to its corresponding prefix.

### Executed queries per dataset

In the table below, we see which queries correspond to every dataset, representing a distinct data provider/LDF server entity.


| Dataset                                         | Queries               |
| ------------------------------------------------|:---------------------:|
| DBpedia 2015-04                                 | Q1, Q3, Q4, Q5, Q6, Q7, Q8, Q9, Q10, Q11, Q12, Q13, Q14, Q15, Q16, Q19, Q20, Q21, Q22, Q24, Q27, Q28, Q29, Q30 |
| Ghent University Academic bibliography (Ughent) | Q2, Q23, Q25, Q29, Q30 |
| Linked Open Vocabulairies (LOV)                 | Q17, Q18, Q26          |
| Virtual International Authority File (VIAF)     | Q28, Q29, Q30          |


### Concurently executed queries

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
| VIAF 1 (VF1)      | Q9, Q10, Q29, Q30       |


**Note**: Q28 needs two datasets to be answered, DBpedia and VIAF.

### iri prefixes to authorities

Finally, we match each authority to a prefix for the corresponding datasets, as presented above.

| Prefix         | Authority                                                             |
| ---------------|:---------------------------------------------------------------------:|
| geo            | http://www.w3.org/2003/01/geo/wgs84_pos# |
| rdfs           | http://www.w3.org/2000/01/rdf-schema# |
| rdf            | http://www.w3.org/1999/02/22-rdf-syntax-ns#: |
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
| dbpedia        | http://dbpedia.org/resource/: |
| dbpedia-owl    | http://dbpedia.org/ontology/ |
| dbpprop        | http://dbpedia.org/property/ |
| dbpclass       | http://dbpedia.org/class/yago/ |
| dbpedia-cat    | dbpedia:Category/ |
| ugent          | http://lib.ugent.be/classification/classification/ |
| ugent-biblio   | http://data.linkeddatafragments.org/.well-known/genid/ugent-biblio/ |

