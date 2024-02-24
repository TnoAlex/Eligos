# Eligos

![GitHub License](https://img.shields.io/github/license/TnoAlex/depends-smell)
![GitHub Workflow Status (with event)](https://img.shields.io/github/actions/workflow/status/TnoAlex/depends-smell/gradle-publish.yml)

_depends-smell_, a static code analysis tool for the [_Kotlin_ programming language](https://kotlinlang.org/).

### Quick Start ...

Clone and compile this repository

#### Requirements

Gradle 6.8.3+ is the minimum requirement. However, the recommended versions together with the other tools recommended
versions are:

| Gradle | Kotlin   | Java Target Level | JDK Min Version |
|--------|----------|-------------------|-----------------|
| `8.0+` | `1.9.10` | `17`              | `17`            |

### Add new language support

Extend ```AbstractSmellAnalyzer,ControlFlowBuilder ```then load your own analyzer using the standard Java spi mechanism.
If you want to add your own rule,try to extend ```Rule``` and load it by Java spi
