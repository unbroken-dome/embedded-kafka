package org.unbrokendome.embedded.kafka.junit5

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.KafkaAdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.unbrokendome.embedded.support.findAnnotatedMethods
import org.unbrokendome.embedded.support.findAnnotation
import org.unbrokendome.embedded.support.getAnnotation
import org.unbrokendome.embedded.zookeeper.EmbeddedZookeeper
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.ServiceLoader
import java.util.function.Supplier
import org.unbrokendome.embedded.kafka.EmbeddedKafka as EmbeddedKafkaService


private val NAMESPACE = ExtensionContext.Namespace.create(EmbeddedKafkaExtension::class.java)

private const val KEY_BROKER_PROPERTIES = "brokerProperties"
private const val KEY_TOPICS = "topics"
private const val KEY_CLOSEABLES = "closeables"
private const val KEY_EXPOSE_SYSTEM_PROPERTIES = "exposeSystemProperties"


@Suppress("UNCHECKED_CAST")
private var ExtensionContext.Store.brokerProperties: Map<String, *>
    get() = get(KEY_BROKER_PROPERTIES, Map::class.java) as? Map<String, *> ?: emptyMap<String, Any?>()
    set(value) {
        put(KEY_BROKER_PROPERTIES, value)
    }


@Suppress("UNCHECKED_CAST")
private var ExtensionContext.Store.topics: Map<String, NewTopic>
    get() = get(KEY_TOPICS, Map::class.java) as? Map<String, NewTopic> ?: emptyMap()
    set(value) {
        put(KEY_TOPICS, value)
    }


val ExtensionContext.embeddedKafka: EmbeddedKafkaService
    get() = getStore(NAMESPACE)
                .get(EmbeddedKafkaService::class.java, EmbeddedKafkaService::class.java)
            .let {
                checkNotNull(it) {
                    "Embedded Kafka is not initialized"
                }
            }


val ExtensionContext.clientProperties: Map<String, Any>
    get() = mapOf(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to this.embeddedKafka.bootstrapServers)


/**
 * JUnit Jupiter extension for running an embedded Kafka broker to a test.
 *
 * In general it is preferable to use the [EmbeddedKafka] annotation instead of
 * `@ExtendsWith(EmbeddedKafkaExtension.class)`, because the former can be configured with additional properties.
 */
