# pgi-les-inntekt-skatt
Henter hendelser fra kafka topic ```privat-pgi-hendelse```, 
gjør REST kall til SKE for å hente inntekter knyttet til hendelser, 
og publiserer deretter inntektene til kafka topicen ```privat-pgi-inntekt```.

For å se hvordan hendelsene publiseres til ```privat-pgi-hendelse```, se følgende github repo: [pgi-les-hendelse-skatt](https://github.com/navikt/pgi-les-hendelse-skatt/)

Dokumentasjonen REST tjeneste vi bruker fra SKE: [SKE pensjonsgivende inntekt](https://skatteetaten.github.io/datasamarbeid-api-dokumentasjon/reference_pgi.html)

#### Metrikker
Grafana dashboards brukes for å f.eks. monitorere minne, cpu-bruk og andre metrikker.
Se [pgi-les-inntekt-skatt grafana dasboard](https://grafana.adeo.no/) TODO: Fiks link

#### Logging
[Kibana](https://logs.adeo.no/app/kibana) benyttes til logging. Søk på f.eks. ```application:pgi-les-inntekt-skatt AND envclass:q``` for logginnslag fra preprod.


#### Kontakt
Kontakt Team Samhandling dersom du har noen spørsmål. Vi finnes blant annet på Slack, i kanalen [#samhandling_pensjonsområdet](https://nav-it.slack.com/archives/CQ08JC3UG)

