# Drone Delivery
![Clojure](https://img.shields.io/badge/Clojure-%23Clojure.svg?style=for-the-badge&logo=Clojure&logoColor=Clojure)
[![CodeScene Code Health](https://codescene.io/projects/17906/status-badges/code-health)](https://codescene.io/projects/17906)

## Getting Started

These instructions will get you up and running with this project on a single host

### Prerequisites / Installing

#### OS X 

Install homebrew if not already available (https://brew.sh/)

```
$ brew tap caskroom/versions
$ brew cask install java8

$ brew install clojure
```


#### Linux

Install SDKman if not already available (https://sdkman.io/)

```
$ sdk install java
$ sdk install leiningen
```


#### Windows

Install Chocolatey if not already available (https://chocolatey.org/)

```
$ choco install jdk8

```

Download and install leiningen directly: https://leiningen.org/


### Running Tests

```
$ lein test

lein test drone-delivery.calc.delivery-test

lein test drone-delivery.calc.score-test

lein test drone-delivery.core-test

lein test drone-delivery.util-test

Ran 9 tests containing 41 assertions.
0 failures, 0 errors.
```

### Running the Solver

Obtain Help:
```
$  lein solve
  -s, --solver <SOLVER>       permuted                 Solver Strategy : ("permuted" "identity" "greedy-permuted")
  -t, --travel <TRAVEL>       euclidean                Travel Type : ("euclidean" "edge-only")
  -o, --output <OUTPUT-FILE>  resources/sample.output  Output file location
  -v                                                   verbose output
  -h, --help
 ```


Run with defaults on sample file:
```
$  lein solve resources/sample.input
```

Observer sample output:
```
$  cat sample.output
```

Run with specific travel type and solver strategy, echoing parsable solution:
```
$  lein solve resources/sample.input --solver greedy-permuted --travel edge-only -v

({:order {:order "WM004", :coords [5 11], :time 22310},
  :start-time 21600,
  :delivery-time 22560.0,
  :completion-time 23520.0,
  :rating :promoter}
 {:order {:order "WM002", :coords [2 -3], :time 18715},
  :start-time 23520.0,
  :delivery-time 23820.0,
  :completion-time 24120.0,
  :rating :promoter}
 {:order {:order "WM001", :coords [-5 11], :time 18710},
  :start-time 24120.0,
  :delivery-time 25080.0,
  :completion-time 26040.0,
  :rating :promoter}
 {:order {:order "WM003", :coords [50 7], :time 19910},
  :start-time 26040.0,
  :delivery-time 29460.0,
  :completion-time 32880.0,
  :rating :neutral})
75N
```

### Run the input generator
```
$  lein generate --help
  -m, --max-distance <integer>     10                    Maximum distance (per axis) for a delivery
  -t, --time-allocated <integer>   16                    Maximum time in hours available for delivery
  -o, --orders <integer>           16                    Number of orders per file
  -n, --number-of-files <integer>  1                     Number of files to generate
  -d, --dir <OUTPUT-DIR>           resources/generated/  Output dir for generated files
  -v                                                     verbose output
  -h, --help 
```

```
$   lein generate -v -d resources/generated/ --number-of-files 10 --orders 6 --max-distance 50 --time-allocated 2
Generating file resources/generated//generated001.input
Generating file resources/generated//generated002.input
Generating file resources/generated//generated003.input
Generating file resources/generated//generated004.input
Generating file resources/generated//generated005.input
Generating file resources/generated//generated006.input
Generating file resources/generated//generated007.input
Generating file resources/generated//generated008.input
Generating file resources/generated//generated009.input
Generating file resources/generated//generated010.input
```

### Run the benchmarker
```
$  lein benchmark -v -h
  -h, --help
  -s, --solvers <command delimited SOLVERS>  identity,permuted,greedy-permuted  Solver Strategies : ("permuted" "identity" "greedy-permuted")
  -t, --travel <TRAVEL>                      euclidean                          Travel Type : ("euclidean" "edge-only")
  -d, --dir <DIR>                            resources/generated/               input/output dir for input/generated files
  -v                                                                            verbose output
```


```
$  lein benchmark -v

  ---===== Solver ' identity ' =====---
Processing  resources/generated//generated001.input
Processing  resources/generated//generated002.input
Processing  resources/generated//generated003.input
Processing  resources/generated//generated004.input
Processing  resources/generated//generated005.input
Processing  resources/generated//generated006.input
Processing  resources/generated//generated007.input
Processing  resources/generated//generated008.input
Processing  resources/generated//generated009.input
Processing  resources/generated//generated010.input
Average score  6.6666665
"Elapsed time: 164.839318 msecs"

  ---===== Solver ' permuted ' =====---
Processing  resources/generated//generated001.input
Processing  resources/generated//generated002.input
Processing  resources/generated//generated003.input
Processing  resources/generated//generated004.input
Processing  resources/generated//generated005.input
Processing  resources/generated//generated006.input
Processing  resources/generated//generated007.input
Processing  resources/generated//generated008.input
Processing  resources/generated//generated009.input
Processing  resources/generated//generated010.input
Average score  40.0
"Elapsed time: 647.431578 msecs"

  ---===== Solver ' greedy-permuted ' =====---
Processing  resources/generated//generated001.input
Processing  resources/generated//generated002.input
Processing  resources/generated//generated003.input
Processing  resources/generated//generated004.input
Processing  resources/generated//generated005.input
Processing  resources/generated//generated006.input
Processing  resources/generated//generated007.input
Processing  resources/generated//generated008.input
Processing  resources/generated//generated009.input
Processing  resources/generated//generated010.input
Average score  36.666668
"Elapsed time: 314.078734 msecs"
```



## Assumptions 

### File Format
* Maximum of 999 entries, conforming to [0-9]{3} shown in sample file
* Order numbers are irrelevant to any required execution order
* All chars on a single line following the format specified may be ignore
* No negation of coordinate system (i.e. S-3 == N3)
* Output NPS score may be a decimal value

### Problem Statement

* Input file's timestamps represent the customer's expected time-of-order-delivery (vs. time-of-order-placement, etc..)
* Deliveries can be made head of schedule and still attain a promoter score
* NPS scoring defined in the picture represents cutoffs of
  * time < 2 Promotor
  * 4 > time >=2 Neutral
  * time >=4 Detractor
* Orders are assumed to be available all at once rather than incrementally
* Travel to order destinations is assumed to be pythagorean, although substitute calculations for edge-only travel is included
* All time-of-order-deliveries start after 6am
* There is no risk of the drone failing and not completing the current and/or subsequent trips
* There is no risk of an unexpected delay to the drone during dropoff


## Future Additions
* Add incremental processing capability (file addendums)
* Add profile(s) to use IOC to control desired strategies
* Instrument calc namespaces with spec for deeper input validation
* Convert some IOC patterns to records to allow java iterop
* Around the clock processing (currently designed on single day increments)

