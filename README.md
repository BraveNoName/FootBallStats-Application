# ⚽ FootballStats

**Football statistics web application built with Java and Spring Boot, featuring domestic league generation, European competitions and database performance testing.**

FootballStats is a web application developed focuses on generating and managing football competition data while providing a testing environment for comparing the performance of different database management systems.

The application simulates domestic football leagues and UEFA club competitions, including team and player generation, match scheduling, match events, league standings and European qualification processes.

An important part of the project is the implementation of multiple database solutions and the evaluation of their performance under comparable workloads.

---

## 📌 Table of Contents

- [Project Overview](#-project-overview)
- [Main Features](#-main-features)
- [Domestic Competitions](#-domestic-competitions)
- [European Competitions](#-european-competitions)
- [European Competition Generation Process](#-european-competition-generation-process)
- [Database Systems](#-database-systems)
- [Performance Testing](#-performance-testing)
- [Technologies](#-technologies)
- [Application Architecture](#-application-architecture)
- [Configuration and Running the Project](#-configuration-and-running-the-project)

---

## 🎯 Project Overview

The main purpose of FootballStats is to provide a football statistics application that can operate with different database management systems.

The project uses a consistent application domain and comparable test scenarios to evaluate how different database technologies handle the same types of operations.

The application supports:

- Domestic football league generation.
- Team and player data management.
- Football match and match event generation.
- League standings and statistical calculations.
- UEFA Champions League, Europa League and Conference League generation.
- European competition qualification simulations.
- Database performance measurements.

The application supports both relational and non-relational database systems. Each database implementation is designed to handle corresponding application operations and performance test scenarios.

---

## ⚽ Main Features

### Domestic League Management

The application generates domestic football leagues for the 2025/2026 season.

The league generation process includes:

- Loading reference data for football leagues and clubs.
- Generating player squads for clubs.
- Generating league matches.
- Simulating match results and events.
- Calculating league standings.
- Assigning European qualification and relegation positions.

The application uses league-specific rules to determine the number of qualification spots and relegation positions.

### Player Generation

FootballStats generates player squads for individual clubs.

Generated players have attributes such as:

- Player quality.
- Market value.
- Club assignment.

Player quality and market values are also used to estimate overall club strength, which influences the results of European qualification simulations.

### Required Data Generation Order

For the application to generate competition data correctly, the following order must be followed:

1. **Generate players** - create player squads and assign players to their respective clubs.
2. **Generate domestic leagues** - generate league matches, simulate match results and events, and calculate league standings.
3. **Generate European competitions** - generate the UEFA Champions League, Europa League and Conference League using the completed domestic league standings and generated player data.

This order is required because European competition generation depends on the results of domestic leagues and on player data used to evaluate club strength during qualification simulations.

### Match Generation and Events

The application generates domestic and European football matches.

Match-related functionality includes:

- Match scheduling.
- Match results.
- Match events.
- Match data retrieval.
- Statistical data processing.

Match events allow the application to represent important actions occurring during football matches.

### League Standings

League tables are calculated using generated match results.

The calculation takes into account:

- Played matches.
- Wins, draws and losses.
- Points.
- Goals scored and conceded.
- Goal difference.

The resulting standings are used to determine the final positions of clubs and their qualification for European competitions.

---

## 🏆 European Competitions

FootballStats implements the generation of three major UEFA club competitions:

- UEFA Champions League.
- UEFA Europa League.
- UEFA Conference League.

The application uses the competition format introduced in the 2024/2025 season, with 36 participating clubs in the league phase of each competition.

The generation process takes domestic league standings and qualification rules into account.

### Qualification Rules

Clubs are assigned to European competitions based on their final domestic league positions and qualification zones.

The application distinguishes between:

- Direct qualification.
- Qualification-round participation.
- Relegation to a lower European competition.
- Elimination from European competitions.

The number of qualification spots depends on the league and its assigned qualification rules.

### Club Strength Evaluation

Club strength is calculated using generated player squads, taking into account player quality and market values.

This value influences qualification simulations and gives stronger clubs a higher probability of progressing through qualification rounds.

### Competition Participation Limits

The system enforces maximum participation limits for clubs from the same domestic league:

| Competition | Maximum clubs per domestic league |
| ----------------- | --------------------------------: |
| Champions League | 5 |
| Europa League | 4 |
| Conference League | 3 |

These restrictions help prevent individual domestic leagues from dominating European competitions.

---

## 🔄 European Competition Generation Process

European competitions are generated sequentially, starting with the Champions League and ending with the Conference League.

The process uses domestic league standings, qualification positions and club strength to determine the final participants.

### 1. Domestic League Standings

The application first generates domestic league tables using match results.

Based on the final standings, clubs receive European qualification positions.

### 2. Champions League Generation

The Champions League is generated first.

The system:

1. Adds clubs qualified directly for the league phase.
2. Loads clubs assigned to qualification rounds.
3. Simulates qualification matches based on club strength.
4. Adds successful clubs until the competition reaches 36 participants.

Clubs that fail to qualify through the Champions League qualification process may be transferred to the Europa League.

### 3. Europa League Generation

The Europa League is generated after the Champions League.

Its participants include:

- Clubs directly qualified for the competition.
- Clubs transferred from Champions League qualification.
- Clubs successfully progressing through Europa League qualification rounds.

Clubs that fail to qualify for the Europa League may move to the Conference League.

### 4. Conference League Generation

The Conference League is generated last.

Its participants include:

- Direct qualifiers.
- Clubs transferred from Europa League qualification.
- Clubs progressing through Conference League qualification rounds.

Clubs that fail to qualify for this competition are eliminated from European competitions.

### 5. Final Competition Setup

After qualification is completed, the application creates the final groups of 36 participants for each competition and assigns UEFA competition pots.

The system also validates domestic league participation limits throughout the generation process.

### Main Components

The European competition generation process is divided into several components:

| Component | Responsibility |
| -------------------------------- | ----------------------------------------------------------------------------------------------------------- |
| `LeagueTableService` | Calculates domestic league standings and assigns European qualification positions. |
| `EuropeanCompetitionService` | Coordinates the generation of European competitions and qualification rounds. |
| `GeneratedPlayerRepository` | Provides generated player data used to evaluate club strength. |
| `MatchRepository` | Provides domestic league match data used for calculating standings. |
| `EuropeanCompetitionParticipant` | Represents a club participating in a European competition and stores its qualification-related information. |

---

## 🗄️ Database Systems

One of the main objectives of the project is to compare different database management systems using the same application domain and comparable test scenarios.

FootballStats supports six database technologies.

### Relational Database Management Systems

| Database | Type |
| -------------------- | ---------- |
| PostgreSQL | Relational |
| MySQL | Relational |
| Microsoft SQL Server | Relational |
| Oracle Database | Relational |

Relational implementations use Spring Data JPA for database communication and entity persistence.

### Non-Relational Database Management Systems

| Database | Type |
| -------- | ----------------- |
| MongoDB | Document-oriented |
| Redis | Key-value |

MongoDB stores application data using a document-based data model, while Redis uses key-value structures for data storage and retrieval.

Separate application implementations are used for MongoDB and Redis to accommodate their different data models and access methods.

---

## 📊 Performance Testing

The project includes a set of performance tests designed to compare database execution time and resource consumption.

Each database system is tested using corresponding scenarios and comparable input data.

### Test Scenarios

The performance tests cover several types of operations:

- **CRUD operations** - inserting, retrieving, updating and deleting records.
- **Data generation** - creating players, leagues, matches and match events.
- **League operations** - generating and retrieving league standings.
- **European competitions** - generating European competition data and retrieving competition information.
- **Pagination** - retrieving data in pages.
- **JOIN queries** - retrieving related data using relational joins.
- **Sorting and filtering** - querying records based on selected conditions.
- **Application workflows** - executing sequences of operations representing application functionality.

### CRUD Data Volumes

CRUD tests use different data volumes:

- 1,000 records.
- 10,000 records.
- 100,000 records.

### Measurement Parameters

The performance evaluation includes:

| Parameter | Description |
| ------------- | -------------------------------------------------- |
| Response Time | Time required to execute an application operation. |
| CPU Usage | CPU utilization of the database service. |
| RAM Usage | Memory consumption of the database service. |
| Block Read | Amount of data read from block devices. |
| Block Write | Amount of data written to block devices. |

The tests are repeated to obtain comparable results and reduce the influence of individual measurement fluctuations.
---

## 🛠️ Technologies

The project uses the following technologies:

| Technology | Purpose |
| -------------------- | --------------------------------------- |
| Java 25 | Main programming language |
| Spring Boot 4 | Application framework |
| Spring Data JPA | Relational database persistence |
| SQL | Relational database queries |
| H2 | Local development database |
| PostgreSQL | Relational database |
| MySQL | Relational database |
| Microsoft SQL Server | Relational database |
| Oracle Database | Relational database |
| MongoDB | Document database |
| Redis | Key-value database |
| Maven | Project build and dependency management |
| REST API | Communication with application services |

---

## 🏗️ Application Architecture

The project uses separate application implementations for relational and non-relational database systems.

Relational database implementations share the JPA-based persistence approach, while MongoDB and Redis use dedicated implementations adapted to their respective data models.

The application is organized around services responsible for business logic and repositories responsible for data access.

The main application components include:

- **Controllers** - expose application functionality through HTTP endpoints.
- **Services** - implement business logic, data generation and competition calculations.
- **Repositories** - handle database access and data persistence.
- **Models and Entities** - represent application data, including leagues, clubs, players, matches and competition participants.

The application can be started with different Spring profiles depending on the selected database implementation.

For local development, H2 is used as the default relational database, allowing the application to run without setting up an external database server.

---

## ⚙️ Configuration and Running the Project

The application uses Spring Boot configuration files to define database connections and application settings.

For local development, the application can be configured to use an H2 database.

### Requirements

- Java 25
- Maven (or the included Maven Wrapper)
- IntelliJ IDEA or another Java IDE
- Docker Desktop with Docker Compose (for running the application with external database containers)

### Running the Application

Clone the repository:

```bash
git clone https://github.com/BraveNoName/FootBallStats.git
```

Navigate to the project directory:

```bash
cd FootBallStats
```

Build the application on Windows:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Or on Linux/macOS:

```bash
./mvnw clean package -DskipTests
```

Run the application using the configured Spring Boot profile for local development.

The H2 database is used for local development and testing, so no external database server is required for this setup.

### Running with Docker Compose

The project can also be run in Docker. In this setup, the Spring Boot application and the selected database management system run in **separate containers**. Docker Compose defines a separate application service and database service for each supported database.

Each application service uses the Spring profile corresponding to its database implementation:

| Database | Spring Profile | Application Container | Database Container |
|---|---|---|---|
| PostgreSQL | `postgres` | `app-postgres` | `postgres-db` |
| MySQL | `mysql` | `app-mysql` | `mysql-db` |
| Microsoft SQL Server | `mssql` | `app-mssql` | `mssql-db` |
| Oracle Database | `oracle` | `app-oracle` | `oracle-db` |
| MongoDB | `mongodb` | `app-mongodb` | `mongo-db` |
| Redis | `redis` | `app-redis` | `redis-db` |

The selected Spring profile activates the appropriate application implementation and database configuration. The application connects to its database through the Docker network, using the database service name as the host.

To build and start a selected application and its database, run the corresponding pair of services. For example, for PostgreSQL:

```bash
docker compose up --build -d postgres-db app-postgres
```

Replace `postgres-db app-postgres` with the appropriate database and application service names from the table to run another implementation.

To stop the containers:

```bash
docker compose down
```

To stop the containers and remove their associated volumes:

```bash
docker compose down -v
```

The Docker setup is separate from local H2 development. When using Docker, both the selected application container and its corresponding database container must be running.

---

# ⚽ FootballStats

**Aplikacja internetowa prezentująca statystyki piłkarskie, stworzona w Javie i Spring Boot. Umożliwia generowanie lig krajowych, rozgrywek europejskich oraz przeprowadzanie testów wydajnościowych systemów bazodanowych.**

FootballStats to aplikacja internetowa która koncentruje się na generowaniu i zarządzaniu danymi dotyczącymi rozgrywek piłkarskich, a także umożliwia porównywanie wydajności różnych systemów zarządzania bazami danych.

Aplikacja symuluje rozgrywki lig krajowych oraz europejskie rozgrywki klubowe UEFA. Obejmuje generowanie drużyn i zawodników, terminarzy, meczów i zdarzeń meczowych, obliczanie tabel ligowych oraz procesy kwalifikacji do europejskich pucharów.

Ważnym elementem projektu jest wykorzystanie różnych rozwiązań bazodanowych i ocena ich wydajności w porównywalnych warunkach testowych.

---

## 📌 Spis treści

- [Opis projektu](#-opis-projektu)
- [Główne funkcjonalności](#-główne-funkcjonalności)
- [Rozgrywki krajowe](#-rozgrywki-krajowe)
- [Rozgrywki europejskie](#-rozgrywki-europejskie)
- [Proces generowania rozgrywek europejskich](#-proces-generowania-rozgrywek-europejskich)
- [Systemy bazodanowe](#-systemy-bazodanowe)
- [Testy wydajnościowe](#-testy-wydajnościowe)
- [Wykorzystane technologie](#-wykorzystane-technologie)
- [Architektura aplikacji](#-architektura-aplikacji)
- [Konfiguracja i uruchomienie projektu](#-konfiguracja-i-uruchomienie-projektu)

---

## 🎯 Opis projektu

Głównym założeniem FootballStats jest stworzenie aplikacji do prezentowania statystyk piłkarskich, która może współpracować z różnymi systemami zarządzania bazami danych.

Projekt wykorzystuje tę samą dziedzinę aplikacyjną i porównywalne scenariusze testowe, aby ocenić sposób obsługi tych samych operacji przez różne technologie bazodanowe.

Aplikacja umożliwia:

- Generowanie krajowych rozgrywek piłkarskich.
- Zarządzanie danymi drużyn i zawodników.
- Generowanie meczów oraz zdarzeń meczowych.
- Obliczanie tabel ligowych i statystyk.
- Generowanie rozgrywek UEFA Champions League, UEFA Europa League i UEFA Conference League.
- Symulowanie procesów kwalifikacyjnych do rozgrywek europejskich.
- Przeprowadzanie pomiarów wydajności systemów bazodanowych.

Projekt obsługuje zarówno relacyjne, jak i nierelacyjne systemy bazodanowe. Poszczególne implementacje zostały dostosowane do sposobu przechowywania i przetwarzania danych w danym systemie.

---

## ⚽ Główne funkcjonalności

### Zarządzanie ligami krajowymi

Aplikacja umożliwia generowanie krajowych rozgrywek piłkarskich na sezon 2025/2026.

Proces generowania lig obejmuje:

- Wczytywanie danych referencyjnych dotyczących lig i klubów.
- Generowanie składów zawodników dla poszczególnych drużyn.
- Generowanie terminarzy ligowych.
- Symulowanie wyników meczów i zdarzeń meczowych.
- Obliczanie tabel ligowych.
- Przypisywanie miejsc premiowanych awansem do rozgrywek europejskich oraz miejsc spadkowych.

Aplikacja uwzględnia indywidualne zasady poszczególnych lig, określające liczbę miejsc kwalifikacyjnych oraz pozycje związane ze spadkiem do niższej klasy rozgrywkowej.

### Generowanie zawodników

FootballStats umożliwia generowanie składów zawodników dla poszczególnych klubów.

Wygenerowani zawodnicy posiadają między innymi następujące atrybuty:

- Umiejętności zawodnika.
- Wartość rynkową.
- Przypisanie do klubu.

Umiejętności zawodników oraz ich wartości rynkowe są również wykorzystywane do szacowania siły poszczególnych drużyn. Wpływa to na wyniki symulacji rund kwalifikacyjnych do rozgrywek europejskich.

### Wymagana kolejność generowania danych

Aby aplikacja poprawnie generowała dane dotyczące rozgrywek, należy zachować następującą kolejność:

1. **Generowanie zawodników** - utworzenie składów zawodników i przypisanie ich do odpowiednich klubów.
2. **Generowanie lig krajowych** - wygenerowanie meczów ligowych, symulacja wyników i zdarzeń meczowych oraz obliczenie tabel ligowych.
3. **Generowanie rozgrywek europejskich** - utworzenie Ligi Mistrzów, Ligi Europy i Ligi Konferencji na podstawie zakończonych rozgrywek krajowych oraz wygenerowanych danych zawodników.

Zachowanie tej kolejności jest wymagane, ponieważ generowanie rozgrywek europejskich zależy od wyników lig krajowych oraz danych zawodników wykorzystywanych do oceny siły drużyn podczas symulacji rund kwalifikacyjnych.

### Generowanie meczów i zdarzeń

Aplikacja umożliwia generowanie meczów rozgrywek krajowych oraz europejskich.

Funkcjonalności związane z meczami obejmują:

- Tworzenie terminarzy.
- Generowanie wyników spotkań.
- Generowanie zdarzeń meczowych.
- Pobieranie danych dotyczących meczów.
- Przetwarzanie danych statystycznych.

Zdarzenia meczowe pozwalają odwzorować najważniejsze sytuacje występujące podczas spotkań piłkarskich.

### Tabele ligowe

Tabele ligowe są obliczane na podstawie wygenerowanych wyników spotkań.

Podczas obliczeń uwzględniane są:

- Rozegrane mecze.
- Zwycięstwa, remisy i porażki.
- Zdobyte punkty.
- Bramki strzelone i stracone.
- Bilans bramkowy.

Uzyskana klasyfikacja pozwala określić końcowe pozycje drużyn oraz ich kwalifikację do rozgrywek europejskich.

---

## 🏆 Rozgrywki europejskie

FootballStats implementuje mechanizm generowania trzech głównych europejskich rozgrywek klubowych UEFA:

- UEFA Champions League (Liga Mistrzów).
- UEFA Europa League (Liga Europy).
- UEFA Conference League (Liga Konferencji).

Aplikacja wykorzystuje format rozgrywek wprowadzony od sezonu 2024/2025, w którym faza ligowa każdej z tych trzech rozgrywek składa się z 36 drużyn.

Proces generowania uczestników uwzględnia końcowe tabele lig krajowych oraz obowiązujące zasady kwalifikacji.

### Zasady kwalifikacji

Drużyny są przypisywane do odpowiednich rozgrywek europejskich na podstawie zajętych miejsc w ligach krajowych oraz wyznaczonych stref kwalifikacyjnych.

System rozróżnia:

- Bezpośrednią kwalifikację do rozgrywek.
- Udział w rundach kwalifikacyjnych.
- Przejście do niższych rozgrywek europejskich.
- Całkowite odpadnięcie z rozgrywek europejskich.

Liczba miejsc kwalifikacyjnych zależy od ligi oraz przypisanych jej zasad kwalifikacji.

### Ocena siły drużyn

Siła drużyny jest obliczana na podstawie wygenerowanego składu zawodników, z uwzględnieniem ich umiejętności i wartości rynkowych.

Uzyskana wartość wpływa na symulację rund kwalifikacyjnych, dając silniejszym drużynom większe prawdopodobieństwo awansu.

### Limity uczestnictwa drużyn

System uwzględnia maksymalną liczbę drużyn z jednej ligi krajowej, które mogą uczestniczyć w poszczególnych rozgrywkach:

| Rozgrywki | Maksymalna liczba drużyn z jednej ligi |
|---|---:|
| Liga Mistrzów | 5 |
| Liga Europy | 4 |
| Liga Konferencji | 3 |

Ograniczenia te zapobiegają nadmiernej dominacji pojedynczych lig krajowych w rozgrywkach europejskich.

---

## 🔄 Proces generowania rozgrywek europejskich

Rozgrywki europejskie są generowane kolejno, rozpoczynając od Ligi Mistrzów, a kończąc na Lidze Konferencji.

Proces wykorzystuje końcowe tabele lig krajowych, przydzielone miejsca kwalifikacyjne oraz siłę poszczególnych drużyn do ustalenia ostatecznej listy uczestników.

### 1. Tabele lig krajowych

W pierwszej kolejności aplikacja generuje tabele lig krajowych na podstawie wyników rozegranych spotkań.

Na podstawie końcowej klasyfikacji drużynom przypisywane są miejsca kwalifikacyjne do rozgrywek europejskich.

### 2. Generowanie Ligi Mistrzów

Liga Mistrzów jest generowana jako pierwsza.

System:

1. Dodaje drużyny posiadające bezpośrednią kwalifikację do fazy ligowej.
2. Wczytuje drużyny przypisane do rund kwalifikacyjnych.
3. Symuluje mecze kwalifikacyjne na podstawie siły poszczególnych drużyn.
4. Dodaje zwycięskie drużyny do momentu uzyskania 36 uczestników.

Drużyny, którym nie uda się zakwalifikować do Ligi Mistrzów, mogą zostać przeniesione do Ligi Europy.

### 3. Generowanie Ligi Europy

Liga Europy jest generowana po zakończeniu procesu tworzenia Ligi Mistrzów.

Jej uczestnikami są:

- Drużyny bezpośrednio zakwalifikowane do rozgrywek.
- Drużyny przeniesione z kwalifikacji do Ligi Mistrzów.
- Drużyny, które uzyskały awans w rundach kwalifikacyjnych Ligi Europy.

Drużyny, którym nie uda się zakwalifikować do Ligi Europy, mogą przejść do Ligi Konferencji.

### 4. Generowanie Ligi Konferencji

Liga Konferencji jest generowana jako ostatnia.

Jej uczestnikami są:

- Drużyny zakwalifikowane bezpośrednio.
- Drużyny przeniesione z kwalifikacji do Ligi Europy.
- Drużyny, które uzyskały awans w rundach kwalifikacyjnych Ligi Konferencji.

Drużyny, którym nie uda się zakwalifikować do tych rozgrywek, zostają całkowicie wyeliminowane z europejskich pucharów.

### 5. Końcowa konfiguracja rozgrywek

Po zakończeniu kwalifikacji aplikacja tworzy końcowe zestawienia 36 uczestników każdej z rozgrywek oraz przypisuje drużyny do odpowiednich koszyków UEFA.

W całym procesie system kontroluje również limity uczestnictwa drużyn z poszczególnych lig krajowych.

### Główne komponenty

Proces generowania rozgrywek europejskich został podzielony na kilka komponentów:

| Komponent | Odpowiedzialność |
|---|---|
| `LeagueTableService` | Oblicza tabele lig krajowych i przypisuje miejsca kwalifikacyjne do rozgrywek europejskich. |
| `EuropeanCompetitionService` | Koordynuje generowanie rozgrywek europejskich oraz symulację rund kwalifikacyjnych. |
| `GeneratedPlayerRepository` | Udostępnia dane wygenerowanych zawodników wykorzystywane do oceny siły drużyn. |
| `MatchRepository` | Udostępnia dane dotyczące meczów lig krajowych wykorzystywane do obliczania tabel. |
| `EuropeanCompetitionParticipant` | Reprezentuje uczestnika rozgrywek europejskich i przechowuje informacje związane z jego udziałem w rozgrywkach. |

---

## 🗄️ Systemy bazodanowe

Jednym z głównych celów projektu jest porównanie różnych systemów zarządzania bazami danych z wykorzystaniem tej samej dziedziny aplikacyjnej i porównywalnych scenariuszy testowych.

FootballStats obsługuje sześć technologii bazodanowych.

### Relacyjne systemy bazodanowe

| System bazodanowy | Rodzaj |
|---|---|
| PostgreSQL | Relacyjny |
| MySQL | Relacyjny |
| Microsoft SQL Server | Relacyjny |
| Oracle Database | Relacyjny |

Implementacje relacyjne wykorzystują Spring Data JPA do komunikacji z bazą danych i zarządzania trwałością danych.

### Nierelacyjne systemy bazodanowe

| System bazodanowy | Rodzaj |
|---|---|
| MongoDB | Dokumentowy |
| Redis | Klucz-wartość |

MongoDB przechowuje dane aplikacji w postaci dokumentów, natomiast Redis wykorzystuje struktury typu klucz-wartość do przechowywania i pobierania danych.

Dla MongoDB i Redis przygotowano osobne implementacje aplikacji, dostosowane do odmiennych modeli danych i sposobów dostępu do nich.

---

## 📊 Testy wydajnościowe

Projekt zawiera zestaw testów wydajnościowych, których celem jest porównanie czasu wykonywania operacji oraz zużycia zasobów przez poszczególne systemy bazodanowe.

Każdy system jest testowany z wykorzystaniem odpowiadających sobie scenariuszy i porównywalnych danych wejściowych.

### Scenariusze testowe

Testy wydajnościowe obejmują kilka rodzajów operacji:

- **Operacje CRUD** - dodawanie, odczytywanie, aktualizowanie i usuwanie rekordów.
- **Generowanie danych** - tworzenie zawodników, lig, meczów i zdarzeń meczowych.
- **Operacje ligowe** - generowanie i pobieranie tabel ligowych.
- **Rozgrywki europejskie** - generowanie danych dotyczących rozgrywek europejskich i pobieranie informacji o uczestnikach.
- **Paginacja** - pobieranie danych z podziałem na strony.
- **Zapytania JOIN** - pobieranie powiązanych danych z wykorzystaniem złączeń tabel relacyjnych.
- **Sortowanie i filtrowanie** - wyszukiwanie rekordów według określonych kryteriów.
- **Przepływy aplikacyjne** - wykonywanie sekwencji operacji odwzorowujących działanie aplikacji.

### Liczba rekordów w testach CRUD

Testy CRUD przeprowadzane są dla różnych wielkości zbiorów danych:

- 1 000 rekordów.
- 10 000 rekordów.
- 100 000 rekordów.

### Parametry pomiarowe

W ramach oceny wydajności analizowane są następujące parametry:

| Parametr | Opis |
|---|---|
| Czas odpowiedzi (Response Time) | Czas wymagany do wykonania operacji aplikacyjnej. |
| Wykorzystanie CPU (CPU Usage) | Obciążenie procesora przez usługę bazodanową. |
| Wykorzystanie RAM (RAM Usage) | Zużycie pamięci operacyjnej przez usługę bazodanową. |
| Odczyt blokowy (Block Read) | Ilość danych odczytanych z urządzeń blokowych. |
| Zapis blokowy (Block Write) | Ilość danych zapisanych na urządzeniach blokowych. |

Testy są powtarzane w celu uzyskania porównywalnych wyników i ograniczenia wpływu pojedynczych odchyleń pomiarowych.
---

## 🛠️ Wykorzystane technologie

W projekcie wykorzystano następujące technologie:

| Technologia | Zastosowanie |
|---|---|
| Java 25 | Główny język programowania |
| Spring Boot 4 | Framework aplikacyjny |
| Spring Data JPA | Obsługa trwałości danych w relacyjnych bazach danych |
| SQL | Język zapytań do relacyjnych baz danych |
| H2 | Lokalna baza danych wykorzystywana podczas developmentu |
| PostgreSQL | Relacyjny system bazodanowy |
| MySQL | Relacyjny system bazodanowy |
| Microsoft SQL Server | Relacyjny system bazodanowy |
| Oracle Database | Relacyjny system bazodanowy |
| MongoDB | Dokumentowy system bazodanowy |
| Redis | System bazodanowy typu klucz-wartość |
| Maven | Budowanie projektu i zarządzanie zależnościami |
| REST API | Komunikacja z usługami aplikacji |

---

## 🏗️ Architektura aplikacji

Projekt wykorzystuje osobne implementacje aplikacji dla relacyjnych i nierelacyjnych systemów bazodanowych.

Implementacje relacyjne korzystają ze wspólnego podejścia opartego na JPA, natomiast MongoDB i Redis wykorzystują dedykowane rozwiązania dostosowane do swoich modeli danych.

Aplikacja została zorganizowana wokół warstwy serwisów odpowiedzialnych za logikę biznesową oraz repozytoriów obsługujących dostęp do danych.

Główne komponenty aplikacji obejmują:

- **Kontrolery (Controllers)** - udostępniają funkcjonalności aplikacji za pośrednictwem punktów końcowych HTTP.
- **Serwisy (Services)** - realizują logikę biznesową, generowanie danych i obliczenia związane z rozgrywkami.
- **Repozytoria (Repositories)** - odpowiadają za dostęp do baz danych i zapisywanie danych.
- **Modele i encje (Models and Entities)** - reprezentują dane aplikacji, w tym ligi, kluby, zawodników, mecze i uczestników rozgrywek.

Aplikację można uruchamiać z wykorzystaniem różnych profili Spring, w zależności od wybranej implementacji bazodanowej.

Podczas lokalnego developmentu wykorzystywana jest baza H2, dzięki czemu możliwe jest uruchomienie aplikacji bez konieczności konfigurowania zewnętrznego serwera bazodanowego.

---

## ⚙️ Konfiguracja i uruchomienie projektu

Aplikacja wykorzystuje pliki konfiguracyjne Spring Boot do określania połączeń z bazami danych oraz ustawień poszczególnych implementacji.

Na potrzeby lokalnego uruchamiania aplikacja może korzystać z bazy danych H2.

### Wymagania

- Java 25.
- Maven (lub dołączony do projektu Maven Wrapper).
- IntelliJ IDEA lub inne środowisko programistyczne obsługujące Javę.
- Docker Desktop z Docker Compose (w przypadku uruchamiania aplikacji z wykorzystaniem zewnętrznych kontenerów bazodanowych).

### Uruchomienie aplikacji

Sklonuj repozytorium:

```bash
git clone https://github.com/BraveNoName/FootBallStats.git
```

Przejdź do katalogu projektu:

```bash
cd FootBallStats
```

Zbuduj aplikację w systemie Windows:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Lub w systemie Linux/macOS:

```bash
./mvnw clean package -DskipTests
```

Następnie uruchom aplikację z wykorzystaniem skonfigurowanego profilu Spring przeznaczonego do lokalnego developmentu.

Baza H2 jest wykorzystywana podczas lokalnego developmentu i testowania, dlatego w tym przypadku nie jest wymagane konfigurowanie zewnętrznego serwera bazodanowego.

### Uruchamianie z wykorzystaniem Docker Compose

Projekt można również uruchomić w środowisku Docker. W takim przypadku aplikacja Spring Boot oraz wybrany system zarządzania bazą danych działają w **osobnych kontenerach**. W konfiguracji Docker Compose zdefiniowano osobną usługę aplikacji i osobną usługę bazy danych dla każdego obsługiwanego systemu.

Każda usługa aplikacji wykorzystuje profil Spring przypisany do danej implementacji bazodanowej:

| System bazodanowy | Profil Spring | Kontener aplikacji | Kontener bazy danych |
|---|---|---|---|
| PostgreSQL | `postgres` | `app-postgres` | `postgres-db` |
| MySQL | `mysql` | `app-mysql` | `mysql-db` |
| Microsoft SQL Server | `mssql` | `app-mssql` | `mssql-db` |
| Oracle Database | `oracle` | `app-oracle` | `oracle-db` |
| MongoDB | `mongodb` | `app-mongodb` | `mongo-db` |
| Redis | `redis` | `app-redis` | `redis-db` |

Wybrany profil Spring aktywuje odpowiednią implementację aplikacji oraz konfigurację połączenia z bazą danych. Aplikacja łączy się z bazą za pośrednictwem sieci Docker, wykorzystując nazwę usługi bazodanowej jako adres hosta.

Aby zbudować i uruchomić wybraną aplikację wraz z przypisaną do niej bazą danych, należy wskazać obie usługi. Przykładowo dla PostgreSQL:

```bash
docker compose up --build -d postgres-db app-postgres
```

Aby uruchomić inną implementację, należy zastąpić `postgres-db app-postgres` odpowiednimi nazwami usług z tabeli.

Zatrzymanie kontenerów:

```bash
docker compose down
```

Zatrzymanie kontenerów i usunięcie powiązanych wolumenów:

```bash
docker compose down -v
```

Środowisko Docker jest niezależne od lokalnego uruchamiania aplikacji z bazą H2. W przypadku korzystania z Dockera muszą działać zarówno wybrany kontener aplikacji, jak i odpowiadający mu kontener bazy danych.

---
