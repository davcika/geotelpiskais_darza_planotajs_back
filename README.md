# Dārza plānotājs — backend

Spring Boot 4.1 (Java 21+) + PostgreSQL 16. Shēmu veido Flyway (`src/main/resources/db/migration`).

## Palaišana

1. Datubāze (vajag Docker Desktop):
   ```
   docker compose up -d
   ```
   Bez Docker: izveido Postgres datubāzi `darza_planotajs` ar lietotāju `darza` / paroli `darza`,
   vai norādi savu caur vides mainīgajiem `DB_URL`, `DB_USER`, `DB_PASSWORD`.
2. Backend: atver mapi IntelliJ kā Maven projektu un palaid `DarzaPlanotajsApplication`
   ar profilu **dev** (Run → Edit Configurations → *Active profiles*: `dev`),
   vai terminālī `mvn spring-boot:run -Dspring-boot.run.profiles=dev`.
   Serveris klausās uz `http://localhost:8080`.
3. Frontend: `darza_planotajs_ang` mapē `npm start`. Angular dev serveris pāradresē `/api`
   uz `localhost:8080` (`proxy.conf.json`), tāpēc CORS nav vajadzīgs.

## Autentifikācija

Pieteikšanās atgriež JWT tokenu, kas jāsūta katrā pieprasījumā galvenē
`Authorization: Bearer <tokens>`. Tokens derīgs 7 dienas. Visi `/api/**` ceļi, izņemot
`/api/auth/**`, prasa tokenu (bez tā — 401).

Izstrādes konts `dev@darza-planotajs.lv` tiek izveidots migrācijā `V2__users.sql`. Ar paroli tajā
ieiet nevar — tikai caur `POST /api/auth/dev-login`, kas strādā tikai profilā `dev`.

Noklusējuma iestatījumi ir droši produkcijai: dev-login ir izslēgts, un bez vides mainīgā
`JWT_SECRET` (gara, nejauša atslēga, vismaz 32 simboli) serveris nestartē. Profils `dev`
(`application-dev.yaml`) ieslēdz dev-login un iestata izstrādes atslēgu.

## API

| Metode | Ceļš | Apraksts |
|---|---|---|
| POST | `/api/auth/register` | Jauns lietotājs + tukšs dārzs (201), atgriež tokenu |
| POST | `/api/auth/login` | Pieteikšanās, atgriež tokenu (401, ja nepareizi dati) |
| POST | `/api/auth/dev-login` | Pieteikšanās izstrādes kontā bez paroles |
| GET | `/api/gardens` | Pieteiktā lietotāja dārzi (`hubGeometry` null = vēl nav onboarding) |
| PUT | `/api/gardens/{id}/hub` | Saglabāt HUB zonu un adresi (400, ja zona nederīga vai objekts paliek ārpusē) |
| GET | `/api/addresses?q=&limit=8` | Adrešu ieteikumi (q 3–100 simboli; 503, kamēr adreses tiek ielādētas) |
| GET | `/api/gardens/{gardenId}/objects` | Visi dārza objekti |
| POST | `/api/gardens/{gardenId}/objects` | Jauns objekts (201) |
| PUT | `/api/objects/{id}` | Labot objektu |
| DELETE | `/api/objects/{id}` | Dzēst objektu (204) |

Svešs dārzs vai objekts atgriež 404, tāpat kā neesošs. Ja dārzam ir HUB zona, objektus var
izveidot un pārvietot tikai tās iekšpusē (dobei jābūt pilnībā zonā, kokam — stumbra punktam).

HUB zonas ķermenis: `{ "addressCode": 101234567, "geometry": "<GeoJSON Polygon>" }` (`addressCode` var būt `null`).
Zona: viens poligons bez caurumiem un pašķrustošanās, 20–50 000 m².

## Adreses

Adrešu ieteikumi nāk no VZD Valsts adrešu reģistra atvērtajiem datiem (`aw_eka.csv`, ēku un zemes
vienību adreses). Licence CC BY 4.0 — lietotnē jānorāda "Addresses © State Land Service of Latvia (VZD), CC BY 4.0".

`AddressImporter` ik pēc 6 stundām pārbauda, vai dati ir novecojuši (tabula tukša vai pēdējais
imports vecāks par 30 dienām), un tad lejupielādē failu (~140 MB) un aizstāj adreses vienā transakcijā.
Pirmais imports sākas ~30 s pēc starta un aizņem dažas minūtes; tikmēr meklēšana atbild 503.
Kļūdas gadījumā vecie dati paliek. Iestatījumi `app.address.import.*`:
- `enabled` — `false` izslēdz importu pavisam;
- `source` — URL vai lokāls faila ceļš (darbam bez interneta);
- `charset`, `max-age`.

Meklēšana: PostgreSQL `pg_trgm` + `unaccent` ("brivibas" atrod "Brīvības", pieļauj drukas kļūdas).
Implementācija ir aiz interfeisa `AddressSearch`, lai to varētu aizstāt ar citu meklētāju.
VZD koordinātas kopš 2026-10-07 ir LKS-2020; tās glabā kā EPSG:3059 (LKS-92), atšķirība ~10 cm.

Reģistrācijas un pieteikšanās ķermenis: `{ "email": "...", "password": "..." }` (parole vismaz 8 simboli, līdz 72 baitiem).
Atbilde: `{ "token": "...", "email": "..." }`.

Objekta ķermenis (POST un PUT):

```json
{
  "type": "BED",
  "name": "Dobe: 1",
  "geometry": "{\"type\":\"Polygon\",\"coordinates\":[[[506000,312000],[506010,312000],[506010,312005],[506000,312000]]]}",
  "crownRadiusM": null
}
```

`geometry` ir GeoJSON teksts koordinātās EPSG:3059 (LKS-92, metri) — tieši tas, ko atgriež
OpenLayers `new GeoJSON().writeGeometry(...)`. Kokiem `type` ir `TREE`, ģeometrija `Point`
un `crownRadiusM` ir vainaga rādiuss metros.

## Augi un stādījumi

| Metode | Ceļš | Apraksts |
|---|---|---|
| GET | `/api/plants` | Viss augu katalogs (V5 migrācija, ~30 kultūras); datumi `MM-DD` |
| GET | `/api/gardens/{gardenId}/plantings?year=2026` | Dārza stādījumi sezonā (sezona = `sownOn` gads) |
| POST | `/api/objects/{objectId}/plantings` | Jauns stādījums dobē: `{plantId, sownOn, variety?, note?}`; tikai `BED`, citādi 400 |
| PUT | `/api/plantings/{id}` | Labo stādījumu |
| DELETE | `/api/plantings/{id}` | Dzēš stādījumu |

Svešs dārzs, dobe vai stādījums atgriež 404. Izdzēšot dobi, tās stādījumi izdzēšas (ON DELETE CASCADE).
