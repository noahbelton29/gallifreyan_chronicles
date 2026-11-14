# Gallifreyan Chronicles Changelog

## 13-14 November 2026

### TARDIS Minds

**Added**

- Added fear stages: uneasy (10), afraid (30), terrified (60)
- Added new mind actions: shield, slam doors, sparks, reveal
- Added `/tardis` subcommands: tp to exterior or interior, TARDIS id
- Added messages for forgiveness, easing PTSD, emergency landings, smoke and using someone else's mallet

**Changed**

- Greatly expanded personalities: each now has its own unwilling-to-fly, mallet-sight, hit, terrified-hit and entry
  reactions (Aggressive fights and slams doors, Defensive refuses, shields and locks up, Submissive balks, Obedient only
  flinches)
- Expanded traits with new stat modifiers (trust, stress, fear, trauma, crash damage, stranger handling), blocked
  pairings and reveal conditions
- Expanded the peace offering: effectiveness now depends on trust, fear, betrayal, PTSD, streaks and cooldowns, with new
  reaction messages
- Expanded the mallet: per-personality fear, trauma and snap-out-of-terror ranges
- Expanded PTSD, trauma and recovery: harder to get, and it heals gradually with calm landings and kindness
- Expanded dyslexia with stress-scaled mix-ups and practice-based improvement
- Expanded `/tardis` mind readout to show per-player bonds (trust, fear, trauma, hits, betrayed)
- Expanded fright on entering with severity tiers and a cooldown
- Passive trust gains (jam persistence, calm landings) no longer happen from someone the TARDIS is afraid of
- Mallet hits now run the wariness check before the control's click action
- Reworked mind trauma reactions: removed the outside-sight reaction, and reactions now trigger from inside range (10
  blocks) only

**Balance** (current values)

- PTSD now needs 80 trauma (was 60)
- Repeated beatings do less lasting harm: each extra hit counts 25% less, down to a 25% floor
- Trauma sensitivity is capped between 0.75x and 1.25x
- Each calm landing heals 1.5 trauma, and recovering from PTSD is gradual: 3% off per calm landing, 10% while easing
- A calm landing needs stress under 30 and gives +2 trust
- Peace offering: +6 trust, -10 stress, -15 fear, -1 trauma
- Peace offering is refused above 70 trust, when the TARDIS is betrayed, or when it is too afraid
- Peace offering has a 5 minute cooldown per player per TARDIS
- Back-to-back gifts lose 25% effectiveness each, down to 25%, and the streak resets after 20 minutes
- Gifts work at half strength on a PTSD TARDIS and at half strength while it is wary
- Betrayal costs 40 trust, and the TARDIS forgives at 40 trust
- Devoted at 75 trust: stress gain x0.75. PTSD: stress gain x1.3
- Jam chance: 60% to 25% while wary (under 20 trust), 10% while cautious (under 35), 20% when afraid. Under 5 trust is
  hostile. Each jam gives +2 trust
- Overusing flight (inside 30 seconds) adds 10 stress, flying damaged adds 8, wrong key adds 4
- Refusal anger escalates: warning at 2, lash out at 3 (4 block range, up to 12 damage), burn at 5
- Aggressive: refuses at 45 stress or 30 trust, 6 to 15 fear per hit, obeys again at 65 fear, 3 trauma per hit
- Defensive: refuses at 50 stress or 30 trust, 10 to 25 fear per hit, obeys at 45 fear, 4 trauma per hit
- Submissive: refuses at 60 stress or 20 trust, 15 fear per hit, obeys at 25 fear, 5 trauma per hit
- Obedient: never refuses, 15 fear per hit, 5 trauma per hit
- Distrusting: starts at 12 trust, trust gain and loss x0.6, trauma taken x0.7, 2.5x trust loss when abused
- Forgiving: stress builds x1.15, calms x1.6, fear fades x1.6
- Grudge-holder: stress calms x0.7, fear fades x0.6, trust gained x1.3, 1.5x trust loss when abused
- Loyal: trust gained x1.6, trust lost x0.6, trauma taken x1.5
- Possessive: trust gained x1.2, strangers start at 25 trust and it refuses them 15 sooner
- Protective: stress builds x1.2, crash damage x0.6
- Skittish: stress builds x1.4, calms x1.8, trust gained x1.15
- Stubborn: trust lost x0.85, trauma taken x0.75, refuses 8 sooner
- Welcoming: trust lost x1.25, trauma taken x1.15, refuses strangers 10 later
- Dyslexic: 80% mix-up chance at the start, improving with practice to a 5% floor; stress above 50 makes it worse and
  above 80 causes bigger mix-ups
- Fright on entering: 1 minute cooldown, effect lasts 4 to 15 seconds, extra fear if betrayed (+0.25) or PTSD (+0.15)

### Flight and Crashes

**Added**

- Added stress-driven turbulence: strain builds from 50 stress, creaks from 60, sparks from 75, crash at 95
- Added emergency crash landing when the TARDIS is barely hanging on
- Added crash smoke and the Choking effect, with a smoke damage type and death messages
- Added crash rumble and camera shake
- Added spark, spark explosion and explosion sounds
- Added a higher vworp pitch under stress (up to 1.3x)

**Changed**

- Crashes add 25 stress
- Protective TARDISes take 40% less crash damage

### Landing

**Added**

- Added a landing search. The TARDIS looks up to 12 blocks around the target (24 vertically) for an open spot, so it no
  longer lands in mid-air
- Added a penalty for enclosed spots so it prefers open ones

**Changed**

- Leaves, flowers, saplings and other soft blocks are now destroyed so it can land as soon as it starts rematerialising

### Controls

**Added**

- Added reverse animation: left-clicking the change increment control cycles it backwards and plays its animation in
  reverse (same cooldown as right click)

**Changed**

- The interior door control now works in flight (still refused when the TARDIS is locked)

**Fixed**

- Fixed left-clicking the change increment control destroying the console

### Stattenheim Remote

**Added**

- Added overlay messages for: linked, already linked, not linked, lost, cannot link, cannot summon, inside a TARDIS,
  recharging
- Added a 20 second cooldown

**Changed**

- You can now only summon a TARDIS you own or are trusted by
- You can no longer summon a TARDIS while inside another TARDIS

### Visuals

**Added**

- Added a lamp orb layer on the exterior

**Changed**

- Rewrote the exterior glow system
- Reworked sky rendering
- Updated the Brachacki console model, texture and animation

**Fixed**

- Made water, and clouds render through the TARDIS exterior as it fades

### Language

**Added**

- Added Spanish (es_es)

**Changed**

- Renamed to "First Doctor's Door", "Second Doctor's Door", "Second Doctor's Door (Alt)", "TARDIS Top"
- Reworded locked messages and credits text

### Config

**Changed**

- Render distance and portal counts now use `Math.clamp`

**Removed**

- Removed the game rule `gc_daylight_cycle_affects_glow`

### Developer

**Added**

- Added datagen builders (`GCConsoleBuilder`, `GCTraitBuilder`, `GCPersonalityBuilder`, `GCJson`) and providers for
  consoles, variants, doors, exteriors, interiors, fades, personalities and traits
