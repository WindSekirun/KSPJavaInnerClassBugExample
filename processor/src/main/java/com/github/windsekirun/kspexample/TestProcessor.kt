package com.github.windsekirun.kspexample

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate

@OptIn(KspExperimental::class)
class TestProcessor(
    @Suppress("unused") private val codeGenerator: CodeGenerator,
    @Suppress("unused") private val logger: KSPLogger,
    @Suppress("unused") private val options: Map<String, String>
) : SymbolProcessor {

    private val propertyActionList = mutableListOf<Action>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(SampleAnnotation::class.java.canonicalName)
            .filterIsInstance<KSPropertyDeclaration>()
            .filter { it.validate() }

        val visitor = TestVisitor()
        symbols.forEach {
            it.accept(visitor, Unit)
        }

        require(propertyActionList.isNotEmpty()) { "Can't find any property with @SampleAnnotation annotated" }

        val group = propertyActionList.groupBy { it.classDeclaration }

        group.keys.forEach { classDeclaration ->
            val properties = classDeclaration.getAllProperties()
                .filter { it.isAnnotationPresent(SampleAnnotation::class) }
                .map { internalVisitPropertyDeclaration(it) }
                .toList()

            val original = group[classDeclaration]

            if (properties != original) {
                val message = buildString {
                    appendLine("Both lists aren't equivalent.")
                    appendLine("Property obtained from visitPropertyDeclaration")
                    appendLine(original?.joinToString("\n"))
                    appendLine()
                    appendLine("Property obtained from getAllProperties")
                    appendLine(properties.joinToString("\n"))
                }

                logger.error(message)
            } else {
                logger.info(
                    "[$classDeclaration] Both lists are equivalent."
                )
            }
        }

        return symbols.filterNot { it.validate() }.toList()
    }

    private fun internalVisitPropertyDeclaration(property: KSPropertyDeclaration): Action {
        val name = property.simpleName.asString()
        val type = property.type.toString()

        require(property.parentDeclaration is KSClassDeclaration) {
            "$name need locate in class"
        }

        val classDeclaration = property.parentDeclaration as KSClassDeclaration
        return Action(classDeclaration, name, type)
    }

    inner class TestVisitor : KSVisitorVoid() {

        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
            propertyActionList += internalVisitPropertyDeclaration(property)
        }
    }

    class TestProvider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment) =
            TestProcessor(environment.codeGenerator, environment.logger, environment.options)
    }

    data class Action(val classDeclaration: KSClassDeclaration, val name: String, val type: String) {
        override fun toString() = "[${classDeclaration}] property $name:$type"
    }
}