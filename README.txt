=========================================================
How does the European competition generation system work?
=========================================================

Based on those league standings, clubs are divided into:

teams with direct qualification,
and teams entering qualification rounds.

Then the generation process happens step by step, from the highest competition to the lowest.

First, the Champions League is created:

all directly qualified clubs are added,
then teams from Champions League qualifiers compete for the remaining spots,
until the competition reaches 36 teams.

During qualification rounds, club strength becomes very important:

stronger squads have a better chance of qualifying,
because the system ranks clubs using squad quality and player market values.

Teams that fail to enter the Champions League:

are not eliminated immediately,
but instead drop down into the Europa League.

Next, the Europa League is generated:

direct qualifiers are added first,
then clubs dropping from Champions League qualification,
followed by Europa League qualification teams.

Again:

teams that fail to enter the Europa League
drop down into the Conference League.

Finally, the Conference League is created:

direct qualifiers,
clubs dropping from Europa League qualification,
and winners of Conference League qualifiers.

Teams that still fail to qualify at this stage:

are completely eliminated from European competitions.

Throughout the entire process, the system constantly enforces league limits:

maximum 5 clubs from the same league in the Champions League,
maximum 4 clubs from the same league in the Europa League,
maximum 3 clubs from the same league in the Conference League.

So even if a league like the Premier League has many strong clubs:

it cannot completely dominate European competitions,
because the limits stop additional clubs from qualifying.

As a result:

the competitions feel much more realistic,
stronger clubs still have an advantage,
the “drop-down” system between competitions works properly,
and unrealistic situations like 12 clubs from one country in a single competition are avoided.


=================================================================
How does the European competition generation work in the project?
=================================================================

1. LeagueTableService

This class is responsible for generating domestic league tables.

Based on:

played matches,
results,
points,
goal difference,

the system calculates the final league standings.

This class also assigns:

European qualification zones (zone),
relegation spots,
playoff positions.

It determines:

which clubs qualify for the Champions League,
Europa League,
Conference League,
and qualification rounds.

For example:

top 5 leagues receive expanded European spots,
the Netherlands and Portugal use custom qualification rules.
2. EuropeanCompetitionService

This is the main class responsible for generating European competitions.

First, it loads:

all leagues included in the project,
and retrieves their final league tables from LeagueTableService.

Then the system:

analyzes European qualification zones,
divides clubs into:
direct qualifiers,
qualification-round teams.

Next, it generates:

the Champions League,
Europa League,
Conference League.

The system:

evaluates club strength,
simulates qualification rounds,
handles club drop-downs between competitions,
and enforces league participation limits.
3. GeneratedPlayerRepository

This class provides generated club players.

Using:

market values,
player quality,
generated squads,

the system calculates overall club strength.

That strength influences:

qualification results,
club selection for European competitions,
and future match simulations.
4. MatchRepository

Repository responsible for domestic league matches.

It provides:

all league matches,
which are later used to:
calculate league tables,
determine European qualification spots.
5. EuropeanCompetitionParticipant

Model representing a participant in European competitions.

It stores:

club name,
league,
country,
UEFA ranking,
competition pot,
competition type,
title holder status.
6. Generation process step by step

The system works in the following order:

Generate domestic league tables.
Assign European qualification spots.
Load direct qualifiers.
Load qualification-round clubs.
Calculate club strength.
Simulate qualification rounds.
Handle drop-downs between competitions.
Enforce country limits.
Create final groups of 36 teams.
Assign UEFA pots.

IMPORTANT: EUROPEAN COMPETITIONS USE NEW FORMAT SINCE SEASON 2024/2025


TODO: 3. sile klubow zeby barcelony itd bylo wiecej zespolow z topu bo  za duzo jaskow jest.
