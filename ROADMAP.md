# Roadmap

Work through the levels in order and tick each box as you finish it.
Tip: fork this repository so you can commit your exercise answers and keep track of your progress.

**For each lesson:** read it, predict the output, run it, then do the TRY IT tasks.
**For each level:** finish the lessons, then the exercises (`./gradlew exercises --tests "lNN_*"`), then the practice projects.

---

## Level 01 — Basics · [README](src/main/kotlin/l01_basics/README.md)

- [ ] 01.1 [Hello, World!](src/main/kotlin/l01_basics/lesson01_hello/HelloWorld.kt)
- [ ] 01.2 [Variables: val, var and const](src/main/kotlin/l01_basics/lesson02_variables/Variables.kt)
- [ ] 01.3 [Basic types and conversions](src/main/kotlin/l01_basics/lesson03_types/BasicTypes.kt)
- [ ] 01.4 [Strings and string templates](src/main/kotlin/l01_basics/lesson04_strings/Strings.kt)
- [ ] 01.5 [Operators](src/main/kotlin/l01_basics/lesson05_operators/Operators.kt)
- [ ] 01.6 [Reading user input](src/main/kotlin/l01_basics/lesson06_input/UserInput.kt)
- [ ] Exercises: [BasicsExercises](src/main/kotlin/l01_basics/exercises/BasicsExercises.kt) (8 tests green)
- [ ] Practice A: [Temperature converter](src/main/kotlin/l01_basics/practice/temperature/TemperatureConverter.kt)
- [ ] Practice B: [BMI calculator](src/main/kotlin/l01_basics/practice/bmi/BmiCalculator.kt)

## Level 02 — Control flow · [README](src/main/kotlin/l02_control_flow/README.md)

