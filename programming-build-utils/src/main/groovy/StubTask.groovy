import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputFiles

class StubTask extends JavaExec {
    @InputFiles
    def input = project.sourceSets.main.java.srcDirs
    @OutputFiles
    def output = project.fileTree(project.sourceSets.main.output.classesDir.parent)

    def StubTask() {
        project.jar.dependsOn this
        setMain('ant.AntStubGenerator$Main')
        setClasspath((project.rootProject.buildscript.configurations.classpath + project.sourceSets.main.runtimeClasspath))
        setArgs([ input.toArray()[0], project.sourceSets.main.output.classesDir])
        logging.captureStandardOutput LogLevel.INFO
    }

    def setClasses(List classes){
        setArgs(getArgs() + classes)
    }
}