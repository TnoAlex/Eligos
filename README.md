# <conter class="img"><img src="./doc/imgs/icon.svg"/></center> Eligos

![GitHub License](https://img.shields.io/github/license/TnoAlex/Eligos) 

Eligos, a static code analysis tool for the [_Kotlin_ programming language](https://kotlinlang.org/).

#### Support Code Smells

In the current version, we can detect the following 11 code smell:

<table>
    <tr  align="center">
        <td><b>Type</b></td>
        <td><b>Description</b></td>
    </tr>
    <tr bgcolor="#FCF3CF"  align="center">
        <td>Circular References 🟨 </td>
        <td>Two or more classes or files have interdependencies that form a closed loop</td>
    </tr>
    <tr bgcolor="#FCF3CF"  align="center">
        <td>Excessive Parameters 🟨</td>
        <td>A method has too many arguments</td>
    </tr>
    <tr bgcolor="#FCF3CF"  align="center">
        <td>Unused Import 🟨</td>
        <td>Classes, attributes, methods, or packages that have been imported into a file, but have never been used</td>
    </tr>
    <tr bgcolor="#FCF3CF"  align="center">
        <td>Complex Method 🟨</td>
        <td>The complexity of the loop is too large</td>
    </tr>
    <tr bgcolor="#D5F5E3"  align="center">
        <td>Provide Immutable Collection 🟩</td>
        <td>Kotlin provides immutable collection types when java calling Kotlin&#39;s API</td>
    </tr>
    <tr  bgcolor="#D5F5E3"  align="center">
        <td>Internal Exposed 🟩</td>
        <td>Java inherits or implements an abstract class or interface for Internal in kotlin and extends its access to public</td>
    </tr>
    <tr  bgcolor="#D5F5E3"  align="center">
        <td>Unclear Platform Type 🟩</td>
        <td>Kotlin calls the Java return value non-null-safe API and does not specify the nullability of the return value</td>
    </tr>
    <tr bgcolor="#D2B4DE"  align="center">
        <td>When Instead Of Cascade If 🟪</td>
        <td>If statements with too many cascades should be replaced with when statements</td>
    </tr>
    <tr bgcolor="#D2B4DE"  align="center">
        <td>Implicit Single Expression Function 🟪</td>
        <td>kotlin&#39;s one-expression method returns a value of type other than Unit, but does not specify the return type</td>
    </tr>
    <tr bgcolor="#D2B4DE"  align="center">
        <td>Object Extends Throwable 🟪</td>
        <td>The class decorated with kotlin object inherits from Throwable</td>
    </tr>
    <tr bgcolor="#D2B4DE"  align="center">
        <td>Optimized Tail Recursion 🟪</td>
        <td>The tail recursion function in Kotlin does not indicate that it is tail recursive</td>
    </tr>
</table>

In the table, yellow is common to Java Kotlin, green is generated when Koltin Java calls each other, and purple is unique to Kotlin.

### Quick Start ...

1. Clone the this Repo
```bash
   git clone https://github.com/TnoAlex/Eligos.git
```
2. Prepare the environment
   Gradle 6.8.3+ is the minimum requirement. However, the recommended versions together with the other tools recommended
   versions are:

| Gradle | Kotlin   | Java Target Level | JDK Min Version |
|--------|----------|-------------------|-----------------|
| `8.0+` | `1.9.10` | `17`              | `17`            |
3. Compiler & Build
```bash
    ./gradlew build #on linux 
    ./gradlew.bat build #on windows
```
4. Find the libs
	You can find the executable cli jar at `eligos-cli/build/libs` and Installable plugin at `eligos-plugin/build/distributions`. 

### Arguments & Options

The CLI tool usage could be listed by `eligos --help`, like following:

```text
Usage: command-parser [<options>] <major language> <source path> [<result
                      output path>]

Options:
  -w, --with=<text>          Secondary languages that are analyzed in
                             collaboration with the primary language
  -ecp, --class-path=<path>  The classpath of the project to be analyzed.
                             (Default is source path and '.',If your project
                             has external jar dependencies, add the paths of
                             them)
  -jh, --jdk-home=<path>     The path of 'JAVA_HOME'. (Default is current jvm's
                             base dir)
  -kv, --kotlin-v=<text>     The version of kotlin in the project
  -jt, --jvm-target=<text>   The target of project's bytecode
  -kl, --kotlin-lib=<path>   The path of kotlin-std-lib. (Default is current
                             kotlin lib's path)
  -p, --prefix=<text>        The result file name prefix
  -f, --format=(JSON|XML|HTML|TEXT)
                             The Presentation of results
  -r, --rules=<path>         Specify the rules to use
  -D, --debug                Out put exception stack
  -Nr, --no-report           Disable reporter (debug only)
  -h, --help                 Show this message and exit

Arguments:
  <major language>      The major language to be analyzed
  <source path>         The path of source files
  <result output path>  The path to out put result
```

If yob use Third-party libraries, or use build tools like `Maven` or `Gradlle`, please set the correct `classpath` so that the tool can find these dependencies and avoid the problem that some external dependencies cannot be resolved. 

### How to add more rules

We provide a simple mechanism to extend eligos. The following steps allow you to create a rule that belongs to you: 

1. Create a Psi file processor which implement `PsiProcessor`, and define a issue which is extend `Issue` 

2. Add an Psi file listener with `@EventListener` annotation and report the issue like this:

   ```kotlin
   class MyProcessor : PsiProcessor{
       @EventLinster
       fun process(ktFile:PsiFile){
           //do someting
           context.reportIssue(MyIssue(hashSetOf(ktFile.virtualFilePath)))
       }
   }
   class MyIssue(affectedFiles:HashSet<String>) : Issue(AnalysisHierarchyEnum.FILE, affectedFiles, "My Issue", null)
   ```

3. Make your rule can scaned by the Component Manager with `@Component` annotation like this

   ```kotlin
   @Component
   class MyProcessor : PsiProcessor{
       @EventLinster
       fun process(ktFile:PsiFile){
           //do someting
       }
   }
   ```

4. If you want this rule only run on plugin, please use `@Suitable` annotation

   ```kotlin
   @Component
   @Suitable(LaunchEnvironment.IDE_PLUGIN)
   class MyProcessor : PsiProcessor{
       @EventLinster
       fun process(ktFile:PsiFile){
           //do someting
       }
   }
   ```

   

5. That's All😝. If you want to customize Eligos in more detail, please refer to the [Eligos Architecture Diagram](./doc/architecture.md) , [Execution Process](./doc/execution_process.md).

   