- [ ] 02.1 [if as an expression](src/main/kotlin/l02_control_flow/lesson01_if/IfExpression.kt)
- [ ] 02.2 [when: Kotlin's super switch](src/main/kotlin/l02_control_flow/lesson02_when/When.kt)
- [ ] 02.3 [Ranges and progressions](src/main/kotlin/l02_control_flow/lesson03_ranges/Ranges.kt)
- [ ] 02.4 [Loops: for, while, do-while, repeat](src/main/kotlin/l02_control_flow/lesson04_loops/Loops.kt)
- [ ] 02.5 [break, continue and labels](src/main/kotlin/l02_control_flow/lesson05_break_continue/BreakContinueLabels.kt)
- [ ] Exercises: [ControlFlowExercises](src/main/kotlin/l02_control_flow/exercises/ControlFlowExercises.kt) (8 tests green)
- [ ] Practice A: [FizzBuzz three ways](src/main/kotlin/l02_control_flow/practice/fizzbuzz/FizzBuzz.kt)
- [ ] Practice B: [Number guessing game](src/main/kotlin/l02_control_flow/practice/guessing/NumberGuessingGame.kt)
- [ ] Practice C: [Grade calculator](src/main/kotlin/l02_control_flow/practice/grades/GradeCalculator.kt)

## Level 03 — Functions · [README](src/main/kotlin/l03_functions/README.md)

- [ ] 03.1 [Function basics](src/main/kotlin/l03_functions/lesson01_basics/FunctionBasics.kt)
- [ ] 03.2 [Default and named arguments](src/main/kotlin/l03_functions/lesson02_default_named/DefaultAndNamedArgs.kt)
- [ ] 03.3 [vararg and the spread operator](src/main/kotlin/l03_functions/lesson03_vararg/VarargAndSpread.kt)
- [ ] 03.4 [Local functions, Unit and Nothing](src/main/kotlin/l03_functions/lesson04_local_unit_nothing/LocalFunctionsUnitNothing.kt)
- [ ] 03.5 [Extension functions and properties](src/main/kotlin/l03_functions/lesson05_extensions/ExtensionFunctions.kt)
- [ ] 03.6 [Infix functions and tail recursion](src/main/kotlin/l03_functions/lesson06_infix_tailrec/InfixAndTailrec.kt)
- [ ] Exercises: [FunctionsExercises](src/main/kotlin/l03_functions/exercises/FunctionsExercises.kt) (8 tests green)
- [ ] Practice A: [A tiny String utility library](src/main/kotlin/l03_functions/practice/stringutils/StringUtils.kt)

## Level 04 — Null safety · [README](src/main/kotlin/l04_null_safety/README.md)

- [ ] 04.1 [Nullable types: String vs String?](src/main/kotlin/l04_null_safety/lesson01_nullable/NullableTypes.kt)
- [ ] 04.2 [Safe calls (?.) and the Elvis operator (?:)](src/main/kotlin/l04_null_safety/lesson02_safe_call/SafeCallAndElvis.kt)
- [ ] 04.3 [The not-null assertion (!!) and better alternatives](src/main/kotlin/l04_null_safety/lesson03_not_null/NotNullAssertion.kt)
- [ ] 04.4 [?.let and smart casts](src/main/kotlin/l04_null_safety/lesson04_let_smart_cast/LetAndSmartCasts.kt)
- [ ] 04.5 [lateinit and lazy](src/main/kotlin/l04_null_safety/lesson05_lateinit_lazy/LateinitAndLazy.kt)
- [ ] 04.6 [Platform types: null safety at the Java border](src/main/kotlin/l04_null_safety/lesson06_platform/PlatformTypes.kt)
- [ ] 04.7 [Safe casts (as?) and nulls in collections](src/main/kotlin/l04_null_safety/lesson07_safe_casts/SafeCastsAndCollections.kt)
- [ ] Exercises: [NullSafetyExercises](src/main/kotlin/l04_null_safety/exercises/NullSafetyExercises.kt) (8 tests green)
- [ ] Practice A: [Safe input parser](src/main/kotlin/l04_null_safety/practice/safeinput/SafeInputParser.kt)
- [ ] Practice B: [The Crash Museum](src/main/kotlin/l04_null_safety/practice/crashmuseum/CrashMuseum.kt)

## Level 05 — OOP · [README](src/main/kotlin/l05_oop/README.md)

- [ ] 05.1 [Classes and objects](src/main/kotlin/l05_oop/lesson01_classes/ClassesAndObjects.kt)
- [ ] 05.2 [Constructors and init blocks](src/main/kotlin/l05_oop/lesson02_constructors/Constructors.kt)
- [ ] 05.3 [Properties: getters, setters and backing fields](src/main/kotlin/l05_oop/lesson03_properties/Properties.kt)
- [ ] 05.4 [Visibility and encapsulation](src/main/kotlin/l05_oop/lesson04_visibility/Visibility.kt)
- [ ] 05.5 [Inheritance and abstract classes](src/main/kotlin/l05_oop/lesson05_inheritance/Inheritance.kt)
- [ ] 05.6 [Interfaces](src/main/kotlin/l05_oop/lesson06_interfaces/Interfaces.kt)
- [ ] 05.7 [Data classes](src/main/kotlin/l05_oop/lesson07_data_classes/DataClasses.kt)
- [ ] 05.8 [Objects and companion objects](src/main/kotlin/l05_oop/lesson08_objects/ObjectsAndCompanions.kt)
- [ ] 05.9 [Enum classes and sealed types](src/main/kotlin/l05_oop/lesson09_enums_sealed/EnumsAndSealed.kt)
- [ ] 05.10 [Nested and inner classes, value classes, Kotlin vs Java](src/main/kotlin/l05_oop/lesson10_nested_value_java/NestedValueAndJava.kt)
- [ ] Exercises: [OopExercises](src/main/kotlin/l05_oop/exercises/OopExercises.kt) (10 tests green)
- [ ] Practice A: [Bank account system](src/main/kotlin/l05_oop/practice/bank/BankAccount.kt)
- [ ] Practice B: [Shapes with a sealed hierarchy](src/main/kotlin/l05_oop/practice/shapes/Shapes.kt)
- [ ] Practice C: [Parking lot](src/main/kotlin/l05_oop/practice/parking/ParkingLot.kt)

## Level 06 — Collections · [README](src/main/kotlin/l06_collections/README.md)

- [ ] 06.1 [Just enough lambdas for collections](src/main/kotlin/l06_collections/lesson01_lambda_primer/LambdaPrimer.kt)
- [ ] 06.2 [Lists](src/main/kotlin/l06_collections/lesson02_lists/Lists.kt)
- [ ] 06.3 [Sets](src/main/kotlin/l06_collections/lesson03_sets/Sets.kt)
- [ ] 06.4 [Maps](src/main/kotlin/l06_collections/lesson04_maps/Maps.kt)
- [ ] 06.5 [Transformations: map, flatMap, zip, associate](src/main/kotlin/l06_collections/lesson05_transformations/Transformations.kt)
- [ ] 06.6 [Filtering and predicates](src/main/kotlin/l06_collections/lesson06_filtering/Filtering.kt)
- [ ] 06.7 [Aggregation and grouping](src/main/kotlin/l06_collections/lesson07_aggregation/Aggregation.kt)
- [ ] 06.8 [Sorting](src/main/kotlin/l06_collections/lesson08_sorting/Sorting.kt)
- [ ] 06.9 [Sequences: lazy collections](src/main/kotlin/l06_collections/lesson09_sequences/Sequences.kt)
- [ ] 06.10 [Pitfalls and immutability](src/main/kotlin/l06_collections/lesson10_pitfalls/CollectionPitfalls.kt)
- [ ] Exercises: [CollectionsExercises](src/main/kotlin/l06_collections/exercises/CollectionsExercises.kt) (10 tests green)
- [ ] Practice A: [Student grade analytics](src/main/kotlin/l06_collections/practice/grades/StudentGradeAnalytics.kt)
- [ ] Practice B: [Word frequency counter](src/main/kotlin/l06_collections/practice/words/WordFrequency.kt)
- [ ] Practice C: [Inventory report](src/main/kotlin/l06_collections/practice/inventory/InventoryReport.kt)

---

## Coming next

| Level | Topic | Highlights |
|------:|-------|------------|
| 07 | Functional | lambdas, higher-order functions, scope functions, inline/reified · *validation pipeline, event bus* |
| 08 | Generics | variance (in/out), constraints, star projection, reified · *Result/Either, generic repository, cache* |
| 09 | Coroutines | suspend, launch/async, Job, dispatchers, structured concurrency, cancellation, timeouts, exceptions, Mutex, channels, select, runTest · *downloader, rate-limited client, restaurant kitchen* |
| 10 | Flow | cold/hot, operators, buffer/conflate, StateFlow, SharedFlow, callbackFlow, Turbine · *stock ticker, search-as-you-type, chat room* |
| 11 | Advanced | delegation, operator overloading, DSL builders, Result, interop · *HTML DSL, config DSL, state machine* |
| 12 | Capstones | library manager CLI, weather aggregator (Ktor + Flow), task scheduler |