class EmbeddedKafkaExtension : TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private val argumentResolvers = ServiceLoader.load(ArgumentResolver::class.java)


    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        postProcessBrokerProperties(context, testInstance)
        postProcessTopics(context, testInstance)

        testInstance.javaClass.findAnnotation<EmbeddedKafka>()
                .ifPresent {
                    if (it.exposeSystemProperties) {
                        context.getStore(NAMESPACE).put(KEY_EXPOSE_SYSTEM_PROPERTIES, true)
                    }
                }
    }


    /**
     * Determines the broker properties for the current context from various sources, and stores them in the
     * ExtensionContext.
     */
    private fun postProcessBrokerProperties(context: ExtensionContext, testInstance: Any) {
        context.getStore(NAMESPACE).let { store ->

            val newBrokerProperties = findBrokerPropertiesFromClassAnnotation(testInstance) +
                    findBrokerPropertiesFromAnnotatedMethods(testInstance)

            if (newBrokerProperties.isNotEmpty()) {
                store.brokerProperties += newBrokerProperties
            }
        }
    }


    private fun findBrokerPropertiesFromClassAnnotation(testInstance: Any): Map<String, *> =
            testInstance.javaClass.findAnnotation<EmbeddedKafka>()
                    .parseBrokerProperties()


    private fun findBrokerPropertiesFromMethodAnnotation(testMethod: Method): Map<String, *> =
            testMethod.findAnnotation<EmbeddedKafka>()
                    .parseBrokerProperties()


    private fun findBrokerPropertiesFromAnnotatedMethods(testInstance: Any): Map<String, *> =
            testInstance.javaClass.findAnnotatedMethods(EmbeddedKafkaProperties::class)
                    .fold(emptyMap<String, Any?>()) { brokerProperties, method ->
                        check(Map::class.java.isAssignableFrom(method.returnType)) {
                            "A method annotated with @EmbeddedKafkaProperties must return a Map"
                        }
                        @Suppress("UNCHECKED_CAST")
                        val newProperties = method
                                .apply { isAccessible = true }
                                .invoke(testInstance.takeUnless { Modifier.isStatic(method.modifiers) }) as Map<String, Any?>

                        if (method.getAnnotation<EmbeddedKafkaProperties>()!!.merge) {
                            brokerProperties + newProperties
                        } else {
                            newProperties
                        }
                    }


    /**
     * Determines the topics to be created for the current context from various sources, and stores them in the
     * ExtensionContext.
     */
    private fun postProcessTopics(context: ExtensionContext, testInstance: Any) {
        context.getStore(NAMESPACE).let { store ->

            val newTopics = findTopicsFromClassAnnotation(testInstance) +
                    findTopicsFromAnnotatedMethods(testInstance)

            if (newTopics.isNotEmpty()) {
                store.topics += newTopics.associate { it.name() to it }
            }
        }
    }


    private fun findTopicsFromClassAnnotation(testInstance: Any): List<NewTopic> =
            testInstance.javaClass.findAnnotation<EmbeddedKafka>()
                    .getNewTopics()


    private fun findTopicsFromMethodAnnotation(testMethod: Method): List<NewTopic> =
            testMethod.findAnnotation<EmbeddedKafka>()
                    .getNewTopics()


    private fun findTopicsFromAnnotatedMethods(testInstance: Any): List<NewTopic> =
            testInstance.javaClass.findAnnotatedMethods(EmbeddedKafkaTopics::class)
                    .flatMap { method ->
                        check(Collection::class.java.isAssignableFrom(method.returnType) ||
                                CharSequence::class.java.isAssignableFrom(method.returnType) ||
                                NewTopic::class.java.isAssignableFrom(method.returnType)) {
                            "A method annotated with @EmbeddedKafkaTopics must return a NewTopic, a String or a Collection"
                        }
                        method
                                .apply { isAccessible = true }
                                .invoke(testInstance.takeUnless { Modifier.isStatic(method.modifiers) })
                                .let { result ->
                                    objectToNewTopics(result, method.getAnnotation(EmbeddedKafkaTopics::class.java))
                                }
                    }


    /**
     * Converts the return value of an [EmbeddedKafkaTopics]-annotated method to a list of [NewTopic]s.
     */
    private fun objectToNewTopics(obj: Any?, annotation: EmbeddedKafkaTopics): List<NewTopic> =
            when (obj) {
                is NewTopic ->
                    listOf(obj)
                is CharSequence ->
                    listOf(NewTopic(obj.toString(), annotation.numPartitions, annotation.replicationFactor))
                is Iterable<*> ->
                    obj.flatMap { objectToNewTopics(it, annotation) }
                null ->
                    emptyList()
                else ->
                    throw IllegalArgumentException("Result of @EmbeddedKafkaTopics method must be a NewTopic, " +
                            "a string, or a collection of those")
            }


    override fun beforeEach(context: ExtensionContext) {

        context.getStore(NAMESPACE).let { store ->

            val embeddedZookeeper = EmbeddedZookeeper()
                    .also { store.put(EmbeddedZookeeper::class.java, it) }

            val embeddedKafka = EmbeddedKafkaService(
                    zkConnectSupplier = Supplier { embeddedZookeeper.zkConnect },
                    additionalBrokerProperties = store.brokerProperties + findBrokerPropertiesFromMethodAnnotation(context.requiredTestMethod))
                    .also { store.put(EmbeddedKafkaService::class.java, it) }

            embeddedZookeeper.startAsync().awaitRunning()
            try {
                embeddedKafka.startAsync().awaitRunning()

                if (store.get(KEY_EXPOSE_SYSTEM_PROPERTIES, Boolean::class.java) == true) {
                    System.setProperty("embedded.kafka.bootstrap.servers", embeddedKafka.bootstrapServers)
                    System.setProperty("embedded.zookeeper.connect", embeddedZookeeper.zkConnect)
                }

                try {
                    (store.topics + findTopicsFromMethodAnnotation(context.requiredTestMethod).associate { it.name() to it })
                            .takeIf { it.isNotEmpty() }
                            ?.let { topics ->
                                val adminClientProps = mapOf(
                                        AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to embeddedKafka.bootstrapServers)
                                KafkaAdminClient.create(adminClientProps).use { adminClient ->
                                    adminClient.createTopics(topics.values)
                                            .all()
                                            .get()
                                }
                            }

                } catch (ex: Exception) {
                    embeddedKafka.stopAsync().awaitTerminated()
                    throw ex
                }

            } catch (ex: Exception) {
                embeddedZookeeper.stopAsync().awaitTerminated()
                throw ex
            }
        }
    }


    override fun afterEach(context: ExtensionContext) {

        context.getStore(NAMESPACE).let { store ->

            if (store.get(KEY_EXPOSE_SYSTEM_PROPERTIES, Boolean::class.java) == true) {
                System.clearProperty("embedded.kafka.bootstrap.servers")
                System.clearProperty("embedded.zookeeper.connect")
            }

            @Suppress("UNCHECKED_CAST")
            store.get(KEY_CLOSEABLES, List::class.java)
                    ?.forEach {
                        (it as? AutoCloseable)?.close()
                    }

            store.get(EmbeddedKafkaService::class.java, EmbeddedKafkaService::class.java)
                    ?.run {
                        stopAsync().awaitTerminated()
                        close()
                    }
            store.remove(EmbeddedKafkaService::class.java)

            store.get(EmbeddedZookeeper::class.java, EmbeddedZookeeper::class.java)
                    ?.run {
                        stopAsync().awaitTerminated()
                        close()
                    }
            store.remove(EmbeddedZookeeper::class.java)
        }
    }


    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
            argumentResolvers
                    .any { it.supports(parameterContext, extensionContext) }


    @Suppress("IMPLICIT_CAST_TO_ANY")
    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any? =
            argumentResolvers
                    .first { it.supports(parameterContext, extensionContext) }
                    .resolve(parameterContext, extensionContext)
                    .also {
                        (it as? AutoCloseable)?.let { extensionContext.registerCloseable(it) }
                    }
                    .let {
                        (it as? DisposableParameter<*>)?.value ?: it
                    }


    /**
     * Registers an [AutoCloseable] object that was injected as a parameter, so it can be closed after the
     * test method has completed.
     */
    private fun ExtensionContext.registerCloseable(obj: AutoCloseable) {
        getStore(NAMESPACE).let { store ->
            @Suppress("UNCHECKED_CAST")
            (store.get(KEY_CLOSEABLES, List::class.java) as? List<AutoCloseable> ?: emptyList())
                    .let { closeables ->
                        store.put(KEY_CLOSEABLES, closeables + listOf(obj))
                    }
        }
    }
}
