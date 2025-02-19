@Command(name = 'create-test', description = "Creates a simple test for the project's testing framework")
@PicocliScript GroovyScriptCommand me

@Parameters(paramLabel = "TEST-NAME", description = 'The name of the test class to create')
@Field String testName

@Option(names = ['-f', '--force'], description = 'Whether to overwrite existing files')
@Field boolean overwrite

@Option(names = ['-l', '--lang'], description = 'The language used for the bean class (options: ${COMPLETION-CANDIDATES})')
@Field SupportedLanguage lang

@Mixin
@Field CommonOptionsMixin autoHelp // adds help, version and other common options to the command

private SupportedLanguage sniffProjectLanguage() {
    if (file("src/main/groovy").exists()) {
        SupportedLanguage.groovy
    } else if (file("src/main/kotlin").exists()) {
        SupportedLanguage.kotlin
    } else {
        SupportedLanguage.java
    }
}

def model = model(testName)
String artifactPath = "${model.packagePath}/${model.className}"
lang = lang ?: SupportedLanguage.findValue(config.sourceLanguage).orElse(sniffProjectLanguage())

def testFramework = config.testFramework
String testConvention = "Test"
String templateFile = "Test"

if (testFramework == "spock") {
    testConvention = "Spec"
    templateFile = "Spec"
    lang = SupportedLanguage.groovy
} else if (testFramework == "junit") {
    lang = SupportedLanguage.java
} else if (testFramework == "spek") {
    lang = SupportedLanguage.kotlin
    templateFile = "Spek"
} else if (testFramework == "kotlintest") {
    lang = SupportedLanguage.kotlin
} else if (lang == SupportedLanguage.groovy) {
    testConvention = "Spec"
    templateFile = "Spec"
}

render template: template("${lang}/${templateFile}.${lang.extension}"),
        destination: file("src/test/${lang}/${artifactPath}${testConvention}.${lang.extension}"),
        model: model,
        overwrite: overwrite

