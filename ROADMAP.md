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

---

## Coming next

| Level | Topic | Highlights |
|------:|-------|------------|
| 05 | OOP | classes, constructors, interfaces, data/enum/sealed classes, objects, value classes · *bank account, shapes, parking lot* |
| 06 | Collections | List/Set/Map, map/filter/fold/groupBy/associate/windowed, sequences · *grade analytics, word counter, inventory* |
| 07 | Functional | lambdas, higher-order functions, scope functions, inline/reified · *validation pipeline, event bus* |
| 08 | Generics | variance (in/out), constraints, star projection, reified · *Result/Either, generic repository, cache* |
| 09 | Coroutines | suspend, launch/async, Job, dispatchers, structured concurrency, cancellation, timeouts, exceptions, Mutex, channels, select, runTest · *downloader, rate-limited client, restaurant kitchen* |
| 10 | Flow | cold/hot, operators, buffer/conflate, StateFlow, SharedFlow, callbackFlow, Turbine · *stock ticker, search-as-you-type, chat room* |
| 11 | Advanced | delegation, operator overloading, DSL builders, Result, interop · *HTML DSL, config DSL, state machine* |
| 12 | Capstones | library manager CLI, weather aggregator (Ktor + Flow), task scheduler |
