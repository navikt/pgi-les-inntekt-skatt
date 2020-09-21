# pgi-les-inntekt-skatt
Henter hendelser fra kafka topic ```privat-pgi-hendelse```, 
gjør REST kall til SKE for å hente inntekter knyttet til hendelser, 
og publiserer deretter inntektene til kafka topicen ```privat-pgi-inntekt```.

For å se hvordan hendelsene publiseres til ```privat-pgi-hendelse```, se følgende github repo: [pgi-les-hendelse-skatt](https://github.com/navikt/pgi-les-hendelse-skatt/)

Dokumentasjon REST tjeneste vi bruker fra SKE: [pensjonsgivende inntekt API](https://skatteetaten.github.io/datasamarbeid-api-dokumentasjon/reference_pgi.html)

#### Bygge lokalt
For å bygge lokalt, så må man ha satt environment variablene GITHUB_ACTOR og GITHUB_TOKEN.
Generer nytt token her: https://github.com/settings/tokens. Husk å KUN gi den følgende tilgangen:

```read:packages Download packages from github package registry```.

Med tokenet generert så har jeg satt det opp slik i .zshrc/.bashrc
```
export GITHUB_ACTOR="username"
# Read only token for downloading github packages
export GITHUB_TOKEN="token"
```

#### Metrikker
Grafana dashboards brukes for å f.eks. monitorere minne, cpu-bruk og andre metrikker.
Se [pgi-les-inntekt-skatt grafana dasboard](https://grafana.adeo.no/) TODO: Fiks link

#### Logging
[Kibana](https://logs.adeo.no/app/kibana) benyttes til logging. Søk på f.eks. ```application:pgi-les-inntekt-skatt AND envclass:q``` for logginnslag fra preprod.

#### Kontakt
Kontakt Team Samhandling dersom du har noen spørsmål. Vi finnes blant annet på Slack, i kanalen [#samhandling_pensjonsområdet](https://nav-it.slack.com/archives/CQ08JC3UG)

